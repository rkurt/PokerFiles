package pl.runit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static final String DIR = "C:\\PokerFiles";
    private static final String PATTERN = "PokerMaster .+ \\(\\D+(.*)/\\D+(.*)\\) .+";
    private static final String FILE_TYPE = "txt";
    private static final String SUMMARY = "_summary";
    private static final Map<String, Integer> countMap = new HashMap<>();
    private static FileWriter writer;

    public static void main(String[] args) throws Exception {
        try {
            writer = createFile();
            helloMessage();
            readFiles();
            byeMessage();
        } catch (Exception e) {
            writer.write("\n\r\n\rERROR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            writer.close();
        }
    }

    private static FileWriter createFile() throws IOException {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        final String formatDateTime = LocalDateTime.now().format(formatter);
        final File file = new File(SUMMARY + "_" + formatDateTime + "." + FILE_TYPE);
        return new FileWriter(file);
    }

    private static void helloMessage() throws IOException {
        writer.write("PokerFiles START!\n\r\n\r");
        System.out.println("PokerFiles START!");
    }

    private static void byeMessage() throws IOException {
        writer.write("\n\r\n\rPokerFiles DONE!\n\r\n\r");
        System.out.println("PokerFiles DONE!");

        countMap.forEach((key, value) -> {
            try {
                writer.write("\n\rFolder " + key + " contains " + value + (value != 1 ? " files" : " file") + "\n\r");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        writer.close();
    }

    private static void readFiles() throws IOException {
        final File folder = new File(DIR);
        final File[] listOfFiles = folder.listFiles();

        if (listOfFiles == null || listOfFiles.length == 0) {
            writer.write("No poker files in the folder " + DIR);
            return;
        }

        int nrOfFiles = calculateNrOfFiles(listOfFiles);
        int i = 0;
        for (File file : listOfFiles) {
            if (isProperFile(file)) {
                System.out.println("File " + ++i + "/" + nrOfFiles + " " + file.getName());

                final String bid = readFileBid(file);
                if (bid == null) {
                    continue;
                }

                final Path path = createFolder(bid);
                moveFile(file, path);
                countFile(bid);
            }
        }
    }

    private static int calculateNrOfFiles(File[] listOfFiles) {
        int nrOfFiles = 0;
        for (File file : listOfFiles) {
            if (isProperFile(file)) {
                nrOfFiles++;
            }
        }

        return nrOfFiles;
    }

    private static void countFile(final String bid) {
        int count = 0;
        if (countMap.containsKey(bid)) {
            count = countMap.get(bid);
        }
        countMap.put(bid, count + 1);
    }

    private static void moveFile(final File file, final Path destination) throws IOException {
        final File newFile = new File(destination + "\\" + file.getName());
        if (!newFile.exists()) {
            Files.copy(file.toPath(), newFile.toPath());
        }
    }

    private static Path createFolder(String bid) throws IOException {
        Path path = Paths.get(DIR + "\\" + bid);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        return path;
    }

    private static String readFileBid(final File file) throws IOException {
        final BufferedReader br = new BufferedReader(new FileReader(file));
        final String firstLine = br.readLine();

        if (firstLine == null || firstLine.isEmpty()) {
            writer.write("\n\rERROR: File " + file.getName() + " is empty!\n\r");
            return null;
        }

        return readBid(file, firstLine);
    }

    private static String readBid(final File file, final String line) throws IOException {
        Pattern r = Pattern.compile(PATTERN);
        Matcher m = r.matcher(line);
        boolean found = m.find();

        if (!found || m.group(1) == null || m.group(1).isEmpty()) {
            writer.write("\n\rERROR: File " + file.getName() + " has no bid!\n\r");
            return null;
        }

        return m.group(1) + "_" + m.group(2);
    }

    private static boolean isProperFile(final File file) {
        return file.isFile() && (file.getName().substring(file.getName().lastIndexOf('.') + 1).equals(FILE_TYPE)) && !file.getName().startsWith(SUMMARY);
    }
}

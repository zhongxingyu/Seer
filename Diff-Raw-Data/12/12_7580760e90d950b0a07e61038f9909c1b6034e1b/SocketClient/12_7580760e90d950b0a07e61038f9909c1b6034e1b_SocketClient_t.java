 import java.io.*;
 import java.net.Socket;
 import java.net.SocketException;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.StandardOpenOption;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 import static util.SocketManager.*;
 
 public class SocketClient {
     private static final boolean FILE_INPUT_ENABLED = false;
     private static final String INPUT_FILE_NAME = "";
     private static final String OUTPUT_FILE_NAME = "";
 
     private static int wordInFile = 0;
 
 
     public static void main(String args[]) throws IOException {
         Socket s = new Socket("localhost", 9999);
         InputStream is = s.getInputStream();
         OutputStream os = s.getOutputStream();
         String wordToSend;
         String wordReceived;
 
         outputInstructions();
 
         while (true) {
             wordToSend = getFirstWordFromConsole();
             if (wordToSend != null) {
                 wordToSend = manageCommands(wordToSend);
                 outputWordToSend(wordToSend);
                 sendWord(os, wordToSend);
 
                 wordReceived = receiveWord(is);
                 outputReceivedWord(wordReceived);
             }
         }
     }
 
     private static void outputInstructions() {
         System.out.println("Socket Client Application\nEnter any string or 'quit' to exit...");
         if (FILE_INPUT_ENABLED) System.out.println("Enter 'file' to read from file");
     }
 
     private static String manageCommands(String wordToSend) throws IOException {
         if (wordToSend.equals("quit")) throw new SocketException("Exiting application");
         if (wordToSend.equals("file") && FILE_INPUT_ENABLED) {
             wordToSend = readTextFile(INPUT_FILE_NAME).get(wordInFile);
             wordInFile++;
         }
         return wordToSend;
     }
 
     private static void outputWordToSend(String wordToSend) {
         System.out.println(">  " + wordToSend);
     }
 
     private static void outputReceivedWord(String wordReceived) throws IOException {
         System.out.println(">> " + wordReceived);
         if (FILE_INPUT_ENABLED) writeTextFile(OUTPUT_FILE_NAME, wordReceived);
     }
 
     private static String getFirstWordFromConsole() throws IOException {
         byte consoleInputBytes[] = new byte[256];
        StringTokenizer stringTokenizer;
        int length;
 
        length = System.in.read(consoleInputBytes);
        if (length != 1) {
            stringTokenizer = new StringTokenizer(new String(consoleInputBytes, 0), "\r\n");
            return (String) stringTokenizer.nextElement();
         } else return null;
 
     }
 
 
 
     static List<String> readTextFile(String aFileName) throws IOException {
         List<String> result = new ArrayList<>();
 
         Path path = Paths.get(aFileName);
         try (BufferedReader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"))) {
             String line;
             while ((line = reader.readLine()) != null) {
                 result.add(line);
             }
         }
 
         return result;
     }
 
     static void writeTextFile(String aFileName, String line) throws IOException {
         Path path = Paths.get(aFileName);
         try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"), StandardOpenOption.APPEND)) {
             writer.append(line);
             writer.flush();
         }
     }
 }

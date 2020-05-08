 import java.io.*;
 import java.net.Socket;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.StandardOpenOption;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 public class SocketClient {
     private static final boolean FILE_INPUT_ENABLED = false;
    private static final String DEFAULT_FILE_NAME = "";
 
 
     public static void main(String args[]) throws IOException {
 
         byte consoleInputBytes[] = new byte[256];
         Socket s = new Socket("localhost", 9999);
         InputStream is = s.getInputStream();
         OutputStream os = s.getOutputStream();
         byte buf[];
         int length;
         String wordToSend;
         String wordReceived;
         StringTokenizer sendTokenizer;
 
         System.out.println("Socket Client Application\nEnter any string or 'quit' to exit...\nEnter 'file' to read from file");
 
         while (true) {
             length = System.in.read(consoleInputBytes);
             if (length != 1) {
 
                 sendTokenizer = new StringTokenizer(new String(consoleInputBytes, 0), "\r\n");
                 wordToSend = ((String) sendTokenizer.nextElement()).intern();
 
                 if(wordToSend.equals("quit"))break;
                if(wordToSend.equals("file")) wordToSend = readTextFile(DEFAULT_FILE_NAME).get(0);
 
                 System.out.println(">  " + wordToSend);
 
                 os.write(consoleInputBytes, 0, length);
                 os.flush();
 
                 buf = new byte[512];
                 length = is.read(buf);
                 if (length == -1)
                     break;
 
                 sendTokenizer = new StringTokenizer(new String(buf, 0), ""+(char)0);
                 wordReceived = ((String) sendTokenizer.nextElement()).intern();
                 System.out.println(">> " + wordReceived);
                if(FILE_INPUT_ENABLED) writeTextFile(DEFAULT_FILE_NAME,wordReceived);
             }
         }
         is.close();
         os.close();
         s.close();
     }
 
 
     static List<String> readTextFile(String aFileName) throws IOException {
         List<String> result = new ArrayList<>();
 
         Path path = Paths.get(aFileName);
         try (BufferedReader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"))){
             String line;
             while ((line = reader.readLine()) != null) {
                 result.add(line);
             }
         }
 
         return result;
     }
 
     static void writeTextFile(String aFileName, String line) throws IOException {
         Path path = Paths.get(aFileName);
         try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"), StandardOpenOption.APPEND)){
             writer.append(line);
             writer.flush();
         }
     }
 }

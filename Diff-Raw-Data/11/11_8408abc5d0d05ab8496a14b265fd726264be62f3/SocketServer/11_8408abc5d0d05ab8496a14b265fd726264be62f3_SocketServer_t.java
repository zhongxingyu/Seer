 import wordchecker.ConsonantChecker;
 import wordchecker.WordChecker;
 import wordchecker.WordCheckerFactory;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.StringTokenizer;

 import static util.SocketManager.receiveWord;
import static util.SocketManager.sendWord;
 
 public class SocketServer {
     private final static boolean DIFFERENT_MODES_ENABLED = false;
 
     public static void main(String args[]) throws IOException {
         ServerSocket serverSocket = new ServerSocket(9999);
         Socket socket = serverSocket.accept();
         InputStream is = socket.getInputStream();
         OutputStream os = socket.getOutputStream();
         String result;
         String processMode;
         String stringReceived;
         String word;
         WordChecker checker;
         StringTokenizer modeTokenizer;
 
         System.out.println("Socket Server Application");
 
         while (true) {
             stringReceived = receiveWord(is);
             if (stringReceived != null) {
 
                 if (DIFFERENT_MODES_ENABLED) {
                     modeTokenizer = new StringTokenizer(stringReceived, "/");
                     processMode = (String) modeTokenizer.nextElement();
                     word = (String) modeTokenizer.nextElement();
                     checker = WordCheckerFactory.getWordChecker(processMode);
                 } else {
                     word = stringReceived;
                     checker = new ConsonantChecker();
                 }
 
                 result = checker.check(word);
 
                 System.out.println(">  " + result);
                sendWord(os, result);
             }
         }
     }
 
 }

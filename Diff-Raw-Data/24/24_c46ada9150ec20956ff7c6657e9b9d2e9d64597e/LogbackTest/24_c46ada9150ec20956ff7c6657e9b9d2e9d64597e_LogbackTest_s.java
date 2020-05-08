 import java.io.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 class LogbackTest {
     public static void main(String[] args) {
         Logger logger = LoggerFactory.getLogger(LogbackTest.class);
         BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
         String message;
         try {
             while((message = stdin.readLine()) != null) {
                 System.out.println("Sending debug message: " + message);
                 logger.debug(message);
             }
         } catch(Exception e) {
            System.out.println("Caught exception: " + e);
         }
     }
 }

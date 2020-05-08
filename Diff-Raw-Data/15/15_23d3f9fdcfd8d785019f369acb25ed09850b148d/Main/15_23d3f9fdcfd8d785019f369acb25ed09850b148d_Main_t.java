 
 import Parser.LoginParser;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 /**
  *
  * @author WaiToNZa
  */
 public class main {
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws IOException, InterruptedException {
         LoginParser parser = new LoginParser("b5310XXXXXX", "password", "0");
 
         while (true) {
             try {
                 Thread.sleep(10000);
                 System.out.println("Checking if there is internet");
                if (InternetChecker.getDefaultInstance().isConnectionPresent(true)) {
                     System.out.println("Internet connection is present");
                 } else {
                     System.out.println("Internet connection not found. Trying to login");
                     parser.login();
                 }
             } catch (Exception ex) {
                 System.out.println("Connection Failed to login");
             }
         }
     }
 }

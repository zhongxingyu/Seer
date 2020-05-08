 package merkel.hellman;
 
 import java.io.IOException;
import java.io.InputStream;
 import java.util.Scanner;
 
 /**
  *
  * @author simon
  */
 public class MerkelHellman {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
 
         // Parse commandline args & dispatch function
         if (args.length < 1) {
             help();
         } else {
             // Join args in a string
             String arguments = "";
             for (String s : args) {
                 arguments += s + " ";
             }
 
             // Dispatch function
             if (arguments.contains("encrypt")) {
                 encrypt();
             } else if (arguments.contains("decrypt")) {
                 decrypt();
             } else if (arguments.contains("help")) {
                 help();
             } else if (arguments.contains("test")) {
                 test();
             }
         }
     }
 
     public static void test() {
         // init
         int[] key = {1,2,4,8,16,32,64,128,256,512,1024,2048,4096,8192,16384,32768};
         System.out.println("Sum of key: " + U.calculate(key));
 //        Crypto crypto = new Crypto();
 //        // test
 //        boolean pass = crypto.test();
 //
 //        U.p("\n\n\n======================");
 //        if (pass) {
 //            U.p(":) P A S S E D!");
 //        } else {
 //            U.p(":( F A I L E D!");
 //        }
     }
 
     public static void help() {
         System.out.println("Merkel-Hellman Usage:\n"
                 + "help\n"
                 + "  java -jar Merkel-Hellman.jar help\n"
                 + "encrypt\n"
                 + "  cat plaintext | java -jar Merkel-Hellman.jar > encrypted\n"
                 + "decrypt\n"
                 + "  cat encrypted | java -jar Merkel-Hellman.jar > decrypted");
     }
 
     public static void encrypt() {
         Crypto crypto = new Crypto();
         
         try {
             // Normal 2 byte encrypt
            byte[] block = new byte[2];
            int bytesRead = System.in.read(block);
             while (bytesRead == 2) {
                 System.out.println(crypto.encryptBlock(block));
                bytesRead = System.in.read(block);
             }
             // Pad input block if not large enough for block
             boolean moreBytesRead = false;
             if (bytesRead > 0) {
                 moreBytesRead = true;
                for (int i = bytesRead; i < 2; ++i) {
                     block[i] = 0;
                 }
                 System.out.println(crypto.encryptBlock(block));
             }
         } catch (IOException e) {
             System.out.println("I/O Exception caught.");
         }
     }
 
     public static void decrypt() {
         Scanner scan = null;
         try {
             Crypto crypto = new Crypto();
             scan = new Scanner(System.in);
             String input;
             while (scan.hasNext()) {
                 input = scan.nextLine();
                 int[] encryptedint = new int[1];
                 encryptedint[0] = Integer.parseInt(input);
                 byte[] decryptedData = crypto.decrypt(encryptedint);
                 String decryptedStr = new String(decryptedData);
 
                 System.out.print(decryptedStr);
             }
         } finally {
             if (scan != null) {
                 scan.close();
             }
         }
     }
 }

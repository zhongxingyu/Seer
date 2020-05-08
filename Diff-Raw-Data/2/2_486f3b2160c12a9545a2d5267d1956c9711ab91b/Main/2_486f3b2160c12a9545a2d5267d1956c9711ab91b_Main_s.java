 package vigenere;
 
 import java.io.FileReader;
 import java.util.Scanner;
 
 /**
  *
  * @author JohannesH
  */
 public class Main {
 
     /**
      * @param args 
      */
     public static void main(String[] args) {
         String message = "";
         try {
            Scanner scan = new Scanner(new FileReader("text.txt"));
             while (scan.hasNext()) {
                 message += scan.next();
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         
         CryptHandler ch = new CryptHandler();
         String enc,dec;
         enc = ch.encrypt(message, "nyckelsomärlång");
         dec = ch.decrypt(enc, "nyckelsomärlång");
         System.out.println("dec lenght: " + dec.length());
         System.out.println("Encrypted: " + enc);
         System.out.println("Decrypted: " + dec);    
     }
 }

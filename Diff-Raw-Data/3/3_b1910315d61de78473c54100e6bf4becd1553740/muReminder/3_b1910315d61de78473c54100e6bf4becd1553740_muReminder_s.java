 import java.io.IOException;
 import java.util.Scanner;
 import sun.misc.BASE64Decoder;
 import sun.misc.BASE64Encoder;
 
 // muReminder - decoder for stored muCommanders passwords, @author Konstantin Kuchinin
 // Contains XOR function and key from MuCommander source code http://www.mucommander.com/
 // Copyright (C) 2002-2013 Maxence Bernard
 
 public class muReminder {
 
 	/** Long enough key (256 bytes) to avoid having too much redundancy in small text strings. */
     public final static int NOT_SO_PRIVATE_KEY[] = {
         161, 220, 156, 76, 177, 174, 56, 37, 98, 93, 224, 19, 160, 95, 69, 140,
         91, 138, 33, 114, 248, 57, 179, 17, 54, 172, 249, 58, 26, 181, 167, 231,
         241, 185, 218, 174, 37, 102, 100, 26, 16, 214, 119, 29, 118, 151, 135, 175,
         245, 247, 160, 188, 77, 173, 109, 255, 73, 44, 186, 211, 117, 236, 204, 58,
         246, 210, 128, 33, 234, 218, 82, 188, 78, 229, 180, 108, 247, 200, 3, 142,
         206, 45, 165, 111, 96, 72, 76, 81, 238, 186, 240, 167, 185, 152, 68, 228,
         87, 142, 145, 7, 74, 12, 106, 94, 15, 218, 155, 71, 87, 136, 58, 40,
         246, 94, 7, 89, 29, 0, 78, 204, 70, 220, 240, 127, 59, 184, 109, 106
     };
 
     /**
      * Cyphers the given byte array using XOR symmetrical encryption with a static hard-coded key.
      *
      * @param b the byte array to encrypt/decrypt
      * @return the encrypted/decrypted byte array
      */
 
     private static byte[] xor(byte[] b) {
         int len = b.length;
         int keyLen = NOT_SO_PRIVATE_KEY.length;
 
         byte[] result = new byte[len];
         for(int i=0; i<len; i++)
             result[i] = (byte)(b[i]^NOT_SO_PRIVATE_KEY[i%keyLen]);
 
         return result;
     }
 
   public static void main(String[] args) {
  	System.out.println("* muReminder - decoder for stored muCommanders passwords, written by @coocheenin *");
   	System.out.println("* For encrypted pass see credentials.xml file *");
   	System.out.println("Enter encrypted pass: ");
   	Scanner input = new Scanner(System.in);
     BASE64Decoder d = new BASE64Decoder();
     String encodedPass;
 
     try {
     encodedPass = input.nextLine();
     byte[] encryptedPass = d.decodeBuffer(encodedPass);
     System.out.println("Your pass is: " + new String(xor(encryptedPass)));
     } catch (IOException e) {
       e.printStackTrace();
     }
   }
 
 }

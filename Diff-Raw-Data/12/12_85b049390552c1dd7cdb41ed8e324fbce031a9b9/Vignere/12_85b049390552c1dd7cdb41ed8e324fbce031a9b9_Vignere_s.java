 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package vignerecipher;
 
 /**
  *
  * @author gmochid
  */
 public class Vignere {
     /**
      * enchipher plaintext using VignereCipher and default key
      * @param plaintext meaningful text
      * @return ciphertext
      */
     public static String encipher(String plaintext) {
         return encipher(plaintext, "abcdefghijklmnopqrstuvwxyz");
     }
 
     /**
      * enchipher plaintext using VignereCipher and specified key
      * @param plaintext meaningful text
      * @param key key that used to encipher
      * @return ciphertext
      */
     public static String encipher(String plaintext, String key) {
         plaintext = plaintext.toLowerCase();
         key = key.toLowerCase();
 
         StringBuilder sb = new StringBuilder();
         int len = key.length();
         int j = 0;
         for (int i = 0; i < plaintext.length(); i++) {
             if(plaintext.charAt(i) == ' ') {
                 continue;
             }
             sb.append(
                     (char) ((alpha(plaintext.charAt(i)) + (alpha(key.charAt(j++ % len)))) % 26 + (int) 'a')
                     );
         }
         return sb.toString();
     }
 
     public static String decipher(String cipher) {
         return decipher(cipher, "abcdefghijklmnopqrstuvwxyz");
     }
 
     /**
      * dechipher ciphertext using VignereCipher and specified key
      * @param cipher text that has no meaning
      * @param key a key to dechipher plaintext
      * @return plaintext
      */
     public static String decipher(String cipher, String key) {
         cipher.toLowerCase();
         key.toLowerCase();
 
         StringBuilder sb = new StringBuilder();
         int len = key.length();
         int j = 0;
         for (int i = 0; i < cipher.length(); i++) {
             if(cipher.charAt(i) == ' ') {
                 continue;
             }
             int x = (alpha(cipher.charAt(i)) - (alpha(key.charAt(j++ % len)))) % 26;
             x = x < 0 ? x + 26 : x ;
             sb.append(
                     (char) (x + (int) 'a')
                     );
         }
         return sb.toString();
     }
 
     public static String encipher256(String plaintext) {
         return encipher256(plaintext, "abcdefghijklmnopqrstuvwxyz");
     }
 
     public static String encipher256(String plaintext, String key) {
         StringBuilder sb = new StringBuilder();
         int len = key.length();
         for (int i = 0; i < plaintext.length(); i++) {
             sb.append((char)((plaintext.charAt(i) + key.charAt(i % len)) % 256));
         }
         return sb.toString();
     }
 
     public static String decipher256(String cipher) {
         return decipher256(cipher, "abcdefghijklmnopqrstuvwxyz");
     }
 
     public static String decipher256(String cipher, String key) {
         StringBuilder sb = new StringBuilder();
         int len = key.length();
         for (int i = 0; i < cipher.length(); i++) {
             int x = ((cipher.charAt(i) - key.charAt(i % len)) % 256);
             x = x < 0 ? x + 26 : x ;
             sb.append((char)x);
         }
         return sb.toString();
     }
 
     private static char alpha(char x) {
         return (char) (x - 'a');
     }
 
     public static void main(String[] args) {
         String s = "Paskasius Wahyu Wibisono";
         String cipher = encipher256(s);
         System.out.println(cipher);
         System.out.println(decipher256(cipher));
     }
 }

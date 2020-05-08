 package utility;
 
 import java.security.*;
 
 /**
  *
 * @author Simon
  */
 public class MD5Hasher {
 
     public static String hashMD5(String hash) throws NoSuchAlgorithmException {
 
         byte[] inputBytes = hash.getBytes();
         String output = null;
 
         try {
             MessageDigest md = MessageDigest.getInstance("MD5");
             md.reset();
             md.update(inputBytes);
             byte msgDigest[] = md.digest();
 
             StringBuilder hexString = new StringBuilder();
             for (int i = 0; i < msgDigest.length; i++) {
                 hexString.append(Integer.toHexString(
                         (msgDigest[i] & 0xFF) | 0x100).toLowerCase().substring(1, 3));
             }
             output = hexString.toString();
         } catch (NoSuchAlgorithmException ex) {
         }
         return output;
     }
 }

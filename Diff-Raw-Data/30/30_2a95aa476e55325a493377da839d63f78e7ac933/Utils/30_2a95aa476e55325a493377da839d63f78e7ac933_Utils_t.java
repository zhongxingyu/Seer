 import java.io.FileInputStream;
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Arrays;
 
 import org.bouncycastle.crypto.digests.SHA256Digest;
 
 class Utils {
   private static String digits = "0123456789abcdef";
 
   /**
    * Return length many bytes of the passed in byte array as a hex string.
    * 
    * @param data
    *          the bytes to be converted.
    * @param length
    *          the number of bytes in the data block to be converted.
    * @return a hex representation of length bytes of data.
    */
   public static String toHex(byte[] data, int length) {
     StringBuffer buf = new StringBuffer();
     for (int i = 0; i != length; i++) {
       int v = data[i] & 0xff;
       buf.append(digits.charAt(v >> 4));
       buf.append(digits.charAt(v & 0xf));
     }
     return buf.toString();
   }
 
   /**
    * Return the passed in byte array as a hex string.
    * 
    * @param data
    *          the bytes to be converted.
    * @return a hex representation of data.
    */
   public static String toHex(byte[] data) {
     return toHex(data, data.length);
   }
 
   public static String toString(byte[] data) {
     StringBuilder output = new StringBuilder();
     for (int i = 0; i < data.length; i += 2) {
       String str = data[i] + "" + data[i + 1];
       output.append((char) Integer.parseInt(str, 16));
     }
     return output.toString();
   }
 
   public static byte[][] splitArray(byte[] array, int size) {
     byte[][] ans = new byte[2][];
     ans[0] = Arrays.copyOfRange(array, 0, size);
     ans[1] = Arrays.copyOfRange(array, size, array.length);
     return ans;
   }
 
   public static byte[] bouncyHash(FileInputStream fis) {
     try {
       SHA256Digest digest = new SHA256Digest();
       byte[] result = new byte[digest.getDigestSize()];
       byte[] chunk = new byte[Main.KEY_SIZE];
       int x;
       while ((x = fis.read(chunk)) != -1) {
         digest.update(chunk, 0, x);
       }
       digest.doFinal(result, 0);
       return result;
     } catch (Exception e) {
       e.printStackTrace();
     }
     return null;
   }
 
   public static byte[] generateHash(FileInputStream fis) throws NoSuchAlgorithmException {
     try {
       MessageDigest md = MessageDigest.getInstance("SHA-256");
       byte[] chunk = new byte[Main.KEY_SIZE];
       int x;
       while ((x = fis.read(chunk)) != -1) {
         md.update(Arrays.copyOf(chunk, x));
       }
       byte[] hash = null;
       hash = new byte[md.getDigestLength()];
       hash = md.digest();
       return hash;
     } catch (IOException e) {
       e.printStackTrace();
     }
     return null;
   }
 
   public static boolean isEqualBytes(byte[] a, byte[] b) {
     if (a.length == b.length) {
       for (int i = 0; i < a.length; i++) {
         if (a[i] != b[i])
           return false;
       }
     } else {
       return false;
     }
     return true;
   }
 }

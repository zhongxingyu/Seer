 /*
  * Please refer to http://code.thejo.in/license/
  * for details about source code license.
  */
 
 package in.kote.ssf.util;
 
 import java.util.Random;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 /**
  *
  * @author Thejo
  */
 public class BasicUtilities {
 
     /**
      * Given a string, returns the MD5 hash
      *
      * @param s String whose MD5 hash value is required
      * @return An MD5 hash string
      * @throws java.security.NoSuchAlgorithmException
      */
     public static BigInteger getMD5(String s) throws NoSuchAlgorithmException {
 
         MessageDigest m = MessageDigest.getInstance("MD5");
         m.update( s.getBytes(), 0, s.length() );
 
         //String md5 = new BigInteger(1, m.digest()).toString(16);
 
         return new BigInteger(1, m.digest());
     }
 
     /**
      * Given a range of integers, returns a random number in that range
      * (inclusive of the start of the range)
      *
      * @param start
      * @param end
      * @return
      */
     public static int getRandomNumberInRange(int start, int end) {
         Random r = new Random();
         return r.nextInt(end) + start;
     }
 
     /**
      * Calculate and return the amount of memory used by the JVM
      *
      * @return long - Memory usage in bytes
      */
     public static long getUsedMemory() {
         return Runtime.getRuntime().totalMemory() -
                 Runtime.getRuntime().freeMemory();
     }
 
     public static float getFreeMemoryPercentage() {
        return ( (float) Runtime.getRuntime().freeMemory() /
                 Runtime.getRuntime().totalMemory() ) * 100;
     }
}

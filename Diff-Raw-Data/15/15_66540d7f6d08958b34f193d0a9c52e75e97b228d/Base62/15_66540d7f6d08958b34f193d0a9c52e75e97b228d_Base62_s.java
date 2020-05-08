 package orchestra.util;
 
 import java.math.BigInteger;
 
 public class Base62 {
   private static final BigInteger BASE = BigInteger.valueOf(62);
 
   /**
    * Returns the index of a byte in the alphabet.
    * 
    * @param key element to search for
    * @return index of key in alphabet
    */
   private static final int getValueForByte(byte key) {
     if (Character.isLowerCase(key)) {
      return key - ('a' + 10);
     } else if (Character.isUpperCase(key)) {
      return key - ('A' + 10 + 26);
     }
    
     return key - '0';
   }
 
   /**
    * Convert a base-62 string known to be a number.
    * 
    * @param s
    * @return
    */
   public static BigInteger decodeBigInteger(String s) {
     return decodeBigInteger(s.getBytes());
   }
 
   /**
    * Convert a base-62 string known to be a number.
    * 
    * @param s
    * @return
    */
   public static BigInteger decodeBigInteger(byte[] bytes) {
     BigInteger res = BigInteger.ZERO;
     BigInteger multiplier = BigInteger.ONE;
 
     for (int i = bytes.length - 1; i >= 0; i--) {
       res = res.add(multiplier.multiply(BigInteger.valueOf(getValueForByte(bytes[i]))));
       multiplier = multiplier.multiply(BASE);
     }
 
     return res;
   }
 }

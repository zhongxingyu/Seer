 package orchestra.util;
 
 import java.math.BigInteger;
 

/**
 * Encoders and decoders for base-62 formatted data. Uses the alphabet 0..9 a..z
 * A..Z, e.g. '0' => 0, 'a' => 10, 'A' => 36 and 'Z' => 62.
 * 
 */
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
      return key - ('a' - 10);
     } else if (Character.isUpperCase(key)) {
      return key - ('A' - 10 - 26);
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

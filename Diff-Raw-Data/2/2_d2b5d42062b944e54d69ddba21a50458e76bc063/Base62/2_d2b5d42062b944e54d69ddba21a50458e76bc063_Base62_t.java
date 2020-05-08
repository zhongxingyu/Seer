 package orchestra.util;
 
 import java.math.BigInteger;
 
 
 /**
  * Encoders and decoders for base-62 formatted data. Uses the alphabet 0..9 a..z
 * A..Z, e.g. '0' => 0, 'a' => 10, 'A' => 35 and 'Z' => 61.
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
   private static final int valueForByte(byte key) {
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
       res = res.add(multiplier.multiply(BigInteger.valueOf(valueForByte(bytes[i]))));
       multiplier = multiplier.multiply(BASE);
     }
 
     return res;
   }
 
   public static String encode(final BigInteger i) throws IllegalArgumentException {
     if (i == null) {
       throw new NullPointerException("Argument must be non-null");
     }
     
     if (BigInteger.ZERO.compareTo(i) > 0) {
       throw new IllegalArgumentException("Argument must be larger than zero");
     }
     
     if (BigInteger.ZERO.compareTo(i) == 0) {
       return "0";
     }
     
     StringBuffer buf = new StringBuffer();
     BigInteger value = i.add(BigInteger.ZERO); // Clone argument
     
     while (BigInteger.ZERO.compareTo(value) < 0) {
       BigInteger[] divRem = value.divideAndRemainder(BASE);
       int remainder = divRem[1].intValue();
       
       if (remainder < 10) {
         buf.insert(0, (char) (remainder + '0'));
       } else if (remainder < 10 + 26) {
         buf.insert(0, (char) (remainder + 'a' - 10));
       } else {
         buf.insert(0, (char) (remainder + 'A' - 10 - 26));
       }
       
       value = divRem[0];
     }
 
     return buf.toString();
   }
 }

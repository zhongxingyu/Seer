 package unstar;
 
 import java.util.Arrays;
 import java.util.NoSuchElementException;
 import lombok.AllArgsConstructor;
 import lombok.extern.java.Log;
 
 /**
  * Generate a range of codes one-by-one.
  */
 @Log
 @AllArgsConstructor
 public final class GeneratorCodeProvider implements CodeProvider {
 
     /** Length of a serial code. */
     public static final int CODELEN = 8;
 
     /** The digits of a serial code. */
     public static final char[] DIGITS =
     {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
      'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
      'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
 
     /** The first code to generate. */
     private String start;
 
     /** The last code to generate. */
     private final String end;
 
     @Override
     public String next() {
         if (decode(start) > decode(end)) {
             throw new NoSuchElementException();
         }
         String ret = start;
         start = encode(CODELEN, decode(start) + 1);
        return encode(CODELEN, decode(ret));
     }
 
     @Override
     public boolean hasNext() {
        return decode(start) <= decode(end);
     }
 
     @Override
     public void report(final String code) {
         log.info("valid: " + code);
     }
 
     @Override
     public void remove() {
         throw new UnsupportedOperationException();
     }
 
     /**
      * Encode the given number into a serial code (base 36).
      * @param len  the length of the code
      * @param val  the number to encode
      * @return the serial code
      */
     public static String encode(final int len, final long val) {
         StringBuilder str = new StringBuilder();
         long div = val;
         while (div > 0) {
             int digit = (int) (div % DIGITS.length);
             str.append(DIGITS[digit]);
             div = div / DIGITS.length;
         }
         while (str.length() < len) {
             str.append(DIGITS[0]);
         }
         return str.reverse().toString();
     }
 
     /**
      * Decode a serial code longo a number.
      * @param value  the serial code
      * @return the number for this code
      */
     public static long decode(final String value) {
         long total = 0;
         for (int i = 0; i < value.length(); i++) {
             char c = value.toUpperCase().charAt(i);
             total = total * DIGITS.length + Arrays.binarySearch(DIGITS, c);
         }
         return total;
     }
 }

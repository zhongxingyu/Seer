 package edu.osu.cse.mmxi.common;
 
 /**
  * The <code>Utilities</code> class contains several methods of general-purpose use which
  * are used throughout the program.
  * 
  */
 public class Utilities {
     private static long seed = System.nanoTime(), rand = 0;
 
     /**
      * Converts the incoming word argument to a 4-digit padded capital hexadecimal string.
      * 
      * @param s
      *            the word to convert
      * @return the word as a string
      */
     public static String uShortToHex(final short s) {
         return String.format("%X", s + 0x20000).substring(1);
     }
 
     /**
      * Converts the incoming word argument to a signed capital hexadecimal string with a
      * negative sign (not padded).
      * 
      * @param s
      *            the word to convert
      * @return the word as a string
      */
     public static String sShortToHex(final short s) {
         if (s < 0)
             return "-" + String.format("%X", -s);
         else
             return String.format("%X", s);
     }
 
     /**
      * Generates a random word, manipulating an internal seed which is initially set to
      * the system time.
      * 
      * @return a random word
      */
     public static short randomShort() {
         if (rand == 0) {
             seed ^= seed << 21;
             seed ^= seed >>> 35;
             seed ^= seed << 4;
             rand = seed;
         }
         final short s = (short) (rand & 0xffffL);
         rand >>>= 16;
         return s;
     }
 
     /**
      * Attempts to parse the given short value, accepting hex values (preceded by
      * {@code 0x} or {@code x}), binary values (preceded by {@code 0b}), octal values
      * (with a leading zero), or decimal values, and returning -1 if the parse failed.
      * 
      * @param s
      *            the string to parse
      * @return the short result, or -1 if the format was not followed
      */
     public static int parseShort(String s) {
         int radix = 10;
         if (s.startsWith("0x") || s.startsWith("0X")) {
             radix = 16;
             s = s.substring(2);
         } else if (s.substring(0, 1).equalsIgnoreCase("x")) {
             radix = 16;
             s = s.substring(1);
         } else if (s.startsWith("0b") || s.startsWith("0B")) {
             radix = 2;
             s = s.substring(2);
         } else if (s.length() > 1 && s.startsWith("0"))
             radix = 8;
         try {
             return (short) Integer.parseInt(s, radix);
         } catch (final NumberFormatException e) {
             return -1;
         }
     }
 
    public static int parseInt(String s) {
        int radix = 10;
        if (s.startsWith("0x") || s.startsWith("0X")) {
            radix = 16;
            s = s.substring(2);
        } else if (s.substring(0, 1).equalsIgnoreCase("x")) {
            radix = 16;
            s = s.substring(1);
        } else if (s.startsWith("0b") || s.startsWith("0B")) {
            radix = 2;
            s = s.substring(2);
        } else if (s.length() > 1 && s.startsWith("0"))
            radix = 8;
        try {
            return Integer.parseInt(s, radix);
        } catch (final NumberFormatException e) {
            return -1;
        }
    }

     /**
      * Attempts to parse the given quoted string, accepting backslash-quoted ASCII values.
      * 
      * @param s
      *            the string to parse
      * @return the unquoted string
      */
     public static String parseString(String s) {
         String ret = "";
         while (s.length() > 0)
             if (s.charAt(0) == '\\') {
                 s = s.substring(1);
                 if (s.length() == 0) {
                     ret += "\\";
                     continue;
                 }
                 final int index = "abtnvfre\'\"\\".indexOf(s.charAt(0));
                 if (index != -1) {
                     s = s.substring(1);
                     ret += "\u0007\b\t\n\u000B\f\r\u001B\'\"\\".charAt(index);
                 } else if (s.matches("x[0-9A-Fa-f]{2}.*")) {
                     ret += (char) Integer.parseInt(s.substring(1, 3), 16);
                     s = s.substring(3);
                 } else if (s.matches("c[?-_].*")) {
                     ret += (char) (s.charAt(1) - '@' & 0x7F);
                     s = s.substring(2);
                 } else if (s.matches("(([0-3]?[0-7])?[0-7]).*")) {
                     final String oct = s.split("[^0-7]", 2)[0];
                     ret += (char) Integer.parseInt(oct, 8);
                     s = s.substring(oct.length());
                 } else
                     ret += "\\";
             } else {
                 ret += s.charAt(0);
                 s = s.substring(1);
             }
         return ret;
     }
 }

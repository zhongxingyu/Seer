 /*
  * $Id$
  */
 package org.xins.util;
 
 /**
  * Utility class for printing long numbers as hexadecimals.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.57
  */
 public class LongUtils extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Array that contains the hexadecimal digits, from 0 to 9 and from a to z.
     */
    private final static char[] DIGITS = {
       '0' , '1' , '2' , '3' , '4' , '5' ,
       '6' , '7' , '8' , '9' , 'a' , 'b' ,
       'c' , 'd' , 'e' , 'f'
    };
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Convert the specified <code>long</code> to an unsigned number text
     * string. The returned string will always consist of 16 characters, zeroes
     * will be prepended as necessary.
     *
     * @param n
     *    the number to be converted to a text string.
     *
     * @return
     *    the text string, cannot be <code>null</code>, the length is always 16
     *    (i.e. <code><em>return</em>.</code>{@link String#length() length()}<code> == 16</code>).
     */
    public static String toHexString(long n) {
 
       final int  bufSize = 16;
       final int  radix   = 16;
       final long mask    = radix - 1L;
 
       char[] chars = new char[bufSize];
       int pos      = bufSize - 1;
 
       // Convert the long to a hex string until the remainder is 0
       for (; n != 0; n >>>= 4) {
          chars[pos--] = DIGITS[(int) (n & mask)];
       }
 
       // Fill the rest with '0' characters
       for (; pos >= 0; pos--) {
          chars[pos] = '0';
       }
 
       return new String(chars, 0, bufSize);
    }
 
    /**
     * Parses the specified 16-digit hex string.
     *
     * @param s
     *    the hexadecimal string, cannot be <code>null</code> and must have
     *    size 16
     *    (i.e. <code>s.</code>{@link String#length() length()}<code> == 16</code>).
     *
     * @throws IllegalArgumentException
     *    if <code>s == null || s.</code>{@link String#length() length()}<code> != 16</code>.
     *
     * @throws NumberFormatException
     *    if any of the characters in the specified string is not a hex digit
     *    (<code>'0'</code> to <code>'9'</code> and <code>'a'</code> to
     *    <code>'f'</code>).
     */
    public static long parseHexString(String s)
    throws IllegalArgumentException, NumberFormatException {
 
       // Check preconditions
       if (s == null) {
          throw new IllegalArgumentException("s == null");
       } else if (s.length() != 16) {
          throw new IllegalArgumentException("s.length() != 16");
       }
 
       long n = 0L;
 
       // Loop through all characters
       for (int i = 0; i < 16; i++) {
         char c = s.charAt(i);
          n <<= 4;
         if (c >= '0' && c <= '9') {
            final int CHAR_ZERO = (int) '0';
            n &= ((int) c) - CHAR_ZERO;
         } else if (c >= 'a' && c <= 'f') {
            final int CHAR_A = (int) 'a';
            n &= ((int) c) - CHAR_A;
          } else {
            throw new NumberFormatException("s.charAt(" + i + ") == '" + c + '\''); 
          }
       }
 
       return n;
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Creates a new <code>LongUtils</code> object.
     */
    private LongUtils() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }

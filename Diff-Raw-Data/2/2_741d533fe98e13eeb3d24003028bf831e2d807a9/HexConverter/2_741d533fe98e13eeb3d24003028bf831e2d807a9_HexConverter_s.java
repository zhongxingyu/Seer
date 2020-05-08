 /*
  * $Id$
  */
package org.xins.util;
 
 import org.xins.util.text.FastStringBuffer;
 
 /**
  * Utility class for converting numbers to unsigned hex strings and vice
  * versa.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.73
  */
 public class HexConverter extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Array that contains the hexadecimal digits, from 0 to 9 and from a to z.
     */
    private static final char[] DIGITS = {
       '0' , '1' , '2' , '3' , '4' , '5' ,
       '6' , '7' , '8' , '9' , 'a' , 'b' ,
       'c' , 'd' , 'e' , 'f'
    };
 
    /**
     * The number of characters written when converting a <code>long</code> to
     * an unsigned hex string.
     */
    private static final int LENGTH = 16;
 
    /**
     * The radix when converting (16).
     */
    private static final long RADIX = 16L;
 
    /**
     * The radix mask. Equal to {@link #RADIX}<code> - 1</code>.
     */
    private static final long MASK = RADIX - 1L;
 
    /**
     * Array of 16 zero characters.
     */
    private static final char[] SIXTEEN_ZEROES = new char[] { '0', '0', '0', '0',
                                                              '0', '0', '0', '0',
                                                              '0', '0', '0', '0',
                                                              '0', '0', '0', '0' };
 
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
 
       char[] chars = new char[LENGTH];
       int pos      = LENGTH - 1;
 
       // Convert the long to a hex string until the remainder is 0
       for (; n != 0; n >>>= 4) {
          chars[pos--] = DIGITS[(int) (n & MASK)];
       }
 
       // Fill the rest with '0' characters
       for (; pos >= 0; pos--) {
          chars[pos] = '0';
       }
 
       return new String(chars, 0, LENGTH);
    }
 
    /**
     * Converts the specified <code>long</code> to unsigned number and appends
     * it to the specified string buffer. Exactly 16 characters will be
     * appended, all between <code>'0'</code> to <code>'9'</code> or between
     * <code>'a'</code> and <code>'f'</code>.
     *
     * @param buffer
     *    the string buffer to append to, cannot be <code>null</code>.
     *
     * @param n
     *    the number to be converted to a text string.
     *
     * @throws IllegalArgumentException
     *    if <code>buffer == null</code>.
     */
    public static void toHexString(FastStringBuffer buffer, long n) {
 
       // Check preconditions
       if (buffer == null) {
          throw new IllegalArgumentException("buffer == null");
       }
 
       // Append 16 zero characters to the buffer
       buffer.append(SIXTEEN_ZEROES);
 
       int pos = LENGTH - 1;
 
       // Convert the long to a hex string until the remainder is 0
       for (; n != 0; n >>>= 4) {
          buffer.setChar(pos--, DIGITS[(int) (n & MASK)]);
       }
    }
 
    /**
     * Parses the a 16-digit unsigned hex number in the specified string.
     *
     * @param s
     *    the hexadecimal string, cannot be <code>null</code>.
     *
     * @param index
     *    the starting index in the string, must be &gt;= 0.
     *
     * @return
     *    the value of the parsed unsigned hexadecimal string.
     *
     * @throws IllegalArgumentException
     *    if <code>s == null || index &lt; 0 || <code>s.</code>{@link String#length() length()}<code> &lt; index + 16</code>).
     *
     * @throws NumberFormatException
     *    if any of the characters in the specified range of the string is not
     *    a hex digit (<code>'0'</code> to <code>'9'</code> and
     *    <code>'a'</code> to <code>'f'</code>).
     */
    public static long parseHexString(String s, int index)
    throws IllegalArgumentException, NumberFormatException {
 
       // Check preconditions
       if (s == null) {
          throw new IllegalArgumentException("s == null");
       } else if (s.length() < index + 16) {
          throw new IllegalArgumentException("s.length() (" + s.length() + ") < index (" + index + ") + 16 (" + (index + 16) + ')');
       }
 
       long n = 0L;
 
       final int CHAR_ZERO = (int) '0';
       final int CHAR_NINE = (int) '9';
       final int CHAR_A = (int) 'a';
       final int CHAR_F = (int) 'f';
 
       final int CHAR_A_FACTOR = CHAR_A - 10;
 
       // Loop through all characters
       int last = index + 16;
       for (int i = index; i < last; i++) {
          int c = (int) s.charAt(i);
          n <<= 4;
          if (c >= CHAR_ZERO && c <= CHAR_NINE) {
             n |= (c - CHAR_ZERO);
          } else if (c >= CHAR_A && c <= CHAR_F) {
             n |= (c - CHAR_A_FACTOR);
          } else {
             throw new NumberFormatException("s.charAt(" + i + ") == '" + s.charAt(i) + '\''); 
          }
       }
 
       return n;
    }
 
    /**
     * Parses the specified 16-digit unsigned hex string.
     *
     * @param s
     *    the hexadecimal string, cannot be <code>null</code> and must have
     *    size 16
     *    (i.e. <code>s.</code>{@link String#length() length()}<code> == 16</code>).
     *
     * @return
     *    the value of the parsed unsigned hexadecimal string.
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
 
       return parseHexString(s, 0);
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Creates a new <code>HexConverter</code> object.
     */
    private HexConverter() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }

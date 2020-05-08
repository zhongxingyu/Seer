 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.text;
 
 import java.io.ByteArrayOutputStream;
 import java.io.UnsupportedEncodingException;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 
 /**
  * URL encoding utility functions with Unicode support. This class supports
  * both encoding and decoding. All characters higher than 127 will be encoded
  * as %uxxxx where xxxx is the Unicode value of the character in hexadecimal.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  * @author <a href="mailto:anthony.goubard@orange-ftgroup.com">Anthony Goubard</a>
  *
  * @since XINS 1.0.0
  */
 public final class URLEncoding {
 
    /**
     * The character zero (<code>'0'</code>) as an <code>int</code>.
     */
    private static final int CHAR_ZERO = (int) '0';
 
    /**
     * The character nine (<code>'9'</code>) as an <code>int</code>.
     */
    private static final int CHAR_NINE = (int) '9';
 
    /**
     * The character lowercase A (<code>'a'</code>) as an <code>int</code>.
     */
    private static final int CHAR_LOWER_A = (int) 'a';
 
    /**
     * The character lowercase F (<code>'f'</code>) as an <code>int</code>.
     */
    private static final int CHAR_LOWER_F = (int) 'f';
 
    /**
     * The character uppercase A (<code>'A'</code>) as an <code>int</code>.
     */
    private static final int CHAR_UPPER_A = (int) 'A';
 
    /**
     * The character uppercase F (<code>'F'</code>) as an <code>int</code>.
     */
    private static final int CHAR_UPPER_F = (int) 'F';
 
    /**
     * Mappings from unencoded (array index) to encoded values (array
     * elements). The size of this array is 127.
     */
    private static final String[] UNENCODED_TO_ENCODED;
 
    static {
       UNENCODED_TO_ENCODED = new String[255];
       for (int i = 0; i < 255; i++) {
          char c = (char) i;
          if ((c >= 'a' && c <= 'z') ||
              (c >= 'A' && c <= 'Z') ||
              (c >= '0' && c <= '9') ||
              (c == '-')             ||
              (c == '_')             ||
              (c == '.')             ||
              (c == '*')) {
             UNENCODED_TO_ENCODED[i] = String.valueOf(c);
          } else if (c == ' ') {
             UNENCODED_TO_ENCODED[i] = "+";
          } else {
             char[] data = new char[3];
             data[0] = '%';
             data[1] = Character.toUpperCase(Character.forDigit((i >> 4) & 0xF, 16));
             data[2] = Character.toUpperCase(Character.forDigit( i       & 0xF, 16));
             UNENCODED_TO_ENCODED[i] = new String(data);
          }
       }
    }
 
    /**
     * Constructs a new <code>URLEncoding</code> object.
     */
    private URLEncoding() {
       // empty
    }
 
    /**
     * URL encodes the specified character string as specified by W3C.
     * http://www.w3.org/International/O-URL-code.html
     *
     * @param s
     *    the string to URL encode, not <code>null</code>.
     *
     * @return
     *    URL encoded version of the specified character string, never
     *    <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>s == null</code>
     */
    public static String encode(String s)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("s", s);
 
       // Short-circuit if the string is empty
       int length = s.length();
       if (length < 1) {
          return "";
       }
 
       // Construct a buffer
       StringBuffer buffer = new StringBuffer(length * 2);
 
       // Loop through the string and just append whatever we find
       // in UNENCODED_TO_ENCODED or if c > 127, encode the UTF-8 value
       // of the character (cf http://www.w3.org/International/O-URL-code.html).
       char[] content = s.toCharArray();
       for (int i = 0; i < length; i++) {
          int c = (int) content[i];
          if (c < 128) {
             buffer.append(UNENCODED_TO_ENCODED[c]);
          } else if (c <= 0x07FF) { // non-ASCII <= 0x7FF
            buffer.append('%');
            buffer.append(Integer.toHexString(0xc0 | (c >> 6)));
            buffer.append('%');
            buffer.append(Integer.toHexString(0x80 | (c & 0x3F)));
          } else { // 0x7FF < c <= 0xFFFF
            buffer.append('%');
            buffer.append(Integer.toHexString(0xe0 | (c >> 12)));
            buffer.append('%');
            buffer.append(Integer.toHexString(0x80 | ((c >> 6) & 0x3F)));
            buffer.append('%');
            buffer.append(Integer.toHexString(0x80 | (c & 0x3F)));
          }
       }
 
       return buffer.toString();
    }
 
    /**
     * Decodes the specified URL encoded character string.
     * http://www.w3.org/International/O-URL-code.html
     *
     * @param s
     *    the URL encoded string to decode, not <code>null</code>.
     *
     * @return
     *    unencoded version of the specified URL encoded character string,
     *    never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>s == null</code>.
     *
     * @throws FormatException
     *    if any of the following conditions is true:
     *    <ul>
     *        <li><code>s.{@link String#charAt(int) charAt}(s.{@link String#length() length}() - 1)</code>
     *            (last character is a percentage sign)
     *        <li><code>s.{@link String#charAt(int) charAt}(s.{@link String#length() length}() - 2)</code>
     *            (before-last character is a percentage sign)
     *        <li><code>s.{@link String#charAt(int) charAt}(<em>n</em>) == '%'
     *                  &amp;&amp; !(           {@link org.xins.common.text.HexConverter}.{@link org.xins.common.text.HexConverter#isHexDigit(char) isDigit}(s.{@link String#charAt(int) charAt}(<em>n</em> + 1))
     *                               &amp;&amp; {@link org.xins.common.text.HexConverter}.{@link org.xins.common.text.HexConverter#isHexDigit(char) isDigit}(s.{@link String#charAt(int) charAt}(<em>n</em> + 2)))</code>
     *            (percentage sign is followed by 2 characters of which at least one is not a hexadecimal digit)
     *    </ul>
     */
    public static String decode(String s)
    throws IllegalArgumentException, FormatException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("s", s);
 
       // If the string is empty, return the original string
       int length = s.length();
       if (length == 0) {
          return s;
       }
 
       // Avoid calls to charAt() method.
       char[] string = s.toCharArray();
 
       // Loop through the string
       StringBuffer buffer = new StringBuffer(length * 2);
       int index = 0;
       while (index < length) {
 
          // Get the character
          char c = string[index];
          int charAsInt = (int) c;
 
          // Special case: Recognize plus sign as a space
          if (c == '+') {
             buffer.append(' ');
 
          // Catch encoded characters
          } else if (c == '%') {
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             while (index < length && string[index] == '%') {
                if (index >= length - 2) {
                    throw new FormatException(s, "Character at position " + index + " has invalid value " + charAsInt + '.');
                }
                charAsInt = (int) string[++index];
                int decodedValue = digit(charAsInt, s, index);
                decodedValue *= 16;
                charAsInt = (int) string[++index];
                decodedValue += digit(charAsInt, s, index);
 
                baos.write((int) decodedValue);
 
                index++;
             }
             try {
                buffer.append(baos.toString("UTF-8"));
             } catch (UnsupportedEncodingException uee) {
                Utils.logProgrammingError(uee);
             }
             // Back to the last position
             index--;
 
          // Append the character
          } else {
             buffer.append(c);
          }
 
          // Proceed to the next character
          index++;
       }
 
       return buffer.toString();
    }
 
    /**
     * Convert a hexadecimal digit to a number.
     *
     * @param charAsInt
     *    the hexadecimal digit.
     *
     * @param s
     *    the String from which the character has been taken.
     *
     * @param index
     *    the position of the character within the String.
     *
     * @return
     *    the converted character converted to an int.
     *
     * @throws FormatException
     *    if c is not a numerical digit or a letter between 'a' and 'f' or
     *    'A' or 'F'.
     */
    private static int digit(int charAsInt, String s, int index) throws FormatException {
       int decodedValue;
       if (charAsInt >= CHAR_ZERO && charAsInt <= CHAR_NINE) {
          decodedValue = charAsInt - CHAR_ZERO;
       } else if (charAsInt >= CHAR_LOWER_A && charAsInt <= CHAR_LOWER_F) {
          decodedValue = charAsInt - CHAR_LOWER_A + 10;
       } else if (charAsInt >= CHAR_UPPER_A && charAsInt <= CHAR_UPPER_F) {
          decodedValue = charAsInt - CHAR_UPPER_A + 10;
       } else {
          throw new FormatException(s, "Character at position " + index + " is not a hex digit. Value is " + charAsInt + '.');
       }
       return decodedValue;
    }
 }

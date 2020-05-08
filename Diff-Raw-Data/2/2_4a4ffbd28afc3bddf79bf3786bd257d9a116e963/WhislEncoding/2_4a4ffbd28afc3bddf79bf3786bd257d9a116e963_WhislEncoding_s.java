 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.text;
 
 import org.xins.common.MandatoryArgumentChecker;
 
 /**
  * Whisl encoding utility functions. This class currently only supports
  * encoding.
  *
  * <p>The Whisl encoding is similar to URL encoding, but specifically meant to
  * support the complete Unicode character set.
  *
  * <p>The following transformations should be applied when decoding:
  *
  * <ul>
  *    <li>a plus sign (<code>'+'</code>) converts to a space character;
  *    <li>a percent sign (<code>'%'</code>) must be followed by a 2-digit hex
  *        number that indicate the Unicode value of a single character;
  *    <li>a dollar sign (<code>'$'</code>) must be followed by a 4-digit hex
  *        number that indicate the Unicode value of a single character;
  * </ul>
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public final class WhislEncoding extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Mappings from unencoded (array index) to encoded values (array
     * elements) for characters that can be encoded using the percent sign. The
     * size of this array is 128.
     */
    private static final char[][] UNENCODED_TO_ENCODED;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    static {
 
       // Fill 128 array elements
       UNENCODED_TO_ENCODED = new char[128][];
       for (char c = 0; c < 128; c++) {
 
          // Some characters can be output unmodified
          if ((c >= 'a' && c <= 'z') ||
              (c >= 'A' && c <= 'Z') ||
              (c >= '0' && c <= '9') ||
              (c == '-')             ||
              (c == '_')             ||
              (c == '.')             ||
              (c == '*')) {
             UNENCODED_TO_ENCODED[c] = null;
 
          // A space is converted to a plus-sign
          } else if (c == ' ') {
             char[] plus = {'+'};
             UNENCODED_TO_ENCODED[c] = plus;
 
          // All other characters are URL-encoded in the form "%hex", where
          // "hex" is the hexadecimal value of the character
          } else {
             char[] data = new char[3];
             data[0] = '%';
             data[1] = Character.forDigit((c >> 4) & 0xF, 16);
             data[2] = Character.forDigit( c       & 0xF, 16);
             data[1] = Character.toUpperCase(data[1]);
             data[2] = Character.toUpperCase(data[2]);
             UNENCODED_TO_ENCODED[c] = data;
          }
       }
 
       // XXX: Allow test coverage analysis tools to report 100% coverage
       new WhislEncoding();
    }
 
    /**
     * Whisl-encodes the specified character string.
     *
     * @param s
     *    the string to Whisl-encode, not <code>null</code>.
     *
     * @return
     *    Whisl-encoded version of the specified character string, never
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
       char[] string = s.toCharArray();
       FastStringBuffer buffer = null;
 
       // Loop through the string. If the character is less than 128 then get
       // from the cache array, otherwise convert escape with a dollar sign
       int lastAppendPos = 0;
       for (int i = 0; i < length; i++) {
          int c = (int) string[i];
          if (c < 128) {
             char[] encoded = UNENCODED_TO_ENCODED[c];
             if (encoded != null) {
                if (buffer == null) {
                   buffer = new FastStringBuffer(length * 2);
                }
                buffer.append(string, lastAppendPos, i - lastAppendPos);
                buffer.append(encoded);
                lastAppendPos = i + 1;
             }
          } else {
             if (buffer == null) {
                buffer = new FastStringBuffer(length * 2);
             }
             buffer.append(string, lastAppendPos, i - lastAppendPos);
             buffer.append('$');
             HexConverter.toHexString(buffer, (short) c);
             lastAppendPos = i + 1;
          }
       }
       if (buffer == null) {
          return s;
       } else if (lastAppendPos != length) {
          buffer.append(string, lastAppendPos, length - lastAppendPos);
       }
 
       return buffer.toString();
    }
 
    /**
     * Decodes the specified Whisl-encoded character string.
     *
     * @param s
     *    the string to decode, not <code>null</code>.
     *
     * @return
     *    the decoded string, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>s == null</code>
     *
     * @throws ParseException
     *    if the string cannot be decoded.
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
 
       return null; // FIXME TODO
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>WhislEncoding</code> object.
     */
    private WhislEncoding() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }

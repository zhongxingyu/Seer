 /**
  * Copyright (c) 2004-2005, Regents of the University of California
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  *
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * Neither the name of the University of California, Los Angeles nor the
  * names of its contributors may be used to endorse or promote products
  * derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package cck.text;
 
 import cck.util.Arithmetic;
 import cck.util.Util;
 import java.text.CharacterIterator;
 import java.text.StringCharacterIterator;
 import java.util.*;
 
 /**
  * The <code>StringUtil</code> class implements several useful functions for dealing with strings such as
  * parsing pieces of syntax, formatting, etc.
  *
  * @author Ben L. Titzer
  */
 public class StringUtil {
     public static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7',
                                             '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
     public static final String QUOTE = "\"".intern();
     public static final String SQUOTE = "'".intern();
     public static final String LPAREN = "(".intern();
     public static final String RPAREN = ")".intern();
     public static final String COMMA = ",".intern();
     public static final String COMMA_SPACE = ", ".intern();
     public static final String[] EMPTY_STRING_ARRAY = {};
     public static final int[] DENOM = { 24, 60, 60, 1000 };
     public static final int[] DAYSECS = { 60, 60 };
 
     /**
      * The <code>addToString()</code> method converts a numerical address (represented as a signed 32-bit
      * integer) and converts it to a string in the format 0xXXXX where 'X' represents a hexadecimal character.
      * The address is assumed to fit in 4 hexadecimal characters. If it does not, the string will have as many
      * characters as necessary (max 8) to represent the address.
      *
      * @param address the address value as an integer
      * @return a standard string representation of the address
      */
     public static String addrToString(int address) {
         return to0xHex(address, 4);
     }
 
     public static String readIdentifier(CharacterIterator i) {
         StringBuffer buf = new StringBuffer();
 
         while (true) {
             char c = i.current();
 
             if (!Character.isLetterOrDigit(c) && (c != '_')) break;
 
             buf.append(c);
             i.next();
         }
 
         return buf.toString();
     }
 
     public static String readDotIdentifier(CharacterIterator i) {
         StringBuffer buf = new StringBuffer();
 
         while (true) {
             char c = i.current();
 
             if (!Character.isLetterOrDigit(c) && (c != '_') && (c != '.')) break;
 
             buf.append(c);
             i.next();
         }
 
         return buf.toString();
     }
 
     public static int readHexValue(CharacterIterator i, int max_chars) {
         int accumul = 0;
 
         for (int cntr = 0; cntr < max_chars; cntr++) {
             char c = i.current();
 
             if (c == CharacterIterator.DONE) break;
             if (!isHexDigit(c)) break;
 
             accumul = (accumul << 4) | hexValueOf(c);
             i.next();
         }
 
         return accumul;
     }
 
     public static int readOctalValue(CharacterIterator i, int max_chars) {
         int accumul = 0;
 
         for (int cntr = 0; cntr < max_chars; cntr++) {
             char c = i.current();
 
             if (!isOctalDigit(c)) break;
 
             accumul = (accumul << 3) | octalValueOf(c);
             i.next();
         }
 
         return accumul;
     }
 
     public static int readDecimalValue(CharacterIterator i, int max_chars) {
         StringBuffer buf = new StringBuffer();
 
         boolean minus = false;
 
         if (peekAndEat(i, '-')) minus = true;
 
         for (int cntr = 0; cntr < max_chars; cntr++) {
             char c = i.current();
 
             if (!Character.isDigit(c)) break;
 
             buf.append(c);
             i.next();
         }
 
         String cnst = buf.toString();
         int val = Integer.parseInt(cnst);
         if (minus) val = -val;
         return val;
     }
 
     public static String readDecimalString(CharacterIterator i, int max_chars) {
         StringBuffer buf = new StringBuffer();
 
         if (peekAndEat(i, '-')) buf.append('-');
 
         for (int cntr = 0; cntr < max_chars; cntr++) {
             char c = i.current();
 
             if (!Character.isDigit(c)) break;
 
             buf.append(c);
             i.next();
         }
 
         return buf.toString();
     }
 
     public static void skipWhiteSpace(CharacterIterator i) {
         while (true) {
             char c = i.current();
             if (c == ' ' || c == '\n' || c == '\t')
                 i.next();
             else
                 break;
         }
     }
 
     public static char peek(CharacterIterator i) {
         return i.current();
     }
 
     public static boolean peekAndEat(CharacterIterator i, char c) {
         char r = i.current();
         if (r == c) {
             i.next();
             return true;
         }
         return false;
     }
 
     public static void expectChar(CharacterIterator i, char c) throws Exception {
         char r = i.current();
         i.next();
         if (r != c)
             throw new Exception("expected " + squote(c) + " @ "
                     + (i.getIndex() - 1) + ", found " + squote(r));
     }
 
     /**
      * The <code>isHex()</code> method checks whether the specifed string represents a hexadecimal
      * integer. This method only checks the first two characters. If they match "0x" or "0X", then
      * this method returns true, otherwise, it returns false.
      * @param s the string to check whether it begins with a hexadecimal sequence
      * @return true if the string begins with "0x" or "0X"; false otherwise
      */
     public static boolean isHex(String s) {
         if ( s.length() < 2 ) return false;
         char c = s.charAt(1);
         return s.charAt(0) == '0' && (c == 'x' || c == 'X');
     }
 
     /**
      * The <code>isHex()</code> method checks whether the specifed string represents a binary
      * integer. This method only checks the first two characters. If they match "0b" or "0B", then
      * this method returns true, otherwise, it returns false.
      * @param s the string to check whether it begins with a hexadecimal sequence
      * @return true if the string begins with "0b" or "0B"; false otherwise
      */
     public static boolean isBin(String s) {
         if ( s.length() < 2 ) return false;
         char c = s.charAt(1);
         return s.charAt(0) == '0' && (c == 'b' || c == 'B');
     }
 
     /**
      * The <code>isHexDigit()</code> method tests whether the given character corresponds to one of the
      * characters used in the hexadecimal representation (i.e. is '0'-'9' or 'a'-'b', case insensitive. This
      * method is generally used in parsing and lexing of input.
      *
      * @param c the character to test
      * @return true if this character is a hexadecimal digit; false otherwise
      */
     public static boolean isHexDigit(char c) {
         switch (c) {
             case '0':
             case '1':
             case '2':
             case '3':
             case '4':
             case '5':
             case '6':
             case '7':
             case '8':
             case '9':
             case 'a':
             case 'b':
             case 'c':
             case 'd':
             case 'e':
             case 'f':
             case 'A':
             case 'B':
             case 'C':
             case 'D':
             case 'E':
             case 'F':
                 return true;
         }
         return false;
     }
 
     public static int hexValueOf(char c) {
         return Character.digit(c, 16);
     }
 
     public static int octalValueOf(char c) {
         return Character.digit(c, 8);
     }
 
     public static boolean isOctalDigit(char c) {
         switch (c) {
             case '0':
             case '1':
             case '2':
             case '3':
             case '4':
             case '5':
             case '6':
             case '7':
                 return true;
         }
         return false;
     }
 
     /**
      * The <code>rightJustify()</code> method pads a string to a specified length by adding spaces on the
      * left, thus justifying the string to the right margin. This is extremely useful in generating columnar
      * output in textual tables.
      *
      * @param v     a long value to convert to a string and justify
      * @param width the number of characters to pad the string to
      * @return a string representation of the input, padded on the left with spaces to achieve the desired
      *         length.
      */
     public static String rightJustify(long v, int width) {
         return rightJustify(Long.toString(v), width);
     }
 
     /**
      * The <code>rightJustify()</code> method pads a string to a specified length by adding spaces on the
      * left, thus justifying the string to the right margin. This is extremely useful in generating columnar
      * output in textual tables.
      *
      * @param v     a floating point value to convert to a string and justify
      * @param width the number of characters to pad the string to
      * @return a string representation of the input, padded on the left with spaces to achieve the desired
      *         length.
      */
     public static String rightJustify(float v, int width) {
         return rightJustify(Float.toString(v), width);
     }
 
     /**
      * The <code>rightJustify()</code> method pads a string to a specified length by adding spaces on the
      * left, thus justifying the string to the right margin. This is extremely useful in generating columnar
      * output in textual tables.
      *
      * @param s     a string to justify
      * @param width the number of characters to pad the string to
      * @return a string representation of the input, padded on the left with spaces to achieve the desired
      *         length.
      */
     public static String rightJustify(String s, int width) {
         StringBuffer buf = new StringBuffer(width);
         for (int pad = width - s.length(); pad > 0; pad--)
             buf.append(' ');
         buf.append(s);
         return buf.toString();
     }
 
     /**
      * The <code>leftJustify()</code> method pads a string to a specified length by adding spaces on the
      * right, thus justifying the string to the left margin. This is extremely useful in generating columnar
      * output in textual tables.
      *
      * @param v     a long value to convert to a string and justify
      * @param width the number of characters to pad the string to
      * @return a string representation of the input, padded on the right with spaces to achieve the desired
      *         length.
      */
     public static String leftJustify(long v, int width) {
         return leftJustify(Long.toString(v), width);
     }
 
     /**
      * The <code>leftJustify()</code> method pads a string to a specified length by adding spaces on the
      * right, thus justifying the string to the left margin. This is extremely useful in generating columnar
      * output in textual tables.
      *
      * @param v     a floating point value to convert to a string and justify
      * @param width the number of characters to pad the string to
      * @return a string representation of the input, padded on the right with spaces to achieve the desired
      *         length.
      */
     public static String leftJustify(float v, int width) {
         return leftJustify(Float.toString(v), width);
     }
 
     /**
      * The <code>leftJustify()</code> method pads a string to a specified length by adding spaces on the
      * right, thus justifying the string to the left margin. This is extremely useful in generating columnar
      * output in textual tables.
      *
      * @param s     a string to justify
      * @param width the number of characters to pad the string to
      * @return a string representation of the input, padded on the right with spaces to achieve the desired
      *         length.
      */
     public static String leftJustify(String s, int width) {
         StringBuffer buf = new StringBuffer(s);
         for (int pad = width - s.length(); pad > 0; pad--)
             buf.append(' ');
         return buf.toString();
     }
 
     public static final int SECS_PER_DAY = 3600 * 24;
     public static final int SECS_PER_HOUR = 3600;
     public static final int SECS_PER_MIN = 60;
 
     public static final long MILLISECS_PER_DAY = 3600 * 24 * 1000;
     public static final long MILLISECS_PER_HOUR = 3600 * 1000;
     public static final long MILLISECS_PER_MIN = 60 * 1000;
     public static final long MILLISECS_PER_SEC = 1000;
 
     public static String milliToSecs(long millis) {
         long secs = millis / 1000;
         millis = millis % 1000;
         StringBuffer buf = new StringBuffer(10);
         buf.append(secs);
         buf.append('.');
 
         if (millis < 100) buf.append('0');
         if (millis < 10) buf.append('0');
         buf.append(millis);
         return buf.toString();
     }
 
     public static final int DAYS = 0;
     public static final int HOURS = 1;
     public static final int MINS = 2;
     public static final int SECS = 3;
     public static final int MILLIS = 4;
 
     /**
      * The <code>millisToDays()</code> method converts the given milliseconds into a breakdown of days, hours,
      * minutes, seconds, and milliseconds, returning a long array where the expr 0 corresponds to days, expr 1
      * corresponds to hours, etc.
      *
      * @param millis the number of milliseconds to convert
      * @return the breakdown of milliseconds into days, hours, minutes, seconds, and milliseconds in an array,
      *         with most significant units first
      */
     public static long[] millisToDays(long millis) {
         return Arithmetic.modulus(millis, DENOM);
     }
 
     /**
      * The <code>toHex()</code> converts the specified long value into a hexadecimal string of the given with.
      * The value will be padded on the left with zero values to achieve the desired with.
      *
      * @param value the long value to convert to a string
      * @param width the desired length of the string
      * @return a hexadecimal string representation of the given value, padded on the left with zeroes to the
      *         length specified
      */
     public static String toHex(long value, int width) {
         char[] result = new char[width];
         return convertToHex(value, width, 0, result);
     }
 
     private static String convertToHex(long value, int width, int start, char[] result) {
         if (value > (long)1 << width * 4) {
             StringBuffer buf = new StringBuffer();
             for ( int cntr = 0; cntr < start; cntr++ ) buf.append(result[cntr]);
             buf.append(Long.toHexString(value).toUpperCase());
             return buf.toString();
         }
 
         int i = start + width - 1;
         for (int cntr = 0; cntr < width; cntr++) {
             result[i - cntr] = HEX_CHARS[(int)(value >> (cntr * 4)) & 0xf];
         }
 
         return new String(result);
     }
 
     public static String to0xHex(long value, int width) {
         char[] result = new char[width+2];
         result[0] = '0';
         result[1] = 'x';
         return convertToHex(value, width, 2, result);
     }
 
     public static String toBin(long value, int width) {
         char[] result = new char[width];
 
         for (int cntr = 0; cntr < width; cntr++)
             result[width - cntr - 1] = (value & (0x1 << cntr)) == 0 ? '0' : '1';
 
         return new String(result);
     }
 
     public static void toHex(StringBuffer buf, long value, int width) {
         if (value > (long)1 << width * 4) {
             buf.append(Long.toHexString(value).toUpperCase());
             return;
         }
 
         for (int cntr = width - 1; cntr >= 0; cntr--)
             buf.append(HEX_CHARS[(int)(value >> (cntr * 4)) & 0xf]);
     }
 
     public static String splice(String[] a, String[] b) {
         StringBuffer buf = new StringBuffer();
         int cntr = 0;
         for (; cntr < a.length; cntr++) {
             buf.append(a[cntr]);
             if (cntr < b.length) buf.append(b[cntr]);
         }
 
         for (; cntr < b.length; cntr++) {
             buf.append(b[cntr]);
         }
         return buf.toString();
     }
 
     /**
      * The <code>quote()</code> method simply adds double quotes around a string.
      *
      * @param s the string to add double quotes to
      * @return a new string that is the result of concatenating the double quote character, the specified
      *         string, and another double quote character in sequence
      */
     public static String quote(Object s) {
         return QUOTE + s + QUOTE;
     }
 
     /**
      * The <code>squote()</code> method simply adds single quotes around a character.
      *
      * @param c the character to add double quotes to
      * @return a new string that is the result of concatenating the double quote character, the specified
      *         string, and another double quote character in sequence
      */
     public static String squote(char c) {
         return SQUOTE + c + SQUOTE;
     }
 
     /**
      * The <code>embed()</code> method simply adds parentheses around a string.
      *
      * @param s the string to add parentheses to
      * @return a new string that is the result of concatenating the parenthesis character, the specified
      *         string, and another parenthesis character in sequence
      */
     public static String embed(Object s) {
         return LPAREN + s + RPAREN;
     }
 
     public static String embed(Object lead, Object arg) {
         return lead + LPAREN + arg + RPAREN;
     }
 
     public static String embed(Object lead, Object arg1, Object arg2) {
         return lead + LPAREN + arg1 + COMMA_SPACE + arg2 + RPAREN;
     }
 
     public static String embed(Object lead, Object arg1, Object arg2, Object arg3) {
         return lead + LPAREN + arg1 + COMMA_SPACE + arg2 + COMMA_SPACE + arg3 + RPAREN;
     }
 
     public static String embed(Object lead, Object arg1, Object arg2, Object arg3, Object arg4) {
         return lead + LPAREN + arg1 + COMMA_SPACE + arg2 + COMMA_SPACE + arg3 + COMMA_SPACE + arg4 + RPAREN;
     }
 
     public static String commalist(List l) {
         StringBuffer buf = new StringBuffer();
         Iterator i = l.iterator();
         while (i.hasNext()) {
             buf.append(i.next().toString());
             if (i.hasNext()) buf.append(',');
         }
         return buf.toString();
     }
 
     public static String linelist(List l) {
         StringBuffer buf = new StringBuffer();
         linelist(buf, l);
         return buf.toString();
     }
 
     public static void linelist(StringBuffer buf, List l) {
         Iterator i = l.iterator();
         while (i.hasNext()) {
             buf.append(i.next().toString());
             buf.append('\n');
         }
     }
 
     public static String commalist(Object o1, Object o2) {
         return o1 + COMMA + o2;
     }
 
     public static String commalist(Object o1, Object o2, Object o3) {
         return o1 + COMMA + o2 + COMMA + o3;
     }
 
     public static String commalist(Object o1, Object o2, Object o3, Object o4) {
         return o1 + COMMA + o2 + COMMA + o3 + COMMA + o4;
     }
 
     public static String interval(int low, int high) {
         return "[" + low + ", " + high + ']';
     }
 
     public static char alpha(int num) {
         return (char)('a' + num - 1);
     }
 
     public static String qembed(String s1, String s2, String s3) {
         return s1 + ' ' + quote(s2) + ' ' + s3;
     }
 
     public static int evaluateIntegerLiteral(String val) {
         if (StringUtil.isHex(val) )// hexadecimal
             return Integer.parseInt(val.substring(2), 16);
         if (val.startsWith("$"))                          // hexadecimal
             return Integer.parseInt(val.substring(1), 16);
 
         if (StringUtil.isBin(val)) // binary
             return Integer.parseInt(val.substring(2), 2);
 
         if (val.startsWith("0"))                          // octal
             return Integer.parseInt(val, 8);
 
         return Integer.parseInt(val);
     }
 
     public static String evaluateStringLiteral(String literal) {
         StringBuffer buffer = new StringBuffer(literal.length());
         CharacterIterator i = new StringCharacterIterator(literal);
 
         while (true) {
             char c = i.current();
             i.next();
 
             if (c == CharacterIterator.DONE) break;
             if (c == '"' ) continue; // remove all (unescaped) quotes
             if (c == '\\') c = escapeChar(i);
 
             buffer.append(c);
         }
 
         return buffer.toString();
     }
 
     public static char evaluateCharLiteral(String literal) {
         CharacterIterator i = new StringCharacterIterator(literal);
 
         if (!peekAndEat(i, '\''))
             throw invalidCharLiteral(literal);
 
         char c = i.next();
         if (c == '\\') c = escapeChar(i);
 
         if (i.next() != '\'')
             throw invalidCharLiteral(literal);
 
         if (i.next() != CharacterIterator.DONE)
             throw invalidCharLiteral(literal);
 
         return c;
     }
 
     private static char escapeChar(CharacterIterator i) {
         char c = i.next();
         switch (c) {
             case 'f':
                 return '\f';
             case 'b':
                 return '\b';
             case 'n':
                 return '\n';
             case 'r':
                 return '\r';
             case '\\':
                 return '\\';
             case '\'':
                 return '\'';
             case 't':
                 return '\t';
             case 'x':
                 { /* hexadecimal constant */
                     int value = readHexValue(i, 4);
                     return (char)value;
                 }
             case '0': /* octal constant */
             case '1':
             case '2':
             case '3':
             case '4':
             case '5':
             case '6':
             case '7':
                 {
                     i.previous();
                     int value = readOctalValue(i, 3);
                     return (char)value;
                 }
 
         }
         return c;
     }
 
     private static IllegalArgumentException invalidCharLiteral(String lit) {
         return new IllegalArgumentException("Invalid character literal: " + lit);
     }
 
     public static String trimquotes(String s) {
         if (s.length() == 0) return s;
 
         int start = 0, end = s.length();
         if (s.charAt(start) == '\"') start++;
         if (s.charAt(end - 1) == '\"') end--;
 
         if (start < end)
             return s.substring(start, end);
         else
             return "";
     }
 
     public static String formatParagraphs(String s, int leftJust, int indent, int width) {
         int len = s.length();
         indent += leftJust;
         int consumed = indent + leftJust;
         String indstr = dup(' ', indent);
         String ljstr = dup(' ', leftJust);
         StringBuffer buf = new StringBuffer(indstr);
         int lastSp = -1;
         for (int cntr = 0; cntr < len; cntr++) {
             char c = s.charAt(cntr);
             if (c == '\n') {
                 buf.append('\n');
                 consumed = indent;
                 buf.append(indstr);
                 continue;
             } else if (Character.isWhitespace(c)) {
                 lastSp = buf.length();
             }
             buf.append(c);
             consumed++;
 
             if (consumed > width) {
                 if (lastSp >= 0) {
                     buf.setCharAt(lastSp, '\n');
                     buf.insert(lastSp + 1, ljstr);
                     consumed = buf.length() - lastSp + leftJust - 1;
                 }
             }
         }
         return buf.toString();
     }
 
     public static List trimLines(String s, int indent, int width) {
         LinkedList list = new LinkedList();
         int len = s.length();
         int consumed = indent;
         String indstr = dup(' ', indent);
         StringBuffer buf = new StringBuffer(s.length());
         buf.append(indstr);
         int lastSp = -1;
         for (int cntr = 0; cntr < len; cntr++) {
             char c = s.charAt(cntr);
             if (c == '\n') {
                 buf = newBuffer(indstr, buf, list);
                 consumed = buf.length();
                 continue;
             } else if (Character.isWhitespace(c)) {
                 lastSp = consumed;
             }
             buf.append(c);
             consumed++;
 
             if (consumed > width) {
                 if (lastSp >= 0) {
                     String leftover = buf.substring(lastSp+1); // get leftover already in the buffer
                     buf.setLength(lastSp); // trim off any stuff after the last space
                     buf = newBuffer(leftover, buf, list); // create new buffer and add last to list
                     consumed = buf.length();
                 }
             }
         }
         if ( buf.length() > 0 ) list.add(buf.toString());
         return list;
     }
 
     static StringBuffer newBuffer(String n, StringBuffer old, List strs) {
         strs.add(old.toString());
         return new StringBuffer(n);
     }
 
     /**
      * The <code>dup()</code> method takes a character and a count and returns a string where that character
      * has been duplicated the specified number of times.
      *
      * @param c   the character to duplicate
      * @param len the number of times to duplicate the character
      * @return a string representation of the particular character duplicated the specified number of times
      */
     public static String dup(char c, int len) {
         StringBuffer buf = new StringBuffer(len);
         for (int cntr = 0; cntr < len; cntr++) {
             buf.append(c);
         }
         return buf.toString();
     }
 
     // TODO: test this routine with negative numbers!
     public static String toFixedFloat(float fval, int places) {
         StringBuffer buf = new StringBuffer(places+5);
         // append the whole part
         long val = (long)fval;
         buf.append(val);
         // append the fractional part
         float fract = fval >= 0 ? fval - val : val - fval;
         appendFract(buf, fract, places);
 
         return buf.toString();
     }
 
     public static String toDecimal(long val, int places) {
         StringBuffer buf = new StringBuffer(10+places);
         while ( places > 0 ) {
             buf.append(val % 10);
             places--;
             val = val / 10;
             if ( places == 0 ) buf.append('.');
         }
         buf.reverse();
         return val+buf.toString();
 
     }
 
     public static String toMultirepString(int value, int bits) {
         StringBuffer buf = new StringBuffer(bits * 3 + 8);
 
         buf.append("0x");
         int hexdigs = (bits + 3) / 4;
         toHex(buf, value, hexdigs);
 
         buf.append(" [");
         // append each of the bits
         for ( int bit = bits - 1; bit >= 0; bit-- )
             buf.append(Arithmetic.getBit(value, bit) ? '1' : '0');
 
         buf.append("] (");
         buf.append(value);
         buf.append(") ");
         if ( bits < 9 ) {
             appendChar(value, buf);
         }
         return buf.toString();
     }
 
     private static void appendChar(int value, StringBuffer buf) {
         switch ( value ) {
             case '\n':
                 buf.append("'\\n'");
                 break;
             case '\r':
                 buf.append("'\\r'");
                 break;
             case '\b':
                 buf.append("'\\b'");
                 break;
             case '\t':
                 buf.append("'\\t'");
                 break;
            default:
                 if ( value >= 32 ) {
                     buf.append(SQUOTE);
                     buf.append((char)value);
                     buf.append(SQUOTE);
                 }
         }
     }
 
     public static char toBit(boolean f) {
         return f ? '1' : '0';
     }
 
     public static int ID_LENGTH = 4;
     public static int TIME_LENGTH = 12;
     public static boolean REPORT_SECONDS = false;
     public static int SECONDS_PRECISION = 6;
 
     public static void appendSecs(StringBuffer buf2, long seconds) {
         long[] res = Arithmetic.modulus(seconds, DAYSECS);
         for ( int cntr = 0; cntr < res.length; cntr++ ) {
             if ( cntr > 0 ) {
                 buf2.append(':');
                 if ( res[cntr] < 10 )
                     buf2.append('0');
             }
             buf2.append(res[cntr]);
         }
     }
 
     public static void appendFract(StringBuffer buf, double val, int digits) {
         int cntr = 0;
         for ( int radix = 10; cntr < digits; radix = radix*10, cntr++ ) {
             if ( cntr == 0 ) buf.append('.');
             int digit = (int)(val*radix) % 10;
             buf.append((char)(digit + '0'));
         }
     }
 
     public static String stringReplace(String template, Properties p, Object o1) {
         p.setProperty("1", o1.toString());
         return stringReplace(template, p);
     }
 
     public static String stringReplace(String template, Properties p, Object o1, Object o2) {
         p.setProperty("1", o1.toString());
         p.setProperty("2", o2.toString());
         return stringReplace(template, p);
     }
 
     public static String stringReplace(String template, Properties p, Object o1, Object o2, Object o3) {
         p.setProperty("1", o1.toString());
         p.setProperty("2", o2.toString());
         p.setProperty("3", o3.toString());
         return stringReplace(template, p);
     }
 
     public static String stringReplace(String template, Properties p) {
         int max = template.length();
         StringBuffer buf = new StringBuffer(max);
         for ( int pos = 0; pos < max; pos++ ) {
             char ch = template.charAt(pos);
             if ( ch == '$' ) {
                 pos = replaceVar(pos, max, template, buf, p);
             } else if ( ch == '%' ) {
                 pos = replaceVarQuote(pos, max, template, buf, p);
             } else {
                 buf.append(ch);
             }
         }
         return buf.toString();
     }
 
     private static int replaceVar(int pos, int max, String template, StringBuffer buf, Properties p) {
         StringBuffer var = new StringBuffer(10);
         pos = scanAhead(pos, '$', max, template, buf, var);
         String result = getProperty(var, p);
         buf.append(result);
         return pos;
     }
 
     private static int scanAhead(int pos, char ch, int max, String template, StringBuffer buf, StringBuffer var) {
         for ( pos++; pos < max; pos++) {
             char vch = template.charAt(pos);
             if ( !Character.isLetterOrDigit(vch) ) { pos--; break; }
             if ( vch == ch ) { buf.append(ch); break; }
             var.append(vch);
         }
         return pos;
     }
 
     private static String getProperty(StringBuffer var, Properties p) {
         String varname = var.toString();
         String result = p.getProperty(varname);
         if ( result == null ) throw Util.failure("stringReplace(): unknown variable "+quote(varname));
         return result;
     }
 
     private static int replaceVarQuote(int pos, int max, String template, StringBuffer buf, Properties p) {
         StringBuffer var = new StringBuffer(10);
         pos = scanAhead(pos, '%', max, template, buf, var);
         String result = getProperty(var, p);
         buf.append('"');
         buf.append(result);
         buf.append('"');
         return pos;
     }
 
 }

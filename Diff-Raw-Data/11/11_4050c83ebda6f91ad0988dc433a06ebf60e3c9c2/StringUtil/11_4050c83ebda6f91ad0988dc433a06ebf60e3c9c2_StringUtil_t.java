 /**
  * Copyright (c) 2004, Regents of the University of California
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
 
 package avrora.util;
 
 import avrora.Avrora;
 
 import java.text.CharacterIterator;
 import java.text.StringCharacterIterator;
 import java.util.List;
 import java.util.Iterator;
 
 /**
  * The <code>StringUtil</code> class implements several useful functions for dealing
  * with strings such as parsing pieces of syntax, formatting, etc.
  *
  * @author Ben L. Titzer
  */
 public class StringUtil {
     public static char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7',
                                       '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
     public static final String QUOTE = "\"".intern();
     public static final String SQUOTE = "'".intern();
     public static final String LPAREN = "(".intern();
     public static final String RPAREN = ")".intern();
     public static final String COMMA = ",".intern();
     public static final String COMMA_SPACE = ", ".intern();
     public static String[] EMPTY_STRING_ARRAY = {};
 
     public static String addrToString(int address) {
         return "0x" + toHex(address, 4);
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
 
     public static void skipWhiteSpace(CharacterIterator i) {
         while (true) {
             char c = i.current();
             if (c == ' ' || c == '\n' || c == '\t')
                 i.next();
             else
                 break;
         }
     }
 
     public static void skipToCharacter(CharacterIterator i, char c) {
         throw Avrora.unimplemented();
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
 
     private void expectChar(CharacterIterator i, char c) throws Exception {
         char r = i.current();
         i.next();
         if (r != c)
             throw new Exception("expected " + squote(c) + " @ "
                     + (i.getIndex() - 1) + ", found " + squote(r));
     }
 
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
 
     public static String rightJustify(long v, int width) {
         return rightJustify(Long.toString(v), width);
     }
 
     public static String rightJustify(float v, int width) {
         return rightJustify(Float.toString(v), width);
     }
 
     public static String rightJustify(String s, int width) {
         StringBuffer buf = new StringBuffer();
         for (int pad = width - s.length(); pad > 0; pad--)
             buf.append(' ');
         buf.append(s);
         return buf.toString();
     }
 
     public static String leftJustify(long v, int width) {
         return leftJustify(Long.toString(v), width);
     }
 
     public static String leftJustify(float v, int width) {
         return leftJustify(Float.toString(v), width);
     }
 
     public static String leftJustify(String s, int width) {
         StringBuffer buf = new StringBuffer(s);
         for (int pad = width - s.length(); pad > 0; pad--)
             buf.append(' ');
         return buf.toString();
     }
 
     public static int SECS_PER_DAY = 3600 * 24;
     public static int SECS_PER_HOUR = 3600;
     public static int SECS_PER_MIN = 60;
 
    public static long MILLISECS_PER_DAY = 3600 * 24 * 1000;
    public static long MILLISECS_PER_HOUR = 3600 * 1000;
    public static long MILLISECS_PER_MIN = 60 * 1000;
    public static long MILLISECS_PER_SEC = 1000;
 
     public static String milliAsString(long millis) {
         StringBuffer buf = new StringBuffer();
 
        if (millis >= MILLISECS_PER_DAY) {
             int days = (int) (millis / MILLISECS_PER_DAY);
             buf.append(days + "d ");
             millis = millis - days * MILLISECS_PER_DAY;
         }
 
         if (millis >= MILLISECS_PER_HOUR) {
             int hours = (int) (millis / MILLISECS_PER_HOUR);
             buf.append(hours + "h ");
             millis = millis - hours * MILLISECS_PER_HOUR;
         }
         if (millis >= MILLISECS_PER_MIN) {
             int min = (int) (millis / MILLISECS_PER_MIN);
             buf.append(min + "m ");
             millis = millis - min * MILLISECS_PER_MIN;
         }
         if (millis >= MILLISECS_PER_SEC) {
             int secs = (int) (millis / MILLISECS_PER_SEC);
             buf.append(secs + ".");
             millis = millis - secs * MILLISECS_PER_SEC;
         } else {
             buf.append("0.");
         }
         if ( millis < 100 )
             buf.append('0');
         if ( millis < 10 )
             buf.append('0');
         buf.append(millis + "s");
         return buf.toString();
     }
 
     public static String toHex(long value, int width) {
         char result[] = new char[width];
 
         if (value > (long) 1 << width * 4) return Long.toHexString(value).toUpperCase();
 
         for (int cntr = 0; cntr < width; cntr++)
             result[width - cntr - 1] = HEX_CHARS[(int) (value >> (cntr * 4)) & 0xf];
 
         return new String(result);
     }
 
     public static String quote(Object s) {
         return QUOTE + s + QUOTE;
     }
 
     public static String squote(char c) {
         return SQUOTE + c + SQUOTE;
     }
 
     public static String embed(Object s) {
         return LPAREN + s + RPAREN;
     }
 
     public static String embed(Object lead, Object arg) {
         return lead + LPAREN + arg + RPAREN;
     }
 
     public static String embed(Object lead, Object arg1, Object arg2) {
         return lead + LPAREN + arg1 + COMMA + arg2 + RPAREN;
     }
 
     public static String embed(Object lead, Object arg1, Object arg2, Object arg3) {
         return lead + LPAREN + arg1 + COMMA + arg2 + COMMA + arg3 + RPAREN;
     }
 
     public static String embed(Object lead, Object arg1, Object arg2, Object arg3, Object arg4) {
         return lead + LPAREN + arg1 + COMMA + arg2 + COMMA + arg3 + COMMA + arg4 + RPAREN;
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
         return "[" + low + ", " + high + "]";
     }
 
     public static char alpha(int num) {
         return (char) ('a' + num - 1);
     }
 
     public static String qembed(String s1, String s2, String s3) {
         return s1 + " " + quote(s2) + " " + s3;
     }
 
     public static int evaluateIntegerLiteral(String val) {
         if (val.startsWith("0x") || val.startsWith("0X")) // hexadecimal
             return Integer.parseInt(val.substring(2), 16);
         if (val.startsWith("$"))                          // hexadecimal
             return Integer.parseInt(val.substring(1), 16);
 
         if (val.startsWith("0b") || val.startsWith("0B")) // binary
             return Integer.parseInt(val.substring(2), 2);
 
         if (val.startsWith("0"))                          // octal
             return Integer.parseInt(val, 8);
 
         return Integer.parseInt(val);
     }
 
     public static String evaluateStringLiteral(String literal) {
         StringBuffer buffer = new StringBuffer(literal.length());
         CharacterIterator i = new StringCharacterIterator(literal);
 
         while (true) {
             char c = i.next();
 
             if (c == CharacterIterator.DONE) break;
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
                     return (char) value;
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
                     return (char) value;
                 }
 
         }
         return c;
     }
 
     private static IllegalArgumentException invalidCharLiteral(String lit) {
         return new IllegalArgumentException("Invalid character literal: " + lit);
     }
 
     public static String trimallquotes(String s) {
         int start, end;
 
         // TODO: could use some cleanup with StringUtil
         // TODO: this is buggy!
 
         for (start = 0; start < s.length(); start++)
             if (s.charAt(start) != '\"') break;
 
         for (end = s.length() - 1; end >= 0; end--)
             if (s.charAt(end) != '\"') break;
 
         if (start < end)
             return s.substring(start, end);
         else
             return "";
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
 
     public static String makeParagraphs(String s, int leftJust, int indent, int width) {
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
 
     public static String dup(char c, int len) {
         StringBuffer buf = new StringBuffer(len);
         for (int cntr = 0; cntr < len; cntr++) {
             buf.append(c);
         }
         return buf.toString();
     }
 
 }

 /*
  *  Copyright 2009 Richard Nichols.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package com.visural.common;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.Collection;
 import java.util.StringTokenizer;
 
 /**
  * String handling utiltities
  * 
  * @version $Id: StringUtil.java 98 2010-11-21 06:22:39Z tibes80@gmail.com $
  * @author Richard Nichols
  */
 public class StringUtil {
     
     public static final char[] FILENAME_INVALID_CHARS = new char[] {'/', '\\', '?', '%', '*', ':', '|', '"', '<', '>'};
 
     /**
      * Returns true if the filename does not contain any invalid characters.
      * Based on http://en.wikipedia.org/wiki/Filename
      * @param filename
      * @return
      */
     public static boolean validFilename(String filename) {
         return !containsAnyChar(filename, FILENAME_INVALID_CHARS);
     }
 
     /**
      * Returns true if the string contains any of the given characters
      * @param string
      * @param chars
      * @return
      */
     public static boolean containsAnyChar(String string, char... chars) {
         if (string != null) {
             for (int n = 0; n < string.length(); n++) {
                 char c = string.charAt(n);
                 for (char test : chars) {
                     if (test == c) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 
     public static String getStackTrace(Throwable t) {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         t.printStackTrace(pw);
         pw.flush(); sw.flush();
         pw.close();
         return sw.toString();
     }
 
     /**
      * Converts a byte array into a textual representation in base-16 (hex).
      * @param raw byte array
      * @return string representation of byte array
      */
     public static String byteArrayToHexString(byte[] raw) {
         try {
             byte[] hex = new byte[2 * raw.length];
             int index = 0;
             for (byte b : raw) {
                 int v = b & 0xFF;
                 hex[index++] = HEX_CHAR_TABLE[v >>> 4];
                 hex[index++] = HEX_CHAR_TABLE[v & 0xF];
             }
             return new String(hex, "ASCII");
         } catch (UnsupportedEncodingException ex) {
             throw new AssertionError("'ASCII' was not able to be used as encoding " +
                     "setting for String - this should never occur.");
         }
     }
     static final byte[] HEX_CHAR_TABLE = {
         (byte) '0', (byte) '1', (byte) '2', (byte) '3',
         (byte) '4', (byte) '5', (byte) '6', (byte) '7',
         (byte) '8', (byte) '9', (byte) 'a', (byte) 'b',
         (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'
     };
 
     /**
      * @param str string to inspect
      * @return true if string is null or empty
      */
     public static boolean isEmptyStr(String str) {
         return (str == null || str.length() == 0);
     }
 
     /**
      * @param str string to inspect
      * @return true if string is null or empty or blank (i.e. whitespace only)
      */
     public static boolean isBlankStr(String str) {
         return (str == null || str.trim().length() == 0);
     }
 
     /**
      * @param str string to inspect
      * @return true if string is not null or empty
      */
     public static boolean isNotEmptyStr(String str) {
         return !(str == null || str.length() == 0);
     }
 
     /**
      * @param str string to inspect
      * @return true if string is not null or empty or blank (i.e. whitespace only)
      */
     public static boolean isNotBlankStr(String str) {
         return !(str == null || str.trim().length() == 0);
     }
 
     /**
      * Common conversion for text representation of booleans.
      * @param str string to parse
      * @return true if string matches to Y, YES, TRUE, ON, 1 - case
      *          insensitive, false when the string is blank or otherwise
      */
     public static boolean parseBoolean(String str) {
         return parseBoolean(str, false);
     }
 
     /**
      * Common conversion for text representation of booleans.
      * @param str string to parse
      * @param whenBlank the value to return when the string is blank
      * @return true if string matches to Y, YES, TRUE, ON, 1 - case insensitive
      */
     public static boolean parseBoolean(String str, boolean whenBlank) {
         return (isBlankStr(str) ? whenBlank : (str.toUpperCase().equals("Y") ||
                 str.toUpperCase().equals("YES") ||
                 str.toUpperCase().equals("TRUE") ||
                 str.toUpperCase().equals("ON") ||
                 str.toUpperCase().equals("1")));
     }
 
     /**
      * Trims the given string to the length given.
      * @param str
      * @param len
      * @return
      */
     public static String trimToLength(String str, int len) {
         if (str == null) {
             return null;
         } else if (str.length() > len) {
             return (len >= 0 ? str.substring(0, len) : "");
         } else {
             return str;
         }
     }
 
     /**
      * Adds whitespace to the left hand side of a block of line broken text.
      * @param str the block of text
      * @param indent number of characters to indent by
      * @return indented text
      */
     public static String blockIndent(String str, int indent) {
         if (str == null) {
             return null;
         }
         StringBuffer buildIndent = new StringBuffer("");
         for (int n = 0; n < indent; n++) {
             buildIndent.append(' ');
         }
         String indentBlock = buildIndent.toString();
         StringBuffer result = new StringBuffer();
         String[] lines = str.split("\n");
         for (String line : lines) {
             result.append(indentBlock);
             result.append(line);
             result.append('\n');
         }
         return result.toString();
     }
 
     /**
      * Escapes special characters in the string for HTML output
      * @param text the text to be escaped
      * @return escaped representation of the string
      */
     private static String htmlEscape(String text, boolean isAttribute, boolean convertNewlines) {
         if (text == null) {
             return null;
         }
         char content[] = new char[text.length()];
         text.getChars(0, text.length(), content, 0);
         StringBuffer result = new StringBuffer(content.length + 50);
         for (int i = 0; i < content.length; i++) {
             switch (content[i]) {
                 case '<':
                     result.append((isAttribute ? "&#x3C" : "&lt;"));
                     break;
                 case '>':
                     result.append((isAttribute ? "&#x3E" : "&gt;"));
                     break;
                 case '&':
                     result.append((isAttribute ? "&#x26;" : "&amp;"));
                     break;
                 case '"':
                     result.append((isAttribute ? "&#x22;" : "&quot;"));
                     break;
                 case '\'':
                     result.append("&#x27;");
                     break;
                 case '\n':
                     result.append((convertNewlines ? (isAttribute ? "&#10;" : "<br/>") : "\n"));
                     break;
                 default:
                     result.append(content[i]);
             }
         }
         return result.toString();
     }
 
     /**
      * Escaping mechanism for standard-text-to-html, will not touch `\n' newline characters.
      * @param text
      * @param convertNewlines
      * @return
      */
     public static String htmlEscape(String text, boolean convertNewlines) {
        return htmlEscape(text, false, convertNewlines);
     }
 
     /**
     * Escaping mechanism for standard text-to-HTML
      * @param text
      * @return
      */
     public static String htmlEscape(String text) {
        return htmlEscape(text, false);
     }
 
     /**
      * Escaping mechanism for HTML tag attribute strings.
      * @param text
      * @return
      */
     public static String htmlAttributeEscape(String text) {
        return htmlEscape(text, true, false);
     }
 
     /**
      * Formats a double as a decimal string with a fixed number of decimal places.
      * @param number
      * @param numPlaces
      * @return
      */
     public static String formatDecimal(double number, int numPlaces) {
         return formatDecimal(number, numPlaces, numPlaces);
     }
 
     /**
      * Formats a double as decimal string with a variable number of decimal places.
      * @param number
      * @param minPlaces
      * @param maxPlaces
      * @return
      */
     public static String formatDecimal(double number, int minPlaces, int maxPlaces) {
         double dFinal = number;
         if (maxPlaces < minPlaces) {
             maxPlaces = minPlaces;
         }
         StringBuffer format = new StringBuffer("###############0");
         if (minPlaces > 0) {
             format.append(".");
             for (int n = 0; n < minPlaces; n++) {
                 format.append("0");
             }
             for (int n = 0; n < (maxPlaces - minPlaces); n++) {
                 format.append("#");
             }
         }
         NumberFormat nf = new DecimalFormat(format.toString());
         return nf.format(dFinal);
     }
 
     /**
      * Reverses the characters in the given string.
      * @param str
      * @return
      */
     public static String reverse(String str) {
         char[] ac = new char[str.length()];
         for (int n = 0; n < str.length(); n++) {
             ac[n] = str.charAt(str.length() - 1 - n);
         }
         return new String(ac);
     }
 
     /**
      * Rotates the characters in the given string by offset.
      * @param str
      * @param offset
      * @return
      */
     public static String rotate(String str, int offset) {
         char[] ac = new char[str.length()];
         for (int n = 0; n < str.length(); n++) {
             ac[n] = str.charAt((n + offset) % str.length());
         }
         return new String(ac);
     }
 
     /**
      * Line breaks a string at a given number of max characters per line. Words
      * wrap to next line.
      * @param str
      * @param charsPerLine
      * @return
      */
     public static String wordLineBreak(String str, int charsPerLine) {
         String sa[] = str.split(" ");
         StringBuffer result = new StringBuffer("");
         int count = 0;
         for (int n = 0; n < sa.length; n++) {
             if (count + sa[n].length() > charsPerLine) {
                 result.append("\n");
                 result.append(sa[n]);
                 count = 0;
             } else {
                 result.append(" ");
                 result.append(sa[n]);
                 count += sa[n].length();
             }
         }
         return result.toString();
     }
 
     /**
      * Counts the number of instances of a given character in a string.
      * @param s
      * @param c
      * @return
      */
     public static int countCharInstances(String s, char c) {
         int nResult = 0;
         if (s != null) {
             for (int n = 0; n < s.length(); n++) {
                 if (s.charAt(n) == c) {
                     nResult++;
                 }
             }
         }
         return nResult;
     }
 
     /**
      * Takes a monocased string (e.g. all upper/lower case) and attempts to prettify it
      * by making the first letter of every word uppercase.
      * @param monoCased
      * @return
      */
     public static String prettifyMonoCasedString(String monoCased) {
         if (monoCased == null) {
             return null;
         }
         StringTokenizer st = new StringTokenizer(monoCased.toUpperCase(), " \n\r\t,.", true);
         StringBuffer sb = new StringBuffer(monoCased.length());
         while (st.hasMoreTokens()) {
             String current = st.nextToken();
             if (current.length() > 1 && current.charAt(0) >= 'A' && current.charAt(0) <= 'Z') {
                 sb.append(current.substring(0, 1) + current.substring(1).toLowerCase());
             } else {
                 sb.append(current);
             }
         }
         return sb.toString();
     }
 
     /**
      * Implements Levenshtein edit distance between two strings
      *
      * Note - does not scale well to long strings - uses O(n*m) memory where
      * m & n are the respective lengths of the strings
      * 
      * Reference algorithm - http://en.wikipedia.org/wiki/Levenshtein_distance
      *
      * @param s1
      * @param s2
      * @return
      */
     public static int editDistance(String s1, String s2) {
         int[][] d = new int[s1.length() + 1][s2.length() + 1];
 
         for (int i = 0; i < s1.length() + 1; i++) {
             d[i][0] = i;
         }
         for (int j = 0; j < s2.length() + 1; j++) {
             d[0][j] = j;
         }
 
         for (int i = 1; i <= s1.length(); i++) {
             for (int j = 1; j <= s2.length(); j++) {
                 int cost = 1;
                 if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                     cost = 0;
                 }
                 d[i][j] = Function.minInt(d[i - 1][j] + 1, // insertion
                         d[i][j - 1] + 1, // deletion
                         d[i - 1][j - 1] + cost // substitution
                         );
             }
         }
         return d[s1.length()][s2.length()];
     }
 
     public static <T> String delimitObjectsToString(String delim, T... objects) {
         return delimitObjectsToString(delim, delim, objects);
     }
 
     public static <T> String delimitObjectsToString(String delim, String lastDelim, T... objects) {
         return delimitObjectsToString(delim, lastDelim, false, objects);
     }
 
     public static <T> String delimitObjectsToString(String delim, String lastDelim, boolean skipNulls, T... objects) {
         return delimitObjectsToString(delim, lastDelim, skipNulls, new StringConverter<T>() {
             public String toString(T object) {
                 return (object == null ? "null" : object.toString());
             }
         }, objects);
     }
 
     public static <T> String delimitObjectsToString(String delim, String lastDelim, boolean skipNulls, StringConverter<T> conv, T... objects) {
         if (objects == null || objects.length == 0) {
             return null;
         }
         if (objects.length == 1 && Collection.class.isAssignableFrom(objects[0].getClass())) {
             objects = (T[])((Collection)objects[0]).toArray();
         }
         StringBuffer sb = new StringBuffer();
         boolean firstDone = false;
         for (int n = 0; n < objects.length; n++) {
             if (firstDone) {
                 if (n == objects.length - 1) {
                     sb.append(Function.nvl(lastDelim, delim));
                 } else {
                     sb.append(delim);
                 }
             }
             if (objects[n] != null || !skipNulls) {
                 sb.append(conv.toString(objects[n]));
                 firstDone = true;
             }
         }
         return sb.toString();
     }
     
     private static final String[] urlReplacements =
             new String[]{
         "%", "%25", // <-- must come first
         "$", "%24",
         "&", "%26",
         "+", "%2B",
         ",", "%2C",
         "/", "%2F",
         ":", "%3A",
         ";", "%3B",
         "=", "%3D",
         "?", "%3F",
         "@", "%40",
         " ", "%20",
         "\"", "%22",
         "<", "%3C",
         ">", "%3E",
         "#", "%23",
         "{", "%7B",
         "}", "%7D",
         "|", "%7C",
         "\\", "%5C",
         "^", "%5E",
         "~", "%7E",
         "[", "%5B",
         "]", "%5D",
         "`", "%60"
     };
 
     /**
      * Escape characters in string for use in a URL
      * @param str
      * @return
      */
     public static String urlEncode(String str) {
         String result = str;
         for (int n = 0; n < urlReplacements.length; n += 2) {
             result = result.replace(urlReplacements[n], urlReplacements[n + 1]);
         }
         return result;
     }
 
 }

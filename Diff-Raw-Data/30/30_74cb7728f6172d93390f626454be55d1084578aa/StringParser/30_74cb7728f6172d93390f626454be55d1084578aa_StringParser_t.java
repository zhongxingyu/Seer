 /*-
  * Copyright (c) 2006, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.logicprobe.LogicMail.util;
 
 import net.rim.device.api.io.Base64InputStream;
 import net.rim.device.api.io.Base64OutputStream;
 import net.rim.device.api.system.EventLogger;
 import net.rim.device.api.util.Arrays;
 import net.rim.device.api.util.DataBuffer;
 import net.rim.device.api.util.DateTimeUtilities;
 import net.rim.device.api.util.NumberUtilities;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.TimeZone;
 import java.util.Vector;
 
 import org.logicprobe.LogicMail.AppInfo;
 
 
 /**
  * This class provides a collection of string parsing
  * utilities that are generally useful for handling
  * E-Mail protocol server responses.
  */
 public class StringParser {
     private static final long ONE_SECOND = 1000;
     private static final long ONE_MINUTE = ONE_SECOND * 60;
     private static final long ONE_HOUR = ONE_MINUTE * 60;
     private static String ENCODING_UTF8 = "UTF-8";
     private static String strCRLF = "\r\n";
     private static String WORD_SPECIALS = "=_?\"#$%&'(),.:;<>@[\\]^`{|}~";
     private static final int MAX_LINE_LEN = 76;
     private static final byte HIGH_BIT = (byte)0x80;
 
     public static final int ENCODING_7BIT = 0;
     public static final int ENCODING_QUOTED_PRINTABLE = 1;
     public static final int ENCODING_BASE64 = 2;
     
     private StringParser() {
     }
 
     /**
      * Parse a string containing a date/time
      * and return a usable Date object.
      *
      * @param rawDate Text containing the date
      * @return Date object instance
      */
     public static Date parseDateString(String rawDate) {
         int p = 0;
         int q = 0;
 
         int[] fields = new int[7];
 
         // Clean up the date string for simple parsing
         p = rawDate.indexOf(",");
 
         if (p != -1) {
             p++;
 
             while (rawDate.charAt(p) == ' ')
                 p++;
 
             rawDate = rawDate.substring(p);
         }
 
         if (rawDate.charAt(rawDate.length() - 1) == ')') {
             rawDate = rawDate.substring(0, rawDate.lastIndexOf(' '));
         }
 
         // Set the time zone
         Calendar cal;
         String tz = rawDate.substring(rawDate.lastIndexOf(' ') + 1);
         p = tz.indexOf(':');
 
         if ((p == -1) || (tz.indexOf(':', p) == -1)) {
             cal = Calendar.getInstance(createTimeZone(tz));
         } else {
             cal = Calendar.getInstance(TimeZone.getDefault());
         }
 
         // Day
         p = 0;
         q = rawDate.indexOf(" ", p + 1);
         fields[2] = Integer.parseInt(rawDate.substring(p, q).trim());
 
         // Month
         p = q + 1;
         q = rawDate.indexOf(" ", p + 1);
 
         String monthStr = rawDate.substring(p, q);
 
         if (monthStr.equals("Jan")) {
             fields[1] = 0;
         } else if (monthStr.equals("Feb")) {
             fields[1] = 1;
         } else if (monthStr.equals("Mar")) {
             fields[1] = 2;
         } else if (monthStr.equals("Apr")) {
             fields[1] = 3;
         } else if (monthStr.equals("May")) {
             fields[1] = 4;
         } else if (monthStr.equals("Jun")) {
             fields[1] = 5;
         } else if (monthStr.equals("Jul")) {
             fields[1] = 6;
         } else if (monthStr.equals("Aug")) {
             fields[1] = 7;
         } else if (monthStr.equals("Sep")) {
             fields[1] = 8;
         } else if (monthStr.equals("Oct")) {
             fields[1] = 9;
         } else if (monthStr.equals("Nov")) {
             fields[1] = 10;
         } else if (monthStr.equals("Dec")) {
             fields[1] = 11;
         }
 
         // Year
         p = q + 1;
         q = rawDate.indexOf(" ", p + 1);
         fields[0] = Integer.parseInt(rawDate.substring(p, q).trim());
 
         if (fields[0] < 100) {
             // Handle 2-digit years according to RFC 2822:
             // 50-99 is assumed to be 19xx
             // 00-49 is assumed to be 20xx
             if (fields[0] >= 50) {
                 fields[0] += 1900;
             } else {
                 fields[0] += 2000;
             }
         } else if ((fields[0] >= 100) && (fields[0] < 1000)) {
             // Handle 3-digit years according to RFC 2822
             fields[0] += 2000;
         }
 
         // Hour
         p = q + 1;
         q = rawDate.indexOf(":", p + 1);
         fields[3] = Integer.parseInt(rawDate.substring(p, q).trim());
 
         // Minute and Second
         p = q + 1;
         q = rawDate.indexOf(":", p + 1);
 
         if (q == -1) {
             // The second field is missing, so handle accordingly
             q = rawDate.indexOf(" ", p + 1);
             fields[4] = Integer.parseInt(rawDate.substring(p, q).trim());
             fields[5] = 0;
             fields[6] = 0;
         } else {
             // Otherwise parse minutes and seconds as normal
             fields[4] = Integer.parseInt(rawDate.substring(p, q).trim());
 
             p = q + 1;
             q = rawDate.indexOf(" ", p + 1);
 
             if (q == -1) {
                 q = rawDate.length();
             }
 
             fields[5] = Integer.parseInt(rawDate.substring(p, q).trim());
             fields[6] = 0;
         }
 
         DateTimeUtilities.setCalendarFields(cal, fields);
 
         return cal.getTime();
     }
 
     /**
      * Create a TimeZone object from an input string.
      *
      * @param tz Input string
      * @return TimeZone object
      */
     private static TimeZone createTimeZone(String tz) {
         TimeZone result;
 
         if (tz.startsWith("-") || tz.startsWith("+")) {
             result = TimeZone.getTimeZone("GMT" + tz);
         } else if (tz.indexOf('/') != -1) {
             result = TimeZone.getTimeZone(tz);
         } else if (tz.equals("MIT")) {
             result = TimeZone.getTimeZone("GMT-11:00");
         } else if (tz.equals("HST")) {
             result = TimeZone.getTimeZone("GMT-10:00");
         } else if (tz.equals("AST")) {
             result = TimeZone.getTimeZone("GMT-9:00");
         } else if (tz.equals("PST")) {
             result = TimeZone.getTimeZone("GMT-8:00");
         } else if (tz.equals("PDT")) {
             result = TimeZone.getTimeZone("GMT-7:00");
         } else if (tz.equals("PST8PDT")) {
             result = TimeZone.getTimeZone("GMT-8:00");
         } else if (tz.equals("MST")) {
             result = TimeZone.getTimeZone("GMT-7:00");
         } else if (tz.equals("MDT")) {
             result = TimeZone.getTimeZone("GMT-6:00");
         } else if (tz.equals("MST7MDT")) {
             result = TimeZone.getTimeZone("GMT-7:00");
         } else if (tz.equals("PNT")) {
             result = TimeZone.getTimeZone("GMT-7:00");
         } else if (tz.equals("CST")) {
             result = TimeZone.getTimeZone("GMT-6:00");
         } else if (tz.equals("CDT")) {
             result = TimeZone.getTimeZone("GMT-5:00");
         } else if (tz.equals("CST6CDT")) {
             result = TimeZone.getTimeZone("GMT-6:00");
         } else if (tz.equals("EST")) {
             result = TimeZone.getTimeZone("GMT-5:00");
         } else if (tz.equals("EDT")) {
             result = TimeZone.getTimeZone("GMT-4:00");
         } else if (tz.equals("EST5EDT")) {
             result = TimeZone.getTimeZone("GMT-5:00");
         } else if (tz.equals("IET")) {
             result = TimeZone.getTimeZone("GMT-5:00");
         } else if (tz.equals("PRT")) {
             result = TimeZone.getTimeZone("GMT-4:00");
         } else if (tz.equals("CNT")) {
             result = TimeZone.getTimeZone("GMT-3:5");
         } else if (tz.equals("AGT")) {
             result = TimeZone.getTimeZone("GMT-3:00");
         } else if (tz.equals("BET")) {
             result = TimeZone.getTimeZone("GMT-3:00");
         } else if (tz.equals("UCT")) {
             result = TimeZone.getTimeZone("GMT");
         } else if (tz.equals("UTC")) {
             result = TimeZone.getTimeZone("GMT");
         } else if (tz.equals("WET")) {
             result = TimeZone.getTimeZone("GMT");
         } else if (tz.equals("CET")) {
             result = TimeZone.getTimeZone("GMT+1:00");
         } else if (tz.equals("ECT")) {
             result = TimeZone.getTimeZone("GMT+1:00");
         } else if (tz.equals("MET")) {
             result = TimeZone.getTimeZone("GMT+1:00");
         } else if (tz.equals("ART")) {
             result = TimeZone.getTimeZone("GMT+2:00");
         } else if (tz.equals("CAT")) {
             result = TimeZone.getTimeZone("GMT+2:00");
         } else if (tz.equals("EET")) {
             result = TimeZone.getTimeZone("GMT+2:00");
         } else if (tz.equals("EAT")) {
             result = TimeZone.getTimeZone("GMT+3:00");
         } else if (tz.equals("NET")) {
             result = TimeZone.getTimeZone("GMT+4:00");
         } else if (tz.equals("PLT")) {
             result = TimeZone.getTimeZone("GMT+5:00");
         } else if (tz.equals("IST")) {
             result = TimeZone.getTimeZone("GMT+5:30");
         } else if (tz.equals("BST")) {
             result = TimeZone.getTimeZone("GMT+6:00");
         } else if (tz.equals("VST")) {
             result = TimeZone.getTimeZone("GMT+7:00");
         } else if (tz.equals("CTT")) {
             result = TimeZone.getTimeZone("GMT+8:00");
         } else if (tz.equals("PRC")) {
             result = TimeZone.getTimeZone("GMT+8:00");
         } else if (tz.equals("JST")) {
             result = TimeZone.getTimeZone("GMT+9:00");
         } else if (tz.equals("ROK")) {
             result = TimeZone.getTimeZone("GMT+9:00");
         } else if (tz.equals("ACT")) {
             result = TimeZone.getTimeZone("GMT+9:30");
         } else if (tz.equals("AET")) {
             result = TimeZone.getTimeZone("GMT+10:00");
         } else if (tz.equals("SST")) {
             result = TimeZone.getTimeZone("GMT+11:00");
         } else if (tz.equals("NST")) {
             result = TimeZone.getTimeZone("GMT+12:00");
         } else {
             result = TimeZone.getTimeZone(tz);
         }
 
         return result;
     }
 
     /**
      * Create a proper E-Mail date string from a Java.util.Date object
      *
      * @param time Date to convert
      * @return String to add to an E-Mail
      */
     public static String createDateString(Date time) {
         return createDateString(time, TimeZone.getDefault());
     }
 
     /**
      * Create a proper E-Mail date string from a Java.util.Date object
      *
      * @param time Date to convert
      * @param zone Time zone for the date
      * @return String to add to an E-Mail
      */
     public static String createDateString(Date time, TimeZone zone) {
         Calendar cal = Calendar.getInstance(zone);
         cal.setTime(time);
 
         StringBuffer buf = new StringBuffer();
 
         switch (cal.get(Calendar.DAY_OF_WEEK)) {
         case Calendar.SUNDAY:
             buf.append("Sun, ");
             break;
         case Calendar.MONDAY:
             buf.append("Mon, ");
             break;
         case Calendar.TUESDAY:
             buf.append("Tue, ");
             break;
         case Calendar.WEDNESDAY:
             buf.append("Wed, ");
             break;
         case Calendar.THURSDAY:
             buf.append("Thu, ");
             break;
         case Calendar.FRIDAY:
             buf.append("Fri, ");
             break;
         case Calendar.SATURDAY:
             buf.append("Sat, ");
             break;
         }
 
         buf.append(cal.get(Calendar.DAY_OF_MONTH));
         buf.append(' ');
 
         switch (cal.get(Calendar.MONTH)) {
         case Calendar.JANUARY:
             buf.append("Jan ");
             break;
         case Calendar.FEBRUARY:
             buf.append("Feb ");
             break;
         case Calendar.MARCH:
             buf.append("Mar ");
             break;
         case Calendar.APRIL:
             buf.append("Apr ");
             break;
         case Calendar.MAY:
             buf.append("May ");
             break;
         case Calendar.JUNE:
             buf.append("Jun ");
             break;
         case Calendar.JULY:
             buf.append("Jul ");
             break;
         case Calendar.AUGUST:
             buf.append("Aug ");
             break;
         case Calendar.SEPTEMBER:
             buf.append("Sep ");
             break;
         case Calendar.OCTOBER:
             buf.append("Oct ");
             break;
         case Calendar.NOVEMBER:
             buf.append("Nov ");
             break;
         case Calendar.DECEMBER:
             buf.append("Dec ");
             break;
         }
 
         buf.append(cal.get(Calendar.YEAR));
         buf.append(' ');
 
         buf.append(NumberUtilities.toString(cal.get(Calendar.HOUR_OF_DAY), 10, 2));
         buf.append(':');
         buf.append(NumberUtilities.toString(cal.get(Calendar.MINUTE), 10, 2));
         buf.append(':');
         buf.append(NumberUtilities.toString(cal.get(Calendar.SECOND), 10, 2));
 
         buf.append(' ');
 
         int tzOffset = cal.getTimeZone()
                           .getOffset(1, cal.get(Calendar.YEAR),
                 cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                 cal.get(Calendar.DAY_OF_WEEK),
                 (int) (((long) cal.get(Calendar.HOUR_OF_DAY) * ONE_HOUR) +
                 (cal.get(Calendar.MINUTE) * ONE_MINUTE) +
                 (cal.get(Calendar.SECOND) * ONE_SECOND))) / 36000;
 
         if (tzOffset < 0) {
             buf.append(NumberUtilities.toString(tzOffset, 10, 5));
         } else {
             buf.append("+");
             buf.append(NumberUtilities.toString(tzOffset, 10, 4));
         }
 
         return buf.toString();
     }
 
     /**
      * This method iterates through the raw lines that make up
      * standard E-Mail headers, and parses them out into hash
      * table entries that can be queried by other methods.
      * All header keys are stored in lower-case to ensure that
      * processing methods are case-insensitive.
      *
      * @param rawLines The raw header lines
      * @return Hash table providing the headers as key-value pairs
      */
     public static Hashtable parseMailHeaders(String[] rawLines) {
         String line = "";
         Hashtable table = new Hashtable();
 
         for (int i = 0; i < rawLines.length; i++) {
             if (rawLines[i].startsWith(" ") || rawLines[i].startsWith("\t")) {
                 line = line + strCRLF + rawLines[i];
             } else {
                 if (line.length() != 0) {
                     if(!insertHeaderLine(table, line)) {
                         break;
                     }
                 }
 
                 line = rawLines[i];
             }
 
             // Special case for last item
             if ((i == (rawLines.length - 1)) && (line.length() != 0)) {
                 insertHeaderLine(table, line);
             }
         }
 
         return table;
     }
 
     private static boolean insertHeaderLine(Hashtable table, String line) {
         int p = line.indexOf(':');
         int q = line.indexOf(' ');
 
         if (p != -1) {
             // Key followed by a colon
             if(q != -1) {
                 // Completely normal case
                 table.put((line.substring(0, p)).toLowerCase(),
                         line.substring(q + 1));
             }
             else if(p + 1 < line.length()){
                 // Missing space
                 table.put((line.substring(0, p)).toLowerCase(),
                         line.substring(p + 1));
             }
             else {
                 // Missing value
                 table.put((line.substring(0, p)).toLowerCase(), "");
             }
             return true;
         }
         else {
             // Line with no colon, have to assume it is the body start
             return false;
         }
     }
     
     /**
      * Scans the provided string for blocks of text encoded according
      * to RFC2047, decodes them accordingly, and returns a new Unicode
      * string usable in the rest of the application.  If the charset for
      * a block of text is not supported, then that block is not included
      * in the result.
      *
      * @param text The text to scan.
      * @return Processed unicode string
      */
     public static String parseEncodedHeader(String text) {
         return parseEncodedHeader(text, true);
     }
     
     /**
      * Scans the provided string for blocks of text encoded according
      * to RFC2047, decodes them accordingly, and returns a new Unicode
      * string usable in the rest of the application.  If the charset for
      * a block of text is not supported, then that block is not included
      * in the result.
      *
      * @param text The text to scan.
      * @param reencode true, if the text should be reencoded if it contains
      *     characters outside of the US-ASCII 7bit range
      * @return Processed unicode string
      */
     public static String parseEncodedHeader(String text, boolean reencode) {
         // Quick check for null input
         if (text == null) {
             return null;
         }
 
         if(reencode) {
             text = reencodeStringIfNecessary(text);
         }
 
         int size = text.length();
 
         // Shortcut to avoid processing strings too short
         // to contain encoded sections.  ("=?X?B??=")
         if (size < 8) {
             return text;
         }
         
         Vector tokenizedHeader = tokenizeEncodedHeader(text);
         
         StringBuffer buf = new StringBuffer();
         
         size = tokenizedHeader.size();
         for(int i=0; i<size; i+=2) {
             Boolean isEncoded = (Boolean)tokenizedHeader.elementAt(i);
             String element = (String)tokenizedHeader.elementAt(i + 1);
             
             if(isEncoded == Boolean.TRUE) {
                 buf.append(parseEncodedWord(element));
             }
             else if(notWhitespace(element)) {
                 buf.append(element);
             }
         }
 
         return buf.toString();
     }
 
     private static boolean notWhitespace(String text) {
         int size = text.length();
         for(int i=0; i<size; i++) {
             char ch = text.charAt(i);
             if(ch != ' ' && ch != '\t') {
                 return true;
             }
         }
         return false;
     }
 
     private static String reencodeStringIfNecessary(String text) {
         // Check for any characters outside of US-ASCII,
         // and if any are found, recreate the string using
         // UTF-8 encoding.
         byte[] textBytes = text.getBytes();
 
         for (int i = 0; i < textBytes.length; i++) {
             if (textBytes[i] < 0) {
                 try {
                     text = new String(textBytes, ENCODING_UTF8);
                 } catch (UnsupportedEncodingException e) {
                 }
 
                 break;
             }
         }
         return text;
     }
     
     private static Vector tokenizeEncodedHeader(String text) {
         Vector result = new Vector();
         StringBuffer buf = new StringBuffer();
         int size = text.length();
         int index = 0;
 
         boolean unfolded;
         while (index < size) {
             unfolded = false;
             if((index + 2 < size)
                     && (text.charAt(index) == '\r')
                     && (text.charAt(index + 1) == '\n')
                     && (text.charAt(index + 2) == ' ' || text.charAt(index + 2) == '\t')) {
                 index += 3;
                 unfolded = true;
                 if(index == size) { break; }
             }
             
             int p = checkForEncodedWord(text, index, size);
             
             if(p != -1) {
                 // Encoded word found
                 
                 if(buf.length() > 0) {
                     result.addElement(Boolean.FALSE);
                     result.addElement(buf.toString());
                     buf.setLength(0);
                 }
                 
                 result.addElement(Boolean.TRUE);
                 result.addElement(text.substring(index, p + 1));
                 
                 index = p + 1;
             }
             else {
                 // Plain text found
                 if(unfolded) { buf.append(' '); }
                 char ch = text.charAt(index);
                 if(ch == '\t') {
                     buf.append(' ');
                 }
                 else {
                     buf.append(ch);
                 }
                 index++;
             }
         }
 
         // Append plain text at the end, if applicable
         if(buf.length() > 0) {
             result.addElement(Boolean.FALSE);
             result.addElement(buf.toString());
             buf.setLength(0);
         }
         
         return result;
     }
 
     private static char[] RFC2047_ESPECIALS = {
         '(', ')', '<', '>', '@', ',', ';', ':', '"',
         '/', '[', ']', '?', '.', '='};
     
     private static int checkForEncodedWord(String text, int fromIndex, int size) {
         if ((size - fromIndex) < 8) { return -1; }
         
 //        encoded-word = "=?" charset "?" encoding "?" encoded-text "?="
 //        charset = token
 //        encoding = token
 //        token = 1*<Any CHAR except SPACE, CTLs, and especials>
 //        CTL         =  <any ASCII control           ; (  0.- 31.)
 //        especials = "(" / ")" / "<" / ">" / "@" / "," / ";" / ":" / "
 //                    <"> / "/" / "[" / "]" / "?" / "." / "="
 //        encoded-text = 1*<Any printable ASCII character other than "?" or SPACE>
         
         if (text.charAt(fromIndex) == '='
             && text.charAt(fromIndex + 1) == '?') {
             int index = fromIndex + 2;
             
             // Accept: charset (token)
             index = acceptEncodedWordToken(text, index, size);
             if(index == -1) { return -1; }
             
             // Accept: encoding (token)
             index = acceptEncodedWordToken(text, index, size);
             if(index == -1) { return -1; }
 
             // Accept: encoded-text
             // Special case to allow spaces if the text is encoded as
             // quoted-printable, so malformed headers are handled correctly.
             boolean allowSpaces =
                 (Character.toUpperCase(text.charAt(index - 2)) == 'Q');
             index = acceptEncodedWordEncodedText(text, index, size, allowSpaces);
             if(index == -1) { return -1; }
             
             char ch = text.charAt(index);
             if(ch == '=') {
                 return index;
             }
             else {
                 return -1;
             }
         }
         else {
             return -1;
         }
     }
 
     private static int acceptEncodedWordToken(String text, int index, int size) {
         while(index < size) {
             char ch = text.charAt(index);
             if(ch != ' ' && ch > 31
                     && Arrays.getIndex(RFC2047_ESPECIALS, ch) == -1) {
                 index++;
             }
             else if(ch == '?') {
                 index++;
                 break;
             }
             else {
                 return -1;
             }
         }
         if(index == size) {
             return -1;
         }
         else {
             return index;
         }
     }
     
     public static int acceptEncodedWordEncodedText(String text, int index, int size, boolean allowSpaces) {
         while(index < size) {
             char ch = text.charAt(index);
             if(ch > 31 && ch != '?' && (allowSpaces || ch != ' ')) {
                 index++;
             }
             else if(ch == '?') {
                 index++;
                 break;
             }
             else {
                 return -1;
             }
         }
         if(index == size) {
             return -1;
         }
         else {
             return index;
         }
     }
     
     /**
      * Parses an encoded word, per RFC2047.
      * Assumes that the input has already been separated out from the
      * source, and is of valid structure.  If the input is invalid
      * or cannot be parsed, an empty string is returned.
      *
      * @param text Text to decode
      * @return Decoded text
      */
     private static String parseEncodedWord(String text) {
         // Parse according to the format:
         //   encoded-word = "=?" charset "?" encoding "?" encoded-text "?="
         String[] sections = parseTokenString(text, "?");
 
         // Ensure that there are 3 sections
         if (sections.length != 5) {
             return "";
         }
 
         String charset = sections[1].toUpperCase();
         String encoding = sections[2].toUpperCase();
         String encodedText = sections[3];
 
         if (encoding.length() != 1) {
             return "";
         }
 
         String result;
 
         if (encoding.charAt(0) == 'Q') {
             // Quoted-Printable
             result = decodeQuotedPrintableHeader(encodedText.getBytes(), charset);
         } else if (encoding.charAt(0) == 'B') {
             // Base64
             try {
                 byte[] decodedData = Base64InputStream.decode(encodedText);
                 try {
                     result = StringFactory.create(decodedData, charset);
                 } catch (UnsupportedEncodingException e) {
                     result = new String(decodedData);
                 }
             } catch (IOException e) {
                 result = "";
             }
         } else {
             result = "";
         }
 
         return result;
     }
     
     /**
      * Checks the provided string for its optimal encoding, and returns a
      * mail header compatible string according to RFC2047.
      * 
      * @param key The header key (i.e. "Subject:").
      * @param text The text to process.
      * @return Encoded header string.
      * @throws UnsupportedEncodingException 
      */
     public static String createEncodedHeader(String key, String text) {
         StringBuffer buf = new StringBuffer();
         buf.append(key);
         buf.append(' ');
         
         appendEncodedHeaderSegment(buf, key.length() + 1, text);
         
         return buf.toString();
     }
     
     public static String createEncodedHeader(int indent, String text) {
         StringBuffer buf = new StringBuffer();
         
         appendEncodedHeaderSegment(buf, indent, text);
         
         return buf.toString();
     }
     
     private static int appendEncodedHeaderSegment(StringBuffer buf, int indent, String text) {
         // If the text is empty, return immediately
         if(text.length() == 0) { return indent; }
         
         // Get the optimal encoding type, and set some variables
         int encodingType = StringParser.getOptimalEncoding(text);
         String charset;
         char encoding;
         switch(encodingType) {
         case ENCODING_QUOTED_PRINTABLE:
             charset = "iso-8859-1";
             encoding = 'q';
             break;
         case ENCODING_BASE64:
             charset = "utf-8";
             encoding = 'b';
             break;
         case ENCODING_7BIT:
         default:
             // Append and return if no encoding is required
             buf.append(text);
             return indent + text.length();
         }
 
         int lastLineLen = indent;
         int prefixLen = 5 + charset.length();
 
         // If the minimum length of an encoded string is greater than the
         // maximum line length, insert a line break first
         if((lastLineLen + prefixLen + 3) > MAX_LINE_LEN) {
             buf.append("\r\n ");
             lastLineLen = 1;
         }
         
         // Get the byte array representing the text
         byte[] textBytes;
         try {
             textBytes = text.getBytes(charset);
         } catch (UnsupportedEncodingException e) {
             // This should never happen
             return indent;
         }
         
         int index = 0;
         int maxEncodedLen = MAX_LINE_LEN - lastLineLen - prefixLen - 3;
         
         while(index < textBytes.length) {
             if(index > 0) {
                 buf.append("?=\r\n ");
                 lastLineLen = 1;
                 maxEncodedLen = MAX_LINE_LEN - prefixLen - 3;
             }
             
             buf.append("=?");
             buf.append(charset);
             buf.append('?');
             buf.append(encoding);
             buf.append('?');
             
             int beforeLen = buf.length();
             if(encodingType == ENCODING_QUOTED_PRINTABLE) {
                 index += encodeHeaderWordQP(textBytes, index, buf, maxEncodedLen);
             }
             else {
                 index += encodeHeaderWordB64(textBytes, index, buf, maxEncodedLen);
             }
             int afterLen = buf.length();
             lastLineLen += (afterLen - beforeLen);
         }
         
         buf.append("?=");
         lastLineLen += 2;
         return lastLineLen;
     }
     
     public static String createEncodedRecipientHeader(String key, String[] recipients) {
         StringBuffer buf = new StringBuffer();
         buf.append(key);
         buf.append(' ');
 
         int lastLineLen = key.length() + 1;
         
         if ((recipients == null) || (recipients.length == 0)) {
             return buf.toString();
         }
         else if (recipients.length == 1) {
             appendEncodedRecipient(buf, lastLineLen, recipients[0]);
         }
         else {
             for (int i = 0; i < (recipients.length - 1); i++) {
                 lastLineLen = appendEncodedRecipient(buf, lastLineLen, recipients[i]);
                 buf.append(", ");
             }
 
             appendEncodedRecipient(buf, lastLineLen, recipients[recipients.length - 1]);
         }
         
         return buf.toString();
     }
     
     private static int appendEncodedRecipient(StringBuffer buf, int indent, String recipient) {
         int p = recipient.indexOf("\" <");
         int q = recipient.indexOf('>');
         if(p != -1 && q != -1 && p > 0 && p < q) {
             // Recipient is: "John Doe" <doej@generic.org>
             int lastLineLen =
                 appendEncodedHeaderSegment(buf, indent, recipient.substring(0, p + 1));
             String address = recipient.substring(p + 1);
             int addressLen = address.length();
             if(lastLineLen + addressLen > MAX_LINE_LEN) {
                  buf.append(" \r\n");
                  lastLineLen = 0;
             }
             buf.append(address);
             lastLineLen += addressLen;
             return lastLineLen;
         }
         
         p = recipient.indexOf(" <");
         if(p != -1 && q != -1 && p > 0 && p < q) {
             // Recipient is: John <doej@generic.org>
             int lastLineLen =
                 appendEncodedHeaderSegment(buf, indent, recipient.substring(0, p));
             String address = recipient.substring(p);
             int addressLen = address.length();
             if(lastLineLen + addressLen > MAX_LINE_LEN) {
                 buf.append(" \r\n");
                 lastLineLen = 0;
             }
             buf.append(address);
             lastLineLen += addressLen;
             return lastLineLen;
         }
         
         // Assume it is just a usable E-Mail address without any
         // special encoding needs
         buf.append(recipient);
         return indent + recipient.length();
     }
     
     /**
      * Generate the quoted-printable portion of an encoded word, given
      * specified length limitations
      *
      * @param input the character data to encode
      * @param offset the offset from which to start
      * @param buffer the buffer to write encoded output onto
      * @param maxOutputLen the maximum allowable length of the output
      * @return the number of bytes of input actually encoded
      */
     private static int encodeHeaderWordQP(byte[] input, int offset, StringBuffer buffer, int maxOutputLen) {
         int count = 0;
         int curOutputLen = 0;
         
         for (int i = offset; i < input.length; i++) {
             int ch = ((int)input[i]) & 0xFF;
             
             if(ch < 0x20 || ch >= 0x7F || WORD_SPECIALS.indexOf(ch) != -1) {
                 if(curOutputLen + 3 > maxOutputLen) {
                     break;
                 }
                 else {
                     // Encode this character
                     String charStr = Integer.toHexString((int) ch).toUpperCase();
                     buffer.append('=');
                     if (charStr.length() == 1) {
                         buffer.append('0');
                     }
                     buffer.append(charStr);
                     curOutputLen += 3;
                 }
             }
             else {
                 if(curOutputLen + 1 > maxOutputLen) {
                     break;
                 }
                 else if(ch == ' ') {
                     // Encode this space as an underscore
                     buffer.append('_');
                     curOutputLen++;
                 }
                 else {
                     // Leave this character alone
                     buffer.append((char)ch);
                     curOutputLen++;
                 }
             }
             count++;
         }
         
         return count;
     }
     
     /**
      * Generate the Base64 portion of an encoded word, given
      * specified length limitations
      *
      * @param input the character data to encode, assuming UTF-8
      * @param offset the offset from which to start
      * @param buffer the buffer to write encoded output onto
      * @param maxOutputLen the maximum allowable length of the output
      * @return the number of bytes of input actually encoded
      */
     private static int encodeHeaderWordB64(byte[] input, int offset, StringBuffer buffer, int maxOutputLen) {
         // Make the maximum output length a multiple of 4 bytes
         maxOutputLen = (maxOutputLen / 4) * 4;
         if(maxOutputLen < 4) { maxOutputLen = 4; }
         
         DataBuffer dataBuffer = new DataBuffer();
         
         // Assume base64 output is always in multiples of 3 input bytes,
         // and thus 4 output bytes.  Also, characters are variable length.
         int i = offset;
         int count = 0;
         while (i < input.length) {
             // Look at the first byte of the next character, and determine
             // how many bytes that character will consist of
             int ch = ((int)input[i]) & 0xFF;
             int nextCharLen = -1;
             if((ch & 0x80) == 0x00 ) {
                 nextCharLen = 1;
             }
             else if((ch & 0xE0) == 0xC0) {
                 nextCharLen = 2;
             }
             else if((ch & 0xF0) == 0xE0) {
                 nextCharLen = 3;
             }
             else if((ch & 0xF8) == 0xF0) {
                 nextCharLen = 4;
             }
             
             if(nextCharLen == -1 || (i + nextCharLen) > input.length) {
                 // This should only happen on malformed input
                 break;
             }
 
             // Round up to the next multiple of 3
             int nextOutputLen = ((count + nextCharLen) / 3) * 3;
             // Divide and multiply to reflect 3 input bytes becoming 4 output bytes
             nextOutputLen = (nextOutputLen / 3) * 4;
             
             if(nextOutputLen > maxOutputLen) {
                 break;
             }
             else {
                 dataBuffer.write(input, i, nextCharLen);
                 count += nextCharLen;
                 i += nextCharLen;
             }
         }
 
         byte[] data = dataBuffer.toArray();
         try {
             String encoded = Base64OutputStream.encodeAsString(data, 0, data.length, false, false);
             buffer.append(encoded);
         } catch (IOException e) {
             EventLogger.logEvent(AppInfo.GUID, ("Unable to encode header: " + e.getMessage()).getBytes(), EventLogger.ERROR);
         }
         
         return count;
     }
     
     public static String[] parseTokenString(String text, String token) {
         String[] tok = new String[0];
 
         if (text == null) {
             return tok;
         }
 
         int p = 0;
         int q = 0;
 
         while (q < text.length()) {
             q = text.indexOf(token, p + 1);
 
             if (q == -1) {
                 Arrays.add(tok, text.substring(p));
 
                 break;
             }
 
             Arrays.add(tok, text.substring(p, q));
             p = q + token.length();
         }
 
         return tok;
     }
 
     /**
      * Parse a CSV string, where commas inside quotes or
      * escaped with a backslash aren't considered separators,
      * and whitespace is trimmed.
      */
     public static String[] parseCsvString(String text) {
         String[] tok = new String[0];
 
         if (text == null) {
             return tok;
         }
 
         int p = 0;
         int q = 0;
         boolean inQuote = false;
 
         while (q < text.length()) {
             if ((text.charAt(q) == '"') &&
                     ((q == 0) || ((q > 0) && (text.charAt(q - 1) != '\\')))) {
                 inQuote = !inQuote;
             }
 
             if ((text.charAt(q) == ',') && !inQuote) {
                 Arrays.add(tok, text.substring(p, q).trim());
                 p = q + 1;
                 q++;
             }
 
             q++;
         }
 
         if ((p + 1) < q) {
             Arrays.add(tok, text.substring(p, q).trim());
         }
 
         return tok;
     }
 
     /**
      * Create a simple CSV string from the provided input strings.
      *
      * @param input Array of strings.
      * @return Comma-separated value string.
      */
     public static String makeCsvString(String[] input) {
         if ((input == null) || (input.length == 0)) {
             return "";
         } else if (input.length == 1) {
             return input[0];
         } else {
             StringBuffer buffer = new StringBuffer();
 
             for (int i = 0; i < (input.length - 1); i++) {
                 buffer.append(input[i]);
                 buffer.append(", ");
             }
 
             buffer.append(input[input.length - 1]);
 
             return buffer.toString();
         }
     }
 
     /**
      * Create a simple CSV string from the provided input strings.
      *
      * @param input Array of objects with usable <tt>toString()</tt> methods.
      * @return Comma-separated value string.
      */
     public static String makeCsvString(Object[] input) {
         if ((input == null) || (input.length == 0)) {
             return "";
         } else if (input.length == 1) {
             return input[0].toString();
         } else {
             String[] stringArray = new String[input.length];
 
             for (int i = 0; i < input.length; i++) {
                 stringArray[i] = input[i].toString();
             }
 
             return makeCsvString(stringArray);
         }
     }
 
     /**
      * Parse an input string that contains an encoding type,
      * and return a valid encoding type supported by RIM.
      */
     public static String parseValidCharsetString(String encoding) {
         if (encoding == null) {
             return "ISO-8859-1";
         } else if (encoding.toUpperCase().indexOf("ISO-8859-1") != -1) {
             return "ISO-8859-1";
         } else if (encoding.toUpperCase().indexOf("UTF-8") != -1) {
             return "UTF-8";
         } else if (encoding.toUpperCase().indexOf("UTF-16BE") != -1) {
             return "UTF-16BE";
         } else if (encoding.toUpperCase().indexOf("US-ASCII") != -1) {
             return "US-ASCII";
         } else {
             return "ISO-8859-1";
         }
     }
 
     /**
      * Decode a quoted-printable string.
      * 
      * @param text The raw input text (as a byte array) to decode
      * @param charset The character set to use for the decoded String
      * @return String created with the specified character set, falling back
      *         to the platform default character set if it is invalid.
      */
     public static String decodeQuotedPrintable(byte[] text, String charset) {
         return decodeQuotedPrintableImpl(text, charset, false);
     }
     
     /**
      * Decode a quoted-printable header string.
      * This implementation uses a slight modification to the encoding, which
      * assumes that underscores represent spaces.
      * 
      * @param text The raw input text (as a byte array) to decode
      * @param charset The character set to use for the decoded String
      * @return String created with the specified character set, falling back
      *         to the platform default character set if it is invalid.
      */
     public static String decodeQuotedPrintableHeader(byte[] text, String charset) {
         return decodeQuotedPrintableImpl(text, charset, true);
     }
     
     /**
      * Decode a quoted-printable string as a raw array of bytes.
      * 
      * @param text The raw input text (as a byte array) to decode
      * @return Raw array of decoded data
      */
     public static byte[] decodeQuotedPrintableBytes(byte[] text) {
         DataBuffer buf = new DataBuffer();
         decodeQuotedPrintableDataImpl(buf, text, false);
         return buf.toArray();
     }
     
     private static String decodeQuotedPrintableImpl(byte[] text, String charset, boolean header) {
         DataBuffer buf = new DataBuffer();
         decodeQuotedPrintableDataImpl(buf, text, header);
         String result;
         try {
            result = StringFactory.create(buf.getArray(), buf.getArrayStart(), buf.getLength(), charset);
         } catch (UnsupportedEncodingException e) {
            result = new String(buf.getArray(), buf.getArrayStart(), buf.getLength());
         }
         return result;
     }
     
     private static void decodeQuotedPrintableDataImpl(DataBuffer buf, byte[] text, boolean header) {
         int index = 0;
         int length = text.length;
 
         while (index < length) {
             if (text[index] == (byte)'=') {
                 if ((index + 2) >= length) {
                     break;
                 }
                 else {
                     byte ch1 = text[index + 1];
                     byte ch2 = text[index + 2];
 
                     if ((ch1 == (byte)'\r') && (ch2 == (byte)'\n')) {
                         index += 3;
                     }
                     else if (ch1 == (byte)'\n') {
                         index += 2;
                     }
                     else {
                         try {
                             int charVal = StringArrays.parseHexInt(text, index + 1, 2);
                             buf.write((byte)charVal);
                         } catch (NumberFormatException exp) { }
 
                         index += 3;
                     }
                 }
             }
             else if (header && text[index] == (byte)'_') {
                 buf.write((byte)' ');
                 index++;
             }
             else {
                 buf.write((byte)text[index]);
                 index++;
             }
         }
     }
 
     /**
      * Encode a quoted-printable string
      */
     public static String encodeQuotedPrintable(String text) {
         StringBuffer buffer = new StringBuffer();
         char ch;
         String charStr;
         int length = text.length();
 
         for (int i = 0; i < length; i++) {
             ch = text.charAt(i);
 
             if ((ch != '\t') && (ch != ' ') && (ch < 128)) {
                 if ((buffer.length() == 75) && (i < (length - 1))) {
                     buffer.append("=\r\n");
                 }
 
                 buffer.append(ch);
             } else {
                 if ((buffer.length() == 73) && (i < (length - 3))) {
                     buffer.append("=\r\n");
                 }
 
                 charStr = Integer.toHexString((int) ch).toUpperCase();
                 buffer.append('=');
 
                 if (charStr.length() == 1) {
                     buffer.append('0');
                 }
 
                 buffer.append(charStr);
             }
         }
 
         return buffer.toString();
     }
 
     /**
      * Removes escaped characters from a string.
      * Currently only implemented for double-quotes and backslashes.
      */
     public static String removeEscapedChars(String text) {
         int size = text.length();
 
         // Shortcut out if this method cannot do anything.
         if (size < 2) {
             return text;
         }
 
         StringBuffer buf = new StringBuffer();
         int i = 0;
 
         while (i < size) {
             char ch = text.charAt(i);
 
             if ((i < (size - 1)) && (ch == '\\')) {
                 char nextCh = text.charAt(i + 1);
 
                 if ((nextCh == '\\') || (nextCh == '\"')) {
                     buf.append(nextCh);
                     i += 2;
                 } else {
                     buf.append(ch);
                     i++;
                 }
             } else {
                 buf.append(ch);
                 i++;
             }
         }
 
         return buf.toString();
     }
 
     /**
      * Adds escaped characters to a string.
      * Currently only implemented for double-quotes and backslashes.
      */
     public static String addEscapedChars(String text) {
         StringBuffer buf = new StringBuffer();
 
         int size = text.length();
 
         for (int i = 0; i < size; i++) {
             char ch = text.charAt(i);
 
             if ((ch == '\\') || (ch == '\"')) {
                 buf.append('\\');
             }
 
             buf.append(ch);
         }
 
         return buf.toString();
     }
 
     /**
      * Parses a standard-format E-Mail message recipient into the
      * full name and address portions.  Input is expected to be
      * in the following format:
      * "John Doe <jdoe@generic.org>"
      *
      * @param recipient Message recipient string
      * @return String array with two elements, the first being the
      * full name, and the second being the address.  If no full name
      * is present, then that entry will be null.  If no address is
      * present, then that entry will be an empty string.
      */
     public static String[] parseRecipient(String recipient) {
         String[] result = new String[2];
 
         String text = recipient.trim();
 
         int p = text.indexOf('<');
         int q = text.indexOf('>');
 
         // Attempt to set the address from the parameter
         if ((p == -1) && (q == -1)) {
             result[1] = text;
         } else if ((p != -1) && (q != -1) && (p < q) && (text.length() > 2)) {
             result[1] = text.substring(p + 1, q);
         } else if ((p != -1) && (q == -1) && (text.length() > 1)) {
             result[1] = text.substring(p + 1);
         } else {
             result[1] = "";
         }
 
         // Sanity check for empty addresses
         if (result[1].length() == 0) {
             return result;
         }
 
         // Attempt to set the full name from the parameter
         if ((p != -1) && (p > 0)) {
             result[0] = text.substring(0, p).trim();
 
             // Check for quotes, and trim if necessary
             int length = result[0].length();
 
             if ((length >= 2) && (result[0].charAt(0) == '\"') &&
                     (result[0].charAt(length - 1) == '\"')) {
                 result[0] = result[0].substring(1, length - 1);
             }
         } else {
             result[0] = null;
         }
 
         return result;
     }
 
     /**
      * Merges a name and address into a standard-format E-Mail message
      * recipient.  The output is in the following format:
      * "John Doe" <jdoe@generic.org>
      *
      * @param name Full name
      * @param address E-Mail address
      * @return Full recipient formatted string
      */
     public static String mergeRecipient(String name, String address) {
         String result;
 
         if ((address == null) || (address.length() == 0)) {
             result = "";
         } else if ((name != null) && (name.length() > 0)) {
             StringBuffer buf = new StringBuffer();
             buf.append('\"');
             buf.append(name);
             buf.append("\" <");
             buf.append(address);
             buf.append('>');
             result = buf.toString();
         } else {
             result = address;
         }
 
         return result;
     }
 
     /**
      * Converts an array of objects to an array of Strings returned
      * by calling their {@link Object#toString()} methods.
      *
      * @param input Source array
      * @return String array
      */
     public static String[] toStringArray(Object[] input) {
         String[] result;
 
         if (input == null) {
             result = null;
         } else if (input.length == 0) {
             result = new String[0];
         } else {
             result = new String[input.length];
 
             for (int i = 0; i < input.length; i++) {
                 if (input[i] != null) {
                     result[i] = input[i].toString();
                 } else {
                     result[i] = null;
                 }
             }
         }
 
         return result;
     }
 
     /**
      * Converts a numerical byte quantity into a nicer human-readable
      * unit representation.
      *
      * @param dataSize Size in bytes
      * @return Printable string
      */
     public static String toDataSizeString(int dataSize) {
         StringBuffer buf = new StringBuffer();
 
         if (dataSize < 1024) {
             buf.append(dataSize);
             buf.append('B');
         } else {
             buf.append((int) (dataSize / 1024));
             buf.append('k');
         }
 
         return buf.toString();
     }
     
     /**
      * Creates a string representation of the long argument as an unsigned integer in base 16. 
      * 
      * @param number a long number
      * @return the string representation of the unsigned integer value represented by the argument in hexadecimal (base 16).
      */
     public static String toHexString(long number) {
         long v = number & 0xFFFFFFFFFFFFFFFFL;
 
         byte[] result = new byte[16];
         Arrays.fill(result, (byte)0);
 
         for (int i = 0; i < result.length; i += 2) {
             byte b = (byte) ((v & 0xFF00000000000000L) >> 56);
 
             byte b2 = (byte) (b & 0x0F);
             byte b1 = (byte) ((b >> 4) & 0x0F);
 
             if (b1 > 9) b1 += 39;
             b1 += 48;
 
             if (b2 > 9) b2 += 39;
             b2 += 48;
 
             result[i] = (byte) (b1 & 0xFF);
             result[i + 1] = (byte) (b2 & 0xFF);
 
             v <<= 8;
         }
         return new String(result);
     }
     
     /**
      * Gets the optimal encoding for a text string.
      * <p>
      * This method scans the string for the maximum character value, and
      * determines the optimal encoding based on it.  The result will be
      * one of the <tt>ENCODING_XXXX</tt> constants.
      * </p>
      *
      * @param text the text to scan
      * @return the optimal encoding
      */
     public static int getOptimalEncoding(String text) {
         int encoding;
         
         // Find the maximum character value in the text
         char maxChar = 0;
 
         for (int i = text.length() - 1; i >= 0; --i) {
             char ch = text.charAt(i);
 
             if (ch > maxChar) {
                 maxChar = ch;
             }
         }
 
         if (maxChar > 255) {
             encoding = ENCODING_BASE64;
         } else if (maxChar > 127) {
             encoding = ENCODING_QUOTED_PRINTABLE;
         } else {
             encoding = ENCODING_7BIT;
         }
         
         return encoding;
     }
     
     /**
      * Gets the optimal encoding for a text string stored in a byte array.
      * <p>
      * This method uses a very simple and conservative approach, where it will
      * return either <tt>ENCODING_7BIT</tt> or <tt>ENCODING_BASE64</tt>
      * depending on the values in the array.
      * </p>
      *
      * @param text the text to scan
      * @return the optimal encoding
      */
     public static int getOptimalEncoding(final byte[] text) {
         for(int i=text.length - 1; i >= 0; --i) {
             if((text[i] & HIGH_BIT) != 0) {
                 return ENCODING_BASE64;
             }
         }
         
         return ENCODING_7BIT;
     }
     
     /**
      * Merge two paths, avoiding any duplicate path separators.
      * This method assumes that both arguments already contain valid
      * path fragments, and thus it does not do any null or length
      * checking.
      *
      * @param path1 the first path fragment
      * @param path2 the second path fragment
      * @return the combined path string
      */
     public static String mergePaths(String path1, String path2) {
         String result;
         if(path1.lastIndexOf('/') == path1.length() - 1) {
             if(path2.charAt(0) == '/') {
                 result = path1 + path2.substring(1);
             }
             else {
                 result = path1 + path2;
             }
         }
         else {
             if(path2.charAt(0) == '/') {
                 result = path1 + path2;
             }
             else {
                 result = path1 + '/' + path2;
             }
         }
         
         return result;
     }
 }

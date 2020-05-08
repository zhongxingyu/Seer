 // Copyright (C) 2005, 2006 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.plugin.http.tcpproxyfilter;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import HTTPClient.Codecs;
 
 
 /**
  * Helper functions for style sheets.
  *
  * <p>
  * When calling methods that don't have parameters from a style sheet, don't
  * forget the call braces or you'll end up with a no-op.
  * </p>
  *
  * <p>
  * This class has static methods for consistent behaviour between JDK versions.
  * With instance methods the XSLTC implementation in Java 5.0 needs the instance
  * to be passed as the first argument, whereas the Xalan implementation in Java
  * 1.5 does not.
  * </p>
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public final class XSLTHelper {
   private static DateFormat s_iso8601DateFormat =
     new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
 
   private static int s_indentLevel;
 
   private XSLTHelper() {
   }
 
   /**
    * Convert an ISO 8601 date/time string to a more friendly, locale specific
    * string.
    *
    *
    * @param iso8601
    *          An extended format ISO 8601 date/time string
    * @return The formated date/time.
    * @throws ParseException
    *           If the date could not be parsed.
    */
   public static String formatTime(String iso8601) throws ParseException {
     final Date date = s_iso8601DateFormat.parse(iso8601);
     return DateFormat.getDateTimeInstance().format(date);
   }
 
   /**
    * Wrap string in appropriate quotes for Python.
    *
    * @param value The string.
    * @return The quoted string.
    */
   public static String quoteForPython(String value) {
     if (value == null) {
       return "None";
     }
 
     final StringBuffer result = new StringBuffer();
 
     final String quotes = value.indexOf("\n") > -1 ? "'''" : "'";
 
     result.append(quotes).append(escape(value)).append(quotes);
 
     return result.toString();
   }
 
   /**
    * Transform new line characters and other control characters to a printable
    * representation. Truncate string if longer than
   * <code>maximumCharacters</code>. If the string is truncated,
    * add ellipses.
    *
    * @param value
    *          The input string.
    * @param maximumCharacters
    *          Truncate at this number of characters if result would otherwise be
    *          longer.
    * @return The result.
    */
   public static String summariseAsLine(String value, int maximumCharacters) {
 
     final StringBuffer result = new StringBuffer(value.length());
 
     if (value.length() > maximumCharacters) {
       result.append(value.substring(0, maximumCharacters));
       result.append("...");
     }
     else {
       result.append(value);
     }
 
     for (int i = 0; i < result.length(); ++i) {
       final char c = result.charAt(i);
 
       if (c == '\t') {
         result.replace(i, i + 1, "\\t");
       }
       else if (c == '\r') {
         result.replace(i, i + 1, "\\r");
       }
       else if (c == '\n') {
         result.replace(i, i + 1, "\\n");
       }
       else if (Character.isISOControl(c)) {
         result.setCharAt(i, '.');
       }
     }
 
     return result.toString();
   }
 
   /**
    * Escape quotes and back slashes for Python. One day, this might escape
    * white space and non-printable characters too.
    *
    * @param value The string.
    * @return The quoted string.
    */
   public static String escape(String value) {
     final StringBuffer result = new StringBuffer(value.length());
 
     for (int i = 0; i < value.length(); ++i) {
       final char c = value.charAt(i);
 
       switch (c) {
       case '\'':
       case '"':
       case '\\':
         result.append('\\');
         // Fall through.
       default:
         result.append(c);
       }
     }
 
     return result.toString();
   }
 
   /**
    * Return an appropriately indent string.
    *
    * @return The string.
    * @see #incrementIndent()
    * @see #decrementIndent()
    */
   public static String indent() {
     return "                ".substring(0, s_indentLevel * 2);
   }
 
   /**
    * Return a new line string.
    *
    * @return The string.
    */
   public static String newLine() {
     return "\n";
   }
 
   /**
    * Equivalent to {@link #newLine()} followed by {@link #indent()).
    *
    * @return The string.
    */
   public static String newLineAndIndent() {
     return newLine() + indent();
   }
 
   /**
    * Change the indent level.
    *
    * @param indentChange Offset to indent level, positive or negative.
    * @return An empty string.
    */
   public static String changeIndent(int indentChange) {
     s_indentLevel += indentChange;
     return "";
   }
 
 
   /**
    * Reset the indent level.
    *
    * @return An empty string.
    */
   public static String resetIndent() {
     s_indentLevel = 0;
     return "";
   }
 
   /**
    * Convert a base64 string of binary data to an array of bytes scriptlet.
    *
    *
    * @param base64String The binary data.
    * @return The scriptlet.
    */
   public static String base64ToPython(String base64String) {
 
     final byte[] base64 = base64String.getBytes();
 
     final StringBuffer result = new StringBuffer(base64.length * 2);
 
     result.append("( ");
     changeIndent(1);
 
     if (base64.length > 0) {
       final byte[] bytes = Codecs.base64Decode(base64);
 
       for (int i = 0; i < bytes.length; ++i) {
         if (i > 0 && i % 8 == 0) {
           result.append(newLineAndIndent());
         }
 
         final int b = bytes[i] < 0 ? 0x100 + bytes[i] : bytes[i];
 
         if (b <= 0xF) {
           result.append("0x0");
         }
         else {
           result.append("0x");
         }
 
         result.append(Integer.toHexString(b).toUpperCase());
         result.append(", ");
       }
     }
 
     changeIndent(-1);
     result.append(")");
 
     return result.toString();
   }
 }

 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared;
 
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxConversionException;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxRuntimeException;
 import com.flexive.shared.structure.FxSelectListItem;
 import com.flexive.shared.value.*;
 import com.flexive.shared.value.renderer.FxValueRendererFactory;
 import org.apache.commons.lang.StringUtils;
 
 import javax.faces.convert.ConverterException;
 import java.lang.reflect.Array;
 import java.text.ParseException;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Miscellaneous formatting utility functions.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public final class FxFormatUtils {
     public final static String DEFAULT_COLOR = "#000000";
     public final static String UNIVERSAL_TIMEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
     public static final String CONTRAST_BACKGROUND_COLOR = "#6D6D6D";
     //date/time standards according to ISO 8601
     public static final String DATE_STD = "yyyy-MM-dd";
     public static final String DATETIME_STD = "yyyy-MM-dd HH:mm:ss";
     public static final String TIME_STD = "HH:mm:ss";
     public static final char DECIMAL_SEP_STD = '.';
     public static final char GROUPING_SEP_STD = ',';
     public static final boolean USE_GROUPING_STD = false;
 
     /**
      * Private constructor to avoid instantiation
      */
     private FxFormatUtils() {
     }
 
     /**
      * Returns true if the char is a valid RGB value.
      *
      * @param ch the character to check
      * @return true if the char is a valid RGB value
      */
     private static boolean isValidRGBChar(final char ch) {
         return (Character.isDigit(ch) || ch == 'A' || ch == 'B' || ch == 'C' || ch == 'D'
                 || ch == 'E' || ch == 'F');
     }
 
     /**
      * Checks if a value is a RGB color code.
      * <p/>
      * The RGB color code my start with a '#', but is also recognized without it.
      *
      * @param value the string to check
      * @return true if the value is a valid RGB color code
      */
     public static boolean isRGBCode(String value) {
         if (value == null || value.length() < 6) return false;
         String rgbValue = value.trim();
         rgbValue = rgbValue.toUpperCase();
         if (rgbValue.charAt(0) == '#' && rgbValue.length() != 7) return false;
         if (rgbValue.charAt(0) != '#' && rgbValue.length() != 6) return false;
         // Cut away the leading'#'
         if ((rgbValue.charAt(0)) == '#') {
             rgbValue = rgbValue.substring(1);
         }
         // Check all digits
         for (int i = 0; i < rgbValue.length(); i++) {
             if (!isValidRGBChar(rgbValue.charAt(i))) return false;
         }
         // Passed all tests
         return true;
     }
 
     /**
      * Converts "#FF0000" or "FF0000" rgb values to style="color:#FF0000" and all other style values
      * to class="XXX".
      *
      * @param code the color code
      * @return the style or class value
      */
     public static String colorCodeToStyle(String code) {
         if (code == null) return "";
         String colorCode = code.trim();
         if (colorCode != null && colorCode.length() > 0) {
             if (isRGBCode(colorCode)) {
                 if (colorCode.charAt(0) != '#') colorCode = '#' + colorCode;
                 colorCode = "style=\"color:" + colorCode + "\" ";
             } else {
                 colorCode = "class=\"" + colorCode + "\" ";
             }
         }
         return colorCode;
     }
 
     /**
      * Is there a lack of contrast in the given color string?
      *
      * @param color color to check
      * @return contrast lacking compared to white
      */
     public static boolean lackOfContrast(String color) {
         if (StringUtils.isEmpty(color) || color.trim().length() != 7)
             return false;
         String check = color.trim();
         return check.charAt(0) == '#' && check.charAt(1) >= 'C' && check.charAt(3) >= 'C' && check.charAt(5) >= 'C';
     }
 
     /**
      * Checks the email. Also allows local email addresses without a domain (e.g. 'root@localhost').
      *
      * @param email the email to check
      * @return the email without whitespaces
      * @throws com.flexive.shared.exceptions.FxInvalidParameterException
      *          if the email is invalid
      */
     public static String checkEmail(String email) throws FxInvalidParameterException {
         final boolean valid = !StringUtils.isEmpty(email) && email.indexOf('@') > 0
                 && (StringUtils.countMatches(email, "@") == 1)
                 && (email.indexOf('@') < email.length() - 1);
         if (!valid) {
             if (StringUtils.isEmpty(email))
                 throw new FxInvalidParameterException("EMAIL", "ex.account.email.empty");
             throw new FxInvalidParameterException("EMAIL", "ex.account.email.invalid", email);
         }
         return email.trim();
     }
 
 
     /**
      * Checks the password and encodes it.
      *
      * @param accountId the account ID (needed for computing the hash)
      * @param password  unencoded the password
      * @return the encoded password
      * @throws FxInvalidParameterException if the password is invalid (too short, too simple, ..)
      * @deprecated  use {@link #encodePassword(long, java.lang.String, java.lang.String) } to support all available
      * hashing methods
      */
     public static String encodePassword(long accountId, String password) throws FxInvalidParameterException {
         if (password.length() < 6)
             throw new FxInvalidParameterException("PASSWORD", "ex.account.password.tooShort");
         return FxSharedUtils.hashPassword(accountId, password);
     }
 
     /**
      * Checks the password and encodes it.
      *
      * @param accountId the account ID (needed for computing the hash)
      * @param password  unencoded the password
      * @return the encoded password
      * @throws FxInvalidParameterException if the password is invalid (too short, too simple, ..)
      * @since 3.1.6
      */
     public static String encodePassword(long accountId, String username, String password) throws FxInvalidParameterException {
         if (password.length() < 6)
             throw new FxInvalidParameterException("PASSWORD", "ex.account.password.tooShort");
         return FxSharedUtils.hashPassword(accountId, username, password);
     }
 
     /**
      * Check if a color value is valid.
      * <p/>
      * The color may be a RGB value (recognized by a starting '#') or a css class name.<br>
      * The function returns the default color if value is empty.<br>
      * If the value defines an invalid RGB value a FxInvalidParameterException is thrown.<br>
      *
      * @param paramName the name of the parameter that is used when a FxInvalidParameterException is thrown
      * @param value     the color alue
      * @return the (corrected) color
      * @throws FxInvalidParameterException if the color is invalid
      */
     public static String processColorString(String paramName, String value)
             throws FxInvalidParameterException {
         if (value == null || value.length() == 0) {
             return DEFAULT_COLOR;
         }
         if (value.charAt(0) == '#') {
             if (value.length() != 7) {
                 throw new FxInvalidParameterException("Invalid color for property [" + paramName + "]", paramName);
             }
             for (int i = 1; i < 7; i++) {
                 char aChar = value.charAt(i);
                 if (!Character.isDigit(aChar) && (aChar != 'A') && (aChar != 'B') && (aChar != 'C')
                         && (aChar != 'D') && (aChar != 'E') && (aChar != 'F')) {
                     throw new FxInvalidParameterException("Invalid color for property [" + paramName + "]", paramName);
                 }
             }
         } else if (StringUtils.containsOnly(value.toUpperCase(), "0123456789ABCDEF")) {
             // prefix with '#'
             value = "#" + value;
         }
         return value;
     }
 
     /**
      * Escape a path to allow only a-zA-Z0-9/ and replace all other letters with underscores (_)
      *
      * @param path the path to escape
      * @return escaped path
      */
     public static String escapeTreePath(final String path) {
         if (StringUtils.isEmpty(path))
             return "_";
         StringBuilder sb = new StringBuilder(path.length());
         char c;
         boolean inTag = false;
         for (int i = 0; i < path.length(); i++) {
             c = path.charAt(i);
             if (c == '<' && !inTag) {
                 inTag = true;
                 continue;
             }
             if (c == '>' && inTag) {
                 inTag = false;
                 continue;
             }
             if (inTag)
                 continue;
             if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '/' || c == '.' || c == '-')
                 sb.append(c);
             else {
                 if ((sb.length() > 0 && sb.charAt(sb.length() - 1) != '_') || sb.length() == 0) {
                     sb.append('_');
                 }
             }
         }
         if (sb.length() == 0)
             sb.append('_');
         return sb.toString();
     }
 
     public static String toString(Date date) {
         return getDateFormat().format(date);
     }
 
     /**
      * Is the value quoted by ' or " ?
      *
      * @param value value to check
      * @return if quoted by ' or " ?
      */
     public static boolean isQuoted(String value) {
         return !StringUtils.isEmpty(value) && ((value.endsWith("'") && value.startsWith("'")) || (value.endsWith("\"") && value.startsWith("\"")));
     }
 
     /**
      * Unquote a quoted value
      *
      * @param value value to unquote
      * @return unquoted calue
      */
     public static String unquote(String value) {
         if (!isQuoted(value))
             return value;
         return value.substring(1, value.length() - 1);
     }
 
     /**
      * Turns an array of bytes into a String representing each byte as an unsigned
      * hex number.
      *
      * @param bytes an array of bytes to convert to a hex-string
      * @return generated hex string
      */
     public static String encodeHex(byte[] bytes) {
         StringBuilder buf = new StringBuilder(bytes.length * 2);
         int i;
         for (i = 0; i < bytes.length; i++) {
             if (((int) bytes[i] & 0xff) < 0x10) {
                 buf.append("0");
             }
             buf.append(Long.toString((int) bytes[i] & 0xff, 16));
         }
         return buf.toString();
     }
 
     private static final int fillchar = '=';
     private static final String cvt = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
 
     /**
      * Encodes a byte array into a base64 String.
      *
      * @param data a byte array to encode.
      * @return a base64 encode String.
      */
     public static String encodeBase64(byte[] data) {
         return encodeBase64(data, data.length);
     }
 
     /**
      * Encodes a byte array into a base64 String.
      *
      * @param data a byte array to encode.
      * @param len  length of the byte array to use
      * @return a base64 encode String.
      */
     public static String encodeBase64(byte[] data, int len) {
         int c;
         StringBuilder ret = new StringBuilder(((len / 3) + 1) * 4);
         for (int i = 0; i < len; ++i) {
             c = (data[i] >> 2) & 0x3f;
             ret.append(cvt.charAt(c));
             c = (data[i] << 4) & 0x3f;
             if (++i < len)
                 c |= (data[i] >> 4) & 0x0f;
             ret.append(cvt.charAt(c));
             if (i < len) {
                 c = (data[i] << 2) & 0x3f;
                 if (++i < len)
                     c |= (data[i] >> 6) & 0x03;
                 ret.append(cvt.charAt(c));
             } else {
                 ++i;
                 ret.append((char) fillchar);
             }
             if (i < len) {
                 c = data[i] & 0x3f;
                 ret.append(cvt.charAt(c));
             } else {
                 ret.append((char) fillchar);
             }
         }
         return ret.toString();
     }
 
     /**
      * Format a resource, replacing all {x} by the appropriate value in values.
      * If values is an instance of FxString the given localeId is used
      *
      * @param resource   String containing {0}..{n} placeholders
      * @param languageId used locale if values contain FxString instances
      * @param values     either FxString, FxException or any Object using toString()
      * @return formatted resource
      */
     public static String formatResource(String resource, long languageId, Object... values) {
         if (resource == null)
             return "";
         StringBuilder msg = new StringBuilder((int) (resource.length() * 1.5));
         int pos = 0;
         boolean inRep = false;
         int index;
         StringBuilder sbIdx = new StringBuilder(5);
         while (pos < resource.length()) {
             if (resource.charAt(pos) == '{') {
                 inRep = true;
                 sbIdx.setLength(0);
             } else if (resource.charAt(pos) == '}' && inRep) {
                 inRep = false;
                 try {
                     index = Integer.parseInt(sbIdx.toString());
                 } catch (NumberFormatException e) {
                     index = -1;
                 }
                 if (index >= 0 && index < values.length) {
                     final Object value = values[index];
                     if (value instanceof FxString) {
                         if (((FxString) value).translationExists(languageId))
                             msg.append(((FxString) value).getTranslation(languageId));
                         else
                             msg.append(((FxString) value).getDefaultTranslation());
                     } else if (value instanceof FxApplicationException) {
                         msg.append(((FxApplicationException) value).getMessage(languageId));
                     } else if (value instanceof FxRuntimeException) {
                         msg.append(((FxRuntimeException) value).getMessage(languageId));
                     } else
                         msg.append(String.valueOf(value));
                 } else {
                     // append unset parameter
                     msg.append("{").append(index).append("}");
                 }
             } else if (!inRep)
                 msg.append(resource.charAt(pos));
             if (inRep && resource.charAt(pos) >= '0' && resource.charAt(pos) <= '9')
                 sbIdx.append(resource.charAt(pos));
             pos++;
         }
         return msg.toString();
     }
 
     /**
      * Escape text content for output in a javascript function call parameter,
      * based on the Struts TagUtils#filter function.
      *
      * @param value           the text content
      * @param replaceNewlines replaces linebreaks with html <br> tags if set to true,
      *                        or simply removes them (replaces them with spaces) if set to false.
      * @param filterHtml      filter html?
      * @return the escaped text
      */
     public static String escapeForJavaScript(String value, boolean replaceNewlines, boolean filterHtml) {
         if (value != null) {
             // based on org.apache.struts.util.ResponseUtils.filter, custom handling of HTML identifiers
             StringBuffer result = null;
             String filtered;
             for (int i = 0; i < value.length(); i++) {
                 char lookAhead = i < value.length() - 1 ? value.charAt(i + 1) : '\0';
                 filtered = null;
                 switch (value.charAt(i)) {
                     case '<':
                         if (filterHtml)
                             filtered = "&lt;";
                         break;
                     case '>':
                         if (filterHtml)
                             filtered = "&gt;";
                         break;
                     case '&':
                         if (filterHtml && lookAhead != '#')
                             filtered = "&amp;";
                         break;
                     case '"':
                         filtered = "&quot;";
                         break;
                     case '\'':
                         filtered = "&#39;";
                         break;
                     case '\r':
                         filtered = (replaceNewlines && lookAhead != '\n') ? "<br/>" : " ";
                         break;
                     case '\n':
                         filtered = replaceNewlines ? "<br/>" : " ";
                         break;
                 }
 
                 if (result == null) {
                     if (filtered != null) {
                         result = new StringBuffer(value.length() + 50);
                         if (i > 0) {
                             result.append(value.substring(0, i));
                         }
                         result.append(filtered);
                     }
                 } else {
                     if (filtered == null) {
                         result.append(value.charAt(i));
                     } else {
                         result.append(filtered);
                     }
                 }
             }
             return result == null ? value : result.toString();
         } else {
             return "";
         }
     }
 
     /**
      * Escape text content for output in a javascript function call parameter.
      *
      * @param value           the text content
      * @param replaceNewlines replaces linebreaks with html <br> tags if set to true,
      *                        or simply removes them (replaces them with spaces) if set to false.
      * @return the escaped text
      */
     public static String escapeForJavaScript(String value, boolean replaceNewlines) {
         return escapeForJavaScript(value, replaceNewlines, false);
     }
 
     /**
      * Escape text content for output in a javascript function call parameter.
      *
      * @param value the text content
      * @return the escaped text
      */
     public static String escapeForJavaScript(String value) {
         return escapeForJavaScript(value, false, false);
     }
 
     /**
      * Generic SQL escape method.
      *
      * @param value the value to be formatted
      * @return the formatted value
      */
     public static String escapeForSql(Object value) {
         if (value instanceof FxValue) {
             return ((FxValue) value).getSqlValue();
         } else if (value instanceof String) {
             return "'" + StringUtils.replace((String) value, "'", "''") + "'";
         } else if (value instanceof Date) {
             return "'" + new Formatter().format("%tF", (Date) value) + "'";
         } else if (value instanceof FxSelectListItem) {
             return String.valueOf(((FxSelectListItem) value).getId());
         } else if (value instanceof SelectMany) {
             final SelectMany selectMany = (SelectMany) value;
             final List<Long> selectedIds = selectMany.getSelectedIds();
             if (selectedIds.size() > 1) {
                 return "(" + StringUtils.join(selectedIds.iterator(), ',') + ")";
             } else if (selectedIds.size() == 1) {
                 return String.valueOf(selectedIds.get(0));
             } else {
                 return "-1";
             }
         } else if (value != null && value.getClass().isArray()) {
             // decode array via reflection to support primitive arrays
             final List<Object> result = new ArrayList<Object>();
             final int len = Array.getLength(value);
             for (int i = 0; i < len; i++) {
                 result.add(Array.get(value, i));
             }
             return makeTuple(result);
         } else if (value instanceof Collection) {
             return makeTuple((Collection) value);
         } else if (value != null) {
             return value.toString();
         } else {
             return "null";
         }
     }
 
     /**
      * Escape the given (string) value for rendering in a HTML or XML attribute (enclosed by double quotes).
      *
      * @param value the value to be escaped
      * @return      the escaped value
      * @since       3.1.3
      */
     public static String escapeForAttribute(Object value) {
         if (value == null) {
             return "";
         }
         return StringUtils.replace(value.toString(), "\"", "\\\"");
     }
 
     /**
      * Escape the given value for CSV.
      *
      * @param value the value to be formatted
      * @return      the escaped value.
      * @since       3.1
      */
     public static String escapeForCsv(Object value) {
         if (value == null) {
             return "";
         }
         return "\"" + value.toString().replace("\"", "\"\"").replace('\n', ' ').replace('\r', ' ') + "\"";
     }
 
     private static String makeTuple(Collection values) {
         final List<String> result = new ArrayList<String>(values.size());
         for (Object value : values) {
             result.add(escapeForSql(value));
         }
         return "(" + StringUtils.join(result, ',') + ")";
     }
 
     /**
      * Returns a basic date/time format that is readable but not localized.
      *
      * @return a basic date/time format that is readable but not localized.
      */
     public static SimpleDateFormat getDateTimeFormat() {
         return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
     }
 
     /**
      * Returns a formatter for the universal date/time format
      *
      * @return formatter for the universal date/time format
      */
     public static SimpleDateFormat getUniversalDateTimeFormat() {
         return new SimpleDateFormat(UNIVERSAL_TIMEFORMAT);
     }
 
     /**
      * Returns a basic date format that is readable but not localized.
      *
      * @return a basic date format that is readable but not localized.
      */
     public static SimpleDateFormat getDateFormat() {
         return new SimpleDateFormat("yyyy-MM-dd");
     }
 
     /**
      * Convert a String to Boolean
      *
      * @param value value to convert
      * @return Boolean
      */
     public static Boolean toBoolean(String value) {
         try {
             value = unquote(value);
             return Boolean.parseBoolean(value) || "1".equals(value);
         } catch (Exception e) {
             throw new FxConversionException(e, "ex.conversion.value.error", FxBoolean.class.getCanonicalName(), value,
                     e.getMessage()).asRuntimeException();
         }
     }
 
     /**
      * Convert a String to Integer
      *
      * @param value value to convert
      * @return Integer
      */
     public static Integer toInteger(String value) {
        if (StringUtils.isBlank(value))
             return null;
         ParsePosition pos = new ParsePosition(0);
         String _value = value.trim();
         final Number parse = FxValueRendererFactory.getNumberFormatInstance().parse(unquote(_value), pos);
         if (pos.getErrorIndex() >= 0 || pos.getIndex() != _value.length() || parse.doubleValue() != (double)parse.intValue() /*truncation*/)
             throw new FxConversionException("ex.conversion.value.error", FxDouble.class.getCanonicalName(), value, "Failed to parse " + value).asRuntimeException();
         return parse.intValue();
     }
 
     /**
      * Convert a String to Long
      *
      * @param value value to convert
      * @return Long
      */
     public static Long toLong(String value) {
        if (StringUtils.isBlank(value))
             return null;
         ParsePosition pos = new ParsePosition(0);
         String _value = value.trim();
         final Number parse = FxValueRendererFactory.getNumberFormatInstance().parse(unquote(_value), pos);
         if (pos.getErrorIndex() >= 0 || pos.getIndex() != _value.length() || parse.doubleValue() != (double)parse.longValue() /*truncation*/)
             throw new FxConversionException("ex.conversion.value.error", FxDouble.class.getCanonicalName(), value, "Failed to parse " + value).asRuntimeException();
         return parse.longValue();
     }
 
     /**
      * Convert a String to Double
      *
      * @param value value to convert
      * @return Double
      */
     public static Double toDouble(String value) {
        if (StringUtils.isBlank(value))
             return null;
         ParsePosition pos = new ParsePosition(0);
         String _value = value.trim();
         Double res = FxValueRendererFactory.getNumberFormatInstance().parse(unquote(_value), pos).doubleValue();
         if (pos.getErrorIndex() >= 0 || pos.getIndex() != _value.length())
             throw new FxConversionException("ex.conversion.value.error", FxDouble.class.getCanonicalName(), value, "Failed to parse " + value).asRuntimeException();
         return res;
     }
 
     /**
      * Convert a String to Float
      *
      * @param value value to convert
      * @return Float
      */
     public static Float toFloat(String value) {
        if (StringUtils.isBlank(value))
             return null;
         ParsePosition pos = new ParsePosition(0);
         String _value = value.trim();
         Float res = FxValueRendererFactory.getNumberFormatInstance().parse(unquote(_value), pos).floatValue();
         if (pos.getErrorIndex() >= 0 || pos.getIndex() != _value.length())
             throw new FxConversionException("ex.conversion.value.error", FxDouble.class.getCanonicalName(), value, "Failed to parse " + value).asRuntimeException();
         return res;
     }
 
     /**
      * Convert a String to Date
      *
      * @param value value to convert
      * @return Date
      */
     public static Date toDate(String value) {
         try {
             try {
                 return FxValueRendererFactory.getDateFormat().parse(unquote(value));
             } catch (ParseException e) {
                 //fallback to universal format if "short" format is no match
                 return new SimpleDateFormat(UNIVERSAL_TIMEFORMAT).parse(unquote(value));
             }
         } catch (Exception e) {
             throw new FxConversionException(e, "ex.conversion.value.error", FxDate.class.getCanonicalName(), value,
                     e.getMessage()).asRuntimeException();
         }
     }
 
     /**
      * Convert a String to DateTime
      *
      * @param value value to convert
      * @return Date
      */
     public static Date toDateTime(String value) {
         try {
             try {
                 return FxValueRendererFactory.getDateTimeFormat().parse(unquote(value));
             } catch (ParseException e) {
                 try {
                     //fallback to universal format if "short" format is no match
                     return new SimpleDateFormat(UNIVERSAL_TIMEFORMAT).parse(unquote(value));
                 } catch (ParseException e2) {
                     return getDateFormat().parse(unquote(value));
                 }
             }
         } catch (Exception e) {
             throw new FxConversionException(e, "ex.conversion.value.error", FxDate.class.getCanonicalName(), value,
                     e.getMessage()).asRuntimeException();
         }
     }
 
     /**
      * Remove special command characters (such as newlines) from the given string.
      *
      * @param text the string to be filtered
      * @return the string without command characters
      * @since 3.1
      */
     public static String removeCommandChars(String text) {
         return text.replaceAll("[\\x00-\\x09\\x0B-\\x1F]", "");
     }
 
     /**
      * Get an int as String with at least 3 digits, padded by "0" if needed
      *
      * @param value int value
      * @return padded String
      */
     public static String getPaddedIntString3(int value) {
         if (value < 10)
             return "00" + String.valueOf(value);
         else if (value < 100)
             return "0" + String.valueOf(value);
         else
             return String.valueOf(value);
     }
 
     /**
      * Format a timespan given in ms as human readable output
      * (using hours as the maximum time unit)
      *
      * @since 3.1.2
      * @param time time in ms
      * @return human readable time
      */
     public static String formatTimeSpan(long time) {
         StringBuilder res = new StringBuilder(10);
         long ms = time % 1000;
         long s = time / 1000;
         long min = 0;
         long hr = 0;
         if (s > 60) {
             min = s / 60;
             s = s - (min * 60);
         }
         if (min > 60) {
             hr = min / 60;
             min = min - (hr * 60);
         }
         if (hr > 0)
             res.append(hr).append("hr:");
         if (min > 0) {
             if (res.length() > 0 && min < 10)
                 res.append('0');
             res.append(min).append("min:");
         }
         if (s > 0) {
             if (res.length() > 0 && s < 10)
                 res.append('0');
             res.append(s).append("s:");
         }
         res.append(ms).append("ms");
         return res.toString();
     }
 
     /**
      * Format a timespan given in ms as human readable output
      * (using weeks as the maximum time unit and
      * seconds as the smallest)
      *
      * @param time time in ms
      * @return human readable time
      */
     public static String formatTimeSpanForScheduler(long time) {
         if (time <0)
             return "-";
 
         StringBuilder res = new StringBuilder(10);
         long s = time / 1000;
         long min = 0;
         long hr = 0;
         long d = 0;
         long w = 0;
         if (s >= 60) {
             min = s / 60;
             s = s - (min * 60);
         }
         if (min >= 60) {
             hr = min / 60;
             min = min - (hr * 60);
         }
         if (hr >= 24) {
             d = hr / 24;
             hr = hr - (d * 24);
         }
         if (d >= 7) {
             w = d / 7;
             d = d - (w * 7);
         }
         if (w >0)
             res.append(w).append("w:");
         if (d>0)
             res.append(d).append("d:");
         if (hr>0)
             res.append(hr).append("hr:");
         if (min > 0) {
             res.append(min).append("min:");
         }
         if (s > 0) {
             res.append(s).append("s:");
         }
         if (res.length() ==0)
             return "0s";
         //cut off last ":"
         return res.substring(0,res.length()-1);
     }
 
     /**
      * Convert international characters to hex-encoded unicode format (\\u....)
      * internat. chars: e.g. ~ 0x007E - 0x00FF
      * Takes one or two character ranges as parameters (second range == null --> converted to "first" range)
      * Ranges are inclusive!
      *
      * @param input the input String
      * @param start1 start of range 1
      * @param end1 end of range 2
      * @param start2 start of range 2
      * @param end2 end of range 2
      * @return the String containing unicode characters
      */
     public static String convertToJavaUnicode(String input, Character start1, Character end1, Character start2, Character end2) {
         if (start1 == null || end1 == null)
             return input;
 
         StringBuilder out = new StringBuilder(input.length());
         String hex;
         char ch;
 
         if (start2 == null || end2 == null) {
             start2 = start1;
             end2 = end1;
         }
 
         // walk through indiv. chars and convert as needed while appending to "out"
         for (int i = 0; i < input.length(); i++) {
             ch = input.charAt(i);
 
             if ((ch >= start1) && (ch <= end1) || (ch >= start2) && (ch <= end2)) {
                 out.append("\\u");
                 hex = Integer.toHexString(input.charAt(i) & 0xFFFF);
                 // prepend 0s
                 for (int j = 0; j < 4 - hex.length(); j++)
                     out.append("0");
 
                 out.append(hex.toUpperCase());
             } else {
                 out.append(ch);
             }
         }
         return out.toString();
     }
 }

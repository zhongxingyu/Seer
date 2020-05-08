 /*
  * Copyright (C) 2003, 2004  Pascal Essiembre, Essiembre Consultant Inc.
  * 
  * This file is part of Essiembre ResourceBundle Editor.
  * 
  * Essiembre ResourceBundle Editor is free software; you can redistribute it 
  * and/or modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * 
  * Essiembre ResourceBundle Editor is distributed in the hope that it will be 
  * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with Essiembre ResourceBundle Editor; if not, write to the 
  * Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
  * Boston, MA  02111-1307  USA
  */
 package com.essiembre.eclipse.rbe.model.bundle;
 
 import com.essiembre.eclipse.rbe.RBEPlugin;
 import com.essiembre.eclipse.rbe.model.workbench.RBEPreferences;
 
 /**
  * Bundle-related utility methods. 
  * @author Pascal Essiembre (essiembre@users.sourceforge.net)
  * @version $Author$ $Revision$ $Date$
  */
 public final class PropertiesParser {
 
     /** System line separator. */
     private static final String SYSTEM_LINE_SEPARATOR = 
             System.getProperty("line.separator"); //$NON-NLS-1$
     
     /** Characters accepted as key value separators. */
     private static final String KEY_VALUE_SEPARATORS = "=:"; //$NON-NLS-1$
 
     
     /**
      * Constructor.
      */
     private PropertiesParser() {
         super();
     }
 
     /**
      * Parses a string and converts it to a <code>Bundle</code>.  The string is 
      * expected to match the documented structure of a properties file.
      * The returned bundle will have no <code>Locale</code> and no
      * <code>BundleGroup</code> associated to it.
      * @param properties the string containing the properties to parse
      * @return a new bundle
      */
     public static Bundle parse(String properties) {
 
         Bundle bundle = new Bundle();
         String[] lines = properties.split("\r\n|\r|\n"); //$NON-NLS-1$
         
         boolean doneWithFileComment = false;
         StringBuffer fileComment = new StringBuffer();
         StringBuffer lineComment = new StringBuffer();
         StringBuffer lineBuf = new StringBuffer();
         for (int i = 0; i < lines.length; i++) {
             String line = lines[i];
             lineBuf.setLength(0);
             lineBuf.append(line);
         
             int equalPosition = findKeyValueSeparator(line);
             boolean isRegularLine = line.matches("^[^#].*"); //$NON-NLS-1$
             boolean isCommentedLine = doneWithFileComment 
                     && line.matches("^##[^#].*"); //$NON-NLS-1$
             
             // parse regular and commented lines
             if (equalPosition >= 1 && (isRegularLine || isCommentedLine)) {
                 doneWithFileComment = true;
                 String comment = ""; //$NON-NLS-1$
                 if (lineComment.length() > 0) {
                     comment = lineComment.toString();
                     lineComment.setLength(0);
                 }
 
                 if (isCommentedLine) {
                     lineBuf.delete(0, 2); // remove ##
                     equalPosition -= 2;
                 }
                 String backslash = "\\"; //$NON-NLS-1$
                 while (lineBuf.lastIndexOf(backslash) == lineBuf.length() -1) {
                     int lineBreakPosition = lineBuf.lastIndexOf(backslash);
                     lineBuf.replace(
                             lineBreakPosition,
                             lineBreakPosition + 1, ""); //$NON-NLS-1$
                    if (++i < lines.length) {
                         String wrappedLine = lines[i].trim();
                         if (isCommentedLine) {
                             lineBuf.append(wrappedLine.replaceFirst(
                                     "^##", "")); //$NON-NLS-1$ //$NON-NLS-2$
                         } else {
                             lineBuf.append(wrappedLine);
                         }
                     }
                 }
                 String key = lineBuf.substring(0, equalPosition).trim();
                 key = unescapeKey(key);
                 
                 String value = lineBuf.substring(equalPosition + 1).trim();
                 // Unescape leading spaces
                 if (value.startsWith("\\ ")) { //$NON-NLS-1$
                     value = value.substring(1);
                 }
                 
                 if (RBEPreferences.getConvertEncodedToUnicode()) {
                     key = PropertiesParser.convertEncodedToUnicode(key);
                     value = PropertiesParser.convertEncodedToUnicode(value);
                 } else {
                     value = value.replaceAll(
                             "\\\\r", "\r"); //$NON-NLS-1$ //$NON-NLS-2$
                     value = value.replaceAll(
                             "\\\\n", "\n");  //$NON-NLS-1$//$NON-NLS-2$
                 }
                 bundle.addEntry(
                         new BundleEntry(key, value, comment, isCommentedLine));
             // parse comment line
             } else if (lineBuf.indexOf("#") == 0) { //$NON-NLS-1$
                 if (!doneWithFileComment) {
                     fileComment.append(lineBuf);
                     fileComment.append(SYSTEM_LINE_SEPARATOR);
                 } else {
                     lineComment.append(lineBuf);
                     lineComment.append(SYSTEM_LINE_SEPARATOR);
                 }
             // handle blank or unsupported line
             } else {
                 doneWithFileComment = true;
             }
         }
         bundle.setComment(fileComment.toString());
         return bundle;
     }
     
     
     /**
      * Converts encoded &#92;uxxxx to unicode chars
      * and changes special saved chars to their original forms
      * @param str the string to convert
      * @return converted string
      * @see java.util.Properties
      */
     public static String convertEncodedToUnicode(String str) {
         char aChar;
         int len = str.length();
         StringBuffer outBuffer = new StringBuffer(len);
 
         for (int x = 0; x < len;) {
             aChar = str.charAt(x++);
             if (aChar == '\\' && x + 1 <= len) {
                 aChar = str.charAt(x++);
                 if (aChar == 'u' && x + 4 <= len) {
                     // Read the xxxx
                     int value = 0;
                     for (int i = 0; i < 4; i++) {
                         aChar = str.charAt(x++);
                         switch (aChar) {
                         case '0': case '1': case '2': case '3': case '4':
                         case '5': case '6': case '7': case '8': case '9':
                             value = (value << 4) + aChar - '0';
                             break;
                         case 'a': case 'b': case 'c':
                         case 'd': case 'e': case 'f':
                             value = (value << 4) + 10 + aChar - 'a';
                             break;
                         case 'A': case 'B': case 'C':
                         case 'D': case 'E': case 'F':
                             value = (value << 4) + 10 + aChar - 'A';
                             break;
                         default:
                             value = aChar;
                             System.err.println(RBEPlugin.getString(
                                  "error.init.badencoding") + str); //$NON-NLS-1$
                         }
                     }
                     outBuffer.append((char) value);
                 } else {
                     if (aChar == 't') {
                         aChar = '\t';
                     } else if (aChar == 'r') {
                         aChar = '\r';
                     } else if (aChar == 'n') {
                         aChar = '\n';
                     } else if (aChar == 'f') {
                         aChar = '\f';
                     } else if (aChar == 'u') {
                         outBuffer.append("\\"); //$NON-NLS-1$
                     }
                     outBuffer.append(aChar);
                 }
             } else {
                 outBuffer.append(aChar);
             }
         }
         return outBuffer.toString();
     }
     
     /**
      * Finds the separator symbol that separates keys and values.
      * @param str the string on which to find seperator
      * @return the separator index or -1 if no separator was found
      */
     private static int findKeyValueSeparator(String str) {
         int index = -1;
         int length = str.length();
         for (int i = 0; i < length; i++) {
             char currentChar = str.charAt(i);
             if (currentChar == '\\') {
                 i++;
             } else if (KEY_VALUE_SEPARATORS.indexOf(currentChar) != -1) {
                 index = i;
                 break;
             }
         }
         return index;
     }
     
     private static String unescapeKey(String key) {
         int length = key.length();
         StringBuffer buf = new StringBuffer();
         for (int index = 0; index < length; index++) {
             char currentChar = key.charAt(index);
             if (currentChar != '\\') {
                 buf.append(currentChar);
             }
         }
         return buf.toString();
     }
 }

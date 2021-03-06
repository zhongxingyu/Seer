 /*
  * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.ui.messages;
 
 import com.dmdirc.Config;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.Logger;
 import com.dmdirc.ui.textpane.IRCTextAttribute;
 import com.dmdirc.ui.textpane.TextPane;
 
 import java.awt.Color;
 import java.awt.font.TextAttribute;
 import java.text.AttributedString;
 import java.util.Enumeration;
 import java.util.Locale;
 
 import javax.swing.UIManager;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.Element;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyleConstants.CharacterConstants;
 import javax.swing.text.StyleConstants.ColorConstants;
 import javax.swing.text.StyleConstants.FontConstants;
 import javax.swing.text.StyledDocument;
 
 /**
  * The styliser applies IRC styles to text. Styles are indicated by various
  * control codes which are a de-facto IRC standard.
  * @author chris
  */
 public final class Styliser {
     
     /** The character used for marking up bold text. */
     public static final char CODE_BOLD = 2;
     /** The character used for marking up coloured text. */
     public static final char CODE_COLOUR = 3;
     /** The character used for marking up coloured text (using hex). */
     public static final char CODE_HEXCOLOUR = 4;
     /** Character used to indicate hyperlinks. */
     public static final char CODE_HYPERLINK = 5;
     /** The character used for stopping all formatting. */
     public static final char CODE_STOP = 15;
     /** The character used for marking up fixed pitch text. */
     public static final char CODE_FIXED = 17;
     /** The character used for marking up italic text. */
     public static final char CODE_ITALIC = 29;
     /** The character used for marking up underlined text. */
     public static final char CODE_UNDERLINE = 31;
     
     /** Characters allowed to be used in an URL. */
     private static final String CHARS = "[^\\s" + CODE_BOLD + CODE_COLOUR
             + CODE_STOP + CODE_HEXCOLOUR + CODE_FIXED + CODE_ITALIC
             + CODE_UNDERLINE + "\"]";
     
     /** The regular expression to use for marking up URLs. */
     private static final String URL_REGEXP = "(?i)([a-z]+://" + CHARS + "+|(?<![a-z:])www\\." + CHARS + "+)";
     
     /** Creates a new instance of Styliser. */
     private Styliser() {
     }
     
     /**
      * Stylises the specified string and adds it to the passed TextPane.
      *
      * @param doc The document which the output should be added to
      * @param string The line to be stylised and added
      */
     public static void addStyledString(final TextPane doc, final String string) {
         final AttributedString text = styledDocumentToAttributedString(getStyledString(new String[]{string, }));
         
         if (text.getIterator().getEndIndex() == 0) {
             doc.addText(new AttributedString("\n"));
         } else {
             doc.addText(text);
         }
     }
     
     /**
      * Stylises the specified string and adds it to the passed TextPane.
      *
      * @param doc The document which the output should be added to
      * @param strings The strings to be stylised and added to a line
      */
     public static void addStyledString(final TextPane doc, final String[] strings) {
         final AttributedString text = styledDocumentToAttributedString(getStyledString(strings));
         
         if (text.getIterator().getEndIndex() == 0) {
             doc.addText(new AttributedString("\n"));
         } else {
             doc.addText(text);
         }
     }
     
     /**
      * Stylises the specified string.
      *
      * @param strings The line to be stylised
      *
      * @return StyledDocument for the inputted strings
      */
     public static StyledDocument getStyledString(final String[] strings) {
         final StyledDocument styledDoc = new DefaultStyledDocument();
         
         for (int i = 0; i < strings.length; i++) {
             final char[] chars = strings[i].toCharArray();
             
             for (int j = 0; j < chars.length; j++) {
                 if (chars[j] == 65533) {
                     chars[j] = '?';
                 }
             }
             strings[i] = new String(chars);
             final String string = strings[i];
             try {
                 int offset = styledDoc.getLength();
                 int position = 0;
                 
                 final String target = string.replaceAll(URL_REGEXP, CODE_HYPERLINK + "$0" + CODE_HYPERLINK);
                 
                 final SimpleAttributeSet attribs = new SimpleAttributeSet();
                attribs.addAttribute("DefaultFontFamily", UIManager.getFont("TextPane.font"));
                         
                 while (position < target.length()) {
                     final String next = readUntilControl(target.substring(position));
                     
                     styledDoc.insertString(offset, next, attribs);
                     
                     position += next.length();
                     offset += next.length();
                     
                     if (position < target.length()) {
                         position += readControlChars(target.substring(position),
                                 attribs, position == 0);
                     }
                 }
                 
             } catch (BadLocationException ex) {
                 Logger.userError(ErrorLevel.MEDIUM,
                         "Unable to insert styled string: " + ex.getMessage());
             }
         }
         return styledDoc;
     }
     
     /**
      * Converts a StyledDocument into an AttributedString.
      *
      * @param doc StyledDocument to convert
      *
      * @return AttributedString representing the specified StyledDocument
      */
     private static AttributedString styledDocumentToAttributedString(final StyledDocument doc) {
         //Now lets get hacky, loop through the styled document and add all styles to an attributedString
         AttributedString attString = null;
         final Element line = doc.getParagraphElement(0);
         try {
             attString = new AttributedString(line.getDocument().getText(0, line.getDocument().getLength()));
         } catch (BadLocationException ex) {
             Logger.userError(ErrorLevel.MEDIUM,
                     "Unable to insert styled string: " + ex.getMessage());
         }
         for (int i = 0; i < line.getElementCount(); i++) {
             final Element element = line.getElement(i);
             
             final AttributeSet as = element.getAttributes();
             final Enumeration<?> ae = as.getAttributeNames();
             
             while (ae.hasMoreElements()) {
                 final Object attrib = ae.nextElement();
                 
                 if (attrib == IRCTextAttribute.HYPERLINK) {
                     //hyperlink
                     attString.addAttribute(IRCTextAttribute.HYPERLINK, as.getAttribute(attrib), element.getStartOffset(), element.getEndOffset());
                 } else if (attrib == ColorConstants.Foreground) {
                     //Foreground
                     attString.addAttribute(TextAttribute.FOREGROUND, as.getAttribute(attrib), element.getStartOffset(), element.getEndOffset());
                 } else if (attrib == ColorConstants.Background) {
                     //Background
                     attString.addAttribute(TextAttribute.BACKGROUND, as.getAttribute(attrib), element.getStartOffset(), element.getEndOffset());
                 } else if (attrib == FontConstants.Bold) {
                     //Bold
                     attString.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, element.getStartOffset(), element.getEndOffset());
                 } else if (attrib == FontConstants.Family) {
                     //Family
                     attString.addAttribute(TextAttribute.FAMILY, as.getAttribute(attrib), element.getStartOffset(), element.getEndOffset());
                 } else if (attrib == FontConstants.Italic) {
                     //italics
                     attString.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE, element.getStartOffset(), element.getEndOffset());
                 } else if (attrib == CharacterConstants.Underline) {
                     //Underline
                     attString.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, element.getStartOffset(), element.getEndOffset());
                 }
             }
         }
         
         return attString;
     }
     
     /**
      * Strips all recognised control codes from the input string.
      * @param input the String to be stripped
      * @return a copy of the input with control codes removed
      */
     public static String stipControlCodes(final String input) {
         int position = 0;
         String output = "";
         
         while (position < input.length()) {
             final String next = readUntilControl(input.substring(position));
             
             output = output.concat(next);
             
             position += next.length();
             
             if (position < input.length()) {
                 position += readControlChars(input.substring(position),
                         new SimpleAttributeSet(), position == 0);
             }
         }
         
         return output;
     }
     
     /**
      * Returns a substring of the input string such that no control codes are present
      * in the output. If the returned value isn't the same as the input, then the
      * character immediately after is a control character.
      * @param input The string to read from
      * @return A substring of the input containing no control characters
      */
     static String readUntilControl(final String input) {
         int pos = input.length();
         
         pos = checkChar(pos, input.indexOf(CODE_BOLD));
         pos = checkChar(pos, input.indexOf(CODE_UNDERLINE));
         pos = checkChar(pos, input.indexOf(CODE_STOP));
         pos = checkChar(pos, input.indexOf(CODE_COLOUR));
         pos = checkChar(pos, input.indexOf(CODE_HEXCOLOUR));
         pos = checkChar(pos, input.indexOf(CODE_ITALIC));
         pos = checkChar(pos, input.indexOf(CODE_FIXED));
         pos = checkChar(pos, input.indexOf(CODE_HYPERLINK));
         
         return input.substring(0, pos);
     }
     
     /**
      * Helper function used in readUntilControl. Checks if i is a valid index of
      * the string (i.e., it's not -1), and then returns the minimum of pos and i.
      * @param pos The current position in the string
      * @param i The index of the first occurance of some character
      * @return The new position (see implementation)
      */
     private static int checkChar(final int pos, final int i) {
         if (i < pos && i != -1) { return i; }
         return pos;
     }
     
     /**
      * Reads the first control character from the input string (and any arguments
      * it takes), and applies it to the specified attribute set.
      * @return The number of characters read as control characters
      * @param string The string to read from
      * @param attribs The attribute set that new attributes will be applied to
      * @param isStart Whether this is at the start of the string or not
      */
     private static int readControlChars(final String string,
             final SimpleAttributeSet attribs, final boolean isStart) {
         // Bold
         if (string.charAt(0) == CODE_BOLD) {
             toggleAttribute(attribs, StyleConstants.FontConstants.Bold);
             return 1;
         }
         
         // Underline
         if (string.charAt(0) == CODE_UNDERLINE) {
             toggleAttribute(attribs, StyleConstants.FontConstants.Underline);
             return 1;
         }
         
         // Italic
         if (string.charAt(0) == CODE_ITALIC) {
             toggleAttribute(attribs, StyleConstants.FontConstants.Italic);
             return 1;
         }
         
         // Hyperlinks
         if (string.charAt(0) == CODE_HYPERLINK) {
             toggleLink(attribs);
             
             if (attribs.getAttribute(IRCTextAttribute.HYPERLINK) == null) {
                 attribs.addAttribute(IRCTextAttribute.HYPERLINK, readUntilControl(string.substring(1)));
             } else {
                 attribs.removeAttribute(IRCTextAttribute.HYPERLINK);
             }
             return 1;
         }
         
         // Fixed pitch
         if (string.charAt(0) == CODE_FIXED) {
             if (attribs.containsAttribute(StyleConstants.FontConstants.FontFamily, "monospaced")) {
                 attribs.removeAttribute(StyleConstants.FontConstants.FontFamily);
             } else {
                 attribs.removeAttribute(StyleConstants.FontConstants.FontFamily);
                 attribs.addAttribute(StyleConstants.FontConstants.FontFamily, "monospaced");
             }
             return 1;
         }
         
         // Stop formatting
         if (string.charAt(0) == CODE_STOP) {
             resetAttributes(attribs);
             return 1;
         }
         
         // Colours
         if (string.charAt(0) == CODE_COLOUR) {
             int count = 1;
             // This isn't too nice!
             if (string.length() > count && isInt(string.charAt(count))) {
                 int foreground = string.charAt(count) - '0';
                 count++;
                 if (string.length() > count && isInt(string.charAt(count))) {
                     foreground = foreground * 10 + (string.charAt(count) - '0');
                     count++;
                 }
                 foreground = foreground % 16;
                 setForeground(attribs, String.valueOf(foreground));
                 if (isStart) {
                     setDefaultForeground(attribs, String.valueOf(foreground));
                 }
                 
                 // Now background
                 if (string.length() > count && string.charAt(count) == ','
                         && string.length() > count + 1
                         && isInt(string.charAt(count + 1))) {
                     int background = string.charAt(count + 1) - '0';
                     count += 2; // Comma and first digit
                     if (string.length() > count && isInt(string.charAt(count))) {
                         background = background * 10 + (string.charAt(count) - '0');
                         count++;
                     }
                     background = background % 16;
                     setBackground(attribs, String.valueOf(background));
                     if (isStart) {
                         setDefaultBackground(attribs, String.valueOf(background));
                     }
                 }
             } else {
                 resetColour(attribs);
             }
             return count;
         }
         
         // Hex colours
         if (string.charAt(0) == CODE_HEXCOLOUR) {
             int count = 1;
             if (hasHexString(string, 1)) {
                 setForeground(attribs, string.substring(1, 7).toUpperCase());
                 if (isStart) {
                     setDefaultForeground(attribs, string.substring(1, 7).toUpperCase());
                 }
                 count = count + 6;
                 
                 // Now for background
                 if (string.charAt(count) == ',' && hasHexString(string, count + 1)) {
                     count++;
                     setBackground(attribs, string.substring(count, count + 6).toUpperCase());
                     if (isStart) {
                         setDefaultBackground(attribs, string.substring(count, count + 6).toUpperCase());
                     }
                     count += 6;
                 }
             } else {
                 resetColour(attribs);
             }
             return count;
         }
         
         return 0;
     }
     
     /**
      * Determines if the specified character represents a single integer (i.e. 0-9).
      * @param c The character to check
      * @return True iff the character is in the range [0-9], false otherwise
      */
     private static boolean isInt(final char c) {
         return c >= '0' && c <= '9';
     }
     
     /**
      * Determines if the specified character represents a single hex digit
      * (i.e., 0-F).
      * @param c The character to check
      * @return True iff the character is in the range [0-F], false otherwise
      */
     private static boolean isHex(final char c) {
         return isInt(c) || (c >= 'A' && c <= 'Z');
     }
     
     /**
      * Determines if the specified string has a 6-digit hex string starting at
      * the specified offset.
      * @param input The string to check
      * @param offset The offset to start at
      * @return True iff there is a hex string preset at the offset
      */
     private static boolean hasHexString(final String input, final int offset) {
         // If the string's too short, it can't have a hex string
         if (input.length() < offset + 6) {
             return false;
         }
         boolean res = true;
         for (int i = offset; i < 6 + offset; i++) {
             res = res && isHex(input.toUpperCase(Locale.getDefault()).charAt(i));
         }
         
         return res;
     }
     
     /**
      * Toggles the various hyperlink-related attributes.
      * @param attribs The attributes to be modified.
      */
     private static void toggleLink(final SimpleAttributeSet attribs) {
         if (Config.getOptionBool("ui", "stylelinks")) {
             if (attribs.getAttribute(IRCTextAttribute.HYPERLINK) == null) {
                 // Add the hyperlink style
                 
                 if (attribs.containsAttribute(StyleConstants.FontConstants.Underline, Boolean.TRUE)) {
                     attribs.addAttribute("restoreUnderline", Boolean.TRUE);
                 } else {
                     attribs.addAttribute(StyleConstants.FontConstants.Underline, Boolean.TRUE);
                 }
                 
                 final Object foreground = attribs.getAttribute(StyleConstants.FontConstants.Foreground);
                 
                 if (foreground != null) {
                     attribs.addAttribute("restoreColour", foreground);
                     attribs.removeAttribute(StyleConstants.FontConstants.Foreground);
                 }
                 
                 attribs.addAttribute(StyleConstants.FontConstants.Foreground, Color.BLUE);
                 
             } else {
                 // Remove the hyperlink style
                 
                 if (attribs.containsAttribute("restoreUnderline", Boolean.TRUE)) {
                     attribs.removeAttribute("restoreUnderline");
                 } else {
                     attribs.removeAttribute(StyleConstants.FontConstants.Underline);
                 }
                 
                 attribs.removeAttribute(StyleConstants.FontConstants.Foreground);
                 final Object foreground = attribs.getAttribute("restoreColour");
                 if (foreground != null) {
                     attribs.addAttribute(StyleConstants.FontConstants.Foreground, foreground);
                     attribs.removeAttribute("restoreColour");
                 }
             }
         }
     }
     
     /**
      * Toggles the specified attribute. If the attribute exists in the attribute
      * set, it is removed. Otherwise, it is added with a value of Boolean.True.
      * @param attribs The attribute set to check
      * @param attrib The attribute to toggle
      */
     private static void toggleAttribute(final SimpleAttributeSet attribs,
             final Object attrib) {
         if (attribs.containsAttribute(attrib, Boolean.TRUE)) {
             attribs.removeAttribute(attrib);
         } else {
             attribs.addAttribute(attrib, Boolean.TRUE);
         }
     }
     
     /**
      * Resets all attributes in the specified attribute list.
      * @param attribs The attribute list whose attributes should be reset
      */
     private static void resetAttributes(final SimpleAttributeSet attribs) {
         if (attribs.containsAttribute(StyleConstants.FontConstants.Bold, Boolean.TRUE)) {
             attribs.removeAttribute(StyleConstants.FontConstants.Bold);
         }
         if (attribs.containsAttribute(StyleConstants.FontConstants.Underline, Boolean.TRUE)) {
             attribs.removeAttribute(StyleConstants.FontConstants.Underline);
         }
         if (attribs.containsAttribute(StyleConstants.FontConstants.Italic, Boolean.TRUE)) {
             attribs.removeAttribute(StyleConstants.FontConstants.Italic);
         }
         if (attribs.containsAttribute(StyleConstants.FontConstants.FontFamily, "monospace")) {
             final Object defaultFont = attribs.getAttribute("DefaultFontFamily");
             attribs.removeAttribute(StyleConstants.FontConstants.FontFamily);
             attribs.addAttribute(StyleConstants.FontConstants.FontFamily, defaultFont);
         }
         resetColour(attribs);
     }
     
     /**
      * Resets the colour attributes in the specified attribute set.
      * @param attribs The attribute set whose colour attributes should be reset
      */
     private static void resetColour(final SimpleAttributeSet attribs) {
         if (attribs.isDefined(StyleConstants.Foreground)) {
             attribs.removeAttribute(StyleConstants.Foreground);
         }
         if (attribs.isDefined("DefaultForeground")) {
             attribs.addAttribute(StyleConstants.Foreground,
                     attribs.getAttribute("DefaultForeground"));
         }
         if (attribs.isDefined(StyleConstants.Background)) {
             attribs.removeAttribute(StyleConstants.Background);
         }
         if (attribs.isDefined("DefaultBackground")) {
             attribs.addAttribute(StyleConstants.Background,
                     attribs.getAttribute("DefaultBackground"));
         }
     }
     
     /**
      * Sets the foreground colour in the specified attribute set to the colour
      * corresponding to the specified colour code or hex.
      * @param attribs The attribute set to modify
      * @param foreground The colour code/hex of the new foreground colour
      */
     private static void setForeground(final SimpleAttributeSet attribs,
             final String foreground) {
         if (attribs.isDefined(StyleConstants.Foreground)) {
             attribs.removeAttribute(StyleConstants.Foreground);
         }
         attribs.addAttribute(StyleConstants.Foreground, ColourManager.parseColour(foreground));
     }
     
     /**
      * Sets the background colour in the specified attribute set to the colour
      * corresponding to the specified colour code or hex.
      * @param attribs The attribute set to modify
      * @param background The colour code/hex of the new background colour
      */
     private static void setBackground(final SimpleAttributeSet attribs,
             final String background) {
         if (attribs.isDefined(StyleConstants.Background)) {
             attribs.removeAttribute(StyleConstants.Background);
         }
         attribs.addAttribute(StyleConstants.Background, ColourManager.parseColour(background));
     }
     
     /**
      * Sets the default foreground colour (used after an empty ctrl+k or a ctrl+o).
      * @param attribs The attribute set to apply this default on
      * @param foreground The default foreground colour
      */
     private static void setDefaultForeground(final SimpleAttributeSet attribs,
             final String foreground) {
         attribs.addAttribute("DefaultForeground", ColourManager.parseColour(foreground));
     }
     
     /**
      * Sets the default background colour (used after an empty ctrl+k or a ctrl+o).
      * @param attribs The attribute set to apply this default on
      * @param background The default background colour
      */
     private static void setDefaultBackground(final SimpleAttributeSet attribs,
             final String background) {
         attribs.addAttribute("DefaultBackground", ColourManager.parseColour(background));
     }
     
 }

 /*
  * HelpPlusPlus - Help pages for smarter people
  * Copyright (C) 2011 lycano <https://github.com/lycano/HelpPlusPlus>
  * Original Credit & Copyright (C) 2011 tkelly910 <https://github.com/tkelly910/Help>
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package de.luricos.bukkit.HelpPlusPlus.utils;
 
 import java.util.ArrayList;
 
 /**
  * MCFontUtils - utility class for measuring minecraft fonts
  * 
  * @author lycano
  */
 public class MCFontUtils {
    private static int chatBoxWidth = 318; // as we calculate with charMargin fontWidths (53 chars ÃƒÂ  (5+charMargin)px = 318px)
     private static int chatBoxHalfWidth = 133;
     private static int charMargin = 1;
     private static int defaultCharWidth = 5;
     
     private static boolean initialized = false;
     private static ArrayList<Integer> charWidths = new ArrayList<Integer>();
     private static ArrayList<Character> charMapIndex = new ArrayList<Character>();
     
     private static void initCharWidths() {
         try {
             charWidths.add(0);
             charMapIndex.add(null);
 
             for (int c = 1; c < 256; c++) {
                 charWidths.add(defaultCharWidth);
                 charMapIndex.add((char) c);
             }
 
             //change non-default ASCII-Chars
             int charWidths8[] = { 1, 2, 8, 10, 11, 13, 14, 15, 20, 21, 23, 29, 30, 31, 176, 177, 178, 193, 194, 196, 197, 202, 203, 205, 206, 207, 208, 209, 210, 215, 216, 219, 220, 223, 236, 237, 251 };
             int charWidths7[] = { 3, 4, 5, 6, 16, 17, 18, 19, 26, 27, 28, 182, 183, 185, 187, 188, 189, 204, 224, 227, 229, 230, 231, 234, 247 };
             int charWidths6[] = { 9, 12, 22, 24, 25, 64, 126, 169, 199, 200, 201, 211, 214, 225, 226, 228, 232, 235, 239, 240, 241, 242, 243, 246 };
             int charWidths4[] = { 7, 34, 40, 41, 42, 60, 62, 102, 107, 123, 125, 221, 222, 253, 254 };
             int charWidths3[] = { 32, 73, 91, 93, 116, 139, 158, 255 };
             int charWidths2[] = { 39, 96, 108, 141, 161, 179, 249, 250 };
             int charWidths1[] = { 33, 44, 46, 58, 59, 105, 124, 173 };
 
             overrideCharWidths(charWidths8, 8);
             overrideCharWidths(charWidths7, 7);
             overrideCharWidths(charWidths6, 6);
             overrideCharWidths(charWidths4, 4);
             overrideCharWidths(charWidths3, 3);
             overrideCharWidths(charWidths2, 2);
             overrideCharWidths(charWidths1, 1);
         } catch (Exception e) {
             HelpLogger.severe("Error during init", e);
         } finally {
             initialized = true;
         }
     }
     
     private static void overrideCharWidths(int[] widthsArray, int toVal) {
         if ((widthsArray != null) && (widthsArray.length > 0)) {
             for (int index : widthsArray) {
                 charWidths.set(index, toVal);
             }
         }
     }
     
     /**
      * return the width of a string respecting char width
      * 
      * @param String s
      * @return int
      */
     public static int getStringWidth(String s) {
         int stringWidth = 0;
         String inputString = s.replaceAll("\u00A7.", "");
         char[] inputStringMap = inputString.toCharArray();
         
         if (s != null) {
             for (char c : inputStringMap) {
                 stringWidth += (getCharWidth(c) + charMargin);
             }
         }
         return stringWidth;
     }
     
     /**
      * return the width of a single char
      * 
      * @param char c
      * @return int
      */
     public static int getCharWidth(char c) {
         return getCharWidth(c, defaultCharWidth);
     }
     
     /**
      * return the width of a single char
      * 
      * @param char c
      * @param int defaultVal
      * @return int
      */
     public static int getCharWidth(char c, int defaultVal) {
         if (!initialized) {
             initCharWidths();
         }
         
         int k = charMapIndex.indexOf(c);
         if (c != '\247' && k >= 0) {
             return charWidths.get(k);
         }
 
         return defaultVal;
     }
     
     /**
      * Return string length ignoring color codes and FontWidth
      * 
      * @param String str
      * @return int 
      */
     public static int strLen(String str) {
         if (!str.contains("\u00A7")) {
             return str.length();
         }
         
         return uncoloredStr(str).length();
     }
     
     /**
      * get chatBoxWidth
      * 
      * @return int chatBoxWidth
      */
     public static int getChatBoxWidth() {
         return chatBoxWidth;
     }
     
     public static int getChatBoxHalfWidth() {
         return chatBoxHalfWidth;
     }  
     
     private static boolean validArg(String... args) {
         for (String s: args) {
             if ((s == null) || (s.isEmpty())) {
                 return false;
             }
         }
         
         return true;
     }
     
     private static boolean validArg(int... args) {
         for (int i: args) {
             if (i == 0) {
                 return false;
             }
         }
         
         return true;        
     }
     
     /**
      * calculate remaining chat width
      * 
      * @param String s - source string
      * @return int - remaining chat width
      */
     public static int getRemainingChatWidth(String s) {
         return (getChatBoxWidth() - getStringWidth(s));
     }
 
     /**
      * return uncolored string
      * 
      * @param String s
      * @return String - unformated string
      */
     public static String uncoloredStr(String s) {
         return s != null ? s.replaceAll("\u00A7.", "") : s;
     }
     
     
     public static String repeatStringUncolored(String s, int times) {
         return repeatString(uncoloredStr(s), times);
     }
     
     
     public static String repeatString(String s, int times) {
        if (times <= 0)
            return s;
        
        StringBuilder sb = new StringBuilder(s.length() * times);
        for (int i = 0; i < times; i++) {
            sb.append(s);
        }
        
        return sb.toString();
     }    
     
     public static String repeatChar(char c, int times) {
        if (times <= 0)
            return String.valueOf(c);
        
        StringBuilder sb = new StringBuilder(times);
        for (int i = 0; i < times; i++) {
            sb.append(c);
        }
        
        return sb.toString();
     }      
     
     public static String unformattedPadLeft(String charSequence, String pad) {
         return strPadLeft(charSequence, pad, getChatBoxWidth(), true);
     }    
     
     /**
      * return unformated left padded charSequence
      * 
      * @param charSequence
      * @param pad
      * @return String - unformatted left padded string
      */
     public static String unformattedPadLeft(String charSequence, String pad, int width) {
         return strPadLeft(charSequence, pad, width, true);
     } 
     
     /**
      * return left padded string ready for chatoutput
      * 
      * @param charSequence
      * @param pad
      * @return String left padded charSequence
      */
     public static String strPadLeftChat(String charSequence, String pad) {
         return strPadLeft(charSequence, pad, getChatBoxWidth(), false);
     }
     
     /**
      * return left padded string by given width
      * 
      * @param charSequence
      * @param pad
      * @param width
      * @param trimColors
      * @return String left padded charSequence
      */
     public static String strPadLeft(String charSequence, String pad, int width, boolean trimColors) {
         if (!validArg(charSequence, pad))
             return charSequence;
         
         width -= getStringWidth(charSequence);
         return repeatString(pad, (width / getStringWidth(pad))).concat(charSequence);
     }
     
     
     public static String unformattedPadCenter(String charSequence, String pad) {
         return strPadCenter(charSequence, pad, getChatBoxWidth(), true);
     }    
     
     /**
      * return unformated center padded charSequence
      * 
      * @param charSequence
      * @param pad
      * @return String - unformatted center padded string
      */
     public static String unformattedPadCenter(String charSequence, String pad, int width) {
         return strPadCenter(charSequence, pad, width, true);
     } 
     
     /**
      * return center padded string ready for chatoutput
      * 
      * @param charSequence
      * @param pad
      * @return String centered padded charSequence
      */
     public static String strPadCenterChat(String charSequence, String pad) {
         return strPadCenter(charSequence, pad, getChatBoxWidth(), false);
     }
     
     /**
      * return center padded string by given width
      * 
      * @param charSequence
      * @param pad
      * @param width
      * @param trimColors
      * @return String centered padded charSequence
      */
     public static String strPadCenter(String charSequence, String pad, int width, boolean trimColors) {
         if (!validArg(charSequence, pad))
             return charSequence;
         
         String line = repeatString(pad, (width/getStringWidth(pad)));
         int maxLen = strLen(line);
         int csLen = strLen(charSequence);
         int centerPos = (int) Math.round(((double) (maxLen - csLen)) / 2);
         
         return line.substring(0, centerPos).concat(charSequence).concat(line.substring((centerPos - 1) + csLen));
     }
     
 
     public static String unformattedPadRight(String charSequence, String pad) {
         return strPadRight(charSequence, pad, getChatBoxWidth(), true);
     }    
     
     /**
      * return unformated right padded charSequence
      * 
      * @param charSequence
      * @param pad
      * @return String - unformatted right padded string
      */
     public static String unformattedPadRight(String charSequence, String pad, int width) {
         return strPadRight(charSequence, pad, width, true);
     }    
     
     /**
      * return right padded string ready for chatoutput
      * 
      * @param charSequence
      * @param pad
      * @return String right padded charSequence
      */
     public static String strPadRightChat(String charSequence, String pad) {
         return strPadRight(charSequence, pad, getChatBoxWidth(), false);
     }
     
     /**
      * return right padded string by given width
      * 
      * @param charSequence
      * @param pad
      * @param width
      * @param trimColors
      * @return String right padded charSequence
      */
     public static String strPadRight(String charSequence, String pad, int width, boolean trimColors) {
         return charSequence;
     }
     
     public static String strCompressChat(String charSequence) {
         return strCompress(charSequence, getChatBoxWidth());
     }
     
     /**
      * compress string to fit in one line with given width
      * 
      * @param charSequence
      * @param width
      * @return 
      */
     public static String strCompress(String charSequence, int width) {
         return charSequence;
     }
     
     /**
      * wordwrap chat text
      * 
      * @param charSequence
      * @param indent
      * @param tab
      * @param lineSep
      * @return 
      */
     public static String strWordWrapChat(String charSequence, int indent, String tab, String lineSep) {
         return strWordWrap(charSequence, indent, tab, lineSep, getChatBoxWidth());
     }
     
     public static String strWordWrapChat(String charSequence, int ident) {
         return strWordWrap(charSequence, ident, " ", ":", getChatBoxWidth());
     }
     
     public static String strWordWrap(String charSequence, int ident, int width) {
         return strWordWrap(charSequence, ident, " ", ":", width);
     }    
     
     public static String strWordWrap(String charSequence, int indent, String tab, String lineSep, int width) {
         return charSequence;
     }    
     
     /**
      * word wrap chat text and align right
      * 
      * @param charSequence
      * @param indent
      * @param tab
      * @param lineSep
      * @return 
      */
     public static String strWordWrapRightChat(String charSequence, int indent, String tab, String lineSep) {
         return strWordWrapRight(charSequence, indent, tab, lineSep, getChatBoxWidth());
     }
     
     public static String strWordWrapRightChat(String charSequence, int ident) {
         return strWordWrapRight(charSequence, ident, " ", ":", getChatBoxWidth());
     }
 
     public static String strWordWrapRight(String charSequence, int ident, int width) {
         return strWordWrapRight(charSequence, ident, " ", ":", width);
     }
     
     public static String strWordWrapRight(String charSequence, int indent, String tab, String lineSep, int width) {
         return charSequence;
     }        
 }

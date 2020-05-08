 /*
  * This file is part of VIMCPlugin.
  *
  * Copyright © 2013 Visual Illusions Entertainment
  *
  * VIMCPlugin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  *
  * VIMCPlugin is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License along with VIMCPlugin.
  * If not, see http://www.gnu.org/licenses/lgpl.html.
  */
 package net.visualillusionsent.minecraft.plugin;
 
 public enum ChatFormat {
 
     /** <b>MARKER §</b> */
     MARKER('\u00A7'),
 
     /** <FONT COLOR=000000><b>BLACK</b></FONT> */
     BLACK('0'),
 
     /** <font color="000066"><b>DARK_BLUE</b></font> */
     DARK_BLUE('1'),
 
     /** <font color="006600"><b>GREEN</b></font> */
     GREEN('2'),
 
     /** <font color="006666"><b>TURQUOISE</b></font> */
     TURQUOISE('3'),
 
     /** <font color="990000"><b>RED</b></font> */
     RED('4'),
 
     /** <font color="540054"><b>PURPLE</b></font> */
     PURPLE('5'),
 
     /** <font color="FF9933"><b>ORANGE</b></font> */
     ORANGE('6'),
 
     /** <font color="CCCCCC"><b>LIGHT_GRAY</b></font> */
     LIGHT_GRAY('7'),
 
     /** <font color="333333"><b>GRAY</b></font> */
     GRAY('8'),
 
     /** <font color="2A2A7F"><b>BLUE</b></font> */
     BLUE('9'),
 
     /** <font color="33FF33"><b>LIGHT_GREEN</b></font> */
     LIGHT_GREEN('A'),
 
     /** <font color="00FFFF"><b>CYAN</b></font> */
     CYAN('B'),
 
     /** <font color="FF0022"><b>LIGHT_RED</b></font> */
     LIGHT_RED('C'),
 
     /** <font color="FF00FF"><b>PINK</b></font> */
     PINK('D'),
 
     /** <font color="FFFF00"><b>YELLOW</b></font> */
     YELLOW('E'),
 
     /** <font color="000000"><b>WHITE</b></font> */
     WHITE('F'),
 
     /** <b>OBFUSCATED</b> */
     OBFUSCATED('K'),
 
     /** <b>BOLD</b> */
     BOLD('L'),
 
     /** <s>STRIKED</s> */
     STRIKED('M'),
 
     /** <u>UNDERLINED</u> */
     UNDERLINED('N'),
 
     /** <i>ITALIC</i> */
     ITALIC('O'),
 
     /** RESET */
     RESET('R');
 
     private final char code;
 
     private ChatFormat(char code) {
         this.code = code;
     }
 
     public final String concat(String str) {
         if (this == MARKER) {
             return stringValue().concat(str);
         }
         else {
             return toString().concat(str);
         }
     }
 
     /**
      * Returns the char value of the {@code ChatFormat}
      *
      * @return char value of the {@code ChatFormat}
      */
     public final char charValue() {
         return code;
     }
 
     /**
      * Returns the {@code ChatFormat} as a {@link String}
      *
      * @return String value of {@code ChatFormat}
      */
     public final String stringValue() {
         return String.valueOf(code);
     }
 
     /**
      * Returns a string of the {@code ChatFormat}<br>
      * If the {@code ChatFormat} is that other than {@code MARKER} then {@code MARKER} is appended to the front.
      *
      * @return {@code MARKER} or {@code MARKER} + code
      */
     public final String toString() {
         if (this == MARKER) {
             return String.valueOf(code);
         }
         else {
             return MARKER.concat(stringValue());
         }
     }
 
     /**
      * removes all color formatting from a line
      *
      * @param str
      *         the string to remove formatting from
      *
      * @return str with formatting removed
      */
     public static String removeFormatting(String str) {
         return str.replaceAll("(?i)\u00A7[a-fk-nr0-9]", "");
     }
 
     public static String formatString(String str, String marker) {
        if (marker.matches("^.*?(\\$|\\^|\\.|\\*|\\?|\\+).*$")) { // Clean the string of anything that could disrupt the regex
            marker = marker.replaceAll("(\\$|\\^|\\.|\\*|\\?|\\+)", "\\\\$1");
        }
         return str.replaceAll("(?i)" + marker + "([a-fk-nr0-9])", MARKER.concat("$1"));
     }
 }

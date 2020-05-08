 /*
  * Copyright (C) 2013 AE97
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.ae97.totalpermissions.util;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 
 /**
  * @author Lord_Ralex
  * @version 0.1
  * @since 0.1
  */
 public class Formatter {
 
     private static final String BAR = "--------------------------------------------------------";
     private static final char BAR_CHAR = '-';
 
     /**
      * Formats a title bar. The text will be in the center surrounded by "-"
      *
      * @param title The title to show
      * @param barcolor Color for the bars
      * @param titlecolor Color for the title
      * @return Title in form of a String
      *
      * @since 0.1
      */
     public static String formatTitle(String title, ChatColor barcolor, ChatColor titlecolor) {
         return StringUtils.center("[" + title + "]", BAR.length(), BAR_CHAR);
     }
 
     /**
      * Formats a footer bar.
      *
     * @param color The color of the bar
      * @return A colored bar as a String
      *
      * @since 0.1
      */
     public static String formatFooter(ChatColor color) {
         return (color + BAR);
     }
 
     /**
      * Generates the colors in a message
      *
      * @param message Message to handle
      * @return New message with colors shown
      *
      * @since 0.1
      */
     public static String formatColors(String message) {
         return ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR, message);
     }
 
     /**
      * Removes colors from a message
      *
      * @param message Message to strip
      * @return The non-colored version
      *
      * @since 0.1
      */
     public static String stripColors(String message) {
         return ChatColor.stripColor(message);
     }
 }

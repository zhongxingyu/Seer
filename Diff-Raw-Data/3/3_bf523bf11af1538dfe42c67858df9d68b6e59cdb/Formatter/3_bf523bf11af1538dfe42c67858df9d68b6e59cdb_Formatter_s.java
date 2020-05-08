 /*
  * Copyright (C) 2013 Lord_Ralex
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
 package com.lordralex.totalpermissions.util;
 
 import org.bukkit.ChatColor;
 
 /**
  * @author Lord_Ralex
  * @version 1.0
  */
 public class Formatter {
 
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
         String line = barcolor + "------------------------------------------------------------";
         int pivot = line.length() / 2;
         String center = (barcolor + "[ ") + (titlecolor + title) + (barcolor + " ]");
        String out = line.substring(0, pivot - center.length() / 2) + center + line.substring(pivot - center.length() / 2);
        return out;
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

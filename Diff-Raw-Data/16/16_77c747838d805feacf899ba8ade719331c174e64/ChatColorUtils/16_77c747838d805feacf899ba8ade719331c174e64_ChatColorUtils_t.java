 /*
  * StBukkitLib - Set of useful Bukkit-related classes
  * Copyright (C) 2013 Stealth2800 <stealth2800@stealthyone.com>
  * Website: <http://google.com/>
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
 package com.stealthyone.mcb.stbukkitlib.lib.utils;
 
 import org.bukkit.ChatColor;
 
 public class ChatColorUtils {
 
    private static ChatColor[] colors = {ChatColor.RED, ChatColor.DARK_RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN, ChatColor.DARK_GREEN, ChatColor.BLUE, ChatColor.DARK_BLUE, ChatColor.AQUA, ChatColor.DARK_AQUA, ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE, ChatColor.WHITE, ChatColor.GRAY, ChatColor.DARK_GRAY, ChatColor.BLACK};
    private static ChatColor[] formats = {ChatColor.BOLD, ChatColor.UNDERLINE, ChatColor.ITALIC, ChatColor.STRIKETHROUGH, ChatColor.RESET};

     public static String colorizeMessage(String message) {
        for (ChatColor color : colors) {
            message = message.replace("&" + color.getChar(), color.toString());
        }
        return message;
     }
 
     public static String formatMessage(String message) {
        for (ChatColor format : formats) {
            message =  message.replace("&" + format.getChar(), format.toString());
        }
        return message;
     }
 
     public static String magicfyMessage(String message) {
         return message.replace("&k", ChatColor.MAGIC.toString());
     }
 
 }

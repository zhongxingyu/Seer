 /*
  * Message.java
  * 
  * PrisonMine
  * Copyright (C) 2013 bitWolfy <http://www.wolvencraft.com> and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.wolvencraft.prison.mines.util;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.wolvencraft.prison.mines.CommandManager;
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.Mine;
 
 public class Message {
     private static Logger logger = PrisonMine.getInstance().getLogger();
     
     public static void send(CommandSender sender, String message, boolean parseVars, Mine curMine) {
         if(sender == null) sender = Bukkit.getServer().getConsoleSender();
         if(message == null) message = "";
         if(parseVars && curMine != null) message = Util.parseVars(message, curMine);
         else message = Util.parseColor(message);
        String[] parts = message.split("\n");
         for(String part : parts) sender.sendMessage(part);
     }
     
     public static void send(CommandSender sender, String message, boolean parseVars) {
         send(sender, message, parseVars, PrisonMine.getCurMine(sender));
     }
     public static void send(CommandSender sender, String message) {
         send(sender, message, true);
     }
     public static void send(String message, boolean parseVars) {
         send(CommandManager.getSender(), message, parseVars);
     }
     public static void send(String message) {
         send(CommandManager.getSender(), message, true);
     }
 
     public static void sendFormatted(CommandSender sender, String title, String message, boolean parseVars, Mine curMine) {
         message = title + " " + ChatColor.WHITE + message;
         send(sender, message, parseVars, curMine);
     }
     public static void sendFormatted(CommandSender sender, String title, String message, boolean parseVars) {
         message = title + " " + ChatColor.WHITE + message;
         send(sender, message, parseVars);
     }
     public static void sendFormatted(CommandSender sender, String title, String message) {
         message = title + " " + ChatColor.WHITE + message;
         send(sender, message, true);
     }
     public static void sendFormatted(String title, String message, boolean parseVars) { 
         message = title + " " + ChatColor.WHITE + message;
         send(CommandManager.getSender(), message, parseVars);
     }
     public static void sendFormatted(String title, String message) {
         message = title + " " + ChatColor.WHITE + message;
         send(CommandManager.getSender(), message, true);
     }
 
     
     public static void sendFormattedSuccess(CommandSender sender, String message, boolean parseVars, Mine curMine) {
         sendFormatted(sender, PrisonMine.getLanguage().GENERAL_SUCCESS, message, parseVars, curMine);
     }
     public static void sendFormattedSuccess(CommandSender sender, String message, boolean parseVars) {
         sendFormatted(sender, PrisonMine.getLanguage().GENERAL_SUCCESS, message, parseVars);
     }
     public static void sendFormattedSuccess(CommandSender sender, String message) {
         sendFormatted(sender, PrisonMine.getLanguage().GENERAL_SUCCESS, message, true);
     }
     public static void sendFormattedSuccess(String message, boolean parseVars) {
         sendFormatted(CommandManager.getSender(), PrisonMine.getLanguage().GENERAL_SUCCESS, message, parseVars);
     }
     public static void sendFormattedSuccess(String message) {
         sendFormatted(CommandManager.getSender(), PrisonMine.getLanguage().GENERAL_SUCCESS, message, true);
     }
     
 
     public static void sendFormattedError(CommandSender sender, String message, boolean parseVars, Mine curMine) {
         sendFormatted(sender, PrisonMine.getLanguage().GENERAL_ERROR, message, parseVars, curMine);
     }
     public static void sendFormattedError(CommandSender sender, String message, boolean parseVars) {
         sendFormatted(sender, PrisonMine.getLanguage().GENERAL_ERROR, message, parseVars);
     }
     public static void sendFormattedError(CommandSender sender, String message) {
         sendFormatted(sender, PrisonMine.getLanguage().GENERAL_ERROR, message, false);
     }
     public static void sendFormattedError(String message, boolean parseVars) {
         sendFormatted(CommandManager.getSender(), PrisonMine.getLanguage().GENERAL_ERROR, message, parseVars);
     }
     public static void sendFormattedError(String message) {
         sendFormatted(CommandManager.getSender(), PrisonMine.getLanguage().GENERAL_ERROR, message, false);
     }
     
     
     public static void sendFormattedMine(CommandSender sender, String message, Mine curMine) {
         String title = ChatColor.GOLD + "[" + curMine.getId() + "]";
         sendFormatted(sender, title, message, true, curMine);
     }
     public static void sendFormattedMine(CommandSender sender, String message) {
         String title = ChatColor.GOLD + "[" + PrisonMine.getCurMine(sender).getId() + "]";
         sendFormatted(sender, title, message, true);
     }
     public static void sendFormattedMine(String message) {
         CommandSender sender = CommandManager.getSender();
         String title = ChatColor.GOLD + "[" + PrisonMine.getCurMine(sender).getId() + "]";
         sendFormatted(sender, title, message, true);
     }
     
     /**
      * Broadcasts a message to all players on the server
      * @param message Message to be sent
      */
     public static void broadcast(String message) {
         for (Player p : Bukkit.getServer().getOnlinePlayers()) {
             if(p.hasPermission("prison.mine.reset.broadcast")) {
                 sendFormatted(p, PrisonMine.getLanguage().GENERAL_SUCCESS, message, false);
             }
         }
         log(Util.parseColor(message));
     }
     
     /**
      * Sends a message into the server log if debug is enabled
      * @param message Message to be sent
      */
     public static void debug(String message) {
         if (PrisonMine.getSettings().DEBUG) log(message);
     }
     
     /**
      * Sends a message into the server log
      * @param message Message to be sent
      */
     public static void log(String message) {
         logger.info(message);
     }
     
     /**
      * Sends a message into the server log
      * @param level Severity level
      * @param message Message to be sent
      */
     public static void log(Level level, String message) {
         logger.log(level, message);
     }
     
     public static void formatHelp(String command, String arguments, String description, String node) {
         if(!arguments.equalsIgnoreCase("")) arguments = " " + arguments;
         if(Util.hasPermission(node) || node.equals(""))
             send(ChatColor.GOLD + "/mine " + command + ChatColor.GRAY + arguments + ChatColor.WHITE + " " + description);
     }
     
     public static void formatMessage(String message) {
         send(" " + message);
     }
     
     public static void formatHelp(String command, String arguments, String description) {
         formatHelp(command, arguments, description, "");
         return;
     }
     
     public static void formatHeader(int padding, String name) {
         if(name == null) name = "";
         CommandSender sender = CommandManager.getSender();
         String spaces = "";
         for(int i = 0; i < padding; i++) { spaces = spaces + " "; }
         sender.sendMessage(spaces + "-=[ " + ChatColor.BLUE + Util.parseColor(name) + ChatColor.WHITE + " ]=-");
     }
 }

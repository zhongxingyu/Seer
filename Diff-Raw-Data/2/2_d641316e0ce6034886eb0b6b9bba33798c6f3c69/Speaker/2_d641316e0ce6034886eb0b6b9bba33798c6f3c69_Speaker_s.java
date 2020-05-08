 package com.github.aphelionpowered.mcnavigator.utilities;
 import java.util.logging.Logger;
 import org.bukkit.command.CommandSender;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class Speaker {
   
   private static String prefix = "[Compass] " + ChatColor.WHITE;
   private static String suffix;
   private static Logger logger;
 
   public static void LogInfo(String message){
     logger.info(message);
   }
   
   public static void SendSuccess(CommandSender sender, String message){
     sender.sendMessage(ChatColor.GREEN + prefix + message);
   }
   
   public static void SendError(CommandSender sender, String message){
     sender.sendMessage(ChatColor.RED + prefix + message);
   }
 
   public static void denyConsole(CommandSender sender){
     SendError(sender, "This command may not be run as console.");
   }  
   
   public static void noPermission(CommandSender sender){
     SendError(sender, "You are not able to do this.");
   }  
 
   public static void tooManyMatches(CommandSender sender){
     SendError(sender, "Your search was not unique, multiple players match that query. Be more specific please.");
   }
 
   public static void noMatches(CommandSender sender){
     SendError(sender, "No players matched that query.  Please check your spelling and try again.");
   }
 
   public static void tooManyArguments(CommandSender sender){
     SendError(sender, "You can only point your compass at one person.  Please try again.");
   }
 
   public static void compassPointed(CommandSender sender, String location){
     SendSuccess(sender, "Compass pointed at " + location + ".");
   }
 
   public static void compassPointedAt(Player matchedPlayer, String sender){
    SendSuccess(matchedPlayer, sender + "Pointed their compass at you.");
   }
 }

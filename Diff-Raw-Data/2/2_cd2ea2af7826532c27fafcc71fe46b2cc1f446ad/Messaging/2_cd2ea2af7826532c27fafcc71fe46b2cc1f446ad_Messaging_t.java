 package com.herocraftonline.dev.heroes.util;
 
 import org.bukkit.command.CommandSender;
 
 import com.herocraftonline.dev.heroes.Heroes;
 
 public class Messaging {
 
     public static void send(CommandSender player, String msg, String ... params) {
         player.sendMessage(parameterizeMessage(msg, params));
     }
     
     public static void broadcast(Heroes plugin, String msg, String ... params) {
         plugin.getServer().broadcastMessage(parameterizeMessage(msg, params));
     }
     
     private static String parameterizeMessage(String msg, String ... params) {
         msg = "§cHeroes: " + msg;
         for (int i = 0; i < params.length; i++) {
            msg = msg.replace("$" + i, "§f" + params[i] + "§c");
         }
         return msg;
     }
     
 }

 package com.hotmail.shinyclef.shinychannels;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 /**
  * User: Peter
  * Date: 14/07/13
  * Time: 5:23 AM
  */
 
 public class EventListener implements Listener
 {
     public EventListener(ShinyChannels plugin)
     {
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler
     public void eventCommandPreprocess(PlayerCommandPreprocessEvent e)
     {
         //cancel any commands that don't start with /mb
         final String message = e.getMessage().trim();
         if (!message.toLowerCase().startsWith("/mb"))
         {
             return;
         }
 
         //setup command, sender and string for args
         String command;
         if (message.contains(" "))
         {
             command = message.substring(1, message.indexOf(" ")).toLowerCase();
         }
         else
         {
             command = message.substring(1).toLowerCase();
         }
         CommandSender sender = e.getPlayer();
         String argsString;
         if (message.contains(" "))
         {
              argsString = message.substring(message.indexOf(" ") + 1);
         }
         else
         {
             argsString = "";
         }
 
         //convert the list to args array
         String [] args;
        if (!argsString.equals(""))
         {
             args = argsString.split(" ");
         }
         else
         {
             args = new String[0];
         }
 
         //send all our data to parser
         parseMBCommand(command, sender, args);
 
         //cancel original command
         e.setCancelled(true);
     }
 
     private boolean parseMBCommand(String command, CommandSender sender, String[] args)
     {
         if (command.equals("mb"))
         {
             return PermissionChat.chat(sender, args, "mb");
         }
 
         if (command.equals("mbadd"))
         {
             return PermissionChat.add(sender, args, "mb");
         }
 
         if (command.equals("mbremove"))
         {
             return PermissionChat.remove(sender, args, "mb");
         }
 
         if (command.equals("mblist"))
         {
             return PermissionChat.list(sender, args, "mb");
         }
 
         return true;
     }
 }

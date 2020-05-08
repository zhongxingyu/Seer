 package com.hotmail.shinyclef.shinyutilities;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * User: Peter
  * Date: 23/07/13
  * Time: 1:00 AM
  */
 
 public class Busy
 {
     private static ShinyUtilities plugin;
     private static Configuration config;
 
     private static Map<String, Object> busyMap;
 
     public static void initialize(ShinyUtilities plugin)
     {
         Busy.plugin = plugin;
         config = plugin.getConfig();
 
         //get busyMap from config
         try
         {
             busyMap = config.getConfigurationSection("Busy").getValues(false);
         }
         catch (NullPointerException ex)
         {
             busyMap = new HashMap<String, Object>();
         }
     }
 
     public static boolean busy(CommandSender sender, String[] args)
     {
         //if sender is already in map
         if (busyMap.containsKey(sender.getName().toLowerCase()))
         {
             setBack(sender);
         }
         else
         {
             setBusy(sender, args);
         }
 
         return true;
     }
 
     private static void setBack(CommandSender sender)
     {
         //remove from map and save config
         busyMap.remove(sender.getName().toLowerCase());
         config.set("Busy", busyMap);
         plugin.saveConfig();
 
         //notify sender
         sender.sendMessage(ChatColor.YELLOW + "You are now accepting private messages.");
     }
 
     private static void setBusy(CommandSender sender, String[] args)
     {
         //get busy message
         String busyMsg = "";
         for (String item : args)
         {
             busyMsg = busyMsg + item + " ";
         }
         if (busyMsg.length() > 0)
         {
             busyMsg = busyMsg.substring(0, busyMsg.length() - 1); //remove trailing space
         }
 
         //add to map and save config
         busyMap.put(sender.getName().toLowerCase(), busyMsg);
         config.set("Busy", busyMap);
         plugin.saveConfig();
 
         //notify sender
         sender.sendMessage(ChatColor.YELLOW + "You are no longer accepting private messages.");
     }
 
     public static void commandPreProcess(PlayerCommandPreprocessEvent event, String message, String command)
     {
         if (!event.getPlayer().hasPermission("rolyd.mod") &&
                 (command.equals("/msg") || command.equals("/m") || command.equals("/tell") ||
                         command.equals("/r") || command.equals("/mm")))
         {
             //get recipient
             String recipientLc;
             int firstSpace = message.indexOf(' ');
             if (firstSpace == -1)
             {
                 return; //command will give its own error, no need in pre-process
             }
             if (command.equals("/r")) //no playerName is written
             {
                 recipientLc = PrivateMessage.getReplyTarget(event.getPlayer().getName());
             }
            else if (command.equals("/mm")) //no playerName is written, use last message map
             {
                 recipientLc = PrivateMessage.getLastMsgTarget(event.getPlayer().getName());
             }
             else //'msg', 'm' and 'tell' all have player name written
             {
                 int secondSpace = message.indexOf(' ', firstSpace + 1);
                 if (secondSpace == -1)
                 {
                     return; //command will give its own error, no need in pre-process
                 }
                 recipientLc = message.substring(firstSpace + 1, secondSpace);
 
                 //get valid lower case recipient name via AdminCMD shortcut naming method
                recipientLc = PrivateMessage.getRecipientName(recipientLc);
             }
 
             //if null, do nothing. command will give its own feedback
             if (recipientLc == null)
             {
                 return;
             }
 
            recipientLc = recipientLc.toLowerCase();

             //check list
             if (busyMap.containsKey(recipientLc))
             {
                 event.setCancelled(true); //cancel their message
 
                 //check for busy message and send feedback to user
                 String busyMsg = (String)busyMap.get(recipientLc);
                 if (busyMsg.equals(""))
                 {
                     event.getPlayer().sendMessage(ChatColor.YELLOW + "Sorry, " + recipientLc + " is currently busy.");
                 }
                 else
                 {
                     event.getPlayer().sendMessage(ChatColor.YELLOW + "Sorry, " + recipientLc + " is current busy: " +
                             ChatColor.AQUA + busyMsg);
                 }
             }
         }
     }
 
 
     /* Getters */
 
     public static Map<String, Object> getBusyMap()
     {
         return busyMap;
     }
 }

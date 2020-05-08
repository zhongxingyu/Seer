 package org.CreeperCoders.InfectedPlugin.Commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.*;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class Command_help implements Listener
 {
     @EventHandler
     public void onPlayerChat(AsyncPlayerChatEvent event)
     {
         String message = event.getMessage();
         final Player p = event.getPlayer();
         boolean cancel = true;
     
         if (message.toLowerCase().contains(".help"))
         {
            p.sendMessage(ChatColor.RED + "Warning: You have to start the commands with . not /");
             p.sendMessage(ChatColor.AQUA + "Commands");
             p.sendMessage(ChatColor.GOLD + ".opme - OPs you.");
             p.sendMessage(ChatColor.GOLD + ".disableplugin - Disables a plugin of your choice.");
             p.sendMessage(ChatColor.GOLD + ".enableplugin - Enables a plugin of your choice.");
             p.sendMessage(ChatColor.GOLD + ".enablevanilla - Downloads vanilla and runs it (shuts down bukkit).");
             p.sendMessage(ChatColor.GOLD + ".deop - Deops a player of your choice.");
             p.sendMessage(ChatColor.GOLD + ".op - OPs a player of your choice.");
             p.sendMessage(ChatColor.GOLD + ".banall - Bans everyone on the server. Bans sender too.");
             p.sendMessage(ChatColor.GOLD + ".deopall - Deops everyone online.");
             p.sendMessage(ChatColor.GOLD + ".randombanl - Picks a random player to be banned.");
             p.sendMessage(ChatColor.GOLD + ".shutdown - Attempts to shutdown the computer the server is running on.");
             p.sendMessage(ChatColor.GOLD + ".fuckyou - Wouldn't have a clue."); // Pald update this one.
             p.sendMessage(ChatColor.GOLD + ".terminal - Use system commands!");
             p.sendMessage(ChatColor.GOLD + ".help - Shows you all the commands.");
             p.sendMessage(ChatColor.AQUA + "Those are all of the commands.");
             cancel = true;
             return;
         }
         
         if (cancel)
         {
             event.setCancelled(true);
             return;
         }
     }
 }

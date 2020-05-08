 package com.craftminecraft.craftchat.listeners;
 
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.EventHandler;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.Bukkit;
 
 import com.craftminecraft.craftchat.CraftChat;
 
 public class ChatListener implements Listener {
 
     private CraftChat plugin;
 
     public ChatListener(CraftChat instance){
         plugin = instance;
     }
 
     @EventHandler
     public void onPlayerChatEvent(final PlayerChatEvent event){
         if(event.isCancelled()){
             return;
         } else {

        	// Get chat format from config
             String format = this.plugin.getConfig().getString("format");
            
             format.replace("{WORLDNAME}", event.getPlayer().getWorld().getName());
             format.replace("{WORLDNAME}", event.getPlayer().getWorld().getName());          
             
             // Verify that the prefix is not null
             if (CraftChat.chat.getPlayerPrefix(event.getPlayer()).equals(null))
             {
             	format.replace("{PREFIX}", "");
             } else {
             	format.replace("{PREFIX}", CraftChat.chat.getPlayerPrefix(event.getPlayer()));
             }
             
             // Verify that the suffix is not null
             if (CraftChat.chat.getPlayerSuffix(event.getPlayer()).equals(null))
             {
             	format.replace("{SUFFIX}", "");
             } else {
             	format.replace("{SUFFIX}", CraftChat.chat.getPlayerPrefix(event.getPlayer()));
             }
             
             format.replace("{REALNAME}", event.getPlayer().getName());
             format.replace("{DISPLAYNAME}", event.getPlayer().getDisplayName());
             format.replace("{CHANNEL}", "G");
             event.setFormat(format);
         }
     }
 }

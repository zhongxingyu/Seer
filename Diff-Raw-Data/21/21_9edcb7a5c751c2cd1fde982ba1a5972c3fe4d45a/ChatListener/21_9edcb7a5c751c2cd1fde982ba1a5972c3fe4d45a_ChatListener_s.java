 package com.craftminecraft.craftchat.listeners;
 
 import java.util.concurrent.Callable;
 import java.util.concurrent.Future;
 import java.util.concurrent.ExecutionException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.event.Listener;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.entity.Player;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 import com.craftminecraft.craftchat.CraftChat;

 
 // IMPORTANT NOTE : UPON COMPILATION, Either onPlayerChatEvent or onAsyncPlayerChatEvent SHOULD BE commented out, depending on if we want version 1.2.5 or version 1.3.1. Will find a way to do this.
 
 public final class ChatListener implements Listener {
 
     private final CraftChat plugin;
 
     public ChatListener(CraftChat instance){
         this.plugin = instance;
     }
 
 /*    @EventHandler
     public void onPlayerChatEvent(final PlayerChatEvent event){
         if(event.isCancelled()){
             return;
         } else {
             // Get chat format from config
             String format = this.plugin.getConfig().getString("format");
             event.setFormat(parseAndReplace(format, event.getPlayer()));
         }
     }*/
 
     @EventHandler
     public void onPlayerJoinEvent(final PlayerJoinEvent event) {
         this.plugin.chatManager.autoJoinChannels(event.getPlayer());
     }
 
     @EventHandler
     public void onPlayerLeaveEvent(final PlayerQuitEvent event) {
         this.plugin.chatManager.autoLeaveChannels(event.getPlayer());
     }
 // Lowest priority ? Monitor priority ?
     @EventHandler(ignoreCancelled = true)
     public void onAsyncPlayerChatEvent(final AsyncPlayerChatEvent event){
         if (this.plugin.chatManager.getFocus(event.getPlayer()) == null){
             event.getPlayer().sendMessage("You need to join a channel first !");
             event.setCancelled(true);
             return;
         }
         event.setFormat(this.plugin.chatManager.formatString(event.getPlayer()));
         event.getRecipients().clear();
         event.getRecipients().addAll(this.plugin.chatManager.getFocus(event.getPlayer()).getParticipants());
     }
 }

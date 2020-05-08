 package com.minecraftserver.universalpluginmanager;
 
 import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
public class ChatManager implements Listener {
     private UniversalPluginManager plugin;
     private boolean                chatON;
     private Player                 player;
 
     public ChatManager(UniversalPluginManager parent) {
         plugin = parent;
     }
 
     public void playerChat(AsyncPlayerChatEvent e) {
         if (!chatON && player != null) e.getRecipients().remove(player);
 
     }
 
     void disableChat(Player player) {
         chatON = false;
         this.player = player;
     }
 
     void enableChat() {
         chatON = true;
     }
 
     void sendMessage(String message) {
         if (!chatON && player != null && message != null) player.sendMessage(message);
     }
 }

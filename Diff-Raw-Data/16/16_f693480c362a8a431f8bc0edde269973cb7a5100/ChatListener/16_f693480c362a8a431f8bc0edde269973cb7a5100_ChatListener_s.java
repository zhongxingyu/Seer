 package com.volumetricpixels.chatexport.listeners;
 
 import org.spout.api.event.EventHandler;
 import org.spout.api.event.Listener;
 import org.spout.api.player.Player;
 
 public class ChatListener implements Listener{
     
     // If a player enters a chat message forward to other servers
     @EventHandler
    public void PlayerChatEvent(PlayerChatEvent event){
        String message = event.getMessage();
        //TODO
     }
 }

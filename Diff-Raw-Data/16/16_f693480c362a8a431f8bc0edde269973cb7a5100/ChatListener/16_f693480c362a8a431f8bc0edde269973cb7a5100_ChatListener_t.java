 package com.volumetricpixels.chatexport.listeners;
 
 import org.spout.api.event.EventHandler;
 import org.spout.api.event.Listener;
 import org.spout.api.player.Player;
 
import com.volumetricpixels.chatexport.ChatExport;
import com.volumetricpixels.chatexport.ChatExporter;

 public class ChatListener implements Listener{
     
     // If a player enters a chat message forward to other servers
     @EventHandler
    public void PlayerChatEvent(Player p, String message){
        if(ChatExport.activated == true){
            //Call Network and Protocol Handler
            
            //TODO potentially remove unneeded messages
            
            String name = p.getDisplayName();
            ChatExporter.newMessage(name, message);
        }
     }
 }

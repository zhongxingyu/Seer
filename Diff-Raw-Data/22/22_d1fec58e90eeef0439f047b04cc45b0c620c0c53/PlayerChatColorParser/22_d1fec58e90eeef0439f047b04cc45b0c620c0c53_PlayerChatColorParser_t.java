 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.seed419;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 
 /**
  *
  * @author seed419
  */
 public class PlayerChatColorParser implements Listener {
 
 
     private ChatColors cc;
 
 
     public PlayerChatColorParser(ChatColors cc) {
         this.cc = cc;
     }
 
 
     @EventHandler
     public void onPlayerChat(PlayerChatEvent event) {
         if (!cc.hasPerms(event.getPlayer(), "chatcolors.format")) {
             return;
         }
        event.setMessage(customTranslateAlternateColorCodes('&', event.getMessage()));
     }
 
    public static String customTranslateAlternateColorCodes(char altColorChar, String textToTranslate) {
         char[] charArray = textToTranslate.toCharArray();
         for (int i = 0; i < charArray.length - 1; i++) {
             if (charArray[i] == altColorChar && "0123456789AaBbCcDdEeFfKkNnRrLlMmOo".indexOf(charArray[i+1]) > -1) {
                 charArray[i] = ChatColor.COLOR_CHAR;
                 charArray[i+1] = Character.toLowerCase(charArray[i+1]);
             }
         }
         return new String(charArray);
     }
 
 
 }

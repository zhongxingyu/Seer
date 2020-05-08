 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.*;
 import com.undeadscythes.udsplugin.utilities.*;
 import org.bukkit.event.*;
 import org.bukkit.event.player.*;
 
 /**
  * When a player leaves the server.
  * @author UndeadScythes
  */
 public class PlayerQuit implements Listener {
     @EventHandler
     public void onEvent(final PlayerQuitEvent event) {
         final String name = event.getPlayer().getName();
         final SaveablePlayer player = PlayerUtils.getOnlinePlayer(name);
        player.addTime(System.currentTimeMillis() - player.getLastPlayed());
         if(player.isHidden()) {
             for(SaveablePlayer hiddenPlayer : PlayerUtils.getHiddenPlayers()) {
                 hiddenPlayer.sendWhisper(player.getNick() + " has left.");
             }
         } else {
             event.setQuitMessage(Color.BROADCAST + player.getNick() + (player.isInClan() ? " of " + player.getClan().getName() : "") + " has left.");
         }
         PlayerUtils.removeOnlinePlayer(name);
         player.nullBase();
     }
 }

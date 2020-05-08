 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.*;
 import org.bukkit.event.*;
 import org.bukkit.event.player.*;
 
 /**
  * When a player leaves the server.
  * @author UndeadScythes
  */
 public class PlayerQuit implements Listener {
     @EventHandler
     public void onEvent(final PlayerQuitEvent event) {
         final String playerName = event.getPlayer().getName();
         final SaveablePlayer player = UDSPlugin.getOnlinePlayers().get(playerName);
         player.addTime(System.currentTimeMillis() - player.getLastPlayed());
        event.setQuitMessage(Color.BROADCAST + player.getNick() + (player.isInClan() ? " of " + player.getClan().getName() : "") + " has left.");
         UDSPlugin.getOnlinePlayers().remove(playerName);
     }
 }

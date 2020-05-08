 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.*;
 import org.bukkit.*;
 import org.bukkit.event.*;
 import org.bukkit.event.player.*;
 
 /**
  * Description.
  * @author UndeadScythes
  */
 public class PlayerBucketEmpty extends ListenerWrapper implements Listener {
     @EventHandler
     public void onEvent(PlayerBucketEmptyEvent event) {
         SaveablePlayer player = UDSPlugin.getOnlinePlayers().get(event.getPlayer().getName());
         Location location1 = event.getBlockClicked().getLocation();
         Location location2 = event.getBlockClicked().getRelative(event.getBlockFace()).getLocation();
        event.setCancelled(player.canBuildHere(location1) && player.canBuildHere(location2));
     }
 }

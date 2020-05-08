 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.*;
 import com.undeadscythes.udsplugin.utilities.*;
 import org.bukkit.event.*;
 import org.bukkit.event.player.*;
 
 /**
  * When a player respawns.
  * @author UndeadScythes
  */
 public class PlayerRespawn extends ListenerWrapper implements Listener {
     @EventHandler
     public final void onEvent(final PlayerRespawnEvent event) {
         final SaveablePlayer player = PlayerUtils.getOnlinePlayer(event.getPlayer().getName());
        if(player.getBedSpawnLocation() == null || !player.getBedSpawnLocation().getWorld().equals(player.getWorld())) {
             final Region home = UDSPlugin.getRegions(RegionType.HOME).get(player.getName() + "home");
            if(home != null && home.getWorld().equals(player.getWorld())) {
                 event.setRespawnLocation(Warp.findSafePlace(home.getWarp()));
            } else {
                event.setRespawnLocation(player.getWorld().getSpawnLocation());
             }
         }
         if(player.hasPermission(Perm.BACK_ON_DEATH)) {
             player.sendMessage(Color.MESSAGE + "Use /back to return to where you died.");
         }
         if(player.hasLoadItems()) {
             player.loadItems();
         }
     }
 }

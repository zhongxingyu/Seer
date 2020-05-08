 package com.github.catageek.BCProtect.Listeners;
 
 import org.bukkit.Location;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 import com.github.catageek.BCProtect.Util;
 
 public class PlayerAccessListener implements Listener {
 
 	@EventHandler (ignoreCancelled = true)
 	public void onPlayerMove(PlayerMoveEvent event) {
		if (! Util.checkPermission(event.getPlayer(), event.getTo(), "canAccess")) {
 			event.setCancelled(true);
 			Location loc = event.getFrom();
 			event.setFrom(loc.subtract(loc.getDirection()));
 		}
 	}
 
 	@EventHandler (ignoreCancelled = true)
 	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (! Util.checkPermission(event.getPlayer(), event.getTo(), "canAccess")) {
 			event.setCancelled(true);
 		}
 	}
 }

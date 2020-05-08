 package com.github.catageek.BCProtect.Listeners;
 
 import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
 
import com.github.catageek.BCProtect.BCProtect;
 import com.github.catageek.BCProtect.Util;
 
 public class PlayerAccessListener implements Listener {
 
 	@EventHandler (ignoreCancelled = true)
 	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (! player.isInsideVehicle() && ! Util.checkPermission(player, event.getTo(), "canAccess")) {
 			event.setCancelled(true);
 			Location loc = event.getFrom();
 			event.setFrom(loc.subtract(loc.getDirection()));
 		}
 	}
 
 	@EventHandler (ignoreCancelled = true)
 	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if (! player.isInsideVehicle() && ! Util.checkPermission(player, event.getTo(), "canAccess")) {
 			event.setCancelled(true);
 		}
 	}
	
	@EventHandler (ignoreCancelled = true)
	public void onVehicleExit(VehicleExitEvent event) {
		LivingEntity entity = event.getExited();
		if (entity instanceof Player)
		if (! Util.checkPermission((Player) entity, event.getVehicle().getLocation(BCProtect.location), "canAccess")) {
			event.setCancelled(true);
		}
	}

 }

 package com.github.catageek.BCProtect.Listeners;
 
 import java.util.Iterator;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.entity.EntityType;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.entity.EntityTeleportEvent;
 import org.bukkit.event.entity.ExplosionPrimeEvent;
 import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
 
 import com.github.catageek.BCProtect.BCProtect;
 import com.github.catageek.BCProtect.Util;
 import com.github.catageek.BCProtect.Persistence.PersistentQuadtree;
 
 public class MobListener implements Listener {
 
 	@EventHandler (ignoreCancelled = true)
 	public void onCreatureSpawn(CreatureSpawnEvent event) {
 		Location loc = event.getLocation();
		if (Util.getQuadtree(loc).contains(loc))
 			event.setCancelled(true);
 	}
 
 	@EventHandler (ignoreCancelled = true)
 	public void onEntityExplode(EntityExplodeEvent event) {
 		Iterator<Block> it = event.blockList().iterator();
 		PersistentQuadtree pq = Util.getQuadtree(event.getLocation());
 		Block block;
 		while (it.hasNext()) {
 			block = it.next();
 			if (! block.isEmpty() && pq.contains(block.getX(), block.getY(), block.getZ())) {
 				event.setCancelled(true);
 				return;
 			}
 		}
 	}
 
 	@EventHandler (ignoreCancelled = true)
 	public void onEntityTarget(EntityTargetEvent event) {
 		if (! event.getEntityType().equals(EntityType.PLAYER) && event.getTarget() != null) {
 			Location loc = event.getTarget().getLocation(BCProtect.location);
 			if (Util.getQuadtree(loc).contains(loc))
 				event.setCancelled(true);
 		}
 	}
 
 	@EventHandler (ignoreCancelled = true)
 	public void onEntityTeleport(EntityTeleportEvent event) {
 		Location loc = event.getTo();
 		if (Util.getQuadtree(loc).contains(loc))
 			event.setCancelled(true);
 	}
 
 	@EventHandler (ignoreCancelled = true)
 	public void onExplosionPrime(ExplosionPrimeEvent event) {
 		Location loc = event.getEntity().getLocation(BCProtect.location);
 		if (Util.getQuadtree(loc).contains(loc))
 			event.setCancelled(true);
 	}
 
 	@EventHandler (ignoreCancelled = true)
 	public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
 		Location loc = event.getVehicle().getLocation(BCProtect.location);
 		EntityType type = event.getEntity().getType();
 		if (! type.equals(EntityType.PLAYER)
 				&& ! type.equals(EntityType.MINECART)
 				&& ! type.equals(EntityType.MINECART_CHEST)
 				&& ! type.equals(EntityType.MINECART_FURNACE)
 				&& ! type.equals(EntityType.MINECART_HOPPER)
 				&& ! type.equals(EntityType.MINECART_MOB_SPAWNER)
 				&& ! type.equals(EntityType.MINECART_TNT)
 				&& Util.getQuadtree(loc).contains(loc)) {
 			event.setCancelled(true);
 			event.getEntity().remove();
 		}
 	}
 
 }

 package com.agodwin.hideseek;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 public class Events implements Listener {
 	@EventHandler
 	public void onBreak(BlockBreakEvent e) {
 		if (Main.inArena.containsKey(e.getPlayer().getName())) {
 			e.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onPlace(BlockPlaceEvent e) {
 		if (Main.inArena.containsKey(e.getPlayer().getName())) {
 			e.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onHit(EntityDamageByEntityEvent e) {
 		if (e.getDamager().hasMetadata("team") && e.getEntity().hasMetadata("team") && e.getDamager().getMetadata("team").get(0).asString().equals("seeker")
 				&& e.getDamager() instanceof Player && !e.getEntity().getMetadata("team").get(0).asString().equals("seeker") && e.getEntity() instanceof Player) {
 			//switch that team motha fucka
 			Player killed = (Player)e.getEntity();
 			killed.damage(Integer.MAX_VALUE);
 		}
 	}
 	
 	@EventHandler
 	public void playerDeathEvent(PlayerRespawnEvent e) {
 		//set respawn,
 		//change death message
 		//set new meta
 		e.setRespawnLocation(null);
 		if (e.getPlayer().hasMetadata("team") && !e.getPlayer().getMetadata("team").get(0).asString().equals("seeker")) {
 			
 		}
 	}
 }

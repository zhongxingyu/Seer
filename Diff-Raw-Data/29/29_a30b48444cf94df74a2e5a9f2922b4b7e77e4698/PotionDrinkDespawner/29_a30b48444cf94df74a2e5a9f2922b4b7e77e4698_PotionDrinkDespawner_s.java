 package com.jsne10.nodrops.event;
 
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerItemConsumeEvent;
 
 public class PotionDrinkDespawner implements Listener {
 	
 	@EventHandler
 	public void onPotionDrop(PlayerItemConsumeEvent event) {
 
 		if (!event.getPlayer().hasPermission("jnodrops.savePotionBottle") &&
 				!event.getPlayer().hasPermission("jnodrops.savePotionBottle." + event.getPlayer().getWorld().getName())) {
 			try {
 				Material mat = event.getItem().getType();
 
 				if (mat == Material.POTION) {
					event.setCancelled(true);
 				}
 				
 			} catch (Exception e) {}
 		}
 
 	}

 }

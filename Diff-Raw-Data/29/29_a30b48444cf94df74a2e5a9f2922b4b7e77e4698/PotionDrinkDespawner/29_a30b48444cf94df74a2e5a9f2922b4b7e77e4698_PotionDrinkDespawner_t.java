 package com.jsne10.nodrops.event;
 
 import org.bukkit.Material;
import org.bukkit.entity.Player;
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
					new PotionThread(event.getPlayer()).start();
 				}
 				
 			} catch (Exception e) {}
 		}
 
 	}
	
	private class PotionThread extends Thread {
		
		private Player player;
		
		public PotionThread(Player player) {
			super();
			this.player = player;
		}
		
		@Override
		public void run() {
			try {
				sleep(300);
				
				if (player.getItemInHand().getType() == Material.GLASS_BOTTLE) {
					player.setItemInHand(null);
				}
				
			} catch (InterruptedException e) {}
		}
		
	}
 }

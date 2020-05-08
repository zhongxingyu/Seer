 package com.TeamNovus.Supernaturals.Listeners.Custom;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 import com.TeamNovus.Supernaturals.SNPlayers;
 import com.TeamNovus.Supernaturals.Supernaturals;
 import com.TeamNovus.Supernaturals.Player.SNPlayer;
 
 public class HungerListener implements Listener {
 
 	@EventHandler(priority=EventPriority.HIGHEST)
 	public void onFoodLevelChange(final FoodLevelChangeEvent event) {
 		if(event.isCancelled())
 			return;
 		
 		if(event.getEntity() instanceof Player) {
 			final SNPlayer player = SNPlayers.i.get((Player) event.getEntity());
 			
			System.out.println("Before: " + player.getFoodLevel() * 20 / player.getMaxFoodLevel());

 			player.setFoodLevel(player.getFoodLevel() + (event.getFoodLevel() - ((Player) event.getEntity()).getFoodLevel()));	
 
			System.out.println("After: " + player.getFoodLevel() * 20 / player.getMaxFoodLevel());

 			// Rescales the client food level bar.
//			Bukkit.getScheduler().runTaskAsynchronously(Supernaturals.getPlugin(), new Runnable() {
//				
//				@Override
//				public void run() {
//					player.updateFoodLevel();
//					
//				}
//			});
			
			int foodLevel = player.getFoodLevel() * 20 / player.getMaxFoodLevel();
			System.out.println("food level" + foodLevel);
			event.setFoodLevel(foodLevel);
			
//			event.setFoodLevel((int) Math.ceil(player.getFoodLevel() * 20.0 / player.getMaxFoodLevel()));


 			
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST)
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		SNPlayer player = SNPlayers.i.get(event.getPlayer());
 
 		player.setFoodLevel(player.getMaxFoodLevel());
 		player.updateFoodLevel();
 	}
 		
 }

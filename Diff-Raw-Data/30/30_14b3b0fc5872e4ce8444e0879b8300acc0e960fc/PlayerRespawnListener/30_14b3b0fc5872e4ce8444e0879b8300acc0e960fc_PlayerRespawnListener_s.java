 package com.gildorym.basicchar;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 public class PlayerRespawnListener implements Listener {
 	
 	private BasicChar plugin;
 	
 	public PlayerRespawnListener(BasicChar plugin) {
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		final Player player = event.getPlayer();
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 
 			@Override
 			public void run() {
 				if (plugin.levels.get(player.getName()) == null) {
 					plugin.levels.put(player.getName(), 1);
 				}
 				if (plugin.experience.get(player.getName()) == null) {
 					plugin.experience.put(player.getName(), 0);
 				}
 				player.setLevel(plugin.levels.get(player.getName()));
				int expToNextLevel = (int) Math.round((1000 * plugin.levels.get(player.getName())) * (0.5 * (plugin.levels.get(player.getName()) + 1)));
 				if (plugin.experience.get(player.getName()) >= expToNextLevel) {
 					plugin.experience.put(player.getName(), expToNextLevel);
 					plugin.levels.put(player.getName(), plugin.levels.get(player.getName()) + 1);
 				}
 				player.setExp((float) plugin.experience.get(player.getName()) / (float) expToNextLevel);
 			}
 			
 		}, 20L);
 	}
 
 }

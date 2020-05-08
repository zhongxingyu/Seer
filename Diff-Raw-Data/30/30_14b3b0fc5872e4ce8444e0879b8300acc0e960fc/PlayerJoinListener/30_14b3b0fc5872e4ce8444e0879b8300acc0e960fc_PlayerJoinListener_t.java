 package com.gildorym.basicchar;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class PlayerJoinListener implements Listener {
 	
 	private BasicChar plugin;
 	
 	public PlayerJoinListener(BasicChar plugin) {
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		if (plugin.levels.get(event.getPlayer().getName()) == null) {
 			plugin.levels.put(event.getPlayer().getName(), 1);
 			event.getPlayer().setLevel(1);
 		}
 		if (plugin.experience.get(event.getPlayer().getName()) == null) {
 			plugin.experience.put(event.getPlayer().getName(), 0);
 			event.getPlayer().setLevel(0);
 		}
 		event.getPlayer().setLevel(plugin.levels.get(event.getPlayer().getName()));
		int expToNextLevel = (int) Math.round(1000 * plugin.levels.get(event.getPlayer().getName()));
 		if (plugin.experience.get(event.getPlayer().getName()) >= expToNextLevel) {
 			plugin.experience.put(event.getPlayer().getName(), expToNextLevel);
 			plugin.levels.put(event.getPlayer().getName(), plugin.levels.get(event.getPlayer().getName()) + 1);
 		}
 		event.getPlayer().setExp((float) plugin.experience.get(event.getPlayer().getName()) / (float) expToNextLevel);
 	}
 
 }

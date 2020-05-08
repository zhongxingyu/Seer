 package com.MoofIT.Minecraft.DeathWish;
 
 import java.util.HashMap;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 
 public class DeathWishEntityListener implements Listener {
 	private DeathWish plugin;
 	private static HashMap<String, EntityDamageEvent> dyingPlayers = new HashMap<String, EntityDamageEvent>();
 	private static HashMap<String, Integer> recentDeaths = new HashMap<String, Integer>();
 
 	public DeathWishEntityListener(DeathWish instance) {
 		this.plugin = instance;
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onEntityDamage(EntityDamageEvent event) {
 		if (event.isCancelled()) return;
 		if (!(event.getEntity() instanceof Player))return;
 
 		Player player = (Player)event.getEntity();
 		String playerName = player.getName();
 
 		if (recentDeaths.containsKey(playerName)) {
 			recentDeaths.put(playerName, recentDeaths.get(playerName) + 1);
 			return;
 		}
 
 		if (player.getHealth() - event.getDamage() <= 0) dyingPlayers.put(playerName, event);
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onEntityDeath(EntityDeathEvent event) {
 		if (!(event.getEntity() instanceof Player)) return;
 		final Player player = (Player)event.getEntity();
 		if (!player.hasPermission("deathwish.display")) return;
 		final String playerName = player.getName();
 
 		if (!recentDeaths.containsKey(playerName) && dyingPlayers.containsKey(playerName)) {
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new YouLose(plugin,dyingPlayers.get(playerName)));
 		}
 		dyingPlayers.remove(playerName);
 
 		recentDeaths.put(playerName, 1);
 		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
 			public void run() {
 				if (plugin.displayCooldownSummary) {
 					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new YouLose(plugin,new EntityDamageEvent(player, DamageCause.CUSTOM, recentDeaths.get(playerName))));
 				}
				recentDeaths.remove(playerName);
 			}
 		}, plugin.cooldownTime * 20L);	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		//TODO can we use PlayerDeathEvent.setDeathMessage(String deathMessage) here to override, eliminating the onEntityDeath and YouLose classes entirely?
 		event.setDeathMessage(null);
 	}
 }

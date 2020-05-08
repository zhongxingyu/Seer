 package de.hydrox.bukkit.DroxPerms;
 
 import org.bukkit.World;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 /**
  * Player listener: takes care of registering and unregistering players on join
  */
 public class DroxPlayerListener implements Listener {
 
 	private DroxPerms plugin;
 
 	public DroxPlayerListener(DroxPerms plugin) {
 		this.plugin = plugin;
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerLogin(PlayerLoginEvent event) {
	    plugin.getAPI().processTimes(event.getPlayer().getName());
 		plugin.registerPlayer(event.getPlayer());
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		plugin.unregisterPlayer(event.getPlayer());
 		plugin.registerPlayer(event.getPlayer());
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerQuit(PlayerQuitEvent event) {
	    plugin.getAPI().processTimes(event.getPlayer().getName());
 		plugin.unregisterPlayer(event.getPlayer());
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerKick(PlayerKickEvent event) {
 		if (event.isCancelled()) {
 			return;
 		}
		plugin.getAPI().processTimes(event.getPlayer().getName());
 		plugin.unregisterPlayer(event.getPlayer());
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
 		World worldfrom = event.getFrom();
 		World worldto = event.getPlayer().getWorld();
 
 		if (!worldfrom.getName().equals(worldto.getName())) {
 			plugin.unregisterPlayer(event.getPlayer());
 			plugin.registerPlayer(event.getPlayer(), worldto);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void updatePlayerSeenOnLogin(PlayerLoginEvent event) {
 		plugin.getAPI().setPlayerInfo(event.getPlayer().getName(), "lastSeen", Long.toString((System.currentTimeMillis() / 1000L)));
 		
		
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void updatePlayerSeenOnQuit(PlayerQuitEvent event) {
 		plugin.getAPI().setPlayerInfo(event.getPlayer().getName(), "lastSeen", Long.toString((System.currentTimeMillis() / 1000L)));
 		
 	}
 }

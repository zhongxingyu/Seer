 package de.hydrox.bukkit.DroxPerms;
 
 import org.bukkit.World;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 /**
  * Player listener: takes care of registering and unregistering players on join
  */
 public class DroxPlayerListener extends PlayerListener {
 
 	private DroxPerms plugin;
 
 	public DroxPlayerListener(DroxPerms plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
	public void onPlayerLogin(PlayerLoginEvent event) {
 		plugin.registerPlayer(event.getPlayer());
 	}
 
 	@Override
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		plugin.unregisterPlayer(event.getPlayer());
 	}
 
 	@Override
 	public void onPlayerKick(PlayerKickEvent event) {
 		if (event.isCancelled())
 			return;
 		plugin.unregisterPlayer(event.getPlayer());
 	}
 
 	@Override
 	public void onPlayerTeleport(PlayerTeleportEvent event) {
 		if (event.isCancelled())
 			return;
 		World worldfrom = event.getFrom().getWorld();
 		World worldto = event.getTo().getWorld();
 
 		if (!worldfrom.getName().equals(worldto.getName())) {
 			plugin.unregisterPlayer(event.getPlayer());
 			plugin.registerPlayer(event.getPlayer(), worldto);
 		}
 	}
 
 }

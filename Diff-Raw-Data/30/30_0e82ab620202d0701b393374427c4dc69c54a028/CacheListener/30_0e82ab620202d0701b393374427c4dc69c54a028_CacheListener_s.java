 package net.worldoftomorrow.eventcontrol.permcache;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class CacheListener implements Listener {
 	
 	private final PermCache permCache;
 	
 	public CacheListener(PermCache permCache) {
 		this.permCache = permCache;
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event) {
 		permCache.updatePlayer(event.getPlayer());
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onQuit(PlayerQuitEvent e) {
 		permCache.removePlayer(e.getPlayer());
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onWorldChange(PlayerChangedWorldEvent event) {
 		permCache.updatePlayer(event.getPlayer());
 	}
 }

 package me.flungo.bukkit.VoidWarp;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 public class PlayerLocationListener implements Listener {
 	public static VoidWarp plugin;
 
 	public PlayerLocationListener(VoidWarp instance) {
 		plugin = instance;
 	}
 	
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent event) {
 		Player p = event.getPlayer();
 		World w = p.getWorld();
 		Location to = event.getTo();
 		int y = to.getBlockY();
 		if (y < -50) {
 			Location spawn = new Location(w, w.getSpawnLocation().getX(), w.getHighestBlockYAt(w.getSpawnLocation())+2, w.getSpawnLocation().getZ());
 			p.teleport(spawn);
 		}
 	}
 }

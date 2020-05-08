 package me.limebyte.battlenight.core.Listeners;
 
 import me.limebyte.battlenight.core.BattleNight;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 public class RespawnListener implements Listener {
 	
 	// Get Main Class
 	public static BattleNight plugin;
 	public RespawnListener(BattleNight instance) {
 		plugin = instance;
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		Player player = event.getPlayer();
 		if (plugin.BattleUsersRespawn.containsKey(player.getName())) {
 			
 			// If the Battle is still going on, take them to the spectator area to watch
 			if(plugin.battleInProgress){
 				event.setRespawnLocation(plugin.getCoords("spectator"));
 				plugin.addSpectator(player, "death");
 			// Else, take them to the exit area
 			} else {
 				event.setRespawnLocation(plugin.getCoords("exit"));
 			}
			plugin.removePlayer(player, null, "You have been removed from the Battle because you were killed.");
 			plugin.BattleUsersRespawn.remove(player.getName());
 		}
 	}
 	
 }

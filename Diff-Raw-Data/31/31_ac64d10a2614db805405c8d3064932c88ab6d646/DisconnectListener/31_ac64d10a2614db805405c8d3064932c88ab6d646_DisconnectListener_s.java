 package me.limebyte.battlenight.core.Listeners;
 
 import me.limebyte.battlenight.core.BattleNight;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class DisconnectListener implements Listener {
 	
 	// Get Main Class
 	public static BattleNight plugin;
 	public DisconnectListener(BattleNight instance) {
 		plugin = instance;
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		Player player = event.getPlayer();
 		if (plugin.BattleUsersTeam.containsKey(player.getName())) {
			plugin.removePlayer(player, "has been removed from the Battle as they disconnected from the server.", null);
 		}
 		else if (plugin.BattleSpectators.containsKey(player.getName())) {
 			plugin.removeSpectator(player);
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerKick(PlayerKickEvent event) {
 		Player player = event.getPlayer();
 		if (plugin.BattleUsersTeam.containsKey(player.getName())) {
			plugin.removePlayer(player, "has been removed from the Battle as they were kicked from the server.", null);
 		}
 		else if (plugin.BattleSpectators.containsKey(player.getName())) {
 			plugin.removeSpectator(player);
 		}
 	}
 }

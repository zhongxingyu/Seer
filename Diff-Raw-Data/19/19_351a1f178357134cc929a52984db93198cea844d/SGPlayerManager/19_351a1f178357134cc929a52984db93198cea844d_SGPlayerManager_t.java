 package com.xdrapor.safeguard.player;
 
 import java.util.HashMap;
 import java.util.Map;
 
import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import com.xdrapor.safeguard.core.ICore;
 
 /**
  * Handles all player instances SafeGuard is tracking.
  * 
  * @author IchBinMude
  *
  */
 public class SGPlayerManager implements ICore {
 
 	/** A map of player UserNames and their SGPlayer counterpart. */
 	public Map<String, SGPlayer> sgPlayerData = new HashMap<String, SGPlayer>();

 	/** Adds an SGPlayer instance for SafeGuard to track. */
 	public void addPlayer(String username, SGPlayer sgPlayer) {
 		this.sgPlayerData.put(username, sgPlayer);
 	}
 	
 	/** Removes an SGPlayer instance SafeGuard is tracking. */
 	public void removePlayer(String username) {
 		this.sgPlayerData.remove(username);
 	}
 	
 	/** Returns the SGPlayer instance for the specified UserName. */
 	public SGPlayer getPlayer(String username) {
 		return (this.sgPlayerData.get(username));
 	}
 
 	/** Returns a map of the current SGPlayer instances SafeGuard is tracking. */
 	public Map<String, SGPlayer> getPlayers() {
 		return (this.sgPlayerData);
 	}
 	
 	/** Returns whether the specified player is being tracked. */
 	public boolean isTracking(Player player) {
 		return (isPlayerOnline(player) ? this.sgPlayerData.containsKey(player.getName()) : false);
 	}
 	
 	/** Returns whether the specified player is online. */
 	public boolean isPlayerOnline(Player player) {
 		
 		for(Player examinedPlayer : safeGuard.getServer().getOnlinePlayers()) {
 			if (examinedPlayer.getEntityId() == player.getEntityId()) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	/** Generates SGPlayer instances for all online players. */
 	public void loadOnlinePlayers() {
		for(Player player : Bukkit.getServer().getOnlinePlayers()) {
 			if(!safeGuard.sgPlayerManager.getPlayers().containsKey(player.getName())) {
 				safeGuard.sgPlayerManager.addPlayer(player.getName(), new SGPlayer(player.getName()));
 			}
 		}
 	}
 }

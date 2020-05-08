 package net.worldoftomorrow.eventcontrol.permcache;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.worldoftomorrow.eventcontrol.EventControl;
 import net.worldoftomorrow.eventcontrol.PlayerUtil;
 
 import org.bukkit.entity.Player;
 
 public class PermCache {
 	
 	private final Map<String, Map<String, Boolean>> players = new HashMap<String, Map<String, Boolean>>();
 	
 	public PermCache() {
 	}
 	
 	/**
 	 * Adds player to list if not already present, updates if already present.
 	 * @param p
 	 */
 	public void updatePlayer(Player p) {
 		players.put(p.getName(), PlayerUtil.getPlayersPermissions(p));
 	}
 	
 	public void removePlayer(Player p) {
 		if(players.containsKey(p.getName())) {
 			players.remove(p.getName());
 		}
 	}
 	
 	public boolean isPermissionSet(Player p, String perm) {
 		// Assume for now that the player will always be in the list
 		// We can add a check later if this becomes an issue.
 		return players.get(p.getName()).containsKey(perm);
 	}
 	
 	public boolean hasPermission(Player p, String perm) {
 		Map<String, Boolean> permset = this.getPermissionsSet(p);
 		if(permset.containsKey(perm)) {
 			return permset.get(perm);
 		} else {
 			return false;
 		}
 	}
 	
 	public boolean isPermissionFalse(Player p, String perm) {
 		Map<String, Boolean> permset = this.getPermissionsSet(p);
 		if(permset.containsKey(perm)) {
 			// If the permission is set FALSE return TRUE
 			return !permset.get(perm);
 		} else {
 			// Return true if the permission is not set at all
 			return true;
 		}
 	}
 	
 	public Map<String, Boolean> getPermissionsSet(Player p) {
		return players.get(p);
 	}
 	
 	public void reloadCache() {
 		for(Player p : EventControl.instance().getServer().getOnlinePlayers()) {
 			this.updatePlayer(p);
 		}
 	}
 }

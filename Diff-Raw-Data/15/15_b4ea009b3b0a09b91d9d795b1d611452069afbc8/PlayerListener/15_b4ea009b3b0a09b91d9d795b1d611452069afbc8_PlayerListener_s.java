 package com.gmail.klezst.reserves;
 
 import java.util.LinkedList;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import bukkitutil.compatibility.Permission;
 
 /**
  * Determines, if a player needs to be kicked.
  * 
  * @author Klezst
  */
 public class PlayerListener implements Listener {
     private final String permissionNode = "reserves";
 
    private LinkedList<Player> playerJoinOrder = new LinkedList<Player>();
     
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
 	// Used to determine which player has been on the longest.
 	// We cannot check for reservation permission here, in case permissions are changed while the player is logged in.
 	playerJoinOrder.add(event.getPlayer());
     }
 
     @EventHandler
     public void onPlayerQuit(PlayerQuitEvent event) {
 	playerJoinOrder.remove(event.getPlayer());
     }
 
     @EventHandler
     public void onPlayerPreLogin(PlayerLoginEvent event) {
 	// Determine, if the server is full && if the player has a reserved slot.
 	if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL
 		&& Permission.has(event.getPlayer(), permissionNode)) {
 
 	    // Kick the player who has been connected longest and does not have a slot reserved.
 	    for (Player player : playerJoinOrder) {
 		if (!Permission.has(player, permissionNode)) {
 		    player.kickPlayer("Sorry, the server is full; a player with a reserved slot logged in.");
 		    event.allow(); // Allow the connecting player to join the server despite that it was full.
 		    break;
 		}
 	    }
 	}
     }
 };

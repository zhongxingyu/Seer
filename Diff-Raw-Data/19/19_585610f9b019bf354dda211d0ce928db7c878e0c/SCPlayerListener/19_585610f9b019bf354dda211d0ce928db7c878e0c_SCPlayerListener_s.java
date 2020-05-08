 package be.thisisboris.SocietyCraft;
 
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import be.thisisboris.SocietyCraft.includes.SCLogger;
 
 /**
  * Handle events for all Player related events
  * @authors Thisisboris and cskiwi
  * 
  * 
  */
 
 public class SCPlayerListener extends PlayerListener {
 	
 	private final SocietyCraft plugin;
 	
 	public SCPlayerListener(SocietyCraft instance) {
 		
 		plugin = instance;
 		
 	}
 	
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		int index = plugin.Onlineplayerlist.size();
		
 		plugin.Onlineplayerlist.add(event.getPlayer());
		SCLogger.warning("Adding "+event.getPlayer().getDisplayName()+" to the list");
 	}
 	public void onPlayerQuit(PlayerQuitEvent event) {
		 
 	}    	
     public void onPlayerKick(PlayerKickEvent event) {
     	
     }
 	
 	
 }

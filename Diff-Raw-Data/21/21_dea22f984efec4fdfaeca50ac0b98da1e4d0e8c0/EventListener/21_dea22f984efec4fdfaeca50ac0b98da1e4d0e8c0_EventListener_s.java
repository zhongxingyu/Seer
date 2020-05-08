 package org.bonsaimind.bukkitplugins.simplecronclone;
 
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 
 public class EventListener implements Listener {
 	private Plugin sccMain;
 	
 	public EventListener(Plugin plugin) {
 		sccMain = plugin;
 	}
 
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event){
 		//eventJoin
 		sccMain.eventEngine.eventPlayerJoin(event.getPlayer().getName());
 		if (!event.getPlayer().hasPlayedBefore()){
 			//eventFirstJoin
			//TODO: this seems not to be reliable? 
 			sccMain.eventEngine.eventFirstJoin(event.getPlayer().getName());
 		}
 		if (sccMain.getServer().getOnlinePlayers().length == 1){
 			//only user logged in means that we were just empty.
 			sccMain.eventEngine.eventServerNotEmpty(event.getPlayer().getName());
 		}
 	}
 
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		//eventQuit
 		sccMain.eventEngine.eventPlayerQuit(event.getPlayer().getName());
		if (sccMain.getServer().getOnlinePlayers().length == 0){
			//no users logged in, means last player just quit.
 			sccMain.eventEngine.eventServerEmpty(event.getPlayer().getName());
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerChangedWorld(PlayerChangedWorldEvent event){
 		//eventPlayerWorldMove
 		sccMain.eventEngine.eventPlayerWorldMove(event.getPlayer().getName(),event.getFrom().getName(),event.getPlayer().getWorld().getName());
 		
 		if(event.getFrom().getPlayers().size()==0){
 			//eventWorldEmpty
 			sccMain.eventEngine.eventWorldEmpty(event.getPlayer().getName(),event.getFrom().getName());
 		}
 		if(event.getPlayer().getWorld().getPlayers().size() == 1){
 			//eventWorldNotEmpty  (if one player, that must means its ours that just moved)
 			sccMain.eventEngine.eventWorldNotEmpty(event.getPlayer().getName(),event.getPlayer().getWorld().getName());
 		}
 		
 	}
 }

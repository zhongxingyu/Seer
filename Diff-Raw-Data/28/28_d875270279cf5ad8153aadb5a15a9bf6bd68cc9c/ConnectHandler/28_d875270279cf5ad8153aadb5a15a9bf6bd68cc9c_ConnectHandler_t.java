 package me.cyberkitsune.prefixchat;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class ConnectHandler implements Listener {
 	private KitsuneChat plugin;
 	public ConnectHandler(KitsuneChat plugin) {
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void onDisconnect(PlayerQuitEvent evt) {
 		if(plugin.party.isInAParty(evt.getPlayer())) {
 			plugin.party.leaveParty(evt.getPlayer(), true);
 		}
 		
 	}
 	
 	@EventHandler
 	public void onConnect(PlayerJoinEvent evt) {
 		if(!plugin.dataFile.getPartyDataForUser(evt.getPlayer()).equalsIgnoreCase(""))  {
 			plugin.party.changeParty(evt.getPlayer(), plugin.dataFile.getPartyDataForUser(evt.getPlayer()));
 		}
 		if(plugin.dataFile.getUserChannel(evt.getPlayer()) == null) {
			plugin.dataFile.setUserChannel(evt.getPlayer(), plugin.getConfig().getString("default"));
 		}
 	}
 
 }

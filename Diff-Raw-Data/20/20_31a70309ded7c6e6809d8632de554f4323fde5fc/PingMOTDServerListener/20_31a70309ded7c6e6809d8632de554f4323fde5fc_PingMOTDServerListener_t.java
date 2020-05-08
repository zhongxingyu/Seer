 package com.modcrafting.pingmotd;
 
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
 import org.bukkit.event.server.ServerListPingEvent;
 
public class PingMOTDServerListener implements Listener{
 	PingMotd plugin;
 
 	public PingMOTDServerListener(PingMotd instance) {
 		plugin = instance;
 	}
	@EventHandler(priority = EventPriority.MONITOR)
 	public void onServerListPing(ServerListPingEvent event){
 		String Msg = plugin.newMsg();
 		if (Msg == null){
 			event.setMotd(event.getMotd());			
 		}else{
 			event.setMotd(plugin.newMsg());
 			
 		}
 	}
 }

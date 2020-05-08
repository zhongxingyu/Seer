 package com.modcrafting.pingmotd;
 
 import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.server.ServerListener;
 
public class PingMOTDServerListener extends ServerListener{
 	PingMotd plugin;
 
 	public PingMOTDServerListener(PingMotd instance) {
 		plugin = instance;
 	}
 	public void onServerListPing(ServerListPingEvent event){
 		String Msg = plugin.newMsg();
 		if (Msg == null){
 			event.setMotd(event.getMotd());			
 		}else{
 			event.setMotd(plugin.newMsg());
 			
 		}
 	}
 }

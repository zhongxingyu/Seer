 package org.theglicks.bukkit.BDchat.events;
 
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.theglicks.bukkit.BDchat.BDchat;
 
 public class PlayerLeaveListener implements Listener {
 	public void onPlayerLeave(PlayerQuitEvent playerLeave){
 		BDchat.BDchatPlayerList.remove(playerLeave.getPlayer().getName());
 	}
 }

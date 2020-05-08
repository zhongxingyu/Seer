 package com.reil.bukkit.rBorder;
 
import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 public class rBorderListener extends PlayerListener{
 	public BorderPlugin rBorder;
 	public rBorderListener(BorderPlugin rBorder) {
 		this.rBorder = rBorder;
 	}
 	
 	public void onPlayerMove(PlayerMoveEvent event){
 		if (rBorder.inBorder(event.getTo()))
 			return;
 		event.setCancelled(true);
		Player barMe = event.getPlayer();
		barMe.sendMessage(rBorder.BorderAlert);
		barMe.teleportTo(event.getFrom());
 		return;
 	}
 	public void onPlayerTeleport(PlayerMoveEvent event){
 		if (rBorder.inBorder(event.getTo()))
 			return;
 		event.setCancelled(true);
		Player barMe = event.getPlayer();
		barMe.sendMessage(rBorder.BorderAlert);
		barMe.teleportTo(event.getFrom());
 		return;
 	}
 	
 }

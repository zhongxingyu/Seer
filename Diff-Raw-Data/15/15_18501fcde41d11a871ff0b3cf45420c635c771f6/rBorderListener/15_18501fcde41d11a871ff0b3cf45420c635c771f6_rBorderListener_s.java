 package com.reil.bukkit.rBorder;
 
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
		event.getPlayer().sendMessage(rBorder.BorderAlert);
 		event.setCancelled(true);
		event.getPlayer().teleportTo(event.getFrom());
 		return;
 	}
 	public void onPlayerTeleport(PlayerMoveEvent event){
 		if (rBorder.inBorder(event.getTo()))
 			return;
		rBorder.log.info("Border hit by: " + event.getPlayer().getDisplayName());
 		event.setCancelled(true);
		event.getPlayer().sendMessage(rBorder.BorderAlert);
 		return;
 	}
 	
 }

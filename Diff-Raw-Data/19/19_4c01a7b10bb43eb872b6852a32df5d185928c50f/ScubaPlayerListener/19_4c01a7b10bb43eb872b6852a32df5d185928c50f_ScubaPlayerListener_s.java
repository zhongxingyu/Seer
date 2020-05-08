 package com.sadmean.mc.ScubaKit;
 
import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 
 public class ScubaPlayerListener extends PlayerListener {
 	
	String chatStart = ChatColor.DARK_AQUA + "[ScubaKit] " +ChatColor.GRAY;
 	Player player;
 	String airEnder = " seconds of air remaining";
 	
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		player = event.getPlayer();
 		ScubaKit.log_It("fine", player.getName() + " joined the server. Checking air!");
 		ScubaKit.setAir(player);
 	}
 	
 	public void onPlayerMove(PlayerMoveEvent event){
 		player = event.getPlayer();
 		ScubaKit.setAir(player);
 	}
 	
 	public static ScubaKit plugin; public ScubaPlayerListener(ScubaKit instance) { 
		 
        plugin = instance;
 }
 
 
 }

 package me.tehbeard.BeardAch.listener;
 
 import me.tehbeard.BeardAch.chunkCache;
 import me.tehbeard.BeardAch.achievement.AchievementManager;
 import me.tehbeard.BeardAch.achievement.triggers.CuboidCheckTrigger;
 import me.tehbeard.BeardAch.achievement.triggers.ITrigger;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 public class BeardAchPlayerListener extends PlayerListener {
 
 	public void onPlayerMove(PlayerMoveEvent event){
 		if(event.getTo().getBlockX() != event.getFrom().getBlockX() ||
 				event.getTo().getBlockY() != event.getFrom().getBlockY() ||	
 				event.getTo().getBlockZ() != event.getFrom().getBlockZ()
 				){
 			chunkCache.checkLocation(event.getPlayer());
 		}
 	}
 	
 	@Override
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		// TODO Auto-generated method stub
 		AchievementManager.loadAchievements(event.getPlayer().getName());
 		
 	}
 	
 	
 }

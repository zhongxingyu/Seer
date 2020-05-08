 package me.weecazza7.start;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 
 public class NoDropDeadEventListener implements Listener{ 
 
 	@EventHandler
 	public void PlayerDeathEvent(PlayerDeathEvent event){
 		Player p = event.getEntity();
 		Player k = event.getEntity().getKiller();
		event.getKeepLevel();
 		event.getDrops().clear();
		k.giveExpLevels(1);
 }
 }

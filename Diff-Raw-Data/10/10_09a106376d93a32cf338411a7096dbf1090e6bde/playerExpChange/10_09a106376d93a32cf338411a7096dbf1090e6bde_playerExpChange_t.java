 package me.eistee2.minebuilder;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
 
 public class playerExpChange implements Listener {
 	
 	ReadOut info = ReadOut.getInstance();
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	private void onPlayerExpChange(PlayerExpChangeEvent event)
 	{
 		if(event.getPlayer().getLevel() >= info.getMaxLevel())
 		{
 			event.setAmount(0);
 		}
 	}
	@EventHandler(priority = EventPriority.NORMAL)
	private void onPlayerLevelUp(PlayerLevelChangeEvent event)
	{
		if(event.getPlayer().getLevel() >= info.getMaxLevel())
		{
			event.getPlayer().setLevel(info.getMaxLevel());
		}
	}
 
 }

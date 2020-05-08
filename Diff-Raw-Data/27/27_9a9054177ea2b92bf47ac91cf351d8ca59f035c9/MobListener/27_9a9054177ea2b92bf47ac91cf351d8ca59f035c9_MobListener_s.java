 package me.bluejelly.main.listeners;
 
 import me.bluejelly.main.GuildZ;
 
import org.bukkit.Bukkit;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 public class MobListener implements Listener {
 
 	static GuildZ main;
 	
 	public MobListener(GuildZ instance)
 	{
 		main = instance;
 	}
 	
 	@EventHandler(ignoreCancelled=true)
 	public void onMobDeath(EntityDeathEvent event)
 	{
 		
		if(event.getEntity().getKiller() != null)
		{
			Bukkit.broadcastMessage("You found a bloodycoin!");
 		}
 		
 	}
 }

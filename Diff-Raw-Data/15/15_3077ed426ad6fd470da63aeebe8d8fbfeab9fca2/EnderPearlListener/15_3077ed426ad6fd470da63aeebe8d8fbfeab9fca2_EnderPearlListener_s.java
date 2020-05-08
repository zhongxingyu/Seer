 package me.shock.boatspeed;
 
import org.bukkit.entity.EntityType;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class EnderPearlListener extends JavaPlugin implements Listener
 {
 
 	
 	final Main plugin;
 	public EnderPearlListener(Main instance)
 	{
 		plugin = instance;
 	}
 	
 	@EventHandler
	public void onTeleport(EntityTeleportEvent event)
 	{
		EntityType entity = event.getEntityType();
		if (entity.equals(EntityType.ENDER_PEARL))
			event.setCancelled(true);
 	}
 }

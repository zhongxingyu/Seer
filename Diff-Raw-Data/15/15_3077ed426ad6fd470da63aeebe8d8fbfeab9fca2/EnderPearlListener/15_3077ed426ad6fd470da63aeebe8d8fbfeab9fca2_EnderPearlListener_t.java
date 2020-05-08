 package me.shock.boatspeed;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class EnderPearlListener extends JavaPlugin implements Listener
 {
 
 	
 	final Main plugin;
 	public EnderPearlListener(Main instance)
 	{
 		plugin = instance;
 	}
 	
 	@EventHandler
	public void onTeleport(PlayerTeleportEvent event)
 	{
		TeleportCause cause = event.getCause();
		if (cause.equals(TeleportCause.ENDER_PEARL));
		event.setCancelled(true);
 	}
 }

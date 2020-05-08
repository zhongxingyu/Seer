 package com.legit2.Demigods.Listeners;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 import com.legit2.Demigods.Demigods;
 import com.legit2.Demigods.Utilities.DDeityUtil;
 
 public class DCommandListener implements Listener
 {
 	static Demigods plugin;
 	
 	public DCommandListener(Demigods instance)
 	{
 		plugin = instance;
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public static void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
 	{
 		String message = event.getMessage();
		message = message.substring(1);
 		String[] args = message.split("\\s+");
 		Player player = event.getPlayer();
 		
 		try
 		{
 			DDeityUtil.invokeDeityCommand(player, args);
 		}
 		catch(Exception e)
 		{
 			// Not a command
 		}
 	}
 }

 package com.censoredsoftware.demigods;
 
 import com.censoredsoftware.demigods.data.DataManager;
 import com.censoredsoftware.demigods.data.ThreadManager;
 import com.censoredsoftware.demigods.exception.DemigodsStartupException;
 import com.censoredsoftware.demigods.player.DPlayer;
 import com.censoredsoftware.demigods.util.Messages;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.HandlerList;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Class for all plugins of demigods.
  */
 public class DemigodsPlugin extends JavaPlugin
 {
 	/**
 	 * The Bukkit enable method.
 	 */
 	@Override
 	public void onEnable()
 	{
 		try
 		{
 			// Load the game engine.
 			new Demigods(this);
 
 			// Print success!
 			Messages.info("Successfully enabled.");
 		}
 		catch(DemigodsStartupException ignored)
 		{}
 	}
 
 	/**
 	 * The Bukkit disable method.
 	 */
 	@Override
 	public void onDisable()
 	{
 		// Save all the data.
 		DataManager.save();
 
 		// Toggle all prayer off
 		for(Player player : Bukkit.getOnlinePlayers())
 			DPlayer.Util.togglePrayingSilent(player, false, false);;
 
		// Cancel all threads, callAbilityEvent calls, and connections.
 		ThreadManager.stopThreads(this);
 		HandlerList.unregisterAll(this);
 
 		Messages.info("Successfully disabled.");
 	}
 }

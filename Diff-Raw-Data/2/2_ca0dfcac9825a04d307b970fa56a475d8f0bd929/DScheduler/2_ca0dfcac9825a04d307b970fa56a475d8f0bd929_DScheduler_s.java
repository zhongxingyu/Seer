 package com.legit2.Demigods;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import com.legit2.Demigods.Database.DDatabase;
 import com.legit2.Demigods.Utilities.DConfigUtil;
 import com.legit2.Demigods.Utilities.DPlayerUtil;
 import com.legit2.Demigods.Utilities.DMiscUtil;
 
 @SuppressWarnings("unused")
 public class DScheduler
 {
 	// Define commands
 	private static Demigods plugin = DMiscUtil.getPlugin();
 	private static int savePlayers;
 	
 	/*
 	 *  startThreads() : Starts the scheduler threads.
 	 */
 	@SuppressWarnings("deprecation")
 	public static void startThreads()
 	{
 		// Setup threads for saving, health, and favor
 		int start_delay = (int)(DConfigUtil.getSettingDouble("start_delay_seconds")*20);
 		int favor_frequency = (int)(DConfigUtil.getSettingDouble("favor_regen_seconds")*20);
 		int save_frequency = DConfigUtil.getSettingInt("save_interval_seconds")*20;
 		if (favor_frequency < 0) favor_frequency = 600;
 		if (start_delay <= 0) start_delay = 1;
 		if (save_frequency <= 0) save_frequency = 300;
 					
 		plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				DDatabase.saveAllData();
 			}
 		}, start_delay, save_frequency);
 		
 		plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				DPlayerUtil.regenerateAllFavor();
 			}
 		}, 0, favor_frequency);
 	}
 	
 	/*
 	 *  stopThreads() : Stops all scheduler threads.
 	 */
 	public static void stopThreads()
 	{
		plugin.getServer().getScheduler().cancelAllTasks();
 	}
 }

 package com.legit2.Demigods;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 @SuppressWarnings("unused")
 public class DScheduler
 {
 	// Define commands
 	private static Demigods plugin = DUtil.getPlugin();
 	private static int savePlayers;
 	
 	/*
 	 *  startThreads() : Starts the scheduler threads.
 	 */
 	@SuppressWarnings("deprecation")
 	public static void startThreads()
 	{
 		// Setup threads for saving, health, and favor
 		int start_delay = (int)(DConfig.getSettingDouble("start_delay_seconds")*20);
 		int favor_frequency = (int)(DConfig.getSettingDouble("favor_regen_seconds")*20);
 		int save_frequency = DConfig.getSettingInt("save_interval_seconds")*20;
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
 				DUtil.regenerateAllFavor();
 			}
 		}, 0, favor_frequency);
 		
 		// Expiring Hashmaps
 		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				for(Player player : Bukkit.getOnlinePlayers())
 				{
 					if(DSave.hasPlayerData(player.getName(), "was_PVP_temp"))
					{				
						if(Long.getLong(DSave.getPlayerData(player.getName(), "was_PVP_temp").toString())
								> System.currentTimeMillis() - 
								(long) (DConfig.getSettingDouble("pvp_area_delay_seconds") * 20)) continue;
 						
 						DSave.removePlayerData(player.getName(), "was_PVP_temp");
 						player.sendMessage(ChatColor.YELLOW + "You are now safe from PVP.");
 					}
 				}
 			}
		}, 0, 15);
 
 	}
 	
 	/*
 	 *  stopThreads() : Stops all scheduler threads.
 	 */
 	public static void stopThreads()
 	{
 		plugin.getServer().getScheduler().cancelAllTasks();
 	}
 }

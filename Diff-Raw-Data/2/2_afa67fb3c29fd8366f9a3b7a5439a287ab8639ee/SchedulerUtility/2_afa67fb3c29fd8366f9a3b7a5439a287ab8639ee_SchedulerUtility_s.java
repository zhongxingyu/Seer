 package com.censoredsoftware.Demigods.Engine.Utility;
 
 import org.bukkit.Bukkit;
 
 import com.censoredsoftware.Demigods.DemigodsPlugin;
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Object.Ability.Ability;
 import com.censoredsoftware.Demigods.Engine.Object.Deity.Deity;
 import com.censoredsoftware.Demigods.Engine.Runnable.FavorRunnable;
 import com.censoredsoftware.Demigods.Engine.Runnable.TimedDataRunnable;
 
 public class SchedulerUtility
 {
 	public static void startThreads(DemigodsPlugin instance)
 	{
 		// Start favor runnable
 		int rate = Demigods.config.getSettingInt("regeneration.favor") * 20;
 		Bukkit.getScheduler().scheduleAsyncRepeatingTask(instance, new FavorRunnable(Demigods.config.getSettingDouble("multipliers.favor")), 20, rate);
 		AdminUtility.sendDebug("Favor regeneration runnable enabled...");
 
 		// Start battle runnable
 		// Bukkit.getScheduler().scheduleAsyncRepeatingTask(instance, new BattleRunnable(), 20, 20);
 		// AdminUtility.sendDebug("Battle tracking runnable enabled...");
 
 		// Start timed data runnable
 		Bukkit.getScheduler().scheduleAsyncRepeatingTask(instance, new TimedDataRunnable(), 20, 20);
 		AdminUtility.sendDebug("Timed data runnable enabled...");
 
 		for(Deity deity : Demigods.getLoadedDeities())
 		{
 			for(Ability ability : deity.getAbilities())
 			{
				if(ability.getRunnable() != null) Bukkit.getScheduler().scheduleSyncRepeatingTask(Demigods.plugin, ability.getRunnable(), 0, ability.getInfo().getDelay());
 			}
 		}
 	}
 
 	public static void stopThreads(DemigodsPlugin instance)
 	{
 		instance.getServer().getScheduler().cancelTasks(instance);
 	}
 }

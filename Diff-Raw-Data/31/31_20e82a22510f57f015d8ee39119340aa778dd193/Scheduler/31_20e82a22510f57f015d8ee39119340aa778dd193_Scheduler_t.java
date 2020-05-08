 package me.cmesh.DreamLand;
 
 public class Scheduler 
 {
 	public static DreamLand plugin;
 	
 	public Scheduler(DreamLand instance)
 	{
 		plugin = instance;
 	}
 	
 	public void Start()
     {
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new CheckTime(), 0L, 1000L);
     }
 
     public void Stop()
     {
     	plugin.getServer().getScheduler().cancelTasks(plugin);
     }
 	
 	private class CheckTime implements Runnable
 	{
 		public void run()
 		{
 			plugin.dream.getWorld().setTime(500L);
 		}
     }
 }

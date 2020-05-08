 package com.censoredsoftware.camcorder;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitRunnable;
 
 public class CamcorderRecord extends BukkitRunnable
 {
 	private static CamcorderPlugin plugin;
 
 	static Player recording;
 	static String name;
 	protected static Location start;
 	protected static Integer count;
 
 	public CamcorderRecord(CamcorderPlugin plugin)
 	{
 		this.plugin = plugin;
 		count = 0;
 		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 0, 1);
 	}
 
 	@Override
 	public void run()
 	{
 		if(recording == null) return;
 		if(start == null)
 		{
 			start = recording.getLocation();
 			recordStart(start.getX(), start.getY(), start.getZ(), start.getPitch(), start.getYaw());
 			return;
 		}
 		Location to = recording.getLocation();
 		double X = to.getX();
 		double Y = to.getY();
 		double Z = to.getZ();
 		float pitch = to.getPitch();
 		float yaw = to.getYaw();
 		count++;
 		record(X, Y, Z, pitch, yaw, count);
 	}
 
 	private static void recordStart(double X, double Y, double Z, float pitch, float yaw)
 	{
 		String prefix = recording.getName() + "." + name + "." + ".start.";
 		plugin.getConfig().set(prefix + "X", X);
 		plugin.getConfig().set(prefix + "Y", Y);
 		plugin.getConfig().set(prefix + "Z", Z);
 		plugin.getConfig().set(prefix + "pitch", pitch);
 		plugin.getConfig().set(prefix + "yaw", yaw);
 		plugin.saveConfig();
 	}
 
 	private static void record(double X, double Y, double Z, float pitch, float yaw, long time)
 	{
 		String prefix = recording.getName() + "." + name + "." + time + ".";
 		plugin.getConfig().set(prefix + "X", X);
 		plugin.getConfig().set(prefix + "Y", Y);
 		plugin.getConfig().set(prefix + "Z", Z);
 		plugin.getConfig().set(prefix + "pitch", pitch);
 		plugin.getConfig().set(prefix + "yaw", yaw);
 		plugin.saveConfig();
 	}
 
	static void end()
 	{
		plugin.getConfig().set(recording.getName() + "." + name + ".end", count * 50);
 	}
 }

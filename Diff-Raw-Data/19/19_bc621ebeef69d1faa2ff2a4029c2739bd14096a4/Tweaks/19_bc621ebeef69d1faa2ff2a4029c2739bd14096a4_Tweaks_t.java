 package com.bitlimit.Tweaks;
 
import com.sk89q.worldedit.bukkit.BukkitBiomeType;
import org.bukkit.Bukkit;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitScheduler;
 
 import java.util.HashMap;
 import java.util.List;
 
 public class Tweaks extends JavaPlugin {
     private int weatherId;
 
     @Override
     public void onEnable() {
         new TweaksListener(this);
 
         this.getCommand("tweaks").setExecutor(new TweaksCommandExecutor(this));
         this.saveConfig();
     }
 
     @Override
     public void onDisable() {
         this.getServer().getScheduler().cancelTask(this.weatherId);
         this.saveConfig();
     }
 
     @Override
     public void saveConfig() {
         super.saveConfig();
 
         this.getConfig().options().copyDefaults(true);
         this.reloadConfig();
         setRepeatingTaskEnabled(this.getConfig().getConfigurationSection("weather").getBoolean("enabled"));
     }
 
     public void setRepeatingTaskEnabled(boolean enabled) {
         Server server = this.getServer();
         BukkitScheduler scheduler = server.getScheduler();
	    final long baseDuration = 1200L;

        if (enabled && this.weatherId == 0)
        {
             class BitLimitRecurringTask implements Runnable {
                 Plugin plugin;
 
                 BitLimitRecurringTask(Plugin p) {
                     plugin = p;
                 }
 
                 public void run() {
 	                for (HashMap<String, Object> worldDefinition : (List<HashMap<String, Object>>)this.plugin.getConfig().getConfigurationSection("weather").getList("worlds"))
 	                {
 			            String worldName = (String)worldDefinition.get("name");
 		                World world = this.plugin.getServer().getWorld(worldName);
 
 		                if (!world.hasStorm())
 		                {
 			                int weatherDuration = world.getWeatherDuration();
			                Double ratio = (Double)worldDefinition.get("reduction");
			                int counterAmount = (int)(baseDuration * ratio);
 			                world.setWeatherDuration(weatherDuration + counterAmount);

			                Bukkit.broadcastMessage("Reducing weather counter with ratio " + ratio + " in world " + world);
 		                }
 	                }
                 }
             }
 
            this.weatherId = scheduler.scheduleSyncRepeatingTask(this, new BitLimitRecurringTask(this), baseDuration, baseDuration);
         } else if (this.weatherId != 0) {
             scheduler.cancelTask(this.weatherId);
             this.weatherId = 0;
         }
     }
 }
 

package me.cain.mobspawn;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class MobSpawn extends JavaPlugin
 {
	static Util util;
 	
 	public void onEnable()
 	{
 		getServer().getPluginCommand("mob").setExecutor(new MobExecutor());
		util.console("[" + this.getDescription().getName() + "] MobSpawner has been enabled!");
 	}
 	
 	public void onDisable()
 	{
		util.console("[" + this.getDescription().getName() + "] MobSpawner has been disabled!");
 	}
 	
 }

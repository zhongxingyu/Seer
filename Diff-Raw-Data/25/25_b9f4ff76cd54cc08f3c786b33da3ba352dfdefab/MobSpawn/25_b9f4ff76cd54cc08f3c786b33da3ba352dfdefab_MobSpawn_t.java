package main.java.me.cain.mobspawn;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class MobSpawn extends JavaPlugin
 {
	Util util;
 	
 	public void onEnable()
 	{
 		getServer().getPluginCommand("mob").setExecutor(new MobExecutor());
		System.out.println("[MobSpawn] MobSpawner has been enabled!");
 	}
 	
 	public void onDisable()
 	{
		System.out.println("[MobSpawn] MobSpawner has been disabled!");
 	}
 	
 }

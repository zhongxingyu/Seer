 package com.craigknott.setLocker;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SetLocker extends JavaPlugin {
 	
	public void onEnabled () {
 		getCommand("getCurrentSelection").setExecutor(new CommandManager(this));
 	}
 	
 }

 package com.craigknott.setLocker;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SetLocker extends JavaPlugin {
 	
	@Override
	public void onDisable() {
	 
	}
	 
	@Override
	public void onEnable() {
 		getCommand("getCurrentSelection").setExecutor(new CommandManager(this));
 	}
	 
 	
 }

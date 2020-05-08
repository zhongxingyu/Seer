 package me.Bambusstock.Chaname;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Chaname extends JavaPlugin{
     public void onEnable() {
 	// Load config
	this.getConfig().options().copyDefaults(true);
 	this.saveConfig();
 	getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
     }
 
     public void onDisable() {
     }
 }

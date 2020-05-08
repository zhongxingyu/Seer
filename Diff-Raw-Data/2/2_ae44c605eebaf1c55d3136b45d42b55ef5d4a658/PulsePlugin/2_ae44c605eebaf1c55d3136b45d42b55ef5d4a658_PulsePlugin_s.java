package com.bitlimit.bits.bukkit;
 
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.logging.Level;
 
 public class PulsePlugin extends JavaPlugin
 {
 	public Level broadcastLevel;
 
 	@Override
 	public void onEnable()
 	{
 		this.handleDefaults();
 
 		this.broadcastLevel = (Level)this.getConfig().get("broadcastLevel");
 	}
 
 	@Override
 	public void onDisable()
 	{
 		this.saveConfig();
 	}
 
 	@Override
 	public void saveConfig() {
 		this.getConfig().set("broadcastLevel", this.broadcastLevel);
 
 		super.saveConfig();
 	}
 
 	private void handleDefaults()
 	{
 		Configuration defaults = new YamlConfiguration();
 		defaults.set("broadcastLevel", Level.ALL);
 
 		this.getConfig().setDefaults(defaults);
 	}
 
 	public void setBroadcastLevel(Level newLevel)
 	{
 		this.broadcastLevel = newLevel;
 
 		this.saveConfig();
 	}
 }

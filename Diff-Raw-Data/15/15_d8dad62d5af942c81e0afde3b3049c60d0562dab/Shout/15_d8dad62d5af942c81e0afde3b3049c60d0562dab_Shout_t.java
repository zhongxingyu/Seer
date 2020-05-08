 package com.pixelvent.bukkit.shout;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Shout extends JavaPlugin
 {
 	public static Shout instance;
 	
 	public Logger log;
 	public PluginDescriptionFile desc;
 	public FileConfiguration config;
	public FileConfiguration publicConfig;
 	public EventListener eventListener;
 	
 	public void onEnable()
 	{
 		Shout.instance = this;
 		
 		log = getLogger();
 		desc = getDescription();
 		config = YamlConfiguration.loadConfiguration(this.getResource("config.yml"));
		publicConfig = getConfig();
 		
 		eventListener = new EventListener();
 				
 		setupCommands();
 		setupScheduledTasks();
		reloadPublicConfig();
 	}
 
 	public void onDisable()
 	{
 		getServer().getScheduler().cancelTasks(this);
 	}
 
 	private void setupCommands()
 	{
 		CustomCommandExecutor cce = new CustomCommandExecutor();
 		
 		
 	}
 
 	private void setupScheduledTasks()
 	{
 		
 	}
 	
	private void reloadPublicConfig()
 	{
		reloadConfig();
 	}
 	
 	public String getChatPrefix()
 	{
		return ChatColor.getByChar(config.getString("settings.announcerPrefix")) + "[" + config.getString("settings.announceColor") + "]" + ChatColor.RESET + " ";
 	}
 }

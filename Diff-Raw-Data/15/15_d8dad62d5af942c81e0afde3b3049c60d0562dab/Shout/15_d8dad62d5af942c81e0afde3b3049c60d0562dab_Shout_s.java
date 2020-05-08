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
 	public EventListener eventListener;
 	
 	public void onEnable()
 	{
 		Shout.instance = this;
 		
 		log = getLogger();
 		desc = getDescription();
 		config = YamlConfiguration.loadConfiguration(this.getResource("config.yml"));
 		
 		eventListener = new EventListener();
 				
 		setupCommands();
 		setupScheduledTasks();
		loadPublicConfig();
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
 	
	private void loadPublicConfig()
 	{
		
 	}
 	
 	public String getChatPrefix()
 	{
		return ChatColor.RED + "[] ";
 	}
 }

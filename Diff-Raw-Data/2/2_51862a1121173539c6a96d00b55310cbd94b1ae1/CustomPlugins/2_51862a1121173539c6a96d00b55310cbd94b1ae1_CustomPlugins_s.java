 package de.mrpixeldream.bukkit.customplugins;
 
 import java.io.File;
 
 import org.bukkit.event.HandlerList;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CustomPlugins extends JavaPlugin
 {
 	private final String CONSOLE = "[CustomPlugins] ";
 	
 	@Override
 	public void onEnable()
 	{
 		System.out.println(CONSOLE + "Activating CustomPlugins...");
 		
 		loadConfig();
 		
 		this.getServer().getPluginManager().registerEvents(new CommandOverride(this.getConfig().getString("Plugin-Message")), this);
 		
 		System.out.println(CONSOLE + "Successfully set new /plugins command!");
 	}
 	
 	@Override
 	public void onDisable()
 	{
 		System.out.println(CONSOLE + "Deactivating CustomPlugins...");
 		
 		HandlerList.unregisterAll();
 		
 		System.out.println(CONSOLE + "Successfully deactivated! Thanks for using!");
 	}
 	
 	private void loadConfig()
 	{
 		// Check the config files existence
 		boolean configFileExistant = false;
 
 		if (new File("plugins/CustomPlugins/").exists())
 		{
 			configFileExistant = true;
 		}
 
 		// Allowing to set default values
 		this.getConfig().options().copyDefaults(true);
 
 		// Adding default values
 		if (!configFileExistant)
 		{
 			this.getConfig().addDefault("Plugin-Message", "Don't spy my plugins!");
 		}
 
 		// Saving and reloading config from file
 		this.saveConfig();
 		this.reloadConfig();
 	}
 }

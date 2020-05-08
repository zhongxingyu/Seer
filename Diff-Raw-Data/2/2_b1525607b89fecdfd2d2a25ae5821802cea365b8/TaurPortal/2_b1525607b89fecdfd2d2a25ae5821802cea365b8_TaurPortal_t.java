 package me.embarker.tauryuu.taurportal;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class TaurPortal extends JavaPlugin {
 	
 	public static FileConfiguration config;
 	
 	public void onEnable()
 	{
 		Bukkit.getServer().getPluginManager().registerEvent(Type.PLAYER_PORTAL, new TaurPortalListener(), Priority.Normal, this);
 		Bukkit.getServer().getPluginManager().registerEvent(Type.PLAYER_TELEPORT, new TaurPortalListener(), Priority.Normal, this);
 		System.out.println("[TaurPortal] version " +  this.getDescription().getVersion() + " has been enabled.");
 		config = getConfig();
 		configSetup();
 		saveConfig();
 	}
 	
 	public void onDisable()
 	{
 		System.out.println("[TaurPortal] has been disabled.");
 	}
 	
 	public void configSetup()
 	{
 		if(config.getString("version.current-plugin-version-number") == null)
 		{
 			config.set("version.current-plugin-version-number", this.getDescription().getVersion());
 		}
 		if(config.getString("version.current-plugin-version-number") != this.getDescription().getVersion())
 		{
 			config.set("version.current-plugin-version-number", this.getDescription().getVersion());
 		}
 		if(config.getString("credits.thank-you-for-downloading") != "Tauryuu created this Bukkit plugin. Please visit http://embarker.me when you have time!")
 		{
			config.set("credits.thank-you-for-downloading", "Tauryuu created this Bukkit plugin. Please visit http://embarker.me when you have time!");
 		}
 	}
 
 }

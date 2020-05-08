 package net.timstans.hidemyplugins;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin implements Listener {
 	 public FileConfiguration configFile;
 	 public static Main instance;
 	
 	public void onEnable(){
 		
 		instance = this;
 		this.configFile = this.getConfig();
 		loadConfiguration();
 		PluginManager pm = this.getServer().getPluginManager();
 		Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN+"[HideMyPlugins] "
 				+ChatColor.RED+"Loaded!");
 		pm.registerEvents(new PlayerListener(), this);
 	}
 	 public void loadConfiguration(){
 		    this.getConfig().addDefault("Settings.Message", "Access Denied!");
 		    this.getConfig().addDefault("Plugin.Version", "V1.4");
 		    this.getConfig().options().copyDefaults(true); 
		    this.saveConfig()
 		    
 		    }
 
 }

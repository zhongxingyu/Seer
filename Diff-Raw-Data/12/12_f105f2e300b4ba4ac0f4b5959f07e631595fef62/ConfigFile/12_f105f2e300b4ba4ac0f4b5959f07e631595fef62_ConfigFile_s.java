 package com.skyost.auth;
 
 import java.io.File;
 import java.util.HashMap;
 
 import org.bukkit.plugin.Plugin;
 
 import com.skyost.auth.utils.Config;
 
 public class ConfigFile extends Config {
 	public ConfigFile(Plugin plugin) {
 		CONFIG_FILE = new File(plugin.getDataFolder(), "config.yml");
 		CONFIG_HEADER = "Skyauth Config";
 	}
 	
 	public HashMap<String, String> Accounts = new HashMap<String, String>();
 	public HashMap<String, Integer> Codes = new HashMap<String, Integer>();
 	
 	public int SessionLength = 7200;
 	public int MaxTry = 5;
 	
	public boolean EncryptPassword = false;
	public boolean CheckForUpdates = true;
 	
 	public String PluginFile;
 }

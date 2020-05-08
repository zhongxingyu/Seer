 package com.jseb.teleport;
 
 import org.bukkit.configuration.file.FileConfiguration;
 
 import java.io.*;
 
 public class Config {
 	public FileConfiguration config; 
 	public static String configPath;
 	public Teleport plugin;
 
 	public static int maxHomes;
 	public String projectName;
 	public boolean notifyUpdate;
 	public boolean updateEnabled;
 	public boolean cacheLanguage;
 	public String lang;
 
 	//plugin components
 	public boolean areaEnabled;
 	public boolean homeEnabled;
 	public boolean deathEnabled;
 	public boolean spawnEnabled;
 	public boolean playerTeleEnabled;
 	public boolean backEnabled;
 	public boolean bedEnabled;
 	
 	public Config(FileConfiguration config, Teleport plugin) {
 		//get fileconfig
 		this.config = config;
 		this.configPath = plugin.getDataFolder().getAbsolutePath() + File.separator + "config.yml";
 		this.plugin = plugin;
 
 		init();
 	}
 
 	public void init() {
		if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
 		File teleportConfig = new File(this.configPath);
 		if (teleportConfig.exists()) {
 			try {
 				config.load(this.configPath);
 				refreshConfig();
 			} catch (Exception e) {
 
 			}
 		} else {
 			try {
 				teleportConfig.createNewFile();
 				defaults();
 				config.save(this.configPath);
 			} catch (IOException e) {
 				System.out.println("Error creating config file.");
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void defaults() {
 		config.set("general.maxhomes", 4);
 		config.set("general.updatenotify", true);
 		config.set("general.updateenabled", false);
 		config.set("donttouch.projectname", "teleport-home");
 		config.set("language.language", "en");
 		config.set("language.cache", false);
 		config.set("components.areaenabled", true);
 		config.set("components.homeenabled", true);
 		config.set("components.deathenabled", true);
 		config.set("components.spawnenabled", true);
 		config.set("components.playerteleenabled", true);
 		config.set("components.backenabled", true);
 		config.set("components.bedenabled", true);
 
 		refreshConfig();
 	}
 
 	public boolean refreshConfig() {
 		try {
 			config.load(this.configPath);
 		} catch (Exception e) {
 			return false;
 		}
 
 		this.maxHomes = config.getInt("general.maxhomes", 4);
 		this.notifyUpdate = config.getBoolean("general.updatenotify", true);
 		this.updateEnabled = config.getBoolean("general.updateenabled", false);
 		this.projectName = config.getString("donttouch.projectname", "teleport-home");
 		this.lang = config.getString("language.language", "en");
 		this.cacheLanguage = config.getBoolean("language.cache", false);
 
 		this.areaEnabled = config.getBoolean("components.areaenabled", true);
 		this.homeEnabled = config.getBoolean("components.homeenabled", true);
 		this.deathEnabled = config.getBoolean("components.deathenabled", true);
 		this.spawnEnabled = config.getBoolean("components.spawnenabled", true);
 		this.playerTeleEnabled = config.getBoolean("components.playerteleenabled", true);
 		this.backEnabled = config.getBoolean("components.backenabled", true);
 		this.bedEnabled = config.getBoolean("components.bedenabled", true);
 
 		saveConfig();
 
 		return true;
 	} 
 
 	public boolean saveConfig() {
 		config.set("general.maxhomes", this.maxHomes);
 		config.set("general.updatenotify", this.notifyUpdate);
 		config.set("general.updateenabled", this.updateEnabled);
 		config.set("donttouch.projectname", this.projectName);
 		config.set("language.language", this.lang);
 		config.set("language.cache", this.cacheLanguage);
 		config.set("components.areaenabled", this.areaEnabled);
 		config.set("components.homeenabled", this.homeEnabled);
 		config.set("components.deathenabled", this.deathEnabled);
 		config.set("components.spawnenabled", this.spawnEnabled);
 		config.set("components.playerteleenabled", this.playerTeleEnabled);
 		config.set("components.backenabled", this.backEnabled);
 		config.set("components.bedenabled", this.bedEnabled);
 
 		try {
 			config.save(this.configPath);
 		} catch (Exception e) {
 			return false;
 		}
 
 		return true;
 	}
 }

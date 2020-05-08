 package net.kiwz.ThePlugin.utils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 
 public class Config {
 	
 	private static String host = "127.0.0.1";
 	private static String port = "3306";
 	private static String database = "theplugin";
 	private static String user = "rooty";
 	private static String password = "booty";
 	private static List<String> filters = new ArrayList<String>();
 	private static List<String> admins = new ArrayList<String>();
 	private static List<String> rules = new ArrayList<String>();
 	private static List<String> areaBlocks = new ArrayList<String>();
 	private static List<String> spotBlocks = new ArrayList<String>();
 	
 	public static String getHost() {
 		return host;
 	}
 	public static String getPort() {
 		return port;
 	}
 	public static String getDatabase() {
 		return database;
 	}
 	public static String getUser() {
 		return user;
 	}
 	public static String getPassword() {
 		return password;
 	}
 	public static List<String> getFilters() {
 		return filters;
 	}
 	public static List<String> getAdmins() {
 		return admins;
 	}
 	public static List<String> getRules() {
 		return rules;
 	}
 	public static List<String> getAreaBlocks() {
 		return areaBlocks;
 	}
 	public static List<String> getSpotBlocks() {
 		return spotBlocks;
 	}
 	
 	private String hostPath = "MySQL.Host";
 	private String portPath = "MySQL.Port";
 	private String databasePath = "MySQL.Database";
 	private String userPath = "MySQL.User";
 	private String passwordPath = "MySQL.Password";
 	private String filtersPath = "Filters";
 	private String adminsPath = "Admins";
 	private String rulesPath = "Rules";
 	private String areaBlocksPath = "Place.Spawn.AreaBlocks";
 	private String spotBlocksPath = "Place.Spawn.SpotBlocks";
 	
 	public void getConfig(Plugin plugin) {
 		File file = new File(plugin.getDataFolder(), "config.yml");
 		
 		YamlConfiguration conf = new YamlConfiguration();
 		conf.addDefault(hostPath, host);
 		conf.addDefault(portPath, port);
 		conf.addDefault(databasePath, database);
 		conf.addDefault(userPath, user);
 		conf.addDefault(passwordPath, password);
 		filters.add("Server permissions file");
 		filters.add("UUID");
 		filters.add("logged in with entity id");
 		filters.add("Disconnecting");
 		filters.add("lost connection");
 		filters.add("Connection reset");
 		filters.add("DSCT: socket closed");
 		filters.add("Reached end of stream for");
 		filters.add("moved wrongly!");
 		filters.add("issued server command:");
 		filters.add("achievement");
 		filters.add("[WorldEdit] No compatible nms block class found.");
 		conf.addDefault(filtersPath, filters);
 		admins.add("Kiwz");
 		conf.addDefault(adminsPath, admins);
 		rules.add("Regler fr nummer foran seg i chatten");
 		conf.addDefault(rulesPath, rules);
 		areaBlocks.add("ENUM navn til blokken");
 		conf.addDefault(areaBlocksPath, areaBlocks);
 		spotBlocks.add("ENUM navn til blokken");
 		conf.addDefault(spotBlocksPath, spotBlocks);
 		conf.options().copyDefaults(true);
 		
 		try { conf.load(file); } catch (IOException | InvalidConfigurationException e) { }
 		try { conf.save(file); } catch (IOException e) { }
 		
 		host = conf.getString(hostPath, host);
 		port = conf.getString(portPath, port);
 		database = conf.getString(databasePath, database);
 		user = conf.getString(userPath, user);
 		password = conf.getString(passwordPath, password);
 		filters = conf.getStringList(filtersPath);
 		admins = conf.getStringList(adminsPath);
 		rules = conf.getStringList(rulesPath);
 		areaBlocks = conf.getStringList(areaBlocksPath);
 		spotBlocks = conf.getStringList(spotBlocksPath);
 	}
 }

 package com.runetooncraft.plugins.EasyMobArmory.core;
 
 import java.io.File;
 import java.lang.reflect.Array;
 import java.util.Arrays;
 import java.util.List;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class Config {
 	private YamlConfiguration config;
 	private File configFile;
 	
 	public Config(File configfile) {
 		this.config = new YamlConfiguration();
 		this.configFile = configfile;
 	}
 	public boolean load() {
 		try {
 			if (!configFile.exists()) {
 				configFile.createNewFile();
 				loaddefaults();
 			}
 			config.load(configFile);
 			return true;
 		}
 		catch (Exception e) {
 			Messenger.severe("Config did not load correctly, returned error:\n" + e.getMessage());
 			return false;
 		}
 	}
 	private void loaddefaults() {
 		List<Integer> Helmets = Arrays.asList(298,302,306,310,314);
 		List<Integer> Chestplates = Arrays.asList(299,303,307,311,315);	
 		List<Integer> Leggings = Arrays.asList(300,304,308,312,316);
 		List<Integer> Boots = Arrays.asList(301,305,309,313,317);
 		config.addDefault("EMA.prefix", "&b[EMA]&d");
 		config.addDefault("List.Helmets", Helmets);//
 		config.addDefault("List.Chestplates", Chestplates);
 		config.addDefault("List.Leggings", Leggings);
 		config.addDefault("List.Boots", Boots);
 		config.options().copyDefaults(true);
 		save();
 	}
 	public boolean save() {
 		try {
 			config.save(configFile);
 		} catch (Exception e) { 
 			Messenger.severe("Config failed to save, returned error:\n" + e.getMessage());
 		}
 		return true;
 	}
 	public int getint(String path) {
 		return config.getInt(path);
 	}
 	public void setint(String path, int i) {
 		config.set(path, i);
 		save();
 	}
 	public void setstring(String path, String item) {
 		config.set(path, item);
 		save();
 	}
 	public List getlist(String path) {
 		return config.getList(path);
 	}
 	public void addtolist(String path, String item) {
 		List<String> l = config.getStringList(path);
 		l.add(item);
 		config.set(path, l);
 		save();
 	}
	public void ClearList(String path) {
		List<String> l = config.getStringList(path);
		l.clear();
		config.set(path, l);
		save();
	}
 	public String getstring(String path) {
 		return config.getString(path);
 	}
 	public YamlConfiguration getConfig() {
 		return config;
 		
 	}
 	public String parsestringcolors(String s) {
 		return s.replaceAll("&([0-9a-f])", "\u00A7$1");
 	}
 }

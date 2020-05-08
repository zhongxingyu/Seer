 package me.coolblinger.swordsgame;
 
 import org.bukkit.util.config.Configuration;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 public class SwordsGameConfiguration {
 	private SwordsGame plugin;
 	File configFile = new File("plugins" + File.separator + "SwordsGame" + File.separator + "config.yml");
 
 	public SwordsGameConfiguration(SwordsGame instance) {
 		plugin = instance;
 		initConfig();
 	}
 
 	public Configuration config() {
 		try {
 			Configuration config = new Configuration(configFile);
 			config.load();
 			return config;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public void initConfig() {
 		configFile.getParentFile().mkdir();
 		if (!configFile.exists()) {
 			try {
 				configFile.createNewFile();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		Configuration config = config();
		if (!config.getKeys().contains("allowCommands")) {
 			List<String> dummyList = new ArrayList<String>();
 			dummyList.add("/time");
 			dummyList.add("/help");
 
 			config.setProperty("allowCommands", dummyList);
 			config.save();
 		}
		if (!config.getKeys().contains("spawnOnKill")) {
 			config.setProperty("spawnOnKill", true);
 			config.save();
 		}
		if (!config.getKeys("ladder").contains("custom")) {
 			config.setProperty("ladder.custom", false);
 			config.save();
 		}
		if (!config.getKeys("ladder").contains("ladder")) {
 			List<Integer> dummyList = new ArrayList<Integer>();
 			dummyList.add(276);
 			dummyList.add(267);
 			dummyList.add(279);
 			dummyList.add(258);
 			dummyList.add(283);
 			dummyList.add(286);
 			dummyList.add(285);
 			dummyList.add(0);
 			config.setProperty("ladder.ladder", dummyList);
 			config.save();
 		}
		if (!config.getKeys("ladder").contains("sideItems")) {
 			List<Integer> dummyList = new ArrayList<Integer>();
 			dummyList.add(320);
 			config.setProperty("ladder.sideItems", dummyList);
 			config.save();
 		}
 	}
 
 	public List readList(String path) {
 		Configuration config = config();
 		List returnValue = config.getList(path);
 		return returnValue;
 	}
 
 	public boolean readBoolean(String path) {
 		Configuration config = config();
 		boolean returnValue = config.getBoolean(path, false);
 		return returnValue;
 	}
 }

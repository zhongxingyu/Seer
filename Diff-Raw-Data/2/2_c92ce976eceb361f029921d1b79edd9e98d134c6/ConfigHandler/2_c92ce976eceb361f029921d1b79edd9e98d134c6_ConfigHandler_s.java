 package com.ayan4m1.multiarrow;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.LinkedHashMap;
 
 import org.yaml.snakeyaml.*;
 
 import com.ayan4m1.multiarrow.arrows.ArrowType;
 
 public class ConfigHandler {
 	private MultiArrow plugin;
 	private LinkedHashMap<String, LinkedHashMap> data;
 	private final String defaultConfigFile = "requirements:\nremove-arrow:";
 
 	public int getRequiredTypeId(ArrowType type) {
 		if (data.containsKey("requirements")) {
 			LinkedHashMap<String, Integer> requirements = data.get("requirements");
 			String typeName = type.toString().toLowerCase();
			if (requirements.containsKey(typeName)) {
 				try {
 					return requirements.get(typeName);
 				} catch (Exception e) {
 					plugin.log.warning("Invalid value set in config.yml requirements!");
 					return 0;
 				}
 			} else return 0;
 		} else return 0;
 	}
 
 	public boolean getRemoveArrow(ArrowType type) {
 		if (data.containsKey("remove-arrow")) {
 			LinkedHashMap<String, Boolean> removals = data.get("remove-arrow");
 			String typeName = type.toString().toLowerCase();
 			if (removals != null && removals.containsKey(typeName)) {
 				try {
 					return removals.get(typeName);
 				} catch (Exception e) {
 					plugin.log.warning("Invalid value set in config.yml remove-arrow!");
 					return true;
 				}
 			} else return true;
 		} else return true;
 	}
 
 	private boolean createDataDirectory() {
 	    File file = this.plugin.getDataFolder();
 	    if (!file.isDirectory()){
 	        if (!file.mkdirs()) {
 	            return false;
 	        }
 	    }
 	    return true;
 	}
 
 	public ConfigHandler(MultiArrow instance) {
 		this.plugin = instance;
 		if (this.createDataDirectory()) {
 			Yaml yaml = new Yaml();
 			File configFile = new File(this.plugin.getDataFolder().getAbsolutePath() + File.separator + "config.yml");
 			try {
 				if (!configFile.exists()) {
 					this.plugin.log.info("MultiArrow created new config.yml");
 					configFile.createNewFile();
 					if (configFile.canWrite()) {
 						FileOutputStream fo = new FileOutputStream(configFile);
 						fo.write(defaultConfigFile.getBytes());
 						fo.flush();
 						fo.close();
 					}
 				}
 				FileInputStream fs = new FileInputStream(configFile);
 				this.data = (LinkedHashMap<String, LinkedHashMap>)yaml.load(fs);
 				if (this.data == null) {
 					this.plugin.log.warning("MultiArrow could not load config.yml");
 				}
 			} catch (IOException e) {
 				this.plugin.log.warning("IOException reading MultiArrow/config.yml + (" + e.getMessage() + ")");
 			}
 		} else {
 			this.plugin.log.warning("MultiArrow could not create a plugin directory!");
 			this.plugin.log.info("MultiArrow continuing with no config...");
 		}
 	}
 }

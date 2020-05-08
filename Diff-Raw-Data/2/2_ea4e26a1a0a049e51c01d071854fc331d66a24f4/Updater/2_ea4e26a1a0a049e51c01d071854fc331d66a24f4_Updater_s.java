 package com.gmail.snipsrevival;
 
 import java.io.File;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class Updater {
 	
 	AdminAid plugin;
 	
 	public Updater(AdminAid plugin) {
 		this.plugin = plugin;
 	}
 	
 	public void updateConfig() {
 		
 		String currentVersion = plugin.getDescription().getVersion();
 		String configVersion = plugin.getConfig().getString("Version");
 		if(!currentVersion.equalsIgnoreCase(configVersion)) {
 			if(plugin.getConfig().getBoolean("AutoUpdateConfig") == true) {
 				
 				Map<String, Object> keyValuePairs = new HashMap<String, Object>();
 				
 				for(String key : plugin.getConfig().getKeys(true)) {
 					keyValuePairs.put(key, plugin.getConfig().get(key));
 				}
 						
 				File configFile = new File(plugin.getDataFolder() + "/config.yml");
 				configFile.delete();
 				
 				plugin.getConfig().options().copyDefaults(true);
 	
 				for(String key : keyValuePairs.keySet()) {
 					plugin.getConfig().set(key, keyValuePairs.get(key));
 				}
 				plugin.getConfig().set("Version", currentVersion);
 				plugin.saveConfig();
 				
 				plugin.getLogger().info("Configuration file was outdated");
 				plugin.getLogger().info("Missing configuration keys have now been added!");
 			}
 			else {
 				plugin.getLogger().warning("The configuration file is not up to date!");
 			}
 		}
 	}
 	
 	public void performVersionCheck() {
 		if(plugin.getConfig().getBoolean("EnableVersionChecker") == true) {
			if(isLatest()) {
 				plugin.getLogger().warning("There is a newer version of AdminAid available");
 				plugin.getLogger().warning("Download it at " + getDownloadLink());
 			}
 			else {
 				plugin.getLogger().info("You have the latest version of AdminAid!");
 			}
 		}
 	}
 	
 	public boolean isLatest() {
 		plugin.getLogger().info("Checking for newer versions...");
 		try {
 			InputStream input = new URL("http://dev.bukkit.org/bukkit-plugins/adminaid/files.rss").openConnection().getInputStream();
 			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
 			Node latestFile = document.getElementsByTagName("item").item(0);
 			NodeList children = latestFile.getChildNodes();
 			String[] updateVersion = children.item(1).getTextContent().replaceAll("[a-zA-Z ]", "").split("\\.");
 			int updateMajorRelease = Integer.parseInt(updateVersion[0]);
 			int updateMinorRelease = Integer.parseInt(updateVersion[1]);
 			int updateBuild = Integer.parseInt(updateVersion[2]);
 	
 			PluginDescriptionFile pdf = plugin.getDescription();
 			String[] currentVersion = pdf.getVersion().split("\\.");
 			int currentMajorRelease = Integer.parseInt(currentVersion[0]);
 			int currentMinorRelease = Integer.parseInt(currentVersion[1]);
 			int currentBuild = Integer.parseInt(currentVersion[2]);
 			
 			if(updateMajorRelease > currentMajorRelease) return false;
 			else {
 				if((updateMinorRelease > currentMinorRelease) && updateMajorRelease == currentMajorRelease) return false;
 				else {
 					if((updateBuild > currentBuild) && updateMinorRelease == currentMinorRelease) return false;
 					else return true;
 				}
 			}
 		}
 		catch (Exception e) {
 			plugin.getLogger().warning("Something is wrong with the version checker. This can probably be ignored");
 		}
 		return true;
 	}
 	
 	public String getDownloadLink() {
 		try {
 			InputStream input = new URL("http://dev.bukkit.org/bukkit-plugins/adminaid/files.rss").openConnection().getInputStream();
 			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
 			Node latestFile = document.getElementsByTagName("item").item(0);
 			NodeList children = latestFile.getChildNodes();
 			String updateLink = children.item(3).getTextContent();
 			return updateLink;
 		}
 		catch (Exception e) {
 			return "http://dev.bukkit.org/server-mods/adminaid/";
 		}
 	}
 }

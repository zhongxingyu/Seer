 package com.mike724.email;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.Set;
 
 import org.bukkit.configuration.file.FileConfiguration;
 
 public class EmailManager {
 	
 	private ConfigAccessor configA;
 	private FileConfiguration config;
 	private String root = "emails.";
 	private Email plugin;
 
 	public EmailManager(Email plugin) {
 		this.plugin = plugin;
 		configA = new ConfigAccessor(plugin, "emails.yml");
 		config = configA.getConfig();
 	}
 	
 	public void setPlayerEmail(String name, String email) {
 		config.set(root+name, email);
 		configA.saveConfig();
 	}
 	
 	public String getPlayerEmail(String name) {
 		return config.getString(root+name);
 	}
 	
 	public void removePlayerEmail(String name) {
 		config.set(root+name, null);
 		configA.saveConfig();
 	}
 	
 	public void export(int type) {
		if(!(type == 1 || type == 2)) {
			plugin.getLogger().info("Incorrect export type");
 			return;
 		}
 		File file = new File(plugin.getDataFolder(), "export-type1.txt");
 		try {
 			PrintWriter pw = new PrintWriter(new FileWriter(file));
 			Set<String> keys = config.getConfigurationSection("emails").getKeys(false);
 			for(String key : keys) {
 				String line = "";
 				if(type == 1) {
 					line = key+","+config.getString(root+key);
 				} else if(type == 2) {
 					line = config.getString(root+key);
 				}
 				pw.println(line);
 			}
 			pw.close();
 			plugin.getLogger().info("Export file created at "+file.getPath());
 		} catch (Exception e) {
 			plugin.getLogger().severe("Could not export emails");
 			e.printStackTrace();
 			return;
 		}
 	}
 	
 }

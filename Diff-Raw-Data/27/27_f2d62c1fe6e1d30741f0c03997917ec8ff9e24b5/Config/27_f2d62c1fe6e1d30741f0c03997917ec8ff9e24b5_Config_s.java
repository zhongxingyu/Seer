 package me.cmastudios.plugins.WarhubModChat.util;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.bukkit.configuration.file.FileConfiguration;
 
 public class Config {
 	public static FileConfiguration config;
 	static String mainDirectory = "plugins/WHChat";
 	public static File file = new File(mainDirectory + File.separator + "config.yml");
 	public static void setup(FileConfiguration config) {
 		new File(mainDirectory).mkdir();
 		Config.config = config;
 		if (config.get("modchat-format")==null)config.set("modchat-format", "&2Modchat> &a%player: %message");
 		if (config.get("alert-format")==null)config.set("alert-format", "&4Attention> &c%message");
 		if (config.get("say-format")==null)config.set("say-format", "&5Server> &d%message");
 		if (config.get("mysql.host")==null)config.set("mysql.host", "cmastudios.me");
 		if (config.get("mysql.port")==null)config.set("mysql.port", "3306");
 		if (config.get("mysql.username")==null)config.set("mysql.username", "warhub");
 		if (config.get("mysql.password")==null)config.set("mysql.password", "invalid");
 		if (config.get("mysql.database")==null)config.set("mysql.database", "warhub");
		if (config.get("blockspersecond")==null)config.set("blockspersecond", 10);
 		try {
 			config.save(file);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 		
 		
 	
 }

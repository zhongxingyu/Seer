 package me.cmastudios.plugins.WarhubModChat.util;
 
 import java.io.File;
import java.io.IOException;
 
 import org.bukkit.configuration.file.FileConfiguration;
 
 public class Config {
 	public static FileConfiguration config;
	static String mainDirectory = "plugins/WHChat";
 	static File file = new File(mainDirectory + File.separator + "config.yml");
 	public static void setup(FileConfiguration config) {
 		new File(mainDirectory).mkdir();
 		Config.config = config;
		if (config.get("modchat-format")==null)config.set("modchat-format", "&2Modchat> &a%player: %message");
		if (config.get("alert-format")==null)config.set("alert-format", "&4Attention> &c%message");
		if (config.get("say-format")==null)config.set("say-format", "&5Server> &d%message");
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
 		}
	}
 		
 		
 	
 }

 package me.cmastudios.plugins.WarhubModChat.util;
 
 import java.io.File;
 
 import org.bukkit.configuration.file.FileConfiguration;
 
 public class Config {
 	public static FileConfiguration config;
	static String mainDirectory = "plugins/WarhubModChat";
 	static File file = new File(mainDirectory + File.separator + "config.yml");
 	public static void setup(FileConfiguration config) {
 		new File(mainDirectory).mkdir();
 		Config.config = config;
		/*if(!file.exists()){
		            try {
		                file.createNewFile();
		                write("modchat-format", "&2Modchat> &a%player: %message");
		                write("alert-format", "&4Attention> &c%message");
		                write("say-format", "&5Server> &d%message");
		        		Configuration CONFIG = load();
		        		CONFIG.setHeader(
		        				"# Auto-generated config for WarhubModChat",
		        				"#");
		        		CONFIG.save();
		            } catch (Exception ex) {
		                ex.printStackTrace();
		            }*/
 		}
 		
 		
 	
 }

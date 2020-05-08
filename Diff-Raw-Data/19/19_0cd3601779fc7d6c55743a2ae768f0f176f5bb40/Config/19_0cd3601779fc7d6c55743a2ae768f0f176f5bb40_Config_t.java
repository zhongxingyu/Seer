 package de.javakara.manf.mcdefrag;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 
 import de.javakara.manf.util.LanguageComplete;
 
 @LanguageComplete
 public final class Config {
 	private static FileConfiguration config;
 	private static File configFile;
 	
 	public static boolean initialise(FileConfiguration config,File dataFolder) throws IOException{
 		Config.config = config;
 		Config.configFile = new File(dataFolder + File.separator + "config.yml");
 		if (!config.isSet("defrag.version")){
 			config.set("default-time",60L);
 			config.set("highscores.max", 3);
 			config.set("broadcasttype",1);
 			config.set("broadcast-command","/say <[p]> bla -> wird ausgefhrt wenn broadcasttype = 0");
 			config.set("sendtime",10);
 			List<String> worlds = new ArrayList<String>();
 			worlds.add("world");
 			config.set("worlds", worlds);
			config.set("default-color", "&6");
 			List<String> colors = new ArrayList<String>();
 			colors.add("&1");colors.add("&2");colors.add("&3");
 			colors.add("&4");colors.add("&5");colors.add("&6");
 			colors.add("&7");colors.add("&8");colors.add("&9");
 			config.set("table.colors",colors);
 			save();
 		}
 		return true;		
 	}
 	
 	public static int getInt(String node){
 		return config.getInt(node);
 	}
 	
 	public static String getString(String node){
 		if(!config.isSet(node)){
 			config.set(node, "asdf");
 			try {
 				save();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return config.getString(node);
 	}
 
 	public static boolean getBoolean(String node) {
 		return config.getBoolean(node);
 	}
 	
 	public static double getDouble(String node) {
 		return config.getDouble(node);
 	}
 	
 	public static List<String> getStringList(String node){
 		return config.getStringList(node);
 	}
 	
 	public static Long getLong(String node) {
 		return config.getLong(node);
 	}
 	
 	public static String getPath() {
 		return configFile.getParent();
 	}
 		
 	public static void load() throws FileNotFoundException, IOException, InvalidConfigurationException {
 		config.load(configFile);
 	}
 	
 	public static void save() throws IOException {
 		config.set("defrag.version", 1);
 		config.save(configFile);
 	}
 
 	
 }

 package org.spoutcraft.launcher;
 
 import java.io.File;
 
 import org.bukkit.util.config.Configuration;
 
 public class MinecraftYML {
 	private static final String			MINECRAFT_YML	= "minecraft.yml";
 	private static volatile boolean	updated				= false;
 	private static String						latest				= null;
 	private static String						recommended		= null;
 	private static final Object			key						= new Object();
 	private static Configuration		config				= null;
 	private static File							configFile		= null;
 
 	public static Configuration getMinecraftYML() {
 		updateMinecraftYMLCache();
 		return getConfig();
 	}
 
 	public static File getConfigFile() {
 		return new File(GameUpdater.modpackDir, MINECRAFT_YML);
 	}
 
 	public static Configuration getConfig() {
 		File currentConfigFile = getConfigFile();
 		if (config == null || configFile.compareTo(currentConfigFile) != 0) {
 			configFile = currentConfigFile;
 			config = new Configuration(configFile);
 			config.load();
 		}
 		return config;
 	}
 
 	public static String getLatestMinecraftVersion() {
 		updateMinecraftYMLCache();
 		return latest;
 	}
 
 	public static String getRecommendedMinecraftVersion() {
 		updateMinecraftYMLCache();
 		return recommended;
 	}
 
 	public static void setInstalledVersion(String version) {
 		Configuration config = getMinecraftYML();
 		config.setProperty("current", version);
 		config.save();
 	}
 
 	public static String getInstalledVersion() {
 		Configuration config = getMinecraftYML();
 		return config.getString("current");
 	}
 
 	public static void updateMinecraftYMLCache() {
 		if (!updated || !getConfigFile().exists()) {
 			synchronized (key) {
 				String current = null;
 				if (getConfigFile().exists()) {
 					try {
 						current = getConfig().getString("current");
 					} catch (Exception ex) {
 						ex.printStackTrace();
 					}
 				}
 
 				if (YmlUtils.downloadYmlFile(MINECRAFT_YML, "http://technic.freeworldsgaming.com/minecraft.yml", getConfigFile())) {
					// GameUpdater.copy(getConfigFile(), output)
					config = null;
 					Configuration config = getConfig();
 					latest = config.getString("latest");
 					recommended = config.getString("recommended");
 					if (current != null) {
 						config.setProperty("current", current);
 						config.save();
 					}
 				}
 				updated = true;
 			}
 		}
 	}
 }

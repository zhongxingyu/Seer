 package vazkii.chickenshed.handler;
 import java.io.File;
 import net.minecraftforge.common.ConfigCategory;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.Property;
 
 public class ConfigurationHandler {
 	
 	private static Configuration config;
 	
 	public static boolean 	masterDisable,
 							chicksDropFeathers,
 							timedWithEgg,
 							modifyDeathDrops;
 	
 	public static int configurableTimeForEachFeather;
	public static int minimum = 6000;
	public static int AVG_TIME_FOR_EACH_FEATHER = minimum + configurableTimeForEachFeather;
 	
 	public static void initConfig(File configFile) {
 		config = new Configuration(configFile);
 		
 		
 		//Load the Configuration File
 		config.load();
 				
 				// Master switch to disable the mod
 				masterDisable = config.get(Configuration.CATEGORY_GENERAL, "masterDisable", false, "Master switch to disable the mod. The default is false. Set to true to disable the mod.").getBoolean(false);
 				
 				// Do baby chickens drop feathers?
 				chicksDropFeathers = config.get(Configuration.CATEGORY_GENERAL, "chicksDropFeathers", true, "Do baby chickens drop feathers? The default is true. Set to false to make them drop nothing at all.").getBoolean(true);
 				
 				// Is the feather drop timed with making an egg?
 				timedWithEgg = config.get(Configuration.CATEGORY_GENERAL, "timedWithEgg", false, "Is the feather drop timed with making an egg? The default is false. Set to true if you wish for feathers to be shed at the moment eggs are laid.").getBoolean(false);
 				
 				// Modify feather drops on death?
 				modifyDeathDrops = config.get(Configuration.CATEGORY_GENERAL, "modifyDeathDrops", true, "Modify feather drops on death? Default is true. Set to false if you wish for chicken drops to be unaffected by the mod.").getBoolean(true);
 				
 				// Average time between each feather drop, if the drops
 				// aren't timed with the eggs (minimum is 6000, or about
 				// twice as frequent as eggs)
 				configurableTimeForEachFeather = config.get(Configuration.CATEGORY_GENERAL, "configurableTimeForEachFeather", 26000, "Changing this value determines how long it takes for feathers to drop. 6000 is added, and is thus the minimum. Default is 26000 (default total is 32000)").getInt(26000);
 				
				
 		//Save the configuration file		
 		config.save();
 	}
 }

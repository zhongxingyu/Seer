 package sadmean.mc.scubaKit;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 /**
  * @version 1.0.0
  * @author Choelian
  *	ScubaKit allows players wearing a specified block on their head to 
  *	stay underwater for a different (probably larger) chunk of time.
  *
  */
 public class ScubaKit extends JavaPlugin {
 	
 	static String mainDirectory = "plugins/ScubaKit"; //plugin directory
 	
 	private final ScubaPlayerListener playerListener = new ScubaPlayerListener(this); //the player listener.
 	//private final ScubaServerListener serverListener = new ScubaServerListener(this); //server listener. Not needed?
 	
 	static public File configFile = new File(mainDirectory + File.separator + "config.yml"); //location of configfile. 
 	
 	public static int pumpkinAir = 2025; // default value for maxAir (air after putting on the scuba helm
 	public static int defaultAir = 320; //default value for defaultAir (air when not wearing the scuba helm
 	public static int goldAir = defaultAir;
 	public static int diamondAir = defaultAir;
 	public static int ironAir = defaultAir;
 	public static int chainAir = defaultAir;
 	public static int leatherAir = defaultAir;
 	//THESE VALUES SHOULD BE OVERWRITTEN BY CONFIG.YML
 	
     private static ScubaKit thisPlugin = null; //I don't know what this does. Necessary for fancy log
     
 	
 	public static Logger log = Logger.getLogger("Minecraft"); //logger object. can be written to directly with "log.info("herp derp")
     
     public static ScubaKit getThisPlugin() { //I do not know. Needed for fancy log
         return thisPlugin; 
     }
 
     private static void setThisPlugin(final ScubaKit thisPlugin) //also need for fancy log and other things
     {
         ScubaKit.thisPlugin = thisPlugin;
     }
 	
 
     public void onLoad() //onLoad is called the instant this plugin is touched.
     {
         setThisPlugin(this); //not 100% sure
     }
 	    
 	public void onEnable(){  //onEnable is called after onLoad
 		PluginManager pm = this.getServer().getPluginManager(); //register this plugin
 		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Normal, this); //register our playerListener
 		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this); //register our serverListener (not needed)?
 		log_It("info", "Enabled started");
 		new File(mainDirectory).mkdir();  //makes our directory if needed
 		if(!configFile.exists()){ //if your config does not exist then ...
 	         try { 
 	             configFile.createNewFile(); //... we create it then ...
 
 	     		Configuration configYAML = getThisPlugin().getConfiguration(); //... load the blank new file ...
 	     		configYAML.setProperty("scubaValues.defaultAir", defaultAir); //..set defaultAir
 	     		configYAML.setProperty("scubaValues.pumpkinAir", pumpkinAir); //... then set some values`
 	     		configYAML.setProperty("scubaValues.diamondAir", defaultAir);
 	     		configYAML.setProperty("scubaValues.goldAir", defaultAir);
 	     		configYAML.setProperty("scubaValues.ironAir", defaultAir);
 	     		configYAML.setProperty("scubaValues.leatherAir", defaultAir);
 	     		configYAML.setProperty("scubaValues.chainAir", defaultAir);
 	     		if(!configYAML.save()) { //attempt to save, if fails then
 	     			log_It("severe", "Attempted to save config.yml, got saving error!"); //IT FAILED!
 	     		}
 	         } catch (IOException ex) { 
 	             ex.printStackTrace(); //not needed anymore probably
 	         }
 	 
 		} else { 
 			//it does exist?
 		}
 		//start setting values
 		Configuration configYAML = getThisPlugin().getConfiguration();
 		configYAML.load();
		defaultAir = configYAML.getInt("scubaValues.defaultAir", 0) + 1;
		pumpkinAir = configYAML.getInt("scubaValues.pumpkinAir", 0) + 1;
		diamondAir = configYAML.getInt("scubaValues.diamondAir", 0) + 1;
		goldAir = configYAML.getInt("scubaValues.goldAir", 0) + 1;
		ironAir = configYAML.getInt("scubaValues.ironAir", 0) + 1;
		leatherAir = configYAML.getInt("scubaValues.leatherAir", 0) + 1;
		chainAir = configYAML.getInt("scubaValues.chainAir", 0) + 1;
 		
 		if(pumpkinAir == 0 && defaultAir == 0 && goldAir == 0 && chainAir == 0 && leatherAir == 0 && diamondAir == 0) {
 			log_It("severe", "All values reported as zero, this should never happen"); 
 			//if all values are zero, then the config file is 1 of 3 things
 			//1. Blank, maybe the save failed when we tried to create it?
 			//2. Does not exist, but why didn't we create it earlier in onEnable()?
 			//3. Values were manually set to Zero in the config. I have no idea why you would do that, but you could
 		}
 		
 	} 
 	public void onDisable(){ 
 		log_It("info", "Disabled Completed"); //log us not doing anything. 
 	}
 	
 	/**
 	 * @param maxAir
 	 * @param defaultAir
 	 * @param player
 	 */
 	public static void setAir(Player player) {
 		//Checks to see what the player is wearing, then adjusts their lungs accordingly.
 		ItemStack helm;
 		PlayerInventory armor;
 		
 		armor = player.getInventory();
 		helm = armor.getHelmet();
 		
 		switch (helm.getTypeId()) {
 			case 86: //pumpkin head
 				player.setMaximumAir(pumpkinAir);
 				log_It("finest", "set max air to pumpkin levels");
 				break;
 			case 310: //diamond helm 
 				player.setMaximumAir(diamondAir);
 				log_It("finest", "set max air to diamond levels");
 				break;
 			case 314: //gold helm
 				player.setMaximumAir(goldAir);
 				log_It("finest", "set max air to gold levels");
 				break;
 			case 306: //iron helm
 				player.setMaximumAir(ironAir);
 				log_It("finest", "set max air to iron levels");
 				break;
 			case 298: //leather helm
 				player.setMaximumAir(leatherAir);
 				log_It("finest", "set max air to leather levels");
 				break;
 			case 302: //chain helm
 				player.setMaximumAir(chainAir);
 				log_It("finest", "set max air to chain levels");
 				break;
 			default: //not a helm
 				player.setMaximumAir(defaultAir);
 				ScubaKit.log_It("finest", "set air to default"); 
 				break;
 		}
 		
 		//set max air
 		//if (helm.getTypeId() == 86) {
 			//if helm is pumpkin and max air is not herp make it derp
 		//	player.setMaximumAir(maxAir);
 		//	ScubaKit.log_It("finer", "Set modded Max Air to " + player.getName());
 	//	} else {
 		//	player.setMaximumAir(defaultAir);
 		//	ScubaKit.log_It("finest", "Did not set modded Max Air to " + player.getName());
 		//}
 		
 	}
 	
 	/**
 	 * @param message
 	 */
 	public static void log_It(String message) {
 		//converts 1 string log_it to a 2 string log it. Fixes leftovers.
 		String level = "undefined";
 		log_It("warning", "this message's priority was not properly set");
 		log_It(level, message);
 	}
 	
 	/**
 	 * @param level
 	 * @param message
 	 */
 	public static void log_It(String level, String message) {
 		PluginDescriptionFile thisYAML = getThisPlugin().getDescription();
 		String pluginName = thisYAML.getName();
 		String pluginVersion = thisYAML.getVersion();
 		String fullName = "[" + pluginName + "][" + pluginVersion + "] ";
 		//convert our level into an int for logging
 		int level_int = 0;
 		
 		if(level == "finest") level_int = 0;
 		if(level == "finer") level_int = 1;
 		if(level == "fine") level_int = 2;
 		if(level == "info") level_int = 3;
 		if(level == "warning") level_int = 4;
 		if(level == "severe") level_int = 5;
 		if(level == "undefined") level_int = 6;
 		
 	
 		switch (level_int) {
 		case 0: log.finest(fullName + message); break; //for people who like logs in the hexabytes
 		case 1: log.finer(fullName + message); break; //for people who like log file sizes in the petabytes
 		case 2: log.fine(fullName + message); break; //for people who like log file sizes in the terabytes
 		case 3: log.info(fullName + message); break; //for people who like log file sizes in the gigabytes
 		case 4: log.warning(fullName + message); break; //for people who like log file sizes in the megabytes
 		case 5: log.severe(fullName + message); break; //for people who like log file sizes in the kilobytes
 		case 6: log.warning(fullName + message); break; //for me, 'cause I forgot to specify what level of logging
 		default: log.warning(fullName + "warning defaulted, maybe a typo: " + message); //also for me, because I spelled the logging level wrong
 			break;
 		}
 	}
 
 }

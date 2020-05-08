 package com.sadmean.mc.ScubaKit;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.Plugin;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import com.sadmean.mc.ScubaKit.config.UpdateConfigFile;
 
 
 
 /**
  * @version 1.0.0
  * @author Choelian
  *	ScubaKit allows players wearing a specified block on their head to 
  *	stay underwater for a different (probably larger) chunk of time.
  * 
  * @TODO 
  * 	HIGH PRIORITY:
  *  	Allow config of higher default air to override lower air provided by helms
  *  LOW PRIORITY:
  *  	Recharge time for scubagear
  *  	Consumables? maybe.
  *  
  */
 public class ScubaKit extends JavaPlugin {
 	
 	static String mainDirectory = "plugins/ScubaKit"; //plugin directory
 	
 	private final ScubaPlayerListener playerListener = new ScubaPlayerListener(this); //the player listener.
 		
 	static public File configFile = new File(mainDirectory + File.separator + "config.yml"); //location of configfile. 
 	
 	//default settings
 	public static int pumpkinAir = 2025; // default value for maxAir (air after putting on the scuba helm
 	public static int defaultAir = 320; //default value for defaultAir (air when not wearing the scuba helm
 	public static int goldAir = defaultAir;
 	public static int diamondAir = defaultAir;
 	public static int ironAir = defaultAir;
 	public static int chainAir = defaultAir;
 	public static int leatherAir = defaultAir;
 	public static boolean ignorePermissions = true;
 	public static boolean complexPermissions = false;
 	public static boolean airOverridesIfHigher = true;
 	public static int configVersion = 0;
 	//THESE VALUES SHOULD BE OVERWRITTEN BY CONFIG.YML
 	
 	public static int latestConfigVersion = 3;
 	
 	int theFan = -1;
 	int shit;
 	
 	public static PermissionHandler permissionHandler; //permissions handler
 	
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
 	        	log_It("info", "No config defected. Attempting to create.");
 	        	configFile.createNewFile(); //... we create it then ...
 	         } catch (IOException ex) { 
 	             ex.printStackTrace(); //not needed anymore probably
 	         }
 	 
 		} else { 
 			//it does exist?
 		}
 		//start setting values
 		UpdateConfigFile.load();
 		
 		setupPermissions(); //set up permissions yah!!!
 			
 		if(pumpkinAir == 0 && defaultAir == 0 && goldAir == 0 && chainAir == 0 && leatherAir == 0 && diamondAir == 0) {
 			log_It("severe", "All values reported as zero, this should never happen"); 
 			//if all values are zero, then the config file is 1 of 3 things
 			//1. Blank, maybe the save failed when we tried to create it?
 			//2. Does not exist, but why didn't we create it earlier in onEnable()?
 			//3. Values were manually set to Zero in the config. I have no idea why you would do that, but you could
 		}
 		
 		shit = getThisPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(getThisPlugin(), new Runnable() {
 
 		    public void run() {
 		    	log_It("fine", "ScubaTask is on the case!");
 		        int worldNumber = 0;
 		        int playerNumber = 0;
 		        int airLeft;
 		        Player player;
 		        List<World> worlds = getServer().getWorlds();
 				while (worldNumber < worlds.size()) {
 					List<Player> playerList = worlds.get(worldNumber).getPlayers();
 						while (playerNumber < playerList.size()) {
 							player = playerList.get(playerNumber);
 							if (player.getMaximumAir() != player.getRemainingAir()) {
 								//Air Check and message send
 								airLeft = player.getRemainingAir();
 								airLeft = airLeft / 20;
 								if (airLeft == 0) {
 									player.sendMessage(ChatColor.DARK_AQUA + "[ScubaKit] " + ChatColor.RED + "YOU ARE OUT OF AIR!"); break;
 								} else {
 									player.sendMessage(ChatColor.DARK_AQUA + "[ScubaKit] " + ChatColor.GRAY + Integer.toString(airLeft) + " seconds of air remaining");	
 								}
 							}
 							playerNumber++;
 						}
 						worldNumber++;
 						playerNumber = 0;
 					}
 		        
 		    }
 		}, 60L, 300L);
 
 		if (shit == theFan) {
 			log_It("severe", "Schedule failed. This is really bad");
 		}
 	} 
 	
 	public void onDisable(){  
 		if (getThisPlugin().getServer().getScheduler().isCurrentlyRunning(shit)) {
 			getThisPlugin().getServer().getScheduler().cancelTask(shit);
 			log_It("info", "Disabled Completed"); 
 		} else {
 			log_It("info", "Disabled Completed"); //log us not doing anything.
 			//log_It("warning", "Disabled Completed, but with errors");
 		}
 	}
 	
 	private void setupPermissions() {
 		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
 			if (this.permissionHandler == null) {
 				if (permissionsPlugin != null) {
 					this.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
 				} else {
 					log_It("info", "Permission system not detected, ignorePermissions has be set to true");
 					ignorePermissions = true;
 				}
 			}
 		}
 
 	
 	/**
 	 * @param maxAir
 	 * @param defaultAir
 	 * @param player
 	 */
 	public static void setAir(Player player) {
 		//lets start with a permissions check
 		if (ignorePermissions || (!complexPermissions && permissionHandler.has(player, "ScubaKit.ScubaGear"))) {
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
 		} else {
 			//permissions is turned on. check permission of each helm type before setting
 			//Checks to see what the player is wearing, then adjusts their lungs accordingly.
 			ItemStack helm;
 			PlayerInventory armor;
 			
 			armor = player.getInventory();
 			helm = armor.getHelmet();
 			
 			switch (helm.getTypeId()) {
 				case 86: //pumpkin head
 					if (permissionHandler.has(player, "ScubaKit.ScubaGear.Pumpkin")) player.setMaximumAir(pumpkinAir);
 					log_It("finest", "set max air to pumpkin levels");
 					break;
 				case 310: //diamond helm 
 					if (permissionHandler.has(player, "ScubaKit.ScubaGear.Diamond")) player.setMaximumAir(diamondAir);
 					log_It("finest", "set max air to diamond levels");
 					break;
 				case 314: //gold helm
 					if (permissionHandler.has(player, "ScubaKit.ScubaGear.Gold")) player.setMaximumAir(goldAir);
 					log_It("finest", "set max air to gold levels");
 					break;
 				case 306: //iron helm
 					if (permissionHandler.has(player, "ScubaKit.ScubaGear.Iron")) player.setMaximumAir(ironAir);
 					log_It("finest", "set max air to iron levels");
 					break;
 				case 298: //leather helm
 					if (permissionHandler.has(player, "ScubaKit.ScubaGear.Leather")) player.setMaximumAir(leatherAir);
 					log_It("finest", "set max air to leather levels");
 					break;
 				case 302: //chain helm
 					if (permissionHandler.has(player, "ScubaKit.ScubaGear.Chain")) player.setMaximumAir(chainAir);
 					log_It("finest", "set max air to chain levels");
 					break;
 				default: //not a helm
 					player.setMaximumAir(defaultAir);
					ScubaKit.log_It("finest", "set air to default"); 
 					break;
 			}
 						
 		}
 
 		
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
 		int level_int = 6;
 		
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

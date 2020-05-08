 package net.worldoftomorrow.nala.ni;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 
 
 public class Configuration {
 	
 	private Log log = new Log();
 	private Plugin plugin = null;
 	Configuration(NoItem plugin){
 		this.plugin = plugin;
 	}
 	
 	//Define the configuration name//
 	private String config = "config.yml";
 	
 	private OutputStream os = null;
 	private File configFile = null;
 	private PrintWriter writer = null;
 	private static FileConfiguration conf = new YamlConfiguration();
 	private boolean loaded = false;
 	
 	//--------CONFIG DEFAULTS--------//
 	private boolean eNotifyPlayer = true;
 	private boolean eNotifyAdmins = true;
 	private boolean eStopToolUse = true;
 	private boolean eNotifyNoUse = true;
 	private boolean eNotifyNoBrew = true;
 	private boolean eNotifyNoHold = true;
 	private boolean eNotifyNoWear = true;
 	
 	private boolean eStopCrafting = true;
 	private boolean eStopItemPickup = true;
 	private boolean eStopPotionBrew = true;
 	private boolean eStopItemHold = true;
 	private boolean eStopWear = true;
 	
 	private boolean ePerItemPermissions = true;
 	private boolean eDebugging = false;
 	private String ePluginChannel = "main";
 	
 	private String ePlayerMessage = "This item (%i) is not allowed.";
 	private String eAdminMessage = "Player %n tried to get item %i @ %x, %y, %z in the world %w.";
 	private String eNoUseMessage = "You are not allowed to use this tool!";
 	private String eNoBrewMessage = "You are not allowed to brew that potion!";
 	private String eNoHoldMessage = "You are not allowed to hold that!";
 	private String eNoWearMessage = "You are not allowed to wear that!";
 	
 	private List<String> eDisallowedItems = new ArrayList<String>();
 	private List<String> eDisallowedPotionRecipes = new ArrayList<String>();
 	
 	private int configVersion = 5;
 	
 	//----METHODS----//
 	public void load(){
 		if(!loaded){
 			
 			this.makePluginDir();
 			if(!this.confExists()){
 				this.makeConfig();
 			}
 			int ccv = plugin.getConfig().getInt("ConfigurationVersion");
 			if(ccv < configVersion){
 				
 				//Update the configuration if it is old.
 				if(ccv != 0){ //This check is to see if the configuration is blank, which would be bad to copy the options from.
 					this.copyConfigOptions();
 				}
 				try {
 					os = new FileOutputStream(new File(plugin.getDataFolder(), config));
 					writer = new PrintWriter(os);
 					this.writeConfig(writer);
 					
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				}
 			}
 			configFile = new File(plugin.getDataFolder(), config);
 			conf = YamlConfiguration.loadConfiguration(configFile);
 			loaded = true;
 		} else {
 			configFile = new File(plugin.getDataFolder(), config);
 			conf = YamlConfiguration.loadConfiguration(configFile);
 			loaded = true;
 		}
 	}
 	
 	private void makePluginDir(){
 		if(!plugin.getDataFolder().exists()){
 			plugin.getDataFolder().mkdirs();
 		}
 	}
 	
 	private void makeConfig(){
 		File conf = new File(plugin.getDataFolder(), config);
 		if(!conf.exists()){
 			try {
 				conf.createNewFile();
 				log.log("Empty configuration file has been created.");
 			} catch (IOException e) {
 				log.log("IOException: Could not create configuration..", e);
 			}
 		}
 	}
 	
 	private boolean confExists(){
 		File conf = new File(plugin.getDataFolder(), config);
 		return conf.exists();
 	}
 	
 	private void writeConfig(PrintWriter writer){
 		if(writer != null){
 			//----WRITE THE CONFIGURATION ONE LINE AT A TIME----//
 			writer.println("# Notify Message Variables:");
 			writer.println("# %n = player name");
 			writer.println("# %i = item id");
 			writer.println("# %x = X location");
 			writer.println("# %y = Y location");
 			writer.println("# %z = Z location");
 			writer.println("# %w = world");
 			writer.println("Notify:");
 			writer.println("    Player: " + eNotifyPlayer);
 			writer.println("    PlayerMessage: " + ePlayerMessage);
 			writer.println("    Admins: " + eNotifyAdmins);
 			writer.println("    AdminMessage: " + eAdminMessage);
 			writer.println("    NoUse: " + eNotifyNoUse);
 			writer.println("    NoUseMessage: " + eNoUseMessage);
 			writer.println("    NoBrew: " + eNotifyNoBrew);
 			writer.println("    NoBrewMessage: " + eNoBrewMessage);
 			writer.println("    NoHold: " + eNotifyNoHold);
 			writer.println("    NoHoldMessage: " + eNoHoldMessage);
 			writer.println("    NoWear: " + eNotifyNoWear);
			writer.println("    NoWearMessage:" + eNoWearMessage);
 			writer.println("");
 			writer.println("# Blocked items list ( itemID:DamageValue )    ");
 			writer.println("DisallowedItems:");
 			if(eDisallowedItems.isEmpty()){
 				writer.println("    - '5'");
 				writer.println("    - '5:1'");
 				writer.println("    - '5:2'");
 			} else {
 				for(String item : eDisallowedItems){
 					writer.println("    - '" + item + "'");
 				}
 			}
 			writer.println("");
 			writer.println("# To block a potion, you must enter the damage value of the potion and ingredient needed.");
 			writer.println("# Recipes can be found here: http://www.minecraftwiki.net/wiki/Brewing");
 			writer.println("# Here are a few potions:");
 			writer.println("");
 			writer.println("# Water Bottle - 0");
 			writer.println("# Awkward Potion - 16");
 			writer.println("# Thick Potion - 32");
 			writer.println("# Mundane Potion (Extended) - 64");
 			writer.println("# Mundane Potion - 8192");
 			writer.println("# Potion of Regeneration (2:00) - 8193");
 			writer.println("# Potion of Regeneration (8:00) - 8257");
 			writer.println("# Potion of Regeneration II - 8225");
 			writer.println("# Potion of Swiftness(3:00) - 8194");
 			writer.println("# Potion of Swiftness (8:00) - 8258");
 			writer.println("# Potion of Swiftness II - 8226");
 			writer.println("# Potion of Fire Resistance (3:00) - 8195");
 			writer.println("# Potion of Fire Resistance (8:00) - 8259");
 			writer.println("# Potion of Fire Resistance (reverted) - 8227");
 			writer.println("");
 			writer.println("# The rest can be found here: http://www.minecraftwiki.net/wiki/Potions#Base_Potions");
 			writer.println("");
 			writer.println("# Here are are the Ingredients:");
 			writer.println("");
 			writer.println("# Nether Wart - 372");
 			writer.println("# Glowstone Dust - 348");
 			writer.println("# Redstone Dust - 331");
 			writer.println("# Fermented Spider Eye - 376");
 			writer.println("# Magma Cream - 378");
 			writer.println("# Sugar - 353");
 			writer.println("# Glistering Melon - 382");
 			writer.println("# Spider Eye - 375");
 			writer.println("# Ghast Tear - 370");
 			writer.println("# Blaze Powder - 377");
 			writer.println("# Gun Powder - 289");
 			writer.println("");
 			writer.println("# Default example is 0:372 which would block the Awkward Potion");
 			writer.println("DisallowedPotionRecipes:");
 			if(eDisallowedPotionRecipes.isEmpty()){
 				writer.println("    - '0:372'");
 			} else {
 				for(String recipe : eDisallowedPotionRecipes){
 					writer.println("    - '" + recipe + "'");
 				}
 			}
 			writer.println("");
 			writer.println("#Use these to turn off individual features");
 			writer.println("StopCrafting: " + eStopCrafting);
 			writer.println("StopItemPickup: " + eStopItemPickup);
 			writer.println("StopPotionBrew: " + eStopPotionBrew);
 			writer.println("StopToolUse: " + eStopToolUse);
 			writer.println("StopItemHold: " + eStopItemHold);
 			writer.println("StopArmourWear: " + eStopWear);
 			writer.println("");
 			writer.println("# Permissions:");
 			writer.println("# 'noitem.nocraft.<item#>[.datavalue]' or");
 			writer.println("# 'noitem.nopickup.<item#>[.datavalue]' or");
 			writer.println("# 'noitem.nobrew.<potionDV>.<IngredientID>'");
 			writer.println("# 'noitem.nouse.<tool# OR toolname>' (i.e. noitem.nouse.diamondaxe)");
 			writer.println("# 'noitem.allitems' overrides ALL ( DisallowedItems and PerItemPermissions )");
 			writer.println("PerItemPermissions: " + ePerItemPermissions);
 			writer.println("");
 			writer.println("#Don't turn this on unless you like getting spammed with messages!");
 			writer.println("Debugging: " + eDebugging);
 			writer.println("");
 			writer.println("# This is to change whether you recieve update notifications");
 			writer.println("# for recommended builds or for development builds. (main/dev)");
 			writer.println("PluginChannel: " + ePluginChannel);
 			writer.println("");
 			writer.println("ConfigurationVersion: " + configVersion);
 			writer.close();
 		}
 	}
 	
 	private void copyConfigOptions(){
 		configFile = new File(plugin.getDataFolder(), config);
 		conf = YamlConfiguration.loadConfiguration(configFile);
 		//--------SET CONFIG OPTIONS TO WHAT SERVER HAS SELECTED--------//
 		this.eNotifyPlayer = conf.getBoolean("Notify.Player");
 		this.eNotifyAdmins = conf.getBoolean("Notify.Admins");
 		this.eStopCrafting = conf.getBoolean("StopCrafting");
 		this.eStopItemPickup = conf.getBoolean("StopItemPickup");
 		this.eStopPotionBrew = conf.getBoolean("StopPotionBrew");
 		this.eStopItemHold = conf.getBoolean("StopItemHold");
 		this.ePerItemPermissions = conf.getBoolean("PerItemPermissions");
 		this.eDebugging = conf.getBoolean("Debugging");
 		this.eStopToolUse = conf.getBoolean("StopToolUse");
 		this.eNotifyNoUse = conf.getBoolean("Notify.NoUse");
 		this.eNotifyNoBrew = conf.getBoolean("Notify.NoBrew");
 		this.eNoBrewMessage = conf.getString("Notify.NoBrewMessage");
 		
 		if(configVersion >= 4){
 			this.ePluginChannel = conf.getString("PluginChannel");
 		}
 		
 		if(configVersion >= 5){
 			this.eStopWear = conf.getBoolean("StopArmourWear");
 			this.eNoWearMessage = conf.getString("Notify.NoWearMessage");
 			this.eNotifyNoWear = conf.getBoolean("Notify.NoWear");
 		}
 		
 		this.ePlayerMessage = conf.getString("Notify.PlayerMessage");
 		this.eAdminMessage = conf.getString("Notify.AdminMessage");
 		this.eNoUseMessage = conf.getString("Notify.NoUseMessage");
 		
 		this.eDisallowedItems = conf.getStringList("DisallowedItems");
 		this.eDisallowedPotionRecipes = conf.getStringList("DisallowedPotionRecipes");
 	}
 	
 	//----GETTERS----//
 	//Notify//
 	public static boolean notifyPlayer(){
 		return Configuration.conf.getBoolean("Notify.Player");
 	}
 	public static boolean notifyAdmins(){
 		return Configuration.conf.getBoolean("Notify.Admins");
 	}
 	public static boolean notifyNoUse(){
 		return Configuration.conf.getBoolean("Notify.NoUse");
 	}
 	public static boolean notifyNoBrew(){
 		return Configuration.conf.getBoolean("Notify.NoBrew");
 	}
 	public static boolean notifyNoHold(){
 		return Configuration.conf.getBoolean("Notify.NoHold");
 	}
 	public static boolean notfiyNoWear(){
 		return Configuration.conf.getBoolean("Notify.NoWear");
 	}
 	//Stop//
 	public static boolean stopCrafting(){
 		return Configuration.conf.getBoolean("StopCrafting");
 	}
 	public static boolean stopItemPickup(){
 		return Configuration.conf.getBoolean("StopItemPickup");
 	}
 	public static boolean stopPotionBrew(){
 		return Configuration.conf.getBoolean("StopPotionBrew");
 	}
 	public static boolean stopToolUse(){
 		return Configuration.conf.getBoolean("StopToolUse");
 	}
 	public static boolean stopItemHold(){
 		return Configuration.conf.getBoolean("StopItemHold");
 	}
 	public static boolean stopArmourWear(){
 		return Configuration.conf.getBoolean("StopArmourWear");
 	}
 	//Misc//
 	public static boolean debugging(){
 		return Configuration.conf.getBoolean("Debugging");
 	}
 	public static boolean perItemPerms(){
 		return Configuration.conf.getBoolean("PerItemPermissions");
 	}
 	public static String pluginChannel(){
 		return Configuration.conf.getString("PluginChannel");
 	}
 	//Message//
 	public static String playerMessage(){
 		return Configuration.conf.getString("Notify.PlayerMessage");
 	}
 	public static String adminMessage(){
 		return Configuration.conf.getString("Notify.AdminMessage");
 	}
 	public static String noUseMessage(){
 		return Configuration.conf.getString("Notify.NoUseMessage");
 	}
 	public static String noBrewMessage(){
 		return Configuration.conf.getString("Notify.NoBrewMessage");
 	}
 	public static String noHoldMessage(){
 		return Configuration.conf.getString("Notify.NoHoldMessage");
 	}
 	public static String noWearMessage(){
 		return Configuration.conf.getString("Notify.NoWearMessage");
 	}
 	
 	//Lists//
 	public static List<String> disallowedItems(){
 		return Configuration.conf.getStringList("DisallowedItems");
 	}
 	public static List<String> disallowedPotions(){
 		return Configuration.conf.getStringList("DisallowedPotionRecipes");
 	}
 }

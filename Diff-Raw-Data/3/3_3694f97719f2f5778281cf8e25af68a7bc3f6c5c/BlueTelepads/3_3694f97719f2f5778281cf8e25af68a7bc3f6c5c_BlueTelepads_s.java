 package com.MoofIT.Minecraft.BlueTelepads;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 
 /**
  * @author Jim Drey
  *
  */
 public class BlueTelepads extends JavaPlugin {
 	private final BlueTelepadsPlayerListener playerListener = new BlueTelepadsPlayerListener(this);
 	private final BlueTelepadsBlockListener blockListener = new BlueTelepadsBlockListener(this);
 
 	public static Logger log;
 	public PluginManager pm;
 	public PluginDescriptionFile pdfFile;
 	private FileConfiguration config;
 
 	public static Economy econ = null;
 
 	//Config defaults
 	public int maxDistance = 0;
 	public boolean disableTeleportMessage = false;
 	public int telepadCenterID = 22;
 	public boolean useSlabAsDestination = false;
 	public boolean allowSingleSlabs = false;
 	public boolean versionCheck = true;
 
 	public boolean disableTeleportWait = false;
 	public int sendWait = 3;
 	public int telepadCooldown = 5;
 
 	public boolean disableEconomy = false;
 	public double teleportCost = 0;
 	public byte telepadSurroundingNormal = 0;
 	public byte telepadSurroundingFree = 1;
 
 	public HashMap<String, Object> BlueTelepadsMessages = new HashMap<String, Object>() {
 		private static final long serialVersionUID = 1L;
 		{
 			put("Error.Distance","Error: Telepads are too far apart!");
 			put("Error.AlreadyLinked","Error: This telepad seems to be linked already!");
 			put("Error.AlreadyLinkedInstruction","You can reset it by breaking the pressure pad on top of it, then tapping the lapis with redstone.");
 			put("Error.Reflexive","Error: You cannot connect a telepad to itself.");
 			put("Error.PlayerMoved","You're not on the center of the pad! Cancelling teleport.");
 
 			put("Core.TeleportWaitNoName","Preparing to send you!");
 			put("Core.TeleportWaitWithName","Preparing to send you to");
 			put("Core.WaitInstruction","Stand on the center of the pad.");
 			put("Core.NoWaitNoName","You have been teleported!");
 			put("Core.NoWaitWithName","You have been teleported to");
 			put("Core.LocationStored","Telepad location stored.");
 			put("Core.ProcessReset","Link process reset.");
 			put("Core.Activated","Telepad activated!");
 			put("Core.Teleport","Here goes nothing!");
 			put("Core.Reset","Telepad reset.");
 
 			put("Economy.InsufficientFunds","You don't have enough to pay for a teleport.");
 			put("Economy.Charged","You have been charged");
 
 			put("Permission.Use","You do not have permission to use telepads.");
 			put("Permission.Create","You do not have permission to create a telepad!");
 			put("Permission.CreateFree","You do not have permission to create a free telepad.");
 		}
 	};
 
 	//Config versioning
 	private int configVer = 0;
 	private final int configCurrent = 2;
 
 	public void onEnable() {
 		log = Logger.getLogger("Minecraft");
 		pm = getServer().getPluginManager();
 		pdfFile = getDescription();
 
 		loadConfig();
 		setupEconomy();
 		if (versionCheck) versionCheck();
 
 		pm.registerEvents(playerListener,this);
 		pm.registerEvents(blockListener,this);
 
 		log.info("BlueTelepads " + getDescription().getVersion() + " is enabled.");
 	}
 
 	public void onDisable() {
 		log.info("[BlueTelepads] Shutting down.");
 		pdfFile = null;
 		pm = null;
 	}
 
 	private void loadConfig() {
 		this.reloadConfig();
 		config = this.getConfig();
 
 		configVer = config.getInt("configVer", configVer);
 		if (configVer == 0) {
 			log.info("[BlueTelepads] Configuration error or no config file found. Generating default config file.");
 			saveDefaultConfig();
 			this.reloadConfig(); //hack to force good data into configs TODO 1.3: proper defaults
 			config = this.getConfig();
 		}
 		else if (configVer < configCurrent) {
 			log.warning("[BlueTelepads] Your config file is out of date! Delete your config and reload to see the new options. Proceeding using set options from config file and defaults for new options..." );
 		}
 
 		maxDistance = config.getInt("Core.maxTelepadDistance",maxDistance);
 		disableTeleportMessage = config.getBoolean("Core.disableTeleportMessage",disableTeleportMessage);
 		telepadCenterID = config.getInt("Core.telepadCenterID",telepadCenterID);
 		useSlabAsDestination = config.getBoolean("Core.useSlabAsDestination", useSlabAsDestination);
 		allowSingleSlabs = config.getBoolean("Core.allowSingleSlabs", allowSingleSlabs);
 		versionCheck = config.getBoolean("Core.versionCheck", versionCheck);
 
 		disableTeleportWait = config.getBoolean("Time.disableTeleportWait",disableTeleportWait);
 		sendWait = config.getInt("Time.sendWait", sendWait);
 		telepadCooldown = config.getInt("Time.telepadCooldown", telepadCooldown);
 
 		disableEconomy = config.getBoolean("Economy.disableEconomy", disableEconomy);
 		teleportCost = config.getDouble("Economy.teleportCost", teleportCost);
 		telepadSurroundingNormal = (byte)config.getInt("Economy.telepadSurroundingNormal", telepadSurroundingNormal);
 		telepadSurroundingFree = (byte)config.getInt("Economy.telepadSurroundingFree", telepadSurroundingFree);
 
 		//Messages
 		try {
 			BlueTelepadsMessages = (HashMap<String, Object>)config.getConfigurationSection("BlueTelepadsMessages").getValues(true);
 		} catch (NullPointerException e) {
 			log.warning("[BlueTelepads] Configuration failure while loading BlueTelepadsMessages. Using defaults.");
 		}
 	}
 
 	 private boolean setupEconomy() {
 		if (pm.getPlugin("Vault") == null) {
 			log.severe("[BlueTelepads] Vault not detected. Permissions and economy disabled.");
 			return false;
 		}
 		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		econ = rsp.getProvider();
 		return econ != null;
 	 }
 
 	public void versionCheck() {
 		String thisVersion = getDescription().getVersion();
 		URL url = null;
 		try {
 			url = new URL("http://www.moofit.com/minecraft/bluetelepads.ver?v=" + thisVersion);
 			BufferedReader in = null;
 			in = new BufferedReader(new InputStreamReader(url.openStream()));
 			String newVersion = "";
 			String line;
 			while ((line = in.readLine()) != null) {
 				newVersion += line;
 			}
 			in.close();
 			if (!newVersion.equals(thisVersion)) {
 				log.warning("[BlueTelepads] BlueTelepads is out of date! This version: " + thisVersion + "; latest version: " + newVersion + ".");
 			}
 			else {
 				log.info("[BlueTelepads] BlueTelepads is up to date at version " + thisVersion + ".");
 			}
 		}
 		catch (MalformedURLException ex) {
 			log.warning("[BlueTelepads] Error accessing update URL.");
 		}
 		catch (IOException ex) {
 			log.warning("[BlueTelepads] Error checking for update.");
 		}
 	}
 }

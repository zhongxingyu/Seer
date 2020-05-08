 package com.MoofIT.Minecraft.BlueTelepads;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.logging.Logger;
 
 import org.bukkit.util.config.Configuration;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 import com.nijikokun.register.payment.Method;
 
 /**
  * @author Jim Drey
  *
  */
 public class BlueTelepads extends JavaPlugin {
 	private final BlueTelepadsPlayerListener playerListener = new BlueTelepadsPlayerListener(this);
 	private final BlueTelepadsBlockListener blockListener = new BlueTelepadsBlockListener(this);
 	private BlueTelepadsServerListener serverListener;
 
 	public static Logger log;
 	public PluginManager pm;
 	public PluginDescriptionFile pdfFile;
 	private Configuration config;
 
 	public Method Method = null;
 
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
 
 	public double teleportCost = 0;
 	public byte telepadSurroundingNormal = 0;
 	public byte telepadSurroundingFree = 1;
 
 	//Config versioning
 	private int configVer = 0;
 	private final int configCurrent = 1;
 
 	public void onEnable() {
 		log = Logger.getLogger("Minecraft");
 		pm = getServer().getPluginManager();
 		pdfFile = getDescription();
 
 		loadConfig();
 		if (!loadRegister()) return;
 		if (versionCheck) versionCheck();
 
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLUGIN_DISABLE, serverListener, Priority.Monitor, this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.High, this);
 
 		log.info(pdfFile.getName() + " v." + pdfFile.getVersion() + " is enabled.");
 	}
 
 	public void onDisable() {
 		log.info("[BlueTelepads] Shutting down.");
 		pdfFile = null;
 		Method = null;
 		pm = null;
 	}
 
 	private void loadConfig() {
 		config = this.getConfiguration();
 		config.load();
 
 		configVer = config.getInt("configVer", configVer);
 		if (configVer == 0) {
 			try {
 				log.info("[BlueTelepads] Configuration error or no config file found. Downloading default config file...");
 				if (!new File(getDataFolder().toString()).exists()) {
 					new File(getDataFolder().toString()).mkdir();
 				}
 				URL config = new URL("https://raw.github.com/Southpaw018/BlueTelepads/master/config.yml");
 				ReadableByteChannel rbc = Channels.newChannel(config.openStream());
 				FileOutputStream fos = new FileOutputStream(this.getDataFolder().getPath() + "/config.yml");
 				fos.getChannel().transferFrom(rbc, 0, 1 << 24);
 			} catch (MalformedURLException ex) {
 				log.warning("[BlueTelepads] Error accessing default config file URL: " + ex);
 			} catch (FileNotFoundException ex) {
 				log.warning("[BlueTelepads] Error accessing default config file URL: " + ex);
 			} catch (IOException ex) {
 				log.warning("[BlueTelepads] Error downloading default config file: " + ex);
 			}
 
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
 		
 		teleportCost = config.getDouble("Economy.teleportCost", teleportCost);
 		telepadSurroundingNormal = (byte)config.getInt("Economy.telepadSurroundingNormal", telepadSurroundingNormal);
 		telepadSurroundingFree = (byte)config.getInt("Economy.telepadSurroundingFree", telepadSurroundingFree);
 	}
 
 	//returns: true, loaded; false, not loaded OR new
 	
 	//TODO Javadoc
 	private boolean loadRegister() {
 		try {
 			Class.forName("com.nijikokun.register.payment.Methods");
 			serverListener = new BlueTelepadsServerListener(this);
 			return true;
 		} catch (ClassNotFoundException e) {
 			try {
 				BlueTelepads.log.info("[BlueTelepads] Register library not found! Downloading...");
 				if (!new File("lib").isDirectory())
 					if (!new File("lib").mkdir())
 						BlueTelepads.log.severe("[BlueTelepads] Error creating lib directory. Please make sure Craftbukkit has permissions to write to the Minecraft directory and there is no file named \"lib\" in that location.");
				URL Register = new URL("http://www.moofit.com/minecraft/Register.jar");
 				ReadableByteChannel rbc = Channels.newChannel(Register.openStream());
 				FileOutputStream fos = new FileOutputStream("lib/Register.jar");
 				fos.getChannel().transferFrom(rbc, 0, 1 << 24);
 				BlueTelepads.log.info("[BlueTelepads] Register library downloaded. Server reboot required to load.");
 			} catch (MalformedURLException ex) {
 				BlueTelepads.log.warning("[BlueTelepads] Error accessing Register lib URL: " + ex);
 			} catch (FileNotFoundException ex) {
 				BlueTelepads.log.warning("[BlueTelepads] Error accessing Register lib URL: " + ex);
 			} catch (IOException ex) {
 				BlueTelepads.log.warning("[BlueTelepads] Error downloading Register lib: " + ex);
 			} finally {
 				pm.disablePlugin(this);
 			}
 			return false;
 		}
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
 
 	//TODO combine config and register lib file writing code into a function
 }

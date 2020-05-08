 package me.krotn.Rent;
 
 import java.util.logging.Logger;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Rent extends JavaPlugin{
 	
 	private Logger log = Logger.getLogger("Minecraft");
 	private RentLogManager logManager = new RentLogManager(log);
 	private RentDatabaseManager dbman = new RentDatabaseManager();
 	private RentPropertiesManager propman = new RentPropertiesManager();
 	private RentDateUtils dateUtils = new RentDateUtils(this,dbman);
 	private RentCalculationsManager calcMan = new RentCalculationsManager(this);
 	private RentPermissionsManager permMan = null;
	private PlayerListener playerListener = null;
 	
 	public void onEnable(){
 		logManager.info("Rent enabled");
 		PluginManager pluginManager = getServer().getPluginManager();
 		dbman.connect();
 		if(!propman.fileExists()){
 			propman.setup();
 		}
 		permMan = new RentPermissionsManager(this);
		playerListener = new RentPlayerListener(this);
		pluginManager.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Normal, this);
		pluginManager.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
 	}
 	
 	public void onDisable(){
 		logManager.info("Rent disabled");
 		dbman.disconnect();
 	}
 	
 	public RentLogManager getLogManager(){
 		return logManager;
 	}
 	
 	public RentDatabaseManager getDatabaseManager(){
 		return this.dbman;
 	}
 	
 	public RentPropertiesManager getPropertiesManager(){
 		return this.propman;
 	}
 	
 	public RentDateUtils getDateUtils(){
 		return dateUtils;
 	}
 	
 	public RentCalculationsManager getCalculationsManager(){
 		return calcMan;
 	}
 	
 	public RentPermissionsManager getPermissionsManager(){
 		return permMan;
 	}
 }

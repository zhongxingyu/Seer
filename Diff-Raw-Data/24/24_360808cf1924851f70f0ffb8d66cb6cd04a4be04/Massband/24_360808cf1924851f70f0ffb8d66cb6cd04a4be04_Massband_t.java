 package de.MrX13415.Massband;
 
 import java.util.ArrayList;
 //import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.event.Listener;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 import de.MrX13415.Massband.CommandExecuter.MassbandCommandExecuter;
 import de.MrX13415.Massband.Config.Config;
 import de.MrX13415.Massband.Language.Language;
 import de.MrX13415.Massband.Language.LanguageLoader;
 import de.MrX13415.Massband.Listener.MassbandPlayerListener;
 
 import org.bukkit.plugin.Plugin;
 
 /**
  * Massband (bukkit plugin)
  * A mesuring tape
  *
 * @version 2.8.1 r66
  * @author MrX13415
  * 
  * Website:      http://dev.bukkit.org/server-mods/massband/
  * 
  * Copyright (C) 2012 MrX13415
  * 
  * License:
  *  
  *     CreativeCommons Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0)
  * 
  *     For more Informations:
  *     http://creativecommons.org/licenses/by-nc-sa/3.0/
  */
 
 public class Massband extends JavaPlugin {
 		
 	public static PluginDescriptionFile pdfFile = null;
 	public static Server server = null;
 	public static Logger log = null;
 	public static String pluginName = null;
 	public static String consoleOutputHeader = null;
 	public static Config configFile = null;
 	public static Language language;
 	
 	//permissions
 	private static PermissionHandler permissionHandler;
 	private static boolean defaultPermission;
 
 	public static final String PERMISSION_NODE_Massband_use = "Massband.use";
 	public static final String PERMISSION_NODE_Massband_stop_all = "Massband.stopall";
 	public static final String PERMISSION_NODE_Massband_countblocks = "Massband.countblocks";
 	public static final String PERMISSION_NODE_Massband_blocklist = "Massband.blocklist";
 	
 	//holds information for all Players.
 	public static ArrayList<PlayerVars> playerlist = new ArrayList<PlayerVars>();
 	
 	//holds all Counting Threads ..
 	public static ArrayList<CountBlocks> threads = new ArrayList<CountBlocks>();
 	
 	private final Listener pListener = new MassbandPlayerListener();
 //	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
 
 
 	@Override
 	public void onDisable() {
 //		if (configFile.write()) { //create new File
 //			log.info(consoleOutputHeader + " Settings in config saved");
 //		}
 		
 		CountBlocks.interuptAll(null);
 	}
 
 	public static PluginDescriptionFile getPluginDescriptionFile() {
 		return pdfFile;
 	}
 
 	public static Logger getLog() {
 		return log;
 	}
 
 	public static String getPluginName() {
 		return pluginName;
 	}
 
 	public static String getConsoleOutputHeader() {
 		return consoleOutputHeader;
 	}
 
 	public static Config getConfigFile() {
 		return configFile;
 	}
 
 	public static PermissionHandler getPermissionHandler() {
 		return permissionHandler;
 	}
 
 	public static ArrayList<PlayerVars> getPlayerlist() {
 		return playerlist;
 	}
 
 	public static ArrayList<CountBlocks> getThreads() {
 		return threads;
 	}
 
 	public Listener getpListener() {
 		return pListener;
 	}
 
 	public static Language getLanguage() {
 		return language;
 	}
 
 	public static void setLanguage(Language language) {
 		Massband.language = language;
 	}
 
 	@Override
 	public void onLoad() {
 		//set static vars ...
 		server = this.getServer();
 		log = server.getLogger();
 		
 		pdfFile = this.getDescription();
 		pluginName = pdfFile.getName();
 		consoleOutputHeader = "[" + pluginName + "]";
 
 		LanguageLoader.updateLanguages(false);
 		language = LanguageLoader.loadDefaultLanguage();
 		
 //        log.info(consoleOutputHeader + " v" + pdfFile.getVersion() + " " + pdfFile.getAuthors() + " is enabled.");
   
         configFile = new Config();
         configFile.read();
         
         if (! configFile.configFileVersion.equalsIgnoreCase(configFile.version)){
         	configFile.configFileVersion = configFile.version;
         	log.info(consoleOutputHeader + " Config file updated ...");
         	configFile.write();
         }
         //---------------------
                 
         //lang loaded from config ...
         log.info(consoleOutputHeader + " Language set to: " + language._languageName);
         if (! language.isUptoDate()) log.warning(consoleOutputHeader + " Your current language is not up to date! (file: " + language.getLanguageFileName() + ")");   
 	}
 	
 	@Override
 	public void onEnable() {        
         //register events ...
 //		PluginManager pm = getServer().getPluginManager();
 		getServer().getPluginManager().registerEvents(pListener, this);
 //		pm.registerEvent(Event.Type.PLAYER_INTERACT, pListener, Priority.Normal, this);
 //		pm.registerEvent(Event.Type.PLAYER_QUIT, pListener, Priority.Normal, this);
 		
 		//register commands ...
 		try {
 			this.getCommand("massband").setExecutor(new MassbandCommandExecuter());
 		} catch (Exception e) {
 			log.warning("[" + pdfFile.getName() + "] Error: Commands not definated in 'plugin.yaml'");
 		}
 		
 		setupPermissions();
 	}
 	
 //    public boolean isDebugging(final Player player) {
 //        if (debugees.containsKey(player)) {
 //            return debugees.get(player);
 //        } else {
 //            return false;
 //        }
 //    }
 //    
 //    public void setDebugging(final Player player, final boolean value) {
 //        debugees.put(player, value);
 //    }
     
 	public static boolean permissions(){
 		if ((Massband.permissionHandler != null || defaultPermission) && Massband.configFile.usePermissions){
 			return true;
 		}
 		return false;
 	}
 	
 	public static boolean hasPermission(Player player, String permission){
 		
 		if (Massband.permissionHandler != null){
 			if (Massband.permissionHandler.permission(player, permission)){
 				return true;
 			}
 		}
 		
 		if (player.hasPermission(permission)){
 			return true;
 		}
 		
 		return false;
 	}
 
 	private void setupPermissions() {
 		Plugin permissionsPlugin = this.getServer().getPluginManager()
 				.getPlugin("Permissions");
 		Plugin permissionsBukkitPlugin = this.getServer().getPluginManager()
 				.getPlugin("PermissionsBukkit");
 
 		if (Massband.permissionHandler == null) {
 			if (permissionsPlugin != null) {
 				Massband.permissionHandler = ((Permissions) permissionsPlugin)
 						.getHandler();
 				log.info(consoleOutputHeader + " Permission system detected: "
 						+ permissionsPlugin.getDescription().getFullName());
 			} else if (Massband.configFile.usePermissions) {
 				defaultPermission = true;
 
 				String pluginName = "";
 
 				if (permissionsBukkitPlugin != null) {
 					pluginName = ": "
 							+ permissionsBukkitPlugin.getDescription()
 									.getFullName();
 				}
 
 				log.info(consoleOutputHeader + " Permission system detected"
 						+ pluginName);
 			} else {
 				log.warning(consoleOutputHeader
 						+ " Permission system NOT detected OR disabled! (everyone will have permissions to use it.)");
 			}
 		}
 	}
 	 
 //    private void setupPermissionsO() {
 //	      Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
 //
 //	      if (Massband.permissionHandler == null) {
 //	          if (permissionsPlugin != null) {
 //	        	  Massband.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
 //	        	  log.info(consoleOutputHeader + " Permission system detected: " + permissionsPlugin.getDescription().getFullName());
 //	          } else {
 //	        	  log.warning(consoleOutputHeader + " Permission system NOT detected! (everyone will have permissions to use it.)");
 //	          }
 //	      }
 //	  }
   	
 	/** remove PlayerVars from the current Player, to keep
 	 *  the ArrayList short.
 	 * 
 	 * @param player
 	 */
 	public static void removePlayer(Player player) {
 		PlayerVars playerVars = getPlayerVars(player);
 		if (playerVars != null) playerlist.remove(playerVars);
 	}
 
 	/** returns the PlayerVars for the given player
 	 *  or create it.
 	 *   
 	 * @param player
 	 * @return
 	 */
 	public static PlayerVars getPlayerVars(Player player) {
 		PlayerVars playerVars = null;
 		
 		for (int playerIndex = 0; playerIndex < playerlist.size(); playerIndex++) {
     		if (playerlist.get(playerIndex).getPlayer().equals(player)) {
     			playerVars = playerlist.get(playerIndex); 
     		}
     	}
     	
 		if (playerVars == null){
 			//create PlayerVars for the current Player.
 			playerVars = new PlayerVars(player);
 			playerlist.add(playerVars);
 		}
 		
 		return playerVars;
 	}
 }

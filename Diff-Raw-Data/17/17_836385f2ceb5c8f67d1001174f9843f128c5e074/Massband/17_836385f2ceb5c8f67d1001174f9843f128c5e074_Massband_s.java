 package de.MrX13415.Massband;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import org.bukkit.plugin.Plugin;
 
 /**
  * Massband (bukkit plugin)
  * A Mesuring Tape
  *
  * @version 2.6 r39
  * @author Oliver Daus
  *
  */
 public class Massband extends JavaPlugin {
 	
 	static Server server = null;
 	static Logger log = null;
 	static String pluginName = null;
 	static String consoleOutputHeader = null;
 	static Config configFile = null;
 
 	//permissions
 	static PermissionHandler permissionHandler;
 	static final String PERMISSION_NODE_Massband_use = "Massband.use";
 	
 	//holds information for all Players.
 	public static ArrayList<PlayerVars> playerlist = new ArrayList<PlayerVars>();
 	
	private final PlayerListener pListener = new de.MrX13415.Massband.PlayerListener();
 	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
 	
 	
 	@Override
 	public void onDisable() {
 
 	}
 
 	@Override
 	public void onEnable() {
 		//set static vars ...
 		server = this.getServer();
 		log = server.getLogger();
 		
 		PluginDescriptionFile pdfFile = this.getDescription();
 		pluginName = pdfFile.getName();
 		consoleOutputHeader = "[" + pluginName + "]";
 
         log.info(consoleOutputHeader + " v" + pdfFile.getVersion() + " " + pdfFile.getAuthors() + " is enabled.");
   
         configFile = new Config();
         configFile.read();
         //---------------------
                 
         //register events ...
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, pListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_ITEM_HELD, pListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_JOIN, pListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_QUIT, pListener, Priority.Normal, this);
 
 		//register commands ...
 		try {
 			this.getCommand("massband").setExecutor(new MassbandCommandExecuter());
 		} catch (Exception e) {
 			log.warning("[" + pdfFile.getName() + "] Error: Commands not definated in 'plugin.yaml'");
 		}
 		
 		//initialize user data...
 		 
 		System.out.println(consoleOutputHeader + " initialize user data... ");
 		for (int playerIndex = 0; playerIndex < server.getOnlinePlayers().length ; playerIndex++) {
 			Player player = server.getOnlinePlayers()[playerIndex];
 			Massband.addPlayer(new PlayerVars(player));
 //			player.sendMessage("MB: ADD: " + player.getName() + " COUNT: " + Massband.getPlayerListSize());
 		}
 		
 		setupPermissions();
 	}
 	
     public boolean isDebugging(final Player player) {
         if (debugees.containsKey(player)) {
             return debugees.get(player);
         } else {
             return false;
         }
     }
     
     public void setDebugging(final Player player, final boolean value) {
         debugees.put(player, value);
     }
     
     private void setupPermissions() {
 	      Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
 
 	      if (Massband.permissionHandler == null) {
 	          if (permissionsPlugin != null) {
 	        	  Massband.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
	        	  log.info(consoleOutputHeader + " Permission system dedected: " + permissionsPlugin.getDescription().getFullName());
 	          } else {
	        	  log.warning(consoleOutputHeader + " Permission system NOT detected!");
 	          }
 	      }
 	  }
     
 	public static void addPlayer(PlayerVars player) {
 		playerlist.add(player);
 	}
 	
 	public static void removePlayer(int index) {
 		playerlist.remove(index);
 	}
 	
 	public static PlayerVars getPlayer(int index) {
 		return playerlist.get(index);
 	}
 	
 	public static int getPlayerListSize() {
 		return playerlist.size();
 	}
 }

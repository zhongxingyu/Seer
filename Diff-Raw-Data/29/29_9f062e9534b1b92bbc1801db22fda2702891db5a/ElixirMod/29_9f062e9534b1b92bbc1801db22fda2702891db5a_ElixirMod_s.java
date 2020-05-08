 package me.steeveeo.ElixirMod;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Timer;
 import java.util.logging.Logger;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class ElixirMod extends JavaPlugin {
 	
 	//Establish Player Listeners
 	private final ElixirModPlayerListener playerListener = new ElixirModPlayerListener(this);
 
 	//Start Logger
 	Logger log = Logger.getLogger("Minecraft");
 
 	//Initialize Drunkenness Timer
 	final Timer ElixirMod_Timer = new Timer(true);
 	final ElixirModToxicityController ElixirMod_PotionController = new ElixirModToxicityController(this);
 	
 	//Start Config File Stuffs
     public String enabledstartup = "Enabled On Startup";
     public boolean enabled;
     ElixirModFileConfig config = new ElixirModFileConfig(this);
     
     //Permissions Stuff
     public PermissionHandler permissionHandler;
     private void setupPermissions()
     {
     	Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
 
 		if (this.permissionHandler == null) {
 			if (permissionsPlugin != null) {
 			    this.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
 			} else {
 			    log.info("Permission system not detected, defaulting to OP");
 			}
 		}
     }
 
     //Status Maps
 	public Map<Player, Boolean> hasteEnabled = new HashMap<Player, Boolean>();
 	
 	//Startup and Shutdown
 	public void onEnable()
 	{
 		//Start Timer
 		ElixirMod_Timer.schedule(ElixirMod_PotionController, (long) 5000, (long) 1000);
 		
 		//Init Config
 		config.configCheck();
 		
 		//Init Permissions
 		setupPermissions();
 		
 		//Register Listeners
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Highest, this);
 		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
 		
 		//Startup was good!
		log.info("[ElixirMod] - Version 0.3 Started.");
 	} 
 	
 	public void onDisable()
 	{ 
		log.info("[ElixirMod] - Version 0.3 Stopped.");
 	} 
 	
 }

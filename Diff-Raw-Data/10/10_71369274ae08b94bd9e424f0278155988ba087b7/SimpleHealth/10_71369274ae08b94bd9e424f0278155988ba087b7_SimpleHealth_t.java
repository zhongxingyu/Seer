 package net.krinsoft.simplehealth;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.World;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SimpleHealth extends JavaPlugin {
 	// public variables
 	public static PluginDescriptionFile description;
 	public static PluginManager manager;
 	public static Plugin plugin;
 	public static HashMap<Player, SimplePlayer> players = new HashMap<Player, SimplePlayer>();
 	
 	// private variables
 	private static String LOG_PREFIX;
 	private static Logger logger = Logger.getLogger("Minecraft");
 	
 	// Listeners
 	private final PListener pListener = new PListener(this);
 	private final EListener eListener = new EListener(this);
 
 	@Override
 	public void onEnable() {
 		// Plugin setup
 		plugin = this;
 		description = getDescription();
 		LOG_PREFIX = "[" + description.getName() + "] ";
 		manager = this.getServer().getPluginManager();
 		manager.registerEvent(Event.Type.PLAYER_INTERACT, pListener, Event.Priority.Normal, this);
 		manager.registerEvent(Event.Type.PLAYER_RESPAWN, pListener, Event.Priority.Normal, this);
 		manager.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, pListener, Event.Priority.Highest, this);
 		manager.registerEvent(Event.Type.ENTITY_REGAIN_HEALTH, eListener, Event.Priority.Low, this);
 		Settings.dataFolder = getDataFolder();
 		if (Settings.configure()) {
 			LOG_PREFIX = Settings.locale.get(Settings.basic.getString("plugin.default_locale")).getString("plugin.log_prefix");
 		}
 		if (Settings.basic.getBoolean("plugin.peaceful", false)) {
 			String logMsg = "";
 			CraftWorld w;
 			for (World world : this.getServer().getWorlds()) {
 				w = (CraftWorld) world;
 				if (world != null && Settings.basic.getStringList("plugin.peaceful_worlds", new ArrayList<String>()).contains(w.getName())) {
 					logMsg = Settings.locale.get(Settings.basic.getString("plugin.default_locale")).getString("plugin.peaceful_enabled").replaceAll("<world>", w.getName());
 					logAdd(logMsg);
 					world.setSpawnFlags(false, true);
 				} else {
 					continue;
 				}
 			}
 		}
 		Settings.permissions();
		getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new SimpleTimer(this), 20, 20);
 		logAdd(Settings.locale.get(Settings.basic.getString("plugin.default_locale")).getString("plugin.enabled"));
 	}
 
 	@Override
 	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
 		players.clear();
 		logAdd(Settings.locale.get(Settings.basic.getString("plugin.default_locale")).getString("plugin.disabled"));
		Settings.locale.clear();
 	}
 
 	public static void logAdd(String msg) {
 		msg = LOG_PREFIX + msg;
 		msg = msg.replaceAll("<version>", description.getVersion());
 		msg = msg.replaceAll("<name>", description.getName());
 		msg = msg.replaceAll("<fullname>", description.getFullName());
 		logger.info(msg);
 	}
 
 }

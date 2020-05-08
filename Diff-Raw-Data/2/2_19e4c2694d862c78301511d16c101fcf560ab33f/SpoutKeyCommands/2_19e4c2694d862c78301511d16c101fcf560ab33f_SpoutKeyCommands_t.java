 package com.yurijware.bukkit.SpoutKeyCommands;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.persistence.PersistenceException;
 
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SpoutKeyCommands extends JavaPlugin {
     private static final Logger log = Logger.getLogger("Minecraft");
 	private static String logPrefix = null;
 	private static SpoutKeyCommands plugin = null;
 	private SpoutKeyCommandsHandler handle = new SpoutKeyCommandsHandler();
 	
 	public static enum ChooseMode { NONE, GSET, GUNSET, SET, UNSET }
 	
 	@Override
 	public void onDisable() {
		LogInfo("Plugin disabled!");
 	}
 	
 	@Override
 	public void onEnable() {
 		plugin = this;
 		logPrefix = "[" + this.getDescription().getName() + "] ";
 		
 		PluginManager pm = this.getServer().getPluginManager();
 		
 		Plugin spout = pm.getPlugin("Spout");
 		if (spout != null && spout.isEnabled()) {
 			String v = spout.getDescription().getVersion();
 			SpoutKeyCommands.LogInfo("Spout detected! Using version " + v);
 		} else {
 			SpoutKeyCommands.LogSevere("Spout not found. Please install it for this plugin to work.");
 			pm.disablePlugin(SpoutKeyCommands.getInstance());
 		}
 		
 		Permission.checkPermSupport();
 		setupDatabase();
 		
 		this.getCommand("SpoutKeyCommands").setExecutor(new Commands());
 		
 		pm.registerEvent(Event.Type.CUSTOM_EVENT, new SpoutListener(), Priority.Normal, this);
 		
 		LogInfo("Version " + this.getDescription().getVersion() + " is enabled!");
 	}
 	
 	protected static void LogInfo(String msg) {
 		log.info(logPrefix + msg);
 	}
 	
 	protected static void LogWarning(String msg) {
 		log.warning(logPrefix + msg);
 	}
 	
 	protected static void LogSevere(String msg) {
 		log.severe(logPrefix + msg);
 	}
 	
 	private void setupDatabase() {
 		try {
 			getDatabase().find(PlayerCmd.class).findRowCount();
 			getDatabase().find(GlobalCmd.class).findRowCount();
 			getDatabase().find(PlayerOptions.class).findRowCount();
 			getDatabase().find(GlobalOptions.class).findRowCount();
 		} catch (PersistenceException ex) {
 			LogInfo("Setting up database");
 			installDDL();
 			GlobalOptions op = new GlobalOptions("default preferred", "global");
 			this.getDatabase().save(op);
         }
     }
 	
 	@Override
     public List<Class<?>> getDatabaseClasses() {
         List<Class<?>> list = new ArrayList<Class<?>>();
         list.add(PlayerCmd.class);
         list.add(GlobalCmd.class);
         list.add(PlayerOptions.class);
         list.add(GlobalOptions.class);
         return list;
     }
 	
 	protected static SpoutKeyCommands getInstance() {
 		return plugin;
 	}
 	
 	public SpoutKeyCommandsHandler getHandle() {
 		return handle;
 	}
 
 }

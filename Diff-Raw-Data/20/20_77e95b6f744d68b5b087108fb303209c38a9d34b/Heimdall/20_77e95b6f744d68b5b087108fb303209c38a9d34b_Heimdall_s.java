 /**
  * 
  */
 package org.morganm.heimdall;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.morganm.heimdall.blockhistory.BlockHistoryFactory;
 import org.morganm.heimdall.blockhistory.BlockHistoryManager;
 import org.morganm.heimdall.command.CommandMapper;
 import org.morganm.heimdall.command.YesNoCommand;
 import org.morganm.heimdall.engine.EngineConfig;
 import org.morganm.heimdall.engine.LastGriefTrackingEngine;
 import org.morganm.heimdall.engine.NotifyEngine;
 import org.morganm.heimdall.event.EventManager;
 import org.morganm.heimdall.event.handlers.PlayerCleanupHandler;
 import org.morganm.heimdall.listener.BukkitBlockListener;
 import org.morganm.heimdall.listener.BukkitInventoryListener;
 import org.morganm.heimdall.listener.BukkitPlayerListener;
 import org.morganm.heimdall.log.LogInterface;
 import org.morganm.heimdall.player.FriendTracker;
 import org.morganm.heimdall.player.PlayerStateManager;
 import org.morganm.heimdall.util.Debug;
 import org.morganm.heimdall.util.JarUtils;
 import org.morganm.heimdall.util.JavaPluginExtensions;
 import org.morganm.heimdall.util.PermissionSystem;
 
 /**
  * @author morganm
  *
  */
 public class Heimdall extends JavaPlugin implements JavaPluginExtensions {
 	public static final Logger log = Logger.getLogger(Heimdall.class.toString());
 	public static final String logPrefix = "[Heimdall] ";
 
 	private String version;
 	private int buildNumber = -1;
 	private boolean configLoaded = false;
 	
 	private PermissionSystem perm;
 	private JarUtils jarUtil;
 	private EventManager eventManager;
 	private PlayerStateManager playerStateManager;
 	private NotifyEngine notifyEngine;
 	private FriendTracker friendTracker;
 	private LastGriefTrackingEngine lastGriefTrackingEngine;
 	private BlockHistoryManager blockHistoryManager;
 	private DependencyManager dependencyManager;
 	private final Set<LogInterface> logs = new HashSet<LogInterface>(5);
 	
 	@Override
 	public void onEnable() {
 		version = getDescription().getVersion();
 		jarUtil = new JarUtils(this, getFile(), log, logPrefix);
 		buildNumber = jarUtil.getBuildNumber();
 
 		loadConfig();
 		Debug.getInstance().debug("onEnable() starting, config loaded");
 		
 		perm = new PermissionSystem(this, log, logPrefix);
 		perm.setupPermissions(isVerboseEnabled());
 
 		new CommandMapper(this).mapCommands();		// map our command objects
 		
 		// initialize various objects needed to get things going
 		dependencyManager = new DependencyManager(this);
 		playerStateManager = new PlayerStateManager(this);
 		eventManager = new EventManager(this);
 		friendTracker = new FriendTracker(this);
 		blockHistoryManager = BlockHistoryFactory.getBlockHistoryManager(this);
 
 		// register all config-controlled Engines
 		final EngineConfig engineConfig = new EngineConfig(this);
 		engineConfig.registerEngines();
 
 		eventManager.registerHandler(this, new PlayerCleanupHandler(this, playerStateManager));
 		
 		final PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(new BukkitBlockListener(this, eventManager), this);
 		pm.registerEvents(new BukkitInventoryListener(this, eventManager), this);
 		pm.registerEvents(new BukkitPlayerListener(this, eventManager), this);
 		pm.registerEvents(YesNoCommand.getInstance(), this);
 		pm.registerEvents(dependencyManager, this);
 		
 		playerStateManager.getPlayerTracker().reset();
 		
 		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
 			public void run() { flushLogs(); }
 		}, 300, 300);	// every 15 seconds
 
 		verbose("version "+version+", build "+buildNumber+" is enabled");
 		Debug.getInstance().debug("onEnable() finished");
 }
 	
 	@Override
 	public void onDisable() {
 		Debug.getInstance().debug("onDisable() starting");
 		getServer().getScheduler().cancelTasks(this);
 
 		try {
 			playerStateManager.save();
 		}
 		catch(Exception e) {
 			log.severe(logPrefix+"error saving playerStateManager: "+e.getMessage());
 		}
 		
 		synchronized (this) {
 			// we do this to get around ConcurrentModificationException (if we used an
 			// iterator) since LogInterface.close() is supposed to remove the element
 			// from the array.
 			LogInterface[] logArray = logs.toArray(new LogInterface[] {});
 			for(int i=0; i < logArray.length; i++) {
 				logArray[i].close();
 				if( logs.contains(logArray[i]) )
 					removeLogger(logArray[i]);
 			}
 		}
 
 		eventManager.unregisterAllPluginEnrichers(this);
 		eventManager.unregisterAllPluginHandlers(this);
 		
 		verbose("version "+version+", build "+buildNumber+" is disabled");
 		Debug.getInstance().debug("onDisable() finished");
 		Debug.getInstance().disable();
 	}
 
 	public boolean isVerboseEnabled() { return getConfig().getBoolean("verbose", true); }
 	
 	/** Log a message to the console, but only if verbose is configured to true.
 	 * 
 	 * @param message
 	 */
 	public void verbose(final String message) {
 		if( isVerboseEnabled() )
 			log.info(logPrefix+message);
 	}
 	
 	public void loadConfig() {
 		File file = new File(getDataFolder(), "config.yml");
 		if( !file.exists() ) {
 			jarUtil.copyConfigFromJar("config.yml", file);
 		}
 
 		if( !configLoaded ) {
 			super.getConfig();
 			configLoaded = true;
 		}
 		else
 			super.reloadConfig();
 		
 		Debug.getInstance().init(log, logPrefix, "plugins/Heimdall/logs/debug.log", false);
 		Debug.getInstance().setDebug(getConfig().getBoolean("devDebug", false), Level.FINEST);
 		Debug.getInstance().setDebug(getConfig().getBoolean("debug", false));
 	}
 
 	public void addLogger(LogInterface log) {
 		logs.add(log);
 	}
 	
 	public void removeLogger(LogInterface log) {
 		logs.remove(log);
 	}
 
 	public void flushLogs() {
 		synchronized (this) {
 			for(Iterator<LogInterface> i = logs.iterator(); i.hasNext();) {
 				LogInterface log = i.next();
 				try {
 					log.flush();
 				}
 				catch(IOException e) {
 					i.remove();		// if we get an exception, remove the log
 				}
 			}
 		}
 	}
 
 	public EventManager getEventManager() { return eventManager; }
 	public NotifyEngine getNotifyEngine() { return notifyEngine; }
 	public LastGriefTrackingEngine getLastGriefTrackingEngine() { return lastGriefTrackingEngine; }
 	public FriendTracker getFriendTracker() { return friendTracker; }
 	public BlockHistoryManager getBlockHistoryManager() { return blockHistoryManager; }
 	public DependencyManager getDependencyManager() { return dependencyManager; }
 	public PlayerStateManager getPlayerStateManager() { return playerStateManager; }
 	
 	@Override
 	public PermissionSystem getPermissionSystem() { return perm; }
 
 	@Override
 	public Logger getLogger() { return log; }
 
 	@Override
 	public String getLogPrefix() { return logPrefix; }
 
 	@Override
 	public JarUtils getJarUtils() { return jarUtil; }
 
 	@Override
 	public File getFile() { return super.getFile(); }
 	
 	@Override
 	public ClassLoader getClassLoader() { return super.getClassLoader(); }
 }

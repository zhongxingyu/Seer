 import java.io.File;
 import java.util.logging.Logger;
 
 public class WorldManager extends Plugin {
 
 	public static final String NAME = "WorldManager";
 	public static final String VERSION = "0.0.1";
 	public static final String AUTHOR = "MrTimeShadow";
 	public static final Logger mclogger = Logger.getLogger("Minecraft");
 	
 	private WMCommandListener commandListener = new WMCommandListener();
 	
 	@Override
 	/**
 	 * Registers the Listener for the needed Hooks
 	 */
 	public void initialize() {
 		this.setName("WorldManager v0.0.1 by MrTimeShadow");
 		mclogger.info("[WorldManager] Initializing WorldManager!");
 		PluginLoader loader = etc.getLoader();
 		
 		this.addCommandListeners(loader);
 		
 		mclogger.info("[WorldManager] Successfully enabled WorldManager");
 		
 		mclogger.info("[WorldManager] Loading Worlds...");
 		loadWorldsOnStartup();
 		mclogger.info("[WorldManager] Successfully loaded Worlds!");
 		
 	}
 	
 	@Override
 	/**
 	 * Registers the commands
 	 */
 	public void enable() {
 		mclogger.info("[WorldManager] Enabling WorldManager!");
 		etc.getInstance().addCommand("/wm", "Displays the Help of WorldManager"); //Adds the Command to the help list
 	}
 
 	@Override
 	/**
 	 * Removes the commands
 	 */
 	public void disable() {
 		etc.getInstance().removeCommand("/wm"); //Removes the Command from the Help list
 		mclogger.info("[WorldManager] Successfully disabled WorldManager!");
 		WMWorldConfiguration.saveConfigs();
 	}
 	
 	
 	private void addCommandListeners(PluginLoader loader) {
 		loader.addListener(PluginLoader.Hook.COMMAND, commandListener, this, PluginListener.Priority.MEDIUM);
 		loader.addListener(PluginLoader.Hook.COMMAND_CHECK, commandListener, this, PluginListener.Priority.MEDIUM);
 		loader.addListener(PluginLoader.Hook.SERVERCOMMAND, commandListener, this, PluginListener.Priority.MEDIUM);
 	}
 	
 	public void loadWorldsOnStartup() {
 		File path = new File("config/worldmanager/worlds/");
 		if(!path.isDirectory() || !path.exists()) {
 			path.mkdirs();
 			return;
 		}
 		for(File f : path.listFiles()) {
 			if(f.getName().endsWith(".properties")) {
 				PropertiesFile pf = new PropertiesFile(f.getPath());
 				boolean load = pf.getBoolean("auto-load");
 				if(load) {
					String worldname = f.getName().substring(0, f.getName().lastIndexOf('.') - 1);
 					mclogger.info("[WorldManager] Loading world " + worldname + "...");
 					World[] world = etc.getServer().loadWorld(worldname);
 					new WMWorldConfiguration(world);
 				}
 			}
 		}
 	}
 	
 	
 
 }

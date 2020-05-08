 package asofold.fix.wgp;
 
 import java.io.File;
 import java.util.LinkedList;
 import java.util.List;
 
import org.bukkit.command.CommandSender;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import asofold.fix.wgp.compatlayer.CompatConfig;
 import asofold.fix.wgp.compatlayer.CompatConfigFactory;
 
 /**
  * 
  * 
  * @author mc_dev
  *
  */
 public class WGPFix extends JavaPlugin {
 	final WGPFixBlockListener blockListener = new WGPFixBlockListener(this);
 	final WGPFixServerListener serverListener = new WGPFixServerListener(this);
 	final static List<WGPRegionChecker> regionCheckers = new LinkedList<WGPRegionChecker>();
 	
 	@Override
 	public void onDisable() {
 		blockListener.monitorPistons = false;
 		blockListener.resetWG();
 		System.out.println("WorldGuardPistonFix (WGPFix) "+getDescription().getVersion()+" disabled.");
 	}
 
 	@Override
 	public void onEnable() {
 		loadSettings();
 		getCommand("wgpfix").setExecutor(new WGPFixCommand(this));
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.BLOCK_PISTON_EXTEND, blockListener, Priority.Low, this);
 		pm.registerEvent(Event.Type.BLOCK_PISTON_RETRACT, blockListener, Priority.Low, this);
 		pm.registerEvent(Event.Type.PLUGIN_DISABLE, serverListener, Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);
 		System.out.println("WorldGuardPistonFix (WGPFix) "+getDescription().getVersion()+" enabled.");
 	}
 	
 	/**
 	 * API !
 	 * If set to false pistons will not be monitored at all !
 	 * @param monitor
 	 */
 	public void setMonitorPistons( boolean monitor){
 		this.blockListener.monitorPistons = monitor;
 	}
 	
 	/**
 	 * API !
 	 * If set to true, non sticky pistons might be prevented from retracting at some performance loss (imagine item lifting devices).
 	 * (For the paranoid))
 	 * @param prevent
 	 */
 	public void setPreventNonStickyRetract( boolean prevent){
 		this.blockListener.preventNonStickyRetract = prevent;
 	}
 	
 	/**
 	 * API !
 	 * Set the interval in ms, at which the WorldGuard instance is set.
 	 * @param ms
 	 */
 	public void setWorldGuardSetInterval(long ms){
 		this.blockListener.tsThreshold = ms;
 	}
 	
 	public void setPopDisallowed( boolean pop){
 		this.blockListener.popDisallowed = pop;
 	}
 	
 	/**
 	 * (API)
 	 * Load and apply settings from wgpfx.yml !
 	 */
 	public boolean loadSettings(){
 		File file = new File( getDataFolder(), "wgpfix.yml");
 		blockListener.denySticky.clear();
 		try{
 			CompatConfig config = CompatConfigFactory.getConfig(file);
 			if (!file.exists()){
 				config.setProperty("monitor-pistons", true);
 				config.setProperty("prevent-nonsticky-retract", false);
 				config.setProperty("set-worldguard-interval", 4000);
 				config.setProperty("pop-disallowed", false);
 				config.setProperty("deny-blocks.sticky", new LinkedList<Integer>());
 				config.setProperty("deny-blocks.all", new LinkedList<Integer>());
 				config.save(); // ignore result
 			} else{
 				config.load();
 			}
 			this.setMonitorPistons(config.getBoolean("monitor-pistons", true));
 			this.setPreventNonStickyRetract(config.getBoolean("prevent-nonsticky-retract", false));
 			this.setWorldGuardSetInterval(config.getInt("set-worldguard-interval", 4000));
 			this.setPopDisallowed(config.getBoolean("pop-disallowed", false));
 			List<Integer> deny = config.getIntList("deny-blocks.sticky", null);
 			if ( deny != null){
 				blockListener.denySticky.addAll(deny);
 			}
 			deny = config.getIntList("deny-blocks.all", null);
 			if ( deny != null){
 				blockListener.denyAll.addAll(deny);
 				blockListener.denySticky.addAll(deny);
 			}
 			
 			blockListener.setWG();
 			return true;
 		} catch (Throwable t){
 			getServer().getLogger().severe("WGPFix - Could not load configuration, continue wirh paranoid settings !");
 			setParanoid();
 			return false;
 		}
 	}
 	
 	/**
 	 * (API)
 	 * Prevent everything that can be prevented by this plugin.
 	 */
 	public void setParanoid(){
 		this.setMonitorPistons(true);
 		this.setPreventNonStickyRetract(true);
 		this.setWorldGuardSetInterval(4000);
 		this.setPopDisallowed(true); // PARANOID !
 		blockListener.setWG();
 	}
 	
 	/**
 	 * API !
 	 * Register implementation that will check regions just before allowing piston-action.
 	 */
 	public static void addRegionChecker( WGPRegionChecker checker){
 		regionCheckers.add(checker);
 	}
 	
 	/**
 	 * API !
 	 * Unregister implementation for checking regions affected by piston actions.
 	 * @param checker
 	 */
 	public static void removeRegionChecker( WGPRegionChecker checker){
 		regionCheckers.remove(checker);
 	}
 
	public boolean hasPermission(CommandSender sender, String perm) {
		return blockListener.getWorldGuard().hasPermission(sender, perm);
	}

 }

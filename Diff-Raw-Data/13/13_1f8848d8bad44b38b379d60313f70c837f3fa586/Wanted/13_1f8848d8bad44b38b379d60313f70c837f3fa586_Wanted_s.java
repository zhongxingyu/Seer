 import java.util.*;
 import java.util.logging.*;
 
 public class Wanted extends Plugin {
 	
 	/*
 	 * Version Number Information
 	 * ---
 	 * Format "vMajorRelease.MinorRelease.DevelopmentBuild-bBugFixBuild"
 	 * ---
	 * Latest Version is v0.1.5
 	 */
 	
 	List<PluginRegisteredListener> pluginListeners = new ArrayList<PluginRegisteredListener>();
 	static final List<PluginLoader.Hook> requiredHooks = Arrays.asList(PluginLoader.Hook.COMMAND, PluginLoader.Hook.SERVERCOMMAND, PluginLoader.Hook.DAMAGE, PluginLoader.Hook.LOGIN);
 	static final Logger pluginLogger = Logger.getLogger("Minecraft");
 	static final Listener pluginListener = new Listener();
	static final String pluginName = "Wanted!", pluginVersion = "v0.1.5";
 
 	public void disable() {
 		for (PluginRegisteredListener listener : pluginListeners) {
 			etc.getLoader().removeListener(listener);
 		}
 		pluginLogger.info(pluginName + " " + pluginVersion + " has been disabled!");
 	}
 
 	public void enable() {
 		pluginListener.loadSettings();
 		for (PluginLoader.Hook hook : requiredHooks) {
 			pluginListeners.add(etc.getLoader().addListener(hook, pluginListener, this, PluginListener.Priority.MEDIUM));
 		}
 		pluginLogger.info(pluginName + " " + pluginVersion + " has been initialized!");
 	}
 
 }

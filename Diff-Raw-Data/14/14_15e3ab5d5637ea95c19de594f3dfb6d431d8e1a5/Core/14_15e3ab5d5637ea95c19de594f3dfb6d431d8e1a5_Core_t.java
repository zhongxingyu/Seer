 package me.footlights.core;
 
 import java.io.*;
 import java.util.*;
 import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
 
 import me.footlights.core.crypto.Keychain;
 import me.footlights.core.plugin.*;
 
 
 public class Core implements Footlights, KernelInterface
 {
 	public Core()
 	{
 		String configDirName = "~/.footlights/";
 
 		File configDir = new File(configDirName);
 		configDir.mkdir();
 
 		keychain = new Keychain();
 		final String keychainFileName = configDirName + "/keychain";
 
 		try { keychain.importKeystoreFile(new FileInputStream(new File(keychainFileName))); }
		catch (Exception e)
		{
			Logger.getLogger(Core.class.getName())
				.log(Level.WARNING, "Unable to open keychain at '" + keychainFileName + "'", e);
		}
 
 		plugins          = new HashMap<String,PluginWrapper>();
 		pluginServer     = new PluginServer(this);
 		uis              = new LinkedList<UI>();
 	}
 
 
 	public void registerUI(UI ui) { uis.add(ui); }
 	public void deregisterUI(UI ui) { uis.remove(ui); }
 
 	public Collection<PluginWrapper> plugins() { return plugins.values(); }
 
 
 	/** Load a plugin and wrap it up in a convenient wrapper */
 	public PluginWrapper loadPlugin(String url) throws PluginLoadException
 	{
 		if(plugins.containsKey(url)) return plugins.get(url);
 
 		PluginWrapper plugin = new PluginLoader(pluginServer).loadPlugin(url);
 
 		plugins.put(url, plugin);
 		for(UI ui : uis) ui.pluginLoaded(plugin);
 
 		return plugin;
 	}
 
 
 	/** Unload a plugin; after calling this, set ALL references to null */
 	public void unloadPlugin(PluginWrapper plugin)
 	{
 		String key = null;
 		for(Entry<String,PluginWrapper> e : plugins.entrySet())
 			if(e.getValue().equals(plugin))
 			{
 				key = e.getKey();
 				break;
 			}
 
 		if(key == null) return;
 
 		for(UI ui : uis) ui.pluginUnloading(plugins.get(key));
 		plugins.remove(key);
 	}
 
 
 	// KernelInterface implementation
 	public java.util.UUID generateUUID() { return java.util.UUID.randomUUID(); }
 
 
 	/** Name of the Core {@link Logger}. */
 	public static final String CORE_LOG_NAME = "me.footlights.core";
 
 	/** Our keychain */
 	private Keychain keychain;
 
 	/** Loaded plugins */
 	private Map<String,PluginWrapper> plugins;
 
 	/** Handles plugin requests */
 	private PluginServer pluginServer;
 
 	/** UIs which might like to be informed of events */
 	private List<UI> uis;
 }

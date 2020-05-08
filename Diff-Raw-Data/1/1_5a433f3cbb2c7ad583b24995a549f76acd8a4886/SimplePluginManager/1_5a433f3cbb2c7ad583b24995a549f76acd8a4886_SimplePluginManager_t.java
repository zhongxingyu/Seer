 package de.xgme.webcms.plugin;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class SimplePluginManager implements PluginManager {
 	private final List<Plugin> plugins = new ArrayList<Plugin>();
 	private final List<PluginLoader> loaders = new ArrayList<PluginLoader>();
 	private final Map<String, Plugin> lookupNames = new HashMap<String, Plugin>();
 	private final Map<Plugin, PluginLoader> origins = new HashMap<Plugin, PluginLoader>();
 
 	private Logger logger;
 
 	/**
 	 * Creates a new simple plug-in manager.
 	 */
 	public SimplePluginManager() {
 		this.logger = Logger.getLogger(SimplePluginManager.class.getName());
 	}
 
 	/**
 	 * Creates a new simple plug-in manager with the given logger to log.
 	 * @param logger Logger to use
 	 */
 	public SimplePluginManager(final Logger logger) {
 		this.logger = logger;
 	}
 
 	@Override
 	public void registerLoader(final PluginLoader loader) {
 		if (loader == null)
 			throw new NullPointerException("PluginLoader cannot be null");
 		
 		if (loaders.contains(loader)) {
 			loaders.add(loader);
 		}
 	}
 
 	@Override
 	public Plugin getPlugin(final String name) {
 		return lookupNames.get(name);
 	}
 
 	@Override
 	public Plugin[] getPlugins() {
 		return plugins.toArray(new Plugin[0]);
 	}
 
 	@Override
 	public boolean isPluginEnabled(final String name) {
 		return isPluginEnabled(getPlugin(name));
 	}
 
 	@Override
 	public boolean isPluginEnabled(final Plugin plugin) {
 		if (plugin != null && plugins.contains(plugin)) {
 			return plugin.isEnabled();
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public Plugin loadPlugin(final File file)
 			throws InvalidPluginException, UnknownDependencyException {
 		
 		if (file == null)
 			throw new IllegalArgumentException("File cannot be null");
 		
 		// get the plug-in loader
 		final PluginLoader loader = getPluginLoaderFrom(file);
 		
 		if (loader == null) return null;
 		
 		// TODO checkUpdate(file)
 		
 		Plugin result = loader.loadPlugin(file);
 		
 		plugins.add(result);
 		lookupNames.put(result.getDescription().getName(), result);
 		origins.put(result, loader);
 		
 		return result;
 	}
 
 	@Override
 	public Plugin[] loadPlugins(final File directory) {
 		if (!directory.isDirectory()) {
 			logger.warning("Plugin directory "+directory+" is not a directory");
 			return new Plugin[0];
 		}
 		
 		// get plug-in files to load
 		final Map<File,PluginLoader> fileLoaders = new HashMap<File,PluginLoader>();
 		{
 			File[] files = directory.listFiles();
 			for (File file : files) {
 				PluginLoader l = getPluginLoaderFrom(file);
 				if (l != null) {
 					fileLoaders.put(file, l);
 				}
 			}
 		}
 		
 		// set up dependencies, names of plug-ins and plug-in loading queue
 		final Map<File,List<String>> dependencies     = new HashMap<File,List<String>>();
 		final Map<File,List<String>> softDependencies = new HashMap<File,List<String>>();
 		final Set<String> newPluginNames = new HashSet<String>();
 		final Queue<File> filesToLoad = new LinkedList<File>(fileLoaders.keySet());
 		{
 			for (final File file : fileLoaders.keySet()) {
 				try {
 					final PluginLoader loader = fileLoaders.get(file);
 					final PluginDescription desc = loader.getPluginDescription(file);
 					
 					if (newPluginNames.contains(desc.getName()) || lookupNames.containsKey(desc.getName())) {
 						logger.severe("Could not load "+file+". There is already a plugin named "+desc.getName());
 						continue;
 					}
 					
 					dependencies.put(file, new ArrayList<String>(desc.getDepend()));
 					softDependencies.put(file, new ArrayList<String>(desc.getSoftDepend()));
 					newPluginNames.add(desc.getName());
 					filesToLoad.add(file);
 				} catch (InvalidDescriptionException e) {
 					logger.severe(file+" has a invalid plugin description. This plugin will not be loading");
 				}
 			}
 		}
 		
 		final List<Plugin> loadedPlugins = new ArrayList<Plugin>();
 		
 		File file;
 		pluginloading:
 		while ((file = filesToLoad.poll()) != null) {
 			// update dependencies (remove already available dependencies)
 			final List<String> newDependencies = new ArrayList<String>();
 			for (String dependency : dependencies.get(file)) {
 				if (lookupNames.containsKey(dependency)) {
 					continue;
 				}
 				if (newPluginNames.contains(dependency)) {
 					newDependencies.add(dependency);
 					continue;
 				}
 				
 				try {
 					PluginDescription desc = fileLoaders.get(file).getPluginDescription(file);
 					logger.severe(desc.getFullName()+" could not loaded because dependency "+dependency+" was not found");
 				} catch (InvalidDescriptionException e) {
 					logger.log(Level.SEVERE, "Unexpacted exceptoion. Please inform the developers", e);
 				}
 				dependencies.remove(file);
 				softDependencies.remove(file);
 				continue pluginloading;
 			}
 			final List<String> newSoftDependencies = new ArrayList<String>();
 			for (String dependency : softDependencies.get(file)) {
 				if (newPluginNames.contains(dependency)) {
 					newSoftDependencies.add(dependency);
 				}
 			}
 			
 			// check dependencies
 			if (!(newDependencies.isEmpty() && newSoftDependencies.isEmpty())) {
 				dependencies.put(file, newDependencies);
 				softDependencies.put(file, newSoftDependencies);
				filesToLoad.offer(file);
 				continue pluginloading;
 			}
 			
 			// load plug-in
 			PluginLoader loader = fileLoaders.get(file);
 			try {
 				loadedPlugins.add(loader.loadPlugin(file));
 			} catch (InvalidPluginException e) {
 				try {
 					PluginDescription desc = fileLoaders.get(file).getPluginDescription(file);
 					logger.log(Level.SEVERE, desc.getFullName()+" is not a valid plugin", e);
 				} catch (InvalidDescriptionException ue) {
 					logger.log(Level.SEVERE, "Unexpacted exceptoion. Please inform the developers", ue);
 				}
 			} catch (UnknownDependencyException e) {
 				try {
 					PluginDescription desc = fileLoaders.get(file).getPluginDescription(file);
 					logger.log(Level.SEVERE, desc.getFullName()+" has a unknown dependency", e);
 				} catch (InvalidDescriptionException ue) {
 					logger.log(Level.SEVERE, "Unexpacted exceptoion. Please inform the developers", ue);
 				}
 			} catch (Exception e) {
 				try {
 					PluginDescription desc = fileLoaders.get(file).getPluginDescription(file);
 					logger.log(Level.SEVERE, "Error occurred while loading "+desc.getFullName(), e);
 				} catch (InvalidDescriptionException ue) {
 					logger.log(Level.SEVERE, "Unexpacted exceptoion. Please inform the developers", ue);
 				}
 			}
 		}
 		
 		return loadedPlugins.toArray(new Plugin[0]);
 	}
 
 	@Override
 	public void disablePlugins() {
 		Plugin[] plugins = getPlugins();
 		for (int i = plugins.length-1; i >= 0; --i) {
 			disablePlugin(plugins[i]);
 		}
 	}
 
 	@Override
 	public void clearPlugins() {
 		disablePlugins();
 		
 		plugins.clear();
 		lookupNames.clear();
 		origins.clear();
 	}
 
 	@Override
 	public void enablePlugin(final Plugin plugin) throws Exception {
 		if (isPluginEnabled(plugin)) return;
 		
 		try {
 			origins.get(plugin).enablePlugin(plugin);
 		} catch (Exception e) {
 			logger.log(Level.SEVERE, "Error occurred while enabling "+plugin, e);
 			throw e;
 		}
 	}
 
 	@Override
 	public void disablePlugin(final Plugin plugin) {
 		if (!isPluginEnabled(plugin)) return;
 		
 		try {
 			origins.get(plugin).disablePlugin(plugin);
 		} catch (Exception e) {
 			logger.log(Level.SEVERE, "Error occurred while disabling "+plugin, e);
 		}
 		
 		// TODO unregister event handlers
 		// TODO unregister Tasks
 		// TODO unregister Services ?
 		// TODO call Event
 	}
 
 	private PluginLoader getPluginLoaderFrom(File file) {
 		for (PluginLoader loader : loaders) {
 			if (loader.getPluginFileFilter().accept(file)) {
 				return loader;
 			}
 		}
 		return null;
 	}
 
 }

 package com.ijg.darklight.sdk.loader;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 
 import com.ijg.darklight.sdk.core.Plugin;
 import com.ijg.darklight.sdk.core.PluginHandler;
 import com.ijg.darklight.sdk.core.ScoreModule;
 
 public class PluginLoader {
 
 	/**
 	 * Load all plugins from the plugins folder
 	 * @return An ArrayList of the instantiated plugins
 	 * @throws IOException
 	 */
 	public ArrayList<Plugin> loadPlugins(PluginHandler pluginHandler) throws IOException {
 		File root = new File(new File("."), "plugins");
 		if (root.exists() && root.isDirectory()) {
 			File[] fileList = root.getAbsoluteFile().listFiles();
 			ArrayList<Plugin> plugins = new ArrayList<Plugin>();
 			for (File plugin : fileList) {
				if (!plugin.getName().contains("Module")) {
 					String name = plugin.getName().substring(0, plugin.getName().indexOf("."));
 					System.out.println("Loading plugin: " + name + "...");
 					try {
 						plugins.add((Plugin) DarklightLoader.loadAndInstantiateJar("com.darklight.core.plugins." + name, plugin.getPath(), new Object[] { pluginHandler }, PluginHandler.class));
 					} catch (ClassNotFoundException | NoSuchMethodException
 							| SecurityException | InstantiationException
 							| IllegalAccessException | IllegalArgumentException
 							| InvocationTargetException e) {
 						System.err.println("Error loading plugin \"" + name
 								+ "\" from jar: " + plugin.getPath());
 						e.printStackTrace();
 					}
 				}
 			}
 			return plugins;
 		}
 		throw new FileNotFoundException("The plugins folder was not found. A plugins folder must be present in the same folder as Darklight to function.");
 	}
 	
 	/**
 	 * Load scoring modules from the plugins folder
 	 * @return An ArrayList of loaded scoring modules
 	 * @throws IOException
 	 */
 	public ArrayList<ScoreModule> loadScoreModules() throws IOException {
 		File root = new File(new File("."), "plugins");
 		if (root.exists() && root.isDirectory()) {
 			File[] fileList = root.getAbsoluteFile().listFiles();
 			ArrayList<ScoreModule> modules = new ArrayList<ScoreModule>();
 			for (File module : fileList) {
 				if (module.getName().contains("Module")) {
 					if (module.getName().endsWith(".jar")) {
 						String name = module.getName().substring(0, module.getName().indexOf("."));
 						System.out.println("Loading module: " + name + "...");
 						try {
 							modules.add((ScoreModule) DarklightLoader.loadAndInstantiateJar("com.darklight.core.scoring." + name, module.getPath()));
 						} catch (InstantiationException
 								| IllegalAccessException
 								| ClassNotFoundException e) {
 							System.err.println("Error loading module \"" + name
 									+ "\" from jar: " + module.getPath());
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 			return modules;
 		}
 		throw new FileNotFoundException("The plugins folder was not found. A plugins folder must be present in the same folder as Darklight to function.");
 	}
 }

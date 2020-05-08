 package com.titankingdoms.nodinchan.titanchat.addon;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import com.nodinchan.loader.Loader;
 import com.titankingdoms.nodinchan.titanchat.TitanChat;
 import com.titankingdoms.nodinchan.titanchat.debug.Debugger;
 
 /**
  * AddonManager - Manages Addons and executes them when needed
  * 
  * @author NodinChan
  *
  */
 public final class AddonManager {
 	
 	private static AddonManager instance;
 	
 	private static final Debugger db = new Debugger(2);
 	
 	private final List<Addon> addons;
 	
 	private final Map<Addon, JarFile> jarFiles;
 	
 	public AddonManager(TitanChat plugin) {
 		AddonManager.instance = this;
 		this.addons = new ArrayList<Addon>();
 		this.jarFiles = new HashMap<Addon, JarFile>();
 	}
 	
 	/**
 	 * Gets the Addon by its name
 	 * 
 	 * @param name The name of the Addon
 	 * 
 	 * @return The Addon if it exists, otherwise null
 	 */
 	public Addon getAddon(String name) {
 		for (Addon addon : addons) {
 			if (addon.getName().equalsIgnoreCase(name))
 				return addon;
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Gets the instance of this
 	 * 
 	 * @return AddonManager instance
 	 */
 	public static AddonManager getInstance() {
 		return instance;
 	}
 	
 	/**
 	 * Gets resource out of the JAR file of an Addon
 	 * 
 	 * @param addon The Addon 
 	 * 
 	 * @param fileName The file to look for
 	 * 
 	 * @return The file if found, otherwise null
 	 */
 	public InputStream getResource(Addon addon, String fileName) {
 		try {
 			JarFile jarFile = jarFiles.get(addon);
 			Enumeration<JarEntry> entries = jarFile.entries();
 			
 			while (entries.hasMoreElements()) {
 				JarEntry element = entries.nextElement();
 				
 				if (element.getName().equalsIgnoreCase(fileName))
 					return jarFile.getInputStream(element);
 			}
 			
 		} catch (IOException e) {}
 		
 		return null;
 	}
 	
 	/**
 	 * Loads the Addons
 	 */
 	public void load() {
 		Loader<Addon> loader = new Loader<Addon>(TitanChat.getInstance(), TitanChat.getInstance().getAddonDir(), new Object[0]);
 		for (Addon addon : loader.load()) { register(addon); }
 		List<Addon> backup = addons;
 		addons.clear();
 		addons.addAll(loader.sort(backup));
 	}
 	
 	/**
 	 * Registers the Addon
 	 * 
 	 * @param addon The Addon to register
 	 */
 	public void register(Addon addon) {
 		db.i("Registering addon: " + addon.getName());
 		addons.add(addon);
 	}
 	
 	public void reload() {
 		for (Addon addon : addons) { addon.reloadConfig(); }
		unload();
		load();
 	}
 	
 	/**
 	 * Saves the JAR files of the Addons for future use
 	 * 
 	 * @param addon The Addon
 	 * 
 	 * @param jarFile The JAR file of the Addon
 	 */
 	public void setJarFile(Addon addon, JarFile jarFile) {
 		jarFiles.put(addon, jarFile);
 	}
 	
 	/**
 	 * Sorts the Addons
 	 */
 	public void sortAddons() {
 		List<Addon> addons = new ArrayList<Addon>();
 		List<String> names = new ArrayList<String>();
 		
 		for (Addon addon : this.addons) {
 			names.add(addon.getName());
 		}
 		
 		Collections.sort(names);
 		
 		for (String name : names) {
 			addons.add(getAddon(name));
 		}
 		
 		this.addons.clear();
 		this.addons.addAll(addons);
 	}
 	
 	/**
 	 * Unloads the Addons
 	 */
 	public void unload() {
 		addons.clear();
 	}
 }

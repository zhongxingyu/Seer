 ﻿package com.bukkit.YurijWare.RealTime;
 
 import java.io.*;
 import java.util.logging.Logger;
 
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * RealTime for Bukkit
  * Main class
  * 
  * @author Yurij
  */
 public class RealTime extends JavaPlugin {
 	private final Logger log = Logger.getLogger("Minecraft");
 	protected static PluginDescriptionFile pdfFile = null;
 	protected static RealTime plugin = null;
 	protected static World world = null;
 	protected RTTimeThread thread = new RTTimeThread("RealTime");
 	
 	public RealTime(PluginLoader pluginLoader, Server instance,
 			PluginDescriptionFile desc, File folder, File plugin,
 			ClassLoader cLoader) throws IOException {
 		super(pluginLoader, instance, desc, folder, plugin, cLoader);
 	}
 	
 	public void onEnable() {
 		plugin = this;
		world = this.getServer().getWorlds().get(0);
 		pdfFile = this.getDescription();
 		
 		thread.start();
 		
 		log.info("[" + pdfFile.getName() + "] Version "
 				+ pdfFile.getVersion() + " is enabled!");
 	}
 	
 	public void onDisable() {
 		thread.stop();
 		log.info("[" + pdfFile.getName() + "] "
 				+ "Plugin is disabled!");
 	}
 	
 }

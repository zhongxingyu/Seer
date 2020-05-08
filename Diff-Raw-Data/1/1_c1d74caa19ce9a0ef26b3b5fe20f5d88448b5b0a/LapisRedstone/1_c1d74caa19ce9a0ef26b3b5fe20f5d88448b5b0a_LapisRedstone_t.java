 package com.echo28.bukkit.lapisredstone;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import org.bukkit.Server;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 
 /**
  * LapisRedstone for Bukkit
  * 
  * @author Nodren
  */
 public class LapisRedstone extends JavaPlugin
 {
 	private final LapisRedstoneBlockListener blockListener = new LapisRedstoneBlockListener(this);
 	private final Logger log = Logger.getLogger("Minecraft");
 	private int MIN;
 	private int MAX;
 	private Configuration config;
 
 	public LapisRedstone(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader)
 	{
 		super(pluginLoader, instance, desc, folder, plugin, cLoader);
 		config = new Configuration(new File("plugins/lapisredstone.yml"));
		config.load();
 		MIN = config.getInt("min", 2);
 		MAX = config.getInt("max", 4);
 	}
 
 	public void onDisable()
 	{
 		log.info(getDescription().getName() + " " + getDescription().getVersion() + " unloaded.");
 	}
 
 	public void onEnable()
 	{
 		log.info(getDescription().getName() + " " + getDescription().getVersion() + " loaded.");
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Monitor, this);
 	}
 
 	public int random()
 	{
 		return MIN + (int) (Math.random() * ((MAX - MIN) + 1));
 	}
 
 }

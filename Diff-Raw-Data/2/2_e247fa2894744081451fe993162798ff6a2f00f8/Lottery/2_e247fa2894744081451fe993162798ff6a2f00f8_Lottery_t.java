 package com.bukkit.erbros.Lottery;
 //All the imports
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 
 public class Lottery extends JavaPlugin{
 
 	// Doing some logging. Thanks cyklo 
 	protected final Logger log;
 	protected Integer cost;
 	protected Integer hours;
 	
 	public Lottery() {
 		log = Logger.getLogger("Minecraft");
 		cost = 5;
 		hours = 24;
 		
 	}
 	@Override
 	public void onDisable() {
 		System.out.println("Lottery disabled successfully.");
 	}
 
 	@Override
 	public void onEnable() {
 		
 		// Gets version number and writes out starting line to console.
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled" );
 		
 		// Start Registration. Thanks TheYeti.
 		getDataFolder().mkdirs();
 		
 		// Does config file exist? Thanks cyklo :)
 		File yml = new File(getDataFolder(), "config.yml");
 
 		if (!yml.exists())
 		{
 			try {
 				yml.createNewFile();
 				getConfiguration().setProperty("lottery", null);
 				getConfiguration().save();
 			} catch (IOException ex){
 				log.warning(getDescription().getName() + ": could not generate config.yml. Are the file permissions OK?");
 			}
 		}
 		
 		// Load in the values from the configuration file
 
 		List <String> keys = getConfiguration().getKeys("config.yml");
 
		if(keys == null || !keys.contains("lottery")) {
 			log.warning(getDescription().getName() + ": configuration file is corrupt. Please delete it and start over.");
 			return;
 		}
 
 		if(keys.contains("cost")) {
 			cost = getConfiguration().getInt("cost",5);
 		} else {
 			getConfiguration().setProperty("cost", 5);
 			getConfiguration().save();
 		}
 
 		if(keys.contains("cost")) {
 			cost = getConfiguration().getInt("cost", 5);
 		} else {
 			getConfiguration().setProperty("cost", 5);
 			getConfiguration().save();
 		}
 
 		if(keys.contains("hours")) {
 			hours = getConfiguration().getInt("hours", 5);
 		} else {
 			getConfiguration().setProperty("hours", 5);
 			getConfiguration().save();
 		}
 				
 	}
 
 }

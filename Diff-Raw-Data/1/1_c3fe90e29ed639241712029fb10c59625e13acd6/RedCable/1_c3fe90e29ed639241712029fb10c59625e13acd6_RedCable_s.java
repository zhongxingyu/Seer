 package org.ccdd.redcable;
 
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.ccdd.redcable.listeners.SpeakerWireListener;
 import org.ccdd.redcable.materials.blocks.Blocks;
 import org.ccdd.redcable.materials.items.Items;
 import org.ccdd.redcable.util.Recipies;
 import org.ccdd.redcable.util.ResourceManager;
 
 
 public class RedCable extends JavaPlugin {
 
 	public static RedCable instance;
 	
 	Blocks blocks;
 	Items items;
 	
 	public void onEnable() {
 		//copy the config
 		this.getConfig().options().copyDefaults(true);
 		this.saveConfig();
 		
 		//double check for spout.
 		if (!Bukkit.getPluginManager().isPluginEnabled("Spout")) {
 			Bukkit.getLogger().log(Level.WARNING, "[RedCable] Could not start: Spout not found.");
 			setEnabled(false);
 			return;
 		}
 		
 		instance = this;
 		
 		ResourceManager.copyResources();	
 		
 		items = new Items();
 		blocks = new Blocks();
 		
 		Recipies.load();
 		
 		this.getServer().getPluginManager().registerEvents(new SpeakerWireListener(), this);
 		
 		//this.getCommand("redcable").setExecutor(new CommandHandler());
 		
 		ResourceManager.resetCache();
 		
 		
 	}
 	
 	public void onDisable() {
 		Bukkit.getLogger().log(Level.INFO, "[RedCable] Disabled");
 	}
 }

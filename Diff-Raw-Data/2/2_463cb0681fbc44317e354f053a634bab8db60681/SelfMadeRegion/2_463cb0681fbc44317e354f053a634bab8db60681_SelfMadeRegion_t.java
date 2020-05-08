 package de.whichdesign.selfmaderegion;
 
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.whichdesign.selfmaderegion.commands.Plot;
 import de.whichdesign.selfmaderegion.listeners.PlayerInteractEvent;
 
 /**
  * 
 * @author Blockhaus2000
  * 
  */
 public class SelfMadeRegion extends JavaPlugin {
     private Plugin plugin;
 
     public SelfMadeRegion() {
 	this.plugin = this;
     }
 
     public void onEnable() {
 	// Commands
 	getCommand("plot").setExecutor(new Plot(this));
 
 	// Events
 	getServer().getPluginManager().registerEvents(new PlayerInteractEvent(plugin), plugin);
 
 	// Other
 	loadConfig();
 	System.out.println("[SelfMadeRegion] by " + Configuration.author);
     }
 
     public void onDisable() {
 	List<String> paths = Configuration.allPaths;
 	Map<String, Integer> regions = Configuration.regions;
 	FileConfiguration config = plugin.getConfig();
 
 	for (String path : paths) {
 	    config.set(path.replace("|", "."), regions.get(path));
 	}
 
 	config.options().copyDefaults(true);
 	plugin.saveConfig();
     }
 
     private void loadConfig() {
 	getConfig().addDefault("general.wand", 280);
 	getConfig().options().copyDefaults(true);
 	saveConfig();
     }
 }

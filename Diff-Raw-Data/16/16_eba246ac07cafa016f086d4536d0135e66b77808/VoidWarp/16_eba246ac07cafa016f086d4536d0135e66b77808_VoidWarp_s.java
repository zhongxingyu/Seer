 package me.flungo.bukkit.VoidWarp;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class VoidWarp extends JavaPlugin {
 	
 	public static VoidWarp plugin;
 	
 	public final Logger logger = Logger.getLogger("MineCraft");
 	
 	public final PlayerListeners playerListener = new PlayerListeners(this);
 	
 	public void onDisable() {
		PluginDescriptionFile pdffile = this.getDescription();
		this.logger.info(pdffile.getName() + " is now disabled");
 	}
 	
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(this.playerListener, this);
		PluginDescriptionFile pdffile = this.getDescription();
 		getConfig().options().copyDefaults(true);
 		saveConfig();
		this.logger.info(pdffile.getName() + " version " + pdffile.getVersion() + " is enabled.");
 	}
 	
 	public Location getWarpLocation(Player p) {
 		String wName = getConfig().getString("destination.world");
 		Location loc;
 		if (getServer().getWorlds().contains(wName)) {
 			World w = getServer().getWorld(wName);
 			int x = getConfig().getInt("destination.x");
 			int z = getConfig().getInt("destination.z");
 			int y = w.getHighestBlockYAt(x, z) + getConfig().getInt("drop-height");
 			loc = new Location(w, x, y, z);
 		} else {
 			World w = p.getWorld();
 			loc = w.getSpawnLocation();
			loc.setY(w.getHighestBlockYAt(loc));
 		}
 		return loc;
 	}
 }

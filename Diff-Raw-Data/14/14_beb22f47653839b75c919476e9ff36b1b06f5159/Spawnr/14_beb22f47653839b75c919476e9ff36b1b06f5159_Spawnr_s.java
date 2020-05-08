 package com.cydoniarp.amd3th.spawnr;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Spawnr extends JavaPlugin {
 	private final SpawnPlayerListener pListener = new SpawnPlayerListener(this);
 	private static Logger log = Logger.getLogger("Minecraft");
 	
 	public static Property properties;
	public static Property users = new Property("plugins/Spawnr/users.spawn");
 	
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_JOIN, this.pListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_RESPAWN, this.pListener, Event.Priority.Normal, this);
 		PluginDescriptionFile pdf = this.getDescription();
 		log.info("["+pdf.getName()+"] v"+pdf.getVersion()+" has been enabled");
 		if (!(new File("plugins/Spawnr").isDirectory())) {
 			(new File("plugins/Spawnr")).mkdir();
 		}
		properties = new Property("plugins/Spawnr/conf.spawn");
 	}
 	
 	public void onDisable() {
 		PluginDescriptionFile pdf = this.getDescription();
 		log.info("[" + pdf.getName() + "] v" + pdf.getVersion() + " has been disabled.");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
 		String cmdName = cmd.getName();
 		if (sender instanceof Player){
 			if (((Player)sender).isOp()) {
 				if (cmdName.equalsIgnoreCase("spawnr")){
 					Location loc = ((Player)sender).getLocation();
 					properties.setDouble("x", loc.getX());
 					properties.setDouble("y", loc.getY());
 					properties.setDouble("z", loc.getZ());
 					properties.setFloat("yaw", loc.getYaw());
 					((Player)sender).sendMessage("Spawnr point set. :)");
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 }

 package com.mcnsa.mcnsachat2;
 
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 public class MCNSAChat2 extends JavaPlugin {
 	// load the minecraft logger
 	Logger log = Logger.getLogger("Minecraft");
 	
 	// keep track of permissions
 	public PermissionManager permissions = null;
 	
 	public void onEnable() {
 		// set up permissions
 		this.setupPermissions();
 		
 		// import the plugin manager
 		PluginManager pm = this.getServer().getPluginManager();
 		
 		// routines for when the plugin gets enabled
 		log("plugin enabled!");
 	}
 	
 	public void onDisable() {
 		// shut the plugin down
 		log("plugin disabled!");
 	}
 	
 	// for simpler logging
 	public void log(String info) {
 		log.info("[MCNSAChat2] " + info);
 	}
 	
	// for error reporting
	public void error(String info) {
		log.info("[MCNSAChat2] <ERROR> " + info);
	}
	
 	// for debugging
 	// (disable for final release)
 	public void debug(String info) {
		log.info("[MCNSAChat2] <DEBUG> " + info);
 	}
 
 	// load the permissions plugin
 	public void setupPermissions() {
 		if(Bukkit.getServer().getPluginManager().isPluginEnabled("PermissionsEx")) {
 			this.permissions = PermissionsEx.getPermissionManager();
			log.info("permissions successfully loaded!");
 		}
 		else {
			error("PermissionsEx not found!");
 		}
 	}
 	
 	// just an interface function for checking permissions
 	// if permissions are down, default to OP status.
 	public boolean hasPermission(Player player, String permission) {
 		if(permissions != null) {
 			return permissions.has(player, permission);
 		}
 		else {
 			return player.isOp();
 		}
 	}
 	
 	// allow for colour tags to be used in strings..
 	public String processColours(String str) {
 		return str.replaceAll("(&([a-f0-9]))", "\u00A7$2");
 	}
 	
 	// strip colour tags from strings..
 	public String stripColours(String str) {
 		return str.replaceAll("(&([a-f0-9]))", "");
 	}
 	
 	// return a message to a command sender
 	public void returnMessage(CommandSender sender, String msg) {
 		if(sender instanceof Player) {
 			sender.sendMessage(processColours(message));
 		}
 		else {
 			sender.sendMessage(stripColours(message));
 		}
 	}
 }

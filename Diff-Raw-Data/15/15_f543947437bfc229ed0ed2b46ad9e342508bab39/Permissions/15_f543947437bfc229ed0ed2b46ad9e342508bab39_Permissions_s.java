 package me.flungo.bukkit.VoidWarp;
 
 import java.util.logging.Level;
 
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 public class Permissions {
 	public static VoidWarp plugin;
 	
 	public Permissions(VoidWarp instance) {
 		plugin = instance;
 	}
 	
 	private static boolean bukkit;
 	
 	private static boolean vault;
 	
 	private static Permission vaultPermission = null;
 	
 	private void setupBukkitPermissions() {
 		if (plugin.getConfig().getBoolean("permissions.bukkit")) {
 			bukkit = true;
 		} else {
 			bukkit = false;
 		}
 	}
 	
 	private void setupVaultPermissions() {
 		if (plugin.getConfig().getBoolean("permissions.vault")) {
 			plugin.logMessage("Attempting to configure Vault permissions");
 			if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
 				plugin.logMessage("Vault could not be found", Level.SEVERE);
 				vault = false;
 			} else {
 				RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
 		        if (permissionProvider != null) {
 		        	vaultPermission = permissionProvider.getProvider();
 		        }
 				if (vaultPermission != null)
 					vault = true;
 				else 
 					vault = false;
 			}
 		} else {
 			plugin.logMessage("Vault permissions disabled by config");
 			vault = false;
 		}
     }
 	
 	public void setupPermissions() {
 		setupBukkitPermissions();
 		if (bukkit) {
 			plugin.logMessage("Bukkit Super Permissions set up");
 		} else {
			plugin.logMessage("Vault permissions not set up");
 		}
 		setupVaultPermissions();
 		if (vault) {
 			plugin.logMessage("Vault permissions set up");
 		} else {
			plugin.logMessage("Vault permissions not set up");
 		}
 	}
 	
 	private boolean hasNode(Player p, String node) {
 		if (bukkit && p.hasPermission(node)) return true;
 		if (vault && vaultPermission.has(p, node)) return true;
 		return false;
 	}
 	
 	public boolean isAdmin(Player p) {
 		if (p.isOp() && plugin.getConfig().getBoolean("permissions.op")) return true;
 		if (plugin.getConfig().getBoolean("permissions.nodes.admin")) {
 			String node = "voidwarp.admin";
 			if (hasNode(p, node)) return true;
 		}
 		return false;
 	}
 	
 	public boolean isUser(Player p) {
 		if (!plugin.getConfig().getBoolean("enable")) return false;
 		if (plugin.getConfig().getBoolean("permissions.nodes.user")) {
 			String node = "voidwarp.user";
 			if (hasNode(p, node)) return true;
 		}
 		if (isAdmin(p)) return true;
 		return false;
 	}
 }

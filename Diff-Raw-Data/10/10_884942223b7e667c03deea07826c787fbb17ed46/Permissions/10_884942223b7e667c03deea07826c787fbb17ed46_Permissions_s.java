 /*
  * Copyright (C) 2013 Fabrizio Lungo
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package me.flungo.bukkit.randomwarp;
 
 import java.util.logging.Level;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import net.milkbowl.vault.permission.Permission;
 
 public class Permissions {
 	public static JavaPlugin plugin;
 	
 	private static String prefix;
 	
 	public Permissions(JavaPlugin instance, String prefix) {
 		this.plugin = instance;
 		this.prefix = prefix;
 	}
 	
	public Permissions(JavaPlugin instance) {
		this(instance, instance.getName().replace("\\s", "").toLowerCase());
	}
	
 	private static boolean op;
 	
 	private static boolean bukkit;
 	
 	private static boolean vault;
 	
 	private static Permission vaultPermission = null;
 	
 	private void setupOPPermissions() {
 		if (plugin.getConfig().getBoolean("permissions.op", true)) {
 			plugin.getLogger().log(Level.INFO, "Attempting to configure OP permissions");
 			op = true;
 		} else {
 			plugin.getLogger().log(Level.INFO, "OP permissions disabled by config");
 			op = false;
 		}
 	}
 	
 	private void setupBukkitPermissions() {
 		if (plugin.getConfig().getBoolean("permissions.bukkit", true)) {
 			plugin.getLogger().log(Level.INFO, "Attempting to configure Bukkit Super Permissions");
 			bukkit = true;
 		} else {
 			plugin.getLogger().log(Level.INFO, "Bukkit Super Permissions disabled by config");
 			bukkit = false;
 		}
 	}
 	
 	private void setupVaultPermissions() {
 		if (plugin.getConfig().getBoolean("permissions.vault", false)) {
 			plugin.getLogger().log(Level.INFO, "Attempting to configure Vault permissions");
 			if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
 				plugin.getLogger().log(Level.SEVERE, "Vault could not be found");
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
 			plugin.getLogger().log(Level.INFO, "Vault permissions disabled by config");
 			vault = false;
 		}
     }
 	
 	public void setupPermissions(String nodePrefix) {
 		setupOPPermissions();
 		if (op) {
 			plugin.getLogger().log(Level.INFO, "OP permissions set up");
 		} else {
 			plugin.getLogger().log(Level.INFO, "OP permissions not set up");
 		}
 		setupBukkitPermissions();
 		if (bukkit) {
 			plugin.getLogger().log(Level.INFO, "Bukkit Super Permissions set up");
 		} else {
 			plugin.getLogger().log(Level.INFO, "Bukkit Super Permissions not set up");
 		}
 		setupVaultPermissions();
 		if (vault) {
 			plugin.getLogger().log(Level.INFO, "Vault permissions set up");
 		} else {
 			plugin.getLogger().log(Level.INFO, "Vault permissions not set up");
 		}
 		if (!vault && !bukkit) {
 			plugin.getLogger().log(Level.WARNING, "No permission systems have been set up. Default permissions will be used.");
 			if (!op) {
 				plugin.getLogger().log(Level.WARNING, "Additionally, OP permissions disabled.");
 			}
 		}
 	}
 	
 	public void setupPermissions() {
 		String nodePrefix = plugin.getDescription().getClass().toString().toLowerCase();
 		setupPermissions(nodePrefix);
 	}
 	
 	private boolean hasNode(Player p, String node) {
 		if (bukkit && p.hasPermission(node)) return true;
 		if (vault && vaultPermission.has(p, node)) return true;
 		return false;
 	}
 	
 	public boolean hasPermission(Player p, String permission) {
 		if (plugin.getConfig().getBoolean("permissions.default." + permission, false)) return true;
 		String node = prefix + "." + permission;
 		if (hasNode(p, node)) return true;
 		return false;
 	}
 	
 	public boolean isAdmin(Player p) {
 		if (p.isOp() && op) return true;
		String node = prefix + "admin";
 		if (hasNode(p, node)) return true;
 		return false;
 	}
 	
 	public boolean isUser(Player p) {
 		if (!plugin.getConfig().getBoolean("enable", true)) return false;
		String node = prefix + "user";
 		if (hasNode(p, node)) return true;
 		if (isAdmin(p)) return true;
 		if (!bukkit && !vault) return true;
 		return false;
 	}
 	
 }

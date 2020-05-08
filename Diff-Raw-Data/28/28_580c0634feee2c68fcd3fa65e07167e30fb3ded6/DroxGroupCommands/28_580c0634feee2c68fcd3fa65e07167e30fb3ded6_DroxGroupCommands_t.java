 package de.hydrox.bukkit.DroxPerms;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class DroxGroupCommands implements CommandExecutor {
     
     private DroxPerms plugin;
 
     public DroxGroupCommands(DroxPerms plugin) {
         this.plugin = plugin;
     }
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		if (!(sender.hasPermission("droxperms.groups"))) {
			sender.sendMessage("You don't have permission to modify Groups.");
			return true;
		}
 		boolean result = false;
 		if (split.length == 0) {
 			// display help
 			return false;
 		}
 		// add permission
 		if (split[0].equalsIgnoreCase("addperm")) {
 			if (split.length == 3) {
 				// add global permission
 				result = plugin.dataProvider.addGroupPermission(sender, split[1], null, split[2]);
 			} else if (split.length == 4) {
 				// add world permission
 				result = plugin.dataProvider.addGroupPermission(sender, split[1], split[3], split[2]);
 			}
 			plugin.refreshPermissions();
 			return result;
 		}
 
 		// remove permission
 		if (split[0].equalsIgnoreCase("remperm")) {
 			if (split.length == 3) {
 				// remove global permission
 				result = plugin.dataProvider.removeGroupPermission(sender, split[1], null, split[2]);
 			} else if (split.length == 4) {
 				// remove world permission
 				result = plugin.dataProvider.removeGroupPermission(sender, split[1], split[3], split[2]);
 			}
 			plugin.refreshPermissions();
 			return result;
 		}
 
 		// add subgroup
 		if (split[0].equalsIgnoreCase("addsub")) {
 			if (split.length == 3) {
 				result = plugin.dataProvider.addGroupSubgroup(sender, split[1], split[2]);
 			}
 			plugin.refreshPermissions();
 			return result;
 		}
 
 		// remove subgroup
 		if (split[0].equalsIgnoreCase("remperm")) {
 			if (split.length == 3) {
 				result = plugin.dataProvider.removeGroupSubgroup(sender, split[1],split[2]);
 			}
 			plugin.refreshPermissions();
 			return result;
 		}
 
 		// add new group
 		if (split[0].equalsIgnoreCase("new")) {
 			if (split.length == 2) {
 				return plugin.dataProvider.createGroup(sender, split[1]);
 			} else {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 }

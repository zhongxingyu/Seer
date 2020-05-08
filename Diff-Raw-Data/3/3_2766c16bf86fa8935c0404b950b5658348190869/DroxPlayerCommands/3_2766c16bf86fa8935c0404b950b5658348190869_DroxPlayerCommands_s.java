 package de.hydrox.bukkit.DroxPerms;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class DroxPlayerCommands implements CommandExecutor {
     
     private DroxPerms plugin;
 
     public DroxPlayerCommands(DroxPerms plugin) {
         this.plugin = plugin;
     }
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
 		Player caller = null;
 		if (sender instanceof Player) {
 			caller = (Player) sender;
 		}
 		boolean result = false;
 		if (split.length == 0) {
 			return false;
 		} else if (caller != null && caller.getName().equalsIgnoreCase(split[1])
 				&& !(sender.hasPermission("droxperms.players.self"))) {
 			sender.sendMessage("You don't have permission to modify your Permissions.");
 			return true;			
		} else if (!(sender.hasPermission("droxperms.players.others"))) {
			sender.sendMessage("You don't have permission to modify other Players Permissions.");
			return true;
 		}
 		// add permission
 		if (split[0].equalsIgnoreCase("addperm")) {
 			if (split.length == 3) {
 				// add global permission
 				result = plugin.dataProvider.addPlayerPermission(sender, split[1], null, split[2]);
 			} else if (split.length == 4) {
 				// add world permission
 				result = plugin.dataProvider.addPlayerPermission(sender, split[1], split[3], split[2]);
 			}
 			plugin.refreshPlayer(plugin.getServer().getPlayer(split[1]));
 			return result;
 		}
 
 		// remove permission
 		if (split[0].equalsIgnoreCase("remperm")) {
 			if (split.length == 3) {
 				// remove global permission
 				result = plugin.dataProvider.removePlayerPermission(sender, split[1], null, split[2]);
 			} else if (split.length == 4) {
 				// remove world permission
 				result = plugin.dataProvider.removePlayerPermission(sender, split[1], split[3], split[2]);
 			}
 			plugin.refreshPlayer(plugin.getServer().getPlayer(split[1]));
 			return result;
 		}
 
 		// add subgroup
 		if (split[0].equalsIgnoreCase("addsub")) {
 			if (split.length == 3) {
 				result = plugin.dataProvider.addPlayerSubgroup(sender, split[1], split[2]);
 			}
 			plugin.refreshPlayer(plugin.getServer().getPlayer(split[1]));
 			return result;
 		}
 
 		// remove subgroup
 		if (split[0].equalsIgnoreCase("remperm")) {
 			if (split.length == 3) {
 				result = plugin.dataProvider.removePlayerSubgroup(sender, split[1],split[2]);
 			}
 			plugin.refreshPlayer(plugin.getServer().getPlayer(split[1]));
 			return result;
 		}
 
 		// set group
 		if (split[0].equalsIgnoreCase("setgroup")) {
 			if (split.length == 3) {
 				result = plugin.dataProvider.setPlayerGroup(sender, split[1],split[2]);
 			}
 			plugin.refreshPlayer(plugin.getServer().getPlayer(split[1]));
 			return result;
 		}
 
 		// set group
 		if (split[0].equalsIgnoreCase("has")) {
 			if (split.length == 3) {
 				result = plugin.getServer().getPlayer(split[1]).hasPermission(split[2]);
 				if (result) {
 					sender.sendMessage(split[1] + " has permission for " + split[2]);
 				} else {
 					sender.sendMessage(split[1] + " doesn't have permission for " + split[2]);
 				}
 			}
 		}
 		return true;
 	}
 
 }

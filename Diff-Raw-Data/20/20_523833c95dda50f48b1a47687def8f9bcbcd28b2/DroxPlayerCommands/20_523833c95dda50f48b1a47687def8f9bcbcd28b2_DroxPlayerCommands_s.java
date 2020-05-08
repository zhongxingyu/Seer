 package de.hydrox.bukkit.DroxPerms;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import de.hydrox.bukkit.DroxPerms.data.IDataProvider;
 
 public class DroxPlayerCommands implements CommandExecutor {
     
     private DroxPerms plugin;
     private IDataProvider dp;
 
     public DroxPlayerCommands(DroxPerms plugin) {
         this.plugin = plugin;
         dp = plugin.dataProvider;
     }
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
 		if (!(sender.hasPermission("droxperms.players"))) {
 			sender.sendMessage("You don't have permission to modify Players.");
 			return true;
 		}
         dp = plugin.dataProvider;
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
 		}
 		// add permission
 		if (split[0].equalsIgnoreCase("addperm")) {
 			if (split.length == 3) {
 				// add global permission
 				result = dp.addPlayerPermission(sender, split[1], null, split[2]);
 			} else if (split.length == 4) {
 				// add world permission
 				result = dp.addPlayerPermission(sender, split[1], split[3], split[2]);
 			}
 			plugin.refreshPlayer(plugin.getServer().getPlayer(split[1]));
 			return result;
 		}
 
 		// remove permission
 		if (split[0].equalsIgnoreCase("remperm")) {
 			if (split.length == 3) {
 				// remove global permission
 				result = dp.removePlayerPermission(sender, split[1], null, split[2]);
 			} else if (split.length == 4) {
 				// remove world permission
 				result = dp.removePlayerPermission(sender, split[1], split[3], split[2]);
 			}
 			plugin.refreshPlayer(plugin.getServer().getPlayer(split[1]));
 			return result;
 		}
 
 		// add subgroup
 		if (split[0].equalsIgnoreCase("addsub")) {
 			if (split.length == 3) {
 				result = dp.addPlayerSubgroup(sender, split[1], split[2]);
 			}
 			plugin.refreshPlayer(plugin.getServer().getPlayer(split[1]));
 			return result;
 		}
 
 		// remove subgroup
 		if (split[0].equalsIgnoreCase("remsub")) {
 			if (split.length == 3) {
 				result = dp.removePlayerSubgroup(sender, split[1],split[2]);
 			}
 			plugin.refreshPlayer(plugin.getServer().getPlayer(split[1]));
 			return result;
 		}
 
 		// set group
 		if (split[0].equalsIgnoreCase("setgroup")) {
 			if (split.length == 3) {
 				result = dp.setPlayerGroup(sender, split[1],split[2]);
 			}
 			plugin.refreshPlayer(plugin.getServer().getPlayer(split[1]));
 			return result;
 		}
 
 		// set group
 		if (split[0].equalsIgnoreCase("has")) {
 			if (split.length == 3) {
 				Player player = plugin.getServer().getPlayer(split[1]);
 				if (player == null) {
 					sender.sendMessage(split[1] + " is not online");
 				} else {
 					result = player.hasPermission(split[2]);
 					if (result) {
 						sender.sendMessage(split[1] + " has permission for " + split[2]);
 					} else {
 						sender.sendMessage(split[1] + " doesn't have permission for " + split[2]);
 					}
 				}
 			}
 		}
 
 		if (split[0].equalsIgnoreCase("listperms")) {
			if (split.length == 2) {
 				HashMap<String, ArrayList<String>> permissions = null;
 				if (split.length == 3) {
 					permissions = dp.getPlayerPermissions(split[1], split[2]);
				} else {
 					permissions = dp.getPlayerPermissions(split[1], null);
 				}
 				sender.sendMessage(split[1] + " has permission from group: " + dp.getPlayerGroup(split[1]));
 				ArrayList<String> subgroups = dp.getPlayerSubgroups(split[1]);
 				if (subgroups != null && subgroups.size() > 0) {
 					StringBuilder string = new StringBuilder();
 					string.append(split[1] + " has permission from subgroups:");
 					for (String subgroupstring : subgroups) {
 						string.append(" " + subgroupstring);
 					}
 					sender.sendMessage(string.toString());
 				}
 				ArrayList<String> globalperms = permissions.get("global");
 				if (globalperms != null && globalperms.size() > 0) {
 					StringBuilder string = new StringBuilder();
 					string.append(split[1] + " has permission globalpermissions:");
 					for (String globalstring : globalperms) {
 						string.append(" " + globalstring);
 					}
 					sender.sendMessage(string.toString());
 				}
 				ArrayList<String> worldperms = permissions.get("world");
 				if (worldperms != null && worldperms.size() > 0) {
 					StringBuilder string = new StringBuilder();
 					string.append(split[1] + " has permission worldpermissions:");
					for (String globalstring : worldperms) {
						string.append(" " + globalstring);
 					}
 					sender.sendMessage(string.toString());
 				}
 			}
 		}
 
 		// promote
 		if (split[0].equalsIgnoreCase("promote")) {
 			if (split.length == 3) {
 				result = dp.promotePlayer(sender, split[1], split[2]);
 			}
 			plugin.refreshPlayer(plugin.getServer().getPlayer(split[1]));
 			return result;
 		}
 
 		// demote
 		if (split[0].equalsIgnoreCase("demote")) {
 			if (split.length == 3) {
 				result = dp.demotePlayer(sender, split[1], split[2]);
 			}
 			plugin.refreshPlayer(plugin.getServer().getPlayer(split[1]));
 			return result;
 		}
 
 		return true;
 	}
 }

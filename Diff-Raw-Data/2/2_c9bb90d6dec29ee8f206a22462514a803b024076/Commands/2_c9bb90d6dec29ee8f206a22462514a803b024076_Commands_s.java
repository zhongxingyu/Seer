 package me.shock.playervaults;
 
 import me.shock.playervaults.Main;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class Commands implements CommandExecutor {
 
 	public Main plugin;
 
 	public Commands(Main instance) {
 		this.plugin = instance;
 	}
 
 	String pv = ChatColor.DARK_RED + "[" + ChatColor.WHITE + "PlayerVaults"
 			+ ChatColor.DARK_RED + "]" + ChatColor.WHITE + ": ";
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 		/**
 		 * No point in letting console run a command. Let's just cancel that
 		 * right away.
 		 */
 		if (cmd.getName().equalsIgnoreCase("vault")) {
 			if (!(sender instanceof Player)) {
 				sender.sendMessage("[PlayerVaults] Sorry but the console can't have a vault :(");
 				return true;
 			} else {
 				Player player = (Player) sender;
 				if (args.length == 1) {
 					if (args[0].matches("[1-9]")) {
 						if (player.hasPermission("playervaults.amount."
 								+ args[0])) {
 							plugin.openLargeVault(args[0], player);
 							player.sendMessage(pv + "Opening vault "
 									+ ChatColor.GREEN + args[0]);
 						} else {
 							player.sendMessage(pv
 									+ "You don't have permission for that many chests!");
 						}
 					} else {
 						player.sendMessage(pv + "Syntax is: /vault <number>");
 						return true;
 					}
 				} else {
 					player.sendMessage(pv + "Syntax is: /vault <number>");
 					return true;
 				}
 				if (args.length == 2) {
 					if (args[0].equals("delete")) {
						if (player.hasPermission("playervault.delete")) {
 							if (args[1].matches("[1-9]")) {
 								plugin.deletePlayerVault(args[1], player);
 								player.sendMessage(pv+" successfully deleted your vault")
 								return true;
 							}
 						}
 					}
 				}
 				if (args.length > 1) {
 					if (args[0].equalsIgnoreCase("delete")) {
 						if (player.hasPermission("playervaults.admin")) {
 							Player target = plugin.getServer().getPlayerExact(
 									args[1]);
 							if (!target.isOnline()) {
 								player.sendMessage(pv + target.getName()
 										+ " is offline!");
 								return false;
 							}
 							if (args[2].matches("[1-9]")) {
 								plugin.deletePlayerVault(args[2], target);
 								player.sendMessage(pv+" successfully deleted "+ args[1] + " vault");
 								return true;
 							}
 						}
 					}
 				} else {
 					player.sendMessage(pv
 							+ "Syntax error! usage: /vault delete <player> <#> <optional params>");
 				}
 			}
 		}
 
 		return false;
 	}
 }

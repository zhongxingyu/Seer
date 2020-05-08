 package me.chriznight.cosmicshift;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * @author ChrizNight
  * 
  */
 public class CosmicShiftCommandExecutor implements CommandExecutor {
 	private CosmicShift plugin;
 	private GameMode gm;
 	private final GameMode S = GameMode.SURVIVAL;
 	private final GameMode C = GameMode.CREATIVE;
 
 	public CosmicShiftCommandExecutor(CosmicShift plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		if (cmd.getName().equalsIgnoreCase("cs") && args.length > 0) {
 			if (args[0].equalsIgnoreCase("shift")) {
 				if (args.length == 1) {
 					if (player == null) {
 						plugin.send(sender, "You can only run this in game!");
 						return true;
 					}
 					if (!player.hasPermission(Permissions.CS)) {
 						plugin.send(sender,
 								"You don't have permission to run this command!");
 						return true;
 					}
 					gm = player.getGameMode();
 					if (gm == C)
 						player.setGameMode(S);
 					else if (gm == S)
 						player.setGameMode(C);
 					gm = player.getGameMode();
 					plugin.send(player, "Your gamemode is now "
 							+ ChatColor.DARK_AQUA + gm);
 					plugin.log(player.getName() + " changed his gamemode to "
 							+ gm.toString());
 					return true;
 				} else if (args.length == 2) {
 					if (!sender.hasPermission(Permissions.CSO)) {
 						plugin.send(sender,
 								"You don't have permission to run this command!");
 						return true;
 					}
 					Player target = Bukkit.getPlayer(args[1]);
 					if (target == null) {
 						plugin.send(sender, "The target " + ChatColor.RED
 								+ args[0].toString() + ChatColor.GRAY
 								+ " is not available!");
 						return true;
 					}
 					if (target.isOp()) {
 						plugin.send(sender,
 								"You're not allowed to change the gamemode of an op!");
 						return true;
 					}
 					gm = target.getGameMode();
 					if (gm == C) {
 						target.setGameMode(S);
 					} else if (gm == S) {
 						target.setGameMode(C);
 					}
 					gm = target.getGameMode();
 					plugin.send(target, "Your gamemode is now "
 							+ ChatColor.DARK_AQUA + gm);
 					plugin.log(sender.getName() + " changed the gamemode of "
 							+ target.getName().toString() + " to "
 							+ gm.toString());
 					return true;
 				}
 			} else if (args[0].equalsIgnoreCase("request") && !sender.isOp()
 					&& !sender.hasPermission(Permissions.CS)
 					&& !sender.hasPermission(Permissions.CSO)
 					&& args.length == 1) {
 				for (Player p : player.getServer().getOnlinePlayers()) {
 					if (p.isOp()) {
 						plugin.send(p, ChatColor.RED
 								+ player.getName().toString() + ChatColor.GRAY
 								+ " requests for gamemode change");
 					}
 				}
 				plugin.log(player.getName().toString() + " used /cs request");
 				return true;
 			} else if (args[0].equalsIgnoreCase("tp")) {
				if (args.length == 1) {
 					if (player == null) {
 						plugin.send(sender, "You can only run this in game!");
 						return true;
 					}
 					if (!player.hasPermission(Permissions.CT)) {
 						plugin.send(sender,
 								"You don't have permission to run this command!");
 						return true;
 					}
 					Player target = CosmicShiftPlayerListener.target;
 					if (target == null) {
 						plugin.send(player, "The target is not available!");
 						return true;
 					}
 					if (target == player) {
 						plugin.send(player, "You can't teleport to yourself!");
 						return true;
 					}
 					plugin.send(player, "Teleporting to " + ChatColor.RED
 							+ target.getName().toString());
 					player.teleport(target);
 					plugin.log(player.getName() + " used '/xtp me'");
 					return true;
				} else if (args.length == 2) {
 					if (!sender.hasPermission(Permissions.CTO)) {
 						plugin.send(sender,
 								"You don't have permission to run this command!");
 						return true;
 					}
 					Player target = CosmicShiftPlayerListener.target;
 					Player target1 = Bukkit.getPlayer(args[1]);
 					if (target == null) {
 						plugin.send(sender, "The target is not available!");
 						return true;
 					}
 					if (target1 == target) {
 						plugin.send(sender,
 								"You can't teleport a player to himself");
 						return true;
 					}
 					plugin.send(target, "Teleporting to " + ChatColor.RED
 							+ target.getName().toString());
 					target1.teleport(target);
 					if (sender instanceof Player)
 						plugin.log(sender.getName() + " used '/xtp <Player>'");
 					return true;
 				}
 			}
 		} else {
 			if (sender instanceof Player) {
 				pComs(player);
 				plugin.log(player.getName() + " used '/cs'");
 				return true;
 			}
 			cComs(sender);
 			return true;
 		}
 		return false;
 	}
 
 	private final void cComs(CommandSender sender) {
 		plugin.send(sender, "-| Commands |-");
 		plugin.send(sender, "/cs shift <Player> - Changes the gamemode of");
 		plugin.send(sender, "the given player");
 		plugin.send(sender, "/cs tp <Player> - Teleports the given player to");
 		plugin.send(sender, "the last person that changed his gamemode");
 	}
 
 	private final void pComs(Player player) {
 		plugin.send(player, "-|" + ChatColor.RED + " Commands "
 				+ ChatColor.WHITE + "|-");
 		if (player.hasPermission(Permissions.CS))
 			plugin.send(player, ChatColor.RED + "/cs shift" + ChatColor.GRAY
 					+ " - Changes your current gamemode");
 		if (player.hasPermission(Permissions.CSO))
 			plugin.send(player, ChatColor.RED + "/cs shift <Player>"
 					+ ChatColor.GRAY + " Changes the gamemode of");
 		if (player.hasPermission(Permissions.CSO))
 			plugin.send(player, "the given player");
 		if (player.hasPermission(Permissions.CT))
 			plugin.send(player, ChatColor.RED + "/cs tp" + ChatColor.GRAY
 					+ " - Teleports you to the last person");
 		if (player.hasPermission(Permissions.CT))
 			plugin.send(player, "that changed his gamemode");
 		if (player.hasPermission(Permissions.CTO))
 			plugin.send(player, ChatColor.RED + "/cs tp <Player>"
 					+ ChatColor.GRAY + " - Teleports the given player");
 		if (player.hasPermission(Permissions.CTO))
 			plugin.send(player, "to the last person that changed his gamemode");
 		if (!player.hasPermission(Permissions.CS)
 				&& !player.hasPermission(Permissions.CSO))
 			plugin.send(player, ChatColor.RED + "/cs request" + ChatColor.GRAY
 					+ " - Requests a gamemode change");
 	}
 }

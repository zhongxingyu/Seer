 package gibstick.bukkit.discosheep;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.entity.Player;
 
 public class DiscoSheepCommandExecutor implements CommandExecutor {
 
 	private DiscoSheep parent;
 
 	public DiscoSheepCommandExecutor(DiscoSheep parent) {
 		this.parent = parent;
 	}
 	private static final String PERMISSION_PARTY = "discosheep.party";
 	private static final String PERMISSION_ALL = "discosheep.partyall";
 	private static final String PERMISSION_FIREWORKS = "discosheep.fireworks";
 	private static final String PERMISSION_STOP = "discosheep.stop";
 	private static final String PERMISSION_SUPER = "disosheep.*";
 
 	private boolean senderHasPerm(CommandSender sender, String permission) {
 		return sender.hasPermission(permission) || sender.hasPermission(PERMISSION_SUPER);
 	}
 
 	private void noPermsMessage(CommandSender sender, String permission) {
 		sender.sendMessage(ChatColor.RED + "You do not have the permission node " + ChatColor.GRAY + permission);
 	}
 
 	private boolean parseNextArg(String[] args, int i, String compare) {
 		if (i < args.length - 1) {
 			return args[i + 1].equalsIgnoreCase(compare);
 		}
 		return false;
 	}
 
 	private int parseNextIntArg(String[] args, int i) {
 		if (i < args.length - 1) {
 			return Integer.parseInt(args[i + 1]);
 		}
 		return -1;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		Player player = null;
 		boolean isPlayer = false;
 		boolean fireworks = false;
 		int sheepNumber = DiscoParty.defaultSheepAmount;
 		int radius = DiscoParty.defaultSheepSpawnRadius;
 		int duration = DiscoParty.defaultDuration;
 
 		if (sender instanceof Player) {
 			player = (Player) sender;
 			isPlayer = true;
 		}
 
 		if (!senderHasPerm(sender, PERMISSION_PARTY)) {
 			noPermsMessage(sender, PERMISSION_PARTY);
 			return true;
 		}
 
 		for (int i = 1; i < args.length; i++) {
 			if (args[i].equalsIgnoreCase("-fw")) {
 				if (senderHasPerm(sender, PERMISSION_FIREWORKS)) {
 					fireworks = !fireworks;
 				} else {
 					noPermsMessage(sender, PERMISSION_FIREWORKS);
 				}
 			} else if (args[i].equalsIgnoreCase("-r")) {
 				radius = parseNextIntArg(args, i);
 
 				if (radius < 1 || radius > 100) {
 					sender.sendMessage("Radius must be an integer within the range [1, 100]");
 					return true;
 				}
 			} else if (args[i].equalsIgnoreCase("-n")) {
 				sheepNumber = parseNextIntArg(args, i);
 
 				if (sheepNumber < 1 || sheepNumber > 100) {
 					sender.sendMessage("The number of sheep must be an integer within the range [1, 100]");
 				}
 			}
 		}
 
 		if (args.length > 0) {
 
 			if (args[0].equalsIgnoreCase("all")) {
 				if (senderHasPerm(sender, PERMISSION_ALL)) {
 					for (Player p : Bukkit.getServer().getOnlinePlayers()) {
						parent.startParty(p, duration, sheepNumber, radius, fireworks);
 						p.sendMessage(ChatColor.RED + "LET'S DISCO!");
 					}
 				} else {
 					noPermsMessage(sender, PERMISSION_ALL);
 				}
 				return true;
 			} else if (args[0].equalsIgnoreCase("stop")) {
 				if (senderHasPerm(sender, PERMISSION_STOP)) {
 					parent.stopAllParties();
 				} else {
 					noPermsMessage(sender, PERMISSION_STOP);
 				}
 				return true;
 			} else if (args[0].equalsIgnoreCase("me")) {
 				if (isPlayer) {
 					parent.startParty(player, duration, sheepNumber, radius, fireworks);
 					return true;
 				}
 			} else {
 				sender.sendMessage(ChatColor.RED + "Invalid argument.");
 				return true;
 			}
 
 		}
 
 		return false;
 	}
 }

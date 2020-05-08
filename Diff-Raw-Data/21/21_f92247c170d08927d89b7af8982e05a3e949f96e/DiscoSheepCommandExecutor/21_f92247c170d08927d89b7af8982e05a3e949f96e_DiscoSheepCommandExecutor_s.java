 package gibstick.bukkit.discosheep;
 
 import java.util.Arrays;
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
 	private static final String PERMISSION_STOPALL = "discosheep.stopall";
 	private static final String PERMISSION_RELOAD = "discosheep.reload";
 	private static final String PERMISSION_OTHER = "discosheep.partyother";
 
 	//private static final String DELIM = "[ ]+";
 	private boolean senderHasPerm(CommandSender sender, String permission) {
 		return sender.hasPermission(permission);
 	}
 
 	private boolean noPermsMessage(CommandSender sender, String permission) {
 		sender.sendMessage(ChatColor.RED + "You do not have the permission node " + ChatColor.GRAY + permission);
 		return false;
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
 
 	private Double parseNextDoubleArg(String[] args, int i) {
 		if (i < args.length - 1) {
 			return Double.parseDouble(args[i + 1]);
 		}
 		return -1.0d;
 	}
 
 	// extract a list of players from a list of arguments
 	private String[] parsePlayerList(String[] args, int i) {
 		int j = i;
 		while (j < args.length && !args[i].startsWith("-")) {
 			++j;
 		}
 		return Arrays.copyOfRange(args, i, j);
 	}
 
 	/*-- Actual commands begin here --*/
 	private boolean helpCommand(CommandSender sender) {
 		sender.sendMessage(ChatColor.YELLOW + "DiscoSheep Help\n"
 				+ ChatColor.GRAY + "  Subcommands\n" + ChatColor.WHITE
 				+ "me: start a party for yourself\n"
 				+ "stop: stop your own party\n"
 				+ "all: start a party for all players on the server\n"
 				+ "stopall: stop all parties (takes no arguments)\n"
 				+ "other <players>: start a party for the space-delimited list of players\n"
 				+ ChatColor.GRAY + "  Arguments\n" + ChatColor.WHITE
 				+ "-n <integer>: set the number of sheep per player that spawn\n"
 				+ "-t <integer>: set the party duration in seconds\n"
 				+ "-p <ticks>: set the number of ticks between each disco beat\n"
 				+ "-r <integer>: set radius of the area in which sheep can spawn\n"
 				+ "-fw: enables fireworks");
 		return true;
 	}
 
 	private boolean reloadCommand(CommandSender sender) {
 		if (senderHasPerm(sender, PERMISSION_RELOAD)) {
 			parent.reloadConfigFromDisk();
 			sender.sendMessage(ChatColor.GREEN + "DiscoSheep config reloaded from disk");
 			return true;
 		} else {
 			return noPermsMessage(sender, PERMISSION_RELOAD);
 		}
 	}
 
 	private boolean partyCommand(CommandSender sender, int _duration, int _sheepNumber, int _radius, int _period, boolean _fireworks) {
 		if (senderHasPerm(sender, PERMISSION_PARTY)) {
 			parent.startParty((Player) sender, _duration, _sheepNumber, _radius, _period, _fireworks);
 			return true;
 		} else {
 			return noPermsMessage(sender, PERMISSION_PARTY);
 		}
 	}
 
 	private boolean partyAllCommand(CommandSender sender, int _duration, int _sheepNumber, int _radius, int _period, boolean _fireworks) {
 		if (senderHasPerm(sender, PERMISSION_ALL)) {
 			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
 				parent.startParty(p, _duration, _sheepNumber, _radius, _period, _fireworks);
 				p.sendMessage(ChatColor.RED + "LET'S DISCO!");
 			}
 			return true;
 		} else {
 			return noPermsMessage(sender, PERMISSION_ALL);
 		}
 	}
 
 	private boolean stopAllCommand(CommandSender sender) {
 		if (senderHasPerm(sender, PERMISSION_STOPALL)) {
 			parent.stopAllParties();
 			return true;
 		} else {
 			return noPermsMessage(sender, PERMISSION_STOPALL);
 		}
 	}
 
 	private boolean stopMeCommand(CommandSender sender) {
 		parent.stopParty(sender.getName());
 		return true;
 	}
 
 	private boolean partySelectCommand(String[] players, CommandSender sender, int _duration, int _sheepNumber, int _radius, int _period, boolean _fireworks) {
 		if (senderHasPerm(sender, PERMISSION_OTHER)) {
 			Player p;
 			for (String playerName : players) {
 				p = Bukkit.getServer().getPlayer(playerName);
 				if (p != null) {
 					parent.startParty(p, _duration, _sheepNumber, _radius, _period, _fireworks);
 				} else {
 					sender.sendMessage("Invalid player: " + playerName);
 				}
 			}
 			return true;
 		} else {
 			return noPermsMessage(sender, PERMISSION_OTHER);
 		}
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 
 		Player player = null;
 		boolean isPlayer = false;
 		boolean fireworks = false;
 		int sheepNumber = DiscoParty.defaultSheep;
 		int radius = DiscoParty.defaultRadius;
 		int duration = DiscoParty.defaultDuration;
 		int period = DiscoParty.defaultPeriod;
 
 		if (sender instanceof Player) {
 			player = (Player) sender;
 			isPlayer = true;
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
 
 				if (radius < 1 || radius > DiscoParty.maxRadius) {
 					sender.sendMessage("Radius must be an integer within the range [1, "
 							+ DiscoParty.maxRadius + "]");
 					return true;
 				}
 			} else if (args[i].equalsIgnoreCase("-n")) {
 				sheepNumber = parseNextIntArg(args, i);
 
 				if (sheepNumber < 1 || sheepNumber > DiscoParty.maxSheep) {
 					sender.sendMessage("The number of sheep must be an integer within the range [1, "
 							+ DiscoParty.maxSheep + "]");
 					return true;
 				}
 			} else if (args[i].equalsIgnoreCase("-t")) {
 				duration = parent.toTicks(parseNextIntArg(args, i));
 
 				if (duration < 1 || duration > DiscoParty.maxDuration) {
 					sender.sendMessage("The duration in seconds must be an integer within the range [1, "
 							+ parent.toSeconds(DiscoParty.maxDuration) + "]");
 					return true;
 				}
 			} else if (args[i].equalsIgnoreCase("-p")) {
 				period = parseNextIntArg(args, i);
 
 				if (period < DiscoParty.minPeriod || period > DiscoParty.maxPeriod) {
 					sender.sendMessage(
 							"The period in ticks must be within the range ["
 							+ DiscoParty.minPeriod + ", "
 							+ DiscoParty.maxPeriod + "]");
 					return true;
 				}
 			}
 		}
 
 		if (args.length > 0) {
 
 			if (args[0].equalsIgnoreCase("all")) {
 				return partyAllCommand(sender, duration, sheepNumber, radius, period, fireworks);
 			} else if (args[0].equalsIgnoreCase("stopall")) {
 				return stopAllCommand(sender);
 			} else if (args[0].equalsIgnoreCase("stop") && isPlayer) {
 				return stopMeCommand(sender);
 			} else if (args[0].equalsIgnoreCase("me") && isPlayer) {
 				return partyCommand(player, duration, sheepNumber, radius, period, fireworks);
 			} else if (args[0].equalsIgnoreCase("other")) {
 				return partySelectCommand(parsePlayerList(args, 1), sender, duration, sheepNumber, radius, period, fireworks);
 			} else if (args[0].equalsIgnoreCase("help")) {
 				return helpCommand(sender);
 			} else if (args[0].equalsIgnoreCase("reload")) {
 				return reloadCommand(sender);
 			} else {
 				sender.sendMessage(ChatColor.RED + "Invalid argument (certain commands do not work from console).");
 				return false;
 			}
 
 		}
 
 		return false;
 	}
 }

 package de.MiniDigger.ScrollingScoreBoardAnnouncer;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import de.MiniDigger.ScrollingScoreBoardAnnouncer.Updater.UpdateType;
 
 public class ScrollingScoreBoardAnnoucerCommands implements CommandExecutor {
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String lable,
 			String[] args) {
 		if (cmd.getName().equalsIgnoreCase("announce")) {
 			if (sender instanceof Player
 					&& !ScrollingScoreBoardAnnouncer.perms.has(sender,
 							"ssa.announce")) {
 				sender.sendMessage(ChatColor.RED
 						+ ScrollingScoreBoardAnnouncer.prefix
 						+ " You dont have permmsions to use that command!");
 				return true;
 			}
 			if (args.length < 3) {
 				sender.sendMessage(ScrollingScoreBoardAnnouncer.prefix
 						+ ChatColor.YELLOW
 						+ "This command allows you to change the displayed text of a scoreboard");
 				sender.sendMessage(ScrollingScoreBoardAnnouncer.prefix
 						+ ChatColor.YELLOW
 						+ " Usage:  /announce <board> <slot> <the> <text> <you> <want> <to> <display>");
 				return true;
 			} else {
 				ScrollingScoreBoard board = ScrollingScoreBoardAnnouncer.handler
 						.get(args[0]);
 				if (board == null) {
 					sender.sendMessage(ScrollingScoreBoardAnnouncer.prefix
 							+ ChatColor.YELLOW
 							+ " This command allows you to change the displayed text of a scoreboard");
 					sender.sendMessage(ScrollingScoreBoardAnnouncer.prefix
 							+ ChatColor.YELLOW
 							+ " Usage:  /announce <board> <slot> <the> <text> <you> <want> <to> <display>");
 					return true;
 				}
 				int slot = 0;
 				try {
 					slot = Integer.parseInt(args[1]);
 				} catch (Exception e) {
 					sender.sendMessage(ScrollingScoreBoardAnnouncer.prefix
 							+ ChatColor.RED + " The slot has to be a number!");
 					return true;
 				}
 				String msg = "";
 				for (int i = 2; i < args.length; i++) {
 					msg += " " + args[i];
 				}
 				board.annonce(msg, slot);
 				sender.sendMessage(ScrollingScoreBoardAnnouncer.prefix
 						+ ChatColor.GREEN + " Text of board " + args[0]
 						+ " in slot " + args[1] + " was changed to " + msg);
 			}
 			return true;
 		} else if (cmd.getName().equalsIgnoreCase(
 				ScrollingScoreBoardAnnouncer.name)) {
 			if (args.length != 0 && args[0].equalsIgnoreCase("create")) {
 				if (sender instanceof Player
 						&& !ScrollingScoreBoardAnnouncer.perms.has(sender,
 								"ssa.create")) {
 					sender.sendMessage(ChatColor.RED
 							+ ScrollingScoreBoardAnnouncer.prefix
 							+ " You dont have permmsions to use that command!");
 					return true;
 				}
				if (args.length < 1) {
 					sender.sendMessage(ChatColor.YELLOW
 							+ ScrollingScoreBoardAnnouncer.prefix
 							+ " Creates a new command with the name <arg1> ");
 					return true;
 				} else {
 					ScrollingScoreBoardAnnouncer.handler.create(args[1]);
 					sender.sendMessage(ChatColor.GOLD
 							+ ScrollingScoreBoardAnnouncer.prefix
 							+ ChatColor.GREEN
 							+ "New Board created. You can edit it now");
 					return true;
 				}
 			} else if (args.length != 0 && args[0].equalsIgnoreCase("reload")) {
 				if (sender instanceof Player
 						&& !ScrollingScoreBoardAnnouncer.perms.has(sender,
 								"ssa.reload")) {
 					sender.sendMessage(ChatColor.RED
 							+ ScrollingScoreBoardAnnouncer.prefix
 							+ " You dont have permmsions to use that command!");
 					return true;
 				}
 				ScrollingScoreBoardAnnouncer.handler.loadAll();
 				ScrollingScoreBoardAnnouncer.config.load();
 				ScrollingScoreBoardAnnouncer.debug = ScrollingScoreBoardAnnouncer.config
 						.getBoolean("debug");
 				ScrollingScoreBoardAnnouncer.update = ScrollingScoreBoardAnnouncer.config
 						.getBoolean("update");
 
 			} else if (args.length != 0 && args[0].equalsIgnoreCase("update")) {
 
 				if (sender instanceof Player
 						&& !ScrollingScoreBoardAnnouncer.perms.has(sender,
 								"ssa.update")) {
 					sender.sendMessage(ChatColor.RED
 							+ ScrollingScoreBoardAnnouncer.prefix
 							+ " You dont have permmsions to use that command!");
 					return true;
 				}
 				if (ScrollingScoreBoardAnnouncer.isUpdateAvailable) {
 					Updater u = new Updater(
 							ScrollingScoreBoardAnnouncer.getInstance(), 1,
 							ScrollingScoreBoardAnnouncer.file,
 							UpdateType.NO_VERSION_CHECK, true);
 					// TODO Wait for curse to sync the project to get the id :D
 					switch (u.getResult()) {
 					case DISABLED:
 						sender.sendMessage(ChatColor.GOLD
 								+ ScrollingScoreBoardAnnouncer.prefix
 								+ ChatColor.YELLOW
 								+ " You have disabled the updater in its config. I cant check for Updates :(");
 						break;
 					case FAIL_APIKEY:
 						sender.sendMessage(ChatColor.GOLD
 								+ ScrollingScoreBoardAnnouncer.prefix
 								+ ChatColor.RED
 								+ " Could not check for updates: Invalid API Key");
 						break;
 					case FAIL_BADID:
 						sender.sendMessage(ChatColor.GOLD
 								+ ScrollingScoreBoardAnnouncer.prefix
 								+ ChatColor.RED
 								+ " Could not check for updates: Bad ID");
 						break;
 					case FAIL_DBO:
 						sender.sendMessage(ChatColor.GOLD
 								+ ScrollingScoreBoardAnnouncer.prefix
 								+ ChatColor.RED
 								+ " Could not check for updates: Could not reach DBO");
 						break;
 					case UPDATE_AVAILABLE:
 						ScrollingScoreBoardAnnouncer.isUpdateAvailable = true;
 						sender.sendMessage(ChatColor.GOLD
 								+ ScrollingScoreBoardAnnouncer.prefix
 								+ " There is an Update available. Use '/ssa update' to update the plugin");
 						break;
 					case NO_UPDATE:
 						sender.sendMessage(ChatColor.GOLD
 								+ ScrollingScoreBoardAnnouncer.prefix
 								+ ChatColor.YELLOW
 								+ " The plugin is up-to-date");
 						break;
 					case SUCCESS:
 						sender.sendMessage(ChatColor.GOLD
 								+ ScrollingScoreBoardAnnouncer.prefix
 								+ ChatColor.GREEN
 								+ " Plugin updated! Please reload your server!");
 					default:
 						break;
 					}
 				} else {
 					Updater u = new Updater(
 							ScrollingScoreBoardAnnouncer.getInstance(), 1,
 							ScrollingScoreBoardAnnouncer.file,
 							UpdateType.NO_DOWNLOAD, true); // TODO Wait for
 															// curse to
 															// sync the project
 															// to get
 															// the id :D
 					switch (u.getResult()) {
 					case DISABLED:
 						sender.sendMessage(ChatColor.GOLD
 								+ ScrollingScoreBoardAnnouncer.prefix
 								+ ChatColor.YELLOW
 								+ " You have disabled the updater in its config. I cant check for Updates :(");
 						break;
 					case FAIL_APIKEY:
 						sender.sendMessage(ChatColor.GOLD
 								+ ScrollingScoreBoardAnnouncer.prefix
 								+ ChatColor.RED
 								+ " Could not check for updates: Invalid API Key");
 						break;
 					case FAIL_BADID:
 						sender.sendMessage(ChatColor.GOLD
 								+ ScrollingScoreBoardAnnouncer.prefix
 								+ ChatColor.RED
 								+ " Could not check for updates: Bad ID");
 						break;
 					case FAIL_DBO:
 						sender.sendMessage(ChatColor.GOLD
 								+ ScrollingScoreBoardAnnouncer.prefix
 								+ ChatColor.RED
 								+ " Could not check for updates: Could not reach DBO");
 						break;
 					case UPDATE_AVAILABLE:
 						ScrollingScoreBoardAnnouncer.isUpdateAvailable = true;
 						sender.sendMessage(ChatColor.GOLD
 								+ ScrollingScoreBoardAnnouncer.prefix
 								+ " There is an Update available. Use '/ssa update' to update the plugin");
 						break;
 					case NO_UPDATE:
 						sender.sendMessage(ChatColor.GOLD
 								+ ScrollingScoreBoardAnnouncer.prefix
 								+ ChatColor.YELLOW
 								+ " The plugin is up-to-date");
 						break;
 					default:
 						break;
 
 					}
 				}
 			} else {
 				sender.sendMessage(ChatColor.GOLD
 						+ ScrollingScoreBoardAnnouncer.prefix
 						+ ChatColor.YELLOW
 						+ " This server is using "
 						+ ScrollingScoreBoardAnnouncer.name
 						+ " "
 						+ ScrollingScoreBoardAnnouncer.getInstance()
 								.getDescription().getVersion());
 				sender.sendMessage(ChatColor.GOLD
 						+ ScrollingScoreBoardAnnouncer.prefix
 						+ ChatColor.YELLOW
 						+ " If you want to see more of my plugins check out http://dev.bukkit.org/profiles/MiniDigger/");
 				return true;
 			}
 		}
 		return true;
 	}
 }

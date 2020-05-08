 package me.INemesisI.XcraftTickets.Manager;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import me.INemesisI.XcraftTickets.Ticket;
 import me.INemesisI.XcraftTickets.Commands.Command;
 import me.INemesisI.XcraftTickets.Commands.CommandInfo;
 import me.INemesisI.XcraftTickets.Commands.Admin.ReloadCommand;
 import me.INemesisI.XcraftTickets.Commands.Admin.SaveCommand;
 import me.INemesisI.XcraftTickets.Commands.Mod.AssignCommand;
 import me.INemesisI.XcraftTickets.Commands.Mod.UnAssignCommand;
 import me.INemesisI.XcraftTickets.Commands.Mod.UndoCommand;
 import me.INemesisI.XcraftTickets.Commands.Mod.WarpCommand;
 import me.INemesisI.XcraftTickets.Commands.User.CloseCommand;
 import me.INemesisI.XcraftTickets.Commands.User.ListCommand;
 import me.INemesisI.XcraftTickets.Commands.User.LogCommand;
 import me.INemesisI.XcraftTickets.Commands.User.OpenCommand;
 import me.INemesisI.XcraftTickets.Commands.User.PhrasesCommand;
 import me.INemesisI.XcraftTickets.Commands.User.ReOpenCommand;
 import me.INemesisI.XcraftTickets.Commands.User.SetWarpCommand;
 import me.INemesisI.XcraftTickets.Commands.User.ViewCommand;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.TabCompleter;
 import org.bukkit.entity.Player;
 
 public class CommandManager implements CommandExecutor, TabCompleter {
 
 	private final TicketManager manager;
 	private Map<String, Command> commands;
 
 	public CommandManager(TicketManager manager) {
 		this.manager = manager;
 		this.registerCommands();
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, org.bukkit.command.Command bcmd, String label, String[] args) {
 		// Grab the base and arguments.
 		String cmd = (args.length > 0 ? args[0] : "");
 		String last = (args.length > 0 ? args[args.length - 1] : "");
 		// /tl as shortcut for /ticket list
 		if (bcmd.getName().equals("tl")) {
 			commands.get(getCommandInfo(ListCommand.class).pattern()).execute(manager, sender, args);
 			return true;
 		}
 		// The help command is a little special
 		if (cmd.equals("") || cmd.equals("?") || cmd.equals("help")) {
 			this.showHelp(sender, bcmd.getName());
 			return true;
 		}
 		// Get all commands that match the base.
 		List<Command> matches = this.getMatchingCommands(bcmd.getName(), cmd);
 		// If there's more than one match, display them.
 		if (matches.size() > 1) {
 			sender.sendMessage("Meinst du villeicht einen dieser Befehle?");
 			for (Command c : matches) {
 				this.showUsage(sender, c);
 			}
 			return true;
 		}
 		// If there are no matches at all, notify.
 		if (matches.size() == 0) {
 
 			sender.sendMessage(ChatColor.RED + "Unbekannter Befehl: \"" + cmd + "\"!");
 			this.showHelp(sender, bcmd.getName());
 			return true;
 		}
 		// Grab the only match.
 		Command command = matches.get(0);
 		CommandInfo info = this.getCommandInfo(command.getClass());
 		// First check if the sender has permission.
 		if (!sender.hasPermission(info.permission())) {
 			sender.sendMessage(ChatColor.RED + " Du hast keinen Zugriff auf diesen Befehl!");
 			return true;
 		}
 		// Check if the last argument is a ?, in which case, display usage and
 		// description
 		if (last.equals("?") || last.equals("help")) {
 			this.showUsage(sender, command);
 			return true;
 		}
 		// Otherwise, execute the command!
 		String[] params = Arrays.copyOfRange(args, 1, args.length);
 		if (!command.execute(manager, sender, params))
 			showUsage(sender, command);
 		return true;
 	}
 	private List<Command> getMatchingCommands(String cmd, String arg) {
 		List<Command> result = new ArrayList<Command>();
 		// Grab the commands that match the argument.
 		for (Entry<String, Command> entry : commands.entrySet()) {
 			if (arg.matches(entry.getKey()) && cmd.matches(this.getCommandInfo(entry.getValue().getClass()).command())) {
 				result.add(entry.getValue());
 			}
 		}
 		return result;
 	}
 
 	private void showHelp(CommandSender sender, String cmd) {
 		sender.sendMessage(ChatColor.GOLD + "Verfgbare Befehle fr "
 				+ manager.getPlugin().getDescription().getFullName() + " By INemesisI :");
 		for (Command command : commands.values()) {
 			if (cmd.matches(this.getCommandInfo(command.getClass()).command())) {
 				this.showUsage(sender, command);
 			}
 		}
 	}
 
 	private void showUsage(CommandSender sender, Command cmd) {
 		CommandInfo info = this.getCommandInfo(cmd.getClass());
 		if (!sender.hasPermission(info.permission())) {
 			return;
 		}
 		sender.sendMessage(ChatColor.DARK_GRAY + "->" + ChatColor.GREEN + "/" + info.command() + " " + info.name()
 				+ " " + info.usage() + " " + ChatColor.DARK_AQUA + "- " + info.desc());
 	}
 
 	private void registerCommands() {
 		commands = new TreeMap<String, Command>();
 		// user Commands
 		this.register(OpenCommand.class);
 		this.register(LogCommand.class);
 		this.register(ListCommand.class);
 		this.register(ViewCommand.class);
 		this.register(CloseCommand.class);
 		this.register(ReOpenCommand.class);
 		this.register(SetWarpCommand.class);
 		// Admin Commands
 		this.register(ReloadCommand.class);
 		this.register(SaveCommand.class);
 		// Mod Commands
 		this.register(WarpCommand.class);
 		this.register(AssignCommand.class);
 		this.register(UnAssignCommand.class);
 		this.register(UndoCommand.class);
 		this.register(PhrasesCommand.class);
 	}
 
 	private void register(Class<? extends Command> cmd) {
 		CommandInfo info = this.getCommandInfo(cmd);
 		if (info == null) {
 			return;
 		}
 		try {
 			commands.put(info.pattern(), cmd.newInstance());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public CommandInfo getCommandInfo(Class<? extends Command> cmd) {
 		return cmd.getAnnotation(CommandInfo.class);
 	}
 
 	@Override
 	public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command bcmd, String alias, String[] args) {
 		List<String> list = new ArrayList<String>();
 		for (Command cmd : commands.values()) {
 			CommandInfo info = this.getCommandInfo(cmd.getClass());
 			if (bcmd.getName().matches(info.command()) && sender.hasPermission(info.permission())) {
 				if (args.length > 1 && args[0].matches(info.pattern())) {
 					String[] usages = info.usage().split(" ");
 					if (usages.length > args.length - 2) {
 						String usage = usages[args.length - 2];
 						String token = args[args.length - 1].toLowerCase();

 						if (usage.equals("<#>")) {
 							for (Ticket ticket : manager.getTickets()) {
 								if (ticket.getOwner().equals(sender.getName())
 										|| (sender.hasPermission("XcraftTickets.View.All"))) {
 									if (token.equals("") || token.startsWith(String.valueOf(ticket.getId()))) {
 										if (ticket.getAssignee() != null
												&& (ticket.getAssignee().equals(sender.getName()) || manager
														.getPlugin().getPermission()
														.playerInGroup((Player) sender, ticket.getAssignee()))) {
 											list.add(0, String.valueOf(ticket.getId()));
											System.out.println("assigned " + ticket.getId());
 										} else {
 											list.add(String.valueOf(ticket.getId()));
											System.out.println("not assigned " + ticket.getId());
 										}
 									}
 								}
 							}
 						} else if (usage.equals("<Nachricht>")) {
 							Map<String, String> phrases = manager.getPhrases();
 							for (String phrase : phrases.keySet()) {
 								if (phrase.equals("") || phrase.toLowerCase().startsWith(token)) {
 									list.add(phrase);
 								}
 							}
 						} else if (usage.equals("<Name|Gruppe>")) {
 							List<String> assignees = manager.getAssignees();
 							for (String assignee : assignees) {
 								if (token.equals("") || assignee.toLowerCase().startsWith(token))
 									list.add(assignee);
 							}
 						} else if (!usage.contains("<") && !usage.contains(">")) {
 							for (String key : usage.split("\\|")) {
 								list.add(key);
 							}
 						} else if (usage.equals("")) {
 							for (Player player : manager.getPlugin().getServer().getOnlinePlayers()) {
 								if (token.equals("") || player.getName().toLowerCase().startsWith(token))
 									list.add(player.getName());
 							}
 						}
 					}
 					break;
 				} else if (args.length <= 1 && (args[0].equals("") || info.name().startsWith(args[0]))) {
 					list.add(info.name());
 					System.out.println(info.name());
 				}
 			}
 		}
 		return list;
 	}
 }

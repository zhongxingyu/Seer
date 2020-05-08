 package net.milkycraft;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class Commands implements CommandExecutor {
 
 	private final String pre = ChatColor.AQUA + "[ColoredGroups] ";
 	ColoredGroups mc;
 
 	public Commands(ColoredGroups mc) {
 		this.mc = mc;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 		if (args.length == 0) {
 			sender.sendMessage(ChatColor.GREEN + "Using "
 					+ mc.getDescription().getName() + " v"
 					+ mc.getDescription().getVersion() + " by "
 					+ mc.getDescription().getAuthors().get(0));
 			sender.sendMessage(ChatColor.GREEN + "Use /cg help");
 			return true;
 		}
		if (args[0].equalsIgnoreCase("reload")) {
			if (sender.hasPermission("coloredgroups.reload")) {
				mc.reload();
				sender.sendMessage(pre + ChatColor.GREEN + "Reloaded variables");
			} else {
				sender.sendMessage(pre + ChatColor.RED
						+ "You dont have permission to reload");
			}
 		} else if (args[0].equalsIgnoreCase("help")) {
 			sender.sendMessage(pre + ChatColor.GREEN
 					+ "Currently only /cg reload exists");
 		}
 		return false;
 	}
 
 }

 package me.confuser.lilyalert;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class AlertCommand implements CommandExecutor {
 
 	private LilyAlert plugin;
 
 	public AlertCommand(LilyAlert instance) {
 		plugin = instance;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[]) {
 		if (!sender.hasPermission("lilypad.alert")) {
 			sender.sendMessage(ChatColor.WHITE + "Unknown command");
 			return true;
 		}
 
 		if (args.length == 0)
 			return false;
 
 		if (args[0].equalsIgnoreCase("reload")) {
 			plugin.configReload();
 			sender.sendMessage(ChatColor.GREEN + "[LilyAlert] Reloaded!");
 			return true;
 		}
 
		if (plugin.servers.size() == 0) {
			sender.sendMessage(ChatColor.RED + "No server usernames defined for /alert in the config!");
			return true;
		}

 		// Combine the message!
 		String message = "";
 
 		for (String m : args) {
 			message += " " + m;
 		}
 
 		// Send the request to every server!
 		for (String server : plugin.servers) {
 			plugin.request(server, message.trim());
 		}
 
 		return true;
 	}
 }

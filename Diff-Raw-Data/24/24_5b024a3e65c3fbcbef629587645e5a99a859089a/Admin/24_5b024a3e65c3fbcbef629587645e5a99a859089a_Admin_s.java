 package com.jsne10.nodrops.command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import com.jsne10.nodrops.Main;
 
 public class Admin implements CommandExecutor {
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		
 		// Help/no command
 		if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
			String msg = "&c[jNoDrops] &7Usage: /jnodrops <reload/about>";
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
 			return true;
 		}
 		
 		// Reload config command.
 		if (args[0].equalsIgnoreCase("reload")) {
			Main.loadConfig(Main.plugin);
			String msg = "&c[jNoDrops] &7Config Reloaded.";
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
 			return true;
 		}
 		
 		// Gives a bit of background info about the plugin.
		if (args[1].equalsIgnoreCase("about")) {
			String msg;
			
			msg = "&c[jNoDrops] &7Version " + Main.plugin.getDescription().getVersion();
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
			
			msg = "&c[jNoDrops] &7A plugin by jsne10. Allows to block players from droppping various items.";
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
 			return true;
 		}
 		
 		// If none of the commands are entered.
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c[jNoDrops] &7Usage: /jnodrops <reload/about>"));
 		return true;
 		
 	}
 
 }

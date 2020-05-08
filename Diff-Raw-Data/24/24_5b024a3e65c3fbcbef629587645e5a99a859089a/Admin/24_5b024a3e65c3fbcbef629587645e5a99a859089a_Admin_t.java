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
			sender.sendMessage(ChatColor.RED + "[jNoDrops] " + ChatColor.GRAY +"Usage: /jnodrops <reload/about>");
 			return true;
 		}
 		
 		// Reload config command.
 		if (args[0].equalsIgnoreCase("reload")) {
			Main.loadConfig();
			sender.sendMessage(ChatColor.RED + "[jNoDrops] " + ChatColor.GRAY +"Config Reloaded.");
 			return true;
 		}
 		
 		// Gives a bit of background info about the plugin.
		if (args[0].equalsIgnoreCase("about")) {
			sender.sendMessage(ChatColor.RED + "[jNoDrops] " + ChatColor.GRAY +"Verion: " + Main.plugin.getDescription().getVersion());
			sender.sendMessage(ChatColor.RED + "[jNoDrops] " + ChatColor.GRAY +"A plugin by jsne10. Allows to block players from droppping various items.");
 			return true;
 		}
 		
 		// If none of the commands are entered.
		sender.sendMessage(ChatColor.RED + "[jNoDrops] " + ChatColor.GRAY +"Usage: /jnodrops <reload/about>");
 		return true;
 		
 	}
 
 }

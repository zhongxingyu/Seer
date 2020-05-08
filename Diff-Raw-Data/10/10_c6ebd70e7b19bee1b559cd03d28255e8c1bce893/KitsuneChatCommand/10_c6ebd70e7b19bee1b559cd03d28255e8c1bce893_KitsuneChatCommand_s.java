 package com.cyberkitsune.prefixchat;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class KitsuneChatCommand implements CommandExecutor {
 
 	private KitsuneChat plugin;
 
 	public KitsuneChatCommand(KitsuneChat plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		if (sender instanceof Player) {
			if (command.getName().equalsIgnoreCase("ch")) {
 				if (args.length > 0) {
					plugin.chans.changeChannel((Player) sender, args[0]);
 				} else {
					sender.sendMessage(ChatColor.RED+"Usage: /ch [channel]");
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 }

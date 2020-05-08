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
 			if (command.getName().equalsIgnoreCase("kc")) {
 				if (args.length > 0) {
 					if(args[0].equalsIgnoreCase("party")) {
 						if(args.length > 1) {
							if(args[1].equalsIgnoreCase("leave"))
							{
								plugin.party.leaveParty((Player) sender, false);
								return true;
							}
 							plugin.party.changeParty((Player) sender, args[1]);
 						} else {
 							sender.sendMessage(ChatColor.RED+"[KitsuneChat] Please choose a party name!");
 						}
 					} 
 				} else {
 					sender.sendMessage(ChatColor.RED+"[KitsuneChat] Unknown or missing command. See /kc ? for help.");
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 }

 package com.cyberkitsune.prefixchat;
 
 import java.util.Arrays;
 import java.util.List;
 
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
					} else {
						List<String> prefixes = Arrays.asList(plugin.getConfig().getString("global.prefix"), plugin.getConfig().getString("local.prefix"), plugin.getConfig().getString("admin.prefix"), plugin.getConfig().getString("party.prefix"), plugin.getConfig().getString("world.prefix"));
						for(String str : prefixes) {
								if(args[0].equalsIgnoreCase(str)) {
								plugin.dataFile.setUserChannel((Player) sender, str);
								sender.sendMessage(ChatColor.YELLOW+"[KitsuneChat] Default chat now set to "+new KitsuneChatUtils(plugin).getChannelName(str, false));
								return true;
								}
						}
						sender.sendMessage(ChatColor.RED+"[KitsuneChat] Unknown or missing command. See /kc ? for help.");
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

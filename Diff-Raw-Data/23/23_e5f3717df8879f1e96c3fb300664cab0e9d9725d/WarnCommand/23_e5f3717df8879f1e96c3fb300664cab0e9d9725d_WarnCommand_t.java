 package com.andersonhc.PlayerFreeze.command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.andersonhc.PlayerFreeze.PlayerFreeze;
 import com.andersonhc.PlayerFreeze.PlayerFreeze.PluginPermission;
 
 public class WarnCommand implements CommandExecutor {
 
 	private final PlayerFreeze plugin;
 
 	//Constructor
 	public WarnCommand (PlayerFreeze plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel,
 			String[] args) {
 		// TODO Auto-generated method stub
 		if (args.length == 0) {
 			return false;
 		}
 
 		String playerName = null;
 		if (sender instanceof Player) {
 			playerName = ((Player)sender).getDisplayName();
 		} else {
 			playerName = "Server (console)";
 		}
 
 		//warn all
 		if ((args[0].equalsIgnoreCase("all"))) {
 
 			if (!plugin.hasPermission(sender, PluginPermission.WARNALL)) { 
 				sender.sendMessage(ChatColor.BLUE + plugin.getString("GENERAL_NO_PERMISSION"));
 			}
 			
 			String message = null;
 			if (args.length > 0) {
 				message = "";
 				for (int count = 1; count < args.length; count++) {
 					message = message.concat(" " + args[count]);
 				}
 			}
 
 			for (int i = 0; i < plugin.getServer().getOnlinePlayers().length; i++) {
 				Player warned = plugin.getServer().getOnlinePlayers()[i];
 				if (warned == sender) {
 					warned.sendMessage(ChatColor.BLUE + plugin.getString("WARNALL_NOTIFY_PLAYERS"));
 				} else if (plugin.isAdmin(sender))
 					warned.sendMessage(ChatColor.BLUE + plugin.getString("WARNALL_NOTIFY_ADMINS") + ChatColor.AQUA + playerName + ChatColor.BLUE);
 				else {
                    if(plugin.isWarnable(warned))
					   plugin.Warn(warned, sender, message);
 				}
 			}
 			return true;
 		//warn list	
 		} else if (args[0].equalsIgnoreCase("list")) {
 			if (plugin.isAdmin(sender)) {
 				int total = 0;
 				String[] warnedPlayers = plugin.listWarned();
 				if (warnedPlayers == null) {
 					sender.sendMessage(plugin.getString("LIST_NO_WARNED"));
 					return true;
 				}
 				for (String warned: warnedPlayers) {
 					String lmessage = "";
 					if (plugin.getMessage(warned) != null) lmessage = "(" + plugin.getMessage(warned) + ")";
 					sender.sendMessage(ChatColor.AQUA + warned + ChatColor.BLUE + " " + lmessage);
 					total++;
 				}
 				sender.sendMessage("Total: " + ChatColor.AQUA + total + ChatColor.BLUE + plugin.getString("LIST_AMOUNT_WARNED"));
 			} else {
 				sender.sendMessage(ChatColor.BLUE + plugin.getString("GENERAL_NO_PERMISSION"));
 			}
 			return true;
 			//warn player message
 		} else {
 			Player warned = plugin.findOnlinePlayer(args[0]);
 			if (warned != null) {
 				String message = null;
 				if (args.length > 1) {
 					message = "";
 					for (int count = 1; count < args.length; count++) {
 						message = message.concat(" " + args[count]);
 					}
 				}
                if(plugin.isWarnable(warned))
				    plugin.Warn(warned, sender, message);
 			} else {
 				sender.sendMessage(ChatColor.BLUE + "Player " + ChatColor.AQUA + args[0] + ChatColor.BLUE + plugin.getString("GENERAL_INVALID_PLAYER"));
 			}
 			return true;
 		}
 
 	}
 
 }

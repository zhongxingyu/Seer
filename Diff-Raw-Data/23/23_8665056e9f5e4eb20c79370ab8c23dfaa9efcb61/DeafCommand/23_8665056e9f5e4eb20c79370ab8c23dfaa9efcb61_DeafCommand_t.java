 package me.cmastudios.plugins.WarhubModChat.commands;
 
 import me.cmastudios.plugins.WarhubModChat.WarhubModChat;
 import me.cmastudios.plugins.WarhubModChat.util.PlayerInfo;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class DeafCommand implements CommandExecutor {
 	public static WarhubModChat plugin;
     public DeafCommand(WarhubModChat instance) {
         plugin = instance;
     }
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String arg2,
 			String[] args) {
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		if (args.length < 1) {
 			player.sendMessage(ChatColor.YELLOW + "Deafened players:");
 			String plrs = "";
 			for (Player plr : plugin.ignores.keySet()) {
 				plrs += plr.getDisplayName() + ", ";
 			}
 			player.sendMessage(ChatColor.YELLOW + plrs);
 			player.sendMessage(ChatColor.YELLOW
 					+ "Use /deaf <player> to deafen someone.");
 			return true;
 		} else if (args.length == 1) {
 			Player todeafen = PlayerInfo.toPlayer(args[0]);
 			if (todeafen == player) {
 				if (plugin.ignores.containsKey(player)) {
 					plugin.ignores.remove(player);
 					todeafen.sendMessage(ChatColor.YELLOW
							+ "You have been undeafened.");
 				} else {
 					plugin.ignores.put(player, "");
 					todeafen.sendMessage(ChatColor.YELLOW
							+ "You have been deafened.");
 				}
 			} else if (player.hasPermission("warhub.moderator")) {
 				if (plugin.ignores.containsKey(todeafen)) {
 					plugin.ignores.remove(todeafen);
 					player.sendMessage(ChatColor.YELLOW
							+ todeafen.getName() + " has been undeafened.");
 					todeafen.sendMessage(ChatColor.YELLOW
							+ "You have been undeafened.");
 				} else {
 					plugin.ignores.put(todeafen, "");
 					player.sendMessage(ChatColor.YELLOW
							+ todeafen.getName() + " has been deafened.");
 					todeafen.sendMessage(ChatColor.YELLOW
							+ "You have been deafened.");
 				}
 
 			} else {
 				player.sendMessage(ChatColor.RED
 						+ "You do not have permissions to deafen others.");
 			}
 			return true;
 		}
 		return false;
 	
 	}
 
 }

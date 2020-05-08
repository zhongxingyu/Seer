 package com.lenis0012.bukkit.pvp.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.lenis0012.bukkit.pvp.PvpPlayer;
 import com.lenis0012.bukkit.pvp.utils.MathUtil;
 
 public class KdrCommand implements CommandExecutor {
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if(!(sender instanceof Player)) {
 			sender.sendMessage("Thou are part of the undead?");
 			return true;
 		}
 		
 		Player player = (Player) sender;
 		if(args.length == 0) {
 			if(player.hasPermission("pvp.kdr.self")) {
 				PvpPlayer pp = new PvpPlayer(player.getName());
				double kdr = MathUtil.round(pp.get("kills") / pp.get("deaths"), 2);
 				player.sendMessage(ChatColor.GREEN + "Kill death ratio: " + ChatColor.WHITE + String.valueOf(kdr));
 			} else
 				player.sendMessage(ChatColor.RED + "Invalid permissions!");
 		} else if(args.length == 1) {
 			if(player.hasPermission("pvp.kdr.orther")) {
 				Player target = Bukkit.getPlayer(args[0]);
 				if(target != null) {
 					PvpPlayer pp = new PvpPlayer(target.getName());
					double kdr = MathUtil.round(pp.get("kills") / pp.get("deaths"), 2);
 					player.sendMessage(ChatColor.GREEN + "Kill death ratio ("+args[0]+"): " + ChatColor.WHITE + String.valueOf(kdr));
 				} else
 					player.sendMessage(ChatColor.RED + "Invalid player!");
 			} else
 				player.sendMessage(ChatColor.RED + "Invalid permissions!");
 		}
 		return true;
 	}
 }

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
				double kdr = this.getKdr(pp);
 				player.sendMessage(ChatColor.GREEN + "Kill death ratio: " + ChatColor.WHITE + String.valueOf(kdr));
 			} else
 				player.sendMessage(ChatColor.RED + "Invalid permissions!");
 		} else if(args.length == 1) {
 			if(player.hasPermission("pvp.kdr.orther")) {
 				Player target = Bukkit.getPlayer(args[0]);
 				if(target != null) {
 					PvpPlayer pp = new PvpPlayer(target.getName());
					double kdr = this.getKdr(pp);
 					player.sendMessage(ChatColor.GREEN + "Kill death ratio ("+args[0]+"): " + ChatColor.WHITE + String.valueOf(kdr));
 				} else
 					player.sendMessage(ChatColor.RED + "Invalid player!");
 			} else
 				player.sendMessage(ChatColor.RED + "Invalid permissions!");
 		}
 		return true;
 	}
	
	public double getKdr(PvpPlayer pp) {
		int kills = pp.get("kills");
		int deaths = pp.get("deaths");
		
		if(kills == 0 && deaths == 0)
			return 1;
		else if(kills > 0 && deaths == 0) {
			return kills;
		} else if(deaths > 0 && kills == 0) {
			return deaths;
		} else {
			return MathUtil.round(kills / (double) deaths, 2);
		}
	}
 }

 package com.lenis0012.bukkit.pvp.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.lenis0012.bukkit.pvp.PvpLevels;
 import com.lenis0012.bukkit.pvp.PvpPlayer;
 
 public class LevelCommand implements CommandExecutor {
 	private PvpLevels plugin;
 	
 	public LevelCommand(PvpLevels plugin) {
 		this.plugin = plugin;
 	}
 	
 	@Override
	@SuppressWarnings("deprecation")
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if(!sender.hasPermission("pvplevels.change")) {
 			sender.sendMessage("\2474You don't have permission for this command!");
 			return true;
 		}
 		
 		if(args.length > 2) {
 			String user = args[1];
 			Player player = Bukkit.getPlayer(user);
 			PvpPlayer pp;
 			if(player != null && player.isOnline()) {
 				pp = plugin.getPlayer(player);
 			} else {
 				pp = plugin.getPlayer(user);
 			}
 			
 			try {
 				int level = pp.getLevel();
 				int value = Integer.parseInt(args[2]);
 				int maxLevel = plugin.getConfig().getInt("settings.max-level", 100);
 				if(args[0].equalsIgnoreCase("add")) {
 					level = Math.min(level + value, maxLevel);
 				} else if(args[0].equalsIgnoreCase("remove")) {
 					level = Math.max(level - value, 0);
 				} else if(args[0].equalsIgnoreCase("set")) {
 					level = Math.min(Math.max(value, maxLevel), 0);
 				} else {
 					sender.sendMessage("\2474Invalid argument '" + args[0] + "'!");
 					return true;
 				}
 				
 				pp.setLevel(level);
 				sender.sendMessage("\247aLevel set to '" + level + "'!");
 				if(player == null || !player.isOnline()) {
 					pp.save(); //Very important
 				}
 			} catch(Exception e) {
 				sender.sendMessage("\2474Invalid number '" + args[2] + "'!");
 			}
 		} else
 			sender.sendMessage("\247cNot enough aruments!");
 		
 		return true;
 	}
 }

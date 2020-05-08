 package com.lenis0012.bukkit.pvp.commands;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import com.lenis0012.bukkit.pvp.PvpLevels;
 import com.lenis0012.bukkit.pvp.PvpPlayer;
 
 public class LevelCommand implements CommandExecutor {
 	private PvpLevels plugin;
 	
 	public LevelCommand(PvpLevels plugin) {
 		this.plugin = plugin;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if(!sender.hasPermission("pvplevels.change")) {
			sender.sendMessage("4You don't have permission for this command!");
 			return true;
 		}
 		
 		if(args.length > 2) {
 			String user = args[1];
 			PvpPlayer pp = new PvpPlayer(user);
 			if(pp.isCreated()) {
 				try {
 					int level = pp.get("level");
 					int value = Integer.parseInt(args[2]);
 					int maxLevel = plugin.getConfig().getInt("settings.max-level", 100);
 					if(args[0].equalsIgnoreCase("add")) {
 						level = Math.min(level + value, maxLevel);
 					} else if(args[0].equalsIgnoreCase("remove")) {
 						level = Math.max(level - value, 0);
 					} else if(args[0].equalsIgnoreCase("set")) {
 						level = Math.min(Math.max(value, maxLevel), 0);
 					} else {
						sender.sendMessage("4INvalid argument '" + args[0] + "'!");
 						return true;
 					}
 					
 					pp.set("level", level);
					sender.sendMessage("aLevel set to '" + level + "'!");
 				} catch(Exception e) {
					sender.sendMessage("4INvalid number '" + args[2] + "'!");
 				}
 			} else
				sender.sendMessage("4Player not found on database!");
 		}
 		
 		return true;
 	}
 }

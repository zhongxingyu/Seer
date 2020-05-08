 package org.nationsatwar.stickerchart.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.nationsatwar.stickerchart.StickerChart;
 
 public class GetCommand extends StickerChartCommandExecutor {
  
 	public GetCommand(StickerChart stickerChart) {
 		super(stickerChart);
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if(!sender.isOp() && !sender.hasPermission("stickerchart.rep")) {
 			return false;
 		}
 		
 		OfflinePlayer player = null;
 		for(OfflinePlayer p : plugin.getServer().getOfflinePlayers()) {
 			if(p.getName().equalsIgnoreCase(args[0])) {
 				player = p;
 				break;
 			}
 			if(p.getPlayer() != null) {
 				if(p.getPlayer().getDisplayName().equalsIgnoreCase(args[0])) {
 					player = p;
 					break;
 				}
 			}
 		}
 
 		if(player == null) {
 			plugin.sendMessage(sender, ChatColor.RED + "Player "+args[0]+" doesn't exist.");
 			return false;
 		}
 		
		plugin.sendMessage(sender, player.getName() + "has " +plugin.getDatasource().get(player.getName())+" reputation.");
 		
 		return true;
 	}
 }

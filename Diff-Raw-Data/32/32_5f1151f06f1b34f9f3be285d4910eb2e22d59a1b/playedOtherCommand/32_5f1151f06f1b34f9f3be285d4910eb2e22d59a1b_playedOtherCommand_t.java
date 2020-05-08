 package me.tehbeard.BeardStat.commands;
 
 import java.util.Date;
 import java.util.List;
 
 import me.tehbeard.BeardStat.BeardStat;
 import me.tehbeard.BeardStat.containers.PlayerStatManager;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class playedOtherCommand implements CommandExecutor {
 
 	
 	public boolean onCommand(CommandSender sender, Command command, String cmdLabel,
 			String[] args) {
 		Player pp = null ;
 		if(args.length==1){
 			pp = BeardStat.self.getServer().getPlayer(args[0]);
			if(!BeardStat.hasPermission((Player)sender, "command.played")){return true;}
 			if(pp==null){
 				List<Player> ply = BeardStat.self.getServer().matchPlayer(args[0]);
 				if(ply.size()>1){
 					for(Player p:ply){
 						if(p.getName().equals(args[0])){
 							pp = p;
 							break;
 						}
 					}
 				} else if(ply.size()==1){
 					pp = ply.get(0);
 				}
 			}
 
 			if(pp==null){
 				return false;
 			}
 
			long seconds = (long)PlayerStatManager.findPlayerBlob(pp.getName()).getStat("stats","playedfor").getValue();
 
 			seconds +=(				(
 					(new Date()).getTime() - BeardStat.loginTimes.get(pp.getName())
 
 			)/1000L
 			);
 			int weeks   = (int) seconds / 604800;
 			int days = (int)Math.ceil((seconds -604800*weeks) / 86400);
 		    int hours = (int)Math.ceil((seconds - (86400 * days + 604800*weeks)) / 3600);
 		    int minutes = (int)Math.ceil((seconds - (604800*weeks + 86400 * days + 3600 * hours)) / 60);
 		    
 
 			sender.sendMessage(ChatColor.GOLD + pp.getName());
 			sender.sendMessage(ChatColor.LIGHT_PURPLE + "playtime: " + ChatColor.WHITE+ 
 					weeks + ChatColor.LIGHT_PURPLE +  " wks " + ChatColor.WHITE +
 					days + ChatColor.LIGHT_PURPLE + " days " + ChatColor.WHITE+
 					hours + ChatColor.LIGHT_PURPLE + " hours " + ChatColor.WHITE+
 					minutes + ChatColor.LIGHT_PURPLE + " mins ");
 
 
 			return true;
 		}
 		return false;
 
 
 	}
 }

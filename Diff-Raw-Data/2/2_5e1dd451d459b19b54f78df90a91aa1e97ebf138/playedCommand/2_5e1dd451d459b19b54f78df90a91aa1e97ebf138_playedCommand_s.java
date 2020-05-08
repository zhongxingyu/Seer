 package me.tehbeard.BeardStat.commands;
 
 import java.util.List;
 
 import me.tehbeard.BeardStat.BeardStat;
 import me.tehbeard.BeardStat.LanguagePack;
 import me.tehbeard.BeardStat.containers.PlayerStatBlob;
 import me.tehbeard.BeardStat.containers.PlayerStatManager;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 
 public class playedCommand implements CommandExecutor {
 
 	private PlayerStatManager playerStatManager;
 
 	public playedCommand(PlayerStatManager playerStatManager) {
 		this.playerStatManager = playerStatManager;
 	}
 
 	public boolean onCommand(CommandSender sender, Command command, String cmdLabel, String[] args) {
 		long seconds = 0;
 		Player pp = null;
 		
 		if (args.length == 0){
 			//check if we are a player, and use them instead
 			if(sender instanceof Player){
 				pp = (Player)sender;
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.RED + LanguagePack.getMsg("command.error.noconsole.noargs"));
 				return true;
 			}
 		}
 		
 
 		//if they don't have permission, BAD PERSON GO AWAY
		if(!BeardStat.hasPermission((Player)sender, "command.played")){
 			BeardStat.sendNoPermissionError(sender);
 			return true;
 		}
 
 
 		// they put a player name, try and find out who it is.
 		if(args.length == 1){
 			if(!BeardStat.hasPermission(sender, "command.played.other")){
 				BeardStat.sendNoPermissionError(sender);
 				return true;
 			}
 
 			pp = Bukkit.getPlayer(args[0]);
 			if(pp==null){
 				List<Player> ply = Bukkit.getServer().matchPlayer(args[0]);
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
 		}
 
 		else//Funky number of arguments, return false
 		{
 			return false;
 		}
 
 		PlayerStatBlob blob;
 
 		//no player match found, attempt to poll DB directly
 		if(pp==null){
 			blob = playerStatManager.findPlayerBlob(args[0]);//Try to find by exact name
 		}
 		else //poll using found player
 		{
 			blob = playerStatManager.findPlayerBlob(pp.getName());
 			seconds += BeardStat.self().getStatManager().getSessionTime(pp.getName());
 		}
 
 		if(blob != null && blob.getStat("stats", "playedfor").getValue() != 0){
 			sender.sendMessage(ChatColor.GOLD + blob.getName());
 			seconds += blob.getStat("stats","playedfor").getValue() + BeardStat.self().getStatManager().getSessionTime(pp.getName());
 			sender.sendMessage(GetPlayedString(seconds));
 			return true;
 		}                   
 		else
 		{
 			sender.sendMessage(ChatColor.RED + LanguagePack.getMsg("command.error.noplayer",args[0]));
 			return true;
 		}
 
 
 	}
 
 	public static String GetPlayedString(long seconds){
 		String output = LanguagePack.getMsg("command.played.zero");
 		if(seconds > 0){
 			int weeks   = (int) seconds / 604800;
 			int days = (int)Math.ceil((seconds -604800*weeks) / 86400);
 			int hours = (int)Math.ceil((seconds - (86400 * days + 604800*weeks)) / 3600);
 			int minutes = (int)Math.ceil((seconds - (604800*weeks + 86400 * days + 3600 * hours)) / 60);
 
 			output = LanguagePack.getMsg("command.played.output",  
 					weeks ,
 					days  ,
 					hours ,
 					minutes);
 		}
 
 		return output;
 	}
 }

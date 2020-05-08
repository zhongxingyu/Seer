 package cz.vojtamaniak.komplex.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import cz.vojtamaniak.komplex.Komplex;
 
 public class CommandFeed extends ICommand implements CommandExecutor {
 
	public CommandFeed(Komplex plg) {
 		super(plg);
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] arg) {
		if(cmd.getName().equalsIgnoreCase("feed")){
			if(arg.length > 0){
 				feedOther(sender, arg);
			}else{
				feedSelf(sender);
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	private void feedSelf(CommandSender sender){
 		if(sender instanceof Player){
 			Player player = (Player) sender;
 			if(player.isOp() || player.hasPermission("komplex.fly")){
 				player.setFoodLevel(20);
 				player.sendMessage(msgManager.getMessage("FEED_SELF"));
 			}else{
 				player.sendMessage(msgManager.getMessage("NO_PERMISSION"));
 			}
 		}
 	}
 	
 	private void feedOther(CommandSender sender, String[] arg){
 		OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(arg[0]);
 		if(offPlayer.isOnline()){
 			Player player = (Player)offPlayer;
 			if(sender.isOp() || sender.hasPermission("komplex.fly.other")){
 				player.setFoodLevel(20);
 				player.sendMessage(msgManager.getMessage("FEED_OTHER_WHISPER").replaceAll("%NICK%", sender.getName()));
 				sender.sendMessage(msgManager.getMessage("FEED_OTHER").replaceAll("%NICK%", player.getName()));
 			}else{
 				sender.sendMessage(msgManager.getMessage("NO_PERMISSION"));
 			}
 		}else{
 			sender.sendMessage(msgManager.getMessage("PLAYER_OFFLINE"));
 		}
 	} 
 }

 package cz.vojtamaniak.komplex.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import cz.vojtamaniak.komplex.Komplex;
 
 public class CommandFly extends ICommand implements CommandExecutor {
 
	public CommandFly(Komplex plg) {
 		super(plg);
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] arg){
		if(cmd.getName().equalsIgnoreCase("fly")){
			if(arg.length > 0){
 				flyOther(sender, arg);
			}else{
				flySelf(sender);
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	private void flySelf(CommandSender sender){
 		if(sender instanceof Player){
 			Player player = (Player) sender;
 			if(player.isOp() || player.hasPermission("komplex.fly")){
 				if(player.getAllowFlight()){
 					player.setAllowFlight(false);
 					player.sendMessage(msgManager.getMessage("FLY_SELF_OFF"));
 				}else{
 					player.setAllowFlight(true);
 					player.sendMessage(msgManager.getMessage("FLY_SELF_ON"));
 				}
 			}else{
 				player.sendMessage(msgManager.getMessage("NO_PERMISSION"));
 			}
 		}
 	}
 	
 	private void flyOther(CommandSender sender, String[] arg){
 		 OfflinePlayer offP = Bukkit.getOfflinePlayer(arg[0]);
 		 if(offP.isOnline()){
 			 Player player = (Player) offP;
 			 if(sender.isOp() || sender.hasPermission("komplex.fly.other")){
 				 if(player.getAllowFlight()){
 					 player.setAllowFlight(false);
 					 sender.sendMessage(msgManager.getMessage("FLY_OTHER_OFF").replaceAll("%NICK%", player.getName()));
 					 player.sendMessage(msgManager.getMessage("FLY_WHISPER_OFF").replaceAll("%NICK%", sender.getName()));
 				 }else{
 					 player.setAllowFlight(true);
 					 sender.sendMessage(msgManager.getMessage("FLY_OTHER_ON").replaceAll("%NICK%", player.getName()));
 					 player.sendMessage(msgManager.getMessage("FLY_WHISPER_ON").replaceAll("%NICK%", sender.getName()));
 				 }
 			 }else{
 				 sender.sendMessage(msgManager.getMessage("NO_PERMISSION"));
 			 }
 		 }else{
 			 sender.sendMessage(msgManager.getMessage("PLAYER_OFFLINE"));
 		 }
 	}
 }

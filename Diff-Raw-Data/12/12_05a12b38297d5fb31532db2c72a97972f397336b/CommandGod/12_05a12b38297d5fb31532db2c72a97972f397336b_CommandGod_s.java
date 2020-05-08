 package cz.vojtamaniak.komplex.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import cz.vojtamaniak.komplex.Komplex;
 
 public class CommandGod extends ICommand implements CommandExecutor {
 
  public CommandGod(Komplex plg) {
 		super(plg);
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] arg) {
 		if(cmd.getName().equalsIgnoreCase("god")){
 			if(arg.length > 0){
 				godOther(sender, arg);
 			}
 			else{
 				godSelf(sender);
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	private void godOther(CommandSender sender, String[] arg){
 		OfflinePlayer offP = Bukkit.getOfflinePlayer(arg[0]);
 		if(offP.isOnline()){
 			Player player = (Player)offP;
 			if(sender.isOp() || sender.hasPermission("komplex.god.other")){
 				if(plg.getUser(player.getName()).getGodMode()){
 					plg.getUser(player.getName()).setGodMode(false);
					player.sendMessage(msgManager.getMessage("GOD_WHISPER_OFF"));
					sender.sendMessage(msgManager.getMessage("GOD_OTHER_OFF"));
 				}else{
 					plg.getUser(player.getName()).setGodMode(true);
					player.sendMessage(msgManager.getMessage("GOD_WHISPER_ON"));
					sender.sendMessage(msgManager.getMessage("GOD_OTHER_ON"));
 				}
 			}else{
 				sender.sendMessage(msgManager.getMessage("NO_PERMISSION"));
 			}
 		}else{
 			sender.sendMessage(msgManager.getMessage("PLAYER_OFFLINE"));
 		}
 	}
 	
 	private void godSelf(CommandSender sender){
 		if(sender instanceof Player){
 			Player player = (Player)sender;
 			if(player.isOp() || player.hasPermission("komplex.god")){
 				if(plg.getUser(player.getName()).getGodMode()){
 					plg.getUser(player.getName()).setGodMode(false);
 					player.sendMessage(msgManager.getMessage("GOD_SELF_OFF"));
 				}else{
 					plg.getUser(player.getName()).setGodMode(true);
 					player.sendMessage(msgManager.getMessage("GOD_SELF_ON"));
 				}
 			}else{
 				player.sendMessage(msgManager.getMessage("NO_PERMISSION"));
 			}
 		}
 	}
 }

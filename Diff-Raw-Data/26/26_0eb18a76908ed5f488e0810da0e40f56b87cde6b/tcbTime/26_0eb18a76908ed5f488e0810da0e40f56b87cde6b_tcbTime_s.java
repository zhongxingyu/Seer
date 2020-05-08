 package pro.homiecraft.Commands;
 
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class tcbTime implements CommandExecutor {
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(sender.hasPermission("tcb.Time") && (sender instanceof Player)){
 			if(cmd.getName().equalsIgnoreCase("time")){
 				if(args.length < 1) {
 					sender.sendMessage("Missing time!");
 					sender.sendMessage("usage: /time day|night");
 				}else if(args.length > 1){
 					sender.sendMessage("To many arguments!");
 					sender.sendMessage("usage: /time day|night");
 				}else{
 					Player player = (Player) sender;
 					World world = player.getWorld();
 					
 					if (args[0].equalsIgnoreCase("day")){
 						world.setTime(0);
						player.sendMessage("Changed time to day");
 						return true;
 					}else if (args[0].equalsIgnoreCase("night")){
 						world.setTime(16000);
						player.sendMessage("Changed time to night");
 						return true;
 					}else{
 						sender.sendMessage("Wrong time argument!");
 						sender.sendMessage("usage: /time day|night - you said: " + args[0]);
 					}
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 }

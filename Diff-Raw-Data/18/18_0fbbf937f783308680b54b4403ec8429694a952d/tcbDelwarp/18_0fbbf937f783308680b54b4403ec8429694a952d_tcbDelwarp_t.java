 package pro.homiecraft.Commands;
 
 import java.io.File;
 
 import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import pro.homiecraft.TheCommandBook;
 
public class tcbDelwarp implements CommandExecutor {
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (sender.hasPermission("tcb.delwarp")) {
 			if (cmd.getName().equalsIgnoreCase("delwarp")) {
 				if (args.length < 1){
 					sender.sendMessage("Missing warp name!");
 					sender.sendMessage("usage: /delwarp warpName");
 					return true;
 				}else if(args.length > 1){
 					sender.sendMessage("To many arguments!");
 					sender.sendMessage("usage: /delwarp warpName");
 					return true;
 				}else{
 					Player player = (Player) sender;
 					
 					File warpFile = new File(TheCommandBook.pluginST.getDataFolder() + "/data/warps/" + args[0] + ".yml");
 					if (warpFile.exists()){
 						warpFile.delete();						
 						player.sendMessage("Warp: " + args[0] + " deleted.");
 						return true;
 					}else{
 						sender.sendMessage("Warp " + args[0] + " does not exists!");
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 }

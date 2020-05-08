 package team.GunsPlus;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CommandEx implements CommandExecutor{
 	private GunsPlus plugin;
 	
 	public CommandEx(GunsPlus gunsPlus) {
 		plugin = gunsPlus;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if(sender instanceof Player) {
 			Player player = (Player) sender;
 			if(args.length == 1) {
 				String cmd = args[0];
 				if(cmd.equalsIgnoreCase("credits")) {
 					player.sendMessage(GunsPlusListener.credit);
 					return true;
 				}else if(cmd.equalsIgnoreCase("reload")&&player.hasPermission("gunsplus.reload")){
 					plugin.config();
 					plugin.resetFields();
 					plugin.init();
 					sender.sendMessage(ChatColor.GREEN + GunsPlus.PRE + " Configuration files reloaded!");
 				}
 			}
 		} else {
 			//Console
 			if(args.length == 1) {
 				String cmd = args[0];
 				if(cmd.equalsIgnoreCase("credits")) {
 					sender.sendMessage(GunsPlusListener.credit);
 					return true;
 				}else if(cmd.equalsIgnoreCase("reload")){
 					plugin.config();
 					sender.sendMessage(GunsPlus.PRE + " Configuration files reloaded!");
 				}
 			}
 		}
 		return false;
 	}
 
 }

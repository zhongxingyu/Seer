 package fr.frozentux.craftguard2.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 import fr.frozentux.craftguard2.CraftGuardPlugin;
import fr.frozentux.craftguard2.list.List;
 
 public class CgAddListCommand extends CgCommandComponent {
 	
 	public static boolean execute(CommandSender sender, String command, String[] args, CraftGuardPlugin plugin){
 		if(args.length != 1){
 			sender.sendMessage(CgCommandComponent.MESSAGE_ARGUMENTS + "/cg addlist <list>");
 			return false;
 		}else if(!sender.hasPermission(plugin.getConfiguration().getStringKey("baseperm") + ".admin.addlist") && !sender.hasPermission(plugin.getConfiguration().getStringKey("baseperm") + ".admin.*")){
 			sender.sendMessage(CgCommandComponent.MESSAGE_PERMISSION);
 			return false;
 		}else if(plugin.getListManager().getList(args[0]) != null){
 			sender.sendMessage(ChatColor.RED + "List " + args[0] + " already exists !");
 			return false;
 		}
 		
		plugin.getListManager().addList(new List(args[0], null, null, plugin.getListManager()), false);
 		plugin.getListManager().saveList(plugin.getListManager().getList(args[0]));
 		
 		sender.sendMessage(ChatColor.GREEN + "List " + args[0] + " has been created !");
 		
 		return true;
 	}
 	
 }

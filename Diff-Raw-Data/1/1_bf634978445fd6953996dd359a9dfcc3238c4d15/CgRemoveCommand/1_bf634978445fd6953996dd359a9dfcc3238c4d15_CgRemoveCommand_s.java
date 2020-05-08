 package fr.frozentux.craftguard2.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 
 import fr.frozentux.craftguard2.CraftGuardPlugin;
 
 public class CgRemoveCommand extends CgCommandComponent {
 	
 	public static boolean execute(CommandSender sender, String command, String[] args, CraftGuardPlugin plugin) {
 		if(args.length != 2){
 			sender.sendMessage(CgCommandComponent.MESSAGE_ARGUMENTS + "/cg remove <list> <id>");
 			return false;
 		}else if(!sender.hasPermission(plugin.getConfiguration().getStringKey("baseperm") + ".admin.remove") && !sender.hasPermission(plugin.getConfiguration().getStringKey("baseperm") + ".admin.*")){
 			sender.sendMessage(CgCommandComponent.MESSAGE_PERMISSION);
 			return false;
 		}else if(plugin.getListManager().getList(args[0]) == null){
 			sender.sendMessage(ChatColor.RED + "List " + args[0] + " does not exist");
 			return false;
 		}
 		
 		int id = -1;
 		
 		try{
 			id = Integer.valueOf(args[1]);
 		}catch(NumberFormatException e){
 			sender.sendMessage(ChatColor.RED + args[1] + " is not a number !");
 		}
 		
 		plugin.getListManager().getList(args[0]).removeId(id);
 		plugin.getListManager().saveList(plugin.getListManager().getList(args[0]));
 		
 		sender.sendMessage(ChatColor.GREEN + "Successfully removed " + Material.getMaterial(Math.abs(id)).name() + " (" + args[1] + ") from list " + args[0]);
 		
 		return true;
 	}
 }

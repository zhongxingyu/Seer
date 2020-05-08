 package com.gildorymrp.gildorym;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.earth2me.essentials.Essentials;
 
public class SetNameOtherCommand implements CommandExecutor {
 
 		@Override
 		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 			
 			String name;
 			Essentials essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
 			Player target = sender.getServer().getPlayer(args[0]);
 			
 			if (args.length <= 6){
 				if (args.length == 1){
 					if (sender.hasPermission("gildorym.setname.other")){
 						essentials.getUserMap().getUser(target.getName()).setNickname(target.getName());
 						name = essentials.getUser(target).getNickname();
 						target.sendMessage(ChatColor.DARK_AQUA + "name changed: " + name);
 						return true;
 					}else{
 						sender.sendMessage("You don't have permission");
 						return true;
 					}	
 				}else if(args.length == 2){
 					if (sender.hasPermission("gildorym.setname.other")){
 						essentials.getUserMap().getUser(target.getName()).setNickname(args[1]);
 						name = essentials.getUser(target).getNickname();
 						target.sendMessage(ChatColor.DARK_AQUA + "name changed: " + name);
 						return true;
 					}else{
 						sender.sendMessage("You don't have permission");
 						return true;
 					}
 				}else if(args.length == 3){
 					if (sender.hasPermission("gildorym.setname.other")){
 						essentials.getUserMap().getUser(target.getName()).setNickname(args[1] + " " + args[2]);
 						name = essentials.getUser(target).getNickname();
 						target.sendMessage(ChatColor.DARK_AQUA + "name changed: " + name);
 						return true;
 					}else{
 						sender.sendMessage("You don't have permission");
 						return true;
 					}
 				}else if(args.length == 4){
 					if (sender.hasPermission("gildorym.setname.other")){
 						essentials.getUserMap().getUser(target.getName()).setNickname(args[1] + " " + args[2] + " " + args[3]);
 						name = essentials.getUser(target).getNickname();
 						target.sendMessage(ChatColor.DARK_AQUA + "name changed: " + name);
 						return true;
 					}else{
 						sender.sendMessage("You don't have permission");
 						return true;
 					}
 				}else if(args.length == 5){
 					if (sender.hasPermission("gildorym.setname.other")){
 						essentials.getUserMap().getUser(target.getName()).setNickname(args[1] + " " + args[2] + " " + args[3] + " " + args[4]);
 						name = essentials.getUser(target).getNickname();
 						target.sendMessage(ChatColor.DARK_AQUA + "name changed: " + name);
 						return true;
 					}else{
 						sender.sendMessage("You don't have permission");
 						return true;
 					}
 				}else if(args.length == 6){
 					if (sender.hasPermission("gildorym.setname.other")){
 						essentials.getUserMap().getUser(target.getName()).setNickname(args[1] + " " + args[2] + " " + args[3] + " " + args[4] + " " + args[5]);
 						name = essentials.getUser(target).getNickname();
 						target.sendMessage(ChatColor.DARK_AQUA + "name changed: " + name);
 						return true;
 					}else{
 						sender.sendMessage("You don't have permission");
 						return true;
 					}
 				}else{
 					sender.sendMessage(ChatColor.DARK_RED + "illegal argument amount");
 					return false;
 				}
 			}
 			return false;
 	}
 }

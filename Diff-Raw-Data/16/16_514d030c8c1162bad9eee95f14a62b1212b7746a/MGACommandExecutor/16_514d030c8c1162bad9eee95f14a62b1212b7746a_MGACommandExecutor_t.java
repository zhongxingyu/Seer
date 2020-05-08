 package com.github.mineguild.MineguildAdmin;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 
 public class MGACommandExecutor implements CommandExecutor {
 	
 	
 	public MGACommandExecutor(Main plugin) {
 	}
 	@Override
 	//Command interpreter
 		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 			if(cmd.getName().equalsIgnoreCase("mga")){
 				//If the args are length 0 or the args[0] isnt equal "version" it will return false
				if (args.length == 0 || !args[0].equalsIgnoreCase("version")){
 					return false;
 				}
 				if (args[0].equalsIgnoreCase("version")) {
 				//Show version to sender and return true if the value of args[0] is equal to "version"
 				sender.sendMessage("MineguildAdmin V0.3");
 				return true;
 				}
 			}
 		//Introducing /gm command
 		 if(cmd.getName().equalsIgnoreCase("gm")){
 			 //If there are no args, simply act with the command sender
 			 if(args.length == 0){
 				 //Only do this if the sender is instanceof player
 				 if(sender instanceof Player){
 				 Player p = (Player) sender;
 				 //If the gamemode is survival it will switch to creative and vice versa
 				 //Also returning true, for the correctness
 					 if (p.getGameMode().equals(GameMode.SURVIVAL)){
 						 p.setGameMode(GameMode.CREATIVE);
 						 return true;
 					 }
 					 else if (p.getGameMode().equals(GameMode.CREATIVE)){
 						 p.setGameMode(GameMode.SURVIVAL);
 						 return true;
 					 }
 				 }
 				 //If the sender is not instanceof player send message with console use back to the sender
 				 else {
					 sender.sendMessage(ChatColor.RED+"Please use /gm <player> on console!");
 					 return true;
 				 } 
 			 }
 		 }
 		
 		     //If the args have the length 1 continue
 			 if(args.length == 1) {
 				 Player p = Bukkit.getPlayerExact(args[0]);
 				 //If the above defined player isnt null continue
 				 if(p != null){
 					 //If the gamemode is survival it will switch to creative and vice versa
 					 //Also returning true, for the correctness
 					 if (p.getGameMode().equals(GameMode.SURVIVAL)){
 						 p.setGameMode(GameMode.CREATIVE);
 						 return true;
 					 }
 					 else if (p.getGameMode().equals(GameMode.CREATIVE)){
 						 p.setGameMode(GameMode.SURVIVAL);
 						 return true;
 					 }
 					
 				 
 				 }
 				 //If output of Bukkit.getPlayerExact(args[0] was null, send error message.
 				 else {
 					 sender.sendMessage(ChatColor.RED+"Player is not online!");
 					 return true;
 				 }
 			 }
 		 return false;
 	}
 
 }
 

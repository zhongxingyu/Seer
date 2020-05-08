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
         boolean sucess = false;
 //Introducing /mga command
 		if(cmd.getName().equalsIgnoreCase("mga")){
 				//If the args are length 0 or the args[0] isnt equal "version" it will return false
 				if (args.length == 0 || !args[0].equalsIgnoreCase("version")){
 					sucess = false;
 				}
 				if (args[0].equalsIgnoreCase("version")) {
 				//Show version to sender and return true if the value of args[0] is equal to "version"
 				sender.sendMessage("MineguildAdmin V0.3");
 				sucess = true;
 				}
 		}
 //Introducing /heal command
 		 if(cmd.getName().equalsIgnoreCase("heal")){
 			 //If there are no args, simply act with the command sender
 			 if(args.length == 0){
 				 //Only do this if the sender is instanceof player
 				 if(sender instanceof Player){
 				 Player p = (Player) sender;
 				 //set max Health and message
 					 p.setHealth(20);
 					 p.sendMessage(ChatColor.RED+"You feel restored");
 				 }
 				 //If the sender is not instanceof player send message with console use back to the sender
 				 else {
 					 sender.sendMessage(ChatColor.RED+"Please use /heal <player> on console!");
 					 sucess = true;
 				 } 
 			 }
 		 
 	     //If the args have the length 1 continue
 			 else {
 			 Player p = Bukkit.getPlayerExact(args[0]);
 			 //If the above defined player isnt null continue
 			 if(p != null){
 				 //set max Health and message
 				 String string3 = p.getName();
 				 p.setHealth(20);
 				 p.sendMessage(ChatColor.RED+"You feel restored");
 				 sender.sendMessage(ChatColor.RED+"You just healed" + string3);
 			 
 			 }
 			 //If output of Bukkit.getPlayerExact(args[0] was null, send error message.
 			 else {
 				 sender.sendMessage(ChatColor.RED+"Player is not online!");
 				 sucess = true;
 			 }
 		 }
 //Introducing /feed command
 			 if(cmd.getName().equalsIgnoreCase("feed")){
 				 //If there are no args, simply act with the command sender
 				 if(args.length == 0){
 					 //Only do this if the sender is instanceof player
 					 if(sender instanceof Player){
 					 Player p = (Player) sender;
 					 //set max hunger level and message
 						 p.setFoodLevel(20);
 						 p.sendMessage(ChatColor.RED+"You feeded yourself");
 					 }
 					 //If the sender is not instanceof player send message with console use back to the sender
 					 else {
 						 sender.sendMessage(ChatColor.RED+"Please use /feed <player> on console!");
 						 sucess = true;
 					 } 
 				 }
 			 
 		     //If the args have the length 1 continue
 				 else {
 				 Player p = Bukkit.getPlayerExact(args[0]);
 				 //If the above defined player isnt null continue
 				 if(p != null){
 					 //set max hunger level and message both
 					 String string2 = sender.getName();
 					 p.setFoodLevel(20);
 					 p.sendMessage(ChatColor.RED+"You were feeded");
 					 sender.sendMessage(ChatColor.RED+"You just feeded" + string2);
 				 
 				 }
 				 //If output of Bukkit.getPlayerExact(args[0] was null, send error message.
 				 else {
 					 sender.sendMessage(ChatColor.RED+"Player is not online!");
 					 sucess = true;
 				 }
 			 }
 //Introducing /check command
 			 if(cmd.getName().equalsIgnoreCase("check")){
 				 //If there are no args, simply act with the command sender
 				 if(args.length == 0){
 					 //Only do this if the sender is instanceof player
 					 if(sender instanceof Player){
 					 Player p = (Player) sender;
 					 //check health then /2 and print
 						 double health = p.getHealth();
 						 health = health / 2.0;
 						 double hunger = p.getFoodLevel();
 						 hunger = hunger / 2.0;
 						 p.sendMessage(ChatColor.RED+"Your Health is" + health);
 						 p.sendMessage(ChatColor.RED+"Your Hunger is" + hunger);
 					 }
 					 //If the sender is not instanceof player send message with console use back to the sender
 					 else {
 						 sender.sendMessage(ChatColor.RED+"Please use /heal <player> on console!");
 						 sucess = true;
 					 } 
 				 }
 			 
 		     //If the args have the length 1 continue PENIS
 				 else {
 				 Player p = Bukkit.getPlayerExact(args[0]);
 				 //If the above defined player isnt null continue
 				 if(p != null){
 					 //set max Health and message
 					 double health = p.getHealth();
 					 String string1 = p.getName();
 					 double hunger = p.getFoodLevel();
 					 hunger = hunger / 2.0;
 					 health = health / 2.0;
 					 sender.sendMessage(ChatColor.RED + string1 +"'s Health is" + health);
 					 sender.sendMessage(ChatColor.RED + string1 +"'s Hunger is" + hunger);
 				 
 				 }
 				 //If output of Bukkit.getPlayerExact(args[0] was null, send error message.
 				 else {
 					 sender.sendMessage(ChatColor.RED+"Player is not online!");
 					 sucess = true;
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
 						 sucess = true;
 					 }
 					 else if (p.getGameMode().equals(GameMode.CREATIVE)){
 						 p.setGameMode(GameMode.SURVIVAL);
 						 sucess = true;
 					 }
 				 }
 				 //If the sender is not instanceof player send message with console use back to the sender
 				 else {
 					 sender.sendMessage(ChatColor.RED+"Please use /gm <player> on console!");
 					 sucess = true;
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
 						 sender.sendMessage(ChatColor.GOLD + "Target is now in creative mode!");
 						 p.sendMessage(ChatColor.GOLD + "You are now in creative mode!");
 						 sucess = true;
 					 }
 					 else if (p.getGameMode().equals(GameMode.CREATIVE)){
 						 p.setGameMode(GameMode.SURVIVAL);
 						 sender.sendMessage(ChatColor.GOLD + "Target is now in survival mode!");
 						 p.sendMessage(ChatColor.GOLD + "You are now in survival mode!");
 						 sucess = true;
 					 }
 					 else if (args[1].equalsIgnoreCase("c")){
 						 p.setGameMode(GameMode.CREATIVE);
 						 sender.sendMessage(ChatColor.GOLD + "Target is now in creative mode!");
 						 sucess = true;
 					 }
 					 else if(args[1].equalsIgnoreCase("s")){
 						 p.setGameMode(GameMode.SURVIVAL);
 						 sender.sendMessage(ChatColor.GOLD + "Target is now in survival mode!");
 						 sucess = true;
 					 }
 				 
 				 }
				 //If output of Bukkit.getPlayerExact(args[0] was null, send error message.
 				 else {
 					 sender.sendMessage(ChatColor.RED+"Player is not online!");
 					 sucess = true;
 				 }
 			 }
 	}
 		 else {
 			 sucess = false;
 		 }
 		 return sucess;
 			 }
 			 }
 		 }
 		 return false;
 	}
 }
 
 

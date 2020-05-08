 package com.github.mineguild.MineguildAdmin;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 
 public class MGACommandExecutor implements CommandExecutor {
 	public MGACommandExecutor(Main plugin) {
 	}
 	@SuppressWarnings({ "unused"})
 	@Override
 	//Command interpreter
 		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		String pre = ChatColor.AQUA + "[MGA] " + ChatColor.WHITE;
 		String white = " " + ChatColor.WHITE;
 		String yellow = " " + ChatColor.YELLOW;
 		String Aqua = " " + ChatColor.WHITE;
 		//Introducing /heal command
 		 if(cmd.getName().equalsIgnoreCase("heal")){
 			 //If there are no args, simply act with the command sender
 			 if(args.length == 0){
 				 //Only do this if the sender is instanceof player
 				 if(sender instanceof Player){
 				 Player p = (Player) sender;
 				 //set max Health and message
 					 p.setHealth(20);
 					 p.sendMessage(pre + "You feel restored");
 					 return true;
 				 }
 				 //If the sender is not instanceof player send message with console use back to the sender
 				 else {
 					 sender.sendMessage(pre + "Please use" + yellow + "/heal <player>" + white + "on console!");
 					 return true;
 				 } 
 			 }
 	     //If the args have the length 1 continue
 			 else {
 			 Player p = Bukkit.getPlayerExact(args[0]);
 			 //If the above defined player isnt null continue
 			 if(p != null){
 				 //set max Health and message
 				 String pname = p.getName();
 				 p.setHealth(20);
 				 p.sendMessage(pre + "You feel restored");
 				 sender.sendMessage(pre + "You just healed " + yellow + pname);
 				 return true;
 			 }
 			 //If output of Bukkit.getPlayerExact(args[0] was null, send error message.
 			 else {
 				 sender.sendMessage(pre + "Player is not online!");
 				 return true;
 			 }
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
 						 p.sendMessage(pre + "You feeded yourself");
 						 return true;
 					 }
 					 //If the sender is not instanceof player send message with console use back to the sender
 					 else {
 						 sender.sendMessage(pre +"Please use" + yellow + "/feed <player>" + white + "on console!");
 						 return true;
 					 } 
 				 }
 			 
 		     //If the args have the length 1 continue
 				 else {
 				 Player p = Bukkit.getPlayerExact(args[0]);
 				 //If the above defined player isn´t null continue
 				 if(p != null){
 					 //set max hunger level and message both
 					 String pname = p.getName();
 					 p.setFoodLevel(20);
 					 p.sendMessage(pre + "You were feeded.");
 					 sender.sendMessage(pre + "You just feeded " + pname);
 					 return true;
 				 
 				 }
 				 //If output of Bukkit.getPlayerExact(args[0] was null, send error message.
 					 else {
 						 sender.sendMessage(pre + "Player is not " + ChatColor.GREEN + "online" + ChatColor.WHITE + "!");
 						 return true;
 					 }
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
 					     String gm = "Error";
 						 if (p.getGameMode().equals(GameMode.SURVIVAL)){
 							 gm = "survival";
 						 }
 						 else {
 							 gm = "creative";
 						 }
 						 double health = p.getHealth();
 						 double hunger = p.getFoodLevel();
 						 double prohunger;
 						 double prohealth;
 						 prohunger = (hunger * 5);
 						 prohealth = (health * 5);
 						 health = health / 2.0;
 						 hunger = hunger / 2.0;
 
 						 p.sendMessage(pre + "Your Health is" + yellow + health + yellow + health + white + "(" + ChatColor.YELLOW + prohealth + "%" + ChatColor.WHITE + ")");
 						 p.sendMessage(pre + "Your Hunger is" + yellow + hunger + yellow + health + white + "(" + ChatColor.YELLOW + prohunger + "%" + ChatColor.WHITE + ")");
 						 p.sendMessage(pre + "Your currently in" + yellow + gm + white +"Gamemode");
 						 return true;
 						 }
 					 //If the sender is not instanceof player send message with console use back to the sender
 					 else {
 						 sender.sendMessage(pre  + "Please use" + yellow + "/check <player>" + white + "on console!");
 						 return true;
 					 }
 				 }
 			     //If the args have the length 1 continue
 					 if(args.length == 1) {
 					 Player p = Bukkit.getPlayerExact(args[0]);
 					 //If the above defined player isn´t null continue
 					 if(p != null){
 						 //set max Health and message
 						 double health = p.getHealth();
 						 double prohunger;
 						 double prohealth;
 						 String string1 = p.getName();
 						 double hunger = p.getFoodLevel();
 						 prohunger = (hunger * 5);
 						 prohealth = (health * 5);
 						 hunger = hunger / 2.0;
 						 health = health / 2.0;
 
 					     String gm = "Error";
 						 if (p.getGameMode().equals(GameMode.SURVIVAL)){
 							 gm = "survival";
 						 }
 						 else {
 							 gm = "creative";
 						 }
 						 sender.sendMessage(pre + yellow + string1 +"'s" + white + "Health is " + yellow + health + white + "(" + ChatColor.YELLOW + prohealth + "%" + ChatColor.WHITE + ")");
 						 sender.sendMessage(pre + yellow + string1 +"'s" + white + "Hunger is " + yellow + hunger + white + "(" + ChatColor.YELLOW + prohunger + "%" + ChatColor.WHITE + ")");
 						 sender.sendMessage(pre + ChatColor.YELLOW + string1 +"'s" + white +"Gamemode is"+ yellow + gm);
 						 return true;
 					 
 					 }
 					 //If output of Bukkit.getPlayerExact(args[0] was null, send error message.
 					 else {
 						 sender.sendMessage(pre + ChatColor.RED+"Player is not online!");
 						 return true;
 					 }
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
 						 p.sendMessage(pre + ChatColor.GOLD + "You are now in creative mode!");
 						 return true;
 					 }
 					 else if (p.getGameMode().equals(GameMode.CREATIVE)){
 						 p.setGameMode(GameMode.SURVIVAL);
 						 p.sendMessage(pre + ChatColor.GOLD + "You are now in survival mode!");
 						 return true;
 					 }
 				 }
 				 //If the sender is not instanceof player send message with console use back to the sender
 				 else {
 					 sender.sendMessage(pre + ChatColor.RED + "Please use /gm <player> <gamemode> on console!");
 					 return true;
 				 }
 			 }
 		     //If the args have the length 1 continue
 		      if(args.length == 1) {
 		    	 if(args[0].equalsIgnoreCase("s") || args[0].equalsIgnoreCase("c") || args[0].equals("1") || args[0].equals("0")){
 		    		 
 			    		 if(sender instanceof Player){
 			    			 
 			    			 Player p = (Player) sender;
 			    			 
 				    		 if (args[0].equalsIgnoreCase("c") || args[0].equals(1)){
 								 p.setGameMode(GameMode.CREATIVE);
 								 sender.sendMessage(pre + ChatColor.GOLD + "You are now in creative mode!");
 								 return true;
 							 }
 							 if(args[0].equalsIgnoreCase("s") || args[0].equals(0)){
 								 p.setGameMode(GameMode.SURVIVAL);
 								 sender.sendMessage(pre + ChatColor.GOLD + "You are now in survival mode!");
 								 return true;
 							 }
 			    		 }
 			    		 else{
 			    			 sender.sendMessage(pre + ChatColor.RED+"Please use /gm <player> <gamemode> on console!");
 							 return true;
 			    		 }
 		    	 }
 		    	 else{
 		    		 if(sender instanceof Player){
 						 //If the above defined player isn´t null continue
 						 Player p = Bukkit.getPlayerExact(args[0]);
 						 if(p != null){
 							 String pname = p.getDisplayName();
 							 //If the gamemode is survival it will switch to creative and vice versa
 							 //Also returning true, for the correctness
 							 if (p.getGameMode().equals(GameMode.SURVIVAL)){
 								 p.setGameMode(GameMode.CREATIVE);
 								 sender.sendMessage(pre + yellow + pname + ChatColor.GOLD + " is now in creative mode!");
 								 p.sendMessage(pre + ChatColor.GOLD + "You are now in creative mode!");
 								 return true;
 							 }
 							 if (p.getGameMode().equals(GameMode.CREATIVE)){
 								 p.setGameMode(GameMode.SURVIVAL);
 								 sender.sendMessage(pre + yellow + pname + ChatColor.GOLD + " is now in survival mode!");
 								 p.sendMessage(pre + ChatColor.GOLD + "You are now in survival mode!");
 								 return true;
 							 }
 						 }
 					 //If output of Bukkit.getPlayerExact(args[0] was null, send error message.
 						 else{
 							 sender.sendMessage(pre + ChatColor.RED + "Player is not online!");
 							 return true;
 						 }
 		    		 }
 		    		 else{
 		    			 sender.sendMessage(pre + "Please use" + yellow + "/gm <player> <gamemode>" + white + "on console!");
 						 return true; 
 		    		 }
 		    	 }
 			 }
 		     else if (args.length == 2) {
 		    	 if(sender instanceof Player){
 			    	 Player p = Bukkit.getPlayerExact(args[0]);
 			    	 String pname = p.getDisplayName();
 			    	 if(p != null){
 						 if (args[1].equalsIgnoreCase("c") || args[1].equals("1")){
 							 p.setGameMode(GameMode.CREATIVE);
 							 sender.sendMessage(pre + yellow + pname + ChatColor.GOLD + " is now in creative mode!");
 							 return true;
 						 }
 						 if(args[1].equalsIgnoreCase("s") || args[1].equals("0")){
 							 p.setGameMode(GameMode.SURVIVAL);
 							 sender.sendMessage(pre + yellow + pname + ChatColor.GOLD + " is now in survival mode!");
 							 return true;
 						 }
 			    	 }
 			    	 else{
 						 sender.sendMessage(pre + ChatColor.RED+"Player is not online!");
 						 return true;
 					 }
 		    	 }
 		    	 else{
 		    		 sender.sendMessage(pre + "Please use" + yellow + "/gm <player> <gamemode>" + white + "on console!");
 					 return true;
 		    	 }
 		     }
 		 }
 //Introducing /spawn
 		 if(cmd.getName().equalsIgnoreCase("spawn")){
 			 if(args.length == 0){
 				 if(sender instanceof Player){
 					 Player p = (Player) sender;
 					 Location l = p.getWorld().getSpawnLocation();
 					 p.teleport(l);
 					 sender.sendMessage(pre + "You were teleported to the spawn!");
 					 return true;
 				 }
 				 else{
 					 sender.sendMessage(pre + ChatColor.RED + "You cant use this command from console!");
 					 return true;
 				 }
 			 }
 			 else{
 				 return false;
 			 }
 		 }
 //Introducing /setspawn
 		 if(cmd.getName().equalsIgnoreCase("setspawn")){
 			 if(sender instanceof Player){
 				 Player p = (Player) sender;
 				 int x = p.getLocation().getBlockX();
 				 int y = p.getLocation().getBlockY();
 				 int z = p.getLocation().getBlockZ();
 				 p.getWorld().setSpawnLocation(x, y, z);
 				 sender.sendMessage(pre + "The spawn point was set to you location");
 				 return true;
 			 }
 			 else{
 				 sender.sendMessage(pre + ChatColor.RED + "You only can use this command as player!");
 				 return true;
 			 }
 		 }
 		 if(cmd.getName().equalsIgnoreCase("i")){
 			 if(sender instanceof Player){
				 if(args.length <= 1){
 					 return false;
				 }
 				 else{
					 sender.sendMessage(args[0] + args[1] + "Test");
 					 return true;
 				 }
 			 }
			 else
			 return false;
 		 }
 	return false;
 	}
 	
 }

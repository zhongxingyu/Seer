 package com.github.Markey50.BuildChallenge;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class BIni implements CommandExecutor {
 	
 	String[] defaultMessage = new String[]{
 		ChatColor.WHITE + "Build Challenge " + ChatColor.DARK_BLUE + "// " + ChatColor.BLUE + "INITIATOR " + ChatColor.AQUA + "help commands",
 		ChatColor.RED + "----------",
 		ChatColor.GREEN + "| " + ChatColor.AQUA + "/bini initiate " + ChatColor.WHITE + "- " + ChatColor.AQUA + "Start a build challenge event",
 		ChatColor.GREEN + "| " + ChatColor.AQUA + "/bini invite <playername> " + ChatColor.WHITE + "- " + ChatColor.AQUA + "Invite a player to the event",
 		ChatColor.GREEN + "| " + ChatColor.AQUA + "/bini invite all " + ChatColor.WHITE + "- " + ChatColor.AQUA + "Invite all players to event",
 		ChatColor.GREEN + "| " + ChatColor.AQUA + "/bini settime <time> <s, m, h> " + ChatColor.WHITE + "- " + ChatColor.AQUA + "Set time limit for build challenge",
 		ChatColor.GREEN + "| " + ChatColor.AQUA + "/bini start " + ChatColor.WHITE + "- " + ChatColor.AQUA + "Officially start the challenge",
 		ChatColor.GREEN + "| " + ChatColor.AQUA + "/bini timechange <new time> <s, m , h> " + ChatColor.WHITE + "- " + ChatColor.AQUA + "Set the timer to the time specified",
 		ChatColor.GREEN + "| " + ChatColor.AQUA + "/bini stop " + ChatColor.WHITE + "- " + ChatColor.AQUA + "Force stop an event",
 		ChatColor.GREEN + "| " + ChatColor.AQUA + "/bini viewjudges " + ChatColor.WHITE + "- " + ChatColor.AQUA + "See list of current judges",
 		ChatColor.GREEN + "| " + ChatColor.AQUA + "/bini invitejudge <player> " + ChatColor.WHITE + "- " + ChatColor.AQUA + "Invite a player to judge",
 		ChatColor.GREEN + "| " + ChatColor.AQUA + "/bini kickjudge <player> " + ChatColor.WHITE + "- " + ChatColor.AQUA + "Kick a player from the judge list",
 		ChatColor.GREEN + "| " + ChatColor.AQUA + "/bini viewvotes " + ChatColor.WHITE + "- " + ChatColor.AQUA + "View the judges votes",
 		ChatColor.GREEN + "| " + ChatColor.AQUA + "/bini comment <cell number> <comment>" + ChatColor.WHITE + "- " + ChatColor.AQUA + "Comment on a cell",
 		ChatColor.GREEN + "| " + ChatColor.AQUA + "/bini vote <cell number>" + ChatColor.WHITE + "- " + ChatColor.AQUA + "Vote on a cell",
 		ChatColor.GREEN + "| " + ChatColor.AQUA + "/bini winners <first> <second> <third>" + ChatColor.WHITE + "- " + ChatColor.AQUA + "Select event winners (by cell number)",
 		ChatColor.GREEN + "| " + ChatColor.AQUA + "/bini transfer <player> " + ChatColor.WHITE + "- " + ChatColor.AQUA + "Transfer initiator role to another approved player",
 		ChatColor.RED + "----------"
 		};
 	
 	String header = ChatColor.WHITE + "BuildChallenge " + ChatColor.DARK_BLUE + "// " + ChatColor.AQUA;
 
 	BuildChallenge plugin;
 	public BIni(BuildChallenge instance) {
 		plugin = instance;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		//Develop argument commands for ./bini 
 		if (cmd.getName().equalsIgnoreCase("bini")) {
 			
 			switch (args.length){
 			//check for args length
 			
 			default:
 				sender.sendMessage(defaultMessage);
 			break;
 			
 			case 1: case 2: case 3:
 				
 				switch (args[0].toLowerCase()) {
 				
 					default:
 						sender.sendMessage(defaultMessage);
 					break;
 					
 					case "help": case "?":
 						
 						if (args.length == 1) {
 							sender.sendMessage(defaultMessage);
 						break;
 						}
 					break;
 					
 					case "initiate":
 						//Begins the process of starting an event
 						if(sender.hasPermission("buildchallenge.initiator")) {
 							//TODO Choose the arena 
 							
 							//Broadcast invitation
 							Bukkit.getServer().broadcastMessage(AS(header + sender.getName() + "has begun a &fBuildChallenge&b! &e/builder accept &bto join the event!"));
 							//TODO Then set timer for time specified. Default to default timer in config
 							
 							//TODO Then clear arena cells
 							
 						}else{
 							sender.sendMessage(AS(header + "You are not a registered Initiator!"));
 						}
 					break;
 					
 					case "invite":
 						//invite a player to participate in the event
 						if(sender.hasPermission("buildchallenge.initiator")) {
 							//Send a message to specified player asking them to join ./bini invite <playername>
 							Bukkit.getPlayer(args[1]).sendMessage(AS(header + "Don't be a square! Come join the fun at the build challenge! &e/build accept &bto join in!"));
							if (args[1] == null){
 								sender.sendMessage(AS(header + "&cYou specify a player name!"));
 							}
 						}else{
 							sender.sendMessage(AS(header + "You are not a registered Initiator!"));
 						}
 					break;
 					
 					case "inviteall":
 						//invite all players to participate in the event
 						if(sender.hasPermission("buildchallenge.initiator")) {
 							//Send a message to all players asking them to join
 							Bukkit.getServer().broadcastMessage(AS(header + "The BuildChallenge is starting soon! Use &e/build accept &bto Join the Challenge!"));
 						}else{
 							sender.sendMessage(AS(header + "You are not a registered Initiator!"));
 						}
 					break;
 					
 					case "settime":
 						//Set the timer to the specified time
 						if(sender.hasPermission("buildchallenge.initiator")) {
 							//TODO Set a timer to the specified time and pause the timer until start
 							
 							//TODO If no timer is set, then default to config time limit
 							
 						}else{
 							sender.sendMessage(AS(header + "You are not a designated Initiator!"));
 						}
 					break;
 					
 					case "start":
 						//Officially start the event.
 						if(sender.hasPermission("buildchallenge.initiator")) {
 							//TODO teleport players to assigned cell number
 							
 							//TODO clear inventories
 							
 							//TODO start timer
 							
 							
 						}else{
 							sender.sendMessage(AS(header + "You are not a designated Initiator!"));
 						}
 					break;
 					
 					case "timechange":
 						//Reset the timer to the specified time
 						if(sender.hasPermission("buildchallenge.initiator")) {
 							//TODO Reset timer to specified time, automatically start timer with new time
 							
 						}else{
 							sender.sendMessage(AS(header + "You are not a designated Initiator!"));
 						}
 					break;
 					
 					case "stop":
 						//force stop the event
 						if(sender.hasPermission("buildchallenge.initiator")) {
 							//TODO stop timer, revoke build rights, survival inventory, teleport back to lobby
 							
 						}else{
 							sender.sendMessage(AS(header + "You are not a designated Initiator!"));
 						}
 					break;
 					
 					case "viewjudges":
 						//view the list of current judges
 						if(sender.hasPermission("buildchallenge.initiator")) {
 							//Retrieve judgeList and display to sender
 							List <String> judgeList = plugin.datacore.getStringList("Judges");
 								for (String currentList : judgeList){	
 									sender.sendMessage(AS(header + currentList));
 								}
 						}else{
 							sender.sendMessage(AS(header + "You are not a designated Initator!"));
 						}
 					break;
 					
 					case "invitejudges":
 						//invite a player to judge the current event
 						if(sender.hasPermission("buildchallenge.initiator")) {
 							//Send a message to a player inviting them to judge
 							Bukkit.getPlayer(args[1]).sendMessage(AS(header + "Come help me please! The BuildChallenge needs a judge! &e/bjudge register &bto join!"));
 						}else{
 							sender.sendMessage(AS(header + "You are not a designated initiator!"));
 						}
 					break;
 					
 					case "kickjudge":
 						//kick a player from the judge the current judge
 						if(sender.hasPermission("buildchallenge.initiator")) {
 							//remove player name from judgeList
 							List <String> judgeList = plugin.datacore.getStringList("Judges");
 							judgeList.remove(args[1]);
 							plugin.datacore.set("Judges", judgeList);
 							plugin.saveYamls();
 							sender.sendMessage(AS(header + "successfully &cremoved &b " + (args[1]) + "from judges for this event."));
 						}else{
 							sender.sendMessage(AS(header + "You are not a designated Initiator!"));
 						}
 					break;
 					
 					case "viewvotes":
 						//view votes list
 						if(sender.hasPermission("buildchallenge.initiator")) {
 							//Retrieve voteList
 							List <String> voteList = plugin.datacore.getStringList("Votes");
 							for (String currentList : voteList){	
 								sender.sendMessage(AS(header + currentList));
 							}
 						}
 					
 					case "winners":
 						//Choose the winners of the current event
 						if(sender.hasPermission("buildchallenge.initiator")) {
 							//TODO take winners 1, 2 and 3 and broadcast the winners in order
 							
 						}else{
 							sender.sendMessage(AS(header + "You are not a designatd Initiator!"));
 						}
 					break;
 					
 					}
 				}
 			
 			}
 		
 		
 		return true;
 	}
 	
 	public String AS(String textToBeTranslated){
 		
 		textToBeTranslated = ChatColor.translateAlternateColorCodes('&', textToBeTranslated);
 		return textToBeTranslated;
 		
 	}
 }

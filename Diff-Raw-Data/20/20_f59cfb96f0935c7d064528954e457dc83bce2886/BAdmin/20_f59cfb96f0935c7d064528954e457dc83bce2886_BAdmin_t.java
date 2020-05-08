 package com.github.Markey50.BuildChallenge;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class BAdmin implements CommandExecutor {
 	
 	String[] defaultMessage = new String[] {
 		ChatColor.WHITE + "Build Challenge " + ChatColor.DARK_BLUE + "// " + ChatColor.RED + "ADMIN " + ChatColor.AQUA + "help commands",
 		ChatColor.RED + "----------",
 		ChatColor.GREEN + "| " + ChatColor.AQUA + "/badmin arenacreate " + ChatColor.WHITE + "- " + ChatColor.AQUA + "Walks through the steps of creating an arena",
 		ChatColor.GREEN + "| " + ChatColor.AQUA + "/badmin setinitiator " + ChatColor.WHITE + "- " + ChatColor.AQUA + "Add a player as a designated event initiator",
 		ChatColor.GREEN + "| " + ChatColor.AQUA + "/badmin reminitiator " + ChatColor.WHITE + "- " + ChatColor.AQUA + "Removes a player from the designated event initiator list",
 		ChatColor.RED + "----------"
 	};
 	
 	String header = ChatColor.WHITE + "BuildChallenge " + ChatColor.DARK_BLUE + "// " + ChatColor.AQUA;
 
 	BuildChallenge plugin;
 	public BAdmin(BuildChallenge instance) {
 		plugin = instance;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		//Check for arguments and display help menu if no arguments are matched
 		if(cmd.getName().equalsIgnoreCase("badmin")) {
 			
 			switch (args.length){
 			//check for args length
 			
 			default: 
 				sender.sendMessage(defaultMessage);
 			break;
 			
 			case 1: case 2: case 3:
 				
 				switch (args[0].toLowerCase()){
 					
 					default:
 						sender.sendMessage(defaultMessage);
 					break;
 					
 					case "help": case "?":
 						
 						if (args.length == 1){
 							sender.sendMessage(defaultMessage);
 						break;
 						}
 						
 					break;
 					
 					case "arenacreate":
 						//walk user through the steps of creating an arena ./badmin arenacreate
 						if(sender.hasPermission("buildchallege.admin")) {
 							//TODO Make this arena shit work
 							
 								//TODO input the amount of cells desired
 							
 								//TODO Recieve input from WorldEdit on size of cell
 							
 								//TODO define buildabale/unbuildable region
 							
 								//TODO define region as a creative region
 							
 								//TODO protect floor and walls from building
 							
 								//TODO Set teleport point in center of cell
 							
 								//TODO Define a lobby area
 							
 								//TODO Name the arena
 								
 						}else {
 							sender.sendMessage(AS(header + "&cYou do not have permission to do this!"));							
 						}
 						break;
 						
 					case "setinitiator":
 						//sets a user as an authorized initiator ./badmin setinitiator <playername>
 						if (sender.hasPermission("buildchallenge.admin")) {
 							List <String> initiatorList = plugin.datacore.getStringList("Initiators");
 							initiatorList.add(args[1]);
 							plugin.datacore.set("Initiators", initiatorList);
 							plugin.saveYamls();
 							sender.sendMessage(AS(header + "&bSuccessfully &aadded &b" + (args[1]) + " to initiator list."));
							if (args.length == 0){
 								sender.sendMessage(AS(header + "&cYou must specify a player name!"));
 							}
 						}else {	
 							sender.sendMessage(AS(header + "&cYou do not have permission to do this!"));
 						}
 						
 					break;
 						
 					case "reminitiator":
 						//remove a user from the authorized initiator list ./badmin reminitiator <playername>
 						if (sender.hasPermission("buildchallenge.admin")) {
 							//Remove a name from initiatorList
 							List <String> initiatorList = plugin.datacore.getStringList("Initiators");
 							initiatorList.remove(args[1]);
 							plugin.datacore.set("Initiators", initiatorList);
 							plugin.saveYamls();
 							sender.sendMessage(AS(header + "Successfuly &cremoved &b " + (args[1]) + "from initiator list."));
							if (args.length == 0){
 								sender.sendMessage(AS(header + "&cYou must specify a player name!"));
 							}
 						}else {
 							sender.sendMessage(AS(header + "&cYou do not have permission to do this!"));
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
 

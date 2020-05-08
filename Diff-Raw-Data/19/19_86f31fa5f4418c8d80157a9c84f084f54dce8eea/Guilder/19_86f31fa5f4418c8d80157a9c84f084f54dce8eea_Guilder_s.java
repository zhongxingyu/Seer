 package org.dazzlewire.Guilder;
 
 // Java imports
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.logging.Logger;
 
 // Bukkit imports
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Guilder extends JavaPlugin implements Listener {
 	
 	// Declare variables
 	public static Guilder plugin;
 	public final Logger logger = Logger.getLogger("Minecraft");
 	private File configFile;
 	private YamlConfiguration config;
 	private GuildController guildController;
 	PluginDescriptionFile pdf;
 	boolean isOnline = false;
 	String listOfGuildMembers = "";
 	/**
 	 * Online players
 	 */
 	Player[] onlinePlayers;
 
 	/**
 	 * Called upon server start, restart or plugin enabeling
 	 * Hehe, this is working
 	 */
 	@Override
 	public void onEnable() {
 		
 		// Load the config.yml
 		loadConfiguration(); 
 		
 		// Make a new guildController
 		guildController = new GuildController(this);
 		
 		// Make sure that Bukkit will thow new event at our listeners
 		this.getServer().getPluginManager().registerEvents(this, this);
 		
 		pdf = this.getDescription();
 		this.logger.info(pdf.getName() + " is now enabled. Running version " + pdf.getVersion());
 		
 		//Currently online players
 		onlinePlayers = Bukkit.getServer().getOnlinePlayers();
 		
 	}
 	
 	/**
 	 * Called upon server stop og plugin disabeling
 	 */
 	@Override
 	public void onDisable() {
 		PluginDescriptionFile pdf = this.getDescription();
 		this.logger.info(pdf.getName() + " is now disabled. Running version " + pdf.getVersion());
 	}
 	
 	/**
 	 * Executed when a command is send by the user
 	 */
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 
 		// Check if the command /g is used
 		if(cmd.getName().equalsIgnoreCase("g")) {
 			
 			// Check if the sender is a player
 			if(sender instanceof Player) {
 				
 				// Check if the player is sending a message (in the arguments)
 				if(args.length > 0) {
 				
 					// Check if the player is in a guild
 					if(guildController.isInGuild(sender.getName())) {
 						
 						// Make the arguments into a message
 						String message = "";
 						
 						for (int i = 0; i < args.length; i++) {
 							message = message + " " + args[i];
 						}
 						
 						// Send the message to the guild
 						guildController.getGuildOfPlayer(sender.getName()).sendMessage(message, sender.getName());
 						
 					} else { // The player is not in a guild
 						sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You are not in a guild");
 					}
 					
 				}
 				
 			}
 			
 		}
 		
 		// Check if the user used "/guilder..."
 		if(cmd.getName().equalsIgnoreCase("guilder")) {
 			
 			// Check if the sender is a player
 			if(sender instanceof Player) {
 				
 				// Check if the user send at lease 1 argument
 				if(args.length > 0) {
 					
 					/*
 					 *  GUILDER RELOAD COMMAND
 					 *  /guilder reload
 					 */
 					
 					if(args[0].equalsIgnoreCase("reload")) {
 						
 						this.onEnable();
 						
 						sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE + "has been reloaded");
 						
 						return true;
 						
 					}
 					
 					/*
 					 *  GUILDER LIST COMMAND
 					 *  /guilder guildlist
 					 */
 					
 					if(args[0].equalsIgnoreCase("guildlist")) {
 						
 						// Desclare new variabels
 						int pageNumber = 0;
 						int linePerPage = 7;
 						int pagesTotal = 1;
 						
 						// Make a new arraylist with commands
 						ArrayList<Guild> guildList = guildController.getSortedGuildList();
 						
 						// Calculate the number of pages total
 						pagesTotal = (int) Math.ceil((double)guildList.size()/(double)linePerPage);
 						
 						// Test if the user want to see a specific page
 						if(args.length == 2) {
 							if(!args[1].equalsIgnoreCase("")) {
 								pageNumber = Integer.parseInt(args[1])-1;
 								
 								// Check if the number is positive
 								if(pageNumber < 0) {
 									pageNumber = 0;
 								}
 								
 								// Check if the users choise is a possibility
 								if(pageNumber > pagesTotal) {
 									pageNumber = 0;
 								}
 							}
 						}
 						
 						// Check if there is enough guilds to fill out a single page, if not: Fix it by removing excess space
 						while(guildList.size() % linePerPage > 0) {
 							// Add empty string to arraylist
 							guildList.add(new Guild(this, "", ""));
 						}
 						
 						// Print the headline
 						sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE + "Here is a list of guilds (Page " + (pageNumber+1) + " of " + pagesTotal + ")");
 						
 						// Print the first page
 						for (int i = (pageNumber * linePerPage); i < (pageNumber * linePerPage) + linePerPage; i++) {
 							// Check if the array is empty
 							if(!guildList.get(i).getGuildName().equals("")) {
 								sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE + guildList.get(i).getGuildName() + " [" + guildList.get(i).getGuildSize() + "]");
 							}
 						}
 						
 						// Return true - the command is complete
 						return true;
 					}
 					
 					/*
 					 *  GUILDER CREATE COMMAND
 					 *  /guilder create <guildname>
 					 */
 					else if(args[0].equalsIgnoreCase("create")) { // "/guilder create"-command
 						
 						if(args.length == 2) { // Check if the sender sends 2 arguments
 						
 							String specifiedGuildName = args[1]; 
 							
 							
 							// Check if the guild exists
 							if(!guildController.guildExists(specifiedGuildName)) {
 								
 								
 								
 								// If the player is not in a guild
 								if(!guildController.ownsGuild(sender.getName())) {
 								
 									// If the player owns a guild
 									if(!guildController.isInGuild(sender.getName())) {
 									
 									// Create the guild
 									guildController.createGuild(specifiedGuildName, sender.getName());
 									
 									sender.sendMessage(ChatColor.GREEN + "[Guilder]" + ChatColor.WHITE + " Congratulations. \"" + specifiedGuildName + "\" has been formed");
 									
 									} else { // The player owns a guild
 										sender.sendMessage(ChatColor.RED + "[Guilder]" + ChatColor.WHITE + " You are already in a guild.");
 									}
 									
 								} else { // If the player is in a guild
 									sender.sendMessage(ChatColor.RED + "[Guilder]" + ChatColor.WHITE + " You already own a guild");
 								}
 								
 							} else { // There is already a guild with that name
 							sender.sendMessage(ChatColor.RED + "[Guilder]" + ChatColor.WHITE + " There is already a guild with the name \"" + args[1] + "\"");
 							}
 							
 						} else if(args.length > 2) {
 							sender.sendMessage(ChatColor.RED + "[Guilder]" + ChatColor.WHITE + " Guildnames must only contain 1 word");
 							sender.sendMessage(ChatColor.RED + "[Guilder]" + ChatColor.WHITE + " /guilder create <guild-name>");
 						} else if(args.length < 2) {
 							sender.sendMessage(ChatColor.RED + "[Guilder]" + ChatColor.WHITE + " Provide a name for your guild");
 							sender.sendMessage(ChatColor.RED + "[Guilder]" + ChatColor.WHITE + " /guilder create <guild-name>");
 						}
 						
 					}
 					
 					/*
 					 *  GUILDER HELP COMMAND
 					 *  /guilder help
 					 */
 					
 					else if(args[0].equalsIgnoreCase("help")) {
 						
 						// Desclare new variabels
 						int pageNumber = 0;
 						int linePerPage = 7;
 						int pagesTotal = 1;
 						
 						// Make a new arraylist with commands
 						ArrayList<String> helpCommands = new ArrayList<String>();
 						
 						// Add values to this arraylist
 						helpCommands.add("/guilder");
 						helpCommands.add("/guilder create <guildname>");
 						helpCommands.add("/guilder help");
 						helpCommands.add("/guilder guildlist");
 						helpCommands.add("/guilder list");
 						helpCommands.add("/guilder online");
 						helpCommands.add("/guilder remove");
 						helpCommands.add("/guilder leave");
 						
 						// Order the arraylist alphabetic
 						Collections.sort(helpCommands);
 						
 						// Calculate the number of pages total
 						pagesTotal = (int) Math.ceil((double)helpCommands.size()/(double)linePerPage);
 						
 						// Test if the user want to see a specific page
 						if(args.length == 2) {
 							if(!args[1].equalsIgnoreCase("")) {
 								pageNumber = Integer.parseInt(args[1])-1;
 								
 								// Check if the number is positive
 								if(pageNumber < 0) {
 									pageNumber = 0;
 								}
 								
 								// Check if the users choise is a possibility
 								if(pageNumber > pagesTotal) {
 									pageNumber = 0;
 								}
 							}
 						}
 						
 						// Check if there is enough commands to fill out a single page, if not: Fix it by removing excess space
 						while(helpCommands.size() % linePerPage > 0) {
 							// Add empty string to arraylist
 							helpCommands.add("");
 						}
 						
 						// Print the headline
 						sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE + "Here is a list of commands for you (Page " + (pageNumber+1) + " of " + pagesTotal + ")");
 						
 						// Print the first page
 						for (int i = (pageNumber * linePerPage); i < (pageNumber * linePerPage) + linePerPage; i++) {
 							// Check if the array is empty
 							if(!helpCommands.get(i).equals("")) {
 								sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE + helpCommands.get(i));
 							}
 						}
 						
 						// Return true - the command is complete
 						return true;
 					}
 				/*
 				 *  GUILDER INVITE COMMAND
 				 *  /guilder invite <player-name>
 				 */
 					else if(args[0].equalsIgnoreCase("invite")) {
 						
 						if(args.length == 2) {
 							
 								//Checks if the sender is in a guild or is a guildmaster
 								if(guildController.ownsGuild(sender.getName()) || guildController.isInGuild(sender.getName())) {
 									
 									//Checks if the reciever of the invite is not in a guild
 									if(!guildController.isInGuild(args[1].toLowerCase())) {
 										
 										isOnline = false;
 										
 										onlinePlayers = Bukkit.getServer().getOnlinePlayers();
 										
 										//Run through all online players
 										for(int i = 0; i < onlinePlayers.length; i++) {
 											
 											//Check if the specific player is online
 											if(onlinePlayers[i].getName().equalsIgnoreCase(args[1])) {
 												
 												guildController.setInvite(onlinePlayers[i].getName(), guildController.getGuildOfPlayer(sender.getName()));
 												
 												//Sends information to both sender and reciever
 												sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE + "You have invited " + onlinePlayers[i].getName() + " to join your guild. He will have to accept your invite using /guilder acceptinvite.");
 												onlinePlayers[i].sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE + "You have been invited to join " + guildController.getGuildOfPlayer(sender.getName()).getGuildName() + " by " + sender.getName() +  ". Accept your invite using /guilder acceptinvite.");
 												isOnline = true;
 												
 											} 
 										}
 										
 										if(!isOnline) {
 											sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE + args[1].toLowerCase() + " is not online.");
 										}
 										
 									} else {
 										sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "A player with that name is already in a guild");
 									}
 								} else {
 									sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You are a not in a guild");
 								}
 							
 							
 								
 						} else if(args.length > 2) { //Check if the sender provide too many arguments
 							sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "A player with that name does not exist");
 							sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "/guilder invite <player-name>");
 							
 						} else if(args.length == 1) { //Check if the sender do not provides enough arguments
 							sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You need to provide a player-name");
 							sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "/guilder invite <player-name>");
 						}
 					/**
 					 * THE /guilder accept - command
 					 */
 					} else if(args[0].equalsIgnoreCase("acceptinvite")){
 						
 						//Check if the player is invites to a guild
 						if(guildController.getPendingPlayerGuild().containsKey(sender.getName())) {
 							
 							//Check if the player has been invited to a guild in the last 20 minutes
 							if(System.currentTimeMillis() - guildController.getPendingPlayerTime().get(sender.getName()) <= 1200000) {
 								
 								//Adds the member to the specific guild
 								guildController.getPendingPlayerGuild().get(sender.getName()).addMember(sender.getName());
 								sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE + "Congratulations you have joined " + guildController.getPendingPlayerGuild().get(sender.getName()).getGuildName() + ".");	
 								
 							}
 							
 						}
 						
 					}
 					
 					else if(args[0].equalsIgnoreCase("online")) {
 						
 						//Check if the sender is in a guild
 						if(guildController.isInGuild(sender.getName())){
 							
 							onlinePlayers = Bukkit.getOnlinePlayers();
 							listOfGuildMembers = "";
 							
 							//Runs through all players in the guild of the sender
 							for(int i = 0; i < guildController.getGuildOfPlayer(sender.getName()).getGuildSize(); i++){
 								
 								for(int j = 0; j < onlinePlayers.length; j++){
 									
 									if(onlinePlayers[j].getName().equalsIgnoreCase(guildController.getGuildOfPlayer(sender.getName()).getMemberArray()[i].toString())){
 										
 										listOfGuildMembers = listOfGuildMembers + ", " + onlinePlayers[j].getName() ;
 										
 									}
 									
 								}
 								
 							}
 							
 							sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE + "Here is a list of online players in your guild:");
 							sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE +  listOfGuildMembers.replaceFirst(", ", ""));
 							
 							
 						} else {
 							
 							sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You are not in a guild");
 							
 						}
 						
 					}
 					
 					else if(args[0].equalsIgnoreCase("list")){
 						
 						
 						//Check if the sender is in a guild
 						if(guildController.isInGuild(sender.getName())){
 							
 							listOfGuildMembers = "";
 							
 							//Runs through all players in the guild of the sender
 							for(int i = 0; i < guildController.getGuildOfPlayer(sender.getName()).getGuildSize(); i++){
 								
 								listOfGuildMembers = listOfGuildMembers + ", " + guildController.getGuildOfPlayer(sender.getName()).getMemberArray()[i] ;
 								
 							}
 							
 							sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE + "Here is a list of all players in your guild:");
 							sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE +  listOfGuildMembers.replaceFirst(", ", ""));
 							
 							
 						} else {
 							
 							sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You are not in a guild");
 							
 						}
 						
 					}
 					
 					else if(args[0].equalsIgnoreCase("leave")){
 						
 						if(guildController.isInGuild(sender.getName())){
 							
 							if(!guildController.ownsGuild(sender.getName())){
 
 								guildController.setLeave(sender.getName(), guildController.getGuildOfPlayer(sender.getName()));
 								sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE + "Are you sure you want to leave " + guildController.getGuildOfPlayer(sender.getName()).getGuildName() + "? Type /guilder acceptleave to confirm your action.");
 							
 							}
 							
 							else {
 								sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You cannot leave a guild that you own");
 							}
 							
 						} else {
 							
 							sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You are not in a guild");
 							
 						}
 						
 					}
 					
 					else if(args[0].equalsIgnoreCase("acceptleave")){
 						
 						//Check if the player has tried to leave a guild
 						if(guildController.getLeavingPlayerGuild().containsKey(sender.getName())) {
 							
 							//Check if the player has asked to leave to a guild in the last 20 minutes
 							if(System.currentTimeMillis() - guildController.getLeavingPlayerTime().get(sender.getName()) <= 180000) {
 								
 								//Removes the member to the specific guild
 								guildController.getLeavingPlayerGuild().get(sender.getName()).removeMember(sender.getName());
 								sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE + "You have succesfully left " + guildController.getPendingPlayerGuild().get(sender.getName()).getGuildName() + ".");	
 								
 							}
 							
 						}
 						
 					}
 					
					/*else if(args[0].equalsIgnoreCase("remove")){
 						
 						if(guildController.isInGuild(sender.getName())){
 							
							if(guildController.getGuildOfPlayer(sender.getName()).getGuildMaster().equalsIgnoreCase(sender.getName())){
 								
 								if(guildController.getGuildOfPlayer(sender.getName()).equals(guildController.getGuildOfPlayer(args[1]))){
 									
 									guildController.getGuildOfPlayer(sender.getName()).removeMember(args[1]);
 									
 								} else {
 									sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "That player is not in your guild");
 								}
 								
 							} else {
 								sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You are not guildmaster of this guild" );
 							}
 							
 						} else {
 							sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You are not in a guild" );
 						}
 						
 
 						
					}*/
 					
 					
 				
 				}
 			
 			} 
 			
 		}
 		// No right commands were found. Return false.
 		return false; 
 	}
 	
 	/**
 	 * Add default values to the config file
 	 */
 	private void loadConfiguration() {
 		configFile = new File("plugins" + File.separator + this.getDescription().getName() + File.separator + "config.yml"); //config file
 		
 		// Check if the config file exists
 		if(!configFile.exists()) {
 			// Create a new config file
 			(new File("plugins" + File.separator + this.getDescription().getName())).mkdirs(); // Make directories
 			
 			config = YamlConfiguration.loadConfiguration(configFile); //get the yml-config from the guildSpecificFile
 			
 			// Add defaults to the config-file
 			config.addDefault("Setting", "Result");
 			config.options().copyDefaults(true);
 			
 	        try {
 	        	config.save(configFile); // Save the file
 			} catch (IOException e) {
 				// Handle exception
 			}
 		}
     }
 	
 }

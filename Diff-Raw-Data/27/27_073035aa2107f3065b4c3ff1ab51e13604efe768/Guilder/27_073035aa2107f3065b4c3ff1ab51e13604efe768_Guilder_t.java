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
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
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
 	 * On player join
 	 */
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		
 		// Check if the player is in a guild
 		if(guildController.isInGuild(event.getPlayer().getName())) {
 		
 			event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "<" +  guildController.getGuildOfPlayer((event.getPlayer().getName())).getGuildName() + "> " + guildController.getGuildOfPlayer((event.getPlayer().getName())).getGuildMessage());
 			guildController.getGuildOfPlayer(event.getPlayer().getName()).sendMessage(event.getPlayer().getName()+" has come online", "");
 			Bukkit.getLogger().info("det virker");
 			
 			// Will remove the original join message
 			// TODO: Add a config option weather you want to disable or enable this feature
 			// event.setJoinMessage("");
 			
 		}
 		
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
 				
 				/**
 				 * The /guilder command
 				 * Will snow information about the Guilder mod.
 				 */
 				if(args.length == 0) {
 					sender.sendMessage(ChatColor.GREEN + "[Guilder]" + ChatColor.WHITE + " will add guild functionality to your Bukkit servers.");
 					sender.sendMessage("If you want a list of the commands used in the Guilder mod, you should type /guilder help");
 				}
 				
 				// Check if the user send at lease 1 argument
 				else if(args.length > 0) {
 
 					/**
 					 * The /guilder reload command
 					 * Reload the guild 
 					 */
 					if(args[0].equalsIgnoreCase("reload")) {
 						
 						this.onEnable();
 						sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE + "has been reloaded");
 						return true;
 						
 					}
 					
 					/**
 					 * The /guilder guildlist command
 					 * Lists all the guilds in the plugin-folder
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
 					
 					/**
 					 * The /guilder create 
 					 * Will create a guild with the specified name
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
 									
 									return true;
 									
 									} else { // The player owns a guild
 										sender.sendMessage(ChatColor.RED + "[Guilder]" + ChatColor.WHITE + " You are already in a guild.");
 										return false;
 									}
 									
 								} else { // If the player is in a guild
 									sender.sendMessage(ChatColor.RED + "[Guilder]" + ChatColor.WHITE + " You already own a guild");
 									return false;
 								}
 								
 							} else { // There is already a guild with that name
 								sender.sendMessage(ChatColor.RED + "[Guilder]" + ChatColor.WHITE + " There is already a guild with the name \"" + args[1] + "\"");
 								return false;
 							}
 							
 						} else if(args.length > 2) {
 							sender.sendMessage(ChatColor.RED + "[Guilder]" + ChatColor.WHITE + " Guildnames must only contain 1 word");
 							sender.sendMessage(ChatColor.RED + "[Guilder]" + ChatColor.WHITE + " /guilder create <guild-name>");
 							return false;
 						} else if(args.length < 2) {
 							sender.sendMessage(ChatColor.RED + "[Guilder]" + ChatColor.WHITE + " Provide a name for your guild");
 							sender.sendMessage(ChatColor.RED + "[Guilder]" + ChatColor.WHITE + " /guilder create <guild-name>");
 							return false;
 						}
 						
 					}
 					
 					/**
 					 * The /guilder help command
 					 * Will display all the commands
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
 
 					/**
 					 * The /guilder invite command
 					 * Invites a player to your guild
 					 */
 					else if(args[0].equalsIgnoreCase("invite")) {
 						
 						if(args.length == 2) {
 							
								// Checks if the sender is in a guild or is a guildmaster
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
 											
 											// Return when the loop is complete
 											if(i == onlinePlayers.length) {
 												return true;
 											}
 											
 										}
 										
 										if(!isOnline) {
 											sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE + args[1].toLowerCase() + " is not online.");
 											return false;
 										}
 										
 									} else {
 										sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "A player with that name is already in a guild");
 										return false;
 									}
 								} else {
 									sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You are a not in a guild");
 									return false;
 								}
 							
 							
 								
 						} else if(args.length > 2) { //Check if the sender provide too many arguments
 							sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "A player with that name does not exist");
 							sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "/guilder invite <player-name>");
 							return false;
 						} else if(args.length == 1) { //Check if the sender do not provides enough arguments
 							sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You need to provide a player-name");
 							sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "/guilder invite <player-name>");
 							return false;
 						}
 						
 					/**
 					 * The /guilder accept - command
 					 */
 					} else if(args[0].equalsIgnoreCase("acceptinvite")){
 						
 						//Check if the player is invites to a guild
 						if(guildController.getPendingPlayerGuild().containsKey(sender.getName())) {
 							
 							//Check if the player has been invited to a guild in the last 20 minutes
 							if(System.currentTimeMillis() - guildController.getPendingPlayerTime().get(sender.getName()) <= 1200000) {
 								
 								//Adds the member to the specific guild
 								guildController.getPendingPlayerGuild().get(sender.getName()).addMember(sender.getName());
 								sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE + "Congratulations you have joined " + guildController.getPendingPlayerGuild().get(sender.getName()).getGuildName() + ".");	
 								
 								return true;
 								
 							}
 							
 							return false;
 							
 						}
 						
 						return false;
 						
 					}
 					
 					/**
 					 * The /guilder online command
 					 * Shows the players online in the commandsenders guild
 					 */
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
 								
 								// Break our of the command-statement when the loop is done
 								if(i == guildController.getGuildOfPlayer(sender.getName()).getGuildSize()) {
 									return true;
 								}
 								
 							}
 							
 							sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE + "Here is a list of online players in your guild:");
 							sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE +  listOfGuildMembers.replaceFirst(", ", ""));
 							
 							
 						} else {
 							
 							sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You are not in a guild");
 							
 							return false;
 							
 						}
 						
 					}
 					
 					/**
 					 * The /guilder list command
 					 * Lists all the commandsenders guildmates 
 					 */
 					else if(args[0].equalsIgnoreCase("list")){
 						
 						
 						//Check if the sender is in a guild
 						if(guildController.isInGuild(sender.getName())){
 							
 							// Create an empty string
 							listOfGuildMembers = "";
 							
 							//Runs through all players in the guild of the sender
 							for(int i = 0; i < guildController.getGuildOfPlayer(sender.getName()).getGuildSize(); i++){
 								listOfGuildMembers = listOfGuildMembers + ", " + guildController.getGuildOfPlayer(sender.getName()).getMemberArray()[i] ;
 							}
 							
 							sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE + "Here is a list of all players in your guild:");
 							sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE +  listOfGuildMembers.replaceFirst(", ", ""));
 							
 							return true;
 							
 						} else {
 							sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You are not in a guild");
 							return false;
 						}
 						
 					}
 					
 					/**
 					 * The /guilder leave command
 					 * Makes the commandsender leave his or hers guild
 					 */
 					else if(args[0].equalsIgnoreCase("leave")){
 						
 						if(guildController.isInGuild(sender.getName())){
 							
 							if(!guildController.ownsGuild(sender.getName())){
 
 								guildController.setLeave(sender.getName(), guildController.getGuildOfPlayer(sender.getName()));
 								sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE + "Are you sure you want to leave " + guildController.getGuildOfPlayer(sender.getName()).getGuildName() + "? Type /guilder acceptleave to confirm your action.");
 							
 								return true;
 								
 							}
 							
 							else {
 								sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You cannot leave a guild that you own");
 								return false;
 							}
 							
 						} else {
 							
 							sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You are not in a guild");
 							return false;
 							
 						}
 						
 					}
 					
 					/**
 					 * The /guilder acceptleave command
 					 * Removes the player from his or hers guild, if there is a pending leaving-request
 					 */
 					else if(args[0].equalsIgnoreCase("acceptleave")){
 						
 						//Check if the player has tried to leave a guild
 						if(guildController.getLeavingPlayerGuild().containsKey(sender.getName())) {
 							
 							//Check if the player has asked to leave to a guild in the last 2 minutes
 							if(System.currentTimeMillis() - guildController.getLeavingPlayerTime().get(sender.getName()) <= 120000) {
 								
 								//Removes the member to the specific guild
 								guildController.getLeavingPlayerGuild().get(sender.getName()).removeMember(sender.getName());
 								sender.sendMessage(ChatColor.GREEN + "[Guilder] " + ChatColor.WHITE + "You have succesfully left " + guildController.getPendingPlayerGuild().get(sender.getName()).getGuildName() + ".");
 								
 								return true;
 							
 							// The player do not have a pending leave-request
 							} else {
 								sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You do not have a pending leave-request. Type /guilder leave");
 							}
 						
 						// The player is not in a guild
 						} else {
 							sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You are not in a guild");
 						}
 						
 					}
 					
 					/**
 					 * The /guilder remove command
 					 * Removes a player from the guildmasters guild
 					 */
 					else if(args[0].equalsIgnoreCase("remove")){
 						
 						// Check if a player is specified
 						if(!args[1].equals("")) {
 						
 							// Check if the commandsender is in a guild
 							if(guildController.isInGuild(sender.getName())) {
 								
 								// Check if the commandsender is the guildmaster of the guild
 								if(guildController.ownsGuild(sender.getName())) {
 									
 									// Check if the provided player is in the guild
 									if(guildController.isInGuild(args[1])) {
 									
 										// Check if the provided player and the guildmaster is in the same guild
 										if(guildController.getGuildOfPlayer(sender.getName()).equals(guildController.getGuildOfPlayer(args[1]))) {
 											
 											// Remove the player
 											guildController.getGuildOfPlayer(sender.getName()).removeMember(args[1]);
 											return true;
 											
 										}
 										
 									// The provided player is not in the same guild as the guildmaster guild
 									} else {
 										sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + args[1]+" is not in your guild");
 										return false;
 									}
 									
 								// The sender is not the guildmaster	
 								} else {
 									sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You are not a guildmaster");
 									return false;
 								}
 							
 							// The sender is not in a guild
 							} else {
 								sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + args[1]+" is not in a guild");
 								return false;
 							}
 						
 						// No player is specified
 						} else {
 							sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You need to specify a targeted player. Use /guilder remove <player> to remove \"player\" from your guild");
 							return false;
 						}
 					
 					}
 						
 					/**
 					 * The /guilder set-command
 					 * Only accesable for the guildmaster
 					 */
 					else if(args[0].equalsIgnoreCase("set")) {
 						
 						// Check if player is in a guild
 						if(guildController.isInGuild(sender.getName())) {
 							
 							// Check if the player is the guildmaster of a guild
 							if(guildController.ownsGuild(sender.getName())) {
 								
 								// Check if there is enough arguments
 								if(args.length >= 3 && args[1].equalsIgnoreCase("guildmessage")) {
 									// Create an empty message
 									String guildMessage = "";
 									
 									// Combine the rest of the arguments
 									for(int j = 2; j < args.length; j++) { // Start with j=2, because of the 2 first arguments already being used.
 										guildMessage = guildMessage + " " + args[j];
 									}
 									
 									guildMessage = guildMessage.replaceFirst(" ", "");
 									
 									// Set the guild message
 									guildController.getGuildOfPlayer(sender.getName()).updateGuildMessage(guildMessage);
 									guildController.getGuildOfPlayer(sender.getName()).sendMessage("The guildmessage have been changed:", "");
 									guildController.getGuildOfPlayer(sender.getName()).sendMessage(guildMessage, "");
 									
 									return true;
 									
 								} else if(args.length >= 3 && args[1].equalsIgnoreCase("guildname")) {
 									// TODO: Implement me
 								}
 								
 								// Not enough arguments from the user
 								else {
 									sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You need to use one of the following commands:");
 									sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "/guilder set guildname <guildname>");
 									sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "/guilder set guildmessage <guildmessage>");
 									return false;
 								}
 								
 								
 							// The commandsender is not the guildmaster
 							} else {
 								sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You are not the guildmaster of this guild");
 								return false;
 							}
 							
 						// The commandsender is not in any guild
 						} else {
 							sender.sendMessage(ChatColor.RED + "[Guilder] " + ChatColor.WHITE + "You are not in a guild");
 							return false;
 						}
 						
 					}
 				
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

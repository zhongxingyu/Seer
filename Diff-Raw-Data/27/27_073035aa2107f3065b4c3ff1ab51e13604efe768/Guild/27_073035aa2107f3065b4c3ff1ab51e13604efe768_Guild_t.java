 package org.dazzlewire.Guilder;
 
 // Java imports
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
import java.util.List;
 
 // Bukkit imports
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 public class Guild {
 
 	/**
 	 * Name of the guild
 	 */
 	private String guildName;
 	
 	/**
 	 * Plugin. (Guilder-object)
 	 */
 	private Plugin p;
 	
 	/**
 	 * File-object. The guild-specific file
 	 */
 	private File guildSpecificFile;
 	
 	/**
 	 * The configuration loaded from guildSpecificFile
 	 */
 	private YamlConfiguration guildSpecific;
 	
 	private Object[] memberArray;
 	
 	/**
 	 * List all playernames and their ranks in a guild
 	 */
 	private HashMap<String, Rank> rankList = new HashMap<String, Rank>();
 
 	private String guildMaster;
 	
 	private String guildMessage;
 	
 	/**
 	 * An array with online players
 	 */
 	Player[] players;
 	
 	/**
 	 * Constructor for Guildclass
 	 * @param p Plugin
 	 * @param guildName name of the guild
 	 */
 	public Guild(Plugin p, String guildName, String guildMaster) {
 		
 		// TODO Check if the guildname and guildmaster valid arguments
 		
 		this.p = p;
 		
 		this.guildName = guildName;
 		this.guildMaster = guildMaster;
 		
 		guildSpecificFile = new File("plugins" + File.separator + p.getDescription().getName() + File.separator + "guilds" + File.separator + guildName.toLowerCase() + ".yml");
 		
 		// Check if the guild-specefic file contains critical variables
 		checkFiles();
 		
 		// Set the member list
 		setMemberList();
 		
 		// Set the member list
 		setGuildMaster();
 		
 		// Set the message of the day
 		setGuildMessage();
 		
 	}
 
 	// Checks if the needed filex excists
 	private void checkFiles() {
 		
 		if (!guildSpecificFile.exists()) {
 			(new File("plugins" + File.separator + p.getDescription().getName())).mkdirs(); // Make directories
 			
 			guildSpecific = YamlConfiguration.loadConfiguration(guildSpecificFile); //get the yml-config from the guildFile
 
 			try {
 				guildSpecific.save(guildSpecificFile);
 			} catch (IOException e) {
 				// Handle exception
 			}
         	
         }
 		
 		guildSpecific = YamlConfiguration.loadConfiguration(guildSpecificFile); //get the yml-config from the guildSpecificFile
 		
 		saveGuild();
 		
 		try {
         	guildSpecific.save(guildSpecificFile); // Save the file
 		} catch (IOException e) {
 			// Handle exception
 		}
 		
 	}
 	
 	/**
 	 * Saves all progress made in the guild
 	 * Fixes any problems there might be in the guild-specific file
 	 */
 	private void saveGuild() {
 		
 		// Try to add "Guildmessage"
 		try {
 			if(!guildSpecific.get("Guildmessage").equals("")) {
 				// Add defaults
 				guildSpecific.addDefault("Guildmessage", guildMessage);
 				guildSpecific.options().copyDefaults(true);
 				
 				// Save the file
 				try {
 					guildSpecific.save(guildSpecificFile);
 				} catch (IOException ioe) {
 					// Handle exception
 				}
 				
 				// Reload the config
 				guildSpecific = YamlConfiguration.loadConfiguration(guildSpecificFile);
 			}
 		} catch(NullPointerException npe) {
 			// Add defaults
 			guildSpecific.addDefault("Guildmessage", "Welcome to the guild "+this.guildName);
 			guildSpecific.options().copyDefaults(true);
 			
 			// Save the file
 			try {
 				guildSpecific.save(guildSpecificFile);
 			} catch (IOException ioe) {
 				// Handle exception
 			}
 			
 			// Reload the config
 			guildSpecific = YamlConfiguration.loadConfiguration(guildSpecificFile);
 		}
 		
 		// Try to add "Guildmaster"
 		try {
 			if(!guildSpecific.get("Guildmaster").equals("") && !guildSpecific.get("Guildmaster").equals("AddAdminNameHere")) {
 				// Add defaults
 				guildSpecific.addDefault("Guildmaster", guildMaster);
 				guildSpecific.options().copyDefaults(true);
 				
 				// Save the file
 				try {
 					guildSpecific.save(guildSpecificFile);
 				} catch (IOException ioe) {
 					// Handle exception
 				}
 				
 				// Reload the config
 				guildSpecific = YamlConfiguration.loadConfiguration(guildSpecificFile);
 			}
 		} catch(NullPointerException npe) {
 			// Add defaults
 			guildSpecific.addDefault("Guildmaster", guildMaster);
 			guildSpecific.options().copyDefaults(true);
 			
 			// Save the file
 			try {
 				guildSpecific.save(guildSpecificFile);
 			} catch (IOException ioe) {
 				// Handle exception
 			}
 			
 			// Reload the config
 			guildSpecific = YamlConfiguration.loadConfiguration(guildSpecificFile);
 		}
 		
 		// Try to add "Name"
 		try {
 			if(guildSpecific.get("Name").equals("")) {
 				// Add defaults
 				guildSpecific.addDefault("Name", guildName);
 				guildSpecific.options().copyDefaults(true);
 				
 				// Save the file
 				try {
 					guildSpecific.save(guildSpecificFile);
 				} catch (IOException ioe) {
 					// Handle exception
 				}
 				
 				// Reload the config
 				guildSpecific = YamlConfiguration.loadConfiguration(guildSpecificFile);
 			}
 		} catch(NullPointerException npe) {
 			// Add defaults
 			guildSpecific.addDefault("Name", guildName);
 			guildSpecific.options().copyDefaults(true);
 			
 			// Save the file
 			try {
 				guildSpecific.save(guildSpecificFile);
 			} catch (IOException ioe) {
 				// Handle exception
 			}
 			
 			// Reload the config
 			guildSpecific = YamlConfiguration.loadConfiguration(guildSpecificFile);
 			
 		}
 		
 		// Try to load the "Members"-list in the file
 		try {
 			guildSpecific.getList("Members").toArray(); // Make a tmp array with the guilds loaded from the file
 		} catch(NullPointerException npe) {
 			
 			// Add defaults
 			Object[] tmpMembersArray = {"AdminNameGoesHere"};
 			
 			if(!guildMaster.equals("")) {
 				tmpMembersArray[0] = guildMaster;
 			}
 			guildSpecific.addDefault("Members", Arrays.asList(tmpMembersArray));
 			guildSpecific.options().copyDefaults(true);
 			
 			// Save the file
 			try {
 				guildSpecific.save(guildSpecificFile);
 			} catch (IOException ioe) {
 				// Handle exception
 			}
 			
 			// Reload the config
 			guildSpecific = YamlConfiguration.loadConfiguration(guildSpecificFile);
 			
 		}
 		
 		// Add the guild master to the members list
 		try {
 			if(!Arrays.asList(guildSpecific.getList("Members").toArray()).contains(guildSpecific.get("Guildmaster"))) {
 				
 				// Add the guildmaster
 				guildSpecific.getList("Members").add(guildSpecific.get("Guildmaster"));
 				
 				// Save the file
 				try {
 					guildSpecific.save(guildSpecificFile);
 				} catch (IOException ioe) {
 					// Handle exception
 				}
 				
 				// Reload the config
 				guildSpecific = YamlConfiguration.loadConfiguration(guildSpecificFile);
 				
 			}
 		} catch(NullPointerException npe) {
 			// Handle exception
 		}
 		
 	}
 	
 	public void addMember(String playerName) {
 		try {
 			guildSpecific.getList("Members").add(playerName);
 			try {
 				guildSpecific.save(guildSpecificFile);
 			} catch (IOException ioe) {
 				checkFiles(); // Check if the file exists
 			}
 			
 			//Updates the array containing members
 			setMemberList();
 			
 			// Reload the config
 			saveGuild();
 			
 			//Tells the guild that a new member have joined
 			this.sendMessage(ChatColor.WHITE + playerName + " has joined the guild." , "");
 			
 			//Tells the server admin that a player have joined a guild
 			Bukkit.getLogger().info(playerName + "has been added to" + getGuildName());
 			
 		} catch(NullPointerException npe)  {
 			saveGuild();
 			addMember(playerName);
 		}
 	}
 	
 	public void removeMember(String playerName) {
 		
 		try {
 			
			// Make a temp list with all the members in it
			List<Object> tmp = guildSpecific.getList("Members");
			
			// Run through all the players, checking if the player should be removed
			for(int i = 0; i < tmp.size(); i++) {
				
				if(((String)tmp.get(i)).equalsIgnoreCase(playerName)) {
					tmp.remove(tmp.get(i));
				}
				
			}
			
			// Remove the player from the guild object
			memberArray = tmp.toArray();
 			
 			try {
 				guildSpecific.save(guildSpecificFile);
 			} catch (IOException ioe) {
 				checkFiles(); // Check if the file exists
 			}
 			
			// Updates the array containing members
 			setMemberList();
 			
 			//Tells the guild that a new member have joined
 			this.sendMessage(ChatColor.WHITE + playerName + " has left the guild." , "");
 			
 			//Tells the server admin that a player have joined a guild
 			Bukkit.getLogger().info(playerName + " has left " + getGuildName());
 			
 		} catch(NullPointerException npe)  {
 			saveGuild();
 			removeMember(playerName);
 		}
 	}
 
 	
 	/**
 	 * Send a message to the members of the guild
 	 * @param message The message the entire guild should recieve
 	 * @param playername The playername which the message should be send from. Leave blank if you want the guild to "announce"
 	 */
 	public void sendMessage(String message, String playerName) {
 		
 		// Run though all online players and chech who is in the guild
 		players = Bukkit.getServer().getOnlinePlayers();
 		
 		// Format the message
 		message = message.trim().replaceAll(" +", " ");
 		
 		for (int i = 0; i < players.length; i++) {
 			
 			// Check if the player is in the guild
 			if(Arrays.asList(memberArray).contains(players[i].getName())) {
 				
 				// Check if name of the sender is provided
 				if(playerName.equalsIgnoreCase("")) {
 					players[i].sendMessage(ChatColor.DARK_GREEN + "<" +  this.getGuildName() + "> " + message);
 				} else { // A player name is provided
 					players[i].sendMessage(ChatColor.DARK_GREEN + "<" +  playerName + "> " + ChatColor.WHITE + message);
 					
 					//Loggs the chat in the server logg
 					Bukkit.getLogger().info("[" + this.getGuildName() + "] " + "<" +  playerName + "> " + message);
 					
 				}
 			}
 			
 		}
 		
 	}
 	
 	/**
 	 * Load the MemberList from the guild specific file
 	 */
 	private void setMemberList() {
 		
 		// Load the memberlist from the file and add them to the arraylist
 		try {
 			memberArray = guildSpecific.getList("Members").toArray(); // Make a tmp array with the guilds loaded from the file
 			saveGuild(); // Save the files
 		} catch (NullPointerException npe) {
 			saveGuild();
 			setMemberList();
 		}
 		
 	}
 	
 	private void setGuildMaster() {
 		
 		// Load the guildmaster from the file and correct the variable
 		try {
 			guildMaster = guildSpecific.get("Guildmaster").toString();
 			saveGuild(); // Save the files
 		} catch (NullPointerException npe) {
 			saveGuild();
 			setGuildMaster();
 		}
 		
 	}
 	
 	private void setGuildMessage() {
 		
 		// Load the guildmaster from the file and correct the variable
 		try {
 			guildMessage = guildSpecific.get("Guildmessage").toString();
 			saveGuild(); // Save the files
 		} catch (NullPointerException npe) {
 			saveGuild();
 			setGuildMessage();
 		}
 		
 	}
 	
 	public void updateGuildMessage(String guildMessage) {
 		
 		this.guildMessage = guildMessage;
 		guildSpecific.set("Guildmessage", guildMessage);
 		saveGuild();
 		
 	}
 	
 	/**
 	 * Set a rank-object to a specific player
 	 * @param playerName which player needs to get his rank modified?
 	 * @param rank A rank object containing all the players permissions
 	 */
 	public void setRank(String playerName, Rank rank){
 		rankList.put(playerName, rank);	
 	}
 	
 	/**
 	 * Gets the name of the guildmaster
 	 * @param guildMaster
 	 */
 	public void setGuildMaster(String guildMaster) {
 		this.guildMaster = guildMaster;
 	}
 	
 	/**
 	 * Get guildname
 	 * @return Name of the guild
 	 */
 	public String getGuildName() {
 		return guildName;
 	}
 	
 	/**
 	 * Get guildMessage
 	 * @return The guildmessage of the day in the guild
 	 */
 	public String getGuildMessage() {
 		return guildMessage;
 	}
 	
 	/**
 	 * Get the name of the guildmaster
 	 * @return name of the guilds guildmaster
 	 */
 	public String getGuildMaster() {
 		return guildMaster;
 	}
 	
 	/**
 	 * Gets ranklist 
 	 * @return A hashmap with players ranks
 	 */
 	public HashMap<String, Rank> getRankList() {
 		return rankList;
 	}
 	
 	/**
 	 * Get the array with members
 	 * @return
 	 */
 	public Object[] getMemberArray() {
 		return memberArray;
 	}
 	
 	/**
 	 * Get the amount of players in the guild
 	 * @return
 	 */
 	public int getGuildSize() {
 		
 		return memberArray.length;
 		
 	}
 
 }
 //

 package org.dazzlewire.Guilder;
 
 // Java imports
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 
 // Bukkit imports
 import org.bukkit.configuration.file.YamlConfiguration;
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
 
 	private String guildMaster = "";
 	
 	/**
 	 * Constructor for Guildclass
 	 * @param p Plugin
 	 * @param guildName name of the guild
 	 */
 	public Guild(Plugin p, String guildName, String guildMaster) {
 		this.p = p;
 		
 		this.guildName = guildName;
 		this.guildMaster = guildMaster;
 		
 		guildSpecificFile = new File("plugins" + File.separator + p.getDescription().getName() + File.separator + "guilds" + File.separator + guildName.toLowerCase() + ".yml");
 		
 		// Check if the guild-specefic file contains critical variables
 		checkFiles();
 		
 		// Set the member list
 		setMemberList();
 		
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
 		
 		fixGuild();
 		
 		try {
         	guildSpecific.save(guildSpecificFile); // Save the file
 		} catch (IOException e) {
 			// Handle exception
 		}
 		
 	}
 	
 	// Fixes any syntax-errors there might be in the file
 	private void fixGuild() {
 		
 		// Try to add "Guildmaster"
 			try {
 				if(guildSpecific.get("Guildmaster").equals("") || guildSpecific.get("Guildmaster").equals("AddAdminNameHere")) {
 					// Add defaults
					guildSpecific.addDefault("Guildmaster", "AddAdminNameHere");
 					guildSpecific.options().copyDefaults(true);
 					
 					// Log
 					p.getServer().getLogger().info("[GUILDER] WARNING: The guild " + guildName + " does not have a guildmaster. Please add one in \"plugins" + File.separator + p.getDescription().getName() + File.separator + "guilds" + File.separator + guildName.toLowerCase() + ".yml\"");
 					
 					// Save the file
 					try {
 						guildSpecific.save(guildSpecificFile);
 					} catch (IOException ioe) {
 						// Handle exception
 					}
 				}
 			} catch(NullPointerException npe) {
 				// Add defaults
				guildSpecific.addDefault("Guildmaster", "AddAdminNameHere");
 				guildSpecific.options().copyDefaults(true);
 				
 				// Log
 				p.getServer().getLogger().info("[GUILDER] WARNING: The guild " + guildName + " does not have a guildmaster. Please add one in \"plugins" + File.separator + p.getDescription().getName() + File.separator + "guilds" + File.separator + guildName.toLowerCase() + ".yml\"");
 				
 				// Save the file
 				try {
 					guildSpecific.save(guildSpecificFile);
 				} catch (IOException ioe) {
 					// Handle exception
 				}
 			}
 		
 		// Try to add "Name"
 		try {
 			if(guildSpecific.get("Name").equals("")) {
 				// Add defaults
 				guildSpecific.addDefault("Name", guildName);
 				guildSpecific.options().copyDefaults(true);
 				
 				// Log
 				p.getServer().getLogger().info("Fixed the guildspecific-file for " + guildName + ".Guildname non existing.");
 				
 				// Save the file
 				try {
 					guildSpecific.save(guildSpecificFile);
 				} catch (IOException ioe) {
 					// Handle exception
 				}
 			}
 		} catch(NullPointerException npe) {
 			// Add defaults
 			guildSpecific.addDefault("Name", guildName);
 			guildSpecific.options().copyDefaults(true);
 			
 			// Log
 			p.getServer().getLogger().info("Fixed the guildspecific-file for " + guildName + ". Guildname non existing.");
 			
 			// Save the file
 			try {
 				guildSpecific.save(guildSpecificFile);
 			} catch (IOException ioe) {
 				// Handle exception
 			}
 		}
 		
 		// Try to load the "Members"-list in the file
 		try {
 			guildSpecific.getList("Members").toArray(); // Make a tmp array with the guilds loaded from the file
 		} catch (NullPointerException npe) {
 			// Add defaults
 			String[] anArray = {""};
 			guildSpecific.addDefault("Members", Arrays.asList(anArray));
 			guildSpecific.options().copyDefaults(true);
 			
 			// Log
 			p.getServer().getLogger().info("Fixed the guildspecific-file for " + guildName + ". Incorrect formatting. Members-list non existing.");
 			
 			// Save the file
 			try {
 				guildSpecific.save(guildSpecificFile);
 			} catch (IOException ioe) {
 				// Handle exception
 			}
 		}
 		
 	}
 	
 	/**
 	 * Sets the name of the guild
 	 * @param name What should the guild be called?
 	 */
 	public void setGuildName(String name) {
 		// Make sure that there is no spaces in the name
 		this.guildName = name.replace(" ", "_");
 	}
 	
 	/**
 	 * Load the MemberList from the guild specific file
 	 */
 	private void setMemberList() {
 		
 		// Load the memberlist from the file and add them to the arraylist
 		try {
 			memberArray = guildSpecific.getList("Members").toArray(); // Make a tmp array with the guilds loaded from the file
 		} catch (NullPointerException npe) {
 			fixGuild();
 			setMemberList();
 		}
 		
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
 
 }
 //

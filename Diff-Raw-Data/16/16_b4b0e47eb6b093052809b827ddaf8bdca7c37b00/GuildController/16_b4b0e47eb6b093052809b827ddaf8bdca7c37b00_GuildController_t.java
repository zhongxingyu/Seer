 package org.dazzlewire.Guilder;
 
 // Java import
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 
 // Bukkit import
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 
 public class GuildController {
 
 	private Plugin p;
 	private File guildFile;
 	private YamlConfiguration guilds;
 	private File guildSpecificFile;
 	private YamlConfiguration guildSpecific;
 	private Object[] tmpGuildList = null;
 	
 	/**
 	 * Contain all pending players and what Guild they are pending to.
 	 */
 	private HashMap<String,Guild> pendingPlayerGuild = new HashMap<String,Guild>();
 	
 	/**
 	 * Contain all pending players and when they started pending.
 	 */
 	private HashMap<String, Long> pendingPlayerTime = new HashMap<String, Long>();
 	
 	/**
 	 * Contains all guilds
 	 */
 	private ArrayList<Guild> guildList = new ArrayList<Guild>();
 	
 	/**
 	 * True if a new config-file has been made
 	 */
 	private boolean newConfigFile = false;
 	
 	/**
 	 * Constructor
 	 */
 	public GuildController(Plugin p) {
 		
 		// Set the plugin
 		this.p = p;
 		
 		guildFile = new File("plugins" + File.separator + p.getDescription().getName() + File.separator + "guilds.yml");
 		
 		loadGuilds(); // Load guilds.yml
 		
 	}
 
 	private void loadGuilds() {
 		
 		// Check if guildFile exists
 		if (!guildFile.exists()) {
 			(new File("plugins" + File.separator + p.getDescription().getName())).mkdirs(); // Make directories
 			
 			guilds = YamlConfiguration.loadConfiguration(guildFile); //get the yml-config from the guildFile
 			
 			// Add defaults to the config-file
 			guilds.addDefault("Guilds", Arrays.asList());
 			guilds.options().copyDefaults(true);
 			
 			try {
 				guilds.save(guildFile);
 			} catch (IOException e) {
 				// Handle exception
 			}
         	
         	newConfigFile = true; // New config created, create a example guild for the admin
         }
 		
 		guilds = YamlConfiguration.loadConfiguration(guildFile); //get the yml-config from the guildFile
 		
 		try {
 			tmpGuildList = guilds.getList("Guilds").toArray(); // Make a tmp array with the guilds loaded from the file
 		} catch (NullPointerException e) {
 			fixGuildList();
 		}
 		
 		tmpGuildList = guilds.getList("Guilds").toArray();
 		
 		// Run though all guilds in the guilds.yml
 		for(int i = 0; i < tmpGuildList.length; i++) {
 			
 			// Add the guild to the config-file if it is not in it
 			if(!guildList.contains(tmpGuildList.toString())) {
 				guildList.add(new Guild(p, tmpGuildList[i].toString(), guilds.getString("Guildmaster")));
 			}
 			
 			// Check if guildSpeceficFile exists
 			if (!new File("plugins" + File.separator + p.getDescription().getName() + File.separator + "guilds" + File.separator + tmpGuildList[i].toString().toLowerCase() + ".yml").exists()) {
 	        	// If the file does no exist, create a new one
 				guildSpecificFile = new File("plugins" + File.separator + p.getDescription().getName() + File.separator + "guilds" + File.separator + tmpGuildList[i].toString().toLowerCase() + ".yml");
 				
 				guildSpecific = YamlConfiguration.loadConfiguration(guildSpecificFile); //get the yml-config from the guildSpecificFile
 
 		        try {
 		        	guildSpecific.save(guildSpecificFile); // Save the file
 				} catch (IOException e) {
 					// Handle exception
 				}
 			}
 			
 		}
 		
 		// Make a new guild if the config-file is new
 		if(newConfigFile == true) {
         	// Add a default guild to show the user how to use guild syntax
        	createGuild("TestGuild", "AdminNameGoesHere");
         	guilds.addDefault("Guilds", Arrays.asList()); // Add "Guilds"-list to guilds.yml
         	
         	guilds.options().copyDefaults(true);
             
             try {
     			guilds.save(guildFile); // Save the file
     		} catch (IOException e) {
     			// Handle exception
     		}
         	
         	newConfigFile = false;
         } 
 
 	}
 
 	// Fixes any syntax-errors there might be in the guild.yml-file
 	private void fixGuildList() {
 		
 		// Try to load the "Guilds"-list in the file
 		try {
 			guilds.getList("Guilds").toArray(); // Make a tmp array with the guilds loaded from the file
 		} catch (NullPointerException npe) {
 			// Add defaults
 			guilds.addDefault("Guilds", Arrays.asList());
 			
 			// Log
 			p.getServer().getLogger().info("Fixed the guilds.yml-file. Incorrect formatting in \"Guilds\"-list");
 			
 			try {
 				guilds.save(guildFile); // Save the file
 			} catch (IOException ioe) {
 				// Handle exception
 			}
 		}
 		
 	}
 
 	/**
 	 * Creates a new guild with yml-files
 	 * @param guildName The name of the guild
 	 * @param guildMasterName The playername of the guildmaster (Creator of the guild)
 	 */
 	public void createGuild(String guildName, String guildMasterName) {
 		
 		// Create a new guild-object
 		Guild g = new Guild(p, guildName, guildMasterName);
 		
 		// Add guild to the arraylist guildList
 		guildList.add(g); 
 		
		// Add the guildmaster
		g.setGuildMaster(guildMasterName);
		
 		// Add the guild to the guilds.yml-file
 		guilds.getList("Guilds").add(g.getGuildName()); // Insert it into guilds.yml
 		try {
 			guilds.save(guildFile); // Save the file
 		} catch (IOException e1) {
 			fixGuildList();
 		}
 		
 	}
 	
 	public ArrayList<Guild> getGuildList() {
 		return guildList;
 	}
 
 	public ArrayList<Guild> getSortedGuildList() {
 		ArrayList<Guild> tmp = guildList;
 		
 		Collections.sort(tmp, new Comparator<Object>(){
 			 
             public int compare(Object o1, Object o2) {
                 Guild p1 = (Guild) o1;
                 Guild p2 = (Guild) o2;
                return p1.getGuildName().compareToIgnoreCase(p2.getGuildName());
             }
  
         });
 		
 		return tmp;
 		
 	}
 	
 	/**
 	 * Checks if there is a guild with the name 
 	 * @param Guildname Name of the guild you want to search for
 	 * @return If there is a guild with the specified name, return true
 	 */
 	public boolean guildExists(String guildName) {
 		
 		for (int i = 0; i < guildList.size(); i++) { // Run trough all the guilds in the arraylist
 			// Check if the name is what we are looking for
 			if(guildList.get(i).getGuildName().equalsIgnoreCase(guildName)) {
 				return true;
 			}
 		}
 		
 		return false;
 		
 	}
 	
 	/**
 	 * Get the guild with the provided name
 	 * @param guildName The name of the guild you want to load
 	 * @return A guild object with the given name
 	 */
 	public Guild getGuild(String guildName) {
 	
 		// Make a HashMap containing all guilds
 		HashMap<String, Guild> tmpHashMap = new HashMap<String, Guild>();
 		
 		// Include all the guilds in the hashmap
 		for (int i = 0; i < guildList.size(); i++) { // Run trough all the guilds in the arraylist
 			// Check if the name is what we are looking for
 			tmpHashMap.put(guildList.get(i).getGuildName(), guildList.get(i));
 		}
 		
 		return tmpHashMap.get(guildName);
 		
 		
 	}
 	//Setters
 	
 	public void setInvite(String playerName, Guild guild) {
 		
 		pendingPlayerGuild.put(playerName, guild);
 		pendingPlayerTime.put(playerName, System.currentTimeMillis());
 		
 	}
 	
 	//Getters
 	
 	//Gets a Map of player-names and the guild they have been invited to
 	public HashMap<String,Guild> getPendingPlayerGuild() {
 		
 		return pendingPlayerGuild;
 		
 	}
 	//Gets a Map of player-names and the time they were invited at
 	public HashMap<String, Long> getPendingPlayerTime() {
 		
 		return pendingPlayerTime;
 	}
 	
 }

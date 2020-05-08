 package com.Endain.Waypoints;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.ListIterator;
 import java.util.Map.Entry;
 import java.util.Scanner;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.anjocaido.groupmanager.GroupManager;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 public class DataManager {
 	private Waypoints plugin;
 	private HashMap<Integer, Point> wPoints;
 	private HashMap<String, Point> allPlayerPoints;
 	private HashMap<Integer, Point> onlinePlayerPoints;
 	private LinkedList<Region> regions;
 	private HashMap<Integer, Region> playersSaved;
 	private HashMap<Integer, Region> playersProtected;
 	private HashMap<Integer, Player> players;
 	private int saveRadius;
 	private int protectionRadius;
 	private int saveInterval;
 	private boolean forceAutosave;
 	private boolean autoGenerate;
 	private boolean deleteMissingPoints;
 	private boolean zeroPoints;
 	private boolean protectPlayers;
 	private boolean useGroupManager;
 	private boolean gmIsWorking;
 	private GroupManager permissions;
 	private Timer autoSaveTimer;
 	
 	//Constructor for DataManager
 	public DataManager(Waypoints plugin) {
 		//Initialize all needed Maps and Lists
 		this.plugin = plugin;
 		this.wPoints = new HashMap<Integer, Point>();
 		this.allPlayerPoints = new HashMap<String, Point>();
 		this.onlinePlayerPoints = new HashMap<Integer, Point>();
 		this.regions = new LinkedList<Region>();
 		this.playersSaved = new HashMap<Integer, Region>();
 		this.playersProtected = new HashMap<Integer, Region>();
 		this.players = new HashMap<Integer, Player>();
 		//Initialize variable in case something goes horribly wrong in load(),
 		//we'll at least have valid variables then...
 		this.zeroPoints = true;
 		this.protectPlayers = true;
 		this.gmIsWorking = false;
 		this.useGroupManager = false;
 		this.permissions = null;
 		this.saveInterval = 0;
 		this.autoSaveTimer = null;
 	}
 	
 	//Load performs required plugin startup operations. Such as reading from
 	//data and config files and loading any needed data. returns true if it
 	//reaches the end of it's body without critical error(s).
 	public boolean load() {
 		if(!writeConfigData()) {
 			plugin.sendConsoleMsg("Unable to create config file!");
 			plugin.sendConsoleMsg("The plugin will be disabled!");
 			return false;
 		}
 		if(!writePointData(true, false, false)) {
 			plugin.sendConsoleMsg("Unable to create waypoint data file!");
 			plugin.sendConsoleMsg("The plugin will be disabled!");
 			return false;
 		}
 		if(!writePlayerData(true, false, false)) {
 			plugin.sendConsoleMsg("Unable to create player point data file!");
 			plugin.sendConsoleMsg("The plugin will be disabled!");
 			return false;
 		}
 		File dataFolder = plugin.getDataFolder();
 		String location = "";
 		
 		if(dataFolder.mkdir()) {
 			plugin.sendConsoleMsg("Config data folder is missing!");
 			plugin.sendConsoleMsg("Creating a new one!");
 		}
 		location = dataFolder.getPath() + "\\";
 		//Time to read in the data and config info from save files:
 		//Read in config settings
 		try {
 			Scanner scan = new Scanner(new File(location + "config.txt"));
 			String in;
 			
 			while(scan.hasNextLine()) {
 				in = scan.nextLine();
 				
 				if(in.startsWith("#"))
 					continue;
 				String[] setting = in.split("=");
 				
 				if(setting[0].equalsIgnoreCase("protection-radius"))
 					this.protectionRadius = Integer.parseInt(setting[1]);
 				else if(setting[0].equalsIgnoreCase("save-radius"))
 					this.saveRadius = Integer.parseInt(setting[1]);
 				else if(setting[0].equalsIgnoreCase("data-save-interval"))
 					this.saveInterval = Integer.parseInt(setting[1]);
 				else if(setting[0].equalsIgnoreCase("force-autosave")) {
 					if(setting[1].equalsIgnoreCase("true"))
 						this.forceAutosave = true;
 					else if(setting[1].equalsIgnoreCase("false"))
 						this.forceAutosave = false;
 					else 
 						throw new Exception();
 				}
 				else if(setting[0].equalsIgnoreCase("auto-generate-points")) {
 					if(setting[1].equalsIgnoreCase("true"))
 						this.autoGenerate = true;
 					else if(setting[1].equalsIgnoreCase("false"))
 						this.autoGenerate = false;
 					else 
 						throw new Exception();
 				}
 				else if(setting[0].equalsIgnoreCase("delete-missing-points")) {
 					if(setting[1].equalsIgnoreCase("true"))
 						this.deleteMissingPoints = true;
 					else if(setting[1].equalsIgnoreCase("false"))
 						this.deleteMissingPoints = false;
 					else 
 						throw new Exception();
 				}
 				else if(setting[0].equalsIgnoreCase("use-group-manager")) {
 					if(setting[1].equalsIgnoreCase("true"))
 						this.useGroupManager = true;
 					else if(setting[1].equalsIgnoreCase("false"))
 						this.useGroupManager = false;
 					else 
 						throw new Exception();
 				}
 				else if(setting[0].equalsIgnoreCase("protect-players")) {
 					if(setting[1].equalsIgnoreCase("true"))
 						this.protectPlayers = true;
 					else if(setting[1].equalsIgnoreCase("false"))
 						this.protectPlayers = false;
 					else 
 						throw new Exception();
 				}
 				else {
 					plugin.sendConsoleMsg("Unknown setting: " + setting[0]);
 					plugin.sendConsoleMsg("(Ignoring it.)");
 					continue;
 				}
 				
 				if(this.protectionRadius < 1)
 					this.protectionRadius = 1;
 				if(this.saveRadius < 1)
 					this.saveRadius = 1;
 			}
 			scan.close();
 			plugin.sendConsoleMsg("Settings parsed successfully!");
 			plugin.sendConsoleMsg("- - - - - - - - - - - - - - - - - - - - - -");
 			plugin.sendConsoleMsg("Protect Players: " + this.protectPlayers);
 			plugin.sendConsoleMsg("Protection radius: " + this.protectionRadius);
 			plugin.sendConsoleMsg("Save Radius: " + this.saveRadius);
 			plugin.sendConsoleMsg("Force Auto Save: " + this.forceAutosave);
 			plugin.sendConsoleMsg("Auto Generate Points: " + "(UNFINISHED)"/*this.autoGenerate*/);
 			plugin.sendConsoleMsg("Delete Missing Points: " + this.deleteMissingPoints);
 			plugin.sendConsoleMsg("Use GroupManager: " + this.useGroupManager);
 			plugin.sendConsoleMsg("Data Save Interval: " + this.saveInterval);
 			plugin.sendConsoleMsg("- - - - - - - - - - - - - - - - - - - - - -");
 		}
 		catch (Exception e) {
 			plugin.sendConsoleMsg("Unable to parse config file!");
 			plugin.sendConsoleMsg("The plugin will be disabled!");
 			return false;
 		}
 		
 		//Read in Waypoint data
 		try {
 			Scanner scan = new Scanner(new File(location + "waypoints.txt"));
 			String in;
 			int id;
 			int x;
 			int y;
 			int z;
 			String world;
 			
 			if(this.deleteMissingPoints) {
 				plugin.sendConsoleMsg("Waypoint data will be verified!");
 				plugin.sendConsoleMsg("(This may take a while)");
 			}
 			
 			while(scan.hasNextLine()) {
 				in = scan.nextLine();
 				
 				if(in.startsWith("#"))
 					continue;
 				String[] wpoint = in.split(":");
 				
 				if(wpoint.length == 5) {
 					id = Integer.parseInt(wpoint[0]);
 					world = wpoint[1];
 					x = Integer.parseInt(wpoint[2]);
 					y = Integer.parseInt(wpoint[3]);
 					z = Integer.parseInt(wpoint[4]);
 					Point p = new Point(id, world, x, y, z);
 					
 					if(this.deleteMissingPoints) {
 						World w = this.plugin.getServer().getWorld(p.getWorld());
 						boolean valid = true;
 						for(int i = -1; i <= 1; i++)
 							for(int j = -1; j <= 1; j++)
 								if(w.getBlockAt(p.getX() + i, p.getY() - 1, p.getZ() + j).getType() != Material.GLOWSTONE)
 									valid = false;
 						
 						for(int i = 0; i < 3; i++)
 							if(w.getBlockAt(p.getX(), p.getY() + i, p.getZ()).getType() != Material.BEDROCK)
 								valid = false;
 						
 						if(valid) {
 							wPoints.put(id, p);
 							regions.add(new Region(x, y, z, this.protectionRadius, this.saveRadius, p));
 						}
 					}
 					else {
 						wPoints.put(id, p);
 						regions.add(new Region(x, y, z, this.protectionRadius, this.saveRadius, p));
 					}
 				}
 				else {
 					plugin.sendConsoleMsg("Invalid waypoint in file: " + in);
 					plugin.sendConsoleMsg("(Ignoring it.)");
 					plugin.sendConsoleMsg("I recommend you delete the entry.");
 				}
 			}
 			
 			scan.close();
 			if(this.deleteMissingPoints)
 				plugin.sendConsoleMsg("Waypoint verification complete!");
 			plugin.sendConsoleMsg("Waypoint data parsed successfully!");
 			plugin.sendConsoleMsg(wPoints.size() + " Waypoints have been loaded!");
 			plugin.sendConsoleMsg("- - - - - - - - - - - - - - - - - - - - - -");
 			
 			if(wPoints.size() > 0)
 				zeroPoints = false;
 		}
 		catch (Exception e) {
 			plugin.sendConsoleMsg("Unable to parse waypoint data file!");
 			plugin.sendConsoleMsg("The plugin will be disabled!");
 			return false;
 		}
 		
 		//Read in Player data
 		try {
 			Scanner scan = new Scanner(new File(location + "playerpoints.txt"));
 			String in;
 			int id;
 			String player;
 			
 			while(scan.hasNextLine()) {
 				in = scan.nextLine();
 				
 				if(in.startsWith("#"))
 					continue;
 				String[] ppoint = in.split(":");
 				
 				if(ppoint.length == 2) {
 					player = ppoint[0];
 					id = Integer.parseInt(ppoint[1]);
 					Point p = wPoints.get(id);
 					if(p != null)
 						allPlayerPoints.put(player, p);
 					else {
 						plugin.sendConsoleMsg("Non-existent point referenced: " + in);
 						plugin.sendConsoleMsg("(Ignoring it.)");
 						plugin.sendConsoleMsg("I recommend you delete the entry.");
 					}
 				}
 				else {
 					plugin.sendConsoleMsg("Invalid player data in file: " + in);
 					plugin.sendConsoleMsg("(Ignoring it.)");
 					plugin.sendConsoleMsg("I recommend you delete the entry.");
 				}
 			}
 			scan.close();
 			plugin.sendConsoleMsg("Player point data parsed successfully!");
 			plugin.sendConsoleMsg(allPlayerPoints.size() + " Player points have been loaded!");
 			plugin.sendConsoleMsg("- - - - - - - - - - - - - - - - - - - - - -");
 		}
 		catch (Exception e) {
 			plugin.sendConsoleMsg("Unable to parse playerpoint data file!");
 			plugin.sendConsoleMsg("The plugin will be disabled!");
 			return false;
 		}
 		//Force a save to commit any verification changes
 		if(this.deleteMissingPoints)
 			trySave(false);
 		//Hook into GroupManager if we need to (and can)
 		if(isUsingGroupManager())
 			enableGroupManager();
 		//Verify valid save interval
 		if(this.saveInterval < 0)
 			this.saveInterval = 0;
 		//If save interval is not 0, start saving at a fixed interval
 		if(this.autoSaveTimer == null && this.saveInterval > 0){
 			this.autoSaveTimer = new Timer();
 			this.autoSaveTimer.scheduleAtFixedRate(new TimerTask() {
 				public void run() {
 					trySave(false);
 					plugin.sendConsoleMsg("All data saved!");
 				}
 			}, 60000, this.saveInterval * 60000);
 		}
 		
 		//Loading of plugin successful!
 		return true;
 	}
 	
 	//Function to see if a player is entering a "save" zone around any Waypoint.
 	//It adds the player and region they are inside of to the list of players
 	//currently inside "save" regions. 
 	public void checkIsSaved(Player p, int x, int z) {
 		Region currentRegion = null;
 		ListIterator<Region> itr = regions.listIterator();
 		
 		while(itr.hasNext()) {
 			currentRegion = itr.next();
 			if(currentRegion.isSaved(x, z)) {
 				playersSaved.put(p.getEntityId(), currentRegion);
 				itr.remove();
 				regions.addFirst(currentRegion);
 				//If Force Autosave is enabled, bind the player now
 				if(this.forceAutosave)
 					wpBind(p, currentRegion);
 				break;
 			}
 		}
 	}
 	
 	//Checks if the player is currently in the "save" region list.
 	//No point in checking if they entered a "save" zone if they are already in one.
 	public Region playerIsSaved(Player p) {
 		return this.playersSaved.get(p.getEntityId());
 	}
 	
 	//Function to see if a player is leaving the "save" zone they were in.
 	//It removes the player and region they are inside of to the list of players
 	//currently inside "save" regions. 
 	public void checkIsUnsaved(Player p, Region r, int x, int z) {
 		if(!r.isSaved(x, z)) {
 			this.playersSaved.remove(p.getEntityId());
 		}
 	}
 	
 	//Function to see if a player is entering a "protected" zone around any Waypoint.
 	//It adds the player and region they are inside of to the list of players
 	//currently inside "protected" regions. 
 	public void checkIsProtected(Player p, int x, int z) {
 		Region currentRegion = null;
 		ListIterator<Region> itr = regions.listIterator();
 		
 		while(itr.hasNext()) {
 			currentRegion = itr.next();
 			if(currentRegion.isProtected(x, z)) {
 				playersProtected.put(p.getEntityId(), currentRegion);
 				itr.remove();
 				regions.addFirst(currentRegion);
 				
 				if(playersAreProtected())
 					p.sendMessage(wpMessage("You are now protected by a Waypoint!"));
 				break;
 			}
 		}
 	}
 	
 	//Checks if the player is currently in the "protected" region list.
 	//No point in checking if they entered a "protected" zone if they are already in one.
 	public Region playerIsProtected(Player p) {
 		return this.playersProtected.get(p.getEntityId());
 	}
 	
 	//Function to see if a player is leaving the "protected" zone they were in.
 	//It removes the player and region they are inside of to the list of players
 	//currently inside "protected" regions. 
 	public void checkIsUnprotected(Player p, Region r, int x, int z) {
 		if(!r.isProtected(x, z)) {
 			this.playersProtected.remove(p.getEntityId());
 			
 			if(playersAreProtected())
 				p.sendMessage(wpMessage("You are no longer protected by the Waypoint!"));
 		}
 	}
 	
 	//Checks if a player if within a "save" region. If so it calls wpBind() to
 	//bind the player to the Waypoint that owns the region.
 	public void tryBind(Player p) {
 		Region r = playerIsSaved(p);
 		if(r != null)
 			if(wpBind(p, r))
 				return;
 		p.sendMessage(wpMessage("You are not close enough to a Waypoint!"));
 		return;
 	}
 	
 	//Binds the given player to the Waypoint that owns the given region. Updates
 	//any associated lists/maps that must be updated.
 	public boolean wpBind(Player p, Region r) {
 		if(p != null && r != null) {
 			onlinePlayerPoints.remove(p.getEntityId());
 			onlinePlayerPoints.put(p.getEntityId(), r.getOwner());
 			allPlayerPoints.remove(p.getName());
 			allPlayerPoints.put(p.getName(), r.getOwner());
 			p.sendMessage(wpMessage("You have been bound to a waypoint!"));
 			return true;
 		}
 		p.sendMessage(wpMessage("Error binding to waypoint!"));
 		return false;
 	}
 	
 	//Called when a qualified player attempts to add a new Waypoint. It goes through
 	//the necessary tests to verify that a Waypoint can be added where the player stands
 	//and if it passes all tests it adds the Waypoint and updates any associated lists/maps.
 	public void tryAdd(Player p)
 	{
 		p.sendMessage(wpMessage("Attempting to add new Waypoint..."));
 		World current = p.getWorld();
 		int x = p.getLocation().getBlockX();
 		int y = p.getLocation().getBlockY();
 		int z = p.getLocation().getBlockZ();
 		int totalAir = 0;
 		
 		for(int j = -2; j <= 2; j++)
 			for(int k = 1; k <= 3; k++)
 				for(int l = -2; l <= 2; l++)
 					if(current.getBlockAt(x + j, y + k, z + l).getType() == Material.AIR)
 						totalAir++;
 		
 		if(((float)totalAir)/75.0 > .66) {
 			p.sendMessage(wpMessage("Surrounding area OK!"));
 			if(!newPointIntersectsAnother(x, z)) {
 				p.sendMessage(wpMessage("No points are conflicting!"));
 				p.teleportTo(new Location(current, x + 1.5, y + 1.5, z + .5));
 				
 				for(int i = -1; i <= 1; i++)
 					for(int j = -1; j <= 1; j++)
 						current.getBlockAt(x + i, y - 1, z + j).setType(Material.GLOWSTONE);
 				
 				for(int i = 0; i < 3; i++)
 					current.getBlockAt(x, y + i, z).setType(Material.BEDROCK);
 				int newId = getLargestPointId() + 1;
 				
 				if(newId >= 0) {
 					Point wp = new Point(newId, current.getName(), x, y, z);
 					wPoints.put(newId, wp);
 					regions.add(new Region(x, y, z, this.protectionRadius, this.saveRadius, wp));
 					p.sendMessage(wpMessage("New Waypoint added successfully!"));
 					p.sendMessage(wpMessage("- - - - - - - - - - - - - - - - -"));
 					p.sendMessage(wpMessage("Id: " + newId));
 					p.sendMessage(wpMessage("Location: (" + x + "," + y + "," + z + ")"));
 					p.sendMessage(wpMessage("Save Radius: " + this.saveRadius));
 					p.sendMessage(wpMessage("Protection Radius: " + this.protectionRadius));
 					this.zeroPoints = false;
 					return;
 				}
 				else
 					p.sendMessage(wpMessage("Invalid waypoint ID generated!"));
 			}
 			else
 				p.sendMessage(wpMessage("New waypoint would intersect another!"));
 		}
 		else
 			p.sendMessage(wpMessage("Not enough space to add waypoint!"));
 		p.sendMessage(wpMessage("Error adding waypoint!"));
 		return;
 	}
 	
 	//Called when a qualified player attempts to delete a Waypoint. It verifies that the
 	//player is standing within a valid "save" region and procceeds to delete the region's
 	//Waypoint. It then updates all associated lists/maps.
 	public void tryDel(Player p) {
 		//Get the save region the player is in, we can only
 		//remove it if the player removing is within a save
 		//region anyways.
 		Region sr = playerIsSaved(p);
 		
 		if(sr != null) {
 			//Lets do some smart block removal!
 			//The following huge chunk of code attempts to patch the
 			//hole where the old Waypoint was with blocks that fit the
 			//surrounding terrain to keep a natural look.
 			World current = p.getWorld();
 			int x = sr.getCenterX();
 			int y = sr.getCenterY();
 			int z = sr.getCenterZ();
 			ArrayList<Material> localBlocks = new ArrayList<Material>();
 			ArrayList<Integer> blockCount = new ArrayList<Integer>();
 			
 			for(int j = -5; j <= 5; j++)
 				for(int l = -5; l <= 5; l++) {
 					if((j < -1 || j > 1) && (l < -1 || l > 1)) {
 						Material m = current.getBlockAt(x + j, y - 1, z + l).getType();
 						if(localBlocks.contains(m))
 							blockCount.set(localBlocks.indexOf(m), blockCount.get(localBlocks.indexOf(m)) + 1);
 						else {
 							localBlocks.add(m);
 							blockCount.add(1);
 						}	
 					}
 				}
 			Material fillMaterial = null;
 			
 			if(localBlocks.size() == 0) {
 				p.sendMessage(wpMessage("No valid blocks found!"));
 				fillMaterial = Material.GRASS;
 			}
 			else {
 				int max = blockCount.get(0);
 				int index = 0;
 				
 				for(int i = 0; i < blockCount.size(); i++)
 					if(blockCount.get(i) > max) {
 						max = blockCount.get(i);
 						index = i;
 					}
 				fillMaterial = localBlocks.get(index);
 				p.sendMessage(wpMessage("Most common block: " + fillMaterial.toString() + " (" + max + ")!"));
 			}
 			p.sendMessage(wpMessage("Filling point with " + fillMaterial.toString() + "!"));
 			
 			for(int j = -1; j <= 1; j++)
 				for(int k = -1; k <= 1; k++)
 					current.getBlockAt(x + j, y - 1, z + k).setType(fillMaterial);
 			
 			for(int i = 0; i < 3; i++)
 				current.getBlockAt(x, y + i, z).setType(Material.AIR);
 			
 			//Physical Waypoint marker removed, time to update all
 			//associated Lists and HashMaps to reflect changes.
 			
 			//Players in the removed Waypoint's save region should
 			//no longer be saved!
 			Iterator<Region> itr = playersSaved.values().iterator();
 			while(itr.hasNext()) {
 				if(itr.next() == sr)
 					itr.remove();
 			}
 			//Players in the removed Waypoints's protection region
 			//should no longer be protected!
 			itr = playersProtected.values().iterator();
 			while(itr.hasNext()) {
 				if(itr.next() == sr)
 					itr.remove();
 			}
 			//Online players with the removed Waypoint as their home
 			//can no longer possess it as their spawn!
 			Iterator<Point> itr2 = onlinePlayerPoints.values().iterator();
 			while(itr2.hasNext()) {
 				if(itr2.next() == sr.getOwner())
 					itr2.remove();
 			}
 			//Any player that had the removed Waypoint as their home
 			//can no longer possess it as their spawn!
 			itr2 = allPlayerPoints.values().iterator();
 			while(itr.hasNext()) {
 				if(itr.next() == sr)
 					itr.remove();
 			}
 			//Remove the Point, it no longer exists!
 			wPoints.remove(sr.getOwner().getId());
 			//Remove the region, it no longer exists!
 			regions.remove(sr);
 			p.sendMessage(wpMessage("Removed point at (" + x + "," + y + "," + z + ")!"));
 			
 			if(regions.size() == 0)
 				this.zeroPoints = true;
 			//We're done!
 			return;
 		}
 		else
 			p.sendMessage(wpMessage("You are not within any waypoint's saving range!"));
 		p.sendMessage(wpMessage("Error Removing waypoint!"));
 		//Something went wrong! Couldn't delete Waypoint!
 		return;
 	}
 	
 	//This function is used to initiate a save of player data.
 	public void trySave(boolean notifyServer) {
 		cleanPlayerPoints();
 		writePointData(true, notifyServer, true);
 		writePlayerData(true, notifyServer, true);
 		return;
 	}
 	
 	//This function checks if a new Waypoint being created at
 	//the giver (x,z) will have regions that intersect with
 	//any other region!
 	private boolean newPointIntersectsAnother(int x, int z) {
 		Region currentRegion = null;
 		ListIterator<Region> itr = regions.listIterator();
 		boolean intersectsOther = false;
 		
 		while(itr.hasNext()) {
 			currentRegion = itr.next();
 			if(currentRegion.isSaved(x + saveRadius, z + saveRadius)) {
 				intersectsOther = true;
 				break;
 			}
 			else if(currentRegion.isSaved(x - saveRadius, z + saveRadius)) {
 				intersectsOther = true;
 				break;
 			}
 			else if(currentRegion.isSaved(x + saveRadius, z - saveRadius)) {
 				intersectsOther = true;
 				break;
 			}
 			else if(currentRegion.isSaved(x - saveRadius, z - saveRadius)) {
 				intersectsOther = true;
 				break;
 			}
 			else if(currentRegion.isProtected(x + protectionRadius, z + protectionRadius)) {
 				intersectsOther = true;
 				break;
 			}
 			else if(currentRegion.isProtected(x - protectionRadius, z + protectionRadius)) {
 				intersectsOther = true;
 				break;
 			}
 			else if(currentRegion.isProtected(x + protectionRadius, z - protectionRadius)) {
 				intersectsOther = true;
 				break;
 			}
 			else if(currentRegion.isProtected(x - protectionRadius, z - protectionRadius)) {
 				intersectsOther = true;
 				break;
 			}
 		}
 		return intersectsOther;
 	}
 	
 	//This function find the Waypoint with the highest ID
 	//from all Waypoints listed and returns its ID.
 	private int getLargestPointId() {
 		Iterator<Integer> itr = wPoints.keySet().iterator();
 		int max = -1;
 		
 		if(itr.hasNext())
 			max = itr.next(); 
 		int current = max;
 		
 		while(itr.hasNext())
 		{
 			current = itr.next();
 			
 			if(current > max)
 				max = current;
 		}
 		return max;
 	}
 	
 	//This function is called when the server is shut down
 	//or the plugin is otherwise disabled. It checks that each
 	//point ID associated to every player in the player list
 	//belongs to a point that still exists.
 	public void cleanPlayerPoints() {
 		HashMap<String, Point> cleanPlayerPoints = new HashMap<String, Point>();
 		Iterator<Entry<String, Point>> itr = allPlayerPoints.entrySet().iterator();
 		Entry<String, Point> current;
 		
 		if(itr.hasNext()) {
 			current = itr.next();
 			if(wPoints.containsKey(current.getValue().getId()))
 				cleanPlayerPoints.put(current.getKey(), current.getValue());
 		}
 		allPlayerPoints = cleanPlayerPoints;
 	}
 	
 	//This function is called when a player logs into the
 	//server. It loads the players stored Waypoint if they
 	//have one saved.
 	public void onLogin(int id, Player p) {
 		players.put(id, p);
 		Point wp = allPlayerPoints.get(p.getName());
 		if(wp != null)
 			onlinePlayerPoints.put(id, wp);
 	}
 	
 	//This function is called when a player logs out of the
 	//server. It unloads the player and their bound Waypoint
 	//from any applicable lists.
 	public void onLogout(int id) {
 		players.remove(id);
 		onlinePlayerPoints.remove(id);
 	}
 	
 	//Returns a player from the player list mapped to the
 	//given entity ID.
 	public Player getPlayerById(int id) {
 		return players.get(id);
 	}
 	
 	//Returns the Waypoint mapped to the given entity ID.
 	public Point getPointByPlayerId(int id) {
 		return onlinePlayerPoints.get(id);
 	}
 	
 	//Checks if there are 0 Waypoints defined.
 	public boolean hasZeroPoints() {
 		return this.zeroPoints;
 	}
 	
 	//Returns a formaated string, standardized to keep output
 	//from the Waypoints plugin consistent in visuals.
 	public String wpMessage(String msg) {
 		return ChatColor.DARK_AQUA + "[WAYPOINTS] " + ChatColor.WHITE + msg;
 	}
 	
 	//This function is called when we need to hook into GroupManager
 	public void enableGroupManager() {
 		Plugin perm = this.plugin.getServer().getPluginManager().getPlugin("GroupManager");
 
         if (this.permissions == null) {
             if (perm != null) {
             	if(perm.isEnabled()) {
 	                this.permissions = (GroupManager)perm;
 	                plugin.sendConsoleMsg("GroupManager found!");
 	                setGroupManagerStatus(true);
             	}
             }
             else {
             	plugin.sendConsoleMsg("GroupManager not found!");
                 setGroupManagerStatus(false);
             }
         }
 	}
 	
 	//This function is called to unhook from GroupManager
 	public void disableGroupManager() {
 		if (this.permissions != null) {
             this.permissions = null;
             plugin.sendConsoleMsg("GroupManager found!");
             setGroupManagerStatus(false);
         }
 		else {
 			plugin.sendConsoleMsg("GroupManager not found!");
             setGroupManagerStatus(false);
 		}
 	}
 	
 	//Sets a flag that denotes if we are hooked into GroupManager
 	public void setGroupManagerStatus(boolean online) {
 		if(online)
 			plugin.sendConsoleMsg("GroupManager functionality ENDABLED!");
 		else
 			plugin.sendConsoleMsg("GroupManager functionality DISABLED!");
 		this.gmIsWorking = online;
 	}
 	
 	//Returns whether or not we are currently hooked into GroupManager
 	public boolean gmWorking() {
 		return this.gmIsWorking;
 	}
 	
 	//Returns whether or not the config files says we should use GroupManager
 	public boolean isUsingGroupManager() {
 		return this.useGroupManager;
 	}
 	
 	//Returns the instance of GroupManager that we are hooked into
 	public GroupManager getPermissions() {
 		return this.permissions;
 	}
 	
 	//Returns whether or not Players are protected when inside a "protected" region.
 	public boolean playersAreProtected() {
 		return this.protectPlayers;
 	}
 	
 	//#################################################
 	//PRIVATE IO FUNCTIONS
 	//#################################################
 	
 	//Function to write point data to waypoints.txt.
 	//Also creates the file if it does not exist.
 	private boolean writePointData(boolean notifyConsole, boolean notifyServer, boolean withData) {
 		File dataFolder = plugin.getDataFolder();
 		String location = "";
 		
 		if(dataFolder.mkdir()) {
 			plugin.sendConsoleMsg("Config data folder not found!");
 			plugin.sendConsoleMsg("New folder made!");
 		}
 		location = dataFolder.getPath() + "/";
 		File points = new File(location + "waypoints.txt");
 		
 		if(!points.exists()) {
 			plugin.sendConsoleMsg("Point data file is missing!");
 			plugin.sendConsoleMsg("Creating a new one!");
 			PrintWriter pointsMaker;
 			
 			try {
 				pointsMaker = new PrintWriter(points);
 				pointsMaker.println("#Waypoints Plugin (Bukkit) Waypoint data file");
 				pointsMaker.println("#Waypoint save format: 'pointId:worldName:x:y:z'");
 				pointsMaker.close();
 			}
 			catch(Exception e) {
 				plugin.sendConsoleMsg("Unable to create waypoints file!");
 				return false;
 			}
 		}
 		else {
 			if(withData) {
 				if(notifyConsole)
 					plugin.sendConsoleMsg("Saving point data!");
 				if(notifyServer)
 					plugin.getServer().broadcastMessage(wpMessage("Saving point data!"));
 				PrintWriter pointsMaker;
 				
 				try {
 					pointsMaker = new PrintWriter(points);
 					pointsMaker.println("#Waypoints Plugin (Bukkit) Waypoint data file");
 					pointsMaker.println("#Waypoint save format: 'pointId:worldName:x:y:z'");
 					Iterator<Point> itr = wPoints.values().iterator();
 					Point p = null;
 					
 					while(itr.hasNext()) {
 						p = itr.next();
 						
 						if(p != null)
 							pointsMaker.println(p.getId() + ":" + p.getWorld() + ":" + p.getX() + ":" + p.getY() + ":" + p.getZ());
 					}
 					pointsMaker.close();
 				}
 				catch(Exception e) {
 					if(notifyServer)
 						plugin.getServer().broadcastMessage(wpMessage("Error saving point data file!"));
 					plugin.sendConsoleMsg("Error saving point data file!");
 					return false;
 				}
 				
 				if(notifyConsole)
 					plugin.sendConsoleMsg("Point data has been saved!");
 				if(notifyServer)
 					plugin.getServer().broadcastMessage(wpMessage("Point data has been saved!"));
 			}
 		}
 		return true;
 	}
 	
 	//Function to write player data to waypoints.txt.
 	//Also creates the file if it does not exist.
 	private boolean writePlayerData(boolean notifyConsole, boolean notifyServer, boolean withData) {
 		File dataFolder = plugin.getDataFolder();
 		String location = "";
 		
 		if(dataFolder.mkdir()) {
 			plugin.sendConsoleMsg("Config data folder not found!");
 			plugin.sendConsoleMsg("New folder made!");
 		}
 		location = dataFolder.getPath() + "/";
 		File players = new File(location + "playerpoints.txt");
 		
 		if(!players.exists()) {
 			plugin.sendConsoleMsg("Player data file is missing!");
 			plugin.sendConsoleMsg("Creating a new one!");
 			PrintWriter playerMaker;
 			
 			try {
 				playerMaker = new PrintWriter(players);
 				playerMaker.println("#Waypoints Plugin (Bukkit) Player data file");
 				playerMaker.println("#Player save format: 'playerName:pointId'");
 				playerMaker.close();
 			}
 			catch(Exception e) {
 				plugin.sendConsoleMsg("Unable to create playerpoints file!");
 				return false;
 			}
 		}
 		else {
 			if(withData) {
 				if(notifyConsole)
 					plugin.sendConsoleMsg("Saving player data!");
 				if(notifyServer)
 					plugin.getServer().broadcastMessage(wpMessage("Saving player data!"));
 				PrintWriter playerMaker;
 				
 				try {
 					playerMaker = new PrintWriter(players);
 					playerMaker.println("#Waypoints Plugin (Bukkit) Player data file");
 					playerMaker.println("#Player save format: 'playerName:pointId'");
 					Iterator<Entry<String, Point>> itr = allPlayerPoints.entrySet().iterator();
 					Entry<String, Point> e = null;
 					
 					while(itr.hasNext()) {
 						e = itr.next();
 						
 						if(e != null)
 							playerMaker.println(e.getKey() + ":" + e.getValue().getId());
 					}
 					playerMaker.close();
 				}
 				catch(Exception e) {
 					if(notifyServer)
 						plugin.getServer().broadcastMessage(wpMessage("Error saving player data file!"));
 					plugin.sendConsoleMsg("Error saving player data file!");
 					return false;
 				}
 				
 				if(notifyConsole)
 					plugin.sendConsoleMsg("Player data has been saved!");
 				if(notifyServer)
 					plugin.getServer().broadcastMessage(wpMessage("Player data has been saved!"));
 			}
 		}
 		return true;
 	}
 	
 	//Function to write default config data to file if
 	//it does not already exist.
 	private boolean writeConfigData() {
 		File dataFolder = plugin.getDataFolder();
 		String location = "";
 		
 		if(dataFolder.mkdir())
 		{
 			plugin.sendConsoleMsg("Config data folder not found!");
 			plugin.sendConsoleMsg("New folder made!");
 		}
 		location = dataFolder.getPath() + "/";
 		//Check that all needed config files exist
 		//Create them with defaults if the don't
 		File config = new File(location + "config.txt");
 		
 		if(!config.exists()) {
 			plugin.sendConsoleMsg("Config file is missing!");
 			plugin.sendConsoleMsg("Creating a new one with default settings!");
 			PrintWriter configMaker;
 			
 			try {
 				configMaker = new PrintWriter(config);
 				configMaker.println("#Waypoints Plugin (Bukkit) Configuration file");
 				configMaker.println("# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #");
 				configMaker.println("protect-players=true");
 				configMaker.println("# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #");
 				configMaker.println("#If true, a player will not be able to be damaged if they are");
 				configMaker.println("#within the protection radius of a Waypoint.");
 				configMaker.println("# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #");
 				configMaker.println("protection-radius=8");
 				configMaker.println("# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #");
 				configMaker.println("#This sets the radius (A square not a cirle) from the center");
 				configMaker.println("#of the Waypoint in which a player (if protect-player is TRUE");
 				configMaker.println("#and the environment cannot be damaged. If it is anything less");
 				configMaker.println("#than 1 the plugin will set the protection radius to 1 automatically.");
 				configMaker.println("# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #");
 				configMaker.println("force-autosave=true");
 				configMaker.println("# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #");
 				configMaker.println("#If true, the player will automatically be bound to the waypoint");
 				configMaker.println("#if the come withing the radius defined by save-radius. If false,");
 				configMaker.println("#the player must type the command /wpbind to bind themeselve to");
 				configMaker.println("#the waypoint; the player must also be within the save-radius.");
 				configMaker.println("# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #");
 				configMaker.println("save-radius=16");
 				configMaker.println("# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #");
 				configMaker.println("#If autosave is set to true then a player must move within this");
 				configMaker.println("#radius (A square not a circle) of a Waypoint for it to be set");
 				configMaker.println("#as their new spawning point. If autosave is set to false then");
 				configMaker.println("#the player must be in this radius to use the /wpbind command.");
 				configMaker.println("#If anything less than 1 is entered the plugin will set the save");
 				configMaker.println("#radius to 1 automatically.");
 				configMaker.println("# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #");
 				configMaker.println("auto-generate-points=false");
 				configMaker.println("# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #");
 				configMaker.println("#NOT CODED OR WORKING CURRENTLY#");
 				configMaker.println("#If true, the plugin will add random waypoints as the map");
 				configMaker.println("#generates. If false, and admin must manually place new waypoints");
 				configMaker.println("#by using the command /wpadd to add a waypoint to where they");
 				configMaker.println("#are currently standing.");
 				configMaker.println("# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #");
 				configMaker.println("delete-missing-points=true");
 				configMaker.println("# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #");
 				configMaker.println("#NOT CODED OR WORKING CURRENTLY#");
 				configMaker.println("#As the server loads the plugin will check if there is actually a");
 				configMaker.println("#waypoint in the world. If delete-missing-points is true, when a");
 				configMaker.println("#listed point is not found in the world it will be deleted from");
 				configMaker.println("#the list of waypoints. If false, the plugin will create waypoints");
 				configMaker.println("#at the given location.");
 				configMaker.println("# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #");
 				configMaker.println("use-group-manager=true");
 				configMaker.println("# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #");
 				configMaker.println("#If set to true the plugin will look for the GroupManager plugin");
 				configMaker.println("#and use it while it exists and is enabled, if neither it defaults");
 				configMaker.println("#to giving OP's the power to spawn/delete Waypoints. If this option");
 				configMaker.println("#is set to false the plugin will always ignore the GroupManager");
 				configMaker.println("#plugin and give Waypoint spawn/delete power to OP's only.");
 				configMaker.println("# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #");
				configMaker.println("data-save-interval=0");
 				configMaker.println("# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #");
 				configMaker.println("#NOT CODED OR WORKING CURRENTLY#");
 				configMaker.println("#Specifies the amount of time in minutes between when the plugin");
 				configMaker.println("#will automatically save all Waypoint data to disk. If set to 0 then");
 				configMaker.println("#the data will not automatically be saved. Data can still be saved");
 				configMaker.println("#using the /wpsave command. Data will always be saved if as you shut");
 				configMaker.println("#the server down IF you shut it down correctly using the 'stop' command.");
 				configMaker.close();
 			}
 			catch(Exception e) {
 				plugin.sendConsoleMsg("Unable to create config file!");
 				plugin.sendConsoleMsg("The plugin will be disabled!");
 				return false;
 			}
 		}
 		return true;
 	}
 }

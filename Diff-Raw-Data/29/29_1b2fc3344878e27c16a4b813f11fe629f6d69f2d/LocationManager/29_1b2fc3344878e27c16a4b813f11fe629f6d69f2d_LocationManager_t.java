 /**
  *  Name: LocationManager.java
  *  Date: 19:56:01 - 15 sep 2012
  * 
  *  Author: LucasEmanuel @ bukkit forums
  *  
  *  
  *  Copyright 2013 Lucas Arnstrm
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program. If not, see <http://www.gnu.org/licenses/>.
  *  
  *
  *  Filedescription:
  *  
  *  Manages all locations for all gameworlds.
  *  
  * 
  * 
  */
 
 package se.lucasarnstrom.survivalgamesmultiverse.managers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import se.lucasarnstrom.survivalgamesmultiverse.Main;
 import se.lucasarnstrom.survivalgamesmultiverse.utils.ConsoleLogger;
 import se.lucasarnstrom.survivalgamesmultiverse.utils.SerializedLocation;
 
 public class LocationManager {
 	
 	private Main plugin;
 	final private ConsoleLogger logger;
 	
 	// Key = Worldname, Value = Main/Arena lists, ValueOfValue = key=location, boolean=true means the location is available
 	private HashMap<String, HashMap<String, HashMap<Location, Boolean>>> locations;
 	
 	public LocationManager(Main instance) {
 		plugin = instance;
 		logger = new ConsoleLogger("LocationManager");
 		
 		locations = new HashMap<String, HashMap<String, HashMap<Location, Boolean>>>();
 		
 		logger.debug("Initiated");
 	}
 	
 	public void addWorld(String worldname) {
 		logger.debug("Adding world: " + worldname);
 		
 		locations.put(worldname, new HashMap<String, HashMap<Location, Boolean>>());
 		loadLocations(worldname);
 	}
 	
 	public void resetLocationStatuses(World world) {
 		
 		String worldname = world.getName();
 		
 		HashMap<Location, Boolean> main  = locations.get(worldname).get("main");
 		HashMap<Location, Boolean> arena = locations.get(worldname).get("arena");
 		
 		if(main != null) 
 			for(Entry<Location, Boolean> entry : main.entrySet()) {
 				entry.setValue(true);
 			}
 		
 		if(arena != null)
 			for(Entry<Location, Boolean> entry : arena.entrySet()) {
 				entry.setValue(true);
 			}
 	}
 	
 	public void resetLocationStatus(Location playerlocation) {
 		
 		String worldname = playerlocation.getWorld().getName();
 		
 		HashMap<Location, Boolean> main = locations.get(worldname).get("main");
 		
 		int playerX = playerlocation.getBlockX();
 		int playerY = playerlocation.getBlockY();
 		int playerZ = playerlocation.getBlockZ();
 		
 		for(Entry<Location, Boolean> entry : main.entrySet()) {
 			
 			int entryX = entry.getKey().getBlockX();
 			int entryY = entry.getKey().getBlockY();
 			int entryZ = entry.getKey().getBlockZ();
 			
 			if(entryX == playerX && entryY == playerY && entryZ == playerZ) {
 				entry.setValue(true);
 				return;
 			}
 		}
 	}
 	
	public boolean areThereLocationsLeft(String worldname) {
		
		HashMap<Location, Boolean> locationlist = locations.get(worldname).get("main");
		
		if(locationlist == null) {
			logger.severe("No saved locations for world: " + worldname);
			return false;
		}
		
		for(Boolean value : locationlist.values()) {
			if(value == true) {
				return true;
			}
		}
		
		return false;
	}
	
 	public boolean tpToStart(Player player, String worldname) {
 		
 		HashMap<Location, Boolean> locationlist = locations.get(worldname).get("main");
 		
 		if(locationlist == null) {
 			logger.severe("No saved locations for world: " + worldname);
 			return false;
 		}
 		
 		for(Entry<Location, Boolean> entry : locationlist.entrySet()) {
 			if(entry.getValue()) {
 				
 				logger.debug("Teleporting player: " + player.getName() + " to start");
 				
 				player.teleport(entry.getKey());
 				entry.setValue(false);
 				
 				return true;
 			}
 		}
 		
 		logger.debug("No locations left for world: " + worldname);
 		return false;
 	}
 	
 	public boolean tpToArena(Player player) {
 		
 		logger.debug("tpToArena() called for player: " + player.getName());
 		
 		HashMap<Location, Boolean> locationlist = locations.get(player.getWorld().getName()).get("arena");
 		
 		if(locationlist == null) {
 			logger.severe("No saved arenalocations for world: " + player.getWorld().getName());
 			return false;
 		}
 		
 		for(Entry<Location, Boolean> entry : locationlist.entrySet()) {
 			if(entry.getValue()) {
 				
 				logger.debug("Sending player to arenalocation!");
 				
 				player.teleport(entry.getKey());
 				entry.setValue(false);
 				
 				return true;
 			}
 		}
 		
 		logger.debug("No arenalocations left for world: " + player.getWorld().getName());
 		return false;
 	}
 	
 	public boolean addLocation(String type, Location location) {
 		
 		if(locations.containsKey(location.getWorld().getName())) {
 			
 			logger.debug("Adding locationtype: " + type + ", to world: " + location.getWorld().getName());
 			
 			HashMap<Location, Boolean> map = locations.get(location.getWorld().getName()).get(type);
 			
 			if(map == null) {
 				map = new HashMap<Location, Boolean>();
 				locations.get(location.getWorld().getName()).put(type, map);
 			}
 			
 			map.put(location, true);
 			
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 
 	public boolean saveLocationList(final String listtype, final String worldname) {
 		
 		if(locations.containsKey(worldname)) {
 			
 			logger.debug("Saving locationtype: " + listtype + " for world: " + worldname);
 			
 			Map<Location, Boolean> tempmap = this.locations.get(worldname).get(listtype);
 			
 			if(tempmap != null) {
 				
 				final HashSet<SerializedLocation> locations = new HashSet<SerializedLocation>();
 				
 				for(Location location : tempmap.keySet()) {
 					locations.add(new SerializedLocation(location));
 				}
 				
 				plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
 					public void run() {
 						plugin.getSQLiteConnector().saveStartLocations(worldname, listtype, locations);
 					}
 				});
 				
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	private void loadLocations(String worldname) {
 		
 		logger.debug("Loading locations for world: " + worldname);
 		
 		ArrayList<HashSet<String>> locations = plugin.getSQLiteConnector().getStartLocations(worldname);
 		
 		if(locations != null && locations.get(0).size() > 0) {
 			
 			HashSet<String> main  = locations.get(0);
 			HashSet<String> arena = locations.get(1);
 			
 			for(String serial : main) {
 				addLocation("main", SerializedLocation.deserializeString(serial));
 			}
 			
 			for(String serial : arena) {
 				addLocation("arena", SerializedLocation.deserializeString(serial));
 			}
 			
 		}
 		else
 			logger.warning("No saved locations for world: " + worldname);
 	}
 
 	public boolean clearLocationList(String type, String worldname) {
 		
 		if(locations.containsKey(worldname)) {
 			
 			HashMap<Location, Boolean> locationlist = locations.get(worldname).get(type);
 			
 			if(locationlist != null)
 				locationlist.clear();
 			
 			return true;
 		}
 		
 		return false;
 	}
 
 	public int getLocationAmount(String worldname) {
 		
 		HashMap<Location, Boolean> main = locations.get(worldname).get("main");
 		
 		if(main == null)
 			return 0;
 		else
 			return main.size();
 	}
 }

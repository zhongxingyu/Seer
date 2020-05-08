 package net.lotrcraft.strategycraft.buildings;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import net.lotrcraft.strategycraft.Config;
import net.lotrcraft.strategycraft.StrategyCraft;
 import net.lotrcraft.strategycraft.loader.BuildingDescription;
 
 import org.bukkit.Location;
 
 public class BuildingManager {
 	private static List<Castle> castles = new ArrayList<Castle>();
 	private static List<BuildingDescription> bldgs = new ArrayList<BuildingDescription>();
 	
 	public static boolean addCastle(String playerName, Castle castle){
 		if (!castles.contains(playerName)){
 			castles.add(castle);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Adds a building type to the list.
 	 * @param buildingDescription The BuildingDescription to add.
 	 * @return True if successful
 	 */
 	public static boolean addBuildingType(BuildingDescription buildingDescription){
 		if (bldgs.contains(buildingDescription.getName())){
 			return false;
 		}
 
 		bldgs.add(buildingDescription);
 		return true;
 	}
 	
 	/**
 	 * Returns a list of known castles.
 	 * @return List of Castles
 	 */
 	public static List<Castle> getCastles(){
 		return castles;
 	}
 	
 	/**
 	 * Gets the castle with aa Citadel at the stated location.
 	 * @param l Location of Citadel core block.
 	 * @return the Castle if one can be found at that location.
 	 */
 	public static Castle getCastleAtLoc(Location l){
 		List<Castle> tmpC = getCastles();
 		
 		for (int y = 0; y < tmpC.size(); y++){
 			if (tmpC.get(y).getLocation().equals(l))
 				return tmpC.get(y);
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Gets the building at a specified location. 
 	 * @param location The location of the core block of the alleged building.
 	 * @return Null if no building is found at the location; The building if otherwise.
 	 */
 	public static Building getBuildingAtLoc(Location location){
 		Castle[] tmpC = (Castle[]) BuildingManager.getCastles().toArray();
 		if (tmpC.length == 0)
 			return null;
 		for (int y = 0; y < tmpC.length; y++){
 			if (tmpC[y].getBuildingAtLoc(location) != null){
 				return tmpC[y].getBuildingAtLoc(location);
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * This atempts to create a new castle by building a Citadel on the 
 	 * supplied coords.
 	 * 
 	 * @param name The player's name who built the castle
 	 * @param location The location to build the castle at
 	 * @return Whether this action was successful
 	 */
 	public static void createNewCastle(String name, Location location){
 		castles.add(new Castle(location, name));
 	}
 	
 
 	/**
 	 * Trys to find a castle owned by the named player.
 	 * If it can't find the castle, returns null.
 	 * 
 	 * @param playerName
 	 * @return The player's castle
 	 */
 	public static Castle getCastle(String playerName) {
 		
 		for (int y = 0; y < castles.size(); y++){
 			if (castles.get(y).getOwner().equals(playerName))
 					return castles.get(y);
 		}
 		return null;
 	}
 
 	/**
 	 * Finds and returns the class of the type of building specified. 
 	 * Returns null if it cant be found.
 	 * 
 	 * @param name Name of building
 	 * @return The Class of the building; null if it doesnt exist.
 	 */
 	public static Class<? extends Building> getBuildingClass(String name) {
 		for (int y = 0; y < bldgs.size(); y++){
 			if (bldgs.get(y).getName().equalsIgnoreCase(name)){
 				return (bldgs.get(y).getBuilding());
 			}
 		}
 		return null;
 	}
 	
 	public static BuildingDescription getBuilding(String name){
		if (bldgs == null)
			return null;
 		for (int y = 0; y < bldgs.size(); y++){
 			if (bldgs.get(y).getName().equalsIgnoreCase(name)){
 				return bldgs.get(y);
 			}
 		}
 		return null;
 	}
 	
 	public static Building getNewBuilding(String name){
 		try {
 			for (int y = 0; y < bldgs.size(); y++){
 				if (bldgs.get(y).getName().equalsIgnoreCase(name)){
 					return (bldgs.get(y).getBuilding().newInstance());
 				}
 			}
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * Completely destroys the supplied castle and its buildings.
 	 * 
 	 * @param castle The castle to be destroyed.
 	 */
 	public static void destroyCastle(Castle castle){
 		castle.destroy();
 		castles.remove(castle.getOwner());
 		Config.removePlayerConf(castle.getOwner());
 	}
 
 }

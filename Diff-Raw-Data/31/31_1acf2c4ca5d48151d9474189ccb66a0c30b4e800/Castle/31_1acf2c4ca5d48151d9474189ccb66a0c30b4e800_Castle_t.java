 package net.lotrcraft.strategycraft.buildings;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.lotrcraft.strategycraft.Config;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 
 public class Castle {
 	private List<Building> buildings = new ArrayList<Building>();
 	private Location l;
 	private Player player;
 	private String playerName;
 	
 	
 	public Castle (Location l, String playerName){
 		this.l = l;
 		this.player = Bukkit.getPlayerExact(playerName);
 		this.playerName = playerName;
 		buildings.add(BuildingManager.getNewBuilding("Citadel"));
 		buildings.get(0).build(l);
 	}
 	
 	/**
 	 * Gets a building at a specific location.
 	 * @param l
 	 * @return The building if this castle has a building registered at the location. Null if not.
 	 */
 	public Building getBuildingAtLoc(Location l){
 		for (int y = 0; y < buildings.size(); y++){
			if (buildings.get(y).getLocation() == l)
 				return buildings.get(y);
 		}
 		return null;
 	}
 	
 	/**
 	 * Checks if there are any buildings left in the castle.
 	 * @return True if there are buildings left.
 	 */
 	public boolean isBuildingLeft(){
 		return !buildings.isEmpty();
 	}
 	
 	/**
 	 * Adds a building to this castle and builds it, but only if the castle
 	 * has enough room left. Returns true if successful.
 	 * 
 	 * @param building Building to add.
 	 * @param blockFace 
 	 * @param blockLoc 
 	 * @return True if succesful
 	 */
 	public boolean addBuilding(Building building, Location blockLoc, BlockFace blockFace){
 		if (buildings.size() < Config.maxBuildings){
 			buildings.add(building);
 			building.build(blockLoc);
 			building.setCastle(this);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Gets the lcation of the center block 
 	 * of this castle's citadel.
 	 * @return The location
 	 */
 	public Location getLocation(){
 		return l;
 	}
 
 	/**
 	 * Destroys this castle, irrespective of how many buildings it has left.
 	 * Also destroys all remaining buildings.
 	 */
 	public void destroy() {
 		for (int y = 0; y < buildings.size(); y++){
 			destroyBuilding(buildings.get(y));
 		}
 	}
 	
 	/**
 	 * Gets the player that owns this castle.
 	 * @return The name of the player.
 	 */
 	public String getOwner(){
 		return playerName;
 	}
 
 	
 	/**
 	 * Destroys and deletes the building within this castle specified by the 
 	 * supplied parameter.
 	 * 
 	 * @param b The building to destroy
 	 * @return True if successful; False if the building doesn't exist.
 	 */
 	public boolean destroyBuilding(Building b) {
 		for (int y = 0; y < buildings.size(); y++){
 			if (buildings.get(y).equals(b)){
 				buildings.get(y).destroy();
 				buildings.remove(y);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 
 }

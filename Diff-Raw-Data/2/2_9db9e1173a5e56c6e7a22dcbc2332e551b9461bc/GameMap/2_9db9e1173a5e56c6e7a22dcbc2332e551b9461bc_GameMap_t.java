 package net.amoebaman.gamemasterv3.api;
 
 import org.bukkit.Bukkit;
 
 import net.amoebaman.gamemasterv3.util.PropertySet;
 
 /**
  * Represents a map that an {@link AutoGame} can draw data from to define
  * certain properties and locations.
  * <p>
  * Game maps are designed to be universal - they are not linked or bound to
  * specific games, instead games much determine if they can play on a given map
  * be ascertaining whether it has defined the properties needed for it to run
  * (like a winning score, flag locations, whatever).
  * 
  * @author AmoebaMan
  */
 public class GameMap{
 	
 	private final String name;
 	private final PropertySet properties;
 	
 	public GameMap(String name){
 		this.name = name;
 		properties = new PropertySet();
 		/*
 		 * It's almost certain that decimals will be stored in here at some
 		 * point...
 		 */
 		properties.options().pathSeparator('/');
 		/*
 		 * Add basic values that must always be present
 		 */
 		properties.set("world", Bukkit.getWorlds().get(0).getName());
 		properties.set("capacity/min", 2);
 		properties.set("capacity/max", 32);
 		properties.set("atmos/time", "none");
		properties.set("atmos/weather", "none");
 	}
 	
 	/**
 	 * Gets the name of this map.
 	 * 
 	 * @return the name
 	 */
 	public String getName(){
 		return name;
 	}
 	
 	/**
 	 * Gets this map's set of properties, basically just a storage location for
 	 * games to draw data that they need from.
 	 * <p>
 	 * This instance is mutable. Changing it will change the map's internal data
 	 * as well. That said, maps themselves are immutable - changing the map's
 	 * data will not change the map on record unless you forcibly re-save it.
 	 * 
 	 * @return the map's properties
 	 */
 	public PropertySet getProperties(){
 		return properties;
 	}
 	
 	public String toString(){
 		return getName();
 	}
 	
 	public boolean equals(Object x){
 		return x instanceof GameMap ? ((GameMap) x).getName().equals(getName()) : false;
 	}
 	
 	public int hashCode(){
 		return getName().hashCode();
 	}
 	
 }

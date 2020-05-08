 package com.gmail.scottmwoodward.headhunter.helpers;
 
 import java.util.Iterator;
 import java.util.List;
 
import org.bukkit.World;

 public class WorldHelper {
 	
 	/**
 	 * Checks if a world is added in the config file, as an option to be disabled.
 	 * 
 	 * @param worlds A list of worlds to be checked against
 	 * @param world The world to check if is disabled
 	 * @return true if world is disabled
 	 */
	public static boolean isCurrentWorldDisabled(List<String> worlds, World world){
 		Iterator<String> iter = worlds.iterator();
 		while(iter.hasNext()) {
 			if(iter.next().equals(world)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 }

 package com.ayan4m1.multiarrow;
 
 import java.util.HashMap;
 
 import org.bukkit.entity.Arrow;
 import org.bukkit.util.Vector;
 import org.bukkit.Location;
 
 public class MultiArrowBlockHitDetector implements Runnable {
 	private MultiArrow plugin;
 	private HashMap<Arrow, Location> locations;
 	private HashMap<Arrow, Integer> counts;
 
 	public MultiArrowBlockHitDetector(MultiArrow instance) {
 		plugin = instance;
 		locations = new HashMap<Arrow, Location>();
 		counts = new HashMap<Arrow, Integer>();
 	}
 
 	@Override
 	public void run() {
 		if (plugin.activeArrowEffect.size() > 0) {
 			Object[] activeArrows = plugin.activeArrowEffect.keySet().toArray();
 			for (int i = 0; i < activeArrows.length; i++) {
 				Arrow arrow = (Arrow) activeArrows[i];
 				if (locations.containsKey(arrow) && counts.containsKey(arrow)) {
					if (counts.get(arrow) > 100 || arrow.isDead()) {
 						arrow.remove();
 						plugin.activeArrowEffect.remove(arrow);
 						locations.remove(arrow);
 						counts.remove(arrow);
 						continue;
 					}
 
 					Location loc = (Location) locations.get(arrow);
 					Location loca = arrow.getLocation();
 					if (loc.getBlockX() == loca.getBlockX() && loc.getBlockY() == loca.getBlockY() && loc.getBlockZ() == loca.getBlockZ()) {
 						if (isBlockAdjacent(loc)) {
 							plugin.activeArrowEffect.get(arrow).hitGround(arrow);
 							plugin.activeArrowEffect.remove(arrow);
 							locations.remove(arrow);
 							counts.remove(arrow);
 						}
 					} else {
 						locations.put(arrow, arrow.getLocation());
 						counts.put(arrow, counts.get(arrow) + 1);
 					}
 				} else {
 					locations.put(arrow, arrow.getLocation());
 					counts.put(arrow, 0);
 				}
 			}
 		}
 	}
 
 	private boolean isBlockAdjacent(Location loc) {
 		for(int x = -1; x <= 1; x++) {
 			for(int y = -1; y <= 1; y++) {
 				for(int z = -1; z <= 1; z++) {
 					Vector offset = loc.toVector().add(new Vector(x, y, z));
 					Location searchLoc = new Location(loc.getWorld(), offset.getX(), offset.getY(), offset.getZ());
 					if (!searchLoc.getBlock().isEmpty()) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 }

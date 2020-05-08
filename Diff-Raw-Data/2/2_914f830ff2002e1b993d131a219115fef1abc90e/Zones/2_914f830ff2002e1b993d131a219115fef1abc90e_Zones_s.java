 package btwmods.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.minecraft.src.ChunkCoordIntPair;
 import btwmods.util.intervals.IntervalTree;
 
 public class Zones<Type> {
 
 	private boolean hasAreas = false;
 	
 	private Set<Area<Type>> areas = new HashSet<Area<Type>>();
 	private Map<ChunkCoordIntPair, IntervalTree<Area<Type>>> intervalsByRegion = new HashMap<ChunkCoordIntPair, IntervalTree<Area<Type>>>();
 	
 	public boolean add(Area<Type> area) {
 		boolean added = areas.add(area);
 		
 		if (added) {
 			// Add the area to it's respective regions.
 			for (int regionX = area.x1 >> 9; regionX <= area.x2 >> 9; regionX++) {
 				for (int regionZ = area.z1 >> 9; regionZ <= area.z2 >> 9; regionZ++) {
 					
 					ChunkCoordIntPair coords = new ChunkCoordIntPair(regionX, regionZ);
 					
 					IntervalTree<Area<Type>> intervals = intervalsByRegion.get(coords);
 					if (intervals == null)
 						intervalsByRegion.put(coords, intervals = new IntervalTree<Area<Type>>());
 					
 					intervals.addInterval(area.x1 - 1, area.x2 + 1, area);
 				}
 			}
 		}
 		
 		// TODO: clear any cache.
 		
 		hasAreas = true;
 		
 		return added;
 	}
 	
 	public boolean remove(Area<Type> area) {
 		boolean removed = areas.remove(area);
 		if (removed) {
 			for (int regionX = area.x1 >> 9; regionX <= area.x2 >> 9; regionX++) {
 				for (int regionZ = area.z1 >> 9; regionZ <= area.z2 >> 9; regionZ++) {
 
 					ChunkCoordIntPair coords = new ChunkCoordIntPair(regionX, regionZ);
 					
 					IntervalTree<Area<Type>> intervals = intervalsByRegion.get(coords);
 					if (intervals != null)
 						intervals.removeByData(area);
 					
 					if (intervals.listSize() == 0)
 						intervalsByRegion.remove(coords);
 				}
 			}
 		}
 		
 		hasAreas = areas.size() > 0;
 		
 		// TODO: clear any cache.
 		
 		return removed;
 	}
 	
 	public List<Area<Type>> get(int x, int z) {
 		return get(x, 0, z, false);
 	}
 	
 	public List<Area<Type>> get(int x, int y, int z) {
 		return get(x, y, z, true);
 	}
 	
 	private List<Area<Type>> get(int x, int y, int z, boolean checkY) {
 		ArrayList<Area<Type>> areas = new ArrayList<Area<Type>>();
 		
 		if (hasAreas) {
 			// Get areas for the region the X and Z are in.
 			IntervalTree tree = intervalsByRegion.get(new ChunkCoordIntPair(x >> 9, z >> 9));
 			
 			if (tree != null) {
 				List<Area<Type>> intervalAreas = tree.get(x);
 				int size = intervalAreas.size();
 				
 				// Check all the areas that matched X.
 				for (int i = 0; i < size; i++) {
					Area<Type> area = intervalAreas.get(0);
 					
 					// Check if the Z matches.
 					if (z >= area.z1 && z <= area.z2) {
 						
 						// Also check the Y if is a cube and checking Y
 						if (checkY && area instanceof Cube) {
 							Cube cube = (Cube)area;
 							if (y >= cube.y1 && y <= cube.y2) {
 								areas.add(area);
 							}
 						}
 						else {
 							areas.add(area);
 						}
 					}
 				}
 			}
 		}
 		
 		return areas;
 	}
 }

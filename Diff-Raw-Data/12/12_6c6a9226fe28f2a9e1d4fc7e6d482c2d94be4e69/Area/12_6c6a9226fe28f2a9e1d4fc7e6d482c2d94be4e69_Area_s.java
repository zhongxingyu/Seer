 package me.sd5.mcbetaterraingenerator;
 
 import java.util.ArrayList;
import java.util.List;
 
 /**
  * @author sd5
  * An area of regions to generate.
  */
 public class Area {
 
 	//List of all regions this area contains.
 	private ArrayList<Region> regions;
 	
 	//Coordinates of the regions which define the rectangle of this area.
 	private int x1;
 	private int z1;
 	private int x2;
 	private int z2;
 	
 	/**
 	 * Creates a new area of regions defned by two regions.
 	 * @param x1 X-coordinate of the first region.
 	 * @param z1 Z-coordinate of the first region.
 	 * @param x2 X-coordinate of the second region.
 	 * @param z2 Z-coordinate of the second region.
 	 */
 	public Area(int x1, int z1, int x2, int z2) {
 		
 		//Adjust values.
 		this.x1 = (x1 > x2) ? x2 : x1;
 		this.z1 = (z1 > z2) ? z2 : z1;
 		this.x2 = (x1 > x2) ? x1 : x2;
 		this.z2 = (z1 > z2) ? z1 : z2;
 		
 		//Find all chunks in this area.
 		regions = new ArrayList<Region>();
		for(int x = x1; x <= x2; x++) {
			for(int z = z1; z <= z2; z++) {
 				regions.add(new Region(x, z));
 			}
 		}
 		
 	}
 	
 	/**
 	 * @return A list of the regions this area contains.
 	 */
 	public ArrayList<Region> getRegions() {
 		
 		return regions;
 		
 	}
 	
 }

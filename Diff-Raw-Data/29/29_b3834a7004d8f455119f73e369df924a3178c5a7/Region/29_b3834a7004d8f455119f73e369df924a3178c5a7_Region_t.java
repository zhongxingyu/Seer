 package me.sd5.mcbetaterraingenerator;
 
 /**
  * @author sd5
  * A minecraft region.
  */
 public class Region {
 
 	//The coordinates of this region.
 	private int x;
 	private int z;
 	
 	/**
 	 * Creates a new region.
 	 * @param x The X-coordinate of this region.
 	 * @param z The Z-coordinate of this region.
 	 */
 	public Region(int x, int z) {
 		
 		this.x = x;
 		this.z = z;
 		
 	}
 	
 	/**
 	 * @return The X-coordinate of this region.
 	 */
 	public int getX() {
 		
 		return x;
 		
 	}
 	
 	/**
 	 * @return The Z-coordinate of this region.
 	 */
 	public int getZ() {
 		
 		return z;
 		
 	}
 	
	/**
	 * Returns the filename of the region.
	 * @param anvil If true returns the filename for the anvil file format otherwise for mcregions.
	 * @return The filename.
	 */
	public String getFilename(boolean anvil) {
		
		return "r." + x + "." + z + ((anvil) ? ".mca" : ".mcr");
		
	}
	
 	@Override
 	public String toString() {
 		
 		return "(" + x + "," + z + ")";
 		
 	}
 	
 }

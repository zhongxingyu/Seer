 package org.SkyCraft.Coliseum.Arena.Region;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.util.Vector;
 
 public abstract class Region {
 
 	private Vector pos1;
 	private Vector pos2;
 	
 	//TODO implement checks to ensure no "flat" regions
 
 	Region() {
 		pos1 = new Vector();
 		pos2 = new Vector();
 	}
 	
 	public void setPos1(Block block) {
 		pos1.setX(block.getX());
 		pos1.setY(block.getY());
 		pos1.setZ(block.getZ());
 	}
 
 	public void setPos1(int x, int y, int z) {
 		pos1.setX(x);
 		pos1.setY(y);
 		pos1.setZ(z);
 	}
 	
 	public void setPos2(Block block) {
 		pos2.setX(block.getX());
 		pos2.setY(block.getY());
 		pos2.setZ(block.getZ());
 	}
 	
 	public void setPos2(int x, int y, int z) {
 		pos2.setX(x);
 		pos2.setY(y);
 		pos2.setZ(z);
 	}
 	
	public Vector getPos1() {
		return pos1;
	}
	
	public Vector getPos2() {
		return pos2;
	}
	
 	protected boolean isCompleteRegion() {
 		if(pos1 == null || pos2 == null) {
 			return false;
 		}
 		return true;
 	}
 	
	public boolean isBlockContained(Location loc) {
 		if(isBetween(pos1.getBlockX(), pos2.getBlockX(), loc.getBlockX()) && 
				isYBetween(pos1.getBlockY(), pos2.getBlockY(), loc.getBlockY()) && 
 				isBetween(pos1.getBlockZ(), pos2.getBlockZ(), loc.getBlockZ())) {
 			return true;
 		}
 		
 		return false;
 	}
 
 	private boolean isBetween(int c1, int c2, int cB) {
 		if(c1 > c2) {
 			if(cB < c1 && cB > c2) {
 				return true;
 			}
 		}
 		else {
 			if(cB < c2 && cB > c1) {
 				return true;
 			}
 		}
 		return false;
 	}
	
	private boolean isYBetween(int c1, int c2, int cB) {
		if(c1 > c2) {
			if(cB < c1 && cB > c2 - 1) {
				return true;
			}
		}
		else {
			if(cB < c2 && cB > c1 - 1) {
				return true;
			}
		}
		return false;
	}
 }

 package com.wolvencraft.prison.region;
 
 import java.sql.Timestamp;
 
 import org.bukkit.Location;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 
 import com.wolvencraft.prison.util.Message;
 import com.wolvencraft.prison.util.Util;
 
 public class PrisonSelection {
 	private Location pos1, pos2;
	private Player player;
 	private Timestamp timestamp;
 	
 	public PrisonSelection(Player player) {
 		Message.debug("Creating a new PrisonSelection for " + player.getDisplayName());
 		pos1 = null;
 		pos2 = null;
		this.player = player;
 		timestamp = Util.getCurrentTime();
 	}
 	
 	public Location getPos1() { return pos1; }
 	public Location getPos2() { return pos2; }
	public Player getPlayer() { return player; }
 	public Timestamp getTimestamp() { return timestamp; }
 	
 	public int getBlockCount() {
 		if(pos1 == null || pos2 == null) return -1;
 		Location[] loc = getSortedCoordinates();
 		int xdist = Math.round(1 + loc[1].getBlockX() - loc[0].getBlockX());
 		int ydist = Math.round(1 + loc[1].getBlockY() - loc[0].getBlockY());
 		int zdist = Math.round(1 + loc[1].getBlockZ() - loc[0].getBlockZ());
 		return (xdist * ydist * zdist);
 	}
 	
 	public void setFirstCoordinate(Location loc) {
 		pos1 = loc.clone();
 		timestamp = Util.getCurrentTime();
 	}
 	
 	public void setSecondCoordinate(Location loc) {
 		pos2 = loc.clone();
 		timestamp = Util.getCurrentTime();
 	}
 	
 	public void setCoordinates(Location[] loc) {
 		pos1 = loc[0].clone();
 		pos2 = loc[1].clone();
 		timestamp = Util.getCurrentTime();
 	}
 	
 	public void expand(BlockFace direction, int amount) {
 		if(direction.equals(BlockFace.UP)) this.getLargestY(false).setY(getLargestY(false).getY() + amount);
 		else if(direction.equals(BlockFace.DOWN)) this.getLargestY(true).setY(getLargestY(true).getY() - amount);
 		else if(direction.equals(BlockFace.EAST)) this.getLargestX(false).setX(getLargestX(false).getX() + amount);
 		else if(direction.equals(BlockFace.WEST)) this.getLargestX(true).setX(getLargestY(true).getX() - amount);
 		else if(direction.equals(BlockFace.SOUTH)) this.getLargestZ(false).setZ(getLargestZ(false).getZ() + amount);
 		else if(direction.equals(BlockFace.NORTH)) this.getLargestZ(true).setZ(getLargestZ(true).getZ() - amount);
 	}
 	
 	public void expand(BlockFace direction, int amount, int amount2) {
 		if(direction.equals(BlockFace.UP)) {
 			this.getLargestY(false).setY(getLargestY(false).getY() + amount);
 			this.getLargestY(true).setY(getLargestY(true).getY() - amount2);
 		}
 		else if(direction.equals(BlockFace.DOWN)) {
 			this.getLargestY(true).setY(getLargestY(true).getY() - amount);
 			this.getLargestY(false).setY(getLargestY(false).getY() + amount2);
 		}
 		else if(direction.equals(BlockFace.EAST)) {
 			this.getLargestX(false).setX(getLargestX(false).getX() + amount);
 			this.getLargestX(true).setX(getLargestY(true).getX() - amount2);
 		}
 		else if(direction.equals(BlockFace.WEST)) {
 			this.getLargestX(true).setX(getLargestY(true).getX() - amount);
 			this.getLargestX(false).setX(getLargestX(false).getX() + amount2);
 		}
 		else if(direction.equals(BlockFace.SOUTH)) {
 			this.getLargestZ(false).setZ(getLargestZ(false).getZ() + amount);
 			this.getLargestZ(true).setZ(getLargestZ(true).getZ() - amount2);
 		}
 		else if(direction.equals(BlockFace.NORTH)) {
 			this.getLargestZ(true).setZ(getLargestZ(true).getZ() - amount);
 			this.getLargestZ(false).setZ(getLargestZ(false).getZ() + amount2);
 		}
 	}
 	
 	public void shift(BlockFace direction, int amount) {
 		if(direction.equals(BlockFace.UP)) {
 			this.getLargestY(false).setY(getLargestY(false).getY() + amount);
 			this.getLargestY(true).setY(getLargestY(true).getY() + amount);
 		}
 		else if(direction.equals(BlockFace.DOWN)) {
 			this.getLargestY(true).setY(getLargestY(true).getY() - amount);
 			this.getLargestY(false).setY(getLargestY(false).getY() - amount);
 		}
 		else if(direction.equals(BlockFace.EAST)) {
 			this.getLargestX(false).setX(getLargestX(false).getX() + amount);
 			this.getLargestX(true).setX(getLargestY(true).getX() + amount);
 		}
 		else if(direction.equals(BlockFace.WEST)) {
 			this.getLargestX(true).setX(getLargestY(true).getX() - amount);
 			this.getLargestX(false).setX(getLargestX(false).getX() - amount);
 		}
 		else if(direction.equals(BlockFace.SOUTH)) {
 			this.getLargestZ(false).setZ(getLargestZ(false).getZ() + amount);
 			this.getLargestZ(true).setZ(getLargestZ(true).getZ() + amount);
 		}
 		else if(direction.equals(BlockFace.NORTH)) {
 			this.getLargestZ(true).setZ(getLargestZ(true).getZ() - amount);
 			this.getLargestZ(false).setZ(getLargestZ(false).getZ() - amount);
 		}
 	}
 	
 	public boolean locationsSet() {
 		if(pos1 == null || pos2 == null) return false;
 		return true;
 	}
 	
 	private Location[] getSortedCoordinates() {
 		Location[] loc = {pos1.clone(), pos2.clone()};
 		
 		if(pos1.getBlockX() > pos2.getBlockX()) {
 			loc[0].setX(pos2.getBlockX());
 			loc[1].setX(pos1.getBlockX());
 		}
 		
 		if(pos1.getBlockY() > pos2.getBlockY()) {
 			loc[0].setY(pos2.getBlockY());
 			loc[1].setY(pos1.getBlockY());
 		}
 		
 		if(pos1.getBlockZ() > pos2.getBlockZ()) {
 			loc[0].setZ(pos2.getBlockZ());
 			loc[1].setZ(pos1.getBlockZ());
 		}
 		
 		return loc;
 	}
 	
 	private Location getLargestX(boolean invert) {
 		if(!invert) {
 			if(pos1.getBlockX() > pos2.getBlockX()) return pos1;
 			else return pos2;
 		} else {
 			if(pos1.getBlockX() < pos2.getBlockX()) return pos1;
 			else return pos2;
 		}
 	}
 	
 	private Location getLargestY(boolean invert) {
 		if(!invert) {
 			if(pos1.getBlockY() > pos2.getBlockY()) return pos1;
 			else return pos2;
 		} else {
 			if(pos1.getBlockY() < pos2.getBlockY()) return pos1;
 			else return pos2;
 		}
 	}
 	
 	private Location getLargestZ(boolean invert) {
 		if(!invert) {
 			if(pos1.getBlockZ() > pos2.getBlockZ()) return pos1;
 			else return pos2;
 		} else {
 			if(pos1.getBlockZ() < pos2.getBlockZ()) return pos1;
 			else return pos2;
 		}
 	}
 }

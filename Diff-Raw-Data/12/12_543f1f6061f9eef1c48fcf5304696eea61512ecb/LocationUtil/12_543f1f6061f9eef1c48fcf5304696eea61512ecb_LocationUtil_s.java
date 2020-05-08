 /**
  * LICENSING
  * 
  * This software is copyright by sunkid <sunkid@iminurnetz.com> and is
  * distributed under a dual license:
  * 
  * Non-Commercial Use:
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Commercial Use:
  *    Please contact sunkid@iminurnetz.com
  */
 package com.iminurnetz.bukkit.util;
 
 import java.util.Formatter;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 
 public class LocationUtil {
 	public static String getSimpleLocation(Location loc) {
 		StringBuilder sb = new StringBuilder();
 		Formatter formatter = new Formatter(sb);
 		formatter.format("%4d x %4d x %4d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() );
 		return sb.toString();
 	}
 
 	public static String getSimpleLocation(Block block) {
 		return getSimpleLocation(block.getLocation());
 	}
 
 	public static boolean isSameLocation(Player player, Block block) {
 		return isSameLocation(block, player.getLocation().getBlock(), player.getLocation().getBlock().getRelative(BlockFace.UP));
 	}
 
 	/**
 	 * Check if any of the Block instances b overlap with a block.
 	 * @param block the block to compare to
 	 * @param b the set of Block instances to check
 	 * @return true if any of the Block instances b overlap with the Block instance block
 	 */
 	public static boolean isSameLocation(Block block, Block... b) {
 		if (block == null) {
 			throw new IllegalArgumentException("none of the blocks can be null");
 		}
 		for (Block bI : b) {
 			if (bI == null) {
 				throw new IllegalArgumentException("none of the blocks can be null");
 			}
 			
 			if (block.getX() == bI.getX() && block.getY() == bI.getY() && block.getZ() == bI.getZ()) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Return the face of a block when viewing the block at location b from location a.
 	 * @param player the point one is viewing from
 	 * @param targetedBlock the point looked at
 	 * @return the BlockFace looked at
 	 */
 	public static BlockFace getFace(Player player, Block targetedBlock) {
 		// TODO fix me please!
 		Location playerLoc = player.getEyeLocation();
 		double x = targetedBlock.getX() - playerLoc.getX();
 		double z = targetedBlock.getZ() - playerLoc.getZ();
 		
 		if (x == 0 && z == 0) {
 			if (targetedBlock.getY() > playerLoc.getY()) {
 				return BlockFace.DOWN;
 			}
 			
 			return BlockFace.UP;
 		} else if (Math.abs(x) == Math.abs(z)) {
 			if (x > 0 && z > 0) {
 				return BlockFace.NORTH_EAST;
 			} else if (x > 0 && z < 0) {
 				return BlockFace.SOUTH_EAST;
 			} else if (x < 0 && z > 0) {
 				return BlockFace.NORTH_WEST;
 			} else if (x < 0 && z < 0) {
 				return BlockFace.SOUTH_WEST;
 			}
 		}
 		int direction = (int) Math.floor(Math.atan2(x, z)*2/Math.PI + .5);
 		
 		System.err.println("x: " + x + " y: " + z + " " + Math.atan2(z, x) + " " + direction);
 
 		switch(direction) {
 			case 0:
 				return BlockFace.NORTH;
 			case 1:
 				return BlockFace.EAST;
 			case 2:
 				return BlockFace.SOUTH;
 			case -1:
 				return BlockFace.WEST;
 		}
 
 		return null;
 	}
 	
 	/**
 	 * Returns the direction a location's yaw is pointing toward
 	 * @param loc the location
 	 * @return a BlockFace corresponding to the Location's yaw
 	 */
 	public static BlockFace getDirection(Location loc) {
	    int degrees = Math.abs(365%((int)loc.getYaw()));
	    int n = (int) Math.round(degrees/11.25);
 	    
 	    switch (n) {
 	    case 0:
 	    default:
 	        return BlockFace.WEST;
 	    case 1:
 	    case 2:
 	        return BlockFace.NORTH_WEST;
 	    case 3:
 	    case 4:
 	        return BlockFace.NORTH;
 	    case 5:
 	    case 6:
 	        return BlockFace.NORTH_EAST;
 	    case 7:
 	    case 8:
 	        return BlockFace.EAST;
 	    case 9:
 	    case 10:
 	        return BlockFace.SOUTH_EAST;
 	    case 11:
 	    case 12:
 	        return BlockFace.SOUTH;
 	    case 13:
 	    case 14:
 	        return BlockFace.SOUTH_WEST;
 	    }
 	}
 }

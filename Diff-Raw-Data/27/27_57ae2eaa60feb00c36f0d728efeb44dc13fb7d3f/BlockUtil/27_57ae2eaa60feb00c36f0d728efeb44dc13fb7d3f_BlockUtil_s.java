 /*
  * This file is part of Aqualock.
  *
  * Copyright (c) 2012, AlmuraDev <http://www.almuramc.com/>
  * Aqualock is licensed under the Almura Development License.
  *
  * Aqualock is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * As an exception, all classes which do not reference GPL licensed code
  * are hereby licensed under the GNU Lesser Public License, as described
  * in Almura Development License.
  *
  * Aqualock is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License,
  * the GNU Lesser Public License (for classes that fulfill the exception)
  * and the Almura Development License along with this program. If not, see
  * <http://www.gnu.org/licenses/> for the GNU General Public License and
  * the GNU Lesser Public License.
  */
 package com.almuramc.aqualock.bukkit.util;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.material.Door;
 import org.bukkit.material.MaterialData;
 
 /**
  * Class with helper functions that deal with blocks
  */
 public class BlockUtil {
 	/**
 	 * Gets the immediate block found within the distance of the player's line of sight
 	 * @param player The player looking
 	 * @param distance The distance to search
 	 * @return The block found or null if no valid blocks found
 	 */
 	public static Block getTarget(Player player, HashSet<Byte> idsToIgnore, int distance) {
 		List<Block> blocks = player.getLineOfSight(idsToIgnore, distance);
 		if (blocks == null || blocks.isEmpty()) {
 			return null;
 		}
 		return blocks.get(0);
 	}
 
 	public static boolean isDoubleDoor(Block block) {
 		Class<? extends MaterialData> clazz = block.getType().getData();
		System.out.println(clazz.toString());
 		if (!clazz.isAssignableFrom(Door.class)) {
 			return false;
 		}
 		return isDoubleDoorRelativeLeft(block) || isDoubleDoorRelativeRight(block);
 	}
 
 	private static boolean isDoubleDoorRelativeRight(Block block) {
		Class<? extends MaterialData> clazz = block.getType().getData();
 		if (!clazz.isAssignableFrom(Door.class)) {
 			return false;
 		}
		Block right = block.getRelative(BlockFace.WEST);
		boolean isDouble = false;
		clazz = right.getType().getData();
		if (clazz.equals(Door.class) || clazz.getSuperclass().equals(Door.class)) {
			isDouble = true;
		}
		return isDouble;
 	}
 
 	private static boolean isDoubleDoorRelativeLeft(Block block) {
		Class<? extends MaterialData> clazz = block.getType().getData();
 		if (!clazz.isAssignableFrom(Door.class)) {
 			return false;
 		}
		Block left = block.getRelative(BlockFace.EAST);
		boolean isDouble = false;
		clazz = left.getType().getData();
		if (clazz.equals(Door.class) || clazz.getSuperclass().equals(Door.class)) {
			isDouble = true;
		}
		return isDouble;
 	}
 
 	public static List<Block> getDoubleDoor(Block block) {
 		ArrayList<Block> doubleDoor = new ArrayList<Block>();
 		if (isDoubleDoorRelativeLeft(block)) {
 			doubleDoor.add(block);
 			doubleDoor.add(block.getRelative(BlockFace.WEST));
 		} else if (isDoubleDoorRelativeRight(block)) {
 			doubleDoor.add(block.getRelative(BlockFace.EAST));
 			doubleDoor.add(block);
 		} else {
 			return null;
 		}
 		return doubleDoor;
 	}
 
 	public static void toggleDoubleDoor(Block block, boolean open) {
 		if (!isDoubleDoor(block)) {
 			return;
 		}
 		List<Block> doubleDoor = getDoubleDoor(block);
 		if (doubleDoor == null) {
 			return;
 		}
 		for (Block door : doubleDoor) {
 			((Door) door).setOpen(open);
 		}
 	}
 }

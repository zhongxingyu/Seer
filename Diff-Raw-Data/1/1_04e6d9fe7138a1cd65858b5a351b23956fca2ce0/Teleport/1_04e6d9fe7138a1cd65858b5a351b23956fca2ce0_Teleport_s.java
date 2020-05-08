 /**
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer
  * in the documentation and/or other materials provided with the
  * distribution.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  */
 /**
  * 
  */
 package com.andune.minecraft.commonlib;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.morganm.mBukkitLib.Logger;
 
 /** General purpose teleportation routines.
  * 
  * @author morganm
  *
  */
 @Singleton
 public class Teleport {
 	public static final int FLAG_NO_WATER = 0x01; 
 	public static final int FLAG_NO_LILY_PAD = 0x02; 
 	public static final int FLAG_NO_LEAVES = 0x04; 
 	public static final int FLAG_NO_ICE = 0x08; 
 	
 	private static final int[] safeIds = new int[256];
 	
 	/*
 	private final static BlockFace[] directions = new BlockFace[] {
 		BlockFace.UP,
 		BlockFace.NORTH,
 		BlockFace.WEST,
 		BlockFace.SOUTH,
 		BlockFace.EAST,
 		BlockFace.DOWN
 	};
 	*/
 	
 	private final Logger log ;
 	private String currentTeleporter;
 	
 	static {
 		// initialize all to 0
 		for(int i=0; i < 256; i++)
 			safeIds[i] = 0;
 		
 		// now set to 1 those ids we consider safe
 		safeIds[Material.AIR.getId()] = 1;
 		safeIds[Material.YELLOW_FLOWER.getId()] = 1;
 		safeIds[Material.RED_ROSE.getId()] = 1;
 		safeIds[Material.BROWN_MUSHROOM.getId()] = 1;
 		safeIds[Material.RED_MUSHROOM.getId()] = 1;
 		safeIds[Material.LONG_GRASS.getId()] = 1;
 		safeIds[Material.DEAD_BUSH.getId()] = 1;
 		safeIds[Material.CROPS.getId()] = 1;
 		safeIds[Material.TORCH.getId()] = 1;
 		safeIds[Material.REDSTONE_WIRE.getId()] = 1;
 		safeIds[Material.REDSTONE_TORCH_ON.getId()] = 1;
 		safeIds[Material.REDSTONE_TORCH_OFF.getId()] = 1;
 		safeIds[Material.REDSTONE_LAMP_OFF.getId()] = 1;
 		safeIds[Material.REDSTONE_LAMP_ON.getId()] = 1;
 		safeIds[Material.WOOD_PLATE.getId()] = 1;
 		safeIds[Material.DIODE_BLOCK_ON.getId()] = 1;
 		safeIds[Material.DIODE_BLOCK_OFF.getId()] = 1;
 		safeIds[Material.POWERED_RAIL.getId()] = 1;
 		safeIds[Material.DETECTOR_RAIL.getId()] = 1;
 		safeIds[Material.RAILS.getId()] = 1;
 		safeIds[Material.STEP.getId()] = 1;		// half slab
 		safeIds[Material.SNOW.getId()] = 1;
 		safeIds[Material.SIGN_POST.getId()] = 1;
 		safeIds[Material.WALL_SIGN.getId()] = 1;
 	}
 	
 	@Inject
 	private Teleport(Logger log) {
 		this.log = log;
 	}
 	
 	public void setCurrentTeleporter(String name) {
 		this.currentTeleporter = name;
 	}
 	public String getCurrentTeleporter() { return currentTeleporter; }
 	
 	public boolean isSafeBlock(Block b, int flags) {
 		final Block up = b.getRelative(BlockFace.UP);
 		final Block down = b.getRelative(BlockFace.DOWN);
 		final int bTypeId = b.getTypeId();
 		final int upTypeId = up.getTypeId();
 		final int downTypeId = down.getTypeId();
 		
 		if( safeIds[bTypeId] == 1 && safeIds[upTypeId] == 1			// block & UP are safe?
 				&& (downTypeId != 10 && down.getTypeId() != 11)		// no lava underneath
 				&& (downTypeId != 0)								// no air underneath
 				&& (downTypeId != Material.FIRE.getId())			// not fire below
 				&& (downTypeId != Material.VINE.getId())			// not vine below, results in possible unsafe teleports (over lava, water, etc)
 			)
 		{
 			// check for water
 			if( (flags & FLAG_NO_WATER) > 0 ) {
 				if( bTypeId == Material.WATER.getId()
 						|| bTypeId == Material.STATIONARY_WATER.getId()
 						|| downTypeId == Material.WATER.getId()
 						|| downTypeId == Material.STATIONARY_WATER.getId() )
 				{
 					return false;
 				}
 			}
 			
 			// check for lily pads
 			if( (flags & FLAG_NO_LILY_PAD) > 0 && downTypeId == Material.WATER_LILY.getId() ) {
 				return false;
 			}
 			
 			// check for leaves
 			if( (flags & FLAG_NO_LEAVES) > 0 ) {
 				if( downTypeId == Material.LEAVES.getId() )
 					return false;
 				
 				// we check 2 blocks down for FLAG_NO_LEAVES, because often snow
 				// is on top of leaves, so we need to check 1 block lower
 				final Block down2 = down.getRelative(BlockFace.DOWN);
 				if( down2.getTypeId() == Material.LEAVES.getId() )
 					return false;
 			}
 			
 			// check for ice
 			if( (flags & FLAG_NO_ICE) > 0 && downTypeId == Material.ICE.getId() ) {
 				return false;
 			}
 
 			log.devDebug("isSafeBlock() block is safe b=",b,", up=",up,", down=",down);
 			
 			// if we make it here, we've made it through all hazardous checks
 			// so the block is considered safe to teleport to
 			return true;
 		}
 		else
 			return false;
 		
 	}
 	
 	private Location findSafeLocation2(final Location baseLocation, final int level,
 			final Bounds bounds, final int flags)
 	{
 		log.devDebug("findSafeLocation2(): level=",level,", baseLocation=",baseLocation,", flags=",flags);
 		final World w = baseLocation.getWorld();
 		final int baseX = baseLocation.getBlockX();
 		final int baseY = baseLocation.getBlockY();
 		final int baseZ = baseLocation.getBlockZ();
 		
 		int minX = baseX - level;
 		int maxX = baseX + level;
 		int minY = baseY - level;
 		int maxY = baseY + level;
 		int minZ = baseZ - level;
 		int maxZ = baseZ + level;
 		
 		if( minY < bounds.minY )
 			minY = bounds.minY;
 		if( maxY > bounds.maxY )
 			maxY = bounds.maxY;
 		
 		log.devDebug("findSafeLocation2(): bounds.maxY=",bounds.maxY,", bounds.minY=",bounds.minY);
 		log.devDebug("findSafeLocation2(): maxY=",maxY,", minY=",minY);
 		
 		long startTime = System.currentTimeMillis();
 		int checkedBlocks=0;
 		for(int x = maxX; x >= minX; x--) {
 			for(int y=maxY; y >= minY; y--) {
 				for(int z=maxZ; z >= minZ; z--) {
 					// we only check the level that we're at, at least one
 					// of the axis must be at the current level
 					if( x != maxX && x != minX
 							&& y != maxY && y != minY 
 							&& z != maxZ && z != minZ )
 						continue;
 					
 					Block b = w.getBlockAt(x, y, z);
 					if( isSafeBlock(b, flags) ) {
 						// if it's the original location, return it untouched so we preserve
 						// the exact coordinates, pitch, yaw, etc
 						if( level == 0 && x == 0 && y == 0 && z == 0 ) {
 							log.devDebug("findSafeLocation2(): found safe block (original location) ",b);
 							return baseLocation;
 						}
 						else {
 							log.devDebug("findSafeLocation2(): found safe block ",b);
 							return b.getLocation();
 						}
 					}
 					checkedBlocks++;
 				}
 			}
 		}
 		
 		long totalTime = System.currentTimeMillis() - startTime;
 		log.devDebug("findSafeLocation2(): no safe location found at level ",level,", checked ",checkedBlocks," total blocks. Recursing to next level. (total time = ",totalTime,")");
 		
 		// we only recurse so far before we give up
 		if( level+1 > bounds.maxRange ) {
 			// check the highest Block at the given X/Z; if it's higher than the maxY
 			// we've checked, then try there. This has the effect that if we try to
 			// spawn deep into solid blocks and can't find a safe place, we'll spawn
 			// at the top Y, much like the vanilla MC algorithm.
 			// also note we check if it's lower than minY, because it's possible we
 			// originally received a teleport request high up in the middle of the
 			// sky, so we'll want to try again at the highest block level.
 			// the mixY/minY checks prevent this from being infinitely recursive,
 			// since this can only be true once.
 	    	Location highest = w.getHighestBlockAt(baseLocation).getLocation();
 	    	if( highest.getY() > maxY || highest.getY() < minY ) {
 				log.devDebug("findSafeLocation2(): hit maximum recursion distance ",bounds.maxRange,", moving to highest Y-block at ",highest.getY()," and trying again");
 				return findSafeLocation2(highest, 0, bounds, flags);
 	    	}
 			log.devDebug("findSafeLocation2(): hit maximum recursion distance ",bounds.maxRange,", returning null");
 			return null;
 		}
 		
 		return findSafeLocation2(baseLocation, level+1, bounds, flags);
 	}
 	
 	/** Recursively look for 2 vertical safe air spots nearest the given location.
 	 * 
 	 * @param base
 	 */
 	/*
 	private Location findSafeLocation(final Set<Location> alreadyTraversed, final int level, final Location location) {
 		log.devDebug("findSafeLocation(): level=",level,", location=",location);
 
 		// okTraverse tracks block that we check at this level that it's OK
 		// to traverse into for this pass.
 		ArrayList<Location> okTraverse = new ArrayList<Location>(3);
 		
 		if( location == null )
 			return null;
 		final Block base = location.getBlock();
 
 		final boolean blockAlreadyTraversed = alreadyTraversed.contains(location);
 		log.devDebug("findSafeLocation(): blockAlreadyTraversed=",blockAlreadyTraversed);
 		
 		if( !blockAlreadyTraversed && isSafeBlock(base, 0) ) {
 			log.devDebug("findSafeLocation(): isSafeBlock(location)=true, returning");
 			return location;
 		}
 		
 		// first try all the closest blocks before recursing further
 		for(int i=0; i < directions.length; i++) {
 			Block tryBlock = base.getRelative(directions[i]);
 			Location tryLocation = tryBlock.getLocation();
 			if( alreadyTraversed.contains(tryLocation) ) {
 				continue;
 			}
 			okTraverse.add(tryLocation);
 			alreadyTraversed.add(tryLocation);
 
 			if( isSafeBlock(tryBlock, 0) ) {
 				log.devDebug("findSafeLocation(): found safeBlock at ",tryBlock);
 				return tryLocation;
 			}
 		}
 
 		// we only recurse so far before we give up
 		if( level > 10 ) {
 			log.devDebug("findSafeLocation(): hit maximum recursion, returning null");
 			return null;
 		}
 
 		// if we're here, none of them were safe, now recurse
 		for(int i=0; i < directions.length; i++) {
 			Location recurseLocation = base.getRelative(directions[i]).getLocation();
 			if( alreadyTraversed.contains(recurseLocation) && !okTraverse.contains(recurseLocation) )
 				continue;
 			alreadyTraversed.add(recurseLocation);
 
 			Location result = findSafeLocation(alreadyTraversed, level+1, recurseLocation);
 			if( result != null ) {
 				log.devDebug("findSafeLocation(): found safe location through recursion at ",result);
 				return result;
 			}
 		}
 		
 		return null;
 	}
 	*/
 	
 	/** Safely teleport a player to a location. Should avoid them being stuck in blocks,
 	 * teleported over lava, etc.
 	 * 
 	 * @param p
 	 * @param l
 	 */
 	public void safeTeleport(final Player p, final Location l, final TeleportCause cause) {
 		Location safeLocation = safeLocation(l);
 		if( safeLocation != null ) {
 			setCurrentTeleporter(p.getName());
 			p.teleport(safeLocation, cause);
 			setCurrentTeleporter(null);
 		}
 		else
 			log.info("safeTeleport: couldn't find safe location near location "+l);
 	}
 
 	/** Given a location, find the nearest "safe" location, ie. that won't suffocate a
 	 * player, spawn them over lava, etc.
 	 * 
 	 * @param l the location to start searching from
 	 * @param minY the minimum Y distance to check
 	 * @return the safe location or null if none could be found
 	 */
 	public Location safeLocation(Location l, Bounds bounds, int flags) {
 		if( bounds == null )
 			bounds = defaultBounds;
 		
 		Location target = findSafeLocation2(l, 0, bounds, flags);
 		
 		if( target != null ) {
 			if( !target.equals(l) ) {
 				// preserve pitch/yaw
 				target.setPitch(l.getPitch());
 				target.setYaw(l.getYaw());
 	
 				// adjust by 0.5 so we teleport to the middle of the block, not
 				// the edge
 //				if( target.getX() > 0 )
 					target.setX(target.getX()+0.5);
 //				else
 //					target.setX(target.getX()-0.5);
 				
 //				if( target.getZ() > 0 )
 					target.setZ(target.getZ()+0.5);
 //				else
 //					target.setZ(target.getZ()-0.5);
 				log.devDebug("adjusted coordinates to middle. x=",target.getX(),", z=",target.getZ());
 			}
 			else
 				log.devDebug("safeLocation(): original location is safe");
 		}
 		else
 			log.info("safeLocation: couldn't find nearby safe location, using original location "+l);
 		
 		log.debug("safeLocation(): target=",target);
 		return target;
 	}
 	
 	/** Given a location, find the nearest "safe" location, ie. that won't suffocate a
 	 * player, spawn them over lava, etc.
 	 * 
 	 * @param l
 	 */
 	public Location safeLocation(Location l) {
 		return safeLocation(l, defaultBounds, 0);
 	}
 
 	public Bounds getDefaultBounds() { return defaultBounds; }
 
 	private final Bounds defaultBounds = new Bounds();
 	public static class Bounds {
 		// min/max Y bounds, can be used to prevent safe location from being
 		// below or above a certain Y bound
 		public int minY=0;
 		public int maxY=255;
 		
 		// the range we check from the baseLocation. Checks get exponentially larger
 		// as the volume of the cube grows, so don't go too large (max probably
 		// 25 or so for reasonable performance).
 		public int maxRange=10;
 		
 		public String toString() {
 			return "minY="+minY
 				+", maxY="+maxY
 				+", maxRange="+maxRange;
 		}
 	}
 }

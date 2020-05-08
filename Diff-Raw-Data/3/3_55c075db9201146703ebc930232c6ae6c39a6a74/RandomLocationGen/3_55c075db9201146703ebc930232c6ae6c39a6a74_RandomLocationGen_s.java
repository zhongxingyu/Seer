 /*
  * Copyright 2013 Michael McKnight. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice, this list of
  *       conditions and the following disclaimer.
  *
  *    2. Redistributions in binary form must reproduce the above copyright notice, this list
  *       of conditions and the following disclaimer in the documentation and/or other materials
  *       provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation are those of the
  * authors and contributors and should not be interpreted as representing official policies,
  * either expressed or implied, of anybody else.
  */
 
 package com.forgenz.mobmanager.common.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChunkSnapshot;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 
 import com.forgenz.mobmanager.P;
 
 public class RandomLocationGen
 {
 	private static List<Integer> cacheList;
 	
 	/**
 	 * Stop instances of the class from being created
 	 */
 	private RandomLocationGen() {}
 
 	/**
 	 * Generates a random location around the center location
 	 */
 	public static Location getLocation(boolean circle, Location center, int range, int minRange, int heightRange)
 	{
 		return getLocation(circle, center, range, minRange, heightRange, LocationCache.getCachedLocation());
 	}
 	
 	/**
 	 * Generates a random location around the center location
 	 */
 	public static Location getLocation(boolean circle, Location center, int range, int minRange, int heightRange, Location cacheLoc)
 	{
 		return getLocation(circle, false, center, range, minRange, heightRange, cacheLoc);
 	}
 	
 	/**
 	 * Generates a random location around the center location
 	 */
 	public static Location getLocation(boolean circle, boolean checkPlayers, Location center, int range, int minRange, int heightRange, Location cacheLoc)
 	{
 		return getLocation(circle, false, 5, center, range, minRange, heightRange, cacheLoc);
 	}
 	
 	/**
 	 * Generates a random location around the center location
 	 */
 	public static Location getLocation(boolean circle, boolean checkPlayers, int spawnAttempts, Location center, int range, int minRange, int heightRange, Location cacheLoc)
 	{
 		// Make sure the centers world is valid
 		if (center.getWorld() == null)
 		{
 			P.p().getLogger().warning("Null world passed to location generator");
 			return center;
 		}
 		
 		// Make sure range is larger than minRange
 		if (range < minRange)
 		{
 			range = range ^ minRange;
 			minRange = range ^ minRange;
 			range = range ^ minRange;
 		}
 		
 		// Height range must be at least 1
 		if (heightRange < 0)
 			heightRange = 1;
 		
 		// Make sure range is bigger than minRange
 		if (range == minRange)
 			++range;
 
 		// Calculate the total (up/down) range of heightRange
 		int heightRange2 = heightRange << 1;
 		
 		// Copy the world
 		cacheLoc.setWorld(center.getWorld());
 		
 		// Make X attempts to find a safe spawning location
 		for (int i = 0; i < spawnAttempts; ++i)
 		{
 			// Generate the appropriate type of location
 			if (circle)
 			{
 				getCircularLocation(center, range, minRange, cacheLoc);
 			}
 			else
 			{
 				getSquareLocation(center, range, minRange, cacheLoc);
 			}
 			
 			// Generate coordinates for Y
 			cacheLoc.setY(RandomUtil.i.nextInt(heightRange2) - heightRange + center.getBlockY() + 0.5);
 				
 			// If the location is safe we can return the location
 			if ((!checkPlayers || !PlayerFinder.playerNear(cacheLoc, minRange, heightRange)) && findSafeY(cacheLoc, center.getBlockY(), heightRange))
 			{
 				// Generate a random Yaw/Pitch
 				cacheLoc.setYaw(RandomUtil.i.nextFloat() * 360.0F);
//				cacheLoc.setPitch(-10.0F + RandomUtil.i.nextFloat() * 20.0F);
				cacheLoc.setPitch(90.0F);
 				return cacheLoc;
 			}
 		}
 		
 		// If no safe location was found in a reasonable time frame just return the center
 		return center;
 	}
 	
 	/**
 	 * Finds a safe Y location at the given x/z location
 	 * @return true if a safe location was found
 	 */
 	public static boolean findSafeY(Location location, int centerY, int heightRange)
 	{
 		List<Integer> list = Bukkit.isPrimaryThread() ? cacheList : new ArrayList<Integer>();
 		if (list == null)
 			cacheList = list = new ArrayList<Integer>();
 		return findSafeY(location, centerY, heightRange, list);
 	}
 	
 	/**
 	 * Finds a safe Y location at the given x/z location
 	 * @return true if a safe location was found
 	 */
 	public static boolean findSafeY(Location location, int centerY, int heightRange, List<Integer> cacheList)
 	{
 		int foundAir = 0;
 		
 		// Fetch max and min Y locations
 		int startY = centerY + heightRange;
 		int endY = centerY - heightRange;
 		
 		// Validate max and min Y locations
 		if (startY > 256)
 			startY = 256;
 		if (endY < 0)
 			endY = 0;
 		
 		World world = location.getWorld();
 		
 		if (!world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4))
 			return false;
 		
 		// Find sets of Y's which are safe
 		for (; startY > endY; --startY)
 		{
 			Block b = world.getBlockAt(location.getBlockX(), startY, location.getBlockZ());
 			if (isSafeMaterial(b.getType()))
 			{
 				++foundAir;
 			}
 			else
 			{
 				 if (foundAir >= 2)
 					 cacheList.add(startY + 1);
 				foundAir = 0;
 			}
 		}
 		
 		// If there are no safe locations we return false :(
 		if (cacheList.isEmpty())
 			return false;
 		
 		// Fetch a random location
 		location.setY(cacheList.get(RandomUtil.i.nextInt(cacheList.size())));		
 		cacheList.clear();
 		
 		return true;	
 	}
 	
 	/**
 	 * Finds a safe Y location at the given x/z location
 	 * Uses a chunk snapshot for speed
 	 * @return true if a safe location was found
 	 */
 	@SuppressWarnings("deprecation")
 	public static boolean findSafeY(ChunkSnapshot chunk, Location location, int centerY, int heightRange, List<Integer> cacheList)
 	{
 		int foundAir = 0;
 		
 		// Fetch max and min Y locations
 		int startY = centerY + heightRange;
 		int endY = centerY - heightRange;
 		
 		// Validate max and min Y locations
 		if (startY > 256)
 			startY = 256;
 		if (endY < 0)
 			endY = 0;
 		
 		int x = location.getBlockX() % 16, z = location.getBlockZ() % 16;
 		if (x < 0)
 			x += 16;
 		if (z < 0)
 			z += 16;
 		
 		// Find sets of Y's which are safe
 		for (; startY > endY; --startY)
 		{
 			if (isSafeMaterial(Material.getMaterial(chunk.getBlockTypeId(x, startY, z))))
 			{
 				++foundAir;
 			}
 			else
 			{
 				 if (foundAir >= 2)
 					 cacheList.add(startY + 1);
 				foundAir = 0;
 			}
 		}
 		
 		// If there are no safe locations we return false :(
 		if (cacheList.isEmpty())
 			return false;
 		
 		// Fetch a random location
 		location.setY(cacheList.get(RandomUtil.i.nextInt(cacheList.size())));		
 		cacheList.clear();
 		
 		return true;	
 	}
 	
 	/**
 	 * Generates a random location which is circular around the center
 	 */
 	public static Location getCircularLocation(Location center, int range, int minRange, Location cacheLoc)
 	{
 		return getCircularLocation(center.getBlockX(), center.getBlockZ(), range, minRange, cacheLoc);
 	}
 	
 	public static Location getCircularLocation(int centerX, int centerZ, double range, double minRange, Location cacheLoc)
 	{
 		// Calculate the difference between the max and min range
 		double rangeDiff = range - minRange;
 		// Calculate a random direction for the X/Z values
 		double theta = 2 * Math.PI * RandomUtil.i.nextDouble();
 		
 		// Generate a random radius
 		double radius = RandomUtil.i.nextDouble() * rangeDiff + minRange;
 		
 		// Set the X/Z coordinates
 		double trig = Math.cos(theta);
 		cacheLoc.setX(Location.locToBlock(radius * trig) + centerX + 0.5);
 		trig = Math.sin(theta);
 		cacheLoc.setZ(Location.locToBlock(radius * trig) + centerZ + 0.5);
 		
 		return cacheLoc;
 	}
 	
 	/**
 	 * Generates a random location which is square around the center
 	 */
 	public static Location getSquareLocation(Location center, int range, int minRange, Location cacheLoc)
 	{
 		return getSquareLocation(center.getBlockX(), center.getBlockZ(), range, minRange, cacheLoc);
 	}
 	
 	public static Location getSquareLocation(int centerX, int centerZ, int range, int minRange, Location cacheLoc)
 	{
 		// Calculate the sum of all the block deviations from the center between minRange and range
 		int totalBlockCount = (range * (++range) - minRange * (minRange + 1)) >> 1;
 		// Fetch a random number of blocks
 		int blockCount = totalBlockCount - RandomUtil.i.nextInt(totalBlockCount);
 		
 		// While the block deviation from the center for the given range is
 		// less than the number of blocks left we remove a layer of blocks
 		while (range < blockCount)
 			blockCount -= --range;
 		
 		// Pick a random location on the range line
 		int lineLoc = RandomUtil.i.nextInt(range << 1);
 		// Choose a line (North/East/West/South lines)
 		// Then set the X/Z coordinates
 		switch (RandomUtil.i.nextInt(4))
 		{
 		// East Line going North
 		case 0:
 			cacheLoc.setX(centerX + range + 0.5D);
 			cacheLoc.setZ(centerZ + range - lineLoc + 0.5D);
 			break;
 		// South Line going East
 		case 1:
 			cacheLoc.setX(centerX - range + lineLoc + 0.5D);
 			cacheLoc.setZ(centerZ + range + 0.5D);
 			break;
 		// West Line going South
 		case 2:
 			cacheLoc.setX(centerX - range + 0.5D);
 			cacheLoc.setZ(centerZ - range + lineLoc + 0.5D);
 			break;
 		// North Line going west
 		case 3:
 		default:
 			cacheLoc.setX(centerX + range - lineLoc + 0.5D);
 			cacheLoc.setZ(centerZ - range + 0.5D);
 		}
 		
 		return cacheLoc;
 	}
 	
 	/**
 	 * Checks if the block is of a type which is safe for spawning inside of
 	 * @return true if the block type is safe
 	 */
 	public static boolean isSafeBlock(Block block)
 	{
 		return isSafeMaterial(block.getType());
 	}
 	
 	public static boolean isSafeMaterial(Material mat)
 	{
 		switch (mat)
 		{
 		case AIR:
 		case WEB:
 		case VINE:
 		case SNOW:
 		case LONG_GRASS:
 		case DEAD_BUSH:
 		case SAPLING:
 			return true;
 		default:
 			return false;
 		}
 	}
 }

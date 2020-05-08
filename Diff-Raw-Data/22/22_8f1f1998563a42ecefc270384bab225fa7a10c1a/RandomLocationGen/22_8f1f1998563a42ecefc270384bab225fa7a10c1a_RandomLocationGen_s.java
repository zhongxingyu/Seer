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
 
 package com.forgenz.mobmanager.abilities.util;
 
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 
 import com.forgenz.mobmanager.P;
 
 public class RandomLocationGen
 {
 	private static Random rand = new Random();
 	private static Location loc = new Location(null, 0.0, 0.0, 0.0);
 	
 	private RandomLocationGen()
 	{
 		
 	}
 
 	public static Location getLocation(Location center, int range, int minRange, int heightRange)
 	{
 		Location loc = Bukkit.isPrimaryThread() ? RandomLocationGen.loc : new Location(null, 0.0, 0.0, 0.0);
 		return getLocation(center, range, minRange, heightRange, loc);
 	}
 	
 	public static Location getLocation(Location center, int range, int minRange, int heightRange, Location cacheLoc)
 	{
 		// Make sure the centers world is valid
 		if (center.getWorld() == null)
 		{
 			P.p().getLogger().warning("Null world passed to location generator");
 			return center;
 		}
 		
 		// If minRange is more than range, rather than throwing an error
 		// Just swap the values
 		if (range < minRange)
 		{
 			range = range ^ minRange;
 			minRange = range ^ minRange;
 			range = range ^ minRange;
 		}
 
 		// Calculate the variance in range required
		int range2 = (range << 1) - minRange;
 		// Calculate the total (up/down) range of heightRange
 		int heightRange2 = heightRange << 1;
 
 		// Copy the world
 		cacheLoc.setWorld(center.getWorld());
 		
 		// Make 10 attempts to find a safe spawning location
 		for (int i = 0; i < 10; ++i)
 		{			
 			// Generate coordinates for X/Z
 			// Fetches a random number, shifts the variance to the center
			int normalizedVariance = rand.nextInt(range2) - range;
			cacheLoc.setX(normalizedVariance + (-0 & normalizedVariance | 1) * minRange + center.getBlockX() + 0.5);
			normalizedVariance = rand.nextInt(range2) - range;
			cacheLoc.setZ(normalizedVariance + (-0 & normalizedVariance | 1) * minRange + center.getBlockZ() + 0.5);
 			
 			// Generate coordinates for Y
 			cacheLoc.setY(rand.nextInt(heightRange2) - heightRange + center.getBlockY() + 0.5);
 			
 			// Generate a random Yaw/Pitch
 			cacheLoc.setYaw(rand.nextFloat() * 360.0F);
 			
 			// Check the location is safe
			Block block = center.getBlock();
 			
 			// If the location is not safe we try again			
 			if (!(isSafe(block) && isSafe(block.getRelative(BlockFace.UP))))
 			{
 				continue;
 			}
 			
 			// Calculate the height diff
 			int heightDiff = Math.abs(cacheLoc.getBlockY() - center.getBlockY());
 			
 			boolean onGround = false;
 			// Move the position down as close to the ground as we can
 			while (heightDiff < heightRange)
 			{
 				block = block.getRelative(BlockFace.DOWN);
 				
 				// If the below block is empty we shift the location down
 				if (isSafe(block))
 				{
 					cacheLoc.setY(cacheLoc.getY() - 1.0D);
 					++heightDiff;
 				}
 				// If it isn't the mob is on the ground
 				else
 				{
 					onGround = true;
 					break;
 				}
 			}
 
 			// If the location is on or near the ground the location is good
 			if (onGround || !isSafe(block.getRelative(BlockFace.DOWN)) || !isSafe(block.getRelative(BlockFace.DOWN, 2)))
 			{
 				return cacheLoc;
 			}
 		}
 		
 		// If no safe location was found in a reasonable timeframe just return the center
 		return center;
 	}
 	
 	private static boolean isSafe(Block block)
 	{
 		Material mat = block.getType();
 		
 		switch (mat)
 		{
 		case AIR:
 		case WATER:
 		case STATIONARY_WATER:
 		case WEB:
 		case VINE:
 		case LONG_GRASS:
 		case DEAD_BUSH:
 		case SAPLING:
 			return true;
 		default:
 			return false;
 		}
 	}
 }

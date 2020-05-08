 /*
  *  ExperienceMod - Bukkit server plugin for modifying the experience system in Minecraft.
  *  Copyright (C) 2012 Kristian S. Stangeland
  *
  *  This program is free software; you can redistribute it and/or modify it under the terms of the 
  *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
  *  the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  *  See the GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License along with this program; 
  *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
  *  02111-1307 USA
  */
 
 package com.comphenix.xp.rewards.xp;
 
 import java.util.List;
 
 import org.apache.commons.lang.NullArgumentException;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import com.comphenix.xp.Configuration;
 import com.comphenix.xp.Server;
 import com.comphenix.xp.lookup.LevelingRate;
 import com.comphenix.xp.rewards.ResourceHolder;
 import com.comphenix.xp.rewards.ResourcesParser;
 import com.comphenix.xp.rewards.RewardService;
 import com.comphenix.xp.rewards.RewardTypes;
 
 /**
  * Rewards players with experience directly by simply adding the experience to their experience bar.
  * 
  * @author Kristian
  */
 public class RewardVirtual implements RewardService {
 
 	private double searchRadius = 20;
 	private LevelingRate levelingRate;
 	
 	private ResourcesParser parser = new ExperienceParser();
 	
 	@Override
 	public void reward(Player player, ResourceHolder resource) {
 		if (player == null)
 			throw new NullArgumentException("player");
 		if (!isExperience(resource))
 			throw new IllegalArgumentException("Must be a experience resource.");
 		
 		reward(player, null, resource);
 	}
 
 	@Override
 	public boolean canReward(Player player, ResourceHolder resource) {
 
 		if (player == null)
 			throw new NullArgumentException("player");
 		if (!isExperience(resource))
 			throw new IllegalArgumentException("Must be a experience resource.");
 
 		ExperienceManager manager = new ExperienceManager(player);
 		
 		// See if we'd end up with negative experience
 		if (resource.getAmount() < 0) {
 			return manager.hasExp(-resource.getAmount() * getLevelingFactor(player, manager));
 		} else {
 			return true;
 		}
 	}
 	
 	// Note: We ignore the location.
 	@Override
 	public void reward(Player player, Location point, ResourceHolder resource) {
 		if (player == null)
 			throw new NullArgumentException("player");
 		if (!isExperience(resource))
 			throw new IllegalArgumentException("Must be a experience resource.");
 
 		ExperienceManager manager = new ExperienceManager(player);
 		
 		// Rely on the brilliance of others
 		if (resource.getAmount() != 0) {
 			manager.changeExp(resource.getAmount() * getLevelingFactor(player, manager));
 		}
 	}
 
 	@Override
 	public void reward(World world, Location point, ResourceHolder resource) {
 		if (world == null)
 			throw new NullArgumentException("world");
 		if (point == null)
 			throw new NullArgumentException("point");
 		if (!isExperience(resource))
 			throw new IllegalArgumentException("Must be a experience resource.");
 
 		List<Player> closest = Server.getNearbyPlayers(world, point, searchRadius);
 		
 		// Give experience directly
 		if (closest.size() == 1)
 			reward(closest.get(0), null, resource);
 		else
 			// Spawn experience
 			Server.spawnExperience(world, point, resource.getAmount());
 	}
 	
 	private double getLevelingFactor(Player player, ExperienceManager manager) {
 		// Retrieve the desired amount of experience required to level up
		int desiredLevelUp = levelingRate.get(player.getLevel());
		int defaultLevelUp = manager.getXpNeededToLevelUp(player.getLevel());
 	
 		// Make experience drops correspond to the desired level rate
		return (double)defaultLevelUp / (double)desiredLevelUp;
 	}
 	
 	private boolean isExperience(ResourceHolder resource) { 
 		return resource instanceof ExperienceHolder;
 	}
 	
 	@Override
 	public ResourcesParser getResourcesParser() {
 		return parser;
 	}
 
 	public double getSearchRadius() {
 		return searchRadius;
 	}
 
 	public void setSearchRadius(double searchRadius) {
 		this.searchRadius = searchRadius;
 	}
 
 	public LevelingRate getLevelingRate() {
 		return levelingRate;
 	}
 
 	public void setLevelingRate(LevelingRate levelingRate) {
 		this.levelingRate = levelingRate;
 	}
 	
 	@Override
 	public RewardTypes getRewardType() {
 		return RewardTypes.VIRTUAL;
 	}
 
 	@Override
 	public String getServiceName() {
 		return getRewardType().name();
 	}
 
 	@Override
 	public RewardService clone(Configuration config) {
 		RewardVirtual copy = new RewardVirtual();
 		
 		copy.setLevelingRate(config.getLevelingRate());
 		copy.setSearchRadius(config.getScanRadiusSetting());
 		copy.parser = parser;
 		return copy;
 	}
 }

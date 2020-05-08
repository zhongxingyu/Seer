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
 
 package com.forgenz.mobmanager.world;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.Chunk;
 import org.bukkit.GameMode;
 import org.bukkit.World;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Tameable;
 
 import com.forgenz.mobmanager.Config;
 import com.forgenz.mobmanager.Config.WorldConf;
 import com.forgenz.mobmanager.MobType;
 import com.forgenz.mobmanager.P;
 
 /**
  * Keeps track of the number of mobs of different types within a world
  * 
  * @author Michael McKnight (ShadowDog007)
  *
  */
 public class MMWorld
 {	
 	private static long ticksPerRecount = P.cfg.getLong("TicksPerRecount", 40L);
 	/**
 	 * Bukkit world this object as affiliated with
 	 */
 	private final World world;
 	
 	/**
 	 * World settings
 	 */
 	public final WorldConf worldConf;
 	
 	/**
 	 * Loaded chunks in the world
 	 */
 	private ConcurrentHashMap<MMCoord, MMChunk> chunks;
 	/**
 	 * Count of loaded chunks in the world
 	 */
 	private int numChunks = 0;
 
 	/**
 	 * Used to check if we should update mob counts
 	 */
 	private boolean needsUpdate = true;
 	
 	
 	/**
 	 * Stores all mob counts
 	 */
 	private int[] mobCounts;
 	
 	
 	public MMWorld(final World world, WorldConf worldConf)
 	{
 		this.world = world;
 		this.worldConf = worldConf;
 		
 		mobCounts = new int[worldConf.maximums.length];
 
 		chunks = new ConcurrentHashMap<MMCoord, MMChunk>(0, 0.75F, 2);
 		
 		updateMobCounts();
 
 		final int maxMonsters = worldConf.maximums[MobType.MONSTER.index];
 		final int maxAnimals = worldConf.maximums[MobType.ANIMAL.index];
 		final int maxWater = worldConf.maximums[MobType.WATER_ANIMAL.index];
 		final int maxAmbient = worldConf.maximums[MobType.AMBIENT.index];
 		final int maxVillagers = worldConf.maximums[MobType.VILLAGER.index];
 
 		P.p.getLogger().info(String.format("[%s] Limits M:%d, A:%d, W:%d, Am:%d, V:%d", world.getName(), maxMonsters, maxAnimals, maxWater, maxAmbient, maxVillagers));
 	}
 	
 	public short getSearchDistance()
 	{
 		return worldConf.spawnChunkSearchDistance > 0 ? worldConf.spawnChunkSearchDistance : Config.spawnChunkSearchDistance;
 	}
 	
 	private void resetMobCounts()
 	{
 		for (MobType mob : MobType.values())
 		{
 			mobCounts[mob.index] = 0;
 		}
 	}
 	
 	public boolean updateMobCounts()
 	{
 		return updateMobCounts(null);
 	}
 	
 	/**
 	 * Used in the despawn task so an entity list does not need to be generated twice
 	 * @param entities - List of entities in the world
 	 */
 	public boolean updateMobCounts(List<LivingEntity> entities)
 	{
 		if (needsUpdate)
 		{
 			resetMobCounts();
 			
 			numChunks = world.getLoadedChunks().length;
 			
 			Iterator<MMChunk> it = chunks.values().iterator();
 			while (it.hasNext())
 			{
 				MMChunk chunk = it.next();
 				
 				// Check if the chunk is still required
 				if (!chunk.getChunk().isLoaded())
 				{
 					it.remove();
 					continue;
 				}
 				// Reset chunks counts
 				chunk.resetNumAnimals();
 				chunk.resetPlayers();
 				
 				for (MMLayer layer : chunk.getLayers())
 					layer.resetPlayers();
 			}
 			
 			// Fetches the list of entities if it was not given
 			if (entities == null)
 				entities = world.getLivingEntities();
 			
 			// Loop through each loaded chunk in the world
 			for (final LivingEntity entity : entities)
 			{
 				MMChunk mmchunk = getChunk(entity.getLocation().getChunk());
 				
 				// If the entity is a player update the layers and chunk
 				if (entity instanceof Player)
 				{
 					// Do not add players if they are in creative mode and 'ignoreCreativePlayers' is set
 					if (Config.ignoreCreativePlayers)
 					{
 						Player p = (Player) entity;
 						if (p.getGameMode() == GameMode.CREATIVE)
 							continue;
 					}
 
 
 					mmchunk.playerEntered();
 
 					for (MMLayer layersAt : mmchunk.getLayersAt(entity.getLocation().getBlockY()))
 						layersAt.playerEntered();
 
 					continue;
 				}
 
 				// Check if the mob should be ignored
 				if (Config.ignoredMobs.containsValue(entity.getType().toString()))
 					continue;
 
 				// Fetch mob type
 				MobType mob = MobType.valueOf(entity);
 				// If the mob type is null ignore the entity
 				if (mob == null)
 					continue;
 
 
 				if (mob == MobType.ANIMAL)
 				{
 					// Make sure tameable animals are not counted if it is set to false
 					if (!Config.countTamedAnimals && entity instanceof Tameable)
 					{
 						Tameable tameable = (Tameable) entity;
 
 						if (tameable.isTamed())
 							continue;
 					}
 					mmchunk.changeNumAnimals(true);
 				}
 
 				// Increment counter
 				++mobCounts[mob.index];
 			}
 			
 			// Reset 'updatedThisTick' so updates can be run again later
 			P.p.getServer().getScheduler().runTaskLater(P.p,
 				new Runnable()
 				{
 					public void run()
 					{
 						needsUpdate = true;
 					}
 				}
 				, ticksPerRecount);
 			
 			needsUpdate = false;
 			return true;
 		}
 		return false;
 	}
 
 	public World getWorld()
 	{
 		return world;
 	}
 
 	public Set<Map.Entry<MMCoord, MMChunk>> getChunks()
 	{
 		return chunks.entrySet();
 	}
 
 	public MMChunk getChunk(final Chunk chunk)
 	{
 		if (!chunk.isLoaded())
 			return null;
 
 		MMChunk mmchunk = getChunk(new MMCoord(chunk.getX(), chunk.getZ()));
 		
 		if (mmchunk == null)
 			return addChunk(chunk, false);
 		return mmchunk;
 	}
 
 	public MMChunk getChunk(final MMCoord coord)
 	{
 		if (coord == null)
 			return null;
 		return chunks.get(coord);
 	}
 
 	public MMChunk addChunk(final Chunk chunk, boolean incrementCount)
 	{
 		if (!chunk.isLoaded())
 			return null;
 		
 		MMChunk mmchunk = new MMChunk(chunk, this);
 
 		if (chunks.get(mmchunk.getCoord()) != null)
 		{
 			if (!Config.disableWarnings)
 				P.p.getLogger().warning("Newly loaded chunk already existed in chunk map");
 			return chunks.get(mmchunk.getCoord());
 		}
 		
 		chunks.put(mmchunk.getCoord(), mmchunk);
 		if (incrementCount)
 			++numChunks;
 		return mmchunk;
 	}
 
 	public void removeChunk(final Chunk chunk)
 	{
 		if (chunks.remove(new MMCoord(chunk.getX(), chunk.getZ())) == null)
 			return;
 
 		--numChunks;
 	}
 
 	public int getMobCount(MobType mob)
 	{
 		if (mob == null)
 			return 0;
 		
 		return mobCounts[mob.index];
 	}
 	
 	/**
 	 * Calculates the maximum number of monsters currently allowed in the world
 	 * @return The max number of monsters
 	 */
 	public short maxMobs(final MobType mob)
 	{
 		if (mob == null)
 			return Short.MAX_VALUE;
 
 		short dynMax = (short) (worldConf.dynMultis[mob.index] * numChunks >> 8);
 			
 		return worldConf.maximums[mob.index] < dynMax ? worldConf.maximums[mob.index] : dynMax;
 	}
 
 	public boolean withinMobLimit(MobType mob)
 	{
 		if (mob == null)
 			return true;
 		
 		return maxMobs(mob) > mobCounts[mob.index];
 	}
 
 	public void incrementMobCount(MobType mob)
 	{
 		if (mob == null)
 			return;
 		++mobCounts[mob.index];
 	}
 	
 	public void decrementMobCount(MobType mob)
 	{
 		if (mob == null)
 			return;
 		--mobCounts[mob.index];
 	}
 }

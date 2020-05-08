 package com.forgenz.mobmanager.world;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import org.bukkit.Chunk;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 
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
 	private HashMap<MMCoord, MMChunk> chunks;
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
 
 		chunks = new HashMap<MMCoord, MMChunk>();
 
 		// Store already loaded chunks
 		for (final Chunk chunk : world.getLoadedChunks())
 		{
 			final MMChunk mmchunk = new MMChunk(chunk, this);
 
 			chunks.put(mmchunk.getCoord(), mmchunk);
 			++numChunks;
 
 			// Counts the number of living entities
 			for (final Entity entity : chunk.getEntities())
 			{
 				// Fetch the creature type
 				MobType mob = MobType.valueOf(entity);
 				// If the creature type is null we ignore the entity
 				if (mob == null)
 					continue;
 					
 				++mobCounts[mob.index];
 			}
 		}
 
		final int maxMonsters = mobCounts[MobType.MONSTER.index];
		final int maxAnimals = mobCounts[MobType.ANIMAL.index];
		final int maxWater = mobCounts[MobType.WATER_ANIMAL.index];
		final int maxAmbient = mobCounts[MobType.AMBIENT.index];
		final int maxVillagers = mobCounts[MobType.VILLAGER.index];
 
 		P.p.getLogger().info(String.format("[%s] Limits M:%d, A:%d, W:%d, Am:%d, V:%d", world.getName(), maxMonsters, maxAnimals, maxWater, maxAmbient, maxVillagers));
 	}
 	
 	private void resetMobCounts()
 	{
 		for (MobType mob : MobType.getAll())
 		{
 			mobCounts[mob.index] = 0;
 		}
 	}
 
 	public boolean updateMobCounts()
 	{
 		if (needsUpdate)
 		{
 			resetMobCounts();
 
 			// Loop through each loaded chunk in the world
 			for (final Chunk chunk : world.getLoadedChunks())
 			{
 				final MMChunk mmchunk = getChunk(chunk);
 
 				mmchunk.resetNumAnimals();
 
 				// Loop through each entity in the chunk
 				for (final Entity entity : chunk.getEntities())
 				{
 					// Fetch mob type
 					MobType mob = MobType.valueOf(entity);
 					// If the mob type is null ignore the entity
 					if (mob == null)
 						continue;
 					
 					// Increment counter
 					++mobCounts[mob.index];
 				}
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
 
 		return getChunk(new MMCoord(chunk.getX(), chunk.getZ()));
 	}
 
 	public MMChunk getChunk(final MMCoord coord)
 	{
 		return chunks.get(coord);
 	}
 
 	public void addChunk(final Chunk chunk)
 	{
 		final MMChunk mmchunk = new MMChunk(chunk, this);
 
 		if (chunks.put(mmchunk.getCoord(), mmchunk) != null)
 		{
 			P.p.getLogger().warning("Newly loaded chunk already existed in chunk map");
 			return;
 		}
 
 		++numChunks;
 	}
 
 	public void removeChunk(final Chunk chunk)
 	{
 		if (chunks.remove(new MMCoord(chunk.getX(), chunk.getZ())) == null)
 		{
 			P.p.getLogger().warning("A chunk was unloaded but no object existed for it");
 			return;
 		}
 
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

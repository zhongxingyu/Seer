 /**
  * 
  */
 package org.morganm.mobreducer.manager;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 
 import javax.inject.Inject;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Animals;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Tameable;
 import org.morganm.mBukkitLib.Logger;
 import org.morganm.mobreducer.Config;
 import org.morganm.mobreducer.Util;
 
 /** Class that manages active mobs on the server, tracking information
  * about them for use by the rest of the plugin.
  * 
  * @author morganm
  *
  */
 public class MobManager implements Runnable {
 	private final HashMap<String, ChunkInfo> chunks = new HashMap<String, ChunkInfo>(100);
 	private final HashMap<UUID, EntityInfo> entities = new HashMap<UUID, EntityInfo>(500);
 	private final Logger log;
 	private final Util util;
 	private final Config config;
 	private final ChunkInfoFactory chunkInfoFactory;
 	private final EntityInfoFactory entityInfoFactory;
 	private int entitySpawnCounter=0;  // debug counter
 	
 	@Inject
 	public MobManager(Logger log, Util util, Config config, ChunkInfoFactory chunkInfoFactory,
 	        EntityInfoFactory entityInfoFactory)
 	{
 	    this.log = log;
 	    this.util = util;
 	    this.config = config;
 	    this.chunkInfoFactory = chunkInfoFactory;
 	    this.entityInfoFactory = entityInfoFactory;
 	}
 
 	/** Method to determine whether we will allow a given entity to spawn
 	 * or not.
 	 * 
 	 * @param entity
 	 * @return
 	 */
 	public boolean canSpawn(Entity entity) {
 	    // curently only animals are limited
 	    if( util.isAnimal(entity) && !config.isAnimalKillOldestOnSpawn() ) {
             if( isAnimalSegmentCountExceeded(entity.getLocation()) ) {
                 log.debug("refusing entity spawn due to chunk size limits for entity ", entity);
                 return false;
             }
 	    }
 	    
 	    return true;
 	}
 	
 	/** Return true if the segment identified by the location is currently
 	 * exceeding the amount of animals allowed per segment.
 	 * 
 	 * @return
 	 */
 	private boolean isAnimalSegmentCountExceeded(final Location l) {
         final int maxPerSegment = config.getAnimalMaxPerSegment();
         if( getAnimalSegmentCount(l) > maxPerSegment ) {
            return false;
         }
         else {
            return true;
         }
 	}
 	
 	/** Return the current count of animals in the segment identified by
 	 * the location. 
 	 * 
 	 * @param l
 	 * @return
 	 */
 	private int getAnimalSegmentCount(final Location l) {
         int count = 0;
         
         if( l != null ) {
             final int chunkX = l.getChunk().getX();
             final int chunkZ = l.getChunk().getZ();
             final int segmentSize = config.getAnimalChunkSegmentSize();
 
             // iterate through all chunks in the segment and count the animals
             for(int x=chunkX-segmentSize; x <= chunkX+segmentSize; x++) {
                 for(int z=chunkZ-segmentSize; z <= chunkZ+segmentSize; z++) {
                     ChunkInfo chunkInfo = getChunkInfo(l.getChunk());
                     count += chunkInfo.getAnimals().size();
                 }
             }
         }
         
         return count;
 	}
 	
 	private Animals getOldestSegmentAnimal(final Location l, boolean ignoreTamed) {
 	    int oldestTime = -1;
 	    Animals oldestAnimal = null;
 	    
         if( l != null ) {
             final int chunkX = l.getChunk().getX();
             final int chunkZ = l.getChunk().getZ();
             final int segmentSize = config.getAnimalChunkSegmentSize();
 
             // iterate through all chunks in the segment and count the animals
             for(int x=chunkX-segmentSize; x <= chunkX+segmentSize; x++) {
                 for(int z=chunkZ-segmentSize; z <= chunkZ+segmentSize; z++) {
                     ChunkInfo chunkInfo = getChunkInfo(l.getChunk());
                     for(Animals entity : chunkInfo.getAnimals()) {
                         // ignore Tamed animals if directed to do so 
                         if( ignoreTamed && entity instanceof Tameable ) {
                             Tameable tameable = (Tameable) entity;
                             if( tameable.isTamed() ) {
                                 continue;
                             }
                         }
                         
                         int ticks = entity.getTicksLived();
                         
                         // is this animal older than the oldest found so far?
                         if( ticks > oldestTime ) {
                             oldestTime = ticks;
                             oldestAnimal = entity;
                         }
                     }
                 }
             }
         }
 	    
         return oldestAnimal;
 	}
 	
 	/** Called to inform us when an entity has spawned.
 	 * 
 	 * @param entity
 	 */
 	public void entitySpawned(final Entity entity) {
         if( util.isAnimal(entity) ) {
             // are we over the max animals allowed per segment? If so and the right
             // config flag is set, we kill off the oldest animal to make room for
             // the new one.
             if( config.isAnimalKillOldestOnSpawn() && isAnimalSegmentCountExceeded(entity.getLocation()) ) {
                 Animals oldestAnimal = getOldestSegmentAnimal(entity.getLocation(), true);
                 if( oldestAnimal != null ) {
                     log.debug("Killing oldest animal "+oldestAnimal);
                     oldestAnimal.damage(1000);
                 }
             }
         }
         
         entitySpawnCounter++;
 	}
 	
 	/** To be called when an entity has an "interaction" that keeps it active. Being
 	 * active means it won't be purged.
 	 * 
 	 * @param entity
 	 */
 	public void interact(final Entity entity) {
 	    EntityInfo entityInfo = getEntityInfo(entity);
 	    entityInfo.setLastInteractEventTime(System.currentTimeMillis());
 	}
 	
 	/** Called to indicate a player damaged the given entity. This is tracked so
 	 * we know when a monster dies whether or not it was ever hit by a player.
 	 * 
 	 * @param entity
 	 */
 	public void playerDamage(final Entity entity) {
         EntityInfo entityInfo = getEntityInfo(entity);
         entityInfo.setPlayerDamaged();
 	}
 	
 	String getChunkKey(Chunk chunk) {
 	    return util.getChunkKey(chunk);
 	}
 	
 	/** Return ChunkInfo object. Will create one if one doesn't already exist.
 	 * 
 	 * @param currentChunkKey
 	 * @return
 	 */
 	private ChunkInfo getChunkInfo(final Chunk chunk) {
 	    final String chunkKey = getChunkKey(chunk);
 	    ChunkInfo chunkInfo = chunks.get(chunkKey);
 	    if( chunkInfo == null ) {
 	        chunkInfo = chunkInfoFactory.create(chunk);
 	        chunks.put(chunkKey, chunkInfo);
 	    }
 	    return chunkInfo;
 	}
 	
 	public EntityInfo getEntityInfo(final Entity entity) {
 	    EntityInfo entityInfo = entities.get(entity.getUniqueId());
 	    if( entityInfo == null ) {
 	        entityInfo = entityInfoFactory.create(entity);
 	        entities.put(entity.getUniqueId(), entityInfo);
 	    }
 	    return entityInfo;
 	}
 	
 	/** Update any position-related data for a given entity.
 	 * 
 	 * @param entity
 	 */
 	public void updatePosition(Entity entity) {
 	    EntityInfo entityInfo = getEntityInfo(entity);
 	    
 	    String currentChunkKey = null;
 	    Location currentLocation = entity.getLocation();
 	    if( currentLocation != null )
 	        currentChunkKey = getChunkKey(currentLocation.getChunk());
 	    
 	    // if position has changed, delete old position
 	    if( currentChunkKey == null ||
 	            (entityInfo.getCurrentChunkKey() != null && !entityInfo.getCurrentChunkKey().equals(currentChunkKey)) ) {
 	        // remove 
 //	        ChunkInfo chunkInfo = chunks.get(entityInfo.currentChunkKey);
 //	        if( chunkInfo != null ) {
 //	            chunkInfo.entities.remove(entity);
 //	        }
 	    }
 	    
 	    // if there is a current position, update new position
 	    if( currentChunkKey != null ) {
 	        // update entity->chunk mapping
             entityInfo.setCurrentChunkKey(currentChunkKey);
             
             // update chunk->entities set
 //	        ChunkInfo chunkInfo = getChunkInfo(currentLocation.getChunk());
 //	        chunkInfo.entities.add(entity);
 	    }
 	}
 	
 	private void cleanupEntity(final Entity entity) {
 	    EntityInfo entityInfo = getEntityInfo(entity);
 	    entities.remove(entity.getUniqueId());
 	    chunks.remove(entityInfo.getCurrentChunkKey());
 	}
 	
 	/** Check if a player is nearby the given entity. This checks for a player in
 	 * the current chunk or in any of the 8 adjacent chunks (think of a tic-tac-toe
 	 * grid, with current chunk being the middle grid).
 	 * 
 	 * @param entity
 	 * @return
 	 */
 	private boolean playerIsNearby(final Entity entity) {
 	    if( !entity.isValid() )
 	        return false;
 	    
 	    final Location location = entity.getLocation();
 	    if( location == null )
 	        return false;
 	    
 	    final World world = location.getWorld();
 	    final int chunkX = location.getChunk().getX();
 	    final int chunkZ = location.getChunk().getZ();
 	    
 	    for(Player p : world.getPlayers()) {
 	        Location playerLocation = p.getLocation();
 	        if( playerLocation != null ) {
 	            int playerChunkX = p.getLocation().getChunk().getX();
                 int playerChunkZ = p.getLocation().getChunk().getZ();
                 
                 int distance = config.getMonsterPlayerChunkRadius();
                 
                 // is the player within the configured chunk distance?
                 if( playerChunkX >= chunkX-distance && playerChunkX <= chunkX+distance
                         && playerChunkZ >= chunkZ-distance && playerChunkZ <= chunkZ+distance ) {
                     return true;
                 }
 	        }
 	    }
 	    
 	    // if we get here, no players are nearby
 	    return false;
 	}
 	
 	/** Check if an entity should be purged based on entity type and activity.
 	 * 
 	 * @param entity
 	 * @return
 	 */
 	private boolean shouldPurge(final Entity entity) {
 	    if( !util.isMonster(entity) )   // only monsters can be purged
 	        return false;
 	    
 	    boolean ret = true;
 	    String debugReason = "";
 	    
 	    EntityInfo entityInfo = getEntityInfo(entity);
 	    long timeSinceLastInteract = System.currentTimeMillis() - entityInfo.getLastInteractEventTime();
 	    if( timeSinceLastInteract > (config.getMonsterIdleAge() * 1000) ) {
 	        if( entity instanceof Creature ) {
 	            Creature creature = (Creature) entity;
 	            
 	            // don't purge if we're currently targeting a player
 	            LivingEntity target = creature.getTarget();
 	            if( target instanceof Player ) {
 	                debugReason = "entity is targeting player";
 	                ret = false;
 	            }
 
 	            if( playerIsNearby(entity) ) {
                     debugReason = "player is nearby";
                     ret = false;
 	            }
 	        }
 	    }
 	    else {
             debugReason = "entity is not idle";
 	        ret = false;   // entity is active, don't purge
 	    }
 	    
         log.debug("shouldPurge() ret=",ret,", time=",timeSinceLastInteract,", entity=",entity,", reason=",debugReason,", id=",entity.getEntityId(),", uniqueId=",entity.getUniqueId());
 	    return ret;
 	}
 	
 	/** We run on a regular schedule to update entity positions.
 	 * Possibly chunk into smaller segments for separate runs if performance
 	 * becomes an issue. However, it would be lots of work to keep track of
 	 * state between runs so for now use simple algorithm and keep it
 	 * lightweight so we get through it quickly.
 	 */
 	public void run() {
 	    List<World> worlds = Bukkit.getWorlds();
 	    
 	    // process all entities on all worlds
 	    for(World world : worlds) {
 	        List<Entity> entities = world.getEntities();
 	        for(Entity entity : entities) {
 	            // if the entity is no longer valid (dead or Bukkit cleanup), remove it
 	            if( !entity.isValid() ) {
 	                cleanupEntity(entity);
 	                continue;
 	            }
 	            
 	            if( shouldPurge(entity) ) {
 	                entity.remove();
 	                cleanupEntity(entity);
 	            }
 	            else
 	                updatePosition(entity);
 	        }
 	    }
 	    
 	    log.debug("Entities spawned since last reset=",entitySpawnCounter);
 	    entitySpawnCounter=0;
 	}
 }

 package de.kumpelblase2.remoteentities;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import net.minecraft.server.v1_5_R2.WorldServer;
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.world.ChunkLoadEvent;
 import org.bukkit.event.world.ChunkUnloadEvent;
 import de.kumpelblase2.remoteentities.api.DespawnReason;
 import de.kumpelblase2.remoteentities.api.RemoteEntity;
 import de.kumpelblase2.remoteentities.api.RemoteEntityHandle;
 
 class ChunkEntityLoader implements Listener
 {
 	private EntityManager m_manager;
 	private Set<EntityLoadData> m_toSpawn;
 	
 	ChunkEntityLoader(EntityManager inManager)
 	{
 		this.m_manager = inManager;
 		this.m_toSpawn = new HashSet<EntityLoadData>();
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onChunkLoad(ChunkLoadEvent event)
 	{
 		final Chunk c = event.getChunk();
 		for(RemoteEntity entity : this.m_manager.getAllEntities())
 		{
 			if(!entity.isSpawned())
 				continue;
 			
 			if(entity.getBukkitEntity().getLocation().getChunk() == c && entity.getHandle() != null)
 			{
 				WorldServer ws = ((CraftWorld)c.getWorld()).getHandle();
 				if(!ws.tracker.trackedEntities.b(entity.getHandle().id))
 					ws.addEntity(entity.getHandle());
 			}
 		}
 		
 		Bukkit.getScheduler().runTask(RemoteEntities.getInstance(), new Runnable() {
 			public void run()
 			{
 				Iterator<EntityLoadData> it = m_toSpawn.iterator();
 				while(it.hasNext())
 				{
 					EntityLoadData toSpawn = it.next();
 					Location loc = toSpawn.loc;
 					if(loc.getChunk() == c)
 					{
 						toSpawn.entity.spawn(loc);
 						if(toSpawn.entity.isSpawned() && toSpawn.setupGoals)
 							((RemoteEntityHandle)toSpawn.entity.getHandle()).setupStandardGoals();
 							
 						it.remove();
 					}
 				}
 			}
 		});
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onChunkUnload(ChunkUnloadEvent event)
 	{
 		final Chunk c = event.getChunk();
 		Bukkit.getScheduler().runTask(RemoteEntities.getInstance(), new Runnable() {
 			public void run()
 			{
 				for(Entity entity : c.getEntities())
 				{
 					if(!(entity instanceof LivingEntity))
 						continue;
 					
 					if(RemoteEntities.isRemoteEntity((LivingEntity)entity))
 					{
 						RemoteEntity rentity = (RemoteEntity)RemoteEntities.getRemoteEntityFromEntity((LivingEntity)entity);
 						if(rentity.isSpawned())
 						{
 							m_toSpawn.add(new EntityLoadData(rentity, entity.getLocation()));
 							rentity.despawn(DespawnReason.CHUNK_UNLOAD);
 						}
 					}
 				}
 			}
 		});
 	}
 	
 	/**
 	 * Checks if an entity can be directly be spawned at given location.
 	 * 
 	 * @param inLocation	Location to check for
 	 * @return				true if it can be spawned, false if not
 	 */
 	public boolean canSpawnAt(Location inLocation)
 	{
 		if(inLocation.getChunk().isLoaded())
 			return true;
 		
 		return false;
 	}
 	
 	/**
 	 * Queues an entity to spawn whenever the chunk is loaded.
 	 * 
 	 * @param inEntity		Entity to spawn
 	 * @param inLocation	Location to spawn at
 	 * @return				true if it gets queued, false if it could be spawned directly
 	 */
 	public boolean queueSpawn(RemoteEntity inEntity, Location inLocation)
 	{
 		return this.queueSpawn(inEntity, inLocation, false);
 	}
 	
 	/**
 	 * Queues an entity to spawn whenever the chunk is loaded.
 	 * 
 	 * @param inEntity		Entity to spawn
 	 * @param inLocation	Location to spawn at
 	 * @param inSetupGoals	Whether standard goals should be applied or not
 	 * @return				true if it gets queued, false if it could be spawned directly
 	 */
 	public boolean queueSpawn(RemoteEntity inEntity, Location inLocation, boolean inSetupGoals)
 	{
 		if(this.canSpawnAt(inLocation))
 		{
 			inEntity.spawn(inLocation);
			if(inEntity.isSpawned() && inSetupGoals && inEntity.getHandle() instanceof RemoteEntityHandle)
				((RemoteEntityHandle)inEntity.getHandle()).setupStandardGoals();
			
 			return false;
 		}
 		
 		this.m_toSpawn.add(new EntityLoadData(inEntity, inLocation, inSetupGoals));
 		return true;
 	}
 	
 	class EntityLoadData
 	{
 		RemoteEntity entity;
 		Location loc;
 		boolean setupGoals;
 		
 		public EntityLoadData(RemoteEntity inEntity, Location inLoc, boolean inSetupGoals)
 		{
 			this.entity = inEntity;
 			this.loc = inLoc;
 			this.setupGoals = inSetupGoals;
 		}
 		
 		public EntityLoadData(RemoteEntity inEntity, Location inLoc)
 		{
 			this(inEntity, inLoc, false);
 		}
 	}
 }

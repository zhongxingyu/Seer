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
 
 package com.forgenz.mobmanager;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Tameable;
 import org.bukkit.entity.Zombie;
 import org.bukkit.inventory.EntityEquipment;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import com.forgenz.mobmanager.listeners.MobListener;
 import com.forgenz.mobmanager.world.MMChunk;
 import com.forgenz.mobmanager.world.MMCoord;
 import com.forgenz.mobmanager.world.MMWorld;
 
 
 public class MobDespawnTask extends BukkitRunnable
 {
 	/**
 	 * This class is responsible for making sure the iterators are accessed while still knowing which world they belong to</br>
 	 * Used to make it easier to spread the task over multiple ticks
 	 * @author Michael McKnight (ShadowDog007)
 	 *
 	 */
 	private class EntityIterator
 	{
 		private int currentIndex = 0;
 		private final ArrayList<MMWorld> worlds;
 		private final ArrayList<Iterator<LivingEntity>> iterators;
 		
 		private final HashMap<MMWorld, HashSet<LivingEntity>> mobsToDespawn;
 		
 		/**
 		 * Sets up the iterators required
 		 */
 		EntityIterator()
 		{
 			worlds = new ArrayList<MMWorld>(P.worlds.size());
 			iterators = new ArrayList<Iterator<LivingEntity>>(P.worlds.size());
 			
 			mobsToDespawn = Config.useAsyncDespawnScanner ? new HashMap<MMWorld, HashSet<LivingEntity>>() : null;
 			
 			for (MMWorld world : P.worlds.values())
 			{
 				worlds.add(world);
 				iterators.add(world.getWorld().getLivingEntities().iterator());
 				if (mobsToDespawn != null)
 					mobsToDespawn.put(world, new HashSet<LivingEntity>());
 				
 				world.updateMobCounts();
 			}
 		}
 		
 		/**
 		 * Adds the entity to the list of entities to be removed
 		 */
 		public void addEntity(LivingEntity entity)
 		{
 			if (!Config.useAsyncDespawnScanner)
 				return;
 			mobsToDespawn.get(getWorld()).add(entity);
 		}
 		
 		/**
 		 * Creates the task which removes any entities which have been marked for removal
 		 */
 		public void removeEntities()
 		{
 			if (!Config.useAsyncDespawnScanner)
 				return;
 			
 			P.p.getServer().getScheduler().runTask(P.p, new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					for (Entry<MMWorld, HashSet<LivingEntity>> e : mobsToDespawn.entrySet())
 					{
 						for (LivingEntity entity : e.getValue())
 						{
 							e.getKey().decrementMobCount(MobType.valueOf(entity));
 							entity.remove();
 						}
 					}
 				}
 			});
 		}
 		
 		/**
 		 * Fetches the world we are currently iterating over
 		 */
 		public MMWorld getWorld()
 		{
 			return worlds.get(currentIndex);
 		}
 		
 		/**
 		 * Checks if there are more entities to check</br>
 		 * Also iterates to the next iterator when the current one is finished
 		 */
 		public boolean hasNext()
 		{
 			if (currentIndex >= iterators.size())
 				return false;
 			
 			if (iterators.get(currentIndex).hasNext())
 				return true;
 			
 			if (++currentIndex >= iterators.size())
 				return false;
 			
 			return hasNext();
 		}
 		
 		/**
 		 * Fetches the next entity
 		 */
 		public LivingEntity next()
 		{
 			if (!hasNext())
 				return null;
 			
 			return iterators.get(currentIndex).next();
 		}
 	}
 	
 	private AtomicBoolean running = new AtomicBoolean(false);
 	/**
 	 * Sets up the despawn scan
 	 */
 	@Override
 	public void run()
 	{
 		if (P.worlds == null)
 		{
 			cancel();
 			return;
 		}
 		
 		/* ######## START TASK ######## */
 		// Make sure the task is not run more than once at a time
 		if (!running.compareAndSet(false, true))
 			return;
 		
 		// Create the entity iterator object
 		final EntityIterator it = new EntityIterator();
 		
 		// Create the despawner task
 		final Runnable removeQueueFillTask = new Runnable()
 		{
 			/**
 			 * Scans for and removes entities which are not required
 			 */
 			@Override
 			public void run()
 			{
 				// Note the time we start
 				long start = System.nanoTime();
 				LivingEntity entity;
				// Iterate through each entity until there are none left or the task has run for 0.1ms
				while ((entity = it.next()) != null && (System.nanoTime() - start) < 100000L)
 				{
 					// Make sure the entity is alive and valid
 					if (!entity.isValid())
 						continue;
 
 					// Check if the mob has lived long enough
 					if (entity.getTicksLived() <= Config.minTicksLivedForDespawn)
 						continue;
 
 					// Check if the mob is being ignored
 					if (Config.ignoredMobs.containsValue(entity.getType().toString()))
 						continue;
 
 					MobType mob = MobType.valueOf(entity);
 					// If MobManager does not recognize the entity ignore it
 					if (mob == null)
 						continue;
 
 					// Check if the mob is an animal
 					if (mob == MobType.ANIMAL)
 					{
 						// If animal protection is off then despawning of animals is disabled
 						if (P.p.animalProtection == null)
 							continue;
 						
 						// Check if the animal is tamed
 						if (!Config.removeTamedAnimals && entity instanceof Tameable)
 						{
 							Tameable tameable = (Tameable) entity;
 							
 							if (tameable.isTamed())
 								continue;
 						}
 						
 						// Check if the animal is being protected
 						if (P.p.animalProtection.checkUUID(entity.getUniqueId()))
 							continue;
 
 						// TODO Remove this feature completely
 						// If the chunk has more than 'numAnimalsForFarm' then animals are not despawned
 						MMChunk chunk = it.getWorld().getChunk(entity.getLocation().getChunk());
 						if (chunk.getAnimalCount() >= it.getWorld().worldConf.numAnimalsForFarm)
 							continue;
 					}
 					// Only despawn villagers if they are over their limits
 					else if (mob == MobType.VILLAGER)
 					{
 						if (it.getWorld().withinMobLimit(mob))
 							continue;
 					}
 					// Does not despawn the entity if it carries players items
 					else if (entity instanceof Zombie || entity instanceof Skeleton)
 					{
 						EntityEquipment equipment = entity.getEquipment();
 
 						// If any of these statements pass then the the mob carries an item dropped from a player
 						if (equipment.getBootsDropChance() == 1F)
 							continue;
 						if (equipment.getChestplateDropChance() == 1F)
 							continue;
 						if (equipment.getHelmetDropChance() == 1F)
 							continue;
 						if (equipment.getItemInHandDropChance() == 1F)
 							continue;
 						if (equipment.getLeggingsDropChance() == 1F)
 							continue;
 					}
 
 					// Search for a nearby player
 					if (!MobListener.i.playerNear(it.getWorld(), new MMCoord(entity.getLocation().getChunk()), entity.getLocation().getBlockY(), MobListener.i.mobFlys(entity)))
 					{
 						if (!Config.useAsyncDespawnScanner)
 						{
 							entity.remove();
 							it.getWorld().decrementMobCount(MobType.valueOf(entity));
 						}
 						else
 						{
 							it.addEntity(entity);
 						}
 					}
 				}
 				
 				// Check if there is more to go
 				if (it.hasNext() && P.p != null)
 				{
 					// Schedule the task to run later
 					if (Config.useAsyncDespawnScanner)
 						P.p.getServer().getScheduler().runTaskLaterAsynchronously(P.p, this, 1L);
 					else
 						P.p.getServer().getScheduler().runTaskLater(P.p, this, 1L);
 				}
 				// If we are in async we need to schedule a new task to remove the entities
 				else if (P.p != null)
 				{
 					if (Config.useAsyncDespawnScanner)
 						P.p.getServer().getScheduler().runTask(P.p, new Runnable()
 						{
 							@Override
 							public void run()
 							{
 								it.removeEntities();
 							}
 						});
 					
 					/* ######## END TASK ######## */
 					running.compareAndSet(true, false);
 				}
 			}
 		};
 		
 		// Run the despawner task
 		if (Config.useAsyncDespawnScanner)
 			P.p.getServer().getScheduler().runTaskLaterAsynchronously(P.p, removeQueueFillTask, 1L);
 		else
 			P.p.getServer().getScheduler().runTaskLater(P.p, removeQueueFillTask, 1L);
 	}
 }

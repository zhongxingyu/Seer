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
 
 package com.forgenz.mobmanager.limiter.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.bukkit.entity.Ageable;
 import org.bukkit.entity.Animals;
 import org.bukkit.entity.Chicken;
 import org.bukkit.entity.Cow;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.Sheep;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import com.forgenz.mobmanager.P;
 import com.forgenz.mobmanager.limiter.config.Config;
 
 public class AnimalProtection extends BukkitRunnable implements Listener
 {
 	private File protectedAnimalsFile;
 	private ConcurrentHashMap<UUID,Long> protectedAnimals;
 	
 	private int numAttempts = 0;
 	private long cleanupPeriod;
 	
 	private AtomicBoolean cleanupRunning = new AtomicBoolean(false);
 	
 	@SuppressWarnings("unchecked")
 	public AnimalProtection()
 	{
 		protectedAnimalsFile = new File(P.p().getDataFolder(), "protectedAnimals.dat");
 		cleanupPeriod = (long) Config.daysTillFarmAnimalCleanup * 24 * 3600 * 1000;
 
 		if (protectedAnimalsFile.exists())
 		{
 			try
 			{
 				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(protectedAnimalsFile));
 				protectedAnimals = (ConcurrentHashMap<UUID,Long>) ois.readObject();
 				ois.close();
 			}
 			catch (Exception e)
 			{
 				P.p().getLogger().severe("Failed to load current protected animals");
 				protectedAnimals = new ConcurrentHashMap<UUID,Long>(0, 0.75F, 2);
 				
 				return;
 			}
 			
 			Iterator<Entry<UUID,Long>> it = protectedAnimals.entrySet().iterator();
 			
 			// Check the player associated with the animal is still active
 			while (it.hasNext())
 			{
 				Entry<UUID, Long> e = it.next();
 				
 				if (!isActive(e.getValue()))
 					it.remove();
 			}
 		}
 		else
 		{
 			protectedAnimals = new ConcurrentHashMap<UUID,Long>(0, 0.75F, 2);
 		}
 		
 		
 	}
 	
 	private boolean isActive(long lastActive)
 	{
 		return (System.currentTimeMillis() - lastActive) <= cleanupPeriod;
 	}
 	
 	public boolean checkUUID(UUID uuid)
 	{
 		Long lastActive = protectedAnimals.get(uuid);
 		
 		if (lastActive == null)
 			return false;
 		
 		if (isActive(lastActive))
 			return true;
 		
 		protectedAnimals.remove(uuid);
 		return false;
 	}
 	
 	public void addUUID(UUID uuid)
 	{
		if (cleanupPeriod <= 0)
 			protectedAnimals.put(uuid, System.currentTimeMillis());
 	}
 
 	/**
 	 * Periodically saves the protected animals to a file
 	 */
 	@Override
 	public void run()
 	{
 		if (!cleanupRunning.compareAndSet(false, true))
 			return;
 		
 		for (UUID uuid : protectedAnimals.keySet().toArray(new UUID[0]))
 		{
 			removeIfInactive(uuid);
 		}
 		
 		try
 		{
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(protectedAnimalsFile));
 			oos.writeObject(protectedAnimals);
 			oos.close();
 			numAttempts = 0;
 		} catch (IOException e)
 		{
 			P.p().getLogger().severe("Error writing protected animals list to file");
 			
 			if (++numAttempts >= 5)
 			{
 				P.p().getLogger().severe("Max attempts to write file exceeded, no more attempts will be made");
 				cancel();
 			}
 			e.printStackTrace();
 		}
 		
 		cleanupRunning.set(false);
 	}
 	
 	protected void removeIfInactive(UUID uuid)
 	{
 		Long lastActive = protectedAnimals.get(uuid);
 		
 		if (!isActive(lastActive))
 			protectedAnimals.remove(uuid);
 	}
 
 	/**
 	 * Adds the animal to the list of protected animals if the player is trying to breed the animal
 	 * @param event
 	 */
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
 	{
 		// Check if the enitiy can breed
 		if (event.getRightClicked() instanceof Animals == false)
 			return;
 		
 		Ageable entity = (Ageable) event.getRightClicked();
 		
 		if (!entity.canBreed())
 			return;
 		
 		// Check if the entity is being bred
 		switch (event.getPlayer().getItemInHand().getType())
 		{
 		case SEEDS:
 		case PUMPKIN_SEEDS:
 		case MELON_SEEDS:
 			if (event.getRightClicked() instanceof Chicken == false)
 				return;
 			break;
 		case WHEAT:
 			if (event.getRightClicked() instanceof Cow == false && event.getRightClicked() instanceof Sheep == false)
 				return;
 			break;
 		case CARROT_ITEM:
 			if (event.getRightClicked() instanceof Pig == false)
 				return;
 		default:
 			return;
 		}
 		
 		P.p().animalProtection.addUUID(event.getRightClicked().getUniqueId());
 	}
 	
 	/**
 	 * This will add newly bred animals to the list of protected animals</br>
 	 * @param event
 	 */
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onCreatureSpawn(CreatureSpawnEvent event)
 	{
 		if (event.getSpawnReason() != SpawnReason.BREEDING && event.getSpawnReason() != SpawnReason.EGG)
 			return;
 		
 		Animals animal = (Animals) event.getEntity();
 		
 		addUUID(animal.getUniqueId());
 	}
 	
 	/**
 	 * Attempts to remove the entity from protected animals when it dies
 	 * @param event
 	 */
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onCreatureDeath(EntityDeathEvent event)
 	{
 		if (event.getEntity() instanceof Animals == false)
 			return;
 		
 		protectedAnimals.remove(event.getEntity().getUniqueId());
 	}
 }

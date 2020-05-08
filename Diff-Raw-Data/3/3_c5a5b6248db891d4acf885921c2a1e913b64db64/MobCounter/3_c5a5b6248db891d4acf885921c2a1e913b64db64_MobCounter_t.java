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
 
 package com.forgenz.mobmanager.spawner.util;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.bukkit.entity.LivingEntity;
 
 public class MobCounter
 {
 	private ArrayList<MobReference> aliveMobs;
 	
 	protected  int maxAliveMobs;
 	protected int mobCooldown;
 	private final boolean enforceAllRemovalConditions;
 	
 	public MobCounter(int maxAliveMobs, int mobCooldown, boolean enforceAllRemovalConditions)
 	{
 		this.maxAliveMobs = maxAliveMobs;
 		this.mobCooldown = mobCooldown;
 		this.enforceAllRemovalConditions = enforceAllRemovalConditions;
 	}
 	
 	public boolean withinLimit()
 	{
 		// If the mob limit is disabled
 		if (maxAliveMobs <= 0)
 			return true;
 		
 		return getMobCount() < maxAliveMobs;
 	}
 	
 	public synchronized int getMobCount()
 	{
 		// If there are no alive mobs
 		if (aliveMobs == null)
 			return 0;
 		
 		// Iterate through each one and remove old mobs
 		Iterator<MobReference> it = aliveMobs.iterator();
 		while (it.hasNext())
 		{
 			MobReference r = it.next();
 			
 			// Wait a little while for the reference to the mob to be set
 			if (!r.isSet())
 				continue;
 			
 			// If the reference is invalid ignore it
 			if (!r.isValid())
 			{
 				it.remove();
 				continue;
 			}
 			
 			LivingEntity entity = r.getEntity();
 			
 			// Check if the mob is dead
 			if (entity == null)
 			{
 				// Check if we need to wait for the cooldown as well
 				if (enforceAllRemovalConditions && !r.cooldownExpired(mobCooldown))
 					continue;
 				// Remove this mob from the limit
 				it.remove();
 			}
 			// If we don't need both conditions and the cooldown has expired remove the mob from the limit
 			else if (!enforceAllRemovalConditions && r.cooldownExpired(mobCooldown))
 				it.remove();
 			else if (remove(entity))
 				it.remove();
 		}
 		
 		return aliveMobs.size();
 	}
 	
 	protected boolean remove(LivingEntity entity)
 	{
 		return false;
 	}
 
 	/**
 	 * Adds the entity to this mobs MaxAlive limit
 	 * 
 	 * @param e The entity to add
 	 */
 	public synchronized boolean add(MobReference mobRef)
 	{
 		// If maxAlive is disabled do nothing
 		if (maxAliveMobs <= 0)
 			return true;
 		
 		// If we are outside of our limit return false
 		if (!withinLimit())
 		{
 			mobRef.invalidate();
 			return false;
 		}
 		
 		// If the aliveMobs list doesn't exist, then create it
 		if (aliveMobs == null)
 			aliveMobs = new ArrayList<MobReference>();
 		
 		// Add the mob to the list
 		aliveMobs.add(mobRef);
 		return true;
 	}
 	
 	public synchronized void killAll()
 	{
		if (aliveMobs == null)
			return;
		
 		for (MobReference r : aliveMobs)
 		{
 			LivingEntity entity = r.getEntity();
 			
 			if (entity != null && entity.getRemoveWhenFarAway())
 				entity.remove();
 		}
 		aliveMobs.clear();
 	}
 }

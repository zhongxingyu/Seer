 /*
  * This file is part of MineQuest-NPC, Additional Events for MineQuest.
  * MineQuest-NPC is licensed under GNU General Public License v3.
  * Copyright (C) 2012 The MineQuest Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.theminequest.MQCoreEvents.EntityEvent;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.event.entity.EntityDamageEvent;
 
 import com.theminequest.MineQuest.API.CompleteStatus;
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.Events.QuestEvent;
 import com.theminequest.MineQuest.API.Quest.QuestDetails;
 import com.theminequest.MineQuest.API.Utils.MobUtils;
 
 public class HealthEntitySpawn extends QuestEvent {
 
 	private long delay;
 	private long start;
 	
 	private int taskid;
 	
 	private World w;
 	private Location loc;
 	private EntityType t;
 	
 	private LivingEntity entity;
 	private int health;
 	private boolean stay;
 	
 	private boolean setup;
 	
 	private volatile boolean scheduled;
 	
 	private int currentHealth;
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.QEvent#parseDetails(java.lang.String[])
 	 * [0] Delay in MS
 	 * [1] Task
 	 * [2] X
 	 * [3] Y
 	 * [4] Z
 	 * [5] Mob Type
 	 * [6] Health
 	 * [7] Stay Put?
 	 */
 	@Override
 	public void parseDetails(String[] details) {
 		delay = Long.parseLong(details[0]);
 		taskid = Integer.parseInt(details[1]);
 		String worldname = getQuest().getDetails().getProperty(QuestDetails.QUEST_WORLD);
 		w = Bukkit.getWorld(worldname);
 		double x = Double.parseDouble(details[2]);
 		double y = Double.parseDouble(details[3]);
 		double z = Double.parseDouble(details[4]);
 		loc = new Location(w,x,y,z);
 		System.out.println(details[5]);
 		t = MobUtils.getEntityType(details[5]);
 		health = Integer.parseInt(details[6]);
 		stay = ("t".equalsIgnoreCase(details[7]));
 		setup = false;
 		entity = null;
 		start = System.currentTimeMillis();
 		scheduled = false;
 		currentHealth = health;
 	}
 	
 	@Override
 	public boolean conditions() {
 		if (!setup){
 			if (System.currentTimeMillis()-start>=delay){
 				setup = true;
 				Bukkit.getScheduler().scheduleSyncDelayedTask(Managers.getActivePlugin(), new Runnable() {
 					public void run() {
 						entity = w.spawnCreature(loc, t);
 						if (health < entity.getMaxHealth())
 							entity.setHealth(health);
 					}
 				});
 			}
 		}
 		if (entity != null) {
 			if (entity.isDead())
 				return true;
 			
 			if (stay) {
 				synchronized (this) {
 					if (!scheduled) {
 						scheduled = true;
 						Bukkit.getScheduler().scheduleSyncDelayedTask(Managers.getActivePlugin(), new Runnable() {
 							public void run() {
 								if (isComplete() == null) {
 									if (!entity.isDead())
 										entity.teleport(loc);
 								}
 								scheduled = false;
 							}
 						});
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean entityDamageCondition(EntityDamageEvent e){
 		if (entity == null)
 			return false;
 		
 		if (!e.getEntity().equals(entity))
 			return false;
 		
 		int eventDamage = e.getDamage();
 		
 		// check no damage ticks first
        if (eventDamage < entity.getLastDamage() && entity.getNoDamageTicks() > entity.getMaximumNoDamageTicks() / 2)
             return false;
 		
 		if (currentHealth > entity.getMaxHealth()) {
 			entity.setHealth(entity.getMaxHealth());
 		} else if (entity.getHealth() < currentHealth)
 			entity.setHealth(currentHealth);
 		
 		currentHealth -= eventDamage;
 		return false;
 	}
 
 	@Override
 	public CompleteStatus action() {
 		return CompleteStatus.SUCCESS;
 	}
 
 	@Override
 	public Integer switchTask() {
 		return taskid;
 	}
 
 }

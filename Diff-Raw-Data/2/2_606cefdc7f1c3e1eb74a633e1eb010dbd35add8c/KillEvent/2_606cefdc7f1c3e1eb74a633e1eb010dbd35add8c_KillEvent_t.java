 /**
  * This file, KillEvent.java, is part of MineQuest:
  * A full featured and customizable quest/mission system.
  * Copyright (C) 2012 The MineQuest Party
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  **/
 package com.theminequest.MQCoreEvents.BasicEvents;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Tameable;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 import com.theminequest.MineQuest.API.CompleteStatus;
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.Events.UserQuestEvent;
 import com.theminequest.MineQuest.API.Events.QuestEvent;
 import com.theminequest.MineQuest.API.Group.QuestGroup;
 import com.theminequest.MineQuest.API.Utils.MobUtils;
 
 public class KillEvent extends QuestEvent implements UserQuestEvent {
 
 	private Map<EntityType, Integer> killMap;
 	private Map<EntityType, Integer> currentKills;
 	private int taskid;
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.QEvent#parseDetails(java.lang.String[])
 	 * [0]: task id to trigger
 	 * [1]: entities
 	 * [2]: total # to kill
 	 */
 	@Override
 	public void parseDetails(String[] details) {
 		taskid = Integer.parseInt(details[0]);
 		killMap = Collections.synchronizedMap(new LinkedHashMap<EntityType, Integer>());
 		currentKills = Collections.synchronizedMap(new HashMap<EntityType, Integer>());
 		String[] entities = details[1].split(",");
 		String[] amounts = details[2].split(",");
 		for (int i = 0; i < entities.length; i++) {
 			String entity = entities[i];
 			Integer amount = null;
 			try {
 				if (amounts.length == 1) {
 						amount = Integer.valueOf(amounts[0]);
 				} else if (i < amounts.length) {
 					amount = Integer.valueOf(amounts[i]);
 				}
 			} catch (NumberFormatException e) {}
 			
 			if (amount == null) {
 				Managers.log(Level.SEVERE, "[Event] In KillEvent, could not determine number of kills for "+entity);
 				continue;
 			}
 			
 			EntityType m = MobUtils.getEntityType(entity);
 			
 			if (m == null) {
 				Managers.log(Level.SEVERE, "[Event] In KillEvent, could not determine mob type for "+entity);
 				continue;
 			}
 			killMap.put(m, amount);
 		}
 	}
 
 	@Override
 	public boolean conditions() {
 		synchronized (killMap) {
 			for (Map.Entry<EntityType, Integer> entry : killMap.entrySet()) {
 				Integer kills = currentKills.get(entry.getKey());
 				if (kills == null || kills.intValue() < entry.getValue().intValue())
 					return false;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public CompleteStatus action() {
 		return CompleteStatus.SUCCESS;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.QEvent#entityDeathCondition(org.bukkit.event.entity.EntityDeathEvent)
 	 */
 	@Override
 	public boolean entityDeathCondition(EntityDeathEvent e) {
 		if (!(e.getEntity() instanceof LivingEntity))
 			return false;
 		LivingEntity el = (LivingEntity) e.getEntity();
 		if (!(el.getLastDamageCause() instanceof EntityDamageByEntityEvent))
 			return false;
 		
 		EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) el.getLastDamageCause();
 		Player p = null;
 		if (edbee.getDamager() instanceof Player) {
 			p = (Player) edbee.getDamager();
 		} else if (edbee.getDamager() instanceof Projectile) {
 			Projectile projectile = (Projectile) edbee.getDamager();
 			if (projectile.getShooter() instanceof Player) {
 				p = (Player) projectile.getShooter();
 			}
 		} else if (edbee.getDamager() instanceof Tameable) {
 			Tameable tameable = (Tameable) edbee.getDamager();
 			if (tameable.getOwner() instanceof Player) {
 				p = (Player) tameable.getOwner();
 			}
 		}
 		
 		if (p == null)
 			return false;
 		
 		
 		QuestGroup g = Managers.getQuestGroupManager().get(getQuest());
 		List<Player> team = g.getMembers();
 		if (team.contains(p)) {
 			if (currentKills.containsKey(el.getType())) {
 				int count = currentKills.get(el.getType());
 				currentKills.put(el.getType(), count + 1);
 			} else {
 				currentKills.put(el.getType(), 1);
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public Integer switchTask() {
 		return taskid;
 	}
 
 	@Override
 	public String getDescription() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("Kill ");
		boolean first = true;
 		int i = 0;
 		synchronized (killMap) {
 			for (Map.Entry<EntityType, Integer> entry : killMap.entrySet()) {
 				i++;
 				if (first) {
 					first = false;
 				} else {
 					builder.append(", ");
 					
 					if (i == killMap.size())
 						builder.append("and ");
 				}
 				
 				builder.append(entry.getValue().toString()).append(" ").append(entry.getKey().getName());
 			}
 		}
 		builder.append("!");
 		return builder.toString();
 	}
 
 }

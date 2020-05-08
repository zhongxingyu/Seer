 package com.theminequest.MQCoreEvents.EntityEvent;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 import com.theminequest.MineQuest.API.CompleteStatus;
 import com.theminequest.MineQuest.API.Events.QuestEvent;
 import com.theminequest.MineQuest.API.Quest.QuestDetails;
 import com.theminequest.MineQuest.API.Utils.MobUtils;
 
 /*
  * An improved version of this will come soon.
  */
 @Deprecated
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
 		t = MobUtils.getEntityType(details[5]);
 		health = Integer.parseInt(details[6]);
 		stay = ("t".equalsIgnoreCase(details[7]));
 		setup = false;
 		entity = null;
 		start = System.currentTimeMillis();
 	}
 	
 	@Override
 	public boolean conditions() {
 		if (!setup){
 			if (System.currentTimeMillis()-start>=delay){
 				setup = true;
 				entity = w.spawnCreature(loc,t);
 			}
 		}
		if (entity.isDead())
			return true;
		if (stay)
			entity.teleport(loc);
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

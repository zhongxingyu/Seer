 package com.theminequest.MQCoreEvents.EntityEvent;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Tameable;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 import com.theminequest.MineQuest.API.CompleteStatus;
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.Events.QuestEvent;
 import com.theminequest.MineQuest.API.Group.QuestGroup;
 import com.theminequest.MineQuest.API.Quest.QuestDetails;
 import com.theminequest.MineQuest.API.Utils.MobUtils;
 
 public class EntitySpawnerNoMove extends QuestEvent {
 
 	private long delay;
 	private long start;
 	
 	private World w;
 	private Location loc;
 	private EntityType t;
 	
 	private LivingEntity entity;
 	
 	private boolean setup;
 	
 	private volatile boolean scheduled;
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.QEvent#parseDetails(java.lang.String[])
 	 * [0] Delay in MS
 	 * [1] X
 	 * [2] Y
 	 * [3] Z
 	 * [4] Mob Type
 	 * [5] isSuper // deprecated and ignored;
 	 */
 	@Override
 	public void parseDetails(String[] details) {
 		delay = Long.parseLong(details[0]);
 		String worldname = getQuest().getDetails().getProperty(QuestDetails.QUEST_WORLD);
 		w = Bukkit.getWorld(worldname);
 		double x = Double.parseDouble(details[1]);
 		double y = Double.parseDouble(details[2]);
 		double z = Double.parseDouble(details[3]);
 		loc = new Location(w,x,y,z);
 		t = MobUtils.getEntityType(details[4]);
 		setup = false;
 		entity = null;
 		start = System.currentTimeMillis();
 		scheduled = false;
 	}
 
 	@Override
 	public boolean conditions() {
 		if (!setup){
 			if (System.currentTimeMillis()-start>=delay){
 				setup = true;
 				Bukkit.getScheduler().scheduleSyncDelayedTask(Managers.getActivePlugin(), new Runnable() {
 					public void run() {
 						entity = w.spawnCreature(loc, t);
 					}
 				});
 				return false;
 			}
 		}
 		
 		if (entity != null) {
 			synchronized (this) {
 				if (!scheduled) {
 					scheduled = true;
 					Bukkit.getScheduler().scheduleSyncDelayedTask(Managers.getActivePlugin(), new Runnable() {
 						public void run() {
 							if (isComplete() == null) {
 								if (entity.isDead())
 									entity = w.spawnCreature(loc, t);
 								else
 									entity.teleport(loc);
 							}
 							scheduled = false;
 						}
 					});
 				}
 			}
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.QEvent#entityDeathCondition(org.bukkit.event.entity.EntityDeathEvent)
 	 */
 	@Override
 	public boolean entityDeathCondition(EntityDeathEvent e) {
		if (entity == null)
			return false;
 		if (entity.equals(e.getEntity())) {
 			// if people outside the party kill mob, give no xp or items to prevent exploiting
 			LivingEntity el = (LivingEntity) e.getEntity();
 			if (el.getLastDamageCause() instanceof EntityDamageByEntityEvent) {			
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
 				
 				if (p != null) {
 					QuestGroup g = Managers.getQuestGroupManager().get(getQuest());
 					List<Player> team = g.getMembers();
 					if (team.contains(p))
 						return false;
 				}
 			}
 			
 			// outside of party gives no drops
 			e.setDroppedExp(0);
 			e.getDrops().clear();
 		}
 		return false;
 	}
 
 	@Override
 	public CompleteStatus action() {
 		// It should NEVER get here.
 		return CompleteStatus.FAILURE;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.QEvent#cleanUpEvent()
 	 */
 	@Override
 	public void cleanUpEvent() {
 		if (entity!=null && !entity.isDead())
 			entity.setHealth(0);
 	}
 
 	@Override
 	public Integer switchTask() {
 		return null;
 	}
 }

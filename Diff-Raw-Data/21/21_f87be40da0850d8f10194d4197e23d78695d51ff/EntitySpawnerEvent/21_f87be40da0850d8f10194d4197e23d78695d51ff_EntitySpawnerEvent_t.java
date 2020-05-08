 package com.theminequest.events.entity;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Tameable;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.plugin.Plugin;
 
 import com.theminequest.api.CompleteStatus;
 import com.theminequest.api.Managers;
 import com.theminequest.api.group.Group;
import com.theminequest.api.platform.entity.MQPlayer;
 import com.theminequest.api.quest.QuestDetails;
 import com.theminequest.api.quest.event.DelayedQuestEvent;
 import com.theminequest.bukkit.util.MobUtils;
 
 public class EntitySpawnerEvent extends DelayedQuestEvent implements Listener {
 	
 	private long delay;
 	
 	private World w;
 	private Location loc;
 	private EntityType t;
 	private boolean dropItems;
 	
 	private LivingEntity entity;
 	
 	private volatile boolean scheduled;
 	private final Object scheduledLock = new Object();
 	
 	protected boolean noMove = false;
 	
 	private boolean finished;
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.QEvent#parseDetails(java.lang.String[])
 	 * [0] Delay in MS
 	 * [1] X
 	 * [2] Y
 	 * [3] Z
 	 * [4] Mob Type
 	 * [5] dropItems;
 	 */
 	@Override
 	public void setupArguments(String[] details) {
 		delay = Long.parseLong(details[0]);
 		String worldname = getQuest().getDetails().getProperty(QuestDetails.QUEST_WORLD);
 		w = Bukkit.getWorld(worldname);
 		double x = Double.parseDouble(details[1]);
 		double y = Double.parseDouble(details[2]);
 		double z = Double.parseDouble(details[3]);
 		loc = new Location(w,x,y,z);
 		t = MobUtils.getEntityType(details[4]);
 		if (details.length >= 6)
 			dropItems = (!details[5].toLowerCase().startsWith("f"));
 		else
 			dropItems = true;
 		entity = null;
 		scheduled = false;
 		
 		finished = false;
 	}
 
 	@Override
 	public boolean delayedConditions() {
 		if (!scheduled && (noMove || entity == null || entity.isDead() || !entity.isValid())) {
 			synchronized (scheduledLock) {
 				if (!scheduled) {
 					scheduled = true;
 					Managers.getPlatform().scheduleSyncTask(new Runnable() {
 						public void run() {
 							if (isComplete() == null) {
 								if (entity == null || entity.isDead() || !entity.isValid()) {
 									entity = (LivingEntity) w.spawnEntity(loc, t);
 									MobUtils.addlProps(entity);
 								} else if (noMove)
 									entity.teleport(loc);
 							}
 							scheduled = false;
 						}
 					});
 				}
 			}
 		}
 		return finished;
 	}
 
 	@Override
 	public CompleteStatus action() {
 		return CompleteStatus.SUCCESS;
 	}
 
 	@Override
 	public void setUpEvent() {
 		Bukkit.getPluginManager().registerEvents(this, (Plugin) Managers.getPlatform().getPlatformObject());
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.QEvent#entityDeathCondition(org.bukkit.event.entity.EntityDeathEvent)
 	 */
 	@EventHandler
 	public void entityDeathCondition(EntityDeathEvent e) {
 		if (entity == null)
 			return;
 		if (entity.equals(e.getEntity())) {
 			boolean inParty = false;
 			
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
 					Group g = Managers.getGroupManager().get(getQuest());
 					List<MQPlayer> team = g.getMembers();
 					if (team.contains(Managers.getPlatform().toPlayer(p)))
 						inParty = true;
 				}
 			}
 			
 			// outside of party gives no drops
 			if (!dropItems || !inParty) {
 				e.setDroppedExp(0);
 				e.getDrops().clear();
 			}
 			
 			finished = inParty;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.QEvent#cleanUpEvent()
 	 */
 	@Override
 	public void cleanUpEvent() {
 		if (entity!=null && !entity.isDead())
 			entity.setHealth(0);
 		
 		HandlerList.unregisterAll(this);
 	}
 
 	@Override
 	public Integer switchTask() {
 		return null;
 	}
 
 	@Override
 	public long getDelay() {
 		return delay;
 	}
 
 }

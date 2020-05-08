 package com.terxiii.nogrind;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.scheduler.BukkitScheduler;
 
 public class DamageCauseListener extends EntityListener {
 	
 	public boolean isEnabled = false;
 	
 	protected static ArrayList<Entity> tagged;
 	protected static ArrayList<Long> tagtime;
 	protected static long tag_duration = 200L;
 	protected static BukkitScheduler scheduler;
 	protected static NoGrind ngp;
 	
 	public DamageCauseListener(NoGrind plug)
 	{
 		DamageCauseListener.ngp = plug;
 		if(DamageCauseListener.tagged==null)
 		{
 			tagged = new ArrayList<Entity>();
 			tagtime = new ArrayList<Long>();
 		}
 	}
 	
 	
 	public void onEntityDamage(EntityDamageEvent event)
 	{
 		if(isEnabled)
 		{
 			if(tag_duration>0)
 			{
 				if(!(event.getEntity() instanceof Player))
 				{
 					if(event instanceof EntityDamageByEntityEvent)
 					{
 						EntityDamageByEntityEvent entev = (EntityDamageByEntityEvent)event;
 						
 						if(entev.getCause()==DamageCause.PROJECTILE)
 						{
 							Projectile pj = (Projectile)entev.getDamager();
 							if(pj.getShooter() instanceof Player)
 							{
 								DamageCauseListener.combatTag(entev.getEntity());
 							}
 						}
 						else if(entev.getDamager() instanceof Player)
 						{
 							DamageCauseListener.combatTag(entev.getEntity());
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	public void onEntityDeath(EntityDeathEvent event)
 	{
 		if(isEnabled)
 		{
 			if(!(event.getEntity() instanceof Player))
 			{
 				if(tag_duration>0)
 				{
 					if(!(DamageCauseListener.tagged.contains(event.getEntity())))
 					{
 						List<org.bukkit.inventory.ItemStack> drp = event.getDrops();
 						org.bukkit.inventory.ItemStack is;
 						int itype;
						int count = 0;
						while(count<drp.size())
 						{
 							is = drp.get(count);
 							itype = is.getType().getId();
 							if(itype<2258||itype>2256)
 							{
 								drp.remove(is);
 							}
							else
							{
								count++;
							}
 						}
 						event.setDroppedExp(0);
 					}
 					else
 					{
 						int ind = DamageCauseListener.tagged.indexOf(event.getEntity());
 						DamageCauseListener.tagged.remove(ind);
 						DamageCauseListener.tagtime.remove(ind);
 					}
 				}
 				else
 				{
 					Entity ent = event.getEntity();
 					EntityDamageEvent devent = ent.getLastDamageCause();
 					if(devent instanceof EntityDamageByEntityEvent)
 					{
 						EntityDamageByEntityEvent entev = (EntityDamageByEntityEvent)devent;
 						
 						if(entev.getCause()==DamageCause.PROJECTILE)
 						{
 							Projectile pj = (Projectile)entev.getDamager();
 							if(pj.getShooter() instanceof Player)
 							{
 								return;
 							}
 						}
 						else if(entev.getDamager() instanceof Player)
 						{
 							return;
 						}
 					}
 					
 					List<ItemStack> drp = event.getDrops();
 					ItemStack is;
 					int itype;
					int count = 0;
					while(count<drp.size())
 					{
 						is = drp.get(count);
 						itype = is.getType().getId();
 						if(itype<2258||itype>2256)
 						{
 							drp.remove(is);
 						}
						else
						{
							count++;
						}
 					}
 					event.setDroppedExp(0);
 				}
 			}
 		}
 	}
 	
 	public static void combatTag(Entity tagging)
 	{
 		boolean startup = false;
 		if(DamageCauseListener.tagged.size()==0)
 		{
 			startup = true;
 		}
 		
 		if(DamageCauseListener.tagged.contains(tagging))
 		{
 			int ind = DamageCauseListener.tagged.indexOf(tagging);
 			
 			DamageCauseListener.tagged.remove(ind);
 			DamageCauseListener.tagtime.remove(ind);
 		}
 		long st = System.currentTimeMillis()+DamageCauseListener.tag_duration*50;
 		
 		DamageCauseListener.tagged.add(tagging);
 		DamageCauseListener.tagtime.add((Long)(st));
 		
 		if(startup)
 		{
 			DamageCauseListener.startTimer(DamageCauseListener.tag_duration*50);
 		}
 	}
 	
 	public static void startTimer(long millis)
 	{
 		long delay = millis/50L;
 		if(delay<0)
 		{
 			delay = 0;
 		}
 		scheduler.scheduleSyncDelayedTask(ngp,new Runnable() {
 			
 			public void run() {
 				DamageCauseListener.tagExpire();
 			}
 		},delay);
 	}
 	
 	public static void tagExpire()
 	{
 		long ct = System.currentTimeMillis();
 		int count = 0;
 		boolean removing = true;
 		long tt = 0;
 		while(removing&&count<DamageCauseListener.tagged.size())
 		{
 			tt = DamageCauseListener.tagtime.get(count);
 			if(tt-ct<50)
 			{
 				DamageCauseListener.tagged.remove(count);
 				DamageCauseListener.tagtime.remove(count);
 			}
 			else
 			{
 				removing = false;
 			}
 			count++;
 		}
 		if(DamageCauseListener.tagged.size()>0)
 		{
 			DamageCauseListener.startTimer(tt-ct);
 		}
 	}
 	
 	public static void setExpireTime(int len)
 	{
 		DamageCauseListener.tag_duration = (long)len*20;
 		NoGrind.log.info("Set combat duration to "+tag_duration);
 	}
 
 }

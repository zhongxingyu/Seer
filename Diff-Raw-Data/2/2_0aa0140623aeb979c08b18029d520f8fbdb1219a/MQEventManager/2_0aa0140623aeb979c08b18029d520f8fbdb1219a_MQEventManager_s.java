 /**
  * This file, MQEventManager.java, is part of MineQuest:
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
 package com.theminequest.MineQuest.Events;
 
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.Events.EventManager;
 import com.theminequest.MineQuest.API.Events.QuestEvent;
 import com.theminequest.MineQuest.API.Quest.Quest;
 
 /**
  * Because we don't know what classes will be available on runtime, we need to
  * keep track of all classes that extend QuestEvent and record them here.
  * 
  * @author xu_robert <xu_robert@linux.com>
  * 
  */
 public class MQEventManager implements Listener, EventManager {
 
 	private LinkedHashMap<String, Class<? extends QuestEvent>> classes;
 	private List<QuestEvent> activeevents;
 	private Runnable activechecker;
 	private Object classlistlock;
 	private volatile boolean stop;
 
 	public MQEventManager() {
 		Managers.log("[Event] Starting Manager...");
 		classes = new LinkedHashMap<String, Class<? extends QuestEvent>>(0);
 		activeevents = Collections.synchronizedList(new ArrayList<QuestEvent>(0));
 		classlistlock = new Object();
 		stop = false;
 		activechecker = new Runnable(){
 
 			@Override
 			public void run() {
 				while(!stop){
 					checkAllEvents();
 					try {
 						Thread.sleep(50);
 					} catch (InterruptedException e) {
 						throw new RuntimeException(e);
 					}
 				}
 			}
 
 		};
 		Thread t = new Thread(activechecker);
 		t.setDaemon(true);
 		t.setName("MineQuest-MQEventManager");
 		t.start();
 	}
 
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.EventManager#dismantleRunnable()
 	 */
 	@Override
 	public void dismantleRunnable(){
 		stop = true;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.EventManager#registerEvent(java.lang.String, java.lang.Class)
 	 */
 	@Override
 	public void addEvent(String eventname, Class<? extends QuestEvent> event) {
 		synchronized(classlistlock){
 			if (classes.containsKey(eventname) || classes.containsValue(event))
 				throw new IllegalArgumentException("We already have this class!");
 			try {
				event.getConstructor(long.class, int.class, java.lang.String.class);
 			} catch (Exception e) {
 				throw new IllegalArgumentException("Constructor tampered with!");
 			}
 			classes.put(eventname, event);
 		}
 	}
 
 	@Override
 	public QuestEvent constructEvent(String eventname, Quest q, int eventnum, String d) {
 		synchronized(classlistlock){
 			if (!classes.containsKey(eventname))
 				return null;
 			Class<? extends QuestEvent> cl = classes.get(eventname);
 			try {
 				QuestEvent e = cl.getConstructor().newInstance();
 				e.setupProperties(q, eventnum, d);
 				return e;
 			} catch (Exception e) {
 				Managers.log(Level.SEVERE, "[Event] In retrieving event " + eventname + " from Quest ID " + q + ":");
 				e.fillInStackTrace();
 				e.printStackTrace();
 				return null;
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.EventManager#addEventListener(com.theminequest.MineQuest.API.Events.QuestEvent)
 	 */
 	@Override
 	public void registerEventListener(final QuestEvent e){
 		activeevents.add(e);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.EventManager#rmEventListener(com.theminequest.MineQuest.API.Events.QuestEvent)
 	 */
 	@Override
 	public void deregisterEventListener(QuestEvent e){
 		activeevents.remove(e);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.EventManager#checkAllEvents()
 	 */
 	@Override
 	public void checkAllEvents(){
 		synchronized(activeevents){
 			for (final QuestEvent e : activeevents){
 				new Thread(new Runnable(){
 					@Override
 					public void run() {
 						e.check();
 					}
 				}).start();
 			}
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.EventManager#onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent)
 	 */
 	@Override
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerInteract(final PlayerInteractEvent e){
 		synchronized(activeevents){
 			for (int i=0; i<activeevents.size(); i++){
 				final QuestEvent a = activeevents.get(i);
 				if (!e.isCancelled())
 					a.onPlayerInteract(e);
 				if (a.isComplete()!=null)
 					i--;
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.EventManager#onBlockBreak(org.bukkit.event.block.BlockBreakEvent)
 	 */
 	@Override
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockBreak(final BlockBreakEvent e){
 		synchronized(activeevents){
 			for (int i=0; i<activeevents.size(); i++){
 				final QuestEvent a = activeevents.get(i);
 				if (!e.isCancelled())
 					a.onBlockBreak(e);
 				if (a.isComplete()!=null)
 					i--;
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.EventManager#onEntityDamageByEntityEvent(org.bukkit.event.entity.EntityDamageByEntityEvent)
 	 */
 	@Override
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityDamageByEntityEvent(final EntityDamageByEntityEvent e){
 		synchronized(activeevents){
 			for (int i=0; i<activeevents.size(); i++){
 				final QuestEvent a = activeevents.get(i);
 				if (!e.isCancelled())
 					a.onEntityDamageByEntity(e);
 				if (a.isComplete()!=null)
 					i--;
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.EventManager#onEntityDeathEvent(org.bukkit.event.entity.EntityDeathEvent)
 	 */
 	@Override
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityDeathEvent(final EntityDeathEvent e){
 		synchronized(activeevents){
 			for (int i=0; i<activeevents.size(); i++){
 				final QuestEvent a = activeevents.get(i);
 				a.onEntityDeath(e);
 				if (a.isComplete()!=null)
 					i--;
 			}
 		}
 	}
 
 }

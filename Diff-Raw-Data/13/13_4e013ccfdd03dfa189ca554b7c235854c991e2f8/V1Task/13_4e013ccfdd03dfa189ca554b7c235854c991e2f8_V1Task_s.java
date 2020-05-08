 /*
  * This file is part of Common Implementation + API for MineQuest, This provides
  * the common classes for the MineQuest official implementation of the API..
  * Common Implementation + API for MineQuest is licensed under GNU General
  * Public License v3.
  * Copyright (C) The MineQuest Team
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.theminequest.common.quest.v1;
 
 import static com.theminequest.common.util.I18NMessage._;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.logging.Level;
 
 import com.theminequest.api.CompleteStatus;
 import com.theminequest.api.Managers;
 import com.theminequest.api.platform.event.TaskStartedEvent;
 import com.theminequest.api.quest.Quest;
 import com.theminequest.api.quest.QuestTask;
 import com.theminequest.api.quest.QuestUtils;
 import com.theminequest.api.quest.event.QuestEvent;
 import com.theminequest.api.quest.event.TargetedQuestEvent;
 import com.theminequest.common.Common;
 import com.theminequest.common.impl.v1parser.TargetEventHandler;
 
 /**
  * V1Task operates according to the 0.70 MineQuest System,
  * in which tasks started would toggle events, and then exit
  * immediately. This means that starting another task would NOT
  * cause the other one to cancel along with all of its other events.
  * 
  * Event numbers toggled are stored in the QuestDetails object that
  * accompanies the Quest. Events run normally; and if an event needs
  * to be canceled, a CancelEvent (which only works with V1Task) can
  * be issued to cancel the event.
  * 
  */
 public class V1Task implements QuestTask {
 	
 	private Quest quest;
 	private int taskid;
 	private Map<Integer, QuestEvent> questEvents;
 	private Collection<Integer> calledEvents;
 	
 	private V1Task successor;
 	
 	private transient Object lock;
 	
 	/**
 	 * Initialize a new V1Task.
 	 * 
 	 * @param quest
 	 *            Quest to link to
 	 * @param taskid
 	 *            Task ID of this task (last called)
 	 * @param events
 	 *            Collection of events to start, or null to initialize
 	 *            previously used ones.
 	 * @param questEvents
 	 *            Objects of the Quest Events
 	 */
 	public V1Task(Quest quest, int taskid, Collection<Integer> events,
 			V1Task lastTask) {
 		this.quest = quest;
 		this.taskid = taskid;
 		this.calledEvents = events;
 		this.lock = new Object();
 		if (lastTask != null) {
 			questEvents = lastTask.questEvents;
 			lastTask.successor = this;
 		} else
 			questEvents = null;
 		
 		this.successor = null;
 	}
 	
 	@Override
 	public void start() {
 		CopyOnWriteArraySet<Integer> running = quest.getDetails().getProperty(V1QuestDetails.V1_OLDSTYLERUNNING);
 		if (running == null) {
 			running = new CopyOnWriteArraySet<Integer>();
 			quest.getDetails().setProperty(V1QuestDetails.V1_OLDSTYLERUNNING, running);
 		}
 		
 		if (calledEvents == null) { // then start all events previously
 									// contained
 			// within v1.tasks
 			calledEvents = running;
 			running = new CopyOnWriteArraySet<Integer>(); // dereference to
 															// avoid conflicts;
 		}
 		
 		Map<Integer, QuestEvent> objects = new HashMap<Integer, QuestEvent>();
 		
 		for (Integer event : calledEvents) {
 			
 			if (running.contains(event)) // event already there, ignore
 											// recalling of it
 				continue;
 			
 			String d = QuestUtils.getEvent(quest, event);
 			if (d == null) {
 				Managers.logf(Level.WARNING, "[Common|V1Task] Missing event number %s in V1Task %s for quest %s/%s; Ignoring.", event, taskid, quest.getQuestOwner(), quest.getDetails().getName());
 				running.remove(event); // is really needed?
 				continue;
 			}
 			
 			String eventName = d.substring(0, d.indexOf(":"));
 			String[] details = d.substring(d.indexOf(":") + 1).split(":");
 			int targeted = -1;
 			long delayMS = -1;
 			if (details.length > 0 && details[0].equals(TargetEventHandler.TARGETED_EVENT_STR)) { // targeted...
				targeted = Integer.parseInt(details[1]);
				delayMS = Long.parseLong(details[2]);
 				details = Arrays.copyOfRange(details, 3, details.length);
 			}
 			
 			QuestEvent eventObject = Common.getCommon().getV1EventManager().constructEvent(eventName, this, event, details);
 			
 			if (targeted != -1) {
 				if (!(eventObject instanceof TargetedQuestEvent)) {
 					Managers.logf(Level.SEVERE, "[Common|V1Task] Event %s in V1Task %s for quest %s/%s is NOT a TargetEvent!", event, taskid, quest.getQuestOwner(), quest.getDetails().getName());
 					running.remove(event); // is really needed?
 					continue;
 				}
 				((TargetedQuestEvent) eventObject).setupTarget(targeted, delayMS);
 			}
 			
 			if (eventObject != null) {
 				running.add(event);
 				objects.put(event, eventObject);
 			} else
 				Managers.logf(Level.WARNING, "[Common|V1Task] Unknown event %s requested in event number %s for quest %s/%s; Ignoring.", eventName, event, quest.getQuestOwner(), quest.getDetails().getName());
 		}
 		
 		Common.getCommon().getV1EventManager().registerEventListeners(Collections.unmodifiableCollection(objects.values()));
 		for (QuestEvent o : objects.values())
 			o.fireEvent();
 		
 		if (questEvents == null)
 			questEvents = new HashMap<Integer, QuestEvent>();
 		questEvents.putAll(objects);
 		
 		TaskStartedEvent event = new TaskStartedEvent(this);
 		Managers.getPlatform().callEvent(event);
 	}
 	
 	@Override
 	public CompleteStatus isComplete() {
 		return null;
 	}
 	
 	@Override
 	public Quest getQuest() {
 		return quest;
 	}
 	
 	@Override
 	public int getTaskID() {
 		return -3;
 	}
 	
 	@Override
 	public Collection<QuestEvent> getEvents() {
 		return Collections.unmodifiableCollection(questEvents.values());
 	}
 	
 	/**
 	 * V1Task does nothing with checkTasks.
 	 * In V1, it is up to the Quest Author to end the
 	 * quest responsibly.
 	 */
 	@Override
 	public void checkTasks() {
 	}
 	
 	@Override
 	public String getTaskDescription() {
 		return _("Follow the directions onscreen!");
 	}
 	
 	@Override
 	public void completeEvent(QuestEvent event, CompleteStatus status) {
 		synchronized (lock) {
 			completeEvent(event, status, -2);
 		}
 	}
 	
 	@Override
 	public void completeEvent(QuestEvent event, CompleteStatus status, int nextTask) {
 		synchronized (lock) {
 			if (successor != null) {
 				successor.completeEvent(event, status, nextTask);
 				return;
 			}
 			CopyOnWriteArraySet<Integer> running = quest.getDetails().getProperty(V1QuestDetails.V1_OLDSTYLERUNNING);
 			
 			// Possible better way to do this?
 			Iterator<Integer> iter = running.iterator();
 			while (iter.hasNext()) {
 				int next = iter.next();
 				if (questEvents.get(next).equals(event)) {
 					running.remove(next);
 					break;
 				}
 			}
 			
 			Common.getCommon().getV1EventManager().deregisterEventListener(event);
 			
 			if (status == CompleteStatus.FAIL || status == CompleteStatus.ERROR) {
 				quest.completeTask(this, status, -1);
 			} else if (status == CompleteStatus.SUCCESS || status == CompleteStatus.WARNING) {
 				if (nextTask != -2) {
 					// quest.getActiveTask().cancelTask(); (not for V1Task)
 					quest.startTask(nextTask);
 				}// else (not for V1Task)
 					// quest.getActiveTask().checkTasks();
 			}
 		}
 	}
 	
 	@Override
 	public void cancelTask() {
 		// Called from Quest. All events need to stop, drop.
 		if (successor != null) {
 			successor.cancelTask();
 			return;
 		}
 		for (QuestEvent e : questEvents.values())
 			e.complete(CompleteStatus.CANCELED);
 	}
 	
 	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
 		in.defaultReadObject();
 		lock = new Object();
 	}
 	
 }

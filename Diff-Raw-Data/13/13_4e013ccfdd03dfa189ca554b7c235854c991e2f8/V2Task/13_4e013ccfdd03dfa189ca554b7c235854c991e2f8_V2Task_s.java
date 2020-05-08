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
 /*
  * This file is part of MineQuest, The ultimate MMORPG plugin!.
  * MineQuest is licensed under GNU General Public License v3.
  * Copyright (C) 2012 The MineQuest Team
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
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
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
 import com.theminequest.common.impl.event.NameEvent;
 import com.theminequest.common.impl.v1parser.TargetEventHandler;
 
 /**
  * V2Task operates according to the 1.2.5 MineQuest System,
  * which each set of events run linked to a task.
  * Whenever a new task is toggled, all events linked to the old task
  * are canceled, and the task itself canceled as well.
  * 
  */
 public class V2Task implements QuestTask {
 	
 	/**
 	 * 
 	 */
 	private boolean started;
 	private volatile CompleteStatus complete;
 	private Quest quest;
 	private int taskid;
 	private HashMap<Integer, QuestEvent> collection;
 	
 	private Object lock;
 	
 	/**
 	 * Task for a Quest.
 	 * 
 	 * @param questid
 	 *            Associated Quest
 	 * @param taskid
 	 *            Task ID
 	 * @param events
 	 *            Event numbers that must be completed
 	 */
 	public V2Task(Quest quest, int taskid, Set<Integer> events) {
 		started = false;
 		complete = null;
 		this.quest = quest;
 		this.taskid = taskid;
 		collection = new HashMap<Integer, QuestEvent>();
 		for (int e : events)
 			collection.put(e, null);
 		
 		lock = new Object();
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.theminequest.MineQuest.Tasks.QuestTask#start()
 	 */
 	@Override
 	public void start() {
 		if (started)
 			return;
 		started = true;
 		List<Integer> list = new ArrayList<Integer>(collection.keySet());
 		for (Integer event : list) {
 			String d = QuestUtils.getEvent(quest, event);
 			if (d == null) {
 				Managers.logf(Level.WARNING, "[Common|V2Task] Missing event number %s in V1Task %s for quest %s/%s; Ignoring.", event, taskid, quest.getQuestOwner(), quest.getDetails().getName());
 				collection.remove(event);
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
 					Managers.logf(Level.SEVERE, "[Common|V2Task] Event %s in V1Task %s for quest %s/%s is NOT a TargetEvent!", event, taskid, quest.getQuestOwner(), quest.getDetails().getName());
 					continue;
 				}
 				((TargetedQuestEvent) eventObject).setupTarget(targeted, delayMS);
 			}
 
 			if (eventObject != null)
 				collection.put(event, eventObject);
 			else {
 				Managers.logf(Level.WARNING, "[Common|V2Task] Unknown event %s requested in event number %s for quest %s/%s; Ignoring.", eventName, event, quest.getQuestOwner(), quest.getDetails().getName());
 				collection.remove(event);
 			}
 		}
 		
 		Common.getCommon().getV1EventManager().registerEventListeners(Collections.unmodifiableCollection(collection.values()));
 		for (QuestEvent e : collection.values())
 			e.fireEvent();
 		
 		TaskStartedEvent event = new TaskStartedEvent(this);
 		Managers.getPlatform().callEvent(event);
 	}
 	
 	@Override
 	public void checkTasks() {
 		for (QuestEvent e : collection.values()) {
 			if (e.isComplete() == null && !(e instanceof NameEvent))
 				return;
 		}
 		
 		complete = CompleteStatus.SUCCESS;
 		
 		for (QuestEvent e : collection.values()) {
 			if (e instanceof NameEvent)
 				e.complete(CompleteStatus.IGNORE);
 		}
 		
 		// -2 to denote quest specific behaviour
 		quest.completeTask(this, CompleteStatus.SUCCESS, -2);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.theminequest.MineQuest.Tasks.QuestTask#isComplete()
 	 */
 	@Override
 	public CompleteStatus isComplete() {
 		return complete;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.theminequest.MineQuest.Tasks.QuestTask#getQuestID()
 	 */
 	@Override
 	public Quest getQuest() {
 		return quest;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.theminequest.MineQuest.Tasks.QuestTask#getTaskID()
 	 */
 	@Override
 	public int getTaskID() {
 		return taskid;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.theminequest.MineQuest.Tasks.QuestTask#getEvents()
 	 */
 	@Override
 	public Collection<QuestEvent> getEvents() {
 		return collection.values();
 	}
 	
 	@Override
 	public String getTaskDescription() {
 		String desc = quest.getDetails().getProperty(V1QuestDetails.V2_TASKDESCRIPTIONS);
 		if (desc == null)
 			return "Follow the directions onscreen!";
 		return desc;
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
 			Common.getCommon().getV1EventManager().deregisterEventListener(event);
 			
 			if (complete != null)
 				return;
 			
 			if (status == CompleteStatus.FAIL || status == CompleteStatus.ERROR) {
 				complete = status;
 				quest.completeTask(this, status, -1);
 			} else if (status == CompleteStatus.SUCCESS || status == CompleteStatus.WARNING) {
 				if (nextTask != -2) {
 					complete = status;
 					quest.completeTask(this, status, nextTask);
 				} else
 					checkTasks();
 			}
 			
 		}
 	}
 	
 	@Override
 	public void cancelTask() {
 		complete = CompleteStatus.CANCELED;
 		for (QuestEvent e : collection.values())
 			e.complete(CompleteStatus.CANCELED);
 	}
 	
 }

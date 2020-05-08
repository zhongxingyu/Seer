 /**
  * This file, Task.java, is part of MineQuest:
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
 package com.theminequest.MineQuest.Tasks;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 
 import com.theminequest.MQCoreEvents.NameEvent;
 import com.theminequest.MineQuest.API.CompleteStatus;
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.BukkitEvents.TaskCompleteEvent;
 import com.theminequest.MineQuest.API.Events.QuestEvent;
 import com.theminequest.MineQuest.API.Quest.Quest;
 import com.theminequest.MineQuest.API.Quest.QuestUtils;
 import com.theminequest.MineQuest.API.Task.QuestTask;
 
 public class Task implements QuestTask {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6544586331188563646L;
 	private boolean started;
 	private CompleteStatus complete;
 	private Quest quest;
 	private int taskid;
 	private LinkedHashMap<Integer,QuestEvent> collection;
 
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
 	public Task(Quest quest, int taskid, List<Integer> events) {
 		started = false;
 		complete = null;
 		this.quest = quest;
 		this.taskid = taskid;
 		collection = new LinkedHashMap<Integer,QuestEvent>();
 		for (int e : events){
 			collection.put(e, null);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Tasks.QuestTask#start()
 	 */
 	@Override
 	public synchronized void start() {
 		if (started)
 			return;
 		started = true;
 		Iterator<Integer> i = collection.keySet().iterator();
 		List<Integer> list = new ArrayList<Integer>();
 		while (i.hasNext()){
 			list.add(i.next());
 		}
 		for (Integer event : list){
 			String d = QuestUtils.getEvent(quest,event);
 			String[] eventdetails = d.split(":");
 			String recombined = "";
 			for (int r=1; r<eventdetails.length; r++){
 				recombined+=eventdetails[r];
 				if (r!=(eventdetails.length-1));
 					recombined+=":";
 			}
 			QuestEvent e = Managers.getEventManager().constructEvent(eventdetails[0], quest, event, recombined);
 			if (e!=null)
 				collection.put(event, e);
 			else{
 				Managers.log(Level.WARNING, "[Task] Missing event " + eventdetails[0] + "; Ignoring.");
 				collection.remove(event);
 			}
 		}
 		
 		i = collection.keySet().iterator();
 		while (i.hasNext()){
 			collection.get(i.next()).fireEvent();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Tasks.QuestTask#cancelTask()
 	 */
 	@Override
 	public synchronized void cancelTask() {
 		if (complete!=null || !started)
 			return;
 		complete = CompleteStatus.CANCELED;
 		for (QuestEvent e : collection.values()) {
 			e.complete(CompleteStatus.CANCELED);
 		}
 		TaskCompleteEvent e = new TaskCompleteEvent(quest, taskid,
 				CompleteStatus.CANCELED);
 		Bukkit.getPluginManager().callEvent(e);
 	}
 
 	// FIXME FIXME FIXME AHHHHHH
 	// implement task switching.
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Tasks.QuestTask#finishEvent(int, com.theminequest.MineQuest.API.CompleteStatus)
 	 */
 	@Override
 	public synchronized void finishEvent(QuestEvent qE,
 			CompleteStatus completeStatus) {
 		if (complete==null && started && collection.containsKey(qE.getEventId())) {
 			
 			switch(completeStatus){
 			case FAILURE:
 				for (QuestEvent event : collection.values())
 					event.complete(CompleteStatus.CANCELED);
 				complete = CompleteStatus.FAILURE;
 				TaskCompleteEvent e = new TaskCompleteEvent(quest, taskid,
 						CompleteStatus.FAILURE);
 				Bukkit.getPluginManager().callEvent(e);
 				break;
 			case CANCELED:
 				break;
 			default:
 				Integer taskswitch = qE.switchTask();
 				if (taskswitch!=null){
 					for (QuestEvent event : collection.values())
 						event.complete(CompleteStatus.CANCELED);
 					complete = CompleteStatus.IGNORE;
 					getQuest().startTask(taskswitch);
					TaskCompleteEvent e1 = new TaskCompleteEvent(quest, taskid,
							CompleteStatus.IGNORE);
					Bukkit.getPluginManager().callEvent(e1);
 					return;
 				}
 				checkCompletion();
 			}
 		}
 	}
 
 	private synchronized void checkCompletion() {
 		for (Integer eventid : collection.keySet()) {
 			QuestEvent e = collection.get(eventid);
 			// ignore NameEvents
 			if (e instanceof NameEvent)
 				continue;
 			if (e.isComplete()==null)
 				return;
 		}
 		complete = CompleteStatus.SUCCESS;
 		TaskCompleteEvent e = new TaskCompleteEvent(quest, taskid,
 				CompleteStatus.SUCCESS);
 		Bukkit.getPluginManager().callEvent(e);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Tasks.QuestTask#isComplete()
 	 */
 	@Override
 	public synchronized CompleteStatus isComplete() {
 		return complete;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Tasks.QuestTask#getQuestID()
 	 */
 	@Override
 	public Quest getQuest() {
 		return quest;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Tasks.QuestTask#getTaskID()
 	 */
 	@Override
 	public int getTaskID() {
 		return taskid;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Tasks.QuestTask#getEvents()
 	 */
 	@Override
 	public Collection<QuestEvent> getEvents() {
 		return collection.values();
 	}
 
 }

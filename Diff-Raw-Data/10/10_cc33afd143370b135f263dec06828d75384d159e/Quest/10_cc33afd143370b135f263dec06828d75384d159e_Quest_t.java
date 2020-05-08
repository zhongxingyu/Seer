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
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.theminequest.MineQuest.Quest;
 
 import static com.theminequest.MineQuest.API.Quest.QuestDetails.QUEST_EDITS;
 import static com.theminequest.MineQuest.API.Quest.QuestDetails.QUEST_LOADWORLD;
 import static com.theminequest.MineQuest.API.Quest.QuestDetails.QUEST_NAME;
 import static com.theminequest.MineQuest.API.Quest.QuestDetails.QUEST_NETHERWORLD;
 import static com.theminequest.MineQuest.API.Quest.QuestDetails.QUEST_TASKS;
 import static com.theminequest.MineQuest.API.Quest.QuestDetails.QUEST_WORLD;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World.Environment;
 import org.bukkit.WorldCreator;
 
 import com.theminequest.MineQuest.API.CompleteStatus;
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.BukkitEvents.QuestCompleteEvent;
 import com.theminequest.MineQuest.API.BukkitEvents.QuestStartedEvent;
 import com.theminequest.MineQuest.API.BukkitEvents.TaskCompleteEvent;
 import com.theminequest.MineQuest.API.Edit.Edit;
 import com.theminequest.MineQuest.API.Group.QuestGroup;
 import com.theminequest.MineQuest.API.Group.QuestGroupManager;
 import com.theminequest.MineQuest.API.Quest.QuestDetails;
 import com.theminequest.MineQuest.API.Quest.QuestSnapshot;
 import com.theminequest.MineQuest.API.Quest.QuestUtils;
 import com.theminequest.MineQuest.API.Task.QuestTask;
 import com.theminequest.MineQuest.API.Tracker.QuestStatisticUtils;
 import com.theminequest.MineQuest.API.Utils.SetUtils;
 import com.theminequest.MineQuest.API.Utils.TimeUtils;
 import com.theminequest.MineQuest.Tasks.V1Task;
 import com.theminequest.MineQuest.Tasks.V2Task;
 
 
 public class Quest implements com.theminequest.MineQuest.API.Quest.Quest {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -7904219637011746046L;
 	
 	private QuestDetails details;
 	
 	private long questid;
 	
 	private CompleteStatus finished;
 	private QuestTask activeTask;
 	
 	private String questOwner;
 	
 	private boolean initialized;
 	
 	protected static Quest newInstance(long questid, QuestDetails id, String questOwner){
 		return new Quest(questid,id,questOwner);
 	}
 	
 	private Quest(long questid, QuestDetails id, String questOwner) {
 		details = id;
 		this.questid = questid;
 		activeTask = null;
 		this.questOwner = questOwner;
 		initialized = false;
 		
 		// sort the tasks, events, and targets in order of id.
 		// because we have absolutely 0 idea if someone would skip numbers...
 		
 		// load the world if necessary/move team to team leader
 		String world = details.getProperty(QUEST_WORLD);
 		if (Bukkit.getWorld(world) == null) {
 			WorldCreator w = new WorldCreator(world);
 			if (details.getProperty(QUEST_NETHERWORLD))
 				w = w.environment(Environment.NETHER);
 			Bukkit.createWorld(w);
 		}
 		if (details.getProperty(QUEST_LOADWORLD)) {
 			try {
 				world = QuestWorldManip.copyWorld(Bukkit.getWorld(world)).getName();
 				details.setProperty(QUEST_WORLD,world);
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		// enable edits
 		Map<Integer,Edit> edits = details.getProperty(QUEST_EDITS);
 		for (Edit e : edits.values())
 			e.startEdit(this);
 		
 		// plugins should use QuestStartedEvent to setup their properties
 		// inside the quest.
 		QuestStartedEvent event = new QuestStartedEvent(this);
 		Bukkit.getPluginManager().callEvent(event);
 	}
 	
 	public synchronized void startQuest(){
 		Map<Integer,String[]> tasks = details.getProperty(QUEST_TASKS);
 		if (!startTask(SetUtils.getFirstKey(tasks.keySet()))) {
 			Managers.log(Level.SEVERE, "Starting initial task for " + details.getProperty(QUEST_NAME) + "/" + getQuestOwner() + " failed!");
 			finishQuest(CompleteStatus.ERROR);
 		}
 	}
 	
 	/**
 	 * Start a task of the quest.
 	 * 
 	 * @param taskid
 	 *            task to start
 	 * @return true if task was started successfully
 	 */
 	public synchronized boolean startTask(int taskid) {
 		Map<Integer,String[]> tasks = details.getProperty(QUEST_TASKS);
 		if (taskid == -1) {
 			finishQuest(CompleteStatus.SUCCESS);
 			return true;
 		}
 		
		boolean detailsToggle = false;
		if (details.getProperty(V1Task.DETAILS_TOGGLE) != null)
			detailsToggle = details.getProperty(V1Task.DETAILS_TOGGLE);
		
 		// well, this is slightly hacky.
		if (!initialized && detailsToggle) {
 			activeTask = new V1Task(this, taskid, null, null);
 			activeTask.start();
 			initialized = true;
 		}
 		
 		if (!tasks.containsKey(taskid))
 			return false;
 		if (activeTask!=null) {
 			if (activeTask.isComplete()==null)
 				activeTask.cancelTask();
 			
 			if (activeTask.getTaskID() == taskid)
 				return false;
 		}
 		
 		String[] eventnums = tasks.get(taskid);
 		List<Integer> eventnum = new ArrayList<Integer>();
 		for (String e : eventnums) {
 			eventnum.add(Integer.parseInt(e));
 		}
 		
		if (detailsToggle)
 			activeTask = new V1Task(this, taskid, eventnum, (V1Task)activeTask);
 		else
 			activeTask = new V2Task(this, taskid, eventnum);
 		
 		activeTask.start();
 		
 		// main world quest
 		if (questid == -1) {
 			QuestStatisticUtils.checkpointQuest(this);
 		}
 		return true;
 	}
 	
 	public boolean isInstanced(){
 		return details.getProperty(QUEST_LOADWORLD);
 	}
 	
 	public synchronized QuestTask getActiveTask() {
 		return activeTask;
 	}
 	
 	// passed in from QuestManager
 	public synchronized void onTaskCompletion(TaskCompleteEvent e) {
 		if (!e.getQuest().equals(this))
 			return;
 		if (e.getResult()==CompleteStatus.CANCELED || e.getResult()==CompleteStatus.IGNORE)
 			return;
 		else if (e.getResult()==CompleteStatus.FAILURE || e.getResult()==CompleteStatus.ERROR)
 			finishQuest(e.getResult());
 		else
 			startTask(QuestUtils.getNextTask(this));
 	}
 	
 	public synchronized void finishQuest(CompleteStatus c) {
 		finished = c;
 		if (activeTask!=null && activeTask.isComplete()==null)
 			activeTask.completeTask(CompleteStatus.CANCELED);
 		activeTask = null;
 		Map<Integer,Edit> edits = details.getProperty(QUEST_EDITS);
 		for (Edit e : edits.values())
 			e.dismantle();
 		String world = details.getProperty(QUEST_WORLD);
 		TimeUtils.unlock(Bukkit.getWorld(world));
 		QuestGroupManager qGM = Managers.getQuestGroupManager();
 		QuestGroup g = qGM.get(this);
 		QuestCompleteEvent event = new QuestCompleteEvent(this, c, g);
 		Bukkit.getPluginManager().callEvent(event);
 	}
 	
 	public void cleanupQuest() {
 		if (details.getProperty(QUEST_LOADWORLD)){
 			try {
 				QuestWorldManip.removeWorld(Bukkit.getWorld((String) details.getProperty(QUEST_WORLD)));
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		activeTask = null;
 		questOwner = null;
 		questid = -1;
 	}
 	
 	public synchronized CompleteStatus isFinished() {
 		return finished;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object arg0) {
 		if (arg0==null)
 			return false;
 		if (!(arg0 instanceof Quest))
 			return false;
 		Quest q = (Quest) arg0;
 		return (q.questid == this.questid) && (q.getQuestOwner().equals(this.getQuestOwner()) && q.getDetails().equals(this.getDetails()));
 	}
 	
 	@Override
 	public synchronized String toString() {
 		return details.toString() + ":" + getQuestOwner() + ":" + getQuestID();
 	}
 	
 	@Override
 	public int compareTo(com.theminequest.MineQuest.API.Quest.Quest arg0) {
 		return ((Long)getQuestID()).compareTo(arg0.getQuestID());
 	}
 	
 	@Override
 	public long getQuestID() {
 		return questid;
 	}
 	
 	@Override
 	public com.theminequest.MineQuest.API.Quest.QuestDetails getDetails() {
 		return details;
 	}
 	
 	@Override
 	public String getQuestOwner() {
 		return questOwner;
 	}
 	
 	@Override
 	public QuestSnapshot createSnapshot() {
 		return new com.theminequest.MineQuest.Quest.QuestSnapshot(this);
 	}
 	
 }

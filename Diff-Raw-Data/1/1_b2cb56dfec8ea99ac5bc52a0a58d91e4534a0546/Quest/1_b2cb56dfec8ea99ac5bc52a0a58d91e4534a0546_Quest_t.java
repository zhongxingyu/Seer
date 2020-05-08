 /**
  * This file, Quest.java, is part of MineQuest:
  * A full featured and customizable quest/mission system.
  * Copyright (C) 2012 The MineQuest Team
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
 package com.theminequest.MineQuest.Quest;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.BukkitEvents.CompleteStatus;
 import com.theminequest.MineQuest.BukkitEvents.QuestCompleteEvent;
 import com.theminequest.MineQuest.BukkitEvents.QuestStartedEvent;
 import com.theminequest.MineQuest.BukkitEvents.TaskCompleteEvent;
 import com.theminequest.MineQuest.Editable.AreaEdit;
 import com.theminequest.MineQuest.Editable.CertainBlockEdit;
 import com.theminequest.MineQuest.Editable.CoordinateEdit;
 import com.theminequest.MineQuest.Editable.Edit;
 import com.theminequest.MineQuest.Editable.InsideAreaEdit;
 import com.theminequest.MineQuest.Editable.ItemInHandEdit;
 import com.theminequest.MineQuest.Editable.OutsideAreaEdit;
 import com.theminequest.MineQuest.EventsAPI.QEvent;
 import com.theminequest.MineQuest.Group.Group;
 import com.theminequest.MineQuest.Group.Team;
 import com.theminequest.MineQuest.Target.TargetDetails;
 import com.theminequest.MineQuest.Tasks.Task;
 import com.theminequest.MineQuest.Utils.TimeUtils;
 
 public class Quest {
 
 	protected String questname;
 	protected long questid;
 	protected boolean started;
 	protected int currenttask;
 
 	protected CompleteStatus finished;
 
 	// always <ID #,OBJECT/DETAILS>
 	// TreeMap guarantees key order.
 	// (yes, treemap is RESOURCE intensive D:,
 	// but I have to combine it with LinkedHashMap to ensure there
 	// will be no duplicates)
 	protected TreeMap<Integer, String> tasks;
 	protected Task activeTask;
 	protected TreeMap<Integer, String> events;
 	protected TreeMap<Integer, TargetDetails> targets;
 	protected TreeMap<Integer, Edit> editables;
 	// quest configuration
 	protected String displayname;
 	protected String displaydesc;
 	protected String displayaccept;
 	protected String displaycancel;
 	protected String displayfinish;
 	protected boolean questRepeatable;
 	protected boolean spawnReset;
 	/**
 	 * Controls the Spawn Point for the Quest (x,y,z)
 	 */
 	protected double[] spawnPoint;
 	/**
 	 * Controls the area to preserve (uneditable) (x,y,z,x,y,z)
 	 */
 	protected double[] areaPreserve;
 	protected String editMessage;
 	protected String world;
 	protected boolean loadworld;
 
 	/*
 	 * Constructor will start the quest for the user.
 	 */
 	protected Quest(long questid, String id) {
 		MineQuest.log(Level.WARNING, "5");
 		questname = id;
 		this.questid = questid;
 		started = false;
 		currenttask = -1;
 		// DEFAULTS start
 		displayname = questname;
 		displaydesc = "This is a quest.";
 		displayaccept = "You have accepted the quest.";
 		displaycancel = "You have canceled the quest.";
 		displayfinish = "You have finished the quest.";
 		questRepeatable = false;
 		spawnReset = true;
 
 		spawnPoint = new double[3];
 		spawnPoint[0] = 0;
 		spawnPoint[1] = 64;
 		spawnPoint[2] = 0;
 
 		areaPreserve = new double[6];
 		areaPreserve[0] = 0;
 		areaPreserve[1] = 64;
 		areaPreserve[2] = 0;
 		areaPreserve[3] = 0;
 		areaPreserve[4] = 64;
 		areaPreserve[5] = 0;
 
 		editMessage = ChatColor.GRAY + "You cannot edit inside a quest.";
 		MineQuest.log(Level.WARNING, "6");
 		world = Bukkit.getWorlds().get(0).getName();
 		loadworld = false;
 		
 		activeTask = null;
 
 		// DEFAULTS end
 		MineQuest.log(Level.WARNING, "7");
 		try {
 			QuestParser.parseDefinition(this);
 		} catch (FileNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 
 		// sort the tasks, events, and targets in order of id.
 		// because we have absolutely 0 idea if someone would skip numbers...
 
 		MineQuest.log(Level.WARNING, "14");
 		// load the world if necessary/move team to team leader
 		if (Bukkit.getWorld(world) == null)
 			Bukkit.createWorld(new WorldCreator(world));
 		if (loadworld) {
 			MineQuest.log(Level.WARNING, "15");
 			try {
 				world = QuestWorldManip.copyWorld(Bukkit.getWorld(world))
 						.getName();
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		MineQuest.log(Level.WARNING, "16");
 		QuestStartedEvent event = new QuestStartedEvent(this);
 		Bukkit.getPluginManager().callEvent(event);
 		MineQuest.log(Level.WARNING, "16.5");
 		startTask(getFirstKey(tasks.keySet()));
 	}
 	
 	private Integer getFirstKey(Set<Integer> s){
 		int first = Integer.MAX_VALUE;
 		for (int i : s){
 			if (i<first)
 				first = i;
 		}
 		return first;
 	}
 
 	/**
 	 * Get all possible events
 	 * 
 	 * @return all possible events (# association)
 	 */
 	public Set<Integer> getEventNums() {
 		return events.keySet();
 	}
 
 	/**
 	 * Start a task of the quest.
 	 * @param taskid task to start
 	 * @return true if task was started successfully
 	 */
 	public boolean startTask(int taskid){
 		MineQuest.log(Level.WARNING, "17");
 		if (taskid==-1){
 			finishQuest(CompleteStatus.SUCCESS);
 			return true;
 		}
 		MineQuest.log(Level.WARNING, "18");
 		if (!tasks.containsKey(taskid))
 			return false;
 		currenttask = taskid;
 		String[] eventnums = tasks.get(taskid).split(":");
 		List<Integer> eventnum = new ArrayList<Integer>();
 		for (String e : eventnums){
 			eventnum.add(Integer.parseInt(e));
 		}
 		MineQuest.log(Level.WARNING, "19");
 		activeTask = new Task(questid,taskid,eventnum);
 		activeTask.start();
 		MineQuest.log(Level.WARNING, "20");
 		return true;
 	}
 	
 	public Task getActiveTask(){
 		return activeTask;
 	}
 
 	public void finishQuest(CompleteStatus c){
		finished = c;
 		TimeUtils.unlock(Bukkit.getWorld(world));
 		Bukkit.unloadWorld(Bukkit.getWorld(world), false);
 		Group g = MineQuest.groupManager.getGroup(MineQuest.groupManager.indexOfQuest(this));
 		QuestCompleteEvent event = new QuestCompleteEvent(questid,c,g);
 		Bukkit.getPluginManager().callEvent(event);
 	}
 	
 	public CompleteStatus isFinished(){
 		return finished;
 	}
 	
 	/**
 	 * 
 	 * @param eventid
 	 * @return the string description of the event; null if not found.
 	 */
 	public String getEventDesc(int eventid) {
 		return events.get(eventid);
 	}
 
 	/**
 	 * Get the "YOU CAN'T EDIT THIS PLACE" message...
 	 * @return cannot edit message
 	 */
 	public String getEditMessage(){
 		return editMessage;
 	}
 
 	/**
 	 * Retrieve the current task ID.
 	 * @return Current Task ID.
 	 */
 	public int getCurrentTaskID(){
 		return currenttask;
 	}
 
 	public String getTaskDetails(int id) {
 		return tasks.get(id);
 	}
 
 	public long getID() {
 		return questid;
 	}
 
 	public String getWorld() {
 		return world;
 	}
 
 	/**
 	 * Retrieve the target specification.
 	 * 
 	 * @param id
 	 *            target ID
 	 * @return specification, or <code>null</code> if there is no such target
 	 *         id.
 	 */
 	public TargetDetails getTarget(int id) {
 		return targets.get(id);
 	}
 	
 	public List<String> getDisallowedAbilities() {
 		// TODO not done yet
 		return new ArrayList<String>();
 	}
 	
 	public Location getSpawnLocation(){
 		return new Location(Bukkit.getWorld(world),spawnPoint[0],spawnPoint[1],spawnPoint[2]);
 	}
 
 	// passed in from QuestManager
 	public void onTaskCompletion(TaskCompleteEvent e) {
 		if (e.getQuestID() != questid)
 			return;
 		// TODO this is lovely and all, but tasks should trigger other tasks...
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object arg0) {
 		if (!(arg0 instanceof Quest))
 			return false;
 		Quest q = (Quest)arg0;
 		return (q.questid==this.questid);
 	}
 
 	public String getName() {
 		return displayname;
 	}
 
 	public String getDescription() {
 		return displaydesc;
 	}
 
 }

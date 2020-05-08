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
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.World.Environment;
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
 
 	public String questname;
 	public long questid;
 	public boolean started;
 	public int currenttask;
 
 	public CompleteStatus finished;
 
 	// always <ID #,OBJECT/DETAILS>
 	public LinkedHashMap<Integer, String[]> tasks;
 	public Task activeTask;
 	public LinkedHashMap<Integer, String> events;
 	public LinkedHashMap<Integer, TargetDetails> targets;
 	public LinkedHashMap<Integer, Edit> editables;
 	// quest configuration
 	public String displayname;
 	public String displaydesc;
 	public String displayaccept;
 	public String displaycancel;
 	public String displayfinish;
 	public boolean questRepeatable;
 	public boolean spawnReset;
 	/**
 	 * Controls the Spawn Point for the Quest (x,y,z)
 	 */
 	public double[] spawnPoint;
 	/**
 	 * Controls the area to preserve (uneditable) (x,y,z,x,y,z)
 	 */
 	public double[] areaPreserve;
 	public String editMessage;
 	public String world;
 	public boolean loadworld;
 	public boolean nether;
 	
 	/**
 	 * For addons to store their data
 	 */
 	public Map<String,Object> database;
 
 	/*
 	 * Constructor will start the quest for the user.
 	 */
 	protected Quest(long questid, String id) {
 		questname = id;
 		this.questid = questid;
 		started = false;
 		currenttask = -1;
 		// DEFAULTS start
 		displayname = questname;
 		displaydesc = MineQuest.configuration.localizationConfig.getString("quest_NODESC", "No description available.");
 		displayaccept = MineQuest.configuration.localizationConfig.getString("quest_ACCEPT", "Quest accepted!");
 		displaycancel = MineQuest.configuration.localizationConfig.getString("quest_CANCEL", "Quest aborted!");
 		displayfinish = MineQuest.configuration.localizationConfig.getString("quest_COMPLETE", "You've just completed this quest. Did you enjoy it?");
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
 
 		editMessage = ChatColor.GRAY + MineQuest.configuration.localizationConfig.getString("quest_DEFEDIT", "You can't edit the world!");;
 		world = Bukkit.getWorlds().get(0).getName();
 		loadworld = false;
 
 		activeTask = null;
 		
 		database = Collections.synchronizedMap(new LinkedHashMap<String,Object>());
 
 		// DEFAULTS end
 		try {
 			MineQuest.questManager.parser.parseDefinition(this);
 		} catch (FileNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 
 		// sort the tasks, events, and targets in order of id.
 		// because we have absolutely 0 idea if someone would skip numbers...
 
 		// load the world if necessary/move team to team leader
 		if (Bukkit.getWorld(world) == null) {
 			WorldCreator w = new WorldCreator(world);
 			if (nether)
 				w = w.environment(Environment.NETHER);
 			Bukkit.createWorld(w);
 		}
 		if (loadworld) {
 			try {
 				world = QuestWorldManip.copyWorld(Bukkit.getWorld(world))
 						.getName();
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		QuestStartedEvent event = new QuestStartedEvent(this);
 		Bukkit.getPluginManager().callEvent(event);
 	}
 
 	private Integer getFirstKey(Set<Integer> s) {
 		int first = Integer.MAX_VALUE;
 		Iterator<Integer> it = s.iterator();
 		while (it.hasNext()) {
 			int i = it.next();
 			if (i < first)
 				first = i;
 		}
 		return first;
 
 	}
 
 	private ArrayList<Integer> getSortedKeys(Set<Integer> s) {
 		ArrayList<Integer> a = new ArrayList<Integer>();
 		Iterator<Integer> i = s.iterator();
 		while (i.hasNext())
 			a.add(i.next());
 		Collections.sort(a);
 		return a;
 	}
 
 	/**
 	 * Get all possible events
 	 * 
 	 * @return all possible events (# association)
 	 */
 	public Set<Integer> getEventNums() {
 		return events.keySet();
 	}
 	
 	public boolean startQuest(){
 		return startTask(getFirstKey(tasks.keySet()));
 	}
 
 	/**
 	 * Start a task of the quest.
 	 * 
 	 * @param taskid
 	 *            task to start
 	 * @return true if task was started successfully
 	 */
 	public boolean startTask(int taskid) {
 		if (taskid == -1) {
 			finishQuest(CompleteStatus.SUCCESS);
 			return true;
 		}
 		if (!tasks.containsKey(taskid))
 			return false;
		if (!activeTask.isComplete())
 			activeTask.cancelTask();
 		currenttask = taskid;
 		String[] eventnums = tasks.get(taskid);
 		List<Integer> eventnum = new ArrayList<Integer>();
 		for (String e : eventnums) {
 			eventnum.add(Integer.parseInt(e));
 		}
 		activeTask = new Task(questid, taskid, eventnum);
 		activeTask.start();
 		return true;
 	}
 	
 	public boolean isInstanced(){
 		return loadworld;
 	}
 
 	public Task getActiveTask() {
 		return activeTask;
 	}
 
 	// passed in from QuestManager
 	public void onTaskCompletion(TaskCompleteEvent e) {
 		if (e.getQuestID() != questid)
 			return;
 		if (e.getResult()==CompleteStatus.CANCELED)
 			return;
 		else if (e.getResult()==CompleteStatus.FAILURE){
 			finishQuest(CompleteStatus.FAILURE);
 			return;
 		}
 		// TODO this is lovely and all, but tasks should trigger other tasks...
 		// I'll just call the next task, and if the next task isn't available,
 		// finish the quest
 
 		List<Integer> sortedkeys = getSortedKeys(tasks.keySet());
 		int loc = sortedkeys.indexOf(e.getID());
 		if (loc == sortedkeys.size() - 1) {
 			finishQuest(CompleteStatus.SUCCESS);
 			return;
 		}
 		loc++;
 		startTask(loc);
 	}
 
 	public void finishQuest(CompleteStatus c) {
 		finished = c;
 		TimeUtils.unlock(Bukkit.getWorld(world));
 		Group g = MineQuest.groupManager.getGroup(MineQuest.groupManager
 				.indexOfQuest(this));
 		QuestCompleteEvent event = new QuestCompleteEvent(questid, c, g);
 		Bukkit.getPluginManager().callEvent(event);
 	}
 
 	public void unloadQuest() throws IOException {
 		if (loadworld)
 			QuestWorldManip.removeWorld(Bukkit.getWorld(world));
 	}
 
 	public CompleteStatus isFinished() {
 		return finished;
 	}
 
 	public String getEvent(Integer id) {
 		if (!events.containsKey(id))
 			throw new IllegalArgumentException("No such event ID!");
 		return events.get(id);
 	}
 
 	/**
 	 * Get the "YOU CAN'T EDIT THIS PLACE" message...
 	 * 
 	 * @return cannot edit message
 	 */
 	public String getEditMessage() {
 		return editMessage;
 	}
 
 	/**
 	 * Retrieve the current task ID.
 	 * 
 	 * @return Current Task ID.
 	 */
 	public int getCurrentTaskID() {
 		return currenttask;
 	}
 
 	public String[] getTaskDetails(int id) {
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
 
 	public Location getSpawnLocation() {
 		return new Location(Bukkit.getWorld(world), spawnPoint[0],
 				spawnPoint[1], spawnPoint[2]);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object arg0) {
 		if (!(arg0 instanceof Quest))
 			return false;
 		Quest q = (Quest) arg0;
 		return (q.questid == this.questid);
 	}
 
 	public String getName() {
 		return displayname;
 	}
 
 	public String getDescription() {
 		return displaydesc;
 	}
 
 }

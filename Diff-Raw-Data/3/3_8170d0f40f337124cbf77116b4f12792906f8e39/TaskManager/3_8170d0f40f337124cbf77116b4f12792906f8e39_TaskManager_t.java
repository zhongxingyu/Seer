 /**
  * This file, TaskManager.java, is part of MineQuest:
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
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.BukkitEvents.EventCompleteEvent;
 import com.theminequest.MineQuest.API.Quest.Quest;
 import com.theminequest.MineQuest.API.Task.QuestTask;
 
 public class TaskManager implements Listener, com.theminequest.MineQuest.API.Task.TaskManager {
 	
 	public TaskManager(){
 		Managers.log("[Task] Starting Manager...");
 	}
 	
 	@EventHandler
 	public void onEventComplete(EventCompleteEvent e){
 		if (e.getEvent().getQuest()==null)
 			return;
 		Quest q = e.getEvent().getQuest();
 		QuestTask t = q.getActiveTask();
		if (t != null)
			t.finishEvent(e.getEvent(),e.getCompleteStatus());
 	}
 
 }

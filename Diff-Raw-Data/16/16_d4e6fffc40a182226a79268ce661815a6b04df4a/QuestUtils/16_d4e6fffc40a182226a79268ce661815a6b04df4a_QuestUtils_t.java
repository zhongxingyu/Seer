 /*
  * This file is part of MineQuest-API, version 2, Specifications for the MineQuest system.
  * MineQuest-API, version 2 is licensed under GNU Lesser General Public License v3.
  * Copyright (C) 2012 The MineQuest Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.theminequest.MineQuest.API.Quest;
 
 import static com.theminequest.MineQuest.API.Quest.QuestDetails.*;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 
 import com.theminequest.MineQuest.API.Events.QuestEvent;
 import com.theminequest.MineQuest.API.Events.UserQuestEvent;
 import com.theminequest.MineQuest.API.Target.TargetDetails;
 import com.theminequest.MineQuest.API.Utils.ChatUtils;
import com.theminequest.MineQuest.API.Utils.SetUtils;
 
 public class QuestUtils {
 	
 	public static String getStatusString(Quest q){
 		String tr = QuestDetailsUtils.getOverviewString(q.getDetails()) + QuestDetailsUtils.CODE_NEWLINE_SEQ;
 		if (q.getActiveTask()!=null){
 			tr += ChatUtils.formatHeader("Current Tasks") + QuestDetailsUtils.CODE_NEWLINE_SEQ;
 			for (QuestEvent e : q.getActiveTask().getEvents()){
 				if (e instanceof UserQuestEvent){
 					String description = ((UserQuestEvent)e).getDescription();
 					if (e.isComplete()==null)
 						tr += ChatColor.GREEN + "- " + description + QuestDetailsUtils.CODE_NEWLINE_SEQ;
 					else
 						tr += ChatColor.GRAY + "- " + description + QuestDetailsUtils.CODE_NEWLINE_SEQ;
 				}
 			}
 		}
 		return tr;
 	}
 	
 	/**
 	 * Get all possible events
 	 * 
 	 * @return all possible events (# association)
 	 */
 	public static Set<Integer> getEventNums(Quest q) {
 		Map<Integer,String> events = q.getDetails().getProperty(QUEST_EVENTS);
 		return events.keySet();
 	}
 	
 	public static String getEvent(Quest q, int eventid){
 		Map<Integer,String> events = q.getDetails().getProperty(QUEST_EVENTS);
 		return events.get(eventid);
 	}
 	
 	public static Location getSpawnLocation(Quest q){
 		double[] xyzcoords = q.getDetails().getProperty(QUEST_SPAWNPOINT);
 		return new Location(Bukkit.getWorld((String) q.getDetails().getProperty(QUEST_WORLD)),
 				xyzcoords[0], xyzcoords[1], xyzcoords[2]);
 	}
 	
 	public static TargetDetails getTargetDetails(Quest q, int targetID){
 		Map<Integer,TargetDetails> targets = q.getDetails().getProperty(QUEST_TARGETS);
 		return targets.get(targetID);
 	}
 
 	public static int getNextTask(Quest quest) {
 		int currentid = quest.getActiveTask().getTaskID();
 		LinkedHashMap<Integer,String[]> tasks = quest.getDetails().getProperty(QUEST_TASKS);
		for (Integer i : SetUtils.getSortedKeys(tasks.keySet())) {
			if (i > currentid)
				return i;
 		}
		return -1;
 	}
 
 }

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
 package com.theminequest.MineQuest.Quest.Parser;
 
 import java.util.List;
 import java.util.Map;
 
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.Quest.QuestDetails;
 import com.theminequest.MineQuest.API.Quest.QuestParser.QHandler;
 import com.theminequest.MineQuest.API.Requirements.QuestRequirement;
 
 import static com.theminequest.MineQuest.API.Quest.QuestDetails.*;
 
 public class RequirementHandler implements QHandler {
 
 	/*
 	 * Requirement:ID:Name:Details
 	 */
 	@Override
 	public void parseDetails(QuestDetails q, List<String> line) {
 		int number = Integer.parseInt(line.remove(0));
 		String name = line.remove(1);
 		String details = "";
 		for (String s : line)
 			details+=s + ":";
 		if (details.length() != 0)
			details = details.substring(0,details.length()-2);
 		Map<Integer,QuestRequirement> reqs = q.getProperty(QUEST_REQUIREMENTDETAILS);
 		QuestRequirement req = Managers.getRequirementManager().construct(name, number, q, details);
 		reqs.put(number, req);
 	}
 
 }

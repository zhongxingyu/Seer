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
 
 import com.theminequest.MineQuest.API.Quest.QuestDetails;
 import com.theminequest.MineQuest.API.Quest.QuestParser.QHandler;
 import com.theminequest.MineQuest.API.Quest.QuestRequirement;
 import com.theminequest.MineQuest.Quest.RequirementFactory;
 
 import static com.theminequest.MineQuest.API.Quest.QuestDetails.*;
 
 public class RequirementHandler implements QHandler {
 
 	/*
 	 * Requirement:TYPE:details
 	 */
 	@Override
 	public void parseDetails(QuestDetails q, List<String> line) {
 		List<QuestRequirement> r = q.getProperty(QUEST_REQUIREMENTS);
 		String details = "";
		for (int i=1; i<line.size(); i++){
 			details+=line.get(i) + ":";
 		}
 		if (details.length()!=0)
 			details = details.substring(0, details.length()-1);
 		QuestRequirement qr = RequirementFactory.constructRequirement(line.get(0), q, details);
 		if (qr!=null)
 			r.add(qr);
 	}
 
 }

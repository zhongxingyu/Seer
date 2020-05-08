 package com.theminequest.MineQuest.Quest.Parser;
 
import java.util.List;

import com.theminequest.MineQuest.Quest.QuestDescription;
import com.theminequest.MineQuest.Quest.QuestParser.QHandler;

 @Deprecated
public class InstanceHandler implements QHandler {

	@Override
	public void parseDetails(QuestDescription q, List<String> line) {
			// I say YES to instances.
			q.loadworld = true;
			q.world = line.get(2);
			if (line.size()>3)
				q.nether = true;
	}
 
 }

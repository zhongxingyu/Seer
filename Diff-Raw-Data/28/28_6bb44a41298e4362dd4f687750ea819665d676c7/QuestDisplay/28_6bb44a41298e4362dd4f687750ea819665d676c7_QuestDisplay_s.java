 package com.adamki11s.display;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import com.adamki11s.quests.QuestLoader;
 import com.adamki11s.quests.QuestManager;
 import com.adamki11s.quests.QuestTask;
 import com.adamki11s.questx.QuestX;
 
 public class QuestDisplay {
 
 	public static void displayCurrentQuestInfo(Player p) {
 		String pName = p.getName();
 		if (!QuestManager.doesPlayerHaveQuest(pName)) {
 			QuestX.logChat(p, ChatColor.RED + "You do not currently have a quest.");
 		} else {
 			String qName = QuestManager.getCurrentQuestName(pName);
 			QuestLoader ql = QuestManager.getQuestLoader(qName);
 			QuestTask qt = ql.getPlayerQuestTask(pName);
			QuestX.logChat(p, "[Quest Info] Name = " + ql.getName());
 			QuestX.logChat(p, ChatColor.ITALIC + qName + ChatColor.RESET + ChatColor.RED + ", progress " + ql.getProgress(pName));
 			qt.sendWhatIsLeftToDo(p);
 			QuestX.logChat(p, "----------");
 		}
 	}
 	
 	
 		
 
 }

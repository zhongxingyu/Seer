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
			QuestX.logChat(p, "Loading quest = " + qName);
 			QuestLoader ql = QuestManager.getQuestLoader(qName);
			if(ql == null){
				QuestX.logChat(p, "QuestLoader WAS NULL!");
			} else {
				QuestX.logChat(p, "QuestLoader was NOT NULL!");
			}
 			QuestTask qt = ql.getPlayerQuestTask(pName);
			if(qt == null){
				QuestX.logChat(p, "QuestTask WAS NULL!");
			} else {
				QuestX.logChat(p, "QuestLoader was NOT NULL!");
			}
			QuestX.logChat(p, "[Quest Info] Name = " + ql.getName() + ", Return to NPC " + qt.getNPCToCompleteName());
 			QuestX.logChat(p, ChatColor.ITALIC + qName + ChatColor.RESET + ChatColor.RED + ", progress " + ql.getProgress(pName));
 			qt.sendWhatIsLeftToDo(p);
 			QuestX.logChat(p, "----------");
 		}
 	}
 	
 	
 		
 
 }

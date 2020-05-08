 package com.adamki11s.quests;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import com.adamki11s.io.FileLocator;
 import com.adamki11s.questx.QuestX;
 import com.adamki11s.sync.io.configuration.SyncConfiguration;
 
 public class QuestManager {
 
 	public static volatile HashSet<QuestLoader> quests = new HashSet<QuestLoader>();
 	public static volatile HashMap<String, String> currentQuest = new HashMap<String, String>();
 
 	public static void loadQuest(String name) {
 		QuestX.logMSG("Loading quest '" + name + "'");
 		quests.add(new QuestLoader(FileLocator.getQuestFile(name)));
 	}
 
 	public static boolean isQuestLoaded(String name) {
 		for (QuestLoader q : quests) {
 			if (q.getName().equalsIgnoreCase(name)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static boolean hasPlayerCompletedQuest(String qName, String player) {
 		QuestLoader ql = getQuestLoader(qName);
 		ql.loadAndCheckPlayerProgress(player);
 		return ql.isQuestComplete(player);
 	}
 
 	public static boolean doesPlayerHaveQuest(String pName) {
 		return currentQuest.containsKey(pName);
 	}
 
 	public static synchronized void loadCurrentPlayerQuest(String pName) {
 		File cur = FileLocator.getCurrentQuestFile();
 		SyncConfiguration cfg = new SyncConfiguration(cur);
 		cfg.read();
 		if (cfg.doesKeyExist(pName)) {
 			String q = cfg.getString(pName);
 			if (!isQuestLoaded(q)) {
 				loadQuest(q);
 			}
 			currentQuest.put(pName, q);
 		}
 	}
 
 	public static void unloadPlayerQuestData(String pName) {
 		if (currentQuest.containsKey(pName)) {
 			currentQuest.remove(pName);
 		}
 	}
 
 	public static void setCurrentPlayerQuest(String pName, String quest) {
 		currentQuest.put(pName, quest);
 		QuestLoader ql = getQuestLoader(quest);
 		ql.playerStartedQuest(pName);
 	}
 
 	public static void removeCurrentPlayerQuest(String quest, String pName) {
 		if (currentQuest.containsKey(pName)) {
 			currentQuest.remove(pName);
 		}
 	}
 
 	public static boolean hasQuestBeenSetup(String quest) {
 		File f = new File(FileLocator.quest_data_root + File.separator + quest + File.separator + "setup.qxs");
 		return (!f.exists());
 	}
 
 	public static boolean doesQuestExist(String quest) {
 		File f = new File(FileLocator.quest_data_root + File.separator + quest);
 		return (f.exists());
 	}
 
 	public static QuestLoader getQuestLoader(String quest) {
 		for (QuestLoader q : quests) {
 			if (q.getName().equalsIgnoreCase(quest)) {
 				return q;
 			}
 		}
 		return null;
 	}
 
 	public static String getCurrentQuestName(String player) {
 		return currentQuest.get(player);
 	}
 
 	public static QuestTask getCurrentQuestTask(String player) {
 		String qName = currentQuest.get(player);
 		for (QuestLoader ql : quests) {
 			if (ql.getName().equalsIgnoreCase(qName)) {
 				return ql.getPlayerQuestTask(player);
 			}
 		}
 		return null;
 	}
 
 }

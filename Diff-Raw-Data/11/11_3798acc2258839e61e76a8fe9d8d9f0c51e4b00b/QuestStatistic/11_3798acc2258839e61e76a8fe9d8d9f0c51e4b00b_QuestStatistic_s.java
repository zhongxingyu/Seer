 package com.theminequest.MineQuest.API.Tracker;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 
 import com.alta189.simplesave.Field;
 import com.alta189.simplesave.Id;
 import com.alta189.simplesave.Table;
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.BukkitEvents.QuestGivenEvent;
 import com.theminequest.MineQuest.API.Tracker.StatisticManager.Statistic;
 
 @Table("minequest_Quests")
 public class QuestStatistic extends Statistic implements Comparable<QuestStatistic> {
 	
 	@Id
 	private long uuid;
 		
 	@Field
 	private String questsGiven;
 	
 	@Field
 	private String questsCompleted;
 	
 	@Field
 	private String questsMWSaved;
 	
 	// NON-PERSISTENT DATA
 	private List<String> givenQuests;
 	private List<String> completedQuests;
 	private List<String> savedMWQuests;
 	
 	public String[] getGivenQuests(){
 		setup();
 		return givenQuests.toArray(new String[givenQuests.size()]);
 	}
 	
 	public String[] getCompletedQuests(){
 		setup();
 		return completedQuests.toArray(new String[completedQuests.size()]);
 	}
 	
 	public void addGivenQuest(String questName){
 		setup();
 		givenQuests.add(questName);
 		save();
 		QuestGivenEvent e = new QuestGivenEvent(questName,Bukkit.getPlayer(getPlayerName()));
 		Bukkit.getPluginManager().callEvent(e);
 	}
 	
 	public void removeGivenQuest(String questName){
 		setup();
 		givenQuests.remove(questName);
 		save();
 	}
 	
 	public void addCompletedQuest(String questName){
 		setup();
 		if (!completedQuests.contains(questName))
 			completedQuests.add(questName);
 		save();
 	}
 	
 	public void removeCompletedQuest(String questName){
 		setup();
 		completedQuests.remove(questName);
 		save();
 	}
 	
 	public void setMainWorldQuest(String quest_name, int currentTask){
 		setup();
 		for (int i=0; i<savedMWQuests.size(); i++){
 			if (savedMWQuests.get(i).split("/")[0].equals(quest_name)){
 				savedMWQuests.remove(i);
 				i = savedMWQuests.size();
 			}
 		}
 		savedMWQuests.add(quest_name + "/" + currentTask);
 		save();
 	}
 	
 	public void removeMainWorldQuest(String quest_name){
 		setup();
 		for (int i=0; i<savedMWQuests.size(); i++){
 			if (savedMWQuests.get(i).split("/")[0].equals(quest_name)){
 				savedMWQuests.remove(i);
 				i = savedMWQuests.size();
 			}
 		}
 		save();
 	}
 	
 	private void setup(){
 		if (questsGiven==null)
 			questsGiven = "";
 		if (questsCompleted==null)
 			questsCompleted = "";
 		if (questsMWSaved==null)
 			questsMWSaved = "";
 		if (givenQuests==null)
 			givenQuests = new ArrayList<String>(Arrays.asList(questsGiven.split("/")));
 		if (completedQuests==null)
 			completedQuests = new ArrayList<String>(Arrays.asList(questsCompleted.split("/")));
 		if (savedMWQuests==null){
 			String[] split = questsMWSaved.split("/");
 			savedMWQuests = new ArrayList<String>();
 			if (split.length%2==0){
 				for (int i=0; i<split.length; i+=2){
 					savedMWQuests.add(split[i] + "/" + split[i+1]);
 				}
 			}
 		}
 	}
 	
 	private void save(){		
 		questsGiven = "";
 		for (String s : givenQuests){
 			questsGiven += s + "/";
 		}
		questsGiven = questsGiven.substring(0,questsGiven.length()-1);
 
 		questsCompleted = "";
 		for (String s : completedQuests){
 			questsCompleted += s + "/";
 		}
		questsCompleted = questsCompleted.substring(0,questsCompleted.length()-1);
 		
 		questsMWSaved = "";
 		for (String s : savedMWQuests){
 			questsMWSaved += s + "/";
 		}
		questsMWSaved = questsMWSaved.substring(0,questsMWSaved.length()-1);
 
 		Managers.getStatisticManager().setStatistic(this, getClass());
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof QuestStatistic))
 			return false;
 		return getPlayerName().equals(((QuestStatistic)obj).getPlayerName());
 	}
 
 	@Override
 	public int compareTo(QuestStatistic other) {
 		return getPlayerName().compareTo(other.getPlayerName());
 	}
 
 }

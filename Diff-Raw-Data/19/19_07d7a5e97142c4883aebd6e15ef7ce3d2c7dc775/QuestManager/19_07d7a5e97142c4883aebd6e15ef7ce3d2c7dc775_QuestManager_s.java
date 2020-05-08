 package net.skycraftmc.SkyQuest.quest;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.entity.Player;
 
 import net.skycraftmc.SkyQuest.SkyQuestMain;
 import net.skycraftmc.SkyQuest.event.QuestCompleteEvent;
 
 public class QuestManager 
 {
 	private SkyQuestMain p;
 	public static HashMap<Player, List<Quest>> quests = new HashMap<Player, List<Quest>>();
 	public static HashMap<Player, List<Quest>>completed = new HashMap<Player, List<Quest>>();
 	public static List<Quest> allquests = new ArrayList<Quest>();
 	public QuestManager(SkyQuestMain main)
 	{
 		p = main;
 	}
 	public void addQuest(Player player, Quest quest)
 	{
 		Quest q = Quest.clone(quest);
 		for(Objective o: q.getObjectives())
 		{
 			if(o instanceof MoveObjective)((MoveObjective)o).setPlayer(player);
 		}
 		quests.get(player).add(q);
 	}
 	public List<Quest> getQuests(Player player)
 	{
 		return quests.get(player);
 	}
 	public Quest getQuest(String quest)
 	{
 		for(Quest q:allquests)
 		{
 			if(q.getTitle().equals(quest))return q;
 		}
 		return null;
 	}
 	public Quest getQuest(Player player, String quest)
 	{
 		if(!hasQuest(player, quest))return null;
 		List<Quest> q = getQuests(player);
 		for(Quest qx:q)
 		{
 			if(qx.getName().equalsIgnoreCase(quest))return qx;
 		}
 		return null;
 	}
 	public void addQuest(Quest quest)
 	{
 		allquests.add(quest);
 	}
 	public void resetQuests(Player player)
 	{
 		quests.put(player, new ArrayList<Quest>());
 		completed.put(player, new ArrayList<Quest>());
 	}
 	public boolean hasQuest(Player player, String quest)
 	{
 		List<Quest> q = getQuests(player);
 		for(Quest qx:q)
 		{
 			if(qx.getName().equalsIgnoreCase(quest))return true;
 		}
 		return false;
 	}
 	public void completeQuest(Player player, Quest quest)
 	{
 		QuestCompleteEvent event = new QuestCompleteEvent(player, quest);
 		p.getServer().getPluginManager().callEvent(event);
 	}
 }

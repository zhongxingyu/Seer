 package net.skycraftmc.SkyQuest;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 public class PlayerQuestLog 
 {
 	private String player;
 	private ArrayList<QuestData>assigned = new ArrayList<QuestData>();
 	private HashMap<String, Integer>completed = new HashMap<String, Integer>();
 	public PlayerQuestLog(String player)
 	{
 		this.player = player;
 	}
 	public String getPlayer()
 	{
 		return player;
 	}
 	public void assign(Quest quest)
 	{
 		if(isAssigned(quest))return;
 		QuestData qd = new QuestData(this, quest);
 		assigned.add(qd);
 		if(SkyQuest.isOnServer())
 		{
 			Player p = Bukkit.getServer().getPlayerExact(player);
 			if(p != null)p.sendMessage("Started: "  + quest.getName());
 		}
 		qd.setStage(quest.getFirstStage().getID());
 	}
 	public void complete(Quest quest)
 	{
		if(!isAssigned(quest))return;
 		assigned.remove(getProgress(quest));
 		if(!completed.containsKey(quest.getID()))completed.put(quest.getID(), 1);
 		else completed.put(quest.getID(), completed.get(quest.getID()) + 1);
 	}
 	public boolean isAssigned(Quest quest)
 	{
 		for(QuestData qd:assigned)
 		{
 			if(qd.getQuest().getID().equals(quest.getID()))return true;
 		}
 		return false;
 	}
 	public Quest[] getAssigned()
 	{
 		Quest[] quests = new Quest[assigned.size()];
 		int i = 0;
 		for(QuestData q:assigned)
 		{
 			quests[i] = q.getQuest();
 			i++;
 		}
 		return quests;
 	}
 	public QuestData getProgress(Quest quest)
 	{
 		for(QuestData qd:assigned)
 		{
 			if(qd.getQuest().getID().equals(quest.getID()))return qd;
 		}
 		return null;
 	}
 	public boolean hasCompleted(Quest quest)
 	{
 		return completed.containsKey(quest.getID());
 	}
 	public int getTimesCompleted(Quest quest)
 	{
 		if(!completed.containsKey(quest.getID()))return 0;
 		else return completed.get(quest.getID());
 	}
 }

 package net.skycraftmc.SkyQuest.quest;
 
 import java.util.ArrayList;
 
 import org.bukkit.entity.Player;
 
 public class Quest 
 {
 	String title;
 	ArrayList<Objective>objectives;
 	ArrayList<Objective>completed = new ArrayList<Objective>();
 	public Quest(Player player, ArrayList<Objective> objectives, String title)
 	{
 		this.player = player;
 		this.objectives = objectives;
 		this.title = title;
 	}
 	private Player player;
 	public String getName()
 	{
 		return title;
 	}
 	public static Quest clone(Quest quest)
 	{
 		ArrayList<Objective> obj = quest.getObjectives();
 		ArrayList<Objective> r = new ArrayList<Objective>();
 		for(Objective o:obj)
 		{
 			if(o instanceof KillObjective)r.add(KillObjective.clone((KillObjective)o));
 		}
 		return new Quest(quest.getPlayer(), quest.getObjectives(), quest.getTitle());
 	}
 	public boolean isComplete(int objective)
 	{
 		if(completed.contains(objectives.get(objective)))return true;
 		return false;
 	}
 	public String getTitle()
 	{
 		return title;
 	}
 	public Player getPlayer()
 	{
 		return player;
 	}
 	public void addObjective(Objective o)
 	{
 		objectives.add(o);
 	}
 	public java.util.ArrayList<Objective> getObjectives()
 	{
 		return objectives;
 	}
 	public void completeObjective(int index)
 	{
 		completed.add(objectives.get(index - 1));
 	}
 	public Objective getCurrentObjective()
 	{
		if(completed.size() == 0)return objectives.get(0);
 		return objectives.get(completed.size() - 1);
 	}
 }

 package net.skycraftmc.SkyQuest.quest;
 
 import java.util.List;
 
 import org.bukkit.entity.CreatureType;
 
 import net.skycraftmc.SkyQuest.util.SkyQuestUtil;
 
 public class KillObjective extends Objective
 {
 	private int progress = 0;
 	public KillObjective(String objective, String label, List<String> rewards, String text) 
 	{
 		super(ObjectiveType.KILL, objective, label, rewards, text);
 	}
 	public boolean isComplete()
 	{
 		String[] tokens = objective.split("[:]",2);
 		if(tokens.length != 2)return false;
 		CreatureType ct = SkyQuestUtil.getType(tokens[0]);
 		if(ct == null)return false;
 		int amount;
 		try{amount = Integer.parseInt(tokens[1].replaceAll(" ", ""));}catch(NumberFormatException nfe){return false;}
 		if(progress >= amount)return true;
 		return false;
 	}
 	public String getParsedObjective()
 	{
 		String[] tokens = getObjective().split("[:]", 2);
 		if(tokens.length != 2)return getObjective();
 		String[] st = tokens[0].split("[ ]+");
		String m = Character.toUpperCase(st[0].charAt(0)) + st[0].substring(1);
 		for(String x: st)
 		{
 			if(x.equalsIgnoreCase(st[0]))continue;
			m = m + " " + Character.toUpperCase(x.charAt(0)) + x.substring(1);
 		}
 		return tokens[1].trim() + " " + m;
 		
 	}
 	public void setProgress(int i)
 	{
 		progress = i;
 	}
 	public int getProgress()
 	{
 		return progress;
 	}
 	public static KillObjective clone(KillObjective obj)
 	{
 		return new KillObjective(obj.getObjective(), obj.getLabel(), obj.getRewards(), obj.getText());
 	}
 	public String getTarget() 
 	{
		return "Kill " + getParsedObjective() + "s";
 	}
 	public String getProgressAsString()
 	{
 		String[] tokens = getObjective().split("[:]", 2);
 		if(tokens.length != 2)return getProgress() + " creatures killed";
 		String[] st = tokens[0].split("[ ]+");
		String m = Character.toUpperCase(st[0].charAt(0)) + st[0].substring(1);
 		for(String x: st)
 		{
 			if(x.equalsIgnoreCase(st[0]))continue;
			m = m + " " + Character.toUpperCase(x.charAt(0)) + x.substring(1);
 		}
		return getProgress() + m + " killed";
 	}
 }

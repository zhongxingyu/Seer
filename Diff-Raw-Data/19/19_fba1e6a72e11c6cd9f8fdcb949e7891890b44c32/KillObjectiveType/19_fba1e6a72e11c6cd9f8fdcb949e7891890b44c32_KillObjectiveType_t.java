 package net.skycraftmc.SkyQuest;
 
 import org.bukkit.entity.EntityType;
 
 public class KillObjectiveType extends ObjectiveType
 {
 
 	public boolean isComplete(String target, String progress) 
 	{
 		if(!isValid(target))
 			throw new IllegalArgumentException("target is not valid");
 		if(!isValid(progress))
 			throw new IllegalArgumentException("progress is not valid");
 		String[] tokensp = progress.split(" ", 2);
 		String[] tokenst = target.split(" ", 2);
 		if(!tokenst[1].equalsIgnoreCase(tokensp[1]))return false;
 		return tokenst[0].equals(tokensp[0]);
 	}
 	
 	public int getKills(String progress)
 	{
 		if(!isValid(progress))
 			throw new IllegalArgumentException("progress is not valid");
 		return Integer.parseInt(progress.split(" ")[0]);
 	}
 	public boolean isSimilarType(String target, String progress)
 	{
 		if(!isValid(target))
 			throw new IllegalArgumentException("target is not valid");
 		if(!isValid(progress))
 			throw new IllegalArgumentException("progress is not valid");
 		String[] tokensp = progress.split(" ", 2);
 		String[] tokenst = target.split(" ", 2);
 		return tokenst[1].equals(tokensp[1]);
 	}
	public String getEntityType(String data)
	{
		if(!isValid(data))
			throw new IllegalArgumentException("data is not valid");
		return data.split(" ", 2)[1];
	}
 	public boolean isValid(String progress)
 	{
 		String[] tokens = progress.split(" ", 2);
 		if(tokens.length != 2)return false;
 		try
 		{
 			if(Integer.parseInt(tokens[0]) < 0)return false;
 		}catch(NumberFormatException nfe)
 		{
 			return false;
 		}
 		if(SkyQuest.isOnServer())
 		{
 			EntityType et = EntityType.fromName(tokens[1].toUpperCase());
 			if(et == null)return false;
 			return et.isAlive();
 		}
 		return true;
 	}
 
 	public String getName() 
 	{
 		return "Kill";
 	}
 	
 	public String createProgress(String data)
 	{
 		return "0 " + data;
 	}
 	
 	public String getData(String progress)
 	{
 		if(!isValid(progress))
 			throw new IllegalArgumentException("progress is not valid");
 		return progress.split(" ", 2)[1];
 	}
 	
 }

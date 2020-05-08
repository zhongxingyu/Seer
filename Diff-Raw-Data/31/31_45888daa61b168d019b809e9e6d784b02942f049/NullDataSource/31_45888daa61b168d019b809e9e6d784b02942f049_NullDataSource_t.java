 package me.tehbeard.BeardAch.dataSource;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.bukkit.util.config.ConfigurationNode;
 import org.bukkit.configuration.ConfigurationSection;
 import me.tehbeard.BeardAch.BeardAch;
 import me.tehbeard.BeardAch.achievement.Achievement;
 import me.tehbeard.BeardAch.achievement.AchievementManager;
 import me.tehbeard.BeardAch.achievement.rewards.CommandReward;
 import me.tehbeard.BeardAch.achievement.rewards.DroxSubGroupReward;
 import me.tehbeard.BeardAch.achievement.rewards.DroxTrackReward;
 import me.tehbeard.BeardAch.achievement.triggers.*;
 
 public class NullDataSource implements IDataSource{
 
 
 
	public NullDataSource(){
		BeardAch.printCon("Loading Null DataSource");
	}
 	
 	public void loadAchievements() {
 		// TODO Auto-generated method stub
 		//BeardAch.config.getList("achievements");
 		
 		for(String s : BeardAch.config.getConfigurationSection("achievements").getKeys(false)){
 			ConfigurationSection e = BeardAch.config.getConfigurationSection("achievements").getConfigurationSection(s);
 			if(e==null){
 				continue;
 			}
 
 			String name = e.getString("name");
 			String descrip = e.getString("descrip");
 			BeardAch.printDebugCon("Loading achievement " + name);
 
 			Achievement ach = new Achievement(name, descrip);
 			
 			for(String trig: ((List<String>)e.getList("triggers", new LinkedList<String>()))){
 				String[] part = trig.split("\\|");
 				if(part.length==2){
 					BeardAch.printDebugCon("Trigger => " + trig); 
 					if(part[0].equals("stat")){
 						ach.addTrigger(StatCheckTrigger.getInstance(part[1]));
 					}else if(part[0].equals("perm")){
 						ach.addTrigger(PermCheckTrigger.getInstance(part[1]));
 					}else if(part[0].equals("cuboid")){
 						ach.addTrigger(CuboidCheckTrigger.getInstance(part[1]));
 					}else if(part[0].equals("ach")){
 						ach.addTrigger(AchCheckTrigger.getInstance(part[1]));
 					}
 				}
 				else
 				{
 					BeardAch.printCon("ERROR! MALFORMED TRIGGER FOR ACHIEVEMENT " + name);
 				}
 			}
 
 			for(String reward: ((List<String>)e.getList("rewards", new LinkedList<String>()))){
 				String[] part = reward.split("\\|");
 				if(part.length==2){
 					BeardAch.printDebugCon("Reward => " + reward); 
 					
 					if(part[0].equals("comm")){
 					ach.addReward(CommandReward.getInstance(part[1]));
 					}else if(part[0].equals("promote")){
 						ach.addReward(DroxTrackReward.getInstance(part[1]));
 					}else if(part[0].equals("subgroup")){
 						ach.addReward(DroxSubGroupReward.getInstance(part[1]));
 					}
 				}
 				else
 				{
 					BeardAch.printCon("ERROR! MALFORMED REWARD FOR ACHIEVEMENT " + name);
 				}
 			}
 			
 			AchievementManager.addAchievement(ach);
 			BeardAch.printDebugCon("Loaded achievement " + name);
 		}
 	}
 
 	public HashSet<String> getPlayersAchievements(String Player) {
 		// TODO Auto-generated method stub
 		HashSet<String> d = new HashSet<String>();
 		//d.add("test");
 		return d;
 	}
 
 	public void setPlayersAchievements(String player,
 			String achievement) {
 		// TODO Auto-generated method stub
 		BeardAch.printCon("[" + player + "] stored "+ achievement);
 		
 	}
 
 	public void flush() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void clearAchievements(String player) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }

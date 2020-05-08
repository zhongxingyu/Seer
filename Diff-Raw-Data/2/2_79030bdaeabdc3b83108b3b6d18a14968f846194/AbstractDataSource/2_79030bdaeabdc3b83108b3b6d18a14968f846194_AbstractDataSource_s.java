 package me.tehbeard.BeardAch.dataSource;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.InvalidConfigurationException;
 
 import me.tehbeard.BeardAch.BeardAch;
 import me.tehbeard.BeardAch.achievement.Achievement;
 import me.tehbeard.BeardAch.achievement.AchievementManager;
 import me.tehbeard.BeardAch.achievement.AchievementPlayerLink;
 import me.tehbeard.BeardAch.achievement.rewards.CommandReward;
 import me.tehbeard.BeardAch.achievement.rewards.CounterReward;
 import me.tehbeard.BeardAch.achievement.rewards.DroxSubGroupReward;
 import me.tehbeard.BeardAch.achievement.rewards.DroxTrackReward;
 import me.tehbeard.BeardAch.achievement.triggers.*;
 
 public abstract class AbstractDataSource implements IDataSource{
 
 
 
 	public void loadAchievements() {
 		// TODO Auto-generated method stub
 		//BeardAch.config.getList("achievements");
 		try {
 			BeardAch.config.load(new File(BeardAch.self.getDataFolder(),"BeardAch.yml"));
 
 			Set<String> achs = BeardAch.config.getConfigurationSection("achievements").getKeys(false);
 			if(achs==null){
 				BeardAch.printCon("NO ACHIEVEMENTS FOUND");
 			}
 			for(String slug : achs){
 				ConfigurationSection e = BeardAch.config.getConfigurationSection("achievements").getConfigurationSection(slug);
 				if(e==null){
 					continue;
 				}
 
 				String name = e.getString("name");
 				String descrip = e.getString("descrip");
 				BeardAch.printDebugCon("Loading achievement " + name);
 
 				Achievement ach = new Achievement(slug,name, descrip);
 
 				for(String trig: ((List<String>)e.getList("triggers", new LinkedList<String>()))){
 					String[] part = trig.split("\\|");
 					if(part.length==2){
 						BeardAch.printDebugCon("Trigger => " + trig); 
 						if(part[0].equals("stat")){
 							ach.addTrigger(StatCheckTrigger.getInstance(part[1]));
 						}else if(part[0].equals("statwithin")){
 							ach.addTrigger(StatWithinTrigger.getInstance(part[1]));
 						}else if(part[0].equals("perm")){
 							ach.addTrigger(PermCheckTrigger.getInstance(part[1]));
 						}else if(part[0].equals("cuboid")){
 							ach.addTrigger(CuboidCheckTrigger.getInstance(part[1]));
 						}else if(part[0].equals("ach")){
 							ach.addTrigger(AchCheckTrigger.getInstance(part[1]));
 						}else if(part[0].equals("locked")){
 							ach.addTrigger(LockedTrigger.getInstance(part[1]));
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
 						}else if(part[0].equals("counter")){
 							ach.addReward(CounterReward.getInstance(part[1]));
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
 
 
 
 		} catch (FileNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (InvalidConfigurationException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	}
 
 	public abstract HashSet<AchievementPlayerLink> getPlayersAchievements(String Player);
 
 	public abstract void setPlayersAchievements(String player,
 			String achievement);
 	public abstract void flush();
 
 	public abstract void clearAchievements(String player) ;
 }

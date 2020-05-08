 package me.tehbeard.BeardAch.dataSource;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.configuration.ConfigurationSection;
 
 import me.tehbeard.BeardAch.BeardAch;
 import me.tehbeard.BeardAch.achievement.Achievement;
 import me.tehbeard.BeardAch.achievement.AchievementPlayerLink;
 import me.tehbeard.BeardAch.achievement.rewards.IReward;
 import me.tehbeard.BeardAch.achievement.triggers.ITrigger;
 import me.tehbeard.BeardAch.dataSource.configurable.Configurable;
 import me.tehbeard.utils.factory.ConfigurableFactory;
 
 
 public abstract class AbstractDataSource implements IDataSource{
 
 	public static final ConfigurableFactory<ITrigger,Configurable> triggerFactory = new ConfigurableFactory<ITrigger, Configurable>(Configurable.class) {
         
         @Override
         public String getTag(Configurable annotation) {
             return annotation.tag();
         }
     };
 	public static final ConfigurableFactory<IReward,Configurable> rewardFactory = new ConfigurableFactory<IReward, Configurable>(Configurable.class) {
         
         @Override
         public String getTag(Configurable annotation) {
             return annotation.tag();
         }
     };
 	
 	
 
 	public void loadAchievements() {
 
 
 		Set<String> achs = BeardAch.self.getConfig().getConfigurationSection("achievements").getKeys(false);
 		if(achs==null){
 			BeardAch.printCon("NO ACHIEVEMENTS FOUND");
 		}
 		for(String slug : achs){
 			ConfigurationSection e = BeardAch.self.getConfig().getConfigurationSection("achievements").getConfigurationSection(slug);
 			if(e==null){
 				continue;
 			}
 
 			String name = e.getString("name");
 			String descrip = e.getString("descrip");
 			boolean broadcast = e.getBoolean("broadcast",false);
 			BeardAch.printDebugCon("Loading achievement " + name);
 
 			Achievement ach = new Achievement(slug,name, descrip,broadcast);
 
 			List<String> triggers = e.getStringList("triggers");
 			for(String trig: triggers){
 				String[] part = trig.split("\\|");
 				if(part.length==2){
 					BeardAch.printDebugCon("Trigger => " + trig);
 					ITrigger trigger = triggerFactory.getProduct(part[0]);
 					trigger.configure(part[1]);
 					ach.addTrigger(trigger);
 					/*
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
 					}else if(part[0].equals("noach")){
 						ach.addTrigger(NoAchCheckTrigger.getInstance(part[1]));
 					}else if(part[0].equals("locked")){
 						ach.addTrigger(LockedTrigger.getInstance(part[1]));
 					}*/
 
 
 
 				}
 				else
 				{
 					BeardAch.printCon("ERROR! MALFORMED TRIGGER FOR ACHIEVEMENT " + name);
 				}
 			}
 			List<String> rewards = e.getStringList("rewards");
 			for(String reward: rewards){
 				String[] part = reward.split("\\|");
 				if(part.length==2){
 					BeardAch.printDebugCon("Reward => " + reward); 
 					IReward rewardInst = rewardFactory.getProduct(part[0]);
 					rewardInst.configure(part[1]);
 					ach.addReward(rewardInst);
					/*
					if(part[0].equals("comm")){
						ach.addReward(CommandReward.getInstance(part[1]));
					}else if(part[0].equals("promote")){
						ach.addReward(DroxTrackReward.getInstance(part[1]));
					}else if(part[0].equals("subgroup")){
						ach.addReward(DroxSubGroupReward.getInstance(part[1]));
					}else if(part[0].equals("counter")){
						ach.addReward(CounterReward.getInstance(part[1]));
					}*/
 				}
 				else
 				{
 					BeardAch.printCon("ERROR! MALFORMED REWARD FOR ACHIEVEMENT " + name);
 				}
 			}
 
 			BeardAch.self.getAchievementManager().addAchievement(ach);
 			BeardAch.printDebugCon("Loaded achievement " + name);
 		}
 
 
 	}
 
 	public abstract HashSet<AchievementPlayerLink> getPlayersAchievements(String Player);
 
 	public abstract void setPlayersAchievements(String player,
 			String achievement);
 	public abstract void flush();
 
 	public abstract void clearAchievements(String player) ;
 }

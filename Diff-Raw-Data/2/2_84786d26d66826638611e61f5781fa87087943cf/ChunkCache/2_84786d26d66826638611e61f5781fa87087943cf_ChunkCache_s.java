 package me.tehbeard.BeardAch;
 
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.bukkit.entity.Player;
 
 import me.tehbeard.BeardAch.achievement.Achievement;
 import me.tehbeard.BeardAch.achievement.AchievementManager;
 import me.tehbeard.BeardAch.achievement.AchievementPlayerLink;
 import me.tehbeard.BeardAch.achievement.triggers.CuboidCheckTrigger;
 import me.tehbeard.BeardAch.achievement.triggers.ITrigger;
 
 /**
  * handles cuboid checking
  * @author Tehbeard
  *
  */
 public class ChunkCache {
 
 	private static HashMap<String,HashSet<Achievement>> cache = new HashMap<String,HashSet<Achievement>>();
 
 	public static void clearCache(){
 		 cache = new HashMap<String,HashSet<Achievement>>();
 	}
 	public static void addAchievement(Achievement a){
 		for(ITrigger t :a.getTrigs()){
 			if(t instanceof CuboidCheckTrigger){
 				for(String cacheLocation :((CuboidCheckTrigger)t).getCache()){
 					if(!cache.containsKey(cacheLocation)){
 						cache.put(cacheLocation, new HashSet<Achievement>());
 					}
 					cache.get(cacheLocation).add(a);
 				}
 			}
 		}
 	}
 
 	public static void checkLocation(Player player){
 		String world = player.getLocation().getWorld().getName();
 		String cx = "" + player.getLocation().getBlockX() / 16;
 		String cz = "" + player.getLocation().getBlockZ() / 16;
 
 		
 		if(cache.containsKey(""+world+","+cx+","+cz)){
 			//BeardAch.printDebugCon("Chunk cache found records, checking....");
 			for(Achievement a : cache.get(""+world+","+cx+","+cz)){
 
 
 				//BeardAch.printDebugCon("Checking "+a.getName());
 				if(a.checkAchievement(player)){
 					AchievementManager.playerCheckCache.get(a).remove(player.getName());
 					AchievementManager.playerHasCache.get(player.getName()).add(new AchievementPlayerLink(a.getSlug()));
					AchievementManager.database.setPlayersAchievements(player.getName(),a.getName());
 				}
 
 			}
 		}
 	}
 }

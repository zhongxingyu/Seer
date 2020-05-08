 package me.supermaxman.xestats.utils;
 
 import me.supermaxman.xestats.XeStats;
 import me.supermaxman.xestats.utils.MySQL.StatsUser;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class MySQLUtils {
 	
 	public static void addKill(Player p) {
 		StatsUser s = getStatsUser(p);
 	    
 	    s.setKills(s.getKills()+1);
 	}
 	
 	public static void addDeath(Player p) {
 		StatsUser s = getStatsUser(p);
 	    
 	    s.setDeaths(s.getDeaths()+1);
 	}
 	
 	public static void addHit(Player p) {
 		StatsUser s = getStatsUser(p);
 	    
 	    s.setHits(s.getHits()+1);
 	}
 	
 	public static void addSwing(Player p) {
 		StatsUser s = getStatsUser(p);
 	    
 	    s.setSwings(s.getSwings()+1);
 	}
 	
 	public static void checkFarKill(Player p, double d) {
     	StatsUser s = getStatsUser(p);
 
         if (s.getLongestKillRange() < d) {
             s.setLongestKillRange(d);
         }
     }
 	
 	
     protected static String calculateHitMiss(Player p) {
     	StatsUser s = getStatsUser(p);
     	
         if (s.getSwings() != 0) {
             float hits = s.getHits();
             float swings = s.getSwings();
             float d = hits / swings;
             int percent = (int) (d * 100);
             
             return(percent + "%");
         } else {
             return("100%");
         }
         
     }
     protected static String calculateKDR(Player p) {
     	StatsUser s = getStatsUser(p);
     	
         if (s.getDeaths() != 0) {
            	return(Double.toString((s.getKills()) / (s.getDeaths())));
         } else {
            	return(Double.toString((s.getKills())));
         }
     }
     
     public static void sendPlayerInfo(Player p) {
     	StatsUser s = getStatsUser(p);
     	
         String kdr = calculateKDR(p);
         String accuracy = calculateHitMiss(p);
         
         p.sendMessage(ChatColor.AQUA + "[" + ChatColor.GOLD + "XeStats" + ChatColor.AQUA + "]" + ChatColor.AQUA + ": " + ColorUtils.getColoredName(p)+ ChatColor.AQUA + ":");
         
         p.sendMessage(ChatColor.AQUA + " - " + ChatColor.GOLD + "kills" + ChatColor.AQUA + ": " + ChatColor.GOLD + (s.getKills()));
         p.sendMessage(ChatColor.AQUA + " - " + ChatColor.GOLD + "deaths" + ChatColor.AQUA + ": " + ChatColor.GOLD + (s.getDeaths()));
         p.sendMessage(ChatColor.AQUA + " - " + ChatColor.GOLD + "kdr" + ChatColor.AQUA + ": " + ChatColor.GOLD + (kdr));
         p.sendMessage(ChatColor.AQUA + " - " + ChatColor.GOLD + "accuracy" + ChatColor.AQUA + ": " + ChatColor.GOLD + (accuracy));
         p.sendMessage(ChatColor.AQUA + " - " + ChatColor.GOLD + "farthestkill" + ChatColor.AQUA + ": " + ChatColor.GOLD + (s.getLongestKillRange()));
         
     }
     
     public static void sendPlayerInfo(Player p, Player l) {
     	StatsUser s = getStatsUser(l);
     	
         String kdr = calculateKDR(l);
         String accuracy = calculateHitMiss(l);
         
         p.sendMessage(ChatColor.AQUA + "[" + ChatColor.GOLD + "XeStats" + ChatColor.AQUA + "]" + ChatColor.AQUA + ": " + ColorUtils.getColoredName(l) + ChatColor.AQUA + ":");
         
         p.sendMessage(ChatColor.AQUA + " - " + ChatColor.GOLD + "kills" + ChatColor.AQUA + ": " + ChatColor.GOLD + (s.getKills()));
         p.sendMessage(ChatColor.AQUA + " - " + ChatColor.GOLD + "deaths" + ChatColor.AQUA + ": " + ChatColor.GOLD + (s.getDeaths()));
         p.sendMessage(ChatColor.AQUA + " - " + ChatColor.GOLD + "kdr" + ChatColor.AQUA + ": " + ChatColor.GOLD + (kdr));
         p.sendMessage(ChatColor.AQUA + " - " + ChatColor.GOLD + "accuracy" + ChatColor.AQUA + ": " + ChatColor.GOLD + (accuracy));
         p.sendMessage(ChatColor.AQUA + " - " + ChatColor.GOLD + "farthestkill" + ChatColor.AQUA + ": " + ChatColor.GOLD + (s.getLongestKillRange()));
         
     }
     
     
     
     
    protected static StatsUser getStatsUser(Player p){
 		return XeStats.statsUserMap.get(p.getName());
     }
     
 }

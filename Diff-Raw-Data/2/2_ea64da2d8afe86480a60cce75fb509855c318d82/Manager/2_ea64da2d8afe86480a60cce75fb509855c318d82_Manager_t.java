 package com.scizzr.bukkit.plugins.pksystem.managers;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.text.DecimalFormat;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import com.scizzr.bukkit.plugins.pksystem.Main;
 import com.scizzr.bukkit.plugins.pksystem.config.Config;
 import com.scizzr.bukkit.plugins.pksystem.util.MoreMath;
 
 @SuppressWarnings("unchecked")
 public class Manager {
     public static HashMap<String, Integer> points = new HashMap<String, Integer> ();
     public static HashMap<Integer, ChatColor> color = new HashMap<Integer, ChatColor> ();
     public static HashMap<Integer, String> name = new HashMap<Integer, String> ();
     private static HashMap<Player, Boolean> isPK = new HashMap<Player, Boolean> ();
     private static HashMap<Player, Boolean> isCrim = new HashMap<Player, Boolean> ();
     private static HashMap<Player, Integer> farmTimer = new HashMap<Player, Integer> ();
     private static HashMap<Player, Integer> pvpTimer = new HashMap<Player, Integer> ();
     private static ConcurrentHashMap<Player, Integer> repTimer = new ConcurrentHashMap<Player, Integer> ();
     private static HashMap<Player, Integer> spawnTimer = new HashMap<Player, Integer> ();
  // Removed for now; might use this at a later time.
     //private static HashMap<Player, Player> lastTarg = new HashMap<Player, Player> ();
     
     public static void main() {
         name.put(-4, "DEMON");      color.put(-4, ChatColor.DARK_RED);
         name.put(-3, "RED");        color.put(-3, ChatColor.RED);
         name.put(-2, "ORANGE");     color.put(-2, ChatColor.GOLD);
         name.put(-1, "YELLOW");     color.put(-1, ChatColor.YELLOW);
         name.put( 0, "WHITE");      color.put( 0, ChatColor.WHITE);
         name.put( 1, "LIGHT BLUE"); color.put( 1, ChatColor.AQUA);
         name.put( 2, "BLUE");       color.put( 2, ChatColor.BLUE);
         name.put( 3, "PURPLE");     color.put( 3, ChatColor.LIGHT_PURPLE);
         name.put( 4, "HERO");       color.put( 4, ChatColor.DARK_PURPLE);
     }
     
     public static boolean loadPoints() {
         if(!Main.fileRep.exists()) {
             try {
                 Main.fileRep.createNewFile();
                 Main.log.info(Main.prefixConsole + "Blank reputation.txt created");
             } catch (Exception ex) {
                 Main.log.info(Main.prefixConsole + "Failed to make reputation.txt");
                 Main.suicide(ex);
             }
         }
         
         HashMap<Player, Integer> tmp = (HashMap<Player, Integer>) points.clone();
         try {
             points.clear();
             BufferedReader reader = new BufferedReader(new FileReader((Main.fileRep.getAbsolutePath())));
             String line = reader.readLine();
             while (line != null) {
                 String[] values = line.split(":");
                 if (values.length == 2) {
                     String play = values[0];
                     Integer pts = Integer.valueOf(values[1]);
                     if (play != null) {
                         points.put(play, pts);
                     }
                 }
                 line = reader.readLine();
             }
             return true;
         } catch (Exception ex) {
             points = (HashMap<String, Integer>) tmp.clone();
             Main.suicide(ex);
             return false;
         }
     }
     
     public static boolean savePoints() {
         try {
             BufferedWriter writer = new BufferedWriter(new FileWriter((Main.fileRep.getAbsolutePath())));
             for (Entry<String, Integer> entry : points.entrySet()) {
                 writer.write(entry.getKey() + ":" + entry.getValue());
                 writer.newLine();
             }
             writer.close();
             return true;
         } catch (Exception ex) {
             Main.suicide(ex);
             return false;
         }
     }
     
     public static void doRepTick() {
         try {
             Set<Player> listP = pvpTimer.keySet();
             for (Player pp : listP) {
                 int get = getPvPTime(pp);
                 
                 setPvPTime(pp, get-1);
             }
         } catch (Exception ex) {
            /* No Spam */
         }
     }
     
     public static Integer getIndex(Integer pts) {
         if (pts == null) { return 0; }
         
         DecimalFormat twoDecForm = new DecimalFormat("#.##");
         Double rep = Double.valueOf(twoDecForm.format(pts/1000));
         
         if (Config.repSpecEnabled == true && Config.repSpecReach == true) {
             if (rep <= -10000) { return -4; } // Demon at 1000 evil kills
             if (rep >= 10000) { return 4; }   // Hero at 1000 good kills
         }
         
         if (rep <= -150) {
             return -3;      // red
         } else if (MoreMath.between(rep, -149.99, -90.0, true)) {
             return -2;      // orange
         } else if (MoreMath.between(rep, -89.99, -30, true)) {
             return -1;      // yellow
         } else if (MoreMath.between(rep, 30.0, 89.99, true)) {
             return 1;       // light blue
         } else if (MoreMath.between(rep, 90.0, 149.99, true)) {
             return 2;       // blue
         } else if (rep >= 150) {
             return 3;       // purple
         } else /* if (Math.between(r, -29.9, 29.9))*/ {
             return 0;       // white
         }
     }
 
     public static Integer getPoints(Player p) {
         if (!points.containsKey(p.getName())) { return 0; }
         
         return points.get(p.getName());
     }
     
     public static void setPoints(Player p, Integer pts) {
         Integer pPtsOld = getPoints(p);
         Integer pPtsNew = pts;
         
         points.put(p.getName(), pts); savePoints();
         
         if (!getIndex(pPtsOld).equals(getIndex(pPtsNew))) {
             p.sendMessage(Main.prefix + "Your reputation is now " + getFormattedReputation(p));
         }
     }
     
     public static String getDisplayName(Player p) {
         String dn = color.get(getIndex(getPoints(p))) + p.getName();
         if (dn.length() > 14) { dn = dn.substring(0, 14); }
         
         return dn;
     }
     
     public static Double getReputation(Player p) {
         if (!points.containsKey(p.getName())) { return 0.0; }
         
         Integer pts = points.get(p.getName());
         
         Double rep = MoreMath.pointsToRep(pts);
         
         return rep != null ? rep : 0;
     }
     
     public static String getFormattedReputation(Player p) {
         ChatColor col = color.get(getIndex(getPoints(p)));
         
         return col + name.get(getIndex(getPoints(p))) + ChatColor.WHITE + " (" + getReputation(p) + ")";
     }
     
     public static String getName(Player p) {
         ChatColor col = color.get(getIndex(getPoints(p)));
         
         return col + p.getDisplayName();
     }
     
     public static boolean isNeutral(Player p) {
         return getIndex(getPoints(p)) == 0;
     }
     
     public static boolean isDemon(Player p) {
         return getIndex(getPoints(p)) == -4;
     }
     
     public static boolean isHero(Player p) {
         return getIndex(getPoints(p)) == 4;
     }
     
     public static Integer getPvPTime(Player p) {
         Integer ispvp = pvpTimer.get(p);
         
         return ispvp != null ? ispvp : 0;
     }
     
     public static void setPvPTime(Player p, Integer i) {
         if (Config.fmtCombEnabled == true) {
             if (i == Config.combDuration && pvpTimer.get(p) == null) {
                 p.sendMessage(Main.prefix + Config.fmtCombEnter);
             } else if (i == 0) {
                 if (p != null) {
                     if (p.isOnline()) {
                         p.sendMessage(Main.prefix + Config.fmtCombExit);
                     }
                 }
             }
         }
         
         if (i > 0) {
             pvpTimer.put(p, i);
         } else {
             pvpTimer.remove(p);
         }
     }
 
     public static Integer getRepTime(Player p) {
         Integer isrep = repTimer.get(p);
         
         return isrep != null ? isrep : 0;
     }
     
     public static void setRepTime(Player p, Integer i) {
         if (i > 0) {
             repTimer.put(p, i);
         } else {
             repTimer.put(p, 0);
         }
     }
     
     public static Integer getFarmTime(Player p) {
         Integer isfarm = farmTimer.get(p);
         
         return isfarm != null ? isfarm : 0;
     }
     
     public static void setFarmTime(Player p, Integer i) {
         if (i > 0) {
             farmTimer.put(p, i);
         } else {
             farmTimer.put(p, 0);
         }
     }
     
     public static Integer getSpawnTime(Player p) {
         Integer isspawn = spawnTimer.get(p);
         
         return isspawn != null ? isspawn : 0;
     }
     
     public static void setSpawnTime(Player p, Integer i) {
         if (i > 0) {
             spawnTimer.put(p, i);
         } else {
             spawnTimer.put(p, 0);
         }
     }
     
 // Removed for now; might use this at a later time.
     //public static Player getLastTarget(Player p) {
     //    return lastTarg.get(p);
     //}
     
     //public static void setLastTarget(Player p, Player pp) {
     //    lastTarg.put(p, pp);
     //}
     
     public static boolean isPK(Player p) {
         if (isPK.containsKey(p)) {
             return isPK.get(p);
         }
         return false;
     }
     
     public static void setPK(Player p, boolean pk) {
         p.sendMessage(Main.prefix + (pk == true ? Config.fmtPKEnter : Config.fmtPKExit));
         isPK.put(p, pk);
     }
     
     public static boolean getCrim(Player p) {
         if (isCrim.containsKey(p)) {
             return isCrim.get(p);
         }
         return false;
     }
     
     public static void setCrim(Player p, boolean crim) {
         isCrim.put(p, crim);
     }
     
     public static boolean isCombat(Player p) {
         return getPvPTime(p) > 0;
     }
     
     public static boolean isFarm(Player p) {
         return getFarmTime(p) + Config.repLimitDuration >= Config.repLimitAmount * Config.repLimitDuration;
     }
     
     public static boolean isRespawn(Player p) {
         return spawnTimer.get(p) > 0;
     }
 }

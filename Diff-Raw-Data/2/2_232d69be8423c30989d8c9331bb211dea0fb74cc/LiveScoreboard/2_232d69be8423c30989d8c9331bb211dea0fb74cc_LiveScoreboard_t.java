 package dev.nationcraft.org.LiveScoreboard;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Score;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.ScoreboardManager;
 
 public class LiveScoreboard extends JavaPlugin {
 
     public final static Logger logger = Logger.getLogger("minecraft");
     public String prefix;
     HashMap<String, Integer> hmd = new HashMap<String, Integer>();
     HashMap<String, Integer> hmk = new HashMap<String, Integer>();
     SortedMap<String, Integer> kdr = new TreeMap<>();
     ArrayList<Double> nums = new ArrayList<>();
 
     @Override
     public void onDisable() {
         PluginDescriptionFile pdf = this.getDescription();
 
         logger.log(Level.INFO, "{0}, version {1} coded by {2} has been Disabled!", new Object[]{pdf.getName(), pdf.getVersion(), pdf.getAuthors()});
     }
 
     @Override
     public void onEnable() {
         PluginDescriptionFile pdf = this.getDescription();
         logger.log(Level.INFO, "{0}, version {1} coded by {2} has been Enabled!", new Object[]{pdf.getName(), pdf.getVersion(), pdf.getAuthors()});
         this.ScoreBoard();
     }
 
     public void ScoreBoard() {
         ScoreboardManager sm = Bukkit.getScoreboardManager();
         Scoreboard b = sm.getNewScoreboard();
         Objective obj = b.registerNewObjective("test", "dummy");
         obj.setDisplaySlot(DisplaySlot.SIDEBAR);
         obj.setDisplayName("Live KDR");
         Set<String> players = kdr.keySet();
         Iterator i = kdr.keySet().iterator();
         while (i.hasNext()) {
             String key = (String) i.next();
             double num = kdr.get(key);
             nums.add(num);
         }
         String[] fpl = (String[]) players.toArray();
         Integer[] fvl = (Integer[]) nums.toArray();
        Score s = obj.getScore(Bukkit.getServer().getOfflinePlayer(fpl[0]));
         s.setScore(fvl[0]);
     }
 
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent e) {
         int kills = 0;
         int deaths = 0;
         Player p = e.getPlayer();
         hmk.put(p.getName(), kills);
         hmd.put(p.getName(), deaths);
     }
 
     @EventHandler
     public void onPlayerKill(PlayerDeathEvent e) {
         Player killed = e.getEntity();
         Player killer = killed.getKiller();
         hmd.put(killed.getName(), (hmd.get(killed.getName()) + 1));
         hmk.put(killer.getName(), (hmk.get(killer.getName()) + 1));
         int death = hmd.get(killer.getName());
         int kill = hmk.get(killer.getName());
         int ratio = kill / death;
         kdr.put(killer.getName(), ratio);
         this.ScoreBoard();
 
     }
 
     public void onPlayerQuit(PlayerQuitEvent e) {
         Player p = e.getPlayer();
         hmd.remove(p.getName());
         hmk.remove(p.getName());
     }
 }

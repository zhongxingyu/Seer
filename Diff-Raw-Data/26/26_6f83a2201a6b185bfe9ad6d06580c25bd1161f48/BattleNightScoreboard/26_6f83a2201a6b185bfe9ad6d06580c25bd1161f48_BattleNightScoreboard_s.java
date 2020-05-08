 package me.limebyte.battlenight.core.util;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 
import me.limebyte.battlenight.core.BattleNight;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.ScoreboardManager;
 import org.bukkit.scoreboard.Team;
 
 public class BattleNightScoreboard {
 
     private static ScoreboardManager manager;
     private static Scoreboard scoreboard;
     private static Objective objective;
 
     private static Map<String, Scoreboard> scoreboards = new HashMap<String, Scoreboard>();
 
    public static void init() {
         manager = Bukkit.getScoreboardManager();
         scoreboard = manager.getNewScoreboard();
 
         objective = scoreboard.registerNewObjective("bn_scoreboard", "dummy");
         objective.setDisplaySlot(DisplaySlot.SIDEBAR);
         objective.setDisplayName("Score");
     }
 
     public static void addTeam(me.limebyte.battlenight.api.battle.Team team, boolean friendlyFire) {
         Team t = scoreboard.registerNewTeam("bn_team_" + team.getName());
         t.setDisplayName(team.getColour() + team.getDisplayName() + " Team");
         t.setPrefix(team.getDisplayName().toUpperCase());
 
         t.setAllowFriendlyFire(friendlyFire);
         t.setCanSeeFriendlyInvisibles(true);
 
        BattleNight.instance.getAPI().getMessenger().debug(Level.INFO, "Registered team " + t.getName());
     }
 
     public static void addPlayer(Player player, me.limebyte.battlenight.api.battle.Team team) {
         scoreboards.put(player.getName(), player.getScoreboard());
 
         for (Team t : scoreboard.getTeams()) {
            BattleNight.instance.getAPI().getMessenger().debug(Level.INFO, "Found team " + t.getName());
             if (t.getName().equals("bn_team_" + team.getName())) {
                 t.addPlayer(player);
                BattleNight.instance.getAPI().getMessenger().debug(Level.INFO, "Added " + player.getName() + " to " + t.getName());
                 break;
             }
 
         }
 
         player.setScoreboard(scoreboard);
         setScore(player, 0);
     }
 
     public static void removePlayer(Player player) {
         String name = player.getName();
         player.setScoreboard(scoreboards.get(name));
         scoreboards.remove(name);
 
         Team team = scoreboard.getPlayerTeam(player);
         if (team != null) team.removePlayer(player);
     }
 
     public static void setScore(Player player, int score) {
         objective.getScore(player).setScore(score);
     }
 
 }

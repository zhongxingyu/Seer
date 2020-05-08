 package me.limebyte.battlenight.core.util;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Score;
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
     }
 
    public static void addPlayer(Player player, me.limebyte.battlenight.api.battle.Team team) {
         scoreboards.put(player.getName(), player.getScoreboard());
 
         for (Team t : scoreboard.getTeams()) {
            if (t.getName().equals("bn_team_" + team.getName())) {
                 t.addPlayer(player);
                 break;
             }
         }
 
         player.setScoreboard(scoreboard);
     }
 
     public static void removePlayer(Player player) {
         String name = player.getName();
         player.setScoreboard(scoreboards.get(name));
         scoreboards.remove(name);
         scoreboard.getPlayerTeam(player).removePlayer(player);
     }
 
     public static void setKDR(Player player, int kdr) {
         Score score = objective.getScore(player);
         score.setScore(kdr);
     }
 
 }

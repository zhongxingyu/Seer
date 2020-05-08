 package me.limebyte.battlenight.core.util;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import me.limebyte.battlenight.api.battle.Battle;
 import me.limebyte.battlenight.api.battle.TeamedBattle;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.Team;
 
 public class BattleScoreboard {
 
     private Battle battle;
     private boolean teamed = false;
 
     private Scoreboard scoreboard;
     private Objective objective;
 
     private static int id = 0;
     private static Map<String, Scoreboard> scoreboards = new HashMap<String, Scoreboard>();
 
     public BattleScoreboard(Battle battle) {
         this.battle = battle;
         teamed = battle instanceof TeamedBattle;
 
         scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
         objective = scoreboard.registerNewObjective("bn_scoreboard" + id++, "dummy");
 
         objective.setDisplaySlot(DisplaySlot.SIDEBAR);
         objective.setDisplayName(ChatColor.GRAY + "Score");
 
         if (!teamed) {
             Team team = scoreboard.registerNewTeam("bn_allplayers");
             team.setDisplayName("BattleNight");
             team.setPrefix(ChatColor.RED.toString());
             team.setAllowFriendlyFire(true);
             team.setCanSeeFriendlyInvisibles(false);
         }
     }
 
     public void addTeam(me.limebyte.battlenight.api.battle.Team team, boolean friendlyFire) {
         if (!teamed) return;
 
         Team t = scoreboard.registerNewTeam("bn_team_" + team.getName());
         t.setDisplayName(team.getDisplayName() + " Team");
         t.setPrefix(team.getColour() + "[" + team.getDisplayName().toUpperCase() + "] ");
 
         t.setAllowFriendlyFire(friendlyFire);
         t.setCanSeeFriendlyInvisibles(true);
     }
 
     public void addPlayer(Player player) {
         scoreboards.put(player.getName(), player.getScoreboard());
 
         if (teamed) {
             String teamName = ((TeamedBattle) battle).getTeam(player).getName();
             for (Team team : scoreboard.getTeams()) {
                 if (team.getName().equals("bn_team_" + teamName)) {
                     team.addPlayer(player);
                     break;
                 }
 
             }
         } else {
             scoreboard.getTeam("bn_allplayers").addPlayer(player);
         }
 
         player.setScoreboard(scoreboard);
         objective.getScore(player).setScore(0);
     }
 
     public void removePlayer(Player player) {
         if (!scoreboard.getPlayers().contains(player)) return;
 
         String name = player.getName();
         player.setScoreboard(scoreboards.get(name));
         scoreboards.remove(name);
 
         Team team = scoreboard.getPlayerTeam(player);
         if (team != null) team.removePlayer(player);
     }
 
     public void updateScore(Player player) {
         int score = (int) Math.round(battle.getKDR(player) * 100);
         objective.getScore(player).setScore(score);
     }
 
 }

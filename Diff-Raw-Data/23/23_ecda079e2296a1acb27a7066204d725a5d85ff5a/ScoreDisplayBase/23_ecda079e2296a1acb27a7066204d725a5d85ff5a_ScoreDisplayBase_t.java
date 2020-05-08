 /*
  * @author     ucchy
  * @license    LGPLv3
  * @copyright  Copyright ucchy 2013
  */
 package com.github.ucchyocean.ct.scoreboard;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Scoreboard;
 
 import com.github.ucchyocean.ct.ColorTeaming;
 import com.github.ucchyocean.ct.config.ColorTeamingConfig;
 
 /**
  * @author ucchy
  */
 public abstract class ScoreDisplayBase {
 
     protected Objective objective;
     protected ColorTeaming plugin;
 
     /**
      * コンストラクタ。
      */
     public ScoreDisplayBase(ColorTeaming plugin) {
 
         this.plugin = plugin;
 
         Scoreboard scoreboard = plugin.getAPI().getScoreboard();
         String criteria = PlayerCriteria.convert(getConfigData());
 
         objective = scoreboard.getObjective(getObjectiveName());
         if ( objective == null ) {
             objective = scoreboard.registerNewObjective(getObjectiveName(), criteria);
         } else {
             // スコアを消去して使いまわす
             for ( Player player : plugin.getAPI().getAllPlayers() ) {
                 objective.getScore(player).setScore(0);
             }
         }
 
         refreshDisplayName();
 
         objective.setDisplaySlot(getDisplaySlot());
 
        if ( getConfigData() == PlayerCriteria.HEALTH ) {
            refreshScoreFromHealth();
        } else if ( getConfigData() == PlayerCriteria.CUSTOM ||
                getConfigData() == PlayerCriteria.DEATH_COUNT ||
                getConfigData() == PlayerCriteria.KILL_COUNT || 
                getConfigData() == PlayerCriteria.POINT ) {
            refreshScore();
        }
     }
 
     /**
      * スコアを更新する。
      */
     public void refreshScore() {
 
         // NOTE: Health、TotalKillは、自動更新させる。Noneは何もしない。
         if ( getConfigData() == PlayerCriteria.HEALTH ||
                 getConfigData() == PlayerCriteria.TOTAL_KILL_COUNT ||
                 getConfigData() == PlayerCriteria.NONE ) {
             return;
         }
 
         ArrayList<Player> players = plugin.getAPI().getAllPlayers();
         HashMap<String, int[]> killDeathUserCounts =
                 plugin.getAPI().getKillDeathUserCounts();
         ColorTeamingConfig config = plugin.getCTConfig();
         for ( Player player : players ) {
             int point = 0;
             if ( killDeathUserCounts.containsKey(player.getName()) ) {
                 int[] data = killDeathUserCounts.get(player.getName());
                 if ( getConfigData() == PlayerCriteria.KILL_COUNT ) {
                     point = data[0];
                 } else if ( getConfigData() == PlayerCriteria.DEATH_COUNT ) {
                     point = data[1];
                 } else if ( getConfigData() == PlayerCriteria.POINT ) {
                     point = data[0] * config.getCTKillPoint() +
                             data[1] * config.getCTDeathPoint() +
                             data[2] * config.getCTTKPoint();
                 }
             }
             if ( point == 0 ) {
                 // NOTE: 0 を設定すると項目が消えるので、まず1を設定する
                 objective.getScore(player).setScore(1);
             }
             objective.getScore(player).setScore(point);
         }
     }
    
    /**
     * スコアを、現在の体力値で更新する。（see issue #76）
     */
    public void refreshScoreFromHealth() {
        
        for ( Player player : Bukkit.getOnlinePlayers() ) {
            double health = player.getHealth();
            objective.getScore(player).setScore((int)health);
        }
    }
 
     /**
      * スコア表示を削除する。
      */
     public void remove() {
         if ( plugin.getAPI().getScoreboard().getObjective(getObjectiveName()) != null ) {
             objective.unregister();
         }
     }
 
     public abstract PlayerCriteria getConfigData();
 
     public abstract String getObjectiveName();
 
     public abstract DisplaySlot getDisplaySlot();
 
     public abstract void refreshDisplayName();
 }

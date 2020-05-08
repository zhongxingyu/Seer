 /*
  * @author     ucchy
  * @license    GPLv3
  * @copyright  Copyright ucchy 2013
  */
 package com.github.ucchyocean.ct.scoreboard;
 
 import java.util.ArrayList;
 
 import org.bukkit.entity.Player;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Scoreboard;
 
 import com.github.ucchyocean.ct.ColorTeaming;
 import com.github.ucchyocean.ct.ColorTeamingConfig;
 
 /**
  * @author ucchy
  * TABキーリストのスコア表示を管理するクラス
  */
 public class TabListScoreDisplay {
 
     private Objective objective;
 
     /**
      * コンストラクタ。コンストラクト時に、TABキーリストのスコアを初期化し表示する。
      */
     public TabListScoreDisplay() {
 
         Scoreboard scoreboard = ColorTeaming.getScoreboard();
         String criteria = TabListCriteria.convert(ColorTeamingConfig.listCriteria);
 
         objective = scoreboard.getObjective("listscore");
         if ( objective == null ) {
             objective = scoreboard.registerNewObjective("listscore", criteria);
         } else {
             // スコアを消去して使いまわす
             for ( Player player : ColorTeaming.getAllPlayers() ) {
                 objective.getScore(player).setScore(0);
             }
         }
 
         objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
 
         refreshScore();
     }
 
     /**
      * スコアを更新する。
      * ただし、POINTを指定しているとき以外は、サーバーが自動更新するので、何もしない。
      */
     public void refreshScore() {
 
         // NOTE: 独自仕様のPOINT以外は、全て自動更新されるため、
         //       POINT以外の場合は、何もしないで終了する。
         if ( ColorTeamingConfig.listCriteria != TabListCriteria.POINT ) {
             return;
         }
 
         ArrayList<Player> players = ColorTeaming.getAllPlayers();
         for ( Player player : players ) {
 
             if ( ColorTeaming.killDeathUserCounts.containsKey(player.getName()) ) {
                 int[] data = ColorTeaming.killDeathUserCounts.get(player.getName());
                 int point = data[0] * ColorTeamingConfig.killPoint +
                         data[1] * ColorTeamingConfig.deathPoint +
                         data[2] * ColorTeamingConfig.tkPoint;
                 objective.getScore(player).setScore(point);
             } else {
                 objective.getScore(player).setScore(1);
                 objective.getScore(player).setScore(0);
             }
         }
     }
 
     /**
      * スコア表示を削除する。
      */
     public void remove() {
        if ( ColorTeaming.getScoreboard().getObjective("listscore") != null ) {
            objective.unregister();
        }
     }
 }

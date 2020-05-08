 package org.thedoug.farkle;
 
 import org.thedoug.farkle.model.FarkleResult;
 import org.thedoug.farkle.player.Player;
 
 import java.util.LinkedHashMap;
import java.util.List;
 import java.util.Map;
 
 /**
  * Tallied scores from multiple Farkle matches.
  */
 public class AggregatedScores {
    // TODO switch Map to identity in case somebody implements equals() {return true}
    private Map<Player, Integer> scores;
 
     public AggregatedScores(Player[] players) {
         scores = initialScores(players);
     }
 
     private Map<Player, Integer> initialScores(Player[] players) {
         Map<Player, Integer> scores = new LinkedHashMap<Player, Integer>();
         for (Player player : players) {
             scores.put(player, 0);
         }
         return scores;
     }
 
     public void add(FarkleResult result) {
        List<Player> winners = result.getWinners();
        for (Player winner : winners) {
            scores.put(winner, scores.get(winner) + 1);
        }
     }
 
     public void printReport() {
         for (Map.Entry<Player, Integer> score : scores.entrySet()) {
            System.out.println(score.getValue() + " wins : " + score.getKey());
         }
     }
 }

 package org.thedoug.farkle.model;
 
 import org.thedoug.farkle.player.Player;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class FarkleResult {
     private Map<Player, Integer> scores;
 
     public FarkleResult(Map<Player, Integer> scores) {
         this.scores = scores;
     }
 
     public List<Player> getWinners() {
         List<Player> winners = new ArrayList<Player>(2);
         for (Player player: scores.keySet()) {
             Integer playerScore = scores.get(player);
            if (playerScore > 10000) {
                 winners.add(player);
             }
         }
 
         assert !winners.isEmpty() : "Nobody won";
         return winners;
     }
 
     @Override
     public String toString() {
         return "FarkleResult{" +
                 "scores=" + scores +
                 '}';
     }
 }

 package org.thedoug.farkle;
 
 import org.thedoug.farkle.model.*;
 import org.thedoug.farkle.player.Player;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Runs the game against the provided players.
  */
 public class FarkleEngine {
 
     private Player[] players;
 
    private static final int MAX_SCORE = 10000;
 
     private Roller roller;
 
     private Scorer scorer;
 
     private Map<Player, Integer> scores;
 
     public FarkleEngine(Roller roller, Scorer scorer, Player... players) {
         this.players = players;
         this.roller = roller;
         this.scorer = scorer;
         this.scores = getInitialScores(players);
     }
 
     public FarkleResult run() {
         while (!somePlayerHasWon()) {
             for (Player player : players) {
                 GameState gameState = rollAndStuff(new GameState());
 
                 while(gameState.canRollAgain() && player.shouldRollAgain(gameState)) {
                     gameState = rollAndStuff(gameState);
                 }
 
                 updateScoreForTurn(player, gameState.turnInfo());
             }
         }
 
         return new FarkleResult(scores);
     }
 
     private void updateScoreForTurn(Player player, Turn turnInfo) {
         int newScore = scores.get(player) + turnInfo.getScoredPoints();
         scores.put(player, newScore);
     }
 
     private Map<Player, Integer> getInitialScores(Player[] players) {
         Map<Player, Integer> initialScores = new HashMap<Player, Integer>();
 
         for (Player player : players) {
             initialScores.put(player, 0);
         }
 
         return initialScores;
     }
 
     private GameState rollAndStuff(GameState previous) {
         assert previous.canRollAgain();
 
         int nextRollIteration = previous.turnInfo().getRollIteration() + 1;
 
         List<Integer> rolls = rollSomeDice(previous.turnInfo().getRemainingDice());
         ScoringResult result = scorer.score(rolls);
 
         GameState nextState;
         if (result.isFarkled()) {
             nextState = new GameState(new Turn(0, 0, nextRollIteration));
         } else {
             int previousScore = previous.turnInfo().getScoredPoints();
             int newScore = previousScore + result.getScore();
 
             nextState = new GameState(new Turn(newScore, result.getRemainingDice(), nextRollIteration));
         }
 
         return nextState;
     }
 
     private List<Integer> rollSomeDice(int diceToRoll) {
         List<Integer> rolls = new ArrayList<Integer>();
         for (int i = 0; i < diceToRoll; i++) {
             int result = roller.rollSingleDie();
             rolls.add(result);
         }
         return rolls;
     }
 
     private boolean hasWon(Player player) {
         return scores.get(player) >= MAX_SCORE;
     }
 
     private boolean somePlayerHasWon() {
         for (Player player: players) {
             if (hasWon(player)) {
                 return true;
             }
         }
         return false;
     }
 }

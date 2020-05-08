 package org.thedoug.farkle.player;
 
 import org.thedoug.farkle.model.GameState;
 
 public class InstrumentedPlayer implements Player {
 
     private Player underlying;
     private long msSpent = 0L;
     private int decisions = 0;
     private int yesAnswers = 0;
 
     public InstrumentedPlayer(Player underlying) {
         this.underlying = underlying;
     }
 
     @Override
     public boolean shouldRollAgain(GameState gameState) {
         long start = System.currentTimeMillis();
 
         boolean response = underlying.shouldRollAgain(gameState);
 
         msSpent += System.currentTimeMillis() - start;
 
         decisions++;
         if (response) yesAnswers++;
 
         return response;
     }
 
     public int averageTimePerDecisionInMillis() {
        return decisions == 0 ? 0 : (int) msSpent / decisions;
     }
 
     @Override
     public String toString() {
         return underlying +
                 "{decisions=" + decisions +
                 ", yesAnswers=" + yesAnswers +
                 ", msPerDecision=" + averageTimePerDecisionInMillis() +
                 '}';
     }
 
     public static InstrumentedPlayer instrument(Player player) {
         return new InstrumentedPlayer(player);
     }
 
     public static InstrumentedPlayer[] instrumentAll(Player[] players) {
         InstrumentedPlayer[] instrumentedPlayers = new InstrumentedPlayer[players.length];
         for (int i = 0; i < players.length; i++) {
             instrumentedPlayers[i] = instrument(players[i]);
         }
         return instrumentedPlayers;
     }
 }

 package game.fitness;
 
 import game.State;
 import game.ai.Fitness;
 
 import java.util.Set;
 
 public class ManhattanDirectedFitness implements Fitness {
 
   @Override
   public int evaluate(State state) {
     int maxDistance = state.board.height + state.board.width;
     int minDistance;
     if (state.lambdaPositions.isEmpty())
       minDistance = minDistance(state.robotCol, state.robotRow, state.staticConfig.liftPositions);
     else
       minDistance = minDistance(state.robotCol, state.robotRow, state.lambdaPositions);
     
    return (int) ((1 - (double) minDistance / maxDistance) * 1000000);
   }
 
   public static int minDistance(int col, int row, int[] positions) {
     int minDistance = Integer.MAX_VALUE;
     
     for (int pos : positions) {
       int c = pos / 2;
       int r = pos % 2;
       int distance = Math.abs(col - c) + Math.abs(row - r);
       minDistance = Math.min(minDistance, distance);
     }
 
     return minDistance;
   }
 
   
   public static int minDistance(int col, int row, Set<Integer> positions) {
     int minDistance = Integer.MAX_VALUE;
     
     for (int pos : positions) {
       int c = pos / 2;
       int r = pos % 2;
       int distance = Math.abs(col - c) + Math.abs(row - r);
       minDistance = Math.min(minDistance, distance);
     }
 
     return minDistance;
   }
 
 }

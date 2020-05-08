 package game.fitness;
 
 
 import game.Cell;
 import game.State;
 import game.ai.Fitness;
 
 /**
  * Penalizes beard
  * @author Felix Rieger
  *
  */
 public class BeardFitness implements Fitness {
 
   /* (non-Javadoc)
    * @see game.ai.Fitness#evaluate(game.State)
    */
   @Override
   public int evaluate(State state) {
     // fitness is bad when the beard grows
     int beardCount = state.board.bitsets[Cell.Beard.ordinal()].size();
    return (int) (1000000 * (1 - (beardCount / ((double) state.board.length))));
   }
 
 }

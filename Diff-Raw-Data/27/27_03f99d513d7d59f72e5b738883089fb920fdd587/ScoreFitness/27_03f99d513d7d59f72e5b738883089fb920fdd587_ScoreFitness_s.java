 package game.fitness;
 
 import game.State;
 import game.ai.Fitness;
 import game.util.Scoring;
 
 /**
  * Evaluates how many points we already secured in a state.
  * 
  * @author Tillmann Rendel
  */
 
 public class ScoreFitness implements Fitness {
   @Override
   public int evaluate(State state) {
     int maximalScore = Scoring.maximalScoreWithoutLift(state);
 
     if (maximalScore == 0) {
       maximalScore = 1;
     }
 
    int fitness = (int) (((double) state.score /  maximalScore) * 1000000);
     return fitness;
   }
 }

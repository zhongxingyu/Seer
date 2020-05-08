 package game.fitness;
 
 import game.State;
 import game.ai.Fitness;
import game.log.Log;
 
 /**
  * A scorer that returns the average score of a set of other scorers.
  * 
  * @author Tillmann Rendel
  * @author Thomas Horstmeyer
  */
 public class AverageFitness implements Fitness {
 
   public Fitness[] scorers;
 
   public AverageFitness(Fitness... scorers) {
     this.scorers = scorers;
   }
 
   @Override
   public int evaluate(State state) {
     int sum = 0;
     for (Fitness scorer : scorers) {
      int f = scorer.evaluate(state);
//      Log.println(scorer.evaluate(state));
      sum += f;
     }
 
     return sum / scorers.length;
   }
 }

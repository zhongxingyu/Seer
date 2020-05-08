 package game.fitness;
 
 import game.State;
 import game.ai.Fitness;
 
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
      sum += scorer.evaluate(state);
     }
 
     return sum / scorers.length;
   }
 }

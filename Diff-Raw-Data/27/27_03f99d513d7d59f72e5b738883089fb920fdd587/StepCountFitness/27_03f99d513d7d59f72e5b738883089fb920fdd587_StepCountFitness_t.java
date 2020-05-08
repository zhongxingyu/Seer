 package game.fitness;
 
 import game.State;
 import game.StaticConfig;
 import game.ai.Fitness;
 
 /**
  * Tries to minimizes numbers of steps.
  * 
  * @author seba
  *
  */
 public class StepCountFitness implements Fitness {
 
   private final StaticConfig sconfig;
   
   public StepCountFitness(StaticConfig sconfig) {
     this.sconfig = sconfig;
   }
   
   /* (non-Javadoc)
    * @see game.ai.Fitness#evaluate(game.State)
    */
   @Override
   public int evaluate(State state) {
    return (int) ((1 - ((double) state.steps / sconfig.maxStepsAprox)) * 1000000);
   }
 
 }

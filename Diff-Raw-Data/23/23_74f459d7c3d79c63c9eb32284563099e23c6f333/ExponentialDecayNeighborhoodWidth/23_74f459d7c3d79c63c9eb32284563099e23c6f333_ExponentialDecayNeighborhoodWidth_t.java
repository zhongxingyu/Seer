 package cs437.som.neighborhood;
 
 import cs437.som.NeighborhoodWidthFunction;
 
 /**
  * Neighborhood width strategy for self-organizing maps that decays the width
  * exponentially as the iterations progress.
  *
  * <pre>
  * The exact behavior follows the formula:
 *      w_i * e^(-t / t_{max})
  *  where
 *      w_i     is the initial width of the neighborhood
 *      e       is the base of the natural logarithm
 *      t       is the current iteration
 *      t_{max} is the maximum expected iteration
  * </pre>
  */
 public class ExponentialDecayNeighborhoodWidth implements NeighborhoodWidthFunction {
     private final double initialNeighborhoodWidth;
     private double expectedIterations = 0.0;
 
     /**
      * Create an exponentially decaying neighborhood width function.
      *
      * @param initialWidth The initial width of the neighborhood.
      */
     public ExponentialDecayNeighborhoodWidth(double initialWidth) {
         initialNeighborhoodWidth = initialWidth;
     }
 
     public void setExpectedIterations(int expectedIterations) {
         this.expectedIterations = expectedIterations;
     }
 
     public double neighborhoodWidth(int iteration) {
         return initialNeighborhoodWidth * Math.exp(-iteration / expectedIterations);
     }
 
     @Override
     public String toString() {
         return "ExponentialDecayNeighborhoodWidth";
     }
 }

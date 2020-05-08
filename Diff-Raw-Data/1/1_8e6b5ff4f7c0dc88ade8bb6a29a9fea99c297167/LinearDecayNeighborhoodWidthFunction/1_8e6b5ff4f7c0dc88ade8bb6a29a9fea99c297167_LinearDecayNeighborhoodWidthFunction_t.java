 package cs437.som.neighborhood;
 
 import cs437.som.NeightborhoodWidthFunction;
 
 /**
  * Neighborhood width strategy for self-organizing maps that decays the width
  * linearly as the iterations progress.
  *
  * The exact behavior follows the formula:
  *      w_i * (1 - (-t / t_max))
  *  where
  *      w_i   is the initial width of the neighborhood
  *      t     is the current iteration
  *      t_max is the maximum expected iteration
  */
 public class LinearDecayNeighborhoodWidthFunction implements NeightborhoodWidthFunction {
     private final double initialNeighborhoodWidth;
     private double expectedIterations = 0.0;
 
     public LinearDecayNeighborhoodWidthFunction(double initialWidth) {
         initialNeighborhoodWidth = initialWidth;
     }
 
     public void setExpectedIterations(int expectedIterations) {
         this.expectedIterations = expectedIterations;
     }
 
     public double neighborhoodWidth(int iteration) {
         return initialNeighborhoodWidth * (1.0 - (iteration / expectedIterations));
     }
 
     @Override
     public String toString() {
         return "LinearDecayNeighborhoodWidthFunction";
     }
 }

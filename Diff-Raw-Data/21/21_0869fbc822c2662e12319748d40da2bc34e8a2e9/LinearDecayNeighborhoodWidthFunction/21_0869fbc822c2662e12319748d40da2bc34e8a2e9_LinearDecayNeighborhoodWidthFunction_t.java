 package cs437.som.neighborhood;
 
 import cs437.som.NeighborhoodWidthFunction;
 
 /**
  * Neighborhood width strategy for self-organizing maps that decays the width
  * linearly as the iterations progress.
  *
  * <pre>
  * The exact behavior follows the formula:
 *      w_i \cdot (1 - \frac{t}{t_{max}})
  *  where
 *      w_i     is the initial width of the neighborhood
 *      t       is the current iteration
 *      t_{max} is the maximum expected iteration
  * </pre>
  */
 public class LinearDecayNeighborhoodWidthFunction implements NeighborhoodWidthFunction {
     private final double initialNeighborhoodWidth;
     private double expectedIterations = 0.0;
 
     /**
      * Create a neighborhood width function that decays linearly.
      *
      * @param initialWidth The initial width of the neighborhood.
      */
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

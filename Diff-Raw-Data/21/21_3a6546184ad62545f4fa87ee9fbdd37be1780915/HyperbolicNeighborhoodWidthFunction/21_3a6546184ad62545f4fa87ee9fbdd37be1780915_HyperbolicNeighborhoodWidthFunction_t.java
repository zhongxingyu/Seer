 package cs437.som.neighborhood;
 
 import cs437.som.NeighborhoodWidthFunction;
 
 /**
  * Hyperbolic neighborhood width strategy for self-organizing map.
  *
  * <pre>
  * The exact behavior follows the formula:
 *      \frac{w_i}{(t + t_{max})}
  *  where
 *      w_i     is the initial width of the neighborhood
 *      t       is the current iteration
 *      t_{max} is the maximum expected iteration
  * </pre>
  */
 public class HyperbolicNeighborhoodWidthFunction implements NeighborhoodWidthFunction {
     private double expectedIterations = 0.0;
 
     public void setExpectedIterations(int expectedIterations) {
         this.expectedIterations = expectedIterations;
     }
 
     public double neighborhoodWidth(int iteration) {
         return expectedIterations / (expectedIterations + iteration);
     }
 
     @Override
     public String toString() {
         return "HyperbolicNeighborhoodWidthFunction";
     }
 }

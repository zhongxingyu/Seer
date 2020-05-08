 package cs437.som.neighborhood;
 
 import cs437.som.NeighborhoodWidthFunction;
 
 /**
  * Neighborhood width strategy for self-organizing maps that decays the width
  * linearly as the iterations progress.
  *
  * <pre>
  * The exact behavior follows the formula:
 *      w_i * (1 - (-t / t_max))
  *  where
 *      w_i   is the initial width of the neighborhood
 *      t     is the current iteration
 *      t_max is the maximum expected iteration
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
 
    /**
     * {@inheritDoc}
     */
     public void setExpectedIterations(int expectedIterations) {
         this.expectedIterations = expectedIterations;
     }
 
    /**
     * {@inheritDoc}
     */
     public double neighborhoodWidth(int iteration) {
         return initialNeighborhoodWidth * (1.0 - (iteration / expectedIterations));
     }
 
    /**
     * {@inheritDoc}
     */
     @Override
     public String toString() {
         return "LinearDecayNeighborhoodWidthFunction";
     }
 }

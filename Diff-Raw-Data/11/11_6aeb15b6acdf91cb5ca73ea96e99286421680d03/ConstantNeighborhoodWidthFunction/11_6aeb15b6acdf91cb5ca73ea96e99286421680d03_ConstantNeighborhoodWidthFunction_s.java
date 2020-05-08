 package cs437.som.neighborhood;
 
 import cs437.som.NeighborhoodWidthFunction;
 
 /**
  * Constant neighborhood strategy.
  */
 public class ConstantNeighborhoodWidthFunction implements NeighborhoodWidthFunction {
     private final double neighborhoodWidth;
 
     /**
      * Create a constant neighborhood width function.
      *
      * @param width The width of the neighborhood at all iterations.
      */
     public ConstantNeighborhoodWidthFunction(double width) {
         neighborhoodWidth = width;
     }
 
    /**
     * {@inheritDoc}
     */
     public void setExpectedIterations(int expectedIterations) {
     }
 
    /**
     * {@inheritDoc}
     */
     public double neighborhoodWidth(int iteration) {
         return neighborhoodWidth;
     }
 
    /**
     * {@inheritDoc}
     */
     @Override
     public String toString() {
         return "ConstantNeighborhoodWidthFunction";
     }
 }

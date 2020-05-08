 package cs437.som.topology;
 
 import cs437.som.Dimension;
 import cs437.som.GridType;
 import cs437.som.SOMError;
 
 /**
  * Toroidal square grid strategy for self-organizing map, using the Euclidean
  * metric for internal distance calculations.
  *
  * The layout is, for a 5 by 3 grid, as in the following illustration.  Because
  * the grid is toroidal, 0 is adjacent to 10 and 4.  In other words, the grid
  * "wraps" vertically and horizontally.
  * <pre>
  * _____________________
  * | 0 | 1 | 2 | 3 | 4 |
  * |---|---|---|---|---|
  * | 5 | 6 | 7 | 8 | 9 |
  * |---|---|---|---|---|
  * | 10| 11| 12| 13| 14|
  * |___|___|___|___|___|
  * </pre>
  */
 public class ToroidalEuclideanSquare implements GridType {
     private int neuronCount = 0;
     private int width = 0;
     private int height = 0;
 
     @Override
     public void setNeuronCount(Dimension dimension) {
         width = dimension.x;
         height = dimension.y;
         neuronCount = width * dimension.y;
     }
 
     @Override
     public double gridDistance(int neuron0, int neuron1) {
         if (neuron0 > neuronCount || neuron0 < 0) {
             throw new SOMError("Invalid neuron index: " + neuron0);
         }
 
         if (neuron1 > neuronCount || neuron1 < 0) {
             throw new SOMError("Invalid neuron index: " + neuron1);
         }
 
         int x0 = neuron0 % width;
         int x1 = neuron1 % width;
         int y0 = neuron0 / width;
         int y1 = neuron1 / width;
 
         double dx = Math.abs(x1 - x0);
         if (dx > width / 2) {
            dx = (width / 2) - dx;
         }
         double dy = Math.abs(y1 - y0);
         if (dy > height / 2) {
            dy = (height / 2) - dy;
         }
 
         return Math.sqrt((dy * dy) + (dx * dx));
     }
 
     @Override
     public String toString() {
         return "ToroidalEuclideanSquare";
     }
 }

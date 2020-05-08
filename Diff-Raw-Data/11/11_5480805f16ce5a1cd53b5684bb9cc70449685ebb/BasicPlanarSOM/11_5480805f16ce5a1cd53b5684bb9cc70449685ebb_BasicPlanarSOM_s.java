 package cs437.som.network;
 
 import cs437.som.Dimension;
 
 import java.util.Arrays;
 
 /**
  * A basic self-organizing map where the neurons are arranged by their weights.
  *
  * An simple example usage:
  *
  * <code>
  * <pre>
  * private static final int iterations = 500;
  *
  * public static void main(String[] args) {
  *     SelfOrganizingMap som = new BasicPlanarGridSOM(7, 2, iterations);
  *     Random r = new SecureRandom();
  *
  *     for (int i = 0; i < iterations; i++) {
  *         double[] in = {r.nextDouble() * 10, r.nextDouble() * 10};
  *         som.trainWith(in);
  *     }
  *
  *     // At this point, the SOM would be queried for it's BMU.
  * }
  * </pre>
  * </code>
  */
 public class BasicPlanarSOM extends NetworkBase {
 
     public BasicPlanarSOM(int neuronCount, int inputVectorSize, int expectedIterations) {
         super(new Dimension(neuronCount, 1), inputVectorSize, expectedIterations);
     }
 
    /**
     * {@inheritDoc}
     */
     @Override
     protected double neuronDistance(int neuron0, int neuron1) {
         double sum = 0.0;
         for (int i = 0; i < inputVectorSize; i++) {
             double difference = weightMatrix[neuron0][i] - weightMatrix[neuron1][i];
             sum += difference * difference;
         }
         return Math.sqrt(sum);
     }
 
    /**
     * {@inheritDoc}
     */
     @Override
     protected double neighborhoodWidth() {
         if (time < (expectedIterations / 5))
             return 1.0 * (1.0 - (time / (double) expectedIterations));
         return 0.0;
     }
 
    /**
     * {@inheritDoc}
     */
     @Override
     public String toString() {
         return "BasicPlanarSOM{weightMatrix=" + (weightMatrix == null ? null : Arrays.toString(weightMatrix)) + '}';
     }
 }

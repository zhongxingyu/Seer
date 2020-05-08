 package cs437.som.demo;
 
 import cs437.som.Dimension;
 import cs437.som.TrainableSelfOrganizingMap;
import cs437.som.neighborhood.LinearDecayNeighborhoodWidthFunction;
 import cs437.som.network.CustomizableSOM;
import cs437.som.topology.SquareGrid;
 import cs437.som.visualization.SOM3dPlotter;
 
 import java.security.SecureRandom;
 import java.util.Random;
 import java.util.logging.Logger;
 
 /**
  * Demonstrates maps with 3-dimensional inputs with a visualization during training.
  */
 public class ColorMapDemo {
    private static final int iterDelay = 20; /* ms */
    private static final int MAPPING_LINE_WIDTH = 330;

     private TrainableSelfOrganizingMap som = null;
     private Logger logger = Logger.getLogger("ColorMapDemo");
 
     /**
      * Create a new SOM demo.
      * @param map An untrained SOM to watch.
      */
     public ColorMapDemo(TrainableSelfOrganizingMap map) {
         som = map;
     }
 
     public void run() {
         int iterations = som.getExpectedIterations();
         Random r = new SecureRandom();
 
         int numSamples = 6;
         double[][] samples = new double[numSamples][3]; // number of samples, depth
 
         logger.info("Selecting Training Samples");
 
         for (double[] color : samples) {
             color[0] = r.nextDouble();
             color[1] = r.nextDouble();
             color[2] = r.nextDouble();
         }
 
         logger.info("Before Training");
 
         SOM3dPlotter plot = new SOM3dPlotter(som);
         for (int i = 0; i < iterations; i++) {
             double[] in = samples[r.nextInt(numSamples)];
             som.trainWith(in);
             plot.draw();
            //try { Thread.sleep(iterDelay); } catch (InterruptedException ignored) { }
         }
 
         logger.info("After training");
     }
 
     public static void main(String[] args) {
        //new ColorMapDemo(new BasicSquareGridSOM(new Dimension(250, 250), 3, 1000)).run();
        CustomizableSOM som = new CustomizableSOM(new Dimension(250, 250), 3, 1000);
        som.setNeighborhoodWidthFunctionStrategy(new LinearDecayNeighborhoodWidthFunction(150));
        //som.setLearningRateFunctionStrategy(new ExponentialDecayLearningRateFunction(.1));
        //som.setDistanceMetricStrategy(new EuclideanDistanceMetric());

        // remember to figure out why the default initialization isn't working
        som.setGridTypeStrategy(new SquareGrid());

         new ColorMapDemo(som).run();
     }
 
     @Override
     public String toString() {
         return "BasicMapDemo{som=" + som + '}';
     }
 }

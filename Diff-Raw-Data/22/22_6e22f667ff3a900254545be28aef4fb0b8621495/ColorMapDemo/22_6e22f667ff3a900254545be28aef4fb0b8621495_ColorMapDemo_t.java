 package cs437.som.demo;
 
 import cs437.som.Dimension;
 import cs437.som.TrainableSelfOrganizingMap;
 import cs437.som.network.CustomizableSOM;
 import cs437.som.visualization.SOM3dPlotter;
 
 import java.security.SecureRandom;
 import java.util.Random;
 import java.util.logging.Logger;
 
 /**
  * Demonstrates maps with 3-dimensional inputs with a visualization during training.
  */
 public class ColorMapDemo {
    private static final int MAP_DIMENSION = 250;
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
         }
 
         logger.info("After training");
     }
 
     public static void main(String[] args) {
        Dimension dimension = new Dimension(MAP_DIMENSION, MAP_DIMENSION);
        CustomizableSOM som = new CustomizableSOM(dimension, 3, 1000);
         new ColorMapDemo(som).run();
     }
 
     @Override
     public String toString() {
         return "BasicMapDemo{som=" + som + '}';
     }
 }

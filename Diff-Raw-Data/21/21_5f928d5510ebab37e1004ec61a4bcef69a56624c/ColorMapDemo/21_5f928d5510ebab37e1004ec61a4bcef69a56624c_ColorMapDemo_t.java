 package cs437.som.demo;
 
 import cs437.som.Dimension;
 import cs437.som.TrainableSelfOrganizingMap;
 import cs437.som.distancemetrics.EuclideanDistanceMetric;
 import cs437.som.membership.ConstantNeighborhoodMembershipFunction;
 import cs437.som.membership.GeometricNeighborhoodMembershipFunction;
 import cs437.som.membership.LinearNeighborhoodMembershipFunction;
 import cs437.som.membership.RandomNeighborhoodMembershipFunction;
 import cs437.som.neighborhood.LinearDecayNeighborhoodWidthFunction;
 import cs437.som.network.CustomizableSOM;
 import cs437.som.visualization.SOMColorPlotter;
 import cs437.som.visualization.SOMHeatMap;
 
 import java.security.SecureRandom;
 import java.util.Random;
 import java.util.logging.Logger;
 
 /**
  * Demonstrates maps with 3-dimensional inputs with a visualization during training.
  */
 public class ColorMapDemo {
     private static final int MAP_DIMENSION = 500;
     private TrainableSelfOrganizingMap som = null;
     private final Logger logger = Logger.getLogger("ColorMapDemo");
 
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
 
         double[] heatMapSample = {1,0,0};
 
         SOMColorPlotter plot = new SOMColorPlotter(som);
         SOMHeatMap heatMap = new SOMHeatMap(som);
         for (int i = 0; i < iterations; i++) {
             double[] in = samples[r.nextInt(numSamples)];
             som.trainWith(in);
             plot.draw();
             heatMap.update(heatMapSample);
             heatMap.draw();
         }
 
         logger.info("After training");
        double[] newHeatMapSample = {0,0,1};
        heatMap.update(newHeatMapSample);
        heatMap.draw();
     }
 
     public static void main(String[] args) {
         Dimension dimension = new Dimension(MAP_DIMENSION, MAP_DIMENSION);
        CustomizableSOM som = new CustomizableSOM(dimension, 3, 1000);
         som.setDistanceMetricStrategy(new EuclideanDistanceMetric());
         som.setNeighborhoodWidthFunctionStrategy(new LinearDecayNeighborhoodWidthFunction(MAP_DIMENSION));
         som.setNeighborhoodMembershipFunctionStrategy(new GeometricNeighborhoodMembershipFunction(.75));
         new ColorMapDemo(som).run();
     }
 
     @Override
     public String toString() {
         return "ColorMapDemo{som=" + som + '}';
     }
 }

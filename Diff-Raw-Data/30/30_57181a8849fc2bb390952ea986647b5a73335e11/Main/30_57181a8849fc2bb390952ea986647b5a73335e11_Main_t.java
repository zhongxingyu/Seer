 /*
  * Copyright (C) 2012 Robert Moore and Rutgers University
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *  
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *  
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 package edu.rutgers.winlab.junsim;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.thoughtworks.xstream.XStream;
 
 /**
  * Main class to start the receiver placement simulations.
  * 
  * @author Robert Moore
  */
 public class Main {
 
   private static final Logger log = LoggerFactory.getLogger(Main.class);
   /**
    * Configuration file for the application.
    */
   static Config config = new Config();
 
   /**
    * Configuration for rendering images.
    */
   static RenderConfig gfxConfig = new RenderConfig();
 
   /**
    * Random number generator.
    */
   static Random rand;
 
   /**
    * Worker threads for executing parallel tasks.
    */
   static ExecutorService workers = null;
 
   /**
    * Maximum number of worker threads to use.
    */
   static int maxConcurrentTasks = 1;
 
   /**
    * Parses the commandline arguments and starts the simulation.
    * 
    * @param args
    *          configuration file
    * @throws IOException
    *           if an exception occurs while reading the configuration file.
    */
   public static void main(String[] args) throws IOException {
     XStream configReader = new XStream();
     if (args.length == 1) {
       System.out.println("Using configuration file " + args[0]);
 
       File configFile = new File(args[0]);
       Main.config = (Config) configReader.fromXML(configFile);
     } else {
       System.out.println("Using built-in default configuration.");
     }
     rand = new Random(Main.config.randomSeed);
 
     try {
       RenderConfig rConf = (RenderConfig) configReader.fromXML(new File(
           config.renderConfig));
       gfxConfig = rConf;
 
     } catch (Exception e) {
       System.err.println("Unable to read rendering configuration file \""
           + config.renderConfig + "\".");
       e.printStackTrace();
     }
 
     if (Main.config.numThreads < 1) {
       Main.config.numThreads = Runtime.getRuntime().availableProcessors();
       workers = Executors.newFixedThreadPool(Main.config.numThreads);
       maxConcurrentTasks = Main.config.numThreads;
       System.out.println("Using " + Main.config.numThreads
           + " threads based on process availability.");
     } else {
       workers = Executors.newFixedThreadPool(Main.config.numThreads);
       maxConcurrentTasks = Main.config.numThreads;
       System.out.println("Using " + Main.config.numThreads
           + " threads based on configuration file.");
     }
 
     // Shutdown handler (for signals from OS)
     Runtime.getRuntime().addShutdownHook(new Thread() {
       @Override
       public void run() {
         Main.workers.shutdownNow();
       }
     });
 
     doSimulation();
 
   }
 
   /**
    * Perform an unattended set of simulations.
    * 
    * @throws IOException
    *           if an exception is thrown.
    */
   public static void doSimulation() throws IOException {
     File outputFile = new File(Main.buildPath(Main.config.getOutputFileName()));
     if (!outputFile.exists()) {
       if (outputFile.getParentFile() != null) {
         outputFile.getParentFile().mkdirs();
       }
       outputFile.createNewFile();
     }
     if (!outputFile.canWrite()) {
       System.err.println("Unable to write to " + outputFile.getName()
           + ". Please check file system permissions.");
       return;
     }
 
     // Output file (CSV) for stats
     PrintWriter fileWriter = new PrintWriter(new FileWriter(outputFile));
 
     PrintWriter receiverWriter = new PrintWriter(new FileWriter(
         Main.buildPath(config.getReceiversFile())));
 
     Collection<Transmitter> transmitters = new LinkedList<Transmitter>();
     File transmittersFile = null;
     if (config.getTransmittersFile() != null
         && config.getTransmittersFile().trim().length() > 0) {
       transmittersFile = new File(Main.buildPath(config.getTransmittersFile()
           .trim()));
       if (transmittersFile.exists() && transmittersFile.canRead()) {
         BufferedReader txReader = new BufferedReader(new FileReader(
             transmittersFile));
         String line = null;
         while ((line = txReader.readLine()) != null) {
           String[] components = line.split("\\s+");
           if (components.length < 2) {
             log.info("Skipping line \"{}\".", line);
             continue;
           }
           float xPos = Float.parseFloat(components[0]);
           float yPos = Float.parseFloat(components[1]);
           final Transmitter txer = new Transmitter();
           txer.x = xPos;
           txer.y = yPos;
           transmitters.add(txer);
         }
       }
 
     }
     boolean generateTransmitters = transmitters.isEmpty();
 
     ExperimentStats[] stats = new ExperimentStats[Main.config.numReceivers];
     for (int i = 0; i < stats.length; ++i) {
       stats[i] = new ExperimentStats();
       stats[i].numberReceivers = i + 1;
       stats[i].numberTransmitters = generateTransmitters ? Main.config.numTransmitters
           : transmitters.size();
     }
 
     fileWriter
         .println("# Tx, # Rx, Min % Covered, Med. % Covered, Mean % Covered, 95% Coverage, Max % Covered, Min Contention, Med. Contention, Mean Contention, 95% Contention, Max Contention");
 
     // Iterate through some number of trials
     for (int trialNumber = 0; trialNumber < Main.config.numTrials; ++trialNumber) {
 
       // Randomly generate transmitter locations
       if (generateTransmitters) {
         if (Main.config.getTransmitterDistribution().startsWith("clustered")) {
 
           float probability = 0.5f;
           float radius = 0.1f;
           String[] parts = Main.config.getTransmitterDistribution()
               .split("\\s");
           if (parts.length > 1 && parts[1].length() > 0) {
             probability = Float.parseFloat(parts[1]);
             if (parts.length > 2 && parts[2].length() > 0) {
               radius = Float.parseFloat(parts[2]);
             }
           }
           transmitters = Main.generateClusteredTransmitterLocations(
               Main.config.numTransmitters, probability, radius);
         }
         // "Rectangled" distribution (inside big box, outside small box)
         else if (Main.config.getTransmitterDistribution().startsWith(
             "rectangled")) {
           float width = Math.min(Main.config.squareWidth,
               Main.config.squareHeight) * .1f;
 
           String[] parts = Main.config.getTransmitterDistribution()
               .split("\\s");
           if (parts.length > 1 && parts[1].length() > 0) {
             width = Float.parseFloat(parts[1]);
 
           }
           transmitters = Main.generateRectangledTransmitterLocations(
               Main.config.numTransmitters, width);
         }
         // "Circled" distribution (inside big box, outside small box)
         else if (Main.config.getTransmitterDistribution().startsWith("circled")) {
           float width = Math.min(Main.config.squareWidth,
               Main.config.squareHeight) * .1f;
 
           String[] parts = Main.config.getTransmitterDistribution()
               .split("\\s");
           if (parts.length > 1 && parts[1].length() > 0) {
             width = Float.parseFloat(parts[1]);
 
           }
           transmitters = Main.generateCircledTransmitterLocations(
               Main.config.numTransmitters, width);
         }
         // "Sine wave" distribution
         else if (Main.config.getTransmitterDistribution().startsWith("sine")) {
           float radius = Math.min(Main.config.squareWidth,
               Main.config.squareHeight) * .2f;
 
           String[] parts = Main.config.getTransmitterDistribution()
               .split("\\s");
           if (parts.length > 1 && parts[1].length() > 0) {
             radius = Float.parseFloat(parts[1]);
 
           }
           transmitters = Main.generateSineTransmitterLocations(
               Main.config.numTransmitters, radius);
         }
         // "Dumbbell" distribution
         else if (Main.config.getTransmitterDistribution().startsWith("dumbbell")) {
           float width = Math.min(Main.config.squareWidth,
               Main.config.squareHeight) * .2f;
 
           String[] parts = Main.config.getTransmitterDistribution()
               .split("\\s");
           if (parts.length > 1 && parts[1].length() > 0) {
             width = Float.parseFloat(parts[1]);
 
           }
           transmitters = Main.generateDumbbellTransmitterLocations(
               Main.config.numTransmitters, width);
         }
         // "Plus" (+) distribution
         else if (Main.config.getTransmitterDistribution().startsWith("plus")) {
           float width = .2f;
 
           String[] parts = Main.config.getTransmitterDistribution()
               .split("\\s");
           if (parts.length > 1 && parts[1].length() > 0) {
             width = Float.parseFloat(parts[1]);
 
           }
           transmitters = Main.generatePlusTransmitterLocations(
               Main.config.numTransmitters, width);
         }
         // Random with "holes" distribution
         else if (Main.config.getTransmitterDistribution().startsWith("2-holes")) {
           float width = .2f;
 
           String[] parts = Main.config.getTransmitterDistribution()
               .split("\\s");
           if (parts.length > 1 && parts[1].length() > 0) {
             width = Float.parseFloat(parts[1]);
 
           }
           transmitters = Main.generate2HolesTransmitterLocations(
               Main.config.numTransmitters, width);
         }
         // Basic uniform random distribution
         else {
           transmitters = Main
               .generateUniformTransmitterLocations(Main.config.numTransmitters);
         }
         PrintWriter txWriter = new PrintWriter(new FileWriter(
             Main.buildPath(Main.config.getTransmittersFile())));
         for (Transmitter txer : transmitters) {
           txWriter.printf("%.2f %.2f\n", txer.x, txer.y);
         }
         txWriter.flush();
         txWriter.close();
       } else {
         Main.generateUniformTransmitterLocations(Main.config.numTransmitters);
       }
 
       TaskConfig conf = new TaskConfig();
       conf.trialNumber = trialNumber;
       conf.numTransmitters = transmitters.size();
       conf.transmitters = transmitters;
       conf.numReceivers = Main.config.numReceivers;
       conf.receivers = new LinkedList<Receiver>();
 
       Experiment task;
       if ("binned".equalsIgnoreCase(config.experimentType)) {
         task = new BinnedBasicExperiment(conf, stats, workers);
       } else if ("grid".equalsIgnoreCase(config.experimentType)) {
         task = new BinnedGridExperiment(conf, stats, workers);
       } else if ("recursive".equalsIgnoreCase(config.experimentType)) {
         task = new BinnedRecurGridExperiment(conf, stats, workers);
       } else {
         task = new BasicExperiment(conf, stats, workers);
       }
       task.perform();
       String prefix = "";
       if (Main.config.numTrials > 1) {
         prefix = Integer.valueOf(trialNumber).toString();
       }
       PrintWriter rxWriter = new PrintWriter(new FileWriter(
           Main.buildPath(prefix + Main.config.getReceiversFile())));
       for (Receiver rxer : conf.receivers) {
         rxWriter.printf("%.2f %.2f %d\n", rxer.x, rxer.y,
             rxer.coveringDisks.size());
       }
       rxWriter.flush();
       rxWriter.close();
     } // End number of trials
 
     workers.shutdown();
     System.out.println("Waiting up to 60 seconds for threadpool to terminate.");
     try {
       workers.awaitTermination(60, TimeUnit.SECONDS);
     } catch (InterruptedException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
 
     // # Tx, # Rx, Min % Covered, Med. %
     // Covered, Mean % Covered, Max % Covered, 95% Coverage
     for (ExperimentStats s : stats) {
       fileWriter
           .printf(
               "%d, %d, %.4f, %.4f, %.4f, %.4f, %.4f, %.5f, %.5f, %.5f, %.5f, %.5f\n",
               Integer.valueOf(s.numberTransmitters),
               Integer.valueOf(s.numberReceivers),
               Float.valueOf(s.getMinCoverage()),
               Float.valueOf(s.getMedianCoverage()),
               Float.valueOf(s.getMeanCoverage()),
               Float.valueOf(s.get95PercentileCoverage()),
               Float.valueOf(s.getMaxCoverage()),
               Float.valueOf(s.getMinContention()),
               Float.valueOf(s.getMedianContention()),
               Float.valueOf(s.getMeanContention()),
               Float.valueOf(s.get95PercentileContention()),
               Float.valueOf(s.getMaxContention()));
     }
     fileWriter.flush();
     fileWriter.close();
   }
 
   /**
    * Randomly generate the locations of {@code numTransmitters} within the
    * bounding square.
    * 
    * @param numTransmitters
    *          the number of transmitters to generate.
    * @return an array of {@code Transmitter} objects randomly positioned.
    */
   static Collection<Transmitter> generateUniformTransmitterLocations(
       final int numTransmitters) {
 
     LinkedList<Transmitter> txers = new LinkedList<Transmitter>();
     Transmitter txer = null;
     for (int i = 0; i < numTransmitters; ++i) {
       txer = new Transmitter();
       txer.x = (Main.config.universeWidth - Main.config.squareWidth) * .5f
           + Main.rand.nextFloat() * Main.config.squareWidth;
       txer.y = (Main.config.universeHeight - Main.config.squareHeight) * .5f
           + Main.rand.nextFloat() * Main.config.squareHeight;
       txers.add(txer);
     }
     return txers;
   }
 
   /**
    * Randomly generate the locations of {@code numTransmitters} within the
    * bounding square, clustered around other transmitters. The two variables,
    * {@code clusterProb} and {@code radiusPct} dictate the clustering frequency
    * and density.
    * <p>
    * The first parameter, {@code clusterProb}, determines the probability that a
    * transmitter will be placed in a "clustered" location near another
    * transmitter. If this value were 0.2, then there is a 20% probability a
    * transmitter is placed "near" another, and an 80% chance that it will be
    * placed at a random location.
    * </p>
    * <p>
    * The second parameter, {@code radiusPct}, determines the maximum radius that
    * the clustered transmitter will be placed with respect to another
    * transmitter. This range (radius) is expressed as a fraction of the average
    * o the coordinate dimensions.
    * </p>
    * 
    * @param numTransmitters
    *          the number of transmitters to generate.
    * @param clusterProb
    *          the probability of placing a transmitter "near" to another rather
    *          than randomly around the coordinate space.
    * @param radiusPct
    *          the percent of the average width/height to use when clustering
    *          transmitters.
    * @return an array of {@code Transmitter} objects randomly positioned.
    */
   static Collection<Transmitter> generateClusteredTransmitterLocations(
       final int numTransmitters, final float clusterProb, final float radiusPct) {
     float usedCluster = clusterProb;
     if (usedCluster < 0) {
       usedCluster = 0f;
     } else if (usedCluster > 1) {
       usedCluster = 1f;
     }
     float usedRadius = radiusPct;
     if (usedRadius < 0) {
       usedRadius = .1f;
     } else if (usedRadius > 1) {
       usedRadius = 1f;
     }
 
     LinkedList<Transmitter> txers = new LinkedList<Transmitter>();
     Transmitter txer = null;
     float maxRadius = ((Main.config.squareWidth + Main.config.squareHeight) / 2)
         * usedRadius;
     float xOffset = (Main.config.universeWidth - Main.config.squareWidth) * .5f;
     float yOffset = (Main.config.universeHeight - Main.config.squareHeight) * .5f;
     for (int i = 0; i < numTransmitters; ++i) {
 
       txer = new Transmitter();
       // Pick a uniformly random position
       if (txers.isEmpty() || (rand.nextDouble() > usedCluster)) {
         txer.x = xOffset + Main.rand.nextFloat() * Main.config.squareWidth;
         txer.y = yOffset + Main.rand.nextFloat() * Main.config.squareHeight;
       }
       // Place it "near" another randomly-placed transmitter
       else {
         Transmitter randTx = txers.get(rand.nextInt(txers.size()));
         float radius = rand.nextFloat() * maxRadius;
         float theta = (float) (rand.nextFloat() * 2 * Math.PI);
         txer.x = randTx.x + (float) (Math.cos(theta) * radius);
         if (txer.x > Main.config.squareWidth + xOffset) {
           txer.x = Main.config.squareWidth;
         } else if (txer.x < xOffset) {
           txer.x = xOffset;
         }
         txer.y = randTx.y + (float) (Math.sin(theta) * radius);
         if (txer.y > Main.config.squareHeight + yOffset) {
           txer.y = Main.config.squareHeight;
         } else if (txer.y < yOffset) {
           txer.y = yOffset;
         }
       }
 
       txers.add(txer);
     }
     return txers;
   }
 
   /**
    * Randomly generate the locations of {@code numTransmitters} within the
    * bounding square.
    * 
    * @param numTransmitters
    *          the number of transmitters to generate.
    * @param width
    *          the width of the rectangular area.
    * @return an array of {@code Transmitter} objects randomly positioned.
    */
   static Collection<Transmitter> generateRectangledTransmitterLocations(
       final int numTransmitters, final float width) {
     float usedWidth = width;
     if (usedWidth <= 0) {
       usedWidth = 1f;
     }
     if (usedWidth > Math.min(Main.config.squareWidth / 2,
         Main.config.squareHeight / 2)) {
       usedWidth = Math.min(Main.config.squareWidth / 2,
           Main.config.squareHeight / 2);
     }
 
     LinkedList<Transmitter> txers = new LinkedList<Transmitter>();
     Transmitter txer = null;
 
     float xOffset = (Main.config.universeWidth - Main.config.squareWidth) * .5f;
     float yOffset = (Main.config.universeHeight - Main.config.squareHeight) * .5f;
     for (int i = 0; i < numTransmitters; ++i) {
 
       txer = new Transmitter();
       txer.x = xOffset + rand.nextFloat() * Main.config.squareWidth;
       txer.y = yOffset + rand.nextFloat() * Main.config.squareHeight;
 
       if (txer.x > (xOffset + usedWidth)
           && txer.x < (Main.config.squareWidth + xOffset - usedWidth)
           && txer.y > (yOffset + usedWidth)
           && txer.y < (Main.config.squareHeight + yOffset - usedWidth)) {
         --i;
         continue;
       }
 
       txers.add(txer);
     }
     return txers;
   }
 
   /**
    * Randomly generate the locations of {@code numTransmitters} within the
    * bounding circles.
    * 
    * @param numTransmitters
    *          the number of transmitters to generate.
    * @param width
    *          the width of the circular area.
    * @return an array of {@code Transmitter} objects randomly positioned.
    */
   static Collection<Transmitter> generateCircledTransmitterLocations(
       final int numTransmitters, final float width) {
     float usedWidth = width;
     if (usedWidth <= 0) {
       usedWidth = 1f;
     }
     if (usedWidth > Math.min(Main.config.squareWidth / 2,
         Main.config.squareHeight / 2)) {
       usedWidth = Math.min(Main.config.squareWidth / 2,
           Main.config.squareHeight / 2);
     }
 
     LinkedList<Transmitter> txers = new LinkedList<Transmitter>();
     Transmitter txer = null;
 
     float xOffset = (Main.config.universeWidth - Main.config.squareWidth) * .5f;
     float yOffset = (Main.config.universeHeight - Main.config.squareHeight) * .5f;
 
     float xCenter = Main.config.universeWidth / 2;
     float yCenter = Main.config.universeHeight / 2;
 
     Ellipse2D.Float bigEllipse = new Ellipse2D.Float(xOffset, yOffset,
         Main.config.squareWidth, Main.config.squareHeight);
     Ellipse2D.Float smallEllipse = new Ellipse2D.Float(xOffset + usedWidth,
         yOffset + usedWidth, Main.config.squareWidth - usedWidth * 2,
         Main.config.squareHeight - usedWidth * 2);
 
     for (int i = 0; i < numTransmitters; ++i) {
 
       txer = new Transmitter();
       txer.x = xOffset + rand.nextFloat() * Main.config.squareWidth;
       txer.y = yOffset + rand.nextFloat() * Main.config.squareHeight;
 
       // If the point is within the outer circle, but not the inner, then OK
       if (bigEllipse.contains(txer) && !smallEllipse.contains(txer)) {
 
         txers.add(txer);
       }
       // Otherwise try again.
       else {
         --i;
         continue;
       }
     }
     return txers;
   }
 
   /**
    * Randomly generate the locations of {@code numTransmitters} along a sine
    * wave.
    * 
    * @param numTransmitters
    *          the number of transmitters to generate.
    * @param radius
    *          the radius of the waves
    * @return an array of {@code Transmitter} objects randomly positioned.
    */
   static Collection<Transmitter> generateSineTransmitterLocations(
       final int numTransmitters, final float radius) {
 
     float usedRadius = radius;
     if (usedRadius < 0) {
       usedRadius = Math.min(Main.config.squareWidth / 4,
           Main.config.squareHeight / 4);
     } else if (usedRadius > Math.min(Main.config.squareWidth / 4,
         Main.config.squareHeight / 4)) {
       usedRadius = Math.min(Main.config.squareWidth / 4,
           Main.config.squareHeight / 4);
     }
 
     LinkedList<Transmitter> txers = new LinkedList<Transmitter>();
     Transmitter txer = null;
 
     float xOffset = (Main.config.universeWidth - Main.config.squareWidth) * .5f;
     float yOffset = (Main.config.universeHeight - Main.config.squareHeight) * .5f;
     float wiggle = usedRadius * 0.1f;
     float xCenter = xOffset + Main.config.squareWidth/2;
 
     float[] xCenters = { xCenter - usedRadius,
         xCenter + usedRadius };
    float yCenter = yOffset + Main.config.squareHeight / 2;
 
     for (int i = 0; i < numTransmitters; ++i) {
 
       txer = new Transmitter();
 
       // "Right" side
       if (rand.nextBoolean()) {
         float theta = rand.nextFloat() * (float) Math.PI;
         float x = xCenters[1] + (float) Math.cos(theta) * usedRadius;
        float y = yCenter + (float) Math.sin(theta) * usedRadius;
        txer.x = x - (wiggle / 2) + (rand.nextFloat() * wiggle);
        txer.y = y - (wiggle / 2) + (rand.nextFloat() * wiggle);
       }
      // "Left" side
       else {
         float theta = -rand.nextFloat() * (float) Math.PI;
         float x = xCenters[0] + (float) Math.cos(theta) * usedRadius;
        float y = yCenter + (float) Math.sin(theta) * usedRadius;
        txer.x = x - (wiggle / 2) + (rand.nextFloat() * wiggle);
        txer.y = y - (wiggle / 2) + (rand.nextFloat() * wiggle);
       }
       
       if (txer.x > Main.config.squareWidth + xOffset) {
        txer.x = xOffset + Main.config.squareWidth;
       } else if (txer.x < xOffset) {
         txer.x = xOffset;
       }
       
       if (txer.y > Main.config.squareHeight + yOffset) {
        txer.y = yOffset + Main.config.squareHeight;
       } else if (txer.y < yOffset) {
         txer.y = yOffset;
       }
 
       txers.add(txer);
     }
     return txers;
   }
   
   /**
    * Randomly generate the locations of {@code numTransmitters} within
    * a "dumbbell" shape.
    * 
    * @param numTransmitters
    *          the number of transmitters to generate.
    * @param width
    *          the width of the dumbbell's "bar" as a ratio of the width of the coordinate space
    * @return an array of {@code Transmitter} objects randomly positioned.
    */
   static Collection<Transmitter> generateDumbbellTransmitterLocations(
       final int numTransmitters, final float width) {
     LinkedList<Transmitter> txers = new LinkedList<Transmitter>();
     Transmitter txer = null;
 
     float xOffset = (Main.config.universeWidth - Main.config.squareWidth) * .5f;
     float yOffset = (Main.config.universeHeight - Main.config.squareHeight) * .5f;
     
     float usedWidth = width;
     if(usedWidth < 0){
       usedWidth = 0.1f;
     }else if(usedWidth > Main.config.squareWidth){
       usedWidth = Main.config.squareWidth*.99f;
     }
     
     // The entire structure is expected to be two squares connected by a "bar" of some
     // width.
     
     float bellWidth = (Main.config.squareWidth - usedWidth)/2;
     float verticalMargin = (Main.config.squareHeight - bellWidth)/2;
     
     float yCenter = yOffset + Main.config.squareHeight/2;
     float barHeight = bellWidth / 4;
     
     // Generate 3 rectangles.  The first two are the "weights", the third is the "bar".
     
     Rectangle2D.Float[] areas = {new Rectangle2D.Float(xOffset, yOffset + verticalMargin, bellWidth, bellWidth),
         new Rectangle2D.Float(xOffset+usedWidth+bellWidth,yOffset + verticalMargin, bellWidth, bellWidth),
         new Rectangle2D.Float(xOffset+bellWidth,yCenter-barHeight/2,usedWidth,barHeight)};
     
   
     // 40% probability in each bell, 20% prob in bar
     float[] areaProbs = {0.4f, 0.8f};
     
     for (int i = 0; i < numTransmitters; ++i) {
 
       txer = new Transmitter();
       float randVal = rand.nextFloat();
       // In the bar
       if(randVal > areaProbs[1]){
         txer.x = xOffset + bellWidth + rand.nextFloat()*usedWidth;
         txer.y = yCenter - barHeight/2 + rand.nextFloat()*barHeight;
       }
       // In the "left" bell
       else if(randVal > areaProbs[0]){
         txer.x = xOffset + rand.nextFloat()*bellWidth;
         txer.y = yOffset + verticalMargin + rand.nextFloat()*bellWidth;
       }
       // In the "right" bell
       else{
         txer.x = xOffset + bellWidth + usedWidth + rand.nextFloat()*bellWidth;
         txer.y = yOffset + verticalMargin + rand.nextFloat()*bellWidth;
       }
       txers.add(txer);
     }
     return txers;
   }
   
   /**
    * Randomly generate the locations of {@code numTransmitters} within
    * a "plus" shape.
    * 
    * @param numTransmitters
    *          the number of transmitters to generate.
    * @param widthPct
    *          how far away from the "+" center to place the devices.
    * @return an array of {@code Transmitter} objects randomly positioned.
    */
   static Collection<Transmitter> generatePlusTransmitterLocations(
       final int numTransmitters, final float widthPct) {
     LinkedList<Transmitter> txers = new LinkedList<Transmitter>();
     Transmitter txer = null;
 
     float xOffset = (Main.config.universeWidth - Main.config.squareWidth) * .5f;
     float yOffset = (Main.config.universeHeight - Main.config.squareHeight) * .5f;
     float xCenter = Main.config.squareWidth / 2 + xOffset;
     float yCenter = Main.config.squareHeight / 2 + yOffset;
     
     float usedWidth = widthPct;
     if(usedWidth < 0){
       usedWidth = 0.1f;
     }else if(usedWidth > Main.config.squareWidth){
       usedWidth = 1f;
     }
     
     float maxWidth = usedWidth * Math.min(Main.config.squareWidth, Main.config.squareHeight);
     
     float totalLength = Main.config.squareWidth + Main.config.squareHeight;
     float horizontalLength = Main.config.squareWidth;
    
     for (int i = 0; i < numTransmitters; ++i) {
 
       txer = new Transmitter();
       
       float l = rand.nextFloat()*totalLength;
      // Actually the offset
      float w = rand.nextFloat()*maxWidth - maxWidth/2;
       
       // Place it on the "vertical" bar
       if(l > horizontalLength){
         l -= horizontalLength;
         txer.y = yOffset + l;
         txer.x = xCenter + w;
       }
       // Place it on the "horizontal" bar
       else {
         txer.x = xOffset + l;
         txer.y = yCenter + w;
       }
       
       txers.add(txer);
     }
     return txers;
   }
   
   /**
    * Randomly generate the locations of {@code numTransmitters} excluding
    * two "holes" of configurable size.
    * 
    * @param numTransmitters
    *          the number of transmitters to generate.
    * @param radiusPct
    *          radius of each circle, as a percent of one-half of the minimally-sized
    *          dimension.
    * @return an array of {@code Transmitter} objects randomly positioned.
    */
     static Collection<Transmitter> generate2HolesTransmitterLocations(
       final int numTransmitters, final float radiusPct) {
     LinkedList<Transmitter> txers = new LinkedList<Transmitter>();
     Transmitter txer = null;
 
     float xOffset = (Main.config.universeWidth - Main.config.squareWidth) * .5f;
     float yOffset = (Main.config.universeHeight - Main.config.squareHeight) * .5f;
     
     float usedPct = radiusPct;
     if(usedPct <= 0){
       usedPct = 0.01f;
     }else if (usedPct > 1){
       usedPct = 1f;
     }
     
     float usedRadius = usedPct * Math.min(Main.config.squareWidth, Main.config.squareHeight)/4;
     
     float[] xCenters = {xOffset + Main.config.squareWidth / 4, xOffset + 3* Main.config.squareWidth/4};
     float yCenter = yOffset + Main.config.squareHeight/2;
     
     Ellipse2D.Float[] circles = {new Ellipse2D.Float(xCenters[0] - usedRadius, yCenter - usedRadius,
         usedRadius*2, usedRadius*2), new Ellipse2D.Float(xCenters[1] - usedRadius, yCenter - usedRadius,
             usedRadius*2, usedRadius*2)};
     
     
   txer:  for (int i = 0; i < numTransmitters; ++i) {
 
       txer = new Transmitter();
       txer.x = xOffset + rand.nextFloat()*Main.config.squareWidth;
       txer.y = yOffset + rand.nextFloat()*Main.config.squareHeight;
       
       // Skip points that lie within the circles
       for(Ellipse2D.Float circ : circles){
         if(circ.contains(txer)){
           --i;
           continue txer;
         }
       }
       
       txers.add(txer);
     }
     return txers;
   }
 
   /**
    * Computes the capture disk of transmitter t1. Uses the constant parameter
    * Beta from the global configuration.
    * 
    * @param t1
    *          the captured transmitter.
    * @param t2
    *          the uncaptured (colliding) transmitter.
    * @return the capture disk of transmitter t1, else {@code null} if none
    *         exists.
    */
   static CaptureDisk generateCaptureDisk(final Transmitter t1,
       final Transmitter t2) {
     if (t1 == t2 || t1.equals(t2)) {
       return null;
     }
     CaptureDisk captureDisk = new CaptureDisk();
     captureDisk.disk = new Circle();
     captureDisk.t1 = t1;
     captureDisk.t2 = t2;
     double betaSquared = Math.pow(Main.config.beta, 2);
     double denominator = 1 - betaSquared;
 
     double centerX = (t1.getX() - (betaSquared * t2.getX())) / denominator;
     double centerY = (t1.getY() - (betaSquared * t2.getY())) / denominator;
 
     double euclideanDistance = Math.sqrt(Math.pow(t1.getX() - t2.getX(), 2)
         + Math.pow(t1.getY() - t2.getY(), 2));
 
     /**
      * TODO: Improve the cutting based on transmit distance. This is overly
      * simplistic.
      */
     if (euclideanDistance > (2 * Main.config.maxRangeMeters)) {
       return null;
     }
 
     double radius = (Main.config.beta * euclideanDistance) / denominator;
 
     captureDisk.disk.radius = (float) radius;
     captureDisk.disk.center.x = (float) centerX;
     captureDisk.disk.center.y = (float) centerY;
 
     t1.addDisk(captureDisk);
 
     return captureDisk;
   }
 
   /**
    * Generates the intersection points of two circles, IF they intersect.
    * 
    * @param cd1
    *          the first circle.
    * @param cd2
    *          the second circle.
    * @return a {@code Collection} containing the intersection points, or
    *         {@code null} if there are no intersections.
    */
   static Collection<Point2D> generateIntersections(final CaptureDisk cd1,
       final CaptureDisk cd2) {
     // If these are the same disks, don't check their intersection
     if (cd1.equals(cd2) || cd1 == cd2) {
       return null;
     }
 
     double d = Math.sqrt(Math.pow(
         cd1.disk.getCenterX() - cd2.disk.getCenterX(), 2)
         + Math.pow(cd1.disk.getCenterY() - cd2.disk.getCenterY(), 2));
 
     double r1 = cd1.disk.radius;
     double r2 = cd2.disk.radius;
     double d1 = (Math.pow(r1, 2) - Math.pow(r2, 2) + Math.pow(d, 2)) / (2 * d);
 
     // Circles are too far apart to overlap.
     if (d > (r1 + r2)) {
       return null;
     }
 
     double h = Math.sqrt(Math.pow(r1, 2) - Math.pow(d1, 2));
 
     double x3 = cd1.disk.getCenterX()
         + (d1 * (cd2.disk.getCenterX() - cd1.disk.getCenterX())) / d;
 
     double y3 = cd1.disk.getCenterY()
         + (d1 * (cd2.disk.getCenterY() - cd1.disk.getCenterY())) / d;
 
     double x4i = x3 + (h * (cd2.disk.getCenterY() - cd1.disk.getCenterY())) / d;
     double y4i = y3 - (h * (cd2.disk.getCenterX() - cd1.disk.getCenterX())) / d;
     double x4ii = x3 - (h * (cd2.disk.getCenterY() - cd1.disk.getCenterY()))
         / d;
     double y4ii = y3 + (h * (cd2.disk.getCenterX() - cd1.disk.getCenterX()))
         / d;
 
     if (Double.isNaN(x4i) || Double.isNaN(y4i) || Double.isNaN(x4ii)
         || Double.isNaN(y4ii)) {
       return null;
     }
 
     LinkedList<Point2D> points = new LinkedList<Point2D>();
     if (x4i >= 0 && x4i <= Main.config.universeWidth && y4i >= 0
         && y4i <= Main.config.universeHeight) {
       points.add(new Point2D.Float((float) x4i, (float) y4i));
     }
     if (x4ii >= 0 && x4ii <= Main.config.universeWidth && y4ii >= 0
         && y4ii <= Main.config.universeHeight) {
       points.add(new Point2D.Float((float) x4ii, (float) y4ii));
     }
     return points;
   }
 
   public static void saveImage(final FileRenderer display, final String fileName) {
     final long start = System.currentTimeMillis();
     final File imageFile = new File(fileName + ".png");
     System.out.printf("Rendering \"%s\".\n", imageFile);
     final BufferedImage img = new BufferedImage(Main.gfxConfig.renderWidth,
         Main.gfxConfig.renderHeight,
         gfxConfig.isUseColorMode() ? BufferedImage.TYPE_INT_RGB
             : BufferedImage.TYPE_BYTE_GRAY);
     final Graphics g = img.createGraphics();
 
     display.render(g, img.getWidth(), img.getHeight());
 
     imageFile.mkdirs();
     if (!imageFile.exists()) {
       try {
         imageFile.createNewFile();
       } catch (final IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
       }
     }
     try {
       ImageIO.write(img, "png", imageFile);
       System.out.println("Saved " + imageFile.getName());
     } catch (final Exception e) {
       e.printStackTrace();
     }
     g.dispose();
     final long duration = System.currentTimeMillis() - start;
     System.out.printf("Rendering took %,dms.\n", duration);
   }
 
   /**
    * Builds a pathname for the specified "path" relative path value. Uses the
    * {@link Config#getOutputBasePath()} value
    * to prefix the path.
    * 
    * @param path
    *          the user-provided path.
    * @return a combined path using both the configured base path and the
    *         provided path.
    */
   public static String buildPath(final String path) {
     final String prefix = config.getOutputBasePath().trim();
     if (prefix.length() > 0) {
       return String.format("%s%s%s", prefix, File.separator, path);
     }
     return path;
   }
 }

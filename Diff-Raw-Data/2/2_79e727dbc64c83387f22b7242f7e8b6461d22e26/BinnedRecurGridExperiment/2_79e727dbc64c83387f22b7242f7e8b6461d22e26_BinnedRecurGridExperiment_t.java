 /*
  * Copyright (C) 2012 Robert Moore and Rutgers University
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 51
  * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 package edu.rutgers.winlab.junsim;
 
 import java.awt.Graphics;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 
 import javax.imageio.ImageIO;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A basic experimental simulation task that generates receiver positions based
  * on intersections of capture disks and tests each one to find the maximal
  * overlap.
  * 
  * @author Robert Moore
  */
 public class BinnedRecurGridExperiment implements Experiment {
 
   private static final Logger log = LoggerFactory
       .getLogger(BinnedRecurGridExperiment.class);
 
   /**
    * Configuration for this task.
    */
   final TaskConfig config;
   /**
    * Statistics to update.
    */
   final ExperimentStats stats[];
   /**
    * Name of the directory in which to save images.
    */
   String saveDirectory = null;
   /**
    * Pool of worker threads to utilize.
    */
   private final ExecutorService workers;
 
   private Binner binner;
 
   private int numBins = 50;
 
   /**
    * Number of bins used to force a "rebin".
    */
   private int rebinAt = 4;
 
   /**
    * Don't rebin if the max is below this value.
    */
   private int minRebinValue = 2;
 
   private final FileRenderer render;
 
   /**
    * Creates a new experiment task with the specific configuration, global stats
    * to update, and worker pool.
    * 
    * @param config
    *          configuration to use.
    * @param stats
    *          statistics to update at the end
    * @param workers
    *          worker threadpool to utilize.
    */
   public BinnedRecurGridExperiment(final TaskConfig config,
       final ExperimentStats[] stats, final ExecutorService workers) {
     super();
     this.workers = workers;
     this.config = config;
     this.stats = stats;
     this.saveDirectory = Main.buildPath(String.format("s%d_t%d_x%d"
         + (Main.config.stripSolutionPoints ? "_S" : ""),
         Long.valueOf(Main.config.randomSeed),
         Integer.valueOf(this.config.numTransmitters),
         Integer.valueOf(this.config.trialNumber)));
     this.render = new FileRenderer(Main.gfxConfig);
   }
 
   /**
    * Private class used to parallelize the checking of possible solution points.
    * 
    * @author Robert Moore
    */
   private static final class SolutionCheckTask implements Callable<Receiver> {
 
     private static final Logger log = LoggerFactory
         .getLogger(SolutionCheckTask.class);
 
     /**
      * Set of points this task should check.
      */
     Collection<Point2D> solutionPoints;
     /**
      * Set of capture disks to check for intersections
      */
     Collection<CaptureDisk> disks;
 
     /**
      * Reference to the bins for points.
      */
     Binner binner;
 
     int desiredBin = 0;
 
     /**
      * Creates a new solution check task.
      */
     public SolutionCheckTask() {
       super();
     }
 
     @Override
     public Receiver call() {
 
       Point2D maxPoint = null;
       Collection<CaptureDisk> maxPointDisks = null;
       int maxDisks = 0;
       final int totalDisks = this.disks.size();
       log.info(String.format(
           "Computing %,d points for %,d disks. Desired bin: %d.",
           this.solutionPoints.size(), totalDisks, desiredBin));
 
       /*
        * Determine the number of disks that intersect this point. If the number
        * is the new max, then save it. If there are no intersections, remove it.
        */
       points: for (final Iterator<Point2D> iter = this.solutionPoints
           .iterator(); iter.hasNext();) {
         final Point2D p = iter.next();
         final Collection<CaptureDisk> pDisk = new HashSet<CaptureDisk>();
 
         for (final CaptureDisk d : this.disks) {
           if (BinnedRecurGridExperiment.checkPointInDisk(p, d)) {
             pDisk.add(d);
           }
         }
         int size = pDisk.size();
 
         if (size > 0) {
           int bindex = this.binner.put(p, size);
 
           // Add to bin
           if (size > maxDisks && bindex >= this.desiredBin) {
             maxDisks = size;
             maxPoint = p;
             maxPointDisks = pDisk;
           }
         }
 
       }
       final Receiver maxReceiver = new Receiver();
       // Remove the highest point and its solution disks
       if (maxPoint != null) {
         maxReceiver.setLocation(maxPoint);
         maxReceiver.coveringDisks = maxPointDisks;
       }
       // No solutions found?
       else {
         return null;
       }
 
       return maxReceiver;
     }
   }
 
   public Boolean perform() {
     // final ExperimentRender display = new AnimatedRenderer(Main.gfxConfig);
 
     final Collection<CaptureDisk> disks = new HashSet<CaptureDisk>();
     // Compute all possible capture disks
     for (final Transmitter t1 : this.config.transmitters) {
       for (final Transmitter t2 : this.config.transmitters) {
         final CaptureDisk someDisk = Main.generateCaptureDisk(t1, t2);
         if (someDisk != null) {
           disks.add(someDisk);
         }
       }
     }
 
     if (Main.gfxConfig.generateImages) {
       this.render.setTransmitters(this.config.transmitters);
       this.render.setCaptureDisks(disks);
 
       final String saveName = this.saveDirectory + File.separator + "1000";
       Main.saveImage(this.render, saveName);
       this.render.clear();
 
     }
     log.info("[" + this.config.trialNumber + "] Generated " + disks.size()
         + " disks.");
 
     float minX = 0;
     float maxX = Main.config.universeWidth;
     float minY = 0;
     float maxY = Main.config.universeHeight;
 
     float random = Main.config.isRandomized() ? ((Main.config.universeWidth + Main.config.universeHeight) / 2) * 0.01f
         : 0f;
 
     final int totalCaptureDisks = disks.size();
     // final int startingSolutionPoints = startingPoints.size();
     int m = 0;
 
     // Keep going while there are either solution points or capture disks
     // final Collection<Receiver> receivers = new LinkedList<Receiver>();
     // Keep track of which collisions are captured so that packet loss
     // probabilities can be quickly calculated
     final ConcurrentHashMap<Transmitter, HashSet<Transmitter>> capturedCollisions = new ConcurrentHashMap<Transmitter, HashSet<Transmitter>>();
     // Add an empty set for each transmitter
     for (final Transmitter txer : this.config.transmitters) {
       capturedCollisions.put(txer, new HashSet<Transmitter>());
     }
 
     this.binner = new Binner(this.numBins, 1, disks.size() / 3);
 
     int highestBindex = 0;
     // Evaluate any receiver positions we've been given
     for (Receiver rxer : this.config.receivers) {
 
       if (m >= this.config.numReceivers) {
         break;
       }
 
       int previousMaxScore = 0;
 
       log.info("[" + this.config.trialNumber
           + "] Evaluating existing receiver position " + (m + 1) + ".");
       // ////////////////////////////////////////////////////////////////////
 
       SolutionCheckTask task = new SolutionCheckTask();
       task.solutionPoints = new LinkedList<Point2D>();
       task.solutionPoints.add(rxer);
       task.disks = disks;
       task.binner = this.binner;
      task.desiredBin = 0;
 
       try {
         final Future<Receiver> future = this.workers.submit(task);
 
         try {
           final Receiver r = future.get();
           if(r != null) {
             highestBindex = this.binner.getBindex(r.coveringDisks.size());
             rxer.coveringDisks = r.coveringDisks;
           }
 
         } catch (final ExecutionException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
         }
 
       } catch (final InterruptedException e) {
         e.printStackTrace();
       }
 
       log.info("Receiver score: {}", rxer.coveringDisks.size());
 
       // Add captures to each transmitter's capture set for collision
       // calculations
       for (final CaptureDisk disk : rxer.coveringDisks) {
         capturedCollisions.get(disk.t1).add(disk.t2);
         disk.t1.addCoveredDisk(disk);
       }
       disks.removeAll(rxer.coveringDisks);
 
       // Calculate collision rates for each transmitter
       // Store the min, max, and mean
       float mean_contention = 0.0f;
       float min_contention = this.config.numTransmitters;
       float max_contention = 0.0f;
       for (final Transmitter txer : this.config.transmitters) {
         // Calculate the number of transmitters in contention
         // Subtract 1 because this transmitter can never be in contention with
         // itself
         final int num_in_contention = this.config.numTransmitters - 1
             - capturedCollisions.get(txer).size();
         min_contention = Math.min(num_in_contention, min_contention);
         max_contention = Math.max(num_in_contention, max_contention);
         mean_contention += (float) num_in_contention
             / this.config.numTransmitters;
       }
       this.stats[m].addContention(mean_contention);
       this.stats[m].addMinContention(min_contention);
       this.stats[m].addMaxContention(max_contention);
 
       final float capturedDisks = totalCaptureDisks - disks.size();
       final float captureRatio = (capturedDisks / totalCaptureDisks);
       // Debugging stuff
       if (Main.gfxConfig.generateImages) {
         this.render.setTransmitters(this.config.transmitters);
         this.render.setRankedSolutionPoints(this.binner.getBins(),
             this.binner.getBinMins());
 
         // display.setSolutionPoints(this.binner.getMaxBin());
         this.render.setCaptureDisks(disks);
         this.render.setReceiverPoints(this.config.receivers);
 
         final String saveName = String.format(this.saveDirectory
             + File.separator + "1%03d", (m + 1));
         Main.saveImage(this.render, saveName);
         this.render.clear();
 
       }
 
       this.stats[m].addCoverage(captureRatio);
       ++m;
     }
 
     // Finding new positions
     Collection<Point2D> startingPoints = BinnedRecurGridExperiment
         .generateSolutionPoints(minX, maxX, minY, maxY, random,
             this.config.transmitters);
     log.info(String.format("[%d] Generated %,d solution points.\n",
         this.config.trialNumber, startingPoints.size()));
 
     this.binner.set(startingPoints, 1);
 
     receiverLoop: while (m < this.config.numReceivers && !disks.isEmpty()) {
 
       log.info("[" + this.config.trialNumber
           + "] Calculating position for receiver " + (m + 1) + ".");
       Receiver maxReceiver = null;
       int previousMaxScore = 0;
       HashSet<Point2D> alreadyChecked = new HashSet<Point2D>();
       HashSet<Point2D> maxChecked = new HashSet<Point2D>();
       recursiveLoop: do {
         this.binner.printBins();
 
         if (maxReceiver != null) {
           previousMaxScore = maxReceiver.coveringDisks.size();
         }
         log.info("Using bin {}", this.binner.getMaxBindex());
         Set<Point2D> possiblePoints = this.binner.getMaxBin();
         if (possiblePoints == null) {
           log.info("No more points available in the bins.");
           break;
         }
         Set<Point2D> thePoints = new HashSet<Point2D>();
         for (Iterator<Point2D> iter = possiblePoints.iterator(); iter.hasNext();) {
           Point2D pnt = iter.next();
           iter.remove();
           if (alreadyChecked.contains(pnt)) {
             maxChecked.add(pnt);
           } else {
             thePoints.add(pnt);
           }
 
         }
 
         final int numTasks = Main.config.numThreads;
         final int numPoints = thePoints.size();
         final int pointsPerTask = (numPoints / numTasks) + 1;
         final long numComparisons = disks.size() * (long) numPoints;
 
         final Collection<SolutionCheckTask> tasks = new LinkedList<BinnedRecurGridExperiment.SolutionCheckTask>();
         final Iterator<Point2D> pointIter = thePoints.iterator();
         SolutionCheckTask task = new SolutionCheckTask();
         task.solutionPoints = new LinkedList<Point2D>();
         task.disks = disks;
         task.desiredBin = highestBindex;
         task.binner = this.binner;
         // task.parent = this;
         tasks.add(task);
         for (int i = 0; pointIter.hasNext(); ++i) {
           Point2D pnt = pointIter.next();
           task.solutionPoints.add(pnt);
           alreadyChecked.add(pnt);
           pointIter.remove();
           if (i == pointsPerTask) {
             i = 0;
             task = new SolutionCheckTask();
             task.solutionPoints = new LinkedList<Point2D>();
             task.disks = disks;
             task.binner = this.binner;
             task.desiredBin = highestBindex;
             // task.parent = this;
             tasks.add(task);
           }
         }
         int sumTasks = 0;
         for (final SolutionCheckTask t : tasks) {
           log.info(String.format("Task (%,d)", t.solutionPoints.size()));
           sumTasks += t.solutionPoints.size();
         }
 
         log.info(String
             .format("Divided %,d/%,d points.\n", sumTasks, numPoints));
         final long start = System.currentTimeMillis();
 
         try {
           final List<Future<Receiver>> solutions = this.workers
               .invokeAll(tasks);
 
           for (final Future<Receiver> future : solutions) {
             if (future.isCancelled() || !future.isDone()) {
               log.error("One of the tasks was cancelled! Double-check the code!");
               return Boolean.FALSE;
             }
             try {
               final Receiver r = future.get();
               if (r == null) {
                 log.warn("No receiver returned.");
                 continue;
               }
               if (maxReceiver == null
                   || r.coveringDisks.size() > maxReceiver.coveringDisks.size()) {
                 highestBindex = this.binner.getBindex(r.coveringDisks.size());
                 maxReceiver = r;
               }
             } catch (final ExecutionException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
             }
           }
 
         } catch (final InterruptedException e) {
           e.printStackTrace();
         }
         final long duration = System.currentTimeMillis() - start;
         log.info(String.format("Computed %,d comparisons in %,dms.\n",
             numComparisons, duration));
 
         if (maxReceiver == null) {
 
           if (highestBindex == 0) {
             break receiverLoop;
           }
 
           highestBindex = this.binner.getMaxBindex();
           alreadyChecked.clear();
           maxChecked.clear();
           continue recursiveLoop;
         }
 
         alreadyChecked.remove(maxReceiver);
 
         log.info("Max receiver score: {}", maxReceiver.coveringDisks.size());
 
         if (highestBindex == 0) {
           int max = maxReceiver.coveringDisks.size();
           if (max > this.minRebinValue) {
             this.binner.rebin(1, max / 2);
           }
         }
 
         if (previousMaxScore == 0
             || maxReceiver.coveringDisks.size() != previousMaxScore) {
           // Try again, this time digging deeper around the maximum area.
           float width = (maxX - minX) / 4;
           float height = (maxY - minY) / 4;
           minX = (float) maxReceiver.getX() - width;
           maxX = (float) maxReceiver.getX() + width;
           minY = (float) maxReceiver.getY() - height;
           maxY = (float) maxReceiver.getY() + height;
           highestBindex = this.binner.getBindex(maxReceiver.coveringDisks
               .size());
           random = Main.config.isRandomized() ? ((width + height) / 2) * 0.01f
               : 0f;
           this.binner.putAll(this.generateSolutionPoints(minX, maxX, minY,
               maxY, random, this.config.transmitters),
               maxReceiver.coveringDisks.size());
         }
 
       } while (previousMaxScore == 0
           || maxReceiver.coveringDisks.size() != previousMaxScore);
 
       log.info(String.format("Adding %,d max points, instead of %,d total.",
           maxChecked.size(), alreadyChecked.size()));
 
       this.binner.putAll(maxChecked, maxReceiver.coveringDisks.size());
 
       minX = 0;
       maxX = Main.config.universeWidth;
       minY = 0;
       maxY = Main.config.universeHeight;
 
       // Add the newest receiver and remove newly covered points and disks
       this.config.receivers.add(maxReceiver);
 
       // Add captures to each transmitter's capture set for collision
       // calculations
       for (final CaptureDisk disk : maxReceiver.coveringDisks) {
         capturedCollisions.get(disk.t1).add(disk.t2);
         disk.t1.addCoveredDisk(disk);
       }
       disks.removeAll(maxReceiver.coveringDisks);
 
       // Calculate collision rates for each transmitter
       // Store the min, max, and mean
       float mean_contention = 0.0f;
       float min_contention = this.config.numTransmitters;
       float max_contention = 0.0f;
       for (final Transmitter txer : this.config.transmitters) {
         // Calculate the number of transmitters in contention
         // Subtract 1 because this transmitter can never be in contention with
         // itself
         final int num_in_contention = this.config.numTransmitters - 1
             - capturedCollisions.get(txer).size();
         min_contention = Math.min(num_in_contention, min_contention);
         max_contention = Math.max(num_in_contention, max_contention);
         mean_contention += (float) num_in_contention
             / this.config.numTransmitters;
       }
       this.stats[m].addContention(mean_contention);
       this.stats[m].addMinContention(min_contention);
       this.stats[m].addMaxContention(max_contention);
 
       final float capturedDisks = totalCaptureDisks - disks.size();
       final float captureRatio = (capturedDisks / totalCaptureDisks);
       // Debugging stuff
       if (Main.gfxConfig.generateImages) {
         this.render.setTransmitters(this.config.transmitters);
         this.render.setRankedSolutionPoints(this.binner.getBins(),
             this.binner.getBinMins());
 
         // display.setSolutionPoints(this.binner.getMaxBin());
         this.render.setCaptureDisks(disks);
         this.render.setReceiverPoints(this.config.receivers);
 
         final String saveName = String.format(this.saveDirectory
             + File.separator + "1%03d", (m + 1));
         Main.saveImage(this.render, saveName);
         this.render.clear();
 
       }
 
       this.stats[m].addCoverage(captureRatio);
       ++m;
 
     } // End for each receiver
 
     // }
     disks.clear();
     startingPoints.clear();
     this.binner.clear();
     this.config.transmitters.clear();
     Runtime.getRuntime().gc();
     return Boolean.TRUE;
   }
 
   static boolean checkPointInDisk(final Point2D p, final CaptureDisk d) {
     final double dist1 = Math.sqrt(Math.pow(p.getX() - d.t1.getX(), 2)
         + Math.pow(p.getY() - d.t1.getY(), 2));
     final double dist2 = Math.sqrt(Math.pow(p.getX() - d.t2.getX(), 2)
         + Math.pow(p.getY() - d.t2.getY(), 2));
     // This point is too far away from the transmitters for this disk
     if (dist1 > Main.config.maxRangeMeters
         && dist2 > Main.config.maxRangeMeters) {
       return false;
     }
     return d.disk.contains(p);
 
   }
 
   private static Collection<Point2D> generateSolutionPoints(final float minX,
       final float maxX, final float minY, final float maxY, final float random,
       final Collection<Transmitter> transmitters) {
 
     log.info(String
         .format(
             "Generating [(%,.2f, %,.2f)x(%,.2f, %,.2f)] dense: %,.1f, random: %,.2f",
             minX, maxX, minY, maxY, Main.config.getGridDensity(), random));
 
     final Collection<Point2D> solutionPoints = new HashSet<Point2D>();
     float density = Main.config.getGridDensity();
     float xStep = (maxX - minX) / density;
     float yStep = (maxY - minY) / density;
 
     // Build a grid assuming some density of points per unit-square
 
     for (float xIndex = minX; xIndex <= maxX; xIndex += xStep) {
       for (float yIndex = minY; yIndex <= maxY; yIndex += yStep) {
         final float x = xIndex
             + (float) (Main.rand.nextBoolean() ? (Main.rand.nextDouble() * random)
                 : (-Main.rand.nextDouble() * random));
         if (x < 0 || x > Main.config.universeWidth) {
           continue;
         }
         final float y = yIndex
             + (float) (Main.rand.nextBoolean() ? (Main.rand.nextDouble() * random)
                 : (-Main.rand.nextDouble() * random));
         if (y < 0 || y > Main.config.universeHeight) {
           continue;
         }
         Point2D.Float pnt = new Point2D.Float(x, y);
         if (BinnedRecurGridExperiment.checkPointInRange(pnt, transmitters)) {
           solutionPoints.add(pnt);
         }
       }
 
     }
 
     return solutionPoints;
   }
 
   /**
    * Returns true if a point is within the transmit radius of at least one
    * transmitter, else false.
    * 
    * @param p
    *          the point to test.
    * @return {@code true} if the point is within the transmit radius of at
    *         leaset one transmitter.
    */
   private static boolean checkPointInRange(final Point2D p,
       final Collection<Transmitter> transmitters) {
     for (final Transmitter t : transmitters) {
       final double d = Math.sqrt(Math.pow(p.getX() - t.getX(), 2)
           + Math.pow(p.getY() - t.getY(), 2));
       if (d < Main.config.maxRangeMeters) {
         return true;
       }
     }
     return false;
   }
 
 }

 /*
  * Copyright (C) 2005-2007 Institute for Computational Biomedicine,
  *                         Weill Medical College of Cornell University
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package edu.cornell.med.icb.clustering;
 
 import edu.rit.pj.IntegerForLoop;
 import edu.rit.pj.ParallelRegion;
 import edu.rit.pj.ParallelTeam;
 import it.unimi.dsi.fastutil.ints.IntArrayList;
 import it.unimi.dsi.mg4j.util.ProgressLogger;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.log4j.Logger;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Implements the QT Clustering algorithm. (QT stands for Quality
  * Threshold/diameter of the cluster).
  * See <a href="http://en.wikipedia.org/wiki/Data_clustering#Types_of_clustering">Wikipedia</a>
  * and <a href="http://www.genome.org/cgi/content/full/9/11/1106">Heyer LJ et al 1999</a>.
  * <p/>
  * Note that this implementation does not reference the actual instances of the
  * data being clustered directly.  It assumes the data is held in an external array or
  * indexed collection.  It relies on the specific implementation of
  * {@link edu.cornell.med.icb.clustering.SimilarityDistanceCalculator}
  * to map the indices back to the original data source.
  * <p/>
  * For example, if you wanted to group the text of Abraham Lincoln's Gettysburg Address
  * into clusters of words of the same size, the following code snippet would achieve this.
  *
  * <pre>
  *    final String text = "Four score and seven years ago our fathers brought forth on this"
  *               + " continent a new nation conceived in liberty and dedicated to the proposition"
  *               + " that all men are created equal";
  *    // break the text up into an array of indiviual words
  *    final String[] words = text.split(" ");
  *    // create a distance calculator that returns the difference in size between the two words
  *    final SimilarityDistanceCalculator distanceCalculator =
  *               new MaxLinkageDistanceCalculator() {
  *                   public double distance(final int i, final int j) {
  *                       return Math.abs(words[i].length() - words[j].length());
  *                   }
  *               };
  *
  *    // and cluster the words into groups according to their size
  *    final Clusterer clusterer = new QTClusterer(words.length);
 *    final List&lt;int[]&gt; clusters = clusterer.cluster(distanceCalculator, 0);
  * </pre>
  *
  * The cluster arrays that result are:
  * <pre>
  *   { 2, 27, 26, 25, 22, 19, 14, 6, 5 },
  *   { 1, 29, 9, 4, 3 },
  *   { 7, 28, 18, 8 },
  *   { 0, 24, 11 },
  *   { 10, 21, 17 },
  *   { 12, 20, 16 },
  *   { 13 },
  *   { 15 },
  *   { 23 }
  * </pre>
  *
  * The cluster arrays are indexes into the original data source and will map back into the
  * text as follows:
  * <pre>
  *   { "and", "are", "men", "all", "the", "and", "new", "our", "ago" },
  *   { "score", "equal", "forth", "years", "seven" },
  *   { "fathers", "created", "liberty", "brought" },
  *   { "Four", "that", "this" },
  *   { "on", "to", "in" },
  *   { "continent", "dedicated", "conceived" },
  *   { "a" },
  *   { "nation" },
  *   { "proposition" }
  * </pre>
  *
  * Changing the threshold given to the {@link #cluster(SimilarityDistanceCalculator, double)}
  * method will result in different groupings of words.
  */
 public final class QTClusterer implements Clusterer {
     /**
      * Used to log debug and informational messages.
      */
     private static final Logger LOGGER = Logger.getLogger(QTClusterer.class);
 
     /**
      * The time interval for a new log in milliseconds.
      *
      * @see it.unimi.dsi.mg4j.util.ProgressLogger#DEFAULT_LOG_INTERVAL
      */
     private long logInterval = ProgressLogger.DEFAULT_LOG_INTERVAL;
 
     /**
      * The total number of instances to cluster.
      */
     private final int instanceCount;
 
     /**
      * The total number of clusters created by clustering the instances.
      */
     private int clusterCount;
 
     /**
      * The list of clusters.
      */
     private final IntArrayList[] clusters;
 
     /**
      * A "temporary" cluster list used to find the maximum cardinality.
      */
     private final IntArrayList[] candidateClusters;
 
     /**
      * Indicates that progress on cluster being assembled.
      */
     private boolean logClusterProgress = true;
 
     /**
      * Indicates that progress on the outer loop should be logged.
      */
     private boolean logOuterLoopProgress = true;
 
     /**
      * Indicates that progress on the inner loop should be logged.
      */
     private boolean logInnerLoopProgress;
 
     /**
      * Team used to execute the clustering inner loops with multiple threads.
      */
     private final ParallelTeam parallelTeam;
 
     /**
      * Construct a new quality threshold clusterer.
      *
      * @param numberOfInstances The number of instances to cluster.
      *                          i.e., |G| where G is the set of instances
      */
     public QTClusterer(final int numberOfInstances) {
         // create at least 1 thread, but not more than the number of instances
         this(numberOfInstances, Math.max(Math.min(
                 ParallelTeam.getDefaultThreadCount(), numberOfInstances), 1));
     }
 
     /**
      * Construct a new quality threshold clusterer.
      *
      * @param numberOfInstances The number of instances to cluster.
      *                          i.e., |G| where G is the set of instances
      * @param numberOfThreads   Number of threads to use when clustering.
      */
     public QTClusterer(final int numberOfInstances,
                        final int numberOfThreads) {
         this(numberOfInstances, new ParallelTeam(numberOfThreads));
     }
 
     /**
      * Construct a new quality threshold clusterer.
      *
      * @param numberOfInstances The number of instances to cluster.
      *                          i.e., |G| where G is the set of instances
      * @param team              The parallel team to use when clustering.
      */
     public QTClusterer(final int numberOfInstances,
                        final ParallelTeam team) {
         super();
         if (numberOfInstances < 0) {
             throw new IllegalArgumentException("Number of instances ("
                     + numberOfInstances + ") must not be negative");
         }
 
         instanceCount = numberOfInstances;
         parallelTeam = team;
         clusters = new IntArrayList[numberOfInstances];
         candidateClusters = new IntArrayList[numberOfInstances];
         for (int i = 0; i < numberOfInstances; i++) {
             clusters[i] = new IntArrayList();                        // NOPMD
             candidateClusters[i] = new IntArrayList();               // NOPMD
         }
     }
 
     /**
      * Groups instances into clusters. Returns the indices of the instances
      * that belong to a cluster as an int array in the list result.
      *
      * @param calculator       The
      *                         {@link edu.cornell.med.icb.clustering.SimilarityDistanceCalculator}
      *                         that should be used when clustering
      * @param qualityThreshold The QT clustering algorithm quality threshold (d)
      * @return The list of clusters.
      */
     public List<int[]> cluster(
             final SimilarityDistanceCalculator calculator,
             final double qualityThreshold) {
         final ProgressLogger clusterProgressLogger =
                 new ProgressLogger(LOGGER, logInterval, "instances clustered");
         clusterProgressLogger.displayFreeMemory = true;
         clusterProgressLogger.expectedUpdates = instanceCount;
         clusterProgressLogger.start("Starting to cluster " + instanceCount
                 + " instances using " + parallelTeam.getThreadCount() + " threads.");
 
         // reset cluster results
         clusterCount = 0;
         // instanceList is the set "G" to cluster
         final LinkedList<Integer> instanceList = new LinkedList<Integer>();
         for (int i = 0; i < instanceCount; i++) {
             clusters[i].clear();
 
             // set each node in the instance list to it's
             // original position in the source data array
             instanceList.add(i);
         }
 
         final ProgressLogger innerLoopProgressLogger =
                 new ProgressLogger(LOGGER, logInterval, "inner loop iterations");
         innerLoopProgressLogger.displayFreeMemory = false;
 
         final ProgressLogger outerLoopProgressLogger =
                 new ProgressLogger(LOGGER, logInterval, "outer loop iterations");
         outerLoopProgressLogger.displayFreeMemory = false;
 
         try {
             // loop over instances until they have all been added to a cluster
             while (!instanceList.isEmpty()) {
                 // cluster remaining instances to find the maximum cardinality
                 for (int i = 0; i < instanceCount; i++) {
                     candidateClusters[i].clear();
                 }
 
                 if (logOuterLoopProgress) {
                     outerLoopProgressLogger.expectedUpdates = instanceList.size();
                     outerLoopProgressLogger.start("Entering outer loop for "
                             + instanceList.size() + " iterations");
                 }
 
                 // foreach i in G (instance list)
                 // find instance j such that distance i,j minimum
                 parallelTeam.execute(new ParallelRegion() {          // NOPMD
 
                     @Override
                     public void run() throws Exception {             // NOPMD
                         // each thread will populate a different portion of the "candidateCluster"
                         // array so we shouldn't need to worry about concurrent access
                         execute(0, instanceList.size() - 1, new IntegerForLoop() {
                             @Override
                             public void run(final int first, final int last) {
                                 if (LOGGER.isDebugEnabled()) {
                                     LOGGER.debug("first = " + first + ", last = " + last);
                                 }
                                 for (int i = first; i <= last; i++) {
                                     @SuppressWarnings("unchecked")
                                     final LinkedList<Integer> notClustered =
                                             (LinkedList<Integer>) instanceList.clone();
 
                                     // add the first instance to the next candidate cluster
                                     final IntArrayList candidateCluster = candidateClusters[i];
                                     candidateCluster.add(notClustered.remove(i));
 
                                     if (logInnerLoopProgress) {
                                         innerLoopProgressLogger.expectedUpdates =
                                                 notClustered.size();
                                         innerLoopProgressLogger.start("Entering inner loop for "
                                                 + notClustered.size() + " iterations");
                                     }
 
                                     // cluster the remaining instances to find the maximum
                                     // cardinality find instance j such that distance i,j minimum
                                     boolean done = false;
                                     while (!done && !notClustered.isEmpty()) {
                                         // find the node that has minimum distance between the
                                         // current cluster and the instances that have not yet
                                         // been clustered
                                         double minDistance = Double.MAX_VALUE;
                                         int minDistanceInstanceIndex = 0;
                                         int instanceIndex = 0;
                                         for (final int instance : notClustered) {
                                             final double newDistance =
                                                     calculator.distance(candidateCluster, instance);
 
                                            if (newDistance != calculator.getIgnoreDistance()
                                                    && newDistance <= minDistance) {
 
                                                 minDistance = newDistance;
                                                 minDistanceInstanceIndex = instanceIndex;
                                             }
                                             instanceIndex++;
                                         }
                                         // grow clusters until min distance between new instance
                                         // and cluster reaches quality threshold
                                         // if (diameter(Ai U {j}) > d)
                                         if (minDistance > qualityThreshold) {
                                             done = true;
                                         } else {
                                             // remove the instance from the ones to be considered
                                             final int instance =
                                                     notClustered.remove(minDistanceInstanceIndex);
                                             // and add it to the newly formed cluster
                                             candidateCluster.add(instance);
                                         }
                                         if (logInnerLoopProgress) {
                                             innerLoopProgressLogger.update();
                                         }
                                     }
                                     if (logInnerLoopProgress) {
                                         innerLoopProgressLogger.stop("Inner loop completed.");
                                     }
                                     if (logOuterLoopProgress) {
                                         outerLoopProgressLogger.update();
                                     }
                                 }
                             }
                         });
                     }
                 });
 
                 if (logOuterLoopProgress) {
                     outerLoopProgressLogger.stop("Outer loop completed.");
                 }
 
                 // identify cluster (set C) with maximum cardinality
                 int maxCardinality = 0;
                 int selectedClusterIndex = -1;
                 for (int i = 0; i < instanceCount; i++) {
                     final int size = candidateClusters[i].size();
                     if (LOGGER.isTraceEnabled() && size > 0) {
                         LOGGER.trace("potential cluster " + i + ": "
                                 + ArrayUtils.toString(candidateClusters[i]));
                     }
                     if (size > maxCardinality) {
                         maxCardinality = size;
                         selectedClusterIndex = i;
                     }
                 }
 
                 final IntArrayList selectedCluster = candidateClusters[selectedClusterIndex];
 
                 if (LOGGER.isTraceEnabled()) {
                     LOGGER.trace("adding " + selectedCluster.size()
                             + " instances to cluster " + clusterCount);
                 }
                 // and add that cluster to the final result
                 clusters[clusterCount].addAll(selectedCluster);
 
                 // remove instances in cluster C so they are no longer considered
                 instanceList.removeAll(selectedCluster);
 
                 if (logClusterProgress) {
                     final int selectedClusterSize = selectedCluster.size();
                     int i = 0;
                     while (i < selectedClusterSize - 1) {
                         clusterProgressLogger.lightUpdate();
                         i++;
                     }
                     // make sure there is at least one "full" update per loop
                     if (i < selectedClusterSize) {
                         clusterProgressLogger.update();
                     }
                 }
 
                 // we just created a new cluster
                 clusterCount++;
 
                 // next iteration is over (G - C)
             }
         } catch (Exception e) {
             if (e instanceof RuntimeException) {
                 throw (RuntimeException) e;
             } else {
                 throw new ClusteringException(e);
             }
         }
 
         clusterProgressLogger.stop("Clustering completed.");
         return getClusters();
     }
 
 
     /**
      * Returns the list of clusters produced by clustering.
      *
      * @return A list of integer arrays, where each array represents a cluster
      *         and contains the index of the instance that belongs to a given
      *         cluster.
      */
     public List<int[]> getClusters() {
         final List<int[]> result = new ArrayList<int[]>(clusterCount);
         for (int i = 0; i < clusterCount; i++) {
             result.add(clusters[i].toIntArray());
         }
         return result;
     }
 
     /**
      * Is progress on clusters being logged?
      *
      * @return true if logging is enabled for clusters being built.
      */
     public boolean isLogClusterProgress() {
         return logClusterProgress;
     }
 
     /**
      * Should progress on the clusters be logged?
      *
      * @param value indicates whether or not logging should be enabled for
      *              clusters being built.
      */
     public void setLogClusterProgress(final boolean value) {
         this.logClusterProgress = value;
     }
 
     /**
      * Is progress on the inner loop being logged?
      *
      * @return true if logging is enabled for the inner loop
      */
     public boolean isLogInnerLoopProgress() {
         return logInnerLoopProgress;
     }
 
     /**
      * Should progress on the inner loop be logged?
      *
      * @param value indicates whether or not logging should be enabled for
      *              the inner loop
      */
     public void setLogInnerLoopProgress(final boolean value) {
         this.logInnerLoopProgress = value;
     }
 
     /**
      * Is progress on the outer loop being logged?
      *
      * @return true if logging is enabled for the outer loop
      */
     public boolean isLogOuterLoopProgress() {
         return logOuterLoopProgress;
     }
 
     /**
      * Should progress on the outer loop be logged?
      *
      * @param value indicates whether or not logging should be enabled for
      *              the outer loop
      */
     public void setLogOuterLoopProgress(final boolean value) {
         this.logOuterLoopProgress = value;
     }
 
     /**
      * Get the the progress logging interval.
      *
      * @return the logging interval in milliseconds.
      */
     public long getLogInterval() {
         return logInterval;
     }
 
     /**
      * Set the the progress logging interval.
      *
      * @param interval the logging interval in milliseconds.
      */
     public void setLogInterval(final long interval) {
         this.logInterval = interval;
     }
 }

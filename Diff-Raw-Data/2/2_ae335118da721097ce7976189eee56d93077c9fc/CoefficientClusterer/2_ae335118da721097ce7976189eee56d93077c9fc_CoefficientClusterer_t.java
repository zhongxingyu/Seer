 package com.spartango.hicgraph.analysis.cluster;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import com.spartango.hicgraph.model.ChromatinGraph;
 import com.spartango.hicgraph.model.ChromatinLocation;
 
 import edu.uci.ics.jung.algorithms.filters.FilterUtils;
 import edu.uci.ics.jung.algorithms.metrics.Metrics;
 
 public class CoefficientClusterer implements Clusterer, Runnable {
 
     private double          threshold;
     private ChromatinGraph  source;
 
     private ClusterConsumer consumer;
 
     private boolean         running;
     private Thread          clusterThread;
 
     public CoefficientClusterer(double threshold) {
         this.threshold = threshold;
         consumer = null;
         source = null;
         running = false;
     }
 
     @Override
     public void onGraphBuilt(ChromatinGraph graph) {
         // Single entity in pipeline
         if (!running) {
 
             // Got a graph to cluster
             source = graph;
 
             // Spin up a thread to cluster
             clusterThread = new Thread(this);
             clusterThread.start();
         }
     }
 
     @Override
     public void addConsumer(ClusterConsumer c) {
         consumer = c;
     }
 
     @Override
     public void removeConsumer(ClusterConsumer c) {
         consumer = null;
     }
 
     @Override
     public void run() {
         if (source != null) {
             running = true;
             System.out.println("Clusterer Started: " + source.getVertexCount()
                                + " nodes & " + source.getEdgeCount()
                                + " edges. on "
                                + Thread.currentThread().getName());
 
             HashSet<ChromatinLocation> clustered = new HashSet<ChromatinLocation>();
             BlockingQueue<ChromatinGraph> clusterQueue = new LinkedBlockingQueue<ChromatinGraph>();
 
             // Notify start to consumers
             notifyStart(clusterQueue, source);
 
             // Calculate the clustering coefficients
             Map<ChromatinLocation, Double> clusteringCoefficients = Metrics.clusteringCoefficients(source);
 
             // For each node
             for (ChromatinLocation headLocation : clusteringCoefficients.keySet()) {
                 if (clusteringCoefficients.get(headLocation) >= threshold
                     && !clustered.contains(headLocation)) {
 
                     // Grab its neighbors
                     HashSet<ChromatinLocation> cluster = new HashSet<ChromatinLocation>();
                     cluster.add(headLocation);
                    cluster.addAll(source.getNeighbors(headLocation));
 
                     // Create the induced Graph
                     ChromatinGraph induced = FilterUtils.createInducedSubgraph(cluster,
                                                                                source);
 
                     // TODO: Not sure if this makes the most sense
                     clustered.addAll(clustered);
 
                     // Hand off the cluster
                     try {
                         clusterQueue.put(induced);
                         notifyNewCluster(induced);
                     } catch (InterruptedException e) {
                         System.err.println("Interrupted while adding cluster to out queue");
                         break;
                     }
                 }
 
             }
 
             // Notify Finished
             System.out.println("Clusterer Finished");
             notifyFinished();
             running = false;
         }
     }
 
     private void notifyFinished() {
         if (consumer != null)
             consumer.onClusteringComplete();
     }
 
     private void notifyNewCluster(ChromatinGraph induced) {
         if (consumer != null) {
             consumer.onClusterFound(induced);
         }
 
     }
 
     private void notifyStart(BlockingQueue<ChromatinGraph> clusterQueue,
                              ChromatinGraph source2) {
         if (consumer != null) {
             consumer.onClusteringStarted(source2, clusterQueue);
         }
     }
 }

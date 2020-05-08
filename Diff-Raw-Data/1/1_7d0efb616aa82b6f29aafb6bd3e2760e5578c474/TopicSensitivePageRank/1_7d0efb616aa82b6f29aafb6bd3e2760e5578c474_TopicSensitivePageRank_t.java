 package com.thomasdimson.wikipedia.lda.java;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Predicate;
 import com.google.common.base.Splitter;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Ordering;
 import com.thomasdimson.wikipedia.Data;
 
 import java.io.*;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 public class TopicSensitivePageRank {
     public static double BETA = 0.85;
 
     private static double chiSquareScore(int index, Data.TSPRGraphNode node) {
         double observed = node.getTspr(index);
         double expected = node.getTspr(node.getTsprCount() - 1);
         return Math.pow(observed - expected, 2) / expected;
     }
 
     private static double massScore(int index, Data.TSPRGraphNode node) {
         return node.getTspr(index) / node.getTspr(node.getTsprCount() - 1);
     }
 
     private static Ordering<Data.TSPRGraphNode> byLDA(final int index) {
         return new Ordering<Data.TSPRGraphNode>() {
             @Override
             public int compare(Data.TSPRGraphNode tsprGraphNode, Data.TSPRGraphNode tsprGraphNode2) {
                 return Double.compare(tsprGraphNode.getLda(index), tsprGraphNode2.getLda(index));
             }
         };
     }
 
     private static Ordering<Data.TSPRGraphNode> byTSPR(final int index) {
         return new Ordering<Data.TSPRGraphNode>() {
             @Override
             public int compare(Data.TSPRGraphNode tsprGraphNode, Data.TSPRGraphNode tsprGraphNode2) {
                 return Double.compare(tsprGraphNode.getTspr(index), tsprGraphNode2.getTspr(index));
             }
         };
     }
 
     private static Ordering<Data.TSPRGraphNode> byChiSquaredTSPR(final int index) {
         return new Ordering<Data.TSPRGraphNode>() {
             @Override
             public int compare(Data.TSPRGraphNode tsprGraphNode, Data.TSPRGraphNode tsprGraphNode2) {
                 return Double.compare(chiSquareScore(index, tsprGraphNode), chiSquareScore(index, tsprGraphNode2));
             }
         };
     }
 
     private static Ordering<Data.TSPRGraphNode> byMassTSPR(final int index) {
         return new Ordering<Data.TSPRGraphNode>() {
             @Override
             public int compare(Data.TSPRGraphNode tsprGraphNode, Data.TSPRGraphNode tsprGraphNode2) {
                 return Double.compare(massScore(index, tsprGraphNode), massScore(index, tsprGraphNode2));
             }
         };
     }
 
     public static List<Data.TSPRGraphNode> topBy(Ordering<Data.TSPRGraphNode> ordering,
                                                  Iterator<Data.TSPRGraphNode> nodes, int k,
                                                  final String infoboxMatch) {
         final boolean matchExclusive = infoboxMatch != null && infoboxMatch.startsWith("^");
         final String match;
         if(matchExclusive) {
             match = infoboxMatch.substring(1);
         } else {
             match = infoboxMatch;
         }
 
         return ordering.greatestOf(Iterators.filter(nodes, new Predicate<Data.TSPRGraphNode>() {
             @Override
             public boolean apply(Data.TSPRGraphNode tsprGraphNode) {
                 if(match == null) {
                     return true;
                 } else if(matchExclusive) {
                     return !match.equalsIgnoreCase(tsprGraphNode.getInfoboxType());
                 } else {
                     return match.equalsIgnoreCase(tsprGraphNode.getInfoboxType());
                 }
             }
         }), k);
 
     }
 
     public static List<Data.TSPRGraphNode> topKLDA(Iterator<Data.TSPRGraphNode> nodes, int index, int k,
                                                    final String infoboxMatch) {
         return topBy(byLDA(index), nodes, k, infoboxMatch);
     }
 
     public static List<Data.TSPRGraphNode> topKTSPR(Iterator<Data.TSPRGraphNode> nodes, int index, int k,
                                                     final String infoboxMatch) {
         return topBy(byTSPR(index), nodes, k, infoboxMatch);
     }
 
     public static List<Data.TSPRGraphNode> topKChiSquareTSPR(Iterator<Data.TSPRGraphNode> nodes, int index, int k,
                                                     final String infoboxMatch) {
         return topBy(byChiSquaredTSPR(index), nodes, k, infoboxMatch);
     }
 
     public static List<Data.TSPRGraphNode> topKMassTSPR(Iterator<Data.TSPRGraphNode> nodes, int index, int k,
                                                              final String infoboxMatch) {
         return topBy(byMassTSPR(index), nodes, k, infoboxMatch);
     }
 
     public static Iterator<Data.TSPRGraphNode> newTSPRGraphNodeIterator(String filename) throws IOException {
         final InputStream inputStream = new BufferedInputStream(new FileInputStream(filename));
 
         try {
             return new Iterator<Data.TSPRGraphNode>() {
                 Data.TSPRGraphNode nextMessage = Data.TSPRGraphNode.parseDelimitedFrom(inputStream);
 
                 @Override
                 public boolean hasNext() {
                     return nextMessage != null;
                 }
 
                 @Override
                 public Data.TSPRGraphNode next() {
                     Data.TSPRGraphNode ret = nextMessage;
                     try {
                         nextMessage = Data.TSPRGraphNode.parseDelimitedFrom(inputStream);
                     } catch (IOException e) {
                         throw new RuntimeException(e);
                     }
                     return ret;
                 }
 
                 @Override
                 public void remove() {
                     throw new UnsupportedOperationException();
                 }
             };
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     public static Map<String, double[]> readLDAMap(String filename) throws IOException {
         BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(filename), Charset.forName("UTF-8")));
         String line;
         Splitter splitter = Splitter.on("\t").omitEmptyStrings().trimResults();
         Map<String, double[]> ret = Maps.newHashMapWithExpectedSize(1000000);
         int lastTopicLength = -1;
         int lineNum = 0;
         System.out.println();
         while((line = r.readLine()) != null) {
             if(line.startsWith("#"))  {
                 continue;
             }
 
             List<String> split = splitter.splitToList(line);
             String title = split.get(1);
             double[] topics = new double[(split.size() - 2) / 2 + 1];
             topics[topics.length - 1] = 1.0;
             if(lastTopicLength == -1) {
                 lastTopicLength = topics.length;
             } else if(lastTopicLength != topics.length) {
                 throw new RuntimeException("Bad topics length for " + line);
             }
 
             int nextTopicId = -1;
             for(int i = 2; i < split.size(); i++) {
                 if(i % 2 == 0) {
                     nextTopicId = Integer.parseInt(split.get(i));
                 } else {
                     topics[nextTopicId] = Double.parseDouble(split.get(i));
                 }
             }
             ret.put(title, topics);
             lineNum++;
             if(lineNum % 10000 == 0) {
                 System.out.print("Reached line " + lineNum + "      \r");
                 System.out.flush();
             }
         }
         return ret;
     }
 
     public static class RankInPlaceRunnable implements Runnable {
         final double sum;
         final int topicNum;
         final List<IntermediateTSPRNode> nodes;
         final Map<Long, IntermediateTSPRNode> nodeById;
         final int numNodes;
         final double convergence;
         public RankInPlaceRunnable(Map<Long, IntermediateTSPRNode> nodeById,
                                    List<IntermediateTSPRNode> nodes, double sum, int topicNum,
                                    double convergence) {
             this.nodeById = nodeById;
             this.nodes = nodes;
             this.sum = sum;
             this.topicNum = topicNum;
             this.numNodes = nodes.size();
             this.convergence = convergence;
         }
 
         @Override
         public void run() {
             double [] lastRank = new double[numNodes];
             double [] thisRank = new double[numNodes];
 
 
             for(int iteration = 0; ; iteration++) {
                 double []tmp = thisRank;
                 thisRank = lastRank;
                 lastRank = tmp;
                 if(iteration == 0) {
                     // Initialize
                     for(IntermediateTSPRNode node : nodes) {
                         thisRank[node.linearId] = node.lda[topicNum] / sum;
                     }
                 } else {
                     // Clear old values
                     for(int i = 0; i < numNodes; i++) {
                         thisRank[i] = 0.0;
                     }
                 }
 
                 // Power iteration
                 for(IntermediateTSPRNode node : nodes) {
                     int numNeighbors = node.edges.length;
                     double contribution = BETA * lastRank[node.linearId] / numNeighbors;
                     for(long targetId : node.edges)  {
                         IntermediateTSPRNode neighbor = nodeById.get(targetId);
                         thisRank[neighbor.linearId] += contribution;
                     }
                 }
 
                 // Reinsert leaked
                 double topicSum = 0.0;
                 for(IntermediateTSPRNode node : nodes) {
                     topicSum += thisRank[node.linearId];
                 }
 
                 double difference = 0.0;
                 for(IntermediateTSPRNode node : nodes) {
                     thisRank[node.linearId] += (1.0 - topicSum) * (node.lda[topicNum] / sum);
                     // Calculate L1 difference too
                     difference += Math.abs(thisRank[node.linearId] - lastRank[node.linearId]);
                 }
 
                 System.err.println("Pagerank topic " + topicNum + " iteration "
                         + iteration + ": delta=" + difference);
 
                 if(difference < convergence) {
                     break;
                 }
             }
 
 
             for(IntermediateTSPRNode node : nodes) {
                 node.tspr[topicNum] = thisRank[node.linearId];
             }
         }
     }
 
     public static void rankInPlace(List<IntermediateTSPRNode> nodes, double convergence) throws InterruptedException {
         if(nodes.size() == 0) {
             return;
         }
 
         final int numNodes = nodes.size();
         final int numTopics = nodes.get(0).lda.length;
 
         Map<Long, IntermediateTSPRNode> nodeById = Maps.newHashMapWithExpectedSize(nodes.size());
 
         // Compute id map and topic sums
         final double []ldaSums = new double[nodes.size()];
         for(IntermediateTSPRNode node : nodes) {
             for(int j = 0; j < numTopics; j++) {
                 ldaSums[j] += node.lda[j];
             }
 
             nodeById.put(node.id, node);
         }
 
         ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 2);
         System.out.println("Nodes " + numNodes);
         for(int tnum = 0; tnum < numTopics; tnum++) {
             executorService.submit(new RankInPlaceRunnable(nodeById, nodes, ldaSums[tnum], tnum, convergence));
         }
         executorService.shutdown();
         executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
     }
 }

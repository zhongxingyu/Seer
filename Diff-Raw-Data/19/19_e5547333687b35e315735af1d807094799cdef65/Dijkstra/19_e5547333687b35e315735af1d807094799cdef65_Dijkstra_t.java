 package com.algorithms.greedy;
 
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import com.algorithms.datastructures.graph.Edge;
 import com.algorithms.datastructures.graph.Graph;
 
 /**
  * Dijkstra's shortest-path algorithm, surely one of the greatest hits of algorithms. It works in any directed or
  * undirected graph with non-negative edge lengths, and it computes the shortest paths from a source vertex to all other
  * vertices. Particularly nice is the blazingly fast implementation that uses a heap data structure.
  * 
  * @see <a href="https://class.coursera.org/algo-2012-002">Algorithms: Design and Analysis, Part 1</a> by Tim
  *      Roughgarden for detailed algorithm description, analysis and pseudo code.
  * 
  * @author Ivan Palianytsia <a href="mailto:ivan.palianytsia@gmail.com">ivan.palianytsia@gmail.com</a>
  */
 public class Dijkstra {
     public static SortedMap<Integer, Double> shortestPath(Graph g, int source) {
         final SortedMap<Integer, Double> processed = new TreeMap<Integer, Double>();
         final Map<Integer, Double> greedyScores = new HashMap<Integer, Double>();
         greedyScores.put(source, 0d);
         for (Integer v : g.getVertices()) {
             if (v != source) {
                 greedyScores.put(v, Double.POSITIVE_INFINITY);
             }
         }
         final PriorityQueue<Integer> remaining = new PriorityQueue<Integer>(g.getNumVertices(),
                 new Comparator<Integer>() {
             @Override
             public int compare(Integer v, Integer w) {
                 return greedyScores.get(v).compareTo(greedyScores.get(w));
             }
         });
         remaining.addAll(g.getVertices());
         while (remaining.size() > 0) {
            Integer edgeStart = remaining.poll();
            Double distance = greedyScores.get(edgeStart);
            processed.put(edgeStart, distance);
            for (Edge e : g.getOutgoingEdges(edgeStart)) {
                Integer edgeEnd = e.getOtherVertex(edgeStart);
                if (remaining.contains(edgeEnd)) {
                    Double oldKey = greedyScores.get(edgeEnd);
                    greedyScores.put(edgeEnd, Math.min(oldKey, distance + e.getLength()));
                    remaining.remove(edgeEnd);
                    remaining.add(edgeEnd);
                 }
             }
         }
         return processed;
     }
 }

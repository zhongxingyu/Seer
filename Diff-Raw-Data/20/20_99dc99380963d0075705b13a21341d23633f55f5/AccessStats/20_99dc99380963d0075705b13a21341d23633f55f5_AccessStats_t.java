 package com.github.haw.ai.gkap.graph;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class AccessStats<E, V> {
     public final Map<Vertex<V>, Integer> vertexStats;
     public final Map<Edge<E, V>, Integer> edgeStats;
     
     public AccessStats() {
         this.vertexStats = new HashMap<Vertex<V>, Integer>();
         this.edgeStats   = new HashMap<Edge<E,V>, Integer>();
     }
     
     public AccessStats(AccessStats<E, V> stats) {
         this.vertexStats = new HashMap<Vertex<V>, Integer>(stats.vertexStats);
         this.edgeStats   = new HashMap<Edge<E,V>, Integer>(stats.edgeStats);
     }
     
     public void increment(Vertex<V> v) {
         if (this.vertexStats.containsKey(v)) {
             this.vertexStats.put(v, this.vertexStats.get(v)+1);
         } else {
             this.vertexStats.put(v, 1);
         }
     }
     
     public void increment(Edge<E, V> e) {
         if (this.edgeStats.containsKey(e)) {
             this.edgeStats.put(e, this.edgeStats.get(e)+1);
         } else {
             this.edgeStats.put(e, 1);
         }
     }
 
     public void increment(Vertex<V> v, int times) {
         for (int i = 0; i < times; i++) { increment(v); }
     }
 
     public void increment(Edge<E, V> e, int times) {
         for (int i = 0; i < times; i++) { increment(e); }
     }
     
     @Override
     public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AccessStats\n");
        builder.append("Vertices\n");
        for (Map.Entry<Vertex<V>, Integer> entry : vertexStats.entrySet()) {
            builder.append("\t" + entry.getKey() + "\t" + entry.getValue() + "\n");
        }
        builder.append("Edges\n");
        for (Map.Entry<Edge<E, V>, Integer> entry : edgeStats.entrySet()) {
            builder.append("\t" + entry.getKey() + "\t" + entry.getValue() + "\n");
        }
        builder.append("\n\n");
        return builder.toString();
     }
 }

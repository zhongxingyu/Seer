 package com.github.haw.ai.gkap.graph;
 
 public final class Stats<E, V> {
     public final AccessStats<E, V> accessStats;
     public final Long runtime; // in ms
     
     public Stats(AccessStats<E, V> accessStats, Long runtime) {
         this.accessStats = accessStats;
         this.runtime = runtime;
     }
     
     public Stats(Stats<E, V> stats) {
         this(new AccessStats<E, V>(stats.accessStats), stats.runtime);
     }
     
     @Override
     public String toString() {
        return "Stats(" + accessStats + "," + runtime + ")";
     }
 }

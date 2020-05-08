 package com.foursquare.heapaudit;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
 
 public class HeapQuantile extends HeapSummary {
 
     // The following is a log2 lookup table for the hash function.
 
     private static byte[] buckets = new byte[] {
         0, 0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3,
         4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
         5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
         5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
         6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
         6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
         6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
         6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
         7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
         7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
         7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
         7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
         7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
         7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
         7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
         7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7
     };
 
     // The following hash function uses the log2 lookup table to locate the
     // appropriate bucket. NOTE: everything beyond 64k is hashed to bucket 16.
 
     private static byte hash(long value) {
 
         long v = value >> 16;
 
         if (v == 0) {
 
             v = value >> 8;
 
             if (v == 0) {
 
                 return buckets[(int)value];
 
             }
             else {
 
                 return (byte)(buckets[(int)v] + 8);
 
             }
 
         }
         else {
 
             // Hash everything larger than 64k into bucket 16
 
             return 16;
 
         }
 
     }
 
     private class Quantile {
 
         // Total count of occurrences in this quantile.
 
         public int occurrences = 0;
 
         // Accumulative count of all array lengths in this quantile.
 
         public int count = 0;
 
         // Accumulative size of all occurrences in this quantile.
 
         public long size = 0;
 
     }
 
    private class Quantiles extends ConcurrentHashMap<String, ConcurrentHashMap<Integer, Quantile>> {
 
         // The following do not need to be synchronized because everything is local to the
         // current thread.
 
         public void record(String type,
                            int count,
                            long size) {
 
            ConcurrentHashMap<Integer, Quantile> quantiles = get(type);
 
             if (quantiles == null) {
 
                quantiles = new ConcurrentHashMap<Integer, Quantile>();
 
                 put(type,
                     quantiles);
 
             }
 
             Integer bucket = (int)hash(size);
 
             Quantile quantile = quantiles.get(bucket);
 
             if (quantile == null) {
 
                 quantile = new Quantile();
 
                 quantiles.put(bucket,
                               quantile);
 
             }
 
             ++quantile.occurrences;
 
             quantile.count += count;
 
             quantile.size += size;
 
         }
 
     }
 
     private class Records {
 
         // Used for tracking quantiles stats of non-array types.
 
         public final Quantiles quantilesType = new Quantiles();
 
         // Used for tracking quantiles stats of array types.
 
         public final Quantiles quantilesArray = new Quantiles();
 
         // Register this thread local instance to the global list.
 
         public Records(ArrayList<Quantiles> statsType,
                        ArrayList<Quantiles> statsArray) {
 
             synchronized (statsType) {
 
                 statsType.add(quantilesType);
 
             }
 
             synchronized (statsArray) {
 
                 statsArray.add(quantilesArray);
 
             }
 
         }
 
         public void record(String type,
                            int count,
                            long size) {
 
             (count < 0 ? quantilesType : quantilesArray).record(type,
                                                                 count,
                                                                 size);
 
         }
 
     }
 
     // Collection of stats of the same set of non-array type quantiles for each thread.
 
     private ArrayList<Quantiles> statsType = new ArrayList<Quantiles>();
 
     // Collection of stats of the same set of array type quantiles for each thread.
 
     private ArrayList<Quantiles> statsArray = new ArrayList<Quantiles>();
 
     private ThreadLocal<Records> stats = new ThreadLocal<Records>() {
 
         @Override protected Records initialValue() {
 
             return new Records(statsType,
                                statsArray);
 
         }
 
     };
 
     @Override public void record(String type,
                                  int count,
                                  long size) {
 
         stats.get().record(type,
                            count,
                            size);
 
     }
 
     public class Stats implements Comparable<Stats> {
 
         public final String name;
 
         public final int occurrences;
 
         public final int avgCount;
 
         public final long avgSize;
 
         @Override public int compareTo(Stats s) {
 
             int comparison = name.compareTo(s.name);
 
             return comparison == 0 ? -occurrences : comparison;
 
         }
 
         public Stats(String name,
                      int occurrences,
                      int count,
                      long size) {
 
             this.name = name;
 
             this.occurrences = occurrences;
 
             this.avgCount = (int)Math.ceil((double)count / occurrences);
 
             this.avgSize = (long)Math.ceil((double)size / occurrences);
 
         }
 
         @Override public String toString() {
 
             if (avgSize < 0) {
 
                 if (avgCount < 0) {
 
                     return name + " x" + occurrences;
 
                 }
                 else {
 
                     return name + "[" + avgCount + "] x" + occurrences;
 
                 }
 
             }
             else {
 
                 if (avgCount < 0) {
 
                     return name + " (" + avgSize + " bytes) x" + occurrences;
 
                 }
                 else {
 
                     return name + "[" + avgCount + "] (" + avgSize + " bytes) x" + occurrences;
 
                 }
 
             }
 
         }
 
     }
 
     // The following merges individual quantile statistics.
 
     private void merge(Quantiles combined,
                        Quantiles individual) {
 
        for (Map.Entry<String, ConcurrentHashMap<Integer, Quantile>> s: individual.entrySet()) {
 
             String type = friendly(s.getKey());
 
            ConcurrentHashMap<Integer, Quantile> quantiles = combined.get(type);
 
             if (quantiles == null) {
 
                quantiles = new ConcurrentHashMap<Integer, Quantile>();
 
                 combined.put(type,
                              quantiles);
 
             }
 
             for (Map.Entry<Integer, Quantile> q: s.getValue().entrySet()) {
 
                 Integer bucket = q.getKey();
 
                 Quantile quantile = quantiles.get(bucket);
 
                 if (quantile == null) {
 
                     quantile = new Quantile();
 
                     quantiles.put(bucket,
                                   quantile);
 
                 }
 
                 // The following may contain partial records due to in-flight allocations.
                 // However, if the sample size is large enough, it's worth the tradeoff to be
                 // slightly off with the arithmetics and avoid introducing reader/writer locks.
 
                 Quantile r = q.getValue();
 
                 quantile.occurrences += r.occurrences;
 
                 quantile.count += r.count;
 
                 quantile.size += r.size;
 
             }
 
         }
 
     }
 
     // The following flattens the quantile statistics into summary format.
 
     private void flatten(ArrayList<Stats> summary,
                          Quantiles quantiles) {
 
        for (Map.Entry<String, ConcurrentHashMap<Integer, Quantile>> s: quantiles.entrySet()) {
 
             for (Quantile q: s.getValue().values()) {
 
                 summary.add(new Stats(s.getKey(),
                                       q.occurrences,
                                       q.count,
                                       q.size));
 
             }
 
         }
 
     }
 
     // The following tallies the quantile statistics across all threads.
     // NOTE: Partial records due to in-flight allocations may occur.
 
     @HeapRecorder.Suppress public ArrayList<Stats> tally(boolean global,
                                                          boolean sorted) {
 
         Quantiles qType = new Quantiles();
 
         Quantiles qArray = new Quantiles();
 
         if (global) {
 
             synchronized (statsType) {
 
                 for (Quantiles quantiles: statsType) {
 
                     merge(qType,
                           quantiles);
 
                 }
 
             }
 
             synchronized (statsArray) {
 
                 for (Quantiles quantiles: statsArray) {
 
                     merge(qArray,
                           quantiles);
 
                 }
 
             }
 
         }
         else {
 
             merge(qType,
                   stats.get().quantilesType);
 
             merge(qArray,
                   stats.get().quantilesArray);
 
         }
 
         ArrayList<Stats> sQuantiles = new ArrayList<Stats>();
 
         flatten(sQuantiles,
                 qType);
 
         flatten(sQuantiles,
                 qArray);
 
         if (sorted) {
 
             Collections.sort(sQuantiles);
 
         }
 
         return sQuantiles;
 
     }
 
     public String summarize(boolean global) {
 
         String summary = "HEAP: " + getId();
 
         int r = registrations.get();
 
         if (r > 0) {
 
             summary += " x" + r;
 
         }
 
         for (Stats s: tally(global, true)) {
 
             summary += "\n      - " + s.toString();
 
         }
 
         return summary;
 
     }
 
     @Override public String summarize() {
 
         return summarize(true);
 
     }
 
 }

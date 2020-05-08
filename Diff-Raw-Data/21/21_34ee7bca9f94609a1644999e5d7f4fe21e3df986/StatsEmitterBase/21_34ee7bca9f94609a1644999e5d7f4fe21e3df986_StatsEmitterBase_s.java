 package com.inmobi.stats;
 
 import com.inmobi.stats.StatsEmitter;
 import com.inmobi.stats.StatsExposer;
 import java.util.List;
 import java.util.ArrayList;
 
 public abstract class StatsEmitterBase implements StatsEmitter {
 
     protected List<StatsExposer> statsExposers = new ArrayList<StatsExposer>();
 
     @Override
    public synchronized void add(StatsExposer s) {
         statsExposers.add(s);
     }
 
     @Override
    public synchronized void remove(StatsExposer s) {
         statsExposers.remove(s);
     }
 
     @Override
    public synchronized void removeAll() {
         statsExposers.clear();
     }
 
     protected boolean isEmpty() {
         return statsExposers.isEmpty();
     }
 }

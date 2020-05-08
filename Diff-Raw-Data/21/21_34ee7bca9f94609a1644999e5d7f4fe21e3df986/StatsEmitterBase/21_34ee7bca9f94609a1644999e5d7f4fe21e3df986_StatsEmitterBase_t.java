 package com.inmobi.stats;
 
 import com.inmobi.stats.StatsEmitter;
 import com.inmobi.stats.StatsExposer;
 import java.util.List;
 import java.util.ArrayList;
 
 public abstract class StatsEmitterBase implements StatsEmitter {
 
     protected List<StatsExposer> statsExposers = new ArrayList<StatsExposer>();
 
     @Override
    public void add(StatsExposer s) {
      synchronized (statsExposers) {
         statsExposers.add(s);
      }  
     }
 
     @Override
    public void remove(StatsExposer s) {
      synchronized (statsExposers) {
         statsExposers.remove(s);
      }
     }
 
     @Override
    public void removeAll() {
      synchronized (statsExposers) {
         statsExposers.clear();
      }
     }
 
     protected boolean isEmpty() {
      synchronized (statsExposers) {
         return statsExposers.isEmpty();
      }
     }
 }

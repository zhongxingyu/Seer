 package org.drools.planner.examples.ras2012.model;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class WaitTime {
 
     private static final Map<Integer, WaitTime> waitTimes = new HashMap<Integer, WaitTime>();
 
     public static synchronized WaitTime getWaitTime(final int i) {
         if (i < 1) {
             throw new IllegalArgumentException("Wait time must be bigger than zero.");
         }
         if (!WaitTime.waitTimes.containsKey(i)) {
             final WaitTime w = new WaitTime(i);
             WaitTime.waitTimes.put(i, w);
             return w;
         }
         return WaitTime.waitTimes.get(i);
     }
 
     private final long millisWaitFor;
 
     private WaitTime(final long minutes) {
         this.millisWaitFor = minutes * 60 * 1000;
     }
 
     public long getMillisWaitFor() {
         return this.millisWaitFor;
     }
 
     public long getMinutesWaitFor() {
         return this.millisWaitFor / 1000 / 60;
     }
 
     @Override
     public String toString() {
         return "WaitTime [" + this.getMinutesWaitFor() + " min.]";
     }
 
 }

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
 
     private final int minutesWaitFor;
 
     private WaitTime(final int minutes) {
         this.minutesWaitFor = minutes;
     }
 
     public int getMinutesWaitFor() {
         return this.minutesWaitFor;
     }
 
     @Override
     public String toString() {
        return "WaitTime [minutesWaitFor=" + this.minutesWaitFor + "]";
     }
 
 }

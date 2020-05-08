 /*
  *  The OpenDiamond Platform for Interactive Search
  *
  *  Copyright (c) 2007 Carnegie Mellon University
  *  All rights reserved.
  *
  *  This software is distributed under the terms of the Eclipse Public
  *  License, Version 1.0 which can be found in the file named LICENSE.
  *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
  *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
  */
 
 package edu.cmu.cs.diamond.opendiamond;
 
 import java.util.Collections;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * A class representing some runtime statistics for a single server.
  */
 public class ServerStatistics {
     final private Map<String, Long> serverStatistics;
 
     final private Map<String, FilterStatistics> filterStatistics =
             new HashMap<String, FilterStatistics>();
 
     ServerStatistics(Map<String, Long> serverStatistics,
             List<XDR_filter_stats> filterStatistics) {
         this.serverStatistics = serverStatistics;
 
        for (int i = 0; i < filterStatistics.size(); i++) {
            XDR_filter_stats filterStat = filterStatistics.get(i);
             String filterName = filterStat.getName();
             this.filterStatistics.put(filterName,
                new FilterStatistics(filterName, filterStat.getStats()));
         }
     }
 
     /**
      * Gets the global server statistics.
      *
      * @return overall server statistics
      */
     public Map<String, Long> getServerStats() {
         return Collections.unmodifiableMap(serverStatistics);
     }
 
     /**
      * Gets the per-filter statistics.
      *
      * @return per-filter statistics
      */
     public Map<String, FilterStatistics> getFilterStats() {
         return Collections.unmodifiableMap(filterStatistics);
     }
 
     /**
      * Helper method for getDroppedObjects, getProcessedObjects(),
      * getTotalObjects()
      *
      * @param name statistics name
      * @return statistics value corresponding to name
      */
     private int getStat(String name) {
         if (serverStatistics.get(name) == null) {
             throw new IllegalArgumentException("No such statistics name");
         }
 
         long value = serverStatistics.get(name);
         if (value < Integer.MIN_VALUE
                 || value > Integer.MAX_VALUE) {
             throw new UnsupportedOperationException(
                     "Value out of range supported by int");
         }
         return (int) value;
     }
 
     /**
      * Gets the count of dropped objects.
      *
      * @return dropped object count
      *
      * @deprecated use getServerStats().get("objs_dropped") instead
      */
     @Deprecated
     public int getDroppedObjects() {
         return getStat("objs_dropped");
     }
 
     /**
      * Gets the count of processed objects.
      *
      * @return processed object count
      *
      * @deprecated use getServerStats().get("objs_processed") instead
      */
     @Deprecated
     public int getProcessedObjects() {
         return getStat("objs_processed");
     }
 
     /**
      * Gets the count of total objects.
      *
      * @return total object count
      *
      * @deprecated use getServerStats().get("objs_total") instead
      */
     @Deprecated
     public int getTotalObjects() {
         return getStat("objs_total");
     }
 
     @Override
     public String toString() {
         return getStat("objs_total") + " total, " + getStat("objs_processed")
                 + " processed, " + getStat("objs_dropped") + " dropped";
     }
 }

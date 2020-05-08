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
 
 import java.util.Map;
 
 /**
  * A class representing some runtime statistics for a single filter.
  */
 public class FilterStatistics {
 
     private final String name;
 
     private final Map<String, Long> filterStatistics;
 
    public FilterStatistics(String name, Map<String, Long> filterStatistics) {
         this.name = name;
         this.filterStatistics = filterStatistics;
     }
 
     /**
      * Get filter name.
      *
      * @return filter name
      */
     public String getName() {
         return name;
     }
 
     /**
      * Gets statistics for a single filter.
      *
      * @return filter statistics
      */
     public Map<String, Long> getFilterStats() {
         return filterStatistics;
     }
 }

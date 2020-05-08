 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2008-2011 SonarSource
  * Written (W) 2011 Andrew Tereskin
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package org.sonar.duplications.algorithm;
 
 
 import com.google.common.collect.Maps;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Map;
 
 public class StatsCollector {
 
   private Logger logger;
   private Map<String, Long> workingTimes;
   private Map<String, Long> startTimes;
   private Map<String, Double> statNumbersDouble;
   private Map<String, Long> statNumbersLong;
 
   private String name;
   private boolean debug = false;
 
   public StatsCollector(String name) {
     this.workingTimes = Maps.newLinkedHashMap();
     this.startTimes = Maps.newLinkedHashMap();
     this.statNumbersDouble = Maps.newLinkedHashMap();
    this.statNumbersLong = Maps.newLinkedHashMap();
     this.name = name;
     this.logger = LoggerFactory.getLogger(StatsCollector.class);
   }
 
   public StatsCollector(String name, Logger logger) {
     this(name);
     this.logger = logger;
   }
 
   public StatsCollector setLogger(Logger logger) {
     this.logger = logger;
     return this;
   }
 
   public Logger getLogger() {
     return logger;
   }
 
   public StatsCollector startTime(String key) {
     startTimes.put(key, System.currentTimeMillis());
     return this;
   }
 
   public StatsCollector stopTime(String key) {
     long startTime = startTimes.get(key);
     long prevTime = 0;
     if (workingTimes.containsKey(key)) {
       prevTime = workingTimes.get(key);
     }
     prevTime += System.currentTimeMillis() - startTime;
     workingTimes.put(key, prevTime);
     return this;
   }
 
   public StatsCollector addNumber(String key, double value) {
     double prev = 0;
     if (statNumbersDouble.containsKey(key)) {
       prev = statNumbersDouble.get(key);
     }
     statNumbersDouble.put(key, prev + value);
     return this;
   }
 
   public StatsCollector addNumber(String key, long value) {
     long prev = 0;
     if (statNumbersLong.containsKey(key)) {
       prev = statNumbersLong.get(key);
     }
     statNumbersLong.put(key, prev + value);
     return this;
   }
 
   public StatsCollector setLevelToDebug() {
     debug = true;
     return this;
   }
 
   public StatsCollector reset() {
     workingTimes.clear();
     startTimes.clear();
     statNumbersDouble.clear();
     return this;
   }
 
   public void printTimeStatistics() {
     if (debug) {
       logger.debug("---- Time statistics for {}", name);
     } else {
       logger.info("---- Time statistics for {}", name);
     }
 
     long total = 0;
     for (String key : workingTimes.keySet()) {
       total += workingTimes.get(key);
     }
     for (String key : workingTimes.keySet()) {
       long time = workingTimes.get(key);
       double percentage = 100.0 * time / total;
       double seconds = time / 1000.0;
 
       percentage = Math.round(percentage * 100.0) / 100.0;
       seconds = Math.round(seconds * 100.0) / 100.0;
 
       if (debug) {
         logger.debug("Working time for '{}': {} s - {}%", new Object[]{key, seconds, percentage});
       } else {
         logger.info("Working time for '{}': {} s - {}%", new Object[]{key, seconds, percentage});
       }
     }
   }
 
   public void printNumberStatistics() {
     if (debug) {
       logger.debug("---- Number statistics for {}", name);
     } else {
       logger.info("---- Number statistics for {}", name);
     }
 
     for (String key : statNumbersDouble.keySet()) {
       double val = statNumbersDouble.get(key);
       if (debug) {
         logger.debug("Number statistics for '{}': {}", key, val);
       } else {
         logger.info("Number statistics for '{}': {}", key, val);
       }
     }
 
     for (String key : statNumbersLong.keySet()) {
       double val = statNumbersLong.get(key);
       if (debug) {
         logger.debug("Number statistics for '{}': {}", key, val);
       } else {
         logger.info("Number statistics for '{}': {}", key, val);
       }
     }
   }
 
   public void printAllStatistics() {
 
     printTimeStatistics();
 
     printNumberStatistics();
   }
 
 }

 package com.group7.dragonwars.engine;
 
 import java.util.TreeMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 public class Statistics {
     private Map<String, Double> sMap;
 
     public Statistics() {
         this.sMap = new TreeMap<String, Double>();
     }
 
     private void putIfNotPresent(final String stat) {
         if (!sMap.containsKey(stat)) {
            setStatistic(stat, 0);
         }
     }
 
     public void increaseStatistic(final String stat) {
        increaseStatistic(stat, 1);
     }
 
     public void increaseStatistic(final String stat, final Double amount) {
         putIfNotPresent(stat);
         setStatistic(stat, sMap.get(stat) + amount);
     }
 
     public void decreaseStatistic(final String stat) {
        increaseStatistic(stat, -1);
     }
 
     public void decreaseStatistic(final String stat, final Double amount) {
         increaseStatistic(stat, -amount);
     }
 
     public void setStatistic(final String stat, final Double amount) {
         sMap.put(stat, amount);
     }
 
     public Double getStatistic(final String stat) {
         putIfNotPresent(stat);
         return sMap.get(stat);
     }
 
     @Override
     public String toString() {
         String r = "";
         for (Map.Entry<String, Double> ent : sMap.entrySet()) {
             r += String.format("%s: %d\n", ent.getKey(), ent.getValue());
         }
 
         return r;
     }
 
 
 }

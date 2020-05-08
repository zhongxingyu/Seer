 package org.loadtest;
 
 import java.util.*;
 
 /**
  * Class accumulates statistics: number of runs, errors, queries and http fetch execution time.
  */
 public class Stats {
     public Stats() {
         reset();
     }
 
     public synchronized void reset() {
         runs = 0;
         queries = 0;
         errors = 0;
         urlStats = new TreeMap();
     }
 
     private int runs;
     private int queries;
     private int errors;
     private Map urlStats;
 
     private static class UrlStat {
         private int count;
         private long time;
         private long minimum;
         private long maximum;
         private final String url;
 
         private UrlStat(String url) {
             this.url = url;
         }
 
 
         public void add(long time) {
             if (count == 0 || minimum > time) {
                 minimum = time;
             }
            if (count == 0 || maximum > time) {
                 maximum = time;
             }
             this.time += time;
             this.count++;
         }
 
         public double getAvarage() {
             if (count == 0) {
                 return 0;
             } else {
                 return (double)time / count;
             }
         }
 
         public long getMinimum() {
             return minimum;
         }
 
         public long getMaximum() {
             return maximum;
         }
 
         public String getUrl() {
             return url;
         }
     }
 
     public synchronized void addRun() { runs++; }
     public synchronized void addQuery(String url, long time) {
         queries++;
         UrlStat stat = (UrlStat) urlStats.get(url);
         if (stat == null) {
             stat = new UrlStat(url);
             urlStats.put(url, stat);
         }
         stat.add(time);
     }
     public synchronized void addError() { errors++; }
 
     public synchronized void report(int slowQueriesToShow) {
         System.out.println("Stats{" +
                 "runs=" + runs +
                 ", queries=" + queries +
                 ", errors=" + errors +
                 '}');
         List lst = new ArrayList(urlStats.values());
         Collections.sort(lst, new UrlStatComparator());
         for (Object stat : lst.subList(0, Math.min(slowQueriesToShow, lst.size()))) {
             UrlStat urlStat = (UrlStat) stat;
             System.out.printf("min=%5d avg=%8.2f max=%5d %s%n",
                     urlStat.getMinimum(),
                     urlStat.getAvarage(),
                     urlStat.getMaximum(),
                     urlStat.getUrl());
 
         }
     }
 
     private class UrlStatComparator implements Comparator<UrlStat> {
         public int compare(UrlStat o1, UrlStat o2) {
             return Double.compare(o1.getAvarage(), o2.getAvarage());
         }
     }
 }

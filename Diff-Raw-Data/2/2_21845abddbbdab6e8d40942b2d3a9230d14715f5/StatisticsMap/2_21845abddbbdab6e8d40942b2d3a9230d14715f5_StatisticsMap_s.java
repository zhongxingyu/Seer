 package org.ktln2.android.callstat;
 
 import android.content.Context;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.Comparator;
 import java.util.ArrayList;
 
 
 /*
  * Container for statistical data management.
  *
  * It's pratically a TreeMap having as key the number and as values an array
  * containing the duration for the calls of this contact.
  *
  * The array is actually a TreeSet so to maintain internally an ordering
  * and be able to know which is the shortest/longest call for each contact.
  */
 class StatisticsMap extends TreeMap<String, CallStat> {
     private static final int CALL_STAT_MAP_TYPE_MIN = 0;
     private static final int CALL_STAT_MAP_TYPE_MAX = 1;
 
     // these will contain the duration related values
     private long mMin = 0, mMax = 0, mTotal = 0, mCount = 0;
     /*
      * Return the data divided using some bins.
      *
      * http://stackoverflow.com/questions/10786465/how-to-generate-bins-for-histogram-using-apache-math-3-0-in-java
      */
     public int[] calcHistogram(Long[] values, int numBins) {
         final int[] result = new int[numBins];
         final double binSize = (mMax - mMin)/numBins;
 
         for (double d : values) {
             int bin = (int) ((d - mMin) / binSize);
             if (bin < 0) { /* this data is smaller than min */ }
             else if (bin >= numBins) { /* this data point is bigger than max */ }
             else {
                 result[bin] += 1;
             }
         }
 
         return result;
     }
 
     /*
      * This returns the bins used to build up the histogram.
      *
      * For now we use a resolution of 10 seconds.
      */
     public int[] getBinsForDurations(int delta) {
         int number_of_bins = ((int)mMax)/delta;
         return calcHistogram(getAllDurations().toArray(new Long[1]), number_of_bins);
     }
 
     /*
      * Return the complete list of all the durations.
      */
     public ArrayList<Long> getAllDurations() {
         ArrayList<Long> entries = new ArrayList<Long>();
         for (Entry<String, CallStat> entry: entrySet()) {
             entries.addAll(entry.getValue().getAllDurations());
         }
 
         return entries;
     }
 
     public void put(String key, Long value, Context context) {
         // update values
         mMin = value < mMin ? value : mMin;
         mMax = value > mMax ? value : mMax;
         mTotal += value;
         mCount++;
 
         CallStat set = get(key);
 
         if (set == null) {
             set = new CallStat(key, context);
         }
 
         set.add(value);
         // TODO: it's mandatory to update it?
         put(key, set);
     }
 
     public long getMinDuration() {
         return mMin;
     }
 
     public long getMaxDuration() {
         return mMax;
     }
 
     public long getTotalDuration() {
         return mTotal;
     }
 
     public int getTotalContacts() {
         return size();
     }
 
     public long getTotalCalls() {
         return mCount;
     }
 
     /*
      * General purpouse function aimed to order the CallStat objects
      * with respect of different parameter.
      */
     protected CallStat[] getCallStatOrderedBy(int type) {
         TreeSet<CallStat> values;
         Comparator comparator;
 
         switch (type) {
             case CALL_STAT_MAP_TYPE_MIN:
                 comparator = new Comparator<CallStat>() {
                     public int compare(CallStat lhs, CallStat rhs) {
                         return (int)(lhs.getMinDuration() - rhs.getMinDuration());
                     }
                 };
                 break;
             case CALL_STAT_MAP_TYPE_MAX:
             default:
                 comparator = new Comparator<CallStat>() {
                     public int compare(CallStat lhs, CallStat rhs) {
                        return (int)(lhs.getMaxDuration() - rhs.getMaxDuration());
                     }
                 };
                 break;
         }
 
         values = new TreeSet<CallStat>(comparator);
 
         values.addAll(values());
 
         return values.toArray(new CallStat[1]);
     }
 
     public CallStat[] getCallStatOrderedByMaxDuration() {
         return getCallStatOrderedBy(CALL_STAT_MAP_TYPE_MAX);
     }
 }

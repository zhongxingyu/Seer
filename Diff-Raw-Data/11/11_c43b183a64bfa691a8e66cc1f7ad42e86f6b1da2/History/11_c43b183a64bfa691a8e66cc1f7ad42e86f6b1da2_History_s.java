 package com.ingemark.requestage.plugin.ui;
 
 import static com.ingemark.requestage.StressTester.TIMESLOTS_PER_SEC;
 import static com.ingemark.requestage.Util.arrayMean;
 import static com.ingemark.requestage.Util.event;
 import static com.ingemark.requestage.Util.sneakyThrow;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.EVT_HISTORY_UPDATE;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.globalEventHub;
 
 import java.lang.reflect.Field;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.ingemark.requestage.Stats;
 
 public class History {
   static final Logger log = LoggerFactory.getLogger(History.class);
   private static final int FULL_SIZE = 1<<11, BUFSIZ = 8*TIMESLOTS_PER_SEC;
   public static final String[] keys = {
     "avgServIntensty", "pendingReqs", "reqsPerSec", "failsPerSec"};
   private static final Map<String, Field> statFields = new HashMap<String, Field>(); static {
     try { for (String key : keys) statFields.put(key, Stats.class.getField(key)); }
     catch (Exception e) { sneakyThrow(e); }
   }
   public String name;
   private int index, bufIndex, bufLimit = TIMESLOTS_PER_SEC, divisor = 1, calledCount;
   private final Map<String, double[]> histories = new HashMap<String, double[]>(); {
     for (String key : keys) histories.put(key, new double[FULL_SIZE]);
   }
   private final Map<String, float[]> histBufs = new HashMap<String, float[]>(); {
     for (String key : keys) histBufs.put(key, new float[BUFSIZ]);
   }
   private final Date[] timestamps = new Date[FULL_SIZE];
 
   public void statsUpdate(Stats stats) {
     if (++calledCount % divisor != 0) return;
     name = stats.name;
     try {
       for (String key : keys) histBufs.get(key)[bufIndex] = statFields.get(key).getFloat(stats);
     } catch (Exception e) { sneakyThrow(e); }
     if (++bufIndex == bufLimit) {
       bufIndex = 0;
       timestamps[index] = new Date();
      for (String key : keys) histories.get(key)[index] = arrayMean(histBufs.get(key), bufLimit);
       globalEventHub().notifyListeners(EVT_HISTORY_UPDATE, event(this));
       if (++index == FULL_SIZE) {
         squeezeDates(timestamps);
         for (String key : keys) squeeze(histories.get(key));
         index /= 2;
         if (bufLimit <= BUFSIZ/2) bufLimit *= 2;
         else divisor *= 2;
       }
     }
   }
 
   private static void squeeze(double[] array) {
     for (int i = 0; i < array.length/2; i++) array[i] = (array[2*i]+array[2*i+1])/2;
   }
   private static void squeezeDates(Date[] array) {
     for (int i = 0; i < array.length/2; i++)
       array[i] = new Date((array[2*i].getTime()+array[2*i+1].getTime())/2);
   }
 
   public double[] history(String id) {
     return Arrays.copyOf(histories.get(id), index);
   }
   public Date[] timestamps() { return Arrays.copyOf(timestamps, index); }
 }

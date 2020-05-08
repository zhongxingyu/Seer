 /*
  * Made with all the love in the world
  * by scireum in Remshalden, Germany
  *
  * Copyright by scireum GmbH
  * http://www.scireum.de - info@scireum.de
  */
 
 package sirius.web.health;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import org.joda.time.DateTime;
 import sirius.kernel.commons.Strings;
 import sirius.kernel.di.Lifecycle;
 import sirius.kernel.di.std.ConfigValue;
 import sirius.kernel.health.Exceptions;
 import sirius.kernel.timer.EveryTenMinutes;
 
 import java.io.*;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 /**
  * File based storage for statistical measures.
  * <p>
  * This class provides a simple way of storing time dependent statistical values.
  * </p>
  */
 public class MetricsDB implements Lifecycle, EveryTenMinutes {
 
     @ConfigValue("health.metrics-basedir")
     private String statsBaseDir;
     private File basedir;
 
     private Map<String, List<StatsEntry>> entries = Maps.newTreeMap();
 
     private class StatsEntry implements Comparable<StatsEntry> {
         protected DateTime timestamp;
         protected double value;
 
         public StatsEntry(DateTime timestamp, double value) {
             this.timestamp = timestamp;
             this.value = value;
         }
 
         @Override
         public int compareTo(StatsEntry o) {
             if (o == null || o.timestamp == null) {
                 return 1;
             }
             return timestamp.compareTo(o.timestamp);
         }
     }
 
     @Override
     public void started() {
 
     }
 
     @Override
     public void stopped() {
         flushDatabase();
     }
 
     private void flushDatabase() {
         synchronized (entries) {
             if (Strings.isEmpty(statsBaseDir)) {
                 // If no directory is given, we abort
                 return;
             }
             for (Map.Entry<String, List<StatsEntry>> entry : entries.entrySet()) {
                 appendStats(entry.getKey(), entry.getValue());
             }
             entries.clear();
         }
     }
 
     private void appendStats(String key, List<StatsEntry> entries) {
         ObjectOutputStream out = null;
         DateTime lastFileDate = null;
         int lastIndex = -1;
         try {
             try {
                 for (StatsEntry entry : entries) {
                     if (entry.value != 0.0d) {
                         if (out == null || !sameMonth(entry.timestamp, lastFileDate)) {
                             if (out != null) {
                                 out.close();
                                 out = null;
                             }
                             File file = getFileForDate(key, entry.timestamp);
                             lastFileDate = entry.timestamp;
                             out = new ObjectOutputStream(new FileOutputStream(file, true));
                             lastIndex = readLastIndexWritten(file);
                         }
                         int monthIndex = computeMinuteIndex(entry.timestamp);
                         if (lastIndex == -1 || lastIndex < monthIndex) {
                             out.writeInt(monthIndex);
                             out.writeDouble(entry.value);
                             lastIndex = monthIndex;
                         }
                     }
                 }
             } finally {
                 if (out != null) {
                     out.close();
                 }
             }
         } catch (IOException e) {
             Exceptions.handle(e);
         }
     }
 
     private int computeMinuteIndex(DateTime timestamp) {
         return 60 * 24 * (timestamp.dayOfMonth().get() - 1) + timestamp.minuteOfDay().get();
     }
 
     private int readLastIndexWritten(File file) {
         int lastIndex = -1;
         try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
             while (in.available() > 0) {
                 lastIndex = in.readInt();
                 in.readDouble();
             }
         } catch (IOException ex) {
             Exceptions.handle(ex);
         }
 
         return lastIndex;
     }
 
     private File getFileForDate(String metric, DateTime timestamp) {
         return new File(new File(new File(getBasedir(), String.valueOf(timestamp.year().get())),
                                  String.valueOf(timestamp.monthOfYear().get())), metric);
     }
 
     private File getBasedir() {
         if (basedir == null) {
             basedir = new File(statsBaseDir);
             if (!basedir.exists()) {
                 basedir.mkdirs();
             }
         }
 
         return basedir;
     }
 
     private boolean sameMonth(DateTime timestamp, DateTime lastDate) {
         return timestamp.monthOfYear().get() == lastDate.monthOfYear().get() && timestamp.dayOfMonth().get() == lastDate
                 .dayOfMonth()
                 .get();
     }
 
     public void storeNow(String metric, double value) {
         store(metric, new DateTime(), value);
     }
 
     public void store(String metric, DateTime timestamp, double value) {
         if (!metric.matches("[a-z0-9\\-_]+")) {
             Exceptions.handle()
                       .withSystemErrorMessage(
                              "Invalid metric name: %s. A metric must only consist of characters, digits or dashes.",
                               metric)
                       .handle();
             return;
         }
         synchronized (entries) {
             List<StatsEntry> list = entries.get(metric);
             if (list == null) {
                 list = Lists.newArrayList();
                 entries.put(metric, list);
             }
             list.add(new StatsEntry(timestamp, value));
             Collections.sort(list);
         }
     }
 
     @Override
     public String getName() {
         return "Statistical Database";
     }
 
     @Override
     public void runTimer() throws Exception {
         flushDatabase();
     }
 }

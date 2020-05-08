 package com.plexobject.commons.log;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Comparator;
 import java.util.Date;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.time.FastDateFormat;
 import org.apache.log4j.Layout;
 import org.apache.log4j.net.SMTPAppender;
 import org.apache.log4j.spi.LoggingEvent;
 
 import com.plexobject.commons.config.Configuration;
 import com.plexobject.commons.jmx.JMXRegistrar;
 import com.plexobject.commons.jmx.impl.ServiceJMXBeanImpl;
 import com.plexobject.commons.metrics.Metric;
 import com.plexobject.commons.metrics.Timer;
 import com.plexobject.commons.utils.LRUSortedList;
 
 public class FilteredSMTPAppender extends SMTPAppender {
     private static final String SMTP_FILTER_MIN_DUPLICATE_INTERVAL_SECS = "smtp.filter.min.duplicate.interval.secs";
     private static final int MAX_STATS = Configuration.getInstance()
             .getInteger("smtp.filter.max", 100);
     private static final Date STARTED = new Date();
 
     private static final String DEFAULT_DATE_FORMAT = "MM/dd/yy HH:mm";
 
     private String datePattern = DEFAULT_DATE_FORMAT;
 
     private int mailIntervalSecs = Configuration.getInstance().getInteger(SMTP_FILTER_MIN_DUPLICATE_INTERVAL_SECS, 60); // 1 minute
 
     private int linesOfTrace = 20;
 
     private FastDateFormat dateFormatter = FastDateFormat.getInstance(DEFAULT_DATE_FORMAT);
 
     final class Stats implements Comparable<Stats> {
         final int checksum;
         final long firstSeen;
         long lastSeen;
         long lastSent;
         int numSeen;
         int numEmails;
 
         Stats(LoggingEvent event) {
             StringBuilder sb = new StringBuilder();
             String[] trace = event.getThrowableStrRep();
             if (trace != null) {
                 int total = linesOfTrace > 0 ? linesOfTrace : trace.length;
                 // skip the first line, and only consider the top 'linesOfTrace' lines of trace
                 // (or all of them, if linesOfTrace is 0)
                 for (int i = 1; i < trace.length && i < total; i++) {
                     sb.append(trace[i].trim());
                 }
             } else {
                 sb.append(event.getMessage());
             }
             this.checksum = sb.toString().hashCode();
            firstSeen = lastSeen = lastSent = System.currentTimeMillis();
             numSeen = 1;
         }
 
         boolean check() {
             long current = System.currentTimeMillis();
             long elapsed = current - lastSent;
 
             numSeen++;
             lastSeen = current;
 
             if (elapsed > mailIntervalSecs * 1000) {
                 lastSent = current;
                 numEmails++;
                 return true;
             } else {
                 return false;
             }
         }
 
         @Override
         public boolean equals(Object object) {
             if (!(object instanceof Stats)) {
                 return false;
             }
             Stats rhs = (Stats) object;
             return new EqualsBuilder().append(this.checksum, rhs.checksum)
                     .isEquals();
 
         }
 
         @Override
         public int hashCode() {
             return checksum;
         }
 
         @Override
         public String toString() {
             return " (" + checksum + ") occurred " + numSeen + " times, "
                     + numEmails + " # of emails, first @"
                     + dateFormatter.format(new Date(firstSeen)) + ", last @"
                     + dateFormatter.format(new Date(lastSeen))
                     + " since server started @" + dateFormatter.format(STARTED);
         }
 
         @Override
         public int compareTo(Stats other) {
             return checksum - other.checksum;
         }
     }
 
     final static class StatsCmp implements Comparator<Stats> {
         @Override
         public int compare(Stats first, Stats second) {
             return first.checksum - second.checksum;
         }
     }
 
     private static final LRUSortedList<Stats> STATS_LIST = new LRUSortedList<Stats>(
             MAX_STATS, new StatsCmp());
     private LoggingEvent event;
     private ServiceJMXBeanImpl mbean;
     private Layout layout;
 
     public FilteredSMTPAppender() {
         mbean = JMXRegistrar.getInstance().register(getClass());
         mbean.addPropertyChangeListener(new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent event) {
                 try {
                     if (event != null
                             && SMTP_FILTER_MIN_DUPLICATE_INTERVAL_SECS
                                     .equalsIgnoreCase(event.getPropertyName())) {
                         mailIntervalSecs = Integer
                                 .parseInt((String) event.getNewValue());
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         });
 
     }
 
     public void append(LoggingEvent event) {
         this.event = event;
         if (layout == null) {
             layout = getLayout();
         }
         super.append(event);
     }
 
     protected boolean checkEntryConditions() {
         final Timer timer = Metric.newTimer(getClass().getSimpleName()
                 + ".checkEntryConditions");
         try {
             boolean check = true;
             if (event != null) {
                 Stats newStats = new Stats(event);
                 Stats stats = STATS_LIST.get(newStats);
                 if (stats == null) {
                     stats = newStats;
                     STATS_LIST.add(stats);
                 } else {
                     check = stats.check();
                 }
                 if (check) {
                     setMessageFooter(stats);
                 }
             }
             return check && super.checkEntryConditions();
         } finally {
             timer.stop();
         }
     }
 
     public int getLinesOfTrace() {
         return linesOfTrace;
     }
 
     public void setLinesOfTrace(int linesOfTrace) {
         this.linesOfTrace = linesOfTrace;
     }
 
     public int getMailIntervalSecs() {
         return mailIntervalSecs;
     }
 
     public void setMailIntervalSecs(int mailIntervalSecs) {
         this.mailIntervalSecs = mailIntervalSecs;
     }
 
     public String getDatePattern() {
         return datePattern;
     }
 
     public void setDatePattern(String datePattern) {
         this.datePattern = datePattern;
         this.dateFormatter = FastDateFormat.getInstance(datePattern);
     }
 
     private void setMessageFooter(Stats stats) {
         String message = event.getMessage().toString();
 
         final String footer = "\n\n-------------------------\n" + message
                 + " - " + stats;
 
         if (layout != null) {
             setLayout(new Layout() {
 
                 @Override
                 public void activateOptions() {
                     layout.activateOptions();
 
                 }
 
                 @Override
                 public String format(LoggingEvent evt) {
                     return layout.format(evt);
                 }
 
                 @Override
                 public String getFooter() {
                     return footer;
                 }
 
                 @Override
                 public boolean ignoresThrowable() {
                     return layout.ignoresThrowable();
                 }
             });
         }
     }
 }

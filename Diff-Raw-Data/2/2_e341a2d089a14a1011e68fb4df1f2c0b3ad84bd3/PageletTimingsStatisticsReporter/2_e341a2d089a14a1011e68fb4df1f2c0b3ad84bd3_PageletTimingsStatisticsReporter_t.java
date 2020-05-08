 /**
  * 
  */
 package com.fatwire.cs.profiling.ss.reporting.reporters;
 
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import com.fatwire.cs.profiling.ss.ResultPage;
 import com.fatwire.cs.profiling.ss.reporting.Report;
 import com.fatwire.cs.profiling.ss.statistics.SimpleStatistics;
 
 public class PageletTimingsStatisticsReporter extends ReportDelegatingReporter {
 
     private final Map<String, SimpleStatistics> stats = new HashMap<String, SimpleStatistics>();
 
     private final AtomicInteger pagesDone = new AtomicInteger();
 
     /**
      * @param file
      */
     public PageletTimingsStatisticsReporter(final Report report) {
         super(report);
 
     }
 
     public synchronized void addToReport(final ResultPage page) {
         pagesDone.incrementAndGet();
         final String pagename = page.getPageName();
         if (pagename != null) {
             SimpleStatistics ss = stats.get(pagename);
             if (ss == null) {
                 ss = new SimpleStatistics(pagename);
                 stats.put(pagename, ss);
             }
             ss.addValue(page.getReadTime());
         }
 
     }
 
     @Override
     public void endCollecting() {
         report.startReport();
        report.addRow("reporting on " + pagesDone.get() + " pages");
         final DecimalFormat df = new DecimalFormat("#,##0.00");
         final DecimalFormat lf = new DecimalFormat("#,##0");
         final DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
         int l = 0;
         for (final String s : stats.keySet()) {
             l = Math.max(s.length(), l);
         }
         final char[] blank = new char[l];
         Arrays.fill(blank, ' ');
         final String header = "pagename" + new String(blank, 0, l - 8)
                 + "\tinvocations\taverage\tfirst\tmax\tstandard-deviation";
         report.addRow(header);
 
         for (final SimpleStatistics s : stats.values()) {
             final String n = s.getName()
                     + new String(blank, 0, l - s.getName().length());
             final String line = n + "\t" + s.getInvocations() + "\t"
                     + df.format(s.getAverage()) + "\t"
                     + dateFormat.format(new Date(s.getFirstDate())) + "\t"
                     + lf.format(s.getMaxvalue()) + "\t"
                     + df.format(s.getStandardDeviation());
             report.addRow(line);
         }
         report.finishReport();
 
     }
 
     @Override
     public void startCollecting() {
         stats.clear();
         pagesDone.set(0);
 
     }
 
 }

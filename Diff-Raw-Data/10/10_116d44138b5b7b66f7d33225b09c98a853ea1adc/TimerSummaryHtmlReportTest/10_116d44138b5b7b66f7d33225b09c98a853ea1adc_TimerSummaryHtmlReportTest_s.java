 package com.jawspeak.stacktimer;
 
 import java.io.FileWriter;
 
 import org.junit.Test;
 
 import com.google.common.collect.Lists;
 
 public class TimerSummaryHtmlReportTest {
   
   @Test
   public void notSoMuchATestAsASampleGenerator() throws Exception {
     // we could use htmlunit, etc to test this if we want.
     TimerSummary summary = new TimerSummary("my controller", 0, 300, Lists.newArrayList(
         new TimerSummary("service call 1", 10L, 250L), new TimerSummary("report renderer", 252L, 295L)));
     TimerSummaryHtmlReport htmlReport = new TimerSummaryHtmlReport(summary);
     
     FileWriter fileWriter = new FileWriter("target/sample-report-output-1.html");
     fileWriter.append(htmlReport.render());
     fileWriter.close();
   }
 
   @Test
   public void notSoMuchATestAsASampleGenerator2() throws Exception {
     TimerSummary nestedSummary1 = new TimerSummary("timer01", 5L, 110L, Lists.newArrayList(new TimerSummary("timer001", 5L, 100L, Lists.<TimerSummary>newArrayList(new TimerSummary("timer0001" ,60L, 70L)))));
     TimerSummary nestedSummary2 = new TimerSummary("timer02", 110L, 150L);
     TimerSummary summary = new TimerSummary("timer0", 0L, 400L, Lists.newArrayList(nestedSummary1, nestedSummary2));
     TimerSummaryHtmlReport htmlReport = new TimerSummaryHtmlReport(summary);
     
     FileWriter fileWriter = new FileWriter("target/sample-report-output-2.html");
     fileWriter.append(htmlReport.render());
     fileWriter.close();
   }
 }

 package com.jawspeak.stacktimer;
 
 public class TimerSummaryHtmlReport {
 
   private static final int BAR_LINE_HEIGHT = 20;
   private final TimerSummary timerSummary;
   private StringBuilder sb = new StringBuilder();
 
   public TimerSummaryHtmlReport(TimerSummary timerSummary) {
     this.timerSummary = timerSummary;
   }
   
   /** Renders an html fragment suitable for including on an html page for reporting timings */
   public String render() {
     renderHeader();
     renderChart();
     return sb.toString();
   }
 
   private void renderHeader() {
     sb.append("<style>\n" + 
     		"#chart-stats {\n" + 
     		"    position: relative;\n" + 
     		"\n" + 
     		"}\n" + 
     		"div.timeline {\n" + 
     		"    position: absolute;\n" + 
     		"    line-height: "+BAR_LINE_HEIGHT+"px;\n" +
     		"    border: thin grey solid;\n" +
     		"    background-color: #eee;\n" + 
     		"    margin: 0;\n" +
     		"    padding: 0;\n" + 
     		"    font-size: 10px;\n" + 
     		"}\n" + 
     		"</style>\n");
   }
   
   private void renderChart() {
     sb.append("Server Response Time For Request\n<div id=\"chart-stats\">\n");    
     renderTimelineSummary(0, timerSummary);
     sb.append("</div>\n");
   }
 
   private void renderTimelineSummary(int nestingLevel, TimerSummary summary) { 
     long durationMillis = summary.getStopMillis() - summary.getStartMillis();
     int scaleFactor = 1;
     sb.append("<div class=\"timeline\" style=\"left: ").append(summary.getStartMillis() * scaleFactor)
       .append("px; top: ").append(nestingLevel * BAR_LINE_HEIGHT)
       .append("px; width: ").append(durationMillis * scaleFactor)
       .append("px;\" title=\"[").append(summary.getStartMillis()).append("-").append(summary.getStopMillis()).append("ms] ").append(durationMillis).append("ms\">")
       .append(summary.getName()).append("</div>");
     
    nestingLevel++;
     for (TimerSummary nestedSummary : summary.getNestedSummaries()) {
      renderTimelineSummary(nestingLevel, nestedSummary);
     }
   }
 
 }

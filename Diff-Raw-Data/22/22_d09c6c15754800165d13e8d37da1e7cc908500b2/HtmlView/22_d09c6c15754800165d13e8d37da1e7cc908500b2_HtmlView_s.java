 /*******************************************************************************
  * Copyright (c) 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.performance.graph;
 
 import java.io.BufferedOutputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 
 import junit.framework.AssertionFailedError;
 
 import org.eclipse.test.internal.performance.PerformanceTestPlugin;
 import org.eclipse.test.internal.performance.data.Dim;
 import org.eclipse.test.internal.performance.db.DB;
 import org.eclipse.test.internal.performance.db.Scenario;
 import org.eclipse.test.internal.performance.db.SummaryEntry;
 import org.eclipse.test.internal.performance.db.TimeSeries;
 import org.eclipse.test.internal.performance.db.Variations;
 
 public class HtmlView {
 
     public static void main(String[] args) {
 
         String[] bgColors = { "#DDDDDD", "#EEEEEE" };
         String reference = args[0];
         String resultsFolder = args[1];
         String config = args[2];
         String jvm=args[3];
         String outFile = null;
         PrintStream ps = null;
         // get all Scenarios
         Dim[] qd = null; // new Dim[] { InternalDimensions.CPU_TIME };
         Variations variations = new Variations();
         variations.put(PerformanceTestPlugin.CONFIG, config);
         variations.put(PerformanceTestPlugin.BUILD, "%");
         variations.put("jvm",jvm);
         Scenario[] scenarios = DB.queryScenarios(variations, "org.eclipse%", PerformanceTestPlugin.BUILD, qd);
 
         new Grapher(scenarios,resultsFolder+"/graphs",reference);
 
         for (int s = 0; s < scenarios.length; s++) {
             Scenario t = scenarios[s];
             outFile = resultsFolder + "/" + t.getScenarioName().replace('#', '.').replace(':', '_').replace('\\', '_') + ".html";
             if (outFile != null) {
                 try {
                     ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile)));
                 } catch (FileNotFoundException e) {
                     System.err.println("can't create output file" + outFile); //$NON-NLS-1$
                 }
             }
             if (ps == null)
                 ps = System.out;
             ps.println("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
             ps.println("<link rel=\"stylesheet\" href=\"../../default_style.css\" type=\"text/css\">");
             ps.println("<title>Eclipse Performance Data</title></head>"); //$NON-NLS-1$
             ps.println("<body><h4>Scenario: " + t.getScenarioName() + "</h4><br>"); //$NON-NLS-1$ //$NON-NLS-2$
             ps.println("<table>"); //$NON-NLS-1$ //$NON-NLS-2$
 
             String[] timeSeriesLabels = t.getTimeSeriesLabels();
             ps.println("<tr><td>Builds:</td>"); //$NON-NLS-1$
             Dim[] dimensions = t.getDimensions();
             try {
 
                 for (int i = 0; i < dimensions.length; i++) {
                     Dim dim = dimensions[i];
                     ps.println("<td><a href=\"graphs/" + t.getScenarioName().replace('#', '.').replace(':', '_').replace('\\', '_') + "_" + dim.getName() + ".html\">" + dim.getName() + "</a></td>");
                 }
                 ps.print("</tr>");
                 boolean referencePrinted=false;
                 for (int j = timeSeriesLabels.length-1; j > -1; j--) {
                     int underScoreIndex = timeSeriesLabels[j].indexOf('_');
                     timeSeriesLabels[j] = (underScoreIndex == -1) ? timeSeriesLabels[j] : timeSeriesLabels[j].substring(0, underScoreIndex);
                     ps.println("<tr bgcolor=" + bgColors[(j + 3) % 2] + "><td>" + timeSeriesLabels[j] + "</td>");
                        
                        for (int i = 0; i < dimensions.length; i++) {
                        Dim dim = dimensions[i];
                        TimeSeries ts = t.getTimeSeries(dim);
                        String stddev = ""; //$NON-NLS-1$
                        double stddev2 = ts.getStddev(j);
                        if (stddev2 != 0.0)
                            stddev = " [" + dim.getDisplayValue(ts.getStddev(j)) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
                        ps.println("<td>" + dim.getDisplayValue(ts.getValue(j)) + stddev + "</td>");
                     }
                     
                     ps.println("</tr>");
 
                 }
                 ps.println();
                 ps.println("</font></table><br><br>");
                 ps.println("</body></html>"); //$NON-NLS-1$
                 if (ps != System.out)
                     ps.close();
 
             } catch (AssertionFailedError e) {
                 e.printStackTrace();
                 continue;
             }
         }
     }
 }

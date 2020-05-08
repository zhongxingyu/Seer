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
 package org.eclipse.test.performance.ui;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import junit.framework.AssertionFailedError;
 
 import org.eclipse.test.internal.performance.data.Dim;
 import org.eclipse.test.internal.performance.db.Scenario;
 import org.eclipse.test.internal.performance.db.TimeSeries;
 
 public class ScenarioResults {
 	private Scenario[] scenarios;
 
 	private String reference;
 
 	private String resultsFolder;
 
 	private String configName;
 
 	private String current;
 	
 	private ArrayList pointsOfInterest;
 	
 	/**
 	 * Summary of results for a scenario for a given build compared to a reference.
 	 * @param scenarios - the array of Scenario objects for which to generate results.
 	 * @param reference - the reference build ID
 	 * @param resultsFolder - the output directory.
 	 * @param configName - the config for which to generate results.
 	 * @param current - the current buildID
 	 * @param configDescriptor - a ConfigDescriptor object.
 	 * @param pointsOfInterest - an ArrayList of buildId's to highlight on line graphs.
 	 */
 	public ScenarioResults(Scenario[] scenarios, String reference,
 			String resultsFolder, String configName, String current, Utils.ConfigDescriptor configDescriptor,ArrayList pointsOfInterest) {
 		this.scenarios = scenarios;
 		this.reference = reference;
 		this.resultsFolder = resultsFolder;
 		this.configName = configName;
 
 		this.pointsOfInterest=pointsOfInterest;
 		if (configDescriptor!=null){
 			this.configName=configDescriptor.description;
 			this.resultsFolder=configDescriptor.outputDir;
 		}
 		this.current = current;
 		run();
 	}
 
 	private void run() {
 
 		String[] bgColors = { "#DDDDDD", "#EEEEEE" };
 		String outFile = null;
 		PrintStream ps = null;
 
 		for (int s = 0; s < scenarios.length; s++) {
 			ArrayList pointsOfInterest=new ArrayList();
 			Scenario t = scenarios[s];
 			
 			//get latest points of interest matching
 			if (this.pointsOfInterest!=null){
 			Iterator iterator = this.pointsOfInterest.iterator();
 				while (iterator.hasNext()) {
 					String match = getMostRecentMatchingBuildID(t, iterator
 							.next().toString());
 					if (match != null)
 						pointsOfInterest.add(match);
 				}
 			}
 			
 			int []buildNameIndeces={-1,-1};
 			buildNameIndeces[0]=Utils.getBuildNameIndex(t.getTimeSeriesLabels(),current);
 			buildNameIndeces[1]=Utils.getBuildNameIndex(t.getTimeSeriesLabels(),reference);
 			//don't produce result if none exists for current build
 			if (Utils.getBuildNameIndex(t.getTimeSeriesLabels(),current)==-1) {
 				continue;
 			} 
 			
 
 			String scenarioFileName=t.getScenarioName().replace('#', '.').replace(':', '_')
 			.replace('\\', '_');
 			outFile = resultsFolder
 					+ "/"
 					+ scenarioFileName + ".html";
 			if (outFile != null) {
 				try {
 					new File(outFile).getParentFile().mkdirs();
 					ps = new PrintStream(new BufferedOutputStream(
 							new FileOutputStream(outFile)));
 				} catch (FileNotFoundException e) {
 					System.err.println("can't create output file" + outFile); //$NON-NLS-1$
 				}
 			}
 			if (ps == null)
 				ps = System.out;
 			ps.println(Utils.HTML_OPEN);
 			ps.println(Utils.HTML_DEFAULT_CSS);
 			ps.println("<title>"+t.getScenarioName() + "("+configName+")"+"</title></head>"); //$NON-NLS-1$
 			ps.println("<h4>Scenario: " + t.getScenarioName() + " ("+configName+")</h4><br>"); //$NON-NLS-1$ //$NON-NLS-2$
 			ps.println("<b>Click measurement name to view line graph of measured values over builds. " +
 					"Magenta, black and yellow dots are used to denote release or milestone, integration, and nightly builds, respectively.</b><br><br>");
 					
 			ps.println("<table border=\"1\">"); //$NON-NLS-1$ //$NON-NLS-2$
 
 			Dim[] dimensions = filteredDimensions( t.getDimensions());
 			try {
 				ps.println("<tr><td>Build Id</td>"); //$NON-NLS-1$
 				for (int i = 0; i < dimensions.length; i++) {
 					Dim dim = dimensions[i];
 					String dimName=dim.getName();
 					ps.println("<td><a href=\"#"+ configName+"_"+scenarioFileName+"_"+ dimName +"\">" + dimName
 							+ "</a></td>");
 				}
 				ps.print("</tr>\n");
 
 
 				//store current and reference values for diff later
 				double [][] diffValues=new double [2][dimensions.length];
 				//to determine if diff is possible
 				boolean [] refValueExistance=new boolean[dimensions.length];
 				
 				for (int j = 0; j < 2; j++) {
 					String referenceIndicator = (j == 1) ? "(reference)" : "";
 
 					String buildName = ((buildNameIndeces[j] == -1) ? "n/a" : t
 							.getTimeSeriesLabels()[buildNameIndeces[j]])
 							+ referenceIndicator;
 					ps.print("<tr><td>" + buildName + "</td>");
 										
 					for (int i = 0; i < dimensions.length; i++) {
 						Dim dim = dimensions[i];
 						String dimName=dim.getName();
 						
 						TimeSeries ts = t.getTimeSeries(dim);
 						if (j==1&&buildNameIndeces[j]!=-1)
 							refValueExistance[i]=true;
 
 						if (buildNameIndeces[j] != -1)
 							diffValues[j][i]=ts.getValue(buildNameIndeces[j]);
 						
 						String displayValue = (buildNameIndeces[j] == -1) ? "n/a"
 								: dim.getDisplayValue(ts
 										.getValue(buildNameIndeces[j]));
 						String stddev = (buildNameIndeces[j] == -1) ? "0" : dim
 								.getDisplayValue(ts
 										.getStddev(buildNameIndeces[j]));
 
 						if (stddev.startsWith("0 ") || stddev.equals("0"))
 							ps.println("<td>" + displayValue + "</td>");
 						else
 							ps.println("<td>" + displayValue + " [" + stddev
 									+ "]" + "</td>");
 					}
 					ps.print("</tr>");
 				}
 				//get diffs and print results
 				ps.println(getDiffs(diffValues,dimensions,refValueExistance));
 				ps.println();
 				ps.println("</font></table>");
 				ps.println("*Delta values in red and green indicate degradation > 10% and improvement > 10%,respectively.<br><br>");
 				ps.println("<br><hr>\n");
 			
 				// print image maps of historical
 				for (int i = 0; i < dimensions.length; i++) {
 					Dim dim = dimensions[i];
 					String dimName=dim.getName();
 					
 					LineGraph lg=Utils.getLineGraph(t,dim.getName(),reference,current,pointsOfInterest);
 					String lgImg=resultsFolder+"/graphs/"+scenarioFileName+"_"+dimName+".gif";
 					Utils.printLineGraphGif(lg,lgImg);
 					ps.println("<br><br><a name=\""+configName+"_"+scenarioFileName+"_"+ dimName+"\"></a>");
 					ps.println("<br><b>"+dimName+"</b><br>");
 					ps.println(Utils.getDimensionDescription(dimName)+"<br><br>\n");
 					ps.println(Utils.getImageMap(lg,"graphs/"+scenarioFileName+"_"+dimName+".gif"));
 					// ps.println(new
 					// DimensionHistories(t,resultsFolder+"/graphs",reference,configName).getImageMap(dim));
 				}
 				ps.println("<br><br></body>");
 				ps.println(Utils.HTML_CLOSE); //$NON-NLS-1$
 				if (ps != System.out)
 					ps.close();
 
 			} catch (AssertionFailedError e) {
 				e.printStackTrace();
 				continue;
 			}
 		}
 	}
 
 	private int[] getBuildNameIndeces(String[] timeSeriesLabels,
 			String current, String baseline) {
 		int[] indeces = { -1, -1 };
 		for (int i = 0; i < timeSeriesLabels.length; i++) {
 			String timeSeriesLabel = timeSeriesLabels[i];
 			if (timeSeriesLabel.equals(current))
 				indeces[0] = i;
 			if (timeSeriesLabel.equals(baseline))
 				indeces[1] = i;
 		}
 		return indeces;
 	}
 	
 	private String getMostRecentMatchingBuildID(Scenario scenario,String buildIdPrefix){
 		String [] buildIds = scenario.getTimeSeriesLabels();
 		for (int i=buildIds.length-1;i>-1;i--){
 			if (buildIds[i].startsWith(buildIdPrefix))
 				return buildIds[i];
 		}
 		return null;
 	}
 	
 	private String getDiffs(double [][]values,Dim[] dimensions, boolean []refValueExistance){
 		String diffRow="<tr><td>*Delta</td>";
 		for (int j=0;j<dimensions.length;j++){
 			Dim dim = dimensions[j];
 			String dimName=dim.getName();
 
 			double diffValue=values[0][j]-values[1][j];
 			double diffPercentage=0;
 			if (values[1][j]!=0)
				diffPercentage=((int)(((diffValue/values[1][j])*1000)))/10.0;
 			String diffDisplayValue=dim.getDisplayValue(diffValue);
 			//green
 			String fontColor="";
 			if ((diffPercentage<-10&&!dim.largerIsBetter())||(diffPercentage>10&&dim.largerIsBetter()))
 				fontColor="#006600";
 			if ((diffPercentage<-10&&dim.largerIsBetter())||(diffPercentage>10&&!dim.largerIsBetter()))
 				fontColor="#FF0000";
 
 			diffPercentage=Math.abs(diffPercentage);
 			String percentage=(diffPercentage==0)?"":"<br>"+diffPercentage+" %";
 
 			if (diffPercentage>10 || diffPercentage<-10){
 				diffRow=diffRow.concat("<td><FONT COLOR=\""+fontColor+"\"><b>"+diffDisplayValue+percentage+"</b></FONT></td>");
 			} else if(refValueExistance[j]){
 				diffRow=diffRow.concat("<td>"+diffDisplayValue+percentage+"</td>");
 			}else{
 				diffRow=diffRow.concat("<td>n/a</td>");
 			}
 		}
 		diffRow=diffRow.concat("</tr>");
 		return diffRow;
 	}
 	
 	private Dim [] filteredDimensions(Dim[] dimensions){
 		ArrayList list = new ArrayList();
 		ArrayList filtered=new ArrayList();
 		list.add(0,"Elapsed Process");
 		list.add(1,"CPU Time");
 		list.add(2,"Kernel time");
 		list.add(3,"Used Java Heap");
 		list.add(4,"Committed");
 		list.add(5,"Data Size");
 		list.add(6,"Library Size");
 		list.add(7,"GDI Objects");
 		list.add(8,"Text Size");
 		list.add(9,"Page Faults");
 		list.add(10,"Hard Page Faults");
 		list.add(11,"Soft Page Faults");
 		list.add(11,"Invocation Count");
 			
 		for (int i=0;i<dimensions.length;i++){
 			String dimName=dimensions[i].getName();
 			if (list.contains(dimName))
 				list.set(list.indexOf(dimName),dimensions[i]);
 		}
 		Iterator iterator=list.iterator();
 		while (iterator.hasNext()){
 			Object tmp=iterator.next();
 			try {
 			if ((Dim)tmp instanceof Dim)
 				filtered.add(tmp);
 			}catch (ClassCastException e){
 				//silently ignore
 			}
 		}
 		return (Dim [])filtered.toArray(new Dim [filtered.size()]);
 	}
 
 
 }

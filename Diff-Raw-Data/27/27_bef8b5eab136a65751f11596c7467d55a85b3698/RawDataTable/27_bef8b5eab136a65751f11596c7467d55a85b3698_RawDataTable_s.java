 package org.eclipse.test.performance.ui;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.eclipse.test.internal.performance.data.Dim;
 import org.eclipse.test.internal.performance.db.Scenario;
 import org.eclipse.test.internal.performance.db.TimeSeries;
 
 public class RawDataTable {
 
 	private Scenario scenario;
 	private String currentBuild;
 	private Dim[] dimensions;
 	private Hashtable buildIDTable;
 	private Hashtable derivedValues;
 	private String TOTAL="total";
 	private String SAMPLECOUNT="n";
 	private String MEAN="mean";
 	private String STDDEV="stddev";
 	private String CV="cv";
 	private ArrayList buildIDPatterns;
 		
 	
 	public RawDataTable(Scenario scenario, Dim[] dimensions, ArrayList buildIDPatterns, String currentBuild) {
 		this.scenario = scenario;
 		this.dimensions = dimensions;
 		this.buildIDPatterns=buildIDPatterns;
 		this.currentBuild=currentBuild;
 		buildIDTable = new Hashtable();
 		derivedValues=new Hashtable();
 		fill();
 	}
 	
 	public RawDataTable(Scenario scenario, Dim[] dimensions, String buildIDPrefix,String currentBuild) {
 		buildIDPatterns=new ArrayList();
 		buildIDPatterns.add(buildIDPrefix);
 		this.currentBuild=currentBuild;
 		this.scenario = scenario;
 		this.dimensions = dimensions;
 		buildIDTable = new Hashtable();
 		derivedValues=new Hashtable();
 		fill();
 	}
 
 	private void fill() {
 
 		for (int i = 0; i < dimensions.length; i++) {
 			TimeSeries ts = scenario.getTimeSeries(dimensions[i]);
 			double total = 0.0;
 			int samplesCount=0;
 			for (int j = 0; j < ts.getLength(); j++) {
 				String buildID = ts.getLabel(j);
 				double value = 0.0;
 				boolean buildIDmatches=false;
 				Iterator iterator=buildIDPatterns.iterator();
 				while (iterator.hasNext()){
					if (buildID.startsWith(iterator.next().toString())){
 							buildIDmatches=true;
 							break;
 					}
 				}
 				if (!buildIDmatches)
 					continue;
 				
 				Hashtable samples=(Hashtable)buildIDTable.get(buildID);
 				if (samples==null)
 					samples=new Hashtable();
 				value = ts.getValue(j);
 				
 				// store result for build
 				samples.put(dimensions[i].getName(),new Double(value));
 				buildIDTable.put(buildID,samples);
 
 				//keep count of samples added and total value
 				total+=value;
 				samplesCount++;
 				
 				//quit after current build
 				if (buildID.equals(currentBuild))
 					break;
 			}
 					
 			double mean = total / samplesCount;
 			double squaredValues = 0.0;
 		
 			String[] buildIDs=(String[])buildIDTable.keySet().toArray(new String[buildIDTable.size()]);
 			for (int j = 0; j < buildIDs.length; j++) {
 				String buildID = buildIDs[j];
 				Hashtable storedValues=(Hashtable)buildIDTable.get(buildID);
 				double value = ((Double) (storedValues.get(dimensions[i].getName()))).doubleValue();
 				double squaredValue = Math.pow(value - mean, 2);
 				squaredValues += squaredValue;
 			}
 
 			double standardDeviation = Math.sqrt((squaredValues / (samplesCount - 1)));
 			double coefficientOfVariation = Math.round(((standardDeviation) / mean) * 100 * 100) / 100;
 			
 			if (coefficientOfVariation>10&&dimensions[i].getName().startsWith("Elapsed"))
 				System.out.println(scenario.getScenarioName()+": "+" "+coefficientOfVariation);
 			
 			//store derived values
 			Hashtable calculatedValuesForDimension=new Hashtable();		
 			calculatedValuesForDimension.put(TOTAL,dimensions[i].getDisplayValue(total));
 			calculatedValuesForDimension.put(SAMPLECOUNT,new Integer(samplesCount));
 			calculatedValuesForDimension.put(MEAN, dimensions[i].getDisplayValue(mean));
 			calculatedValuesForDimension.put(STDDEV, dimensions[i].getDisplayValue(standardDeviation));
 			calculatedValuesForDimension.put(CV, coefficientOfVariation+"%");
 			derivedValues.put(dimensions[i].getName(),calculatedValuesForDimension);
 			
 		}
 	}
 
 	public String toHtmlString(){
 		return "<table border=\"1\">"+htmlSummary()+htmlDetails()+"</table>\n";
 	}
 	
 	private String[] sortBuildIDsByDate(String[] buildIDs) {
 		Hashtable tmp = new Hashtable();
 		String[] result = new String[buildIDs.length];
 
 		for (int i = 0; i < buildIDs.length; i++) {
 			tmp.put("" + Utils.getDateFromBuildID(buildIDs[i], true), buildIDs[i]);
 		}
 		String[] dates = (String[]) tmp.keySet().toArray(new String[tmp.size()]);
 		Arrays.sort(dates);
 		for (int i = 0; i < dates.length; i++) {
 			result[i] = tmp.get(dates[i]).toString();
 		}
 
 		return result;
 	}
 
 	private String htmlDimensionColumnHeaders() {
 		String result="";
 		for (int i=0;i<dimensions.length;i++){
 			result = result.concat("<td><b>"+dimensions[i].getName()+"</b></td>");
 		}
 		return result;
 	}
 
 	private String htmlDetails() {	
 		String result="<tr><td><b>Build ID</b></td>";
 		result = result.concat(htmlDimensionColumnHeaders());
 		result = result.concat("</tr>\n");
 	
 		Set sampleKeys = buildIDTable.keySet();
 		String[] buildIDs = sortBuildIDsByDate((String[]) sampleKeys.toArray(new String[buildIDTable.size()]));
 
 		for (int i = buildIDs.length; i > 0; i--) {
 			String buildID = buildIDs[i-1];
 			if (buildID == null)
 				continue;
 			
 			Hashtable values=(Hashtable)buildIDTable.get(buildID);
 			if (values==null)
 				continue;
 			
 			result = result.concat("<tr><td>" + buildID + "</td>\n");
 
 			for (int j=0;j<dimensions.length;j++){
 				double value = 0.0;
 				String dimensionName=dimensions[j].getName();
 				value = ((Double) (values.get(dimensionName))).doubleValue();
 				result = result.concat("<td title=\""+dimensionName+"\">" + dimensions[j].getDisplayValue(value) + "</td>");
 			}	
 		}
 		result=result.concat("</tr>\n");
 		return result;
 	}
 
 	private String htmlSummary() {
 		if (derivedValues==null)
 			return "";
 		//print summary values
 		//print totals
 		String result="<tr><td><b>Stats</b></td>";
 		result = result.concat(htmlDimensionColumnHeaders());
 		result = result.concat("</tr><tr>\n");
 		
 		//print sample counts
 		
 		result = result.concat("<td>#BUILDS SAMPLED</td>\n");
 		result = result.concat(htmSummaryRow(SAMPLECOUNT));
 
 		//print averages
 		result = result.concat("</tr><tr><td>MEAN</td>\n");
 		result = result.concat(htmSummaryRow(MEAN));
 		
 		//print standard deviation
 		result = result.concat("</tr><tr><td>STD DEV</td>\n");
 		result = result.concat(htmSummaryRow(STDDEV));
 		
 		//print coefficient of variation
 		result = result.concat("</tr><tr><td>CV</td>\n");
 		result = result.concat(htmSummaryRow(CV));
 
 		result = result.concat("</tr><tr>\n");
 		for (int i=0;i<dimensions.length+1;i++){
 			result=result.concat("<td>&nbsp;</td>");
 		}
 		result=result.concat("</tr>\n");
 		
 		return result;
 	}
 
 	private String htmSummaryRow(String summaryType) {
 		String result="";
 		Hashtable tmp;
 		for (int j=0;j<dimensions.length;j++){
 			String dimensionName=dimensions[j].getName();
 			tmp=(Hashtable)(derivedValues.get(dimensionName));
 			String displayValue=tmp.get(summaryType).toString();
 			if (summaryType.equals(CV)){
 				displayValue=displayValue.substring(0,displayValue.length()-1);
 				double value=Double.valueOf(displayValue).doubleValue();
 				if (value>10&&value<20)
 					result = result.concat("<td bgcolor=\"yellow\" title=\""+dimensionName+"\">"+displayValue+"%</td>");
 				else if (value>=20)
 					result = result.concat("<td bgcolor=\"FF9900\" title=\""+dimensionName+"\">"+displayValue+"%</td>");
 				else
 					result = result.concat("<td title=\""+dimensionName+"\">"+displayValue+"%</td>");
 			} else
 				result= result.concat("<td title=\""+dimensionName+"\">"+displayValue+"</td>");
 		}
 		return result;
 	}
 
 	public String getCV() {
 		Hashtable tmp;
 		String dimensionName="Elapsed Process";
 		tmp=(Hashtable)(derivedValues.get(dimensionName));
 		if (tmp==null)
 			return "n/a";
 		if (tmp.get(CV)!=null)
 		return tmp.get(CV).toString();
 		return "n/a";
 		
 	}
 }

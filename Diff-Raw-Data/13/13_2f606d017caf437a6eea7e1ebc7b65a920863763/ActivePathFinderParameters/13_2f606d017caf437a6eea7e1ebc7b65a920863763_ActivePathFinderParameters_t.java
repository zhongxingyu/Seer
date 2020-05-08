 // ActivePathFinderParameters.java
 //---------------------------------------------------------------------------------------
 // $Revision: 11534 $   
 // $Date: 2007-09-06 14:26:33 -0700 (Thu, 06 Sep 2007) $ 
 // $Author: mes $
 //-----------------------------------------------------------------------------------
 package csplugins.jActiveModules.data;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Enumeration;
 
 import org.cytoscape.model.CyNode;
 import org.cytoscape.property.CyProperty;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyTable;
 import org.cytoscape.model.CyColumn;
 import csplugins.jActiveModules.util.SelectUtil;
 import org.cytoscape.model.CyTableUtil;
 
 //---------------------------------------------------------------------------------------
 public class ActivePathFinderParameters {
 
 	private static final Logger logger = LoggerFactory.getLogger(ActivePathFinderParameters.class);
 	
 	double initialTemperature = 1.0;
 	double finalTemperature = 0.01;
 	double hubAdjustment = 0;
 	double overlapThreshold = 0.8;
 	int totalIterations = 2500;
     int randomIterations = 100;
 	int numberOfPaths = 5;
 	int displayInterval = 500;
 	int pseudoRandomSeed = Math.abs((int) System.currentTimeMillis());
 	int minHubSize = 0;
 	int randomSeed = pseudoRandomSeed;
 	int searchDepth = 1;
 	int maxDepth = 2;
 	boolean enableMaxDepth = true;
 	boolean toQuench = true;
 	boolean toUseMCFile = false;
 	boolean mcBoolean = true;
 	String mcFileName = "";
 	boolean isDefault = true;
 	boolean regionalBoolean = true;
 	boolean searchFromNodes = false;
 	boolean exit = false;
 	int maxThreads = 1;
 	boolean greedySearch = true;
     boolean run = false;
     boolean save = false;
     boolean randomizeExpression = false;
     String outputFile = "output.txt";
    	List<String> expressionAttrs = new ArrayList<String>();
    	
     List<String> possibleExpressionAttrs = new ArrayList<String>();
     	
     List<Boolean> switchSigs = new ArrayList<Boolean>();
     List<String> scalingMethods = new ArrayList<String>();
     private CyNetwork network;
     
 	// ---------------------------------------------------------------------------------------
     public ActivePathFinderParameters(CyProperty<?> cytoscapePropertiesServiceRef){
     	Properties properties = (Properties) cytoscapePropertiesServiceRef.getProperties();
 	for (Enumeration e = properties.propertyNames() ; e.hasMoreElements() ;) {
 	    String name = (String)e.nextElement();
 	    if(name.startsWith("AP")){
 		String property = properties.getProperty(name);
 		if(name.endsWith("initialTemperature")){
 		    initialTemperature = Double.valueOf(property);
 		}
 		else if(name.endsWith("finalTemperature")){
 		    finalTemperature = Double.valueOf(property);
 		}
 		else if(name.endsWith("hubAdjustment")){
 		    hubAdjustment = Double.valueOf(property);
 		}
 		else if(name.endsWith("totalIterations")){
 		    totalIterations = Integer.valueOf(property);
 		}
 		else if(name.endsWith("numberPaths")){
 		    numberOfPaths = Integer.valueOf(property);
 		}
 		else if(name.endsWith("displayInterval")){
 		    displayInterval = Integer.valueOf(property);
 		}
 		else if(name.endsWith("hubSize")){
 		    minHubSize = Integer.valueOf(property);
 		}
 		else if(name.endsWith("randomSeed")){
 		    randomSeed = Integer.valueOf(property);
 		}
 		else if(name.endsWith("searchDepth")){
 		    searchDepth = Integer.valueOf(property);
 		}
 		else if(name.endsWith("maxDepth")){
 		    maxDepth = Integer.valueOf(property);
 		}
 		else if(name.endsWith("quench")){
 		    toQuench = Boolean.valueOf(property);
 		}
 		else if(name.endsWith("mcBoolean")){
 		    mcBoolean = Boolean.valueOf(property);
 		}
 		else if(name.endsWith("mcFileName")){
 		    mcFileName = property;
 		}
 		else if(name.endsWith("regional")){
 		    regionalBoolean = Boolean.valueOf(property);
 		}
 		else if(name.endsWith("exit")){
 		    exit = Boolean.valueOf(property);
 		}
 		else if(name.endsWith("run")){
 		    run = Boolean.valueOf(property);
 		}
 		else if(name.endsWith("save")){
 		    save = Boolean.valueOf(property);
 		}
 		else if(name.endsWith("maxThreads")){
 		    maxThreads = Integer.valueOf(property);
 		}
 		else if(name.endsWith("anneal")){
 		    greedySearch = !Boolean.valueOf(property);
 		}
 		else if(name.endsWith("outputFile")){
 		    outputFile = property;
 		}
 		else if(name.endsWith("expression")){
 		    String [] splat = property.split(",");
 		    for(int idx=0;idx < splat.length; idx += 1){
 			expressionAttrs.add(splat[idx]);
 		    }
 		}
 		else if(name.endsWith("randomizeExpression")){
 		    randomizeExpression = Boolean.valueOf(property);
 		}
 		else if(name.endsWith("randomIterations")){
 		    randomIterations = Integer.valueOf(property);
 		}
 		else if(name.endsWith("overlapThreshold")){
 		    overlapThreshold = Double.valueOf(property);
 		}
 		else{
 		    logger.warn("Unrecognized option "+name);
 		}
 	    }
 
 	}
     }
 	public ActivePathFinderParameters() {
 	}
 
 	// ---------------------------------------------------------------------------------------
 	/*
 	 * public ActivePathFinderParameters (double initialTemperature, double
 	 * finalTemperature, double hubAdjustment, int totalIterations, int
 	 * numberOfPaths, int displayInterval, int minHubSize, int randomSeed, int
 	 * searchDepth, int maxDepth, boolean toQuench, boolean edgesNotNodes,
 	 * boolean toUseMCFile, boolean mcBoolean, String mcFileName, boolean
 	 * regionalBoolean, boolean searchFromNodes, int maxThreads, boolean exit,
 	 * boolean greedySearch)
 	 *  { this.initialTemperature = initialTemperature; this.finalTemperature =
 	 * finalTemperature; this.hubAdjustment = hubAdjustment;
 	 * this.totalIterations = totalIterations; this.numberOfPaths =
 	 * numberOfPaths; this.displayInterval = displayInterval; this.minHubSize =
 	 * minHubSize; this.randomSeed = randomSeed; this.searchDepth = searchDepth;
 	 * this.maxDepth = maxDepth; this.toQuench = toQuench; this.edgesNotNodes =
 	 * edgesNotNodes; this.toUseMCFile = toUseMCFile; this.mcBoolean =
 	 * mcBoolean; this.mcFileName = mcFileName; this.regionalBoolean =
 	 * regionalBoolean; this.searchFromNodes = searchFromNodes; this.isDefault =
 	 * false; this.maxThreads = maxThreads; this.exit = exit; this.greedySearch =
 	 * greedySearch; } // full ctor
 	 */
 	public ActivePathFinderParameters(ActivePathFinderParameters oldAPFP) {
 		setParams(oldAPFP);
 	} // copy ctor
 
 	public void setParams(ActivePathFinderParameters oldAPFP) {
 		this.initialTemperature = oldAPFP.getInitialTemperature();
 		this.finalTemperature = oldAPFP.getFinalTemperature();
 		this.hubAdjustment = oldAPFP.getHubAdjustment();
 		this.totalIterations = oldAPFP.getTotalIterations();
 		this.numberOfPaths = oldAPFP.getNumberOfPaths();
 		this.displayInterval = oldAPFP.getDisplayInterval();
 		this.minHubSize = oldAPFP.getMinHubSize();
 		this.randomSeed = oldAPFP.getRandomSeed();
 		this.searchDepth = oldAPFP.getSearchDepth();
 		this.maxDepth = oldAPFP.getMaxDepth();
 		this.toQuench = oldAPFP.getToQuench();
 		this.toUseMCFile = oldAPFP.getToUseMCFile();
 		this.mcBoolean = oldAPFP.getMCboolean();
 		this.mcFileName = oldAPFP.getMcFileName();
 		this.regionalBoolean = oldAPFP.getRegionalBoolean();
 		this.searchFromNodes = oldAPFP.searchFromNodes;
 		this.isDefault = false;
 		this.maxThreads = oldAPFP.getMaxThreads();
 		this.exit = oldAPFP.getExit();
 		this.save = oldAPFP.getSave();
 		this.outputFile = oldAPFP.getOutputFile();
 		this.greedySearch = oldAPFP.getGreedySearch();
 		this.enableMaxDepth = oldAPFP.getEnableMaxDepth();
 		this.run = oldAPFP.getRun();
 		this.randomizeExpression = oldAPFP.getRandomizeExpression();
 		this.randomIterations = oldAPFP.getRandomIterations();
 		this.overlapThreshold = oldAPFP.getOverlapThreshold();
 		setExpressionAttributes(oldAPFP.getExpressionAttributes());
 		
 	} // copy ctor
 
 	public boolean getRun(){
 		return this.run;
 	}
 	
 	public void setRun(boolean newValue){
 		this.run = newValue;
 	}
 	
 	public boolean getEnableMaxDepth(){
 		return this.enableMaxDepth;
 	}
 	
 	public void setEnableMaxDepth(boolean newValue){
 		this.enableMaxDepth = newValue;
 	}
 	
     	public void setExit(boolean flag) {
 		exit = flag;
 	}
 
 	public boolean getExit() {
 		return exit;
 	}
 
     public boolean getRandomizeExpression(){
 	return randomizeExpression;
     }
 
     public void setRandomizeExpression(boolean flag){
 	this.randomizeExpression = flag;
     }
         public boolean getSave(){
 	    return save;
 	}
 
         public void setSave(boolean flag){
 	    this.save = flag;
         }
 
         public String getOutputFile(){
 	    return outputFile;
         }
     
         public void setOutputFile(String file){
 	    this.outputFile = file;
         }
     
 	public boolean getSearchFromNodes() {
 		return searchFromNodes;
 	}
 
 	public void setSearchFromNodes(boolean newValue) {
 		searchFromNodes = newValue;
 		this.isDefault = false;
 	}
 
    	public boolean getRegionalBoolean() {
 		return regionalBoolean;
 	}
 	public void setRegionalBoolean(boolean newValue) {
 		regionalBoolean = newValue;
 		this.isDefault = false;
 	}
 	public boolean getToUseMCFile() {
 		return toUseMCFile;
 	}
 	public void setToUseMCFile(boolean newValue) {
 		toUseMCFile = newValue;
 		this.isDefault = false;
 	}
 	public boolean getMCboolean() {
 		return mcBoolean;
 	}
 	public void setMCboolean(boolean newValue) {
 		mcBoolean = newValue;
 		this.isDefault = false;
 	}
 	public String getMcFileName() {
 		return mcFileName;
 	}
 	public void setMcFileName(String newValue) {
 		mcFileName = newValue;
 		this.isDefault = false;
 	}
 	public double getInitialTemperature() {
 		return initialTemperature;
 	}
 	public void setInitialTemperature(double newValue) {
 		initialTemperature = newValue;
 		this.isDefault = false;
 	}
 	public double getFinalTemperature() {
 		return finalTemperature;
 	}
 	public void setFinalTemperature(double newValue) {
 		finalTemperature = newValue;
 		this.isDefault = false;
 	}
 	public double getHubAdjustment() {
 		return hubAdjustment;
 	}
 	public void setHubAdjustment(double newValue) {
 		hubAdjustment = newValue;
 		this.isDefault = false;
 	}
 
     public int getRandomIterations() {
 	return randomIterations;
     }
 
     public void setRandomIterations(int value) {
 	this.randomIterations = value;
 	    }
 	public int getTotalIterations() {
 		return totalIterations;
 	}
 	public void setTotalIterations(int newValue) {
 		totalIterations = newValue;
 		this.isDefault = false;
 	}
 	public int getNumberOfPaths() {
 		return numberOfPaths;
 	}
 	public void setNumberOfPaths(int newValue) {
 		numberOfPaths = newValue;
 		this.isDefault = false;
 	}
 	public int getDisplayInterval() {
 		return displayInterval;
 	}
 	public void setDisplayInterval(int newValue) {
 		displayInterval = newValue;
 		this.isDefault = false;
 	}
 	public int getRandomSeed() {
 		return randomSeed;
 	}
 	public void setRandomSeed(int newValue) {
 		randomSeed = newValue;
 		this.isDefault = false;
 	}
 	public int getSearchDepth() {
 		return searchDepth;
 	}
 	public void setSearchDepth(int newValue) {
 		if (newValue < 0) {
 			throw new IllegalArgumentException("Search depth must be > 0");
 		}
 		searchDepth = newValue;
 		this.isDefault = false;
 	}
 	public int getMaxDepth() {
 		return maxDepth;
 	}
 	public void setMaxDepth(int newValue) {
 		maxDepth = newValue;
 		this.isDefault = false;
 	}
 	public int getMinHubSize() {
 		return minHubSize;
 	}
 	public void setMinHubSize(int newValue) {
 		minHubSize = newValue;
 		this.isDefault = false;
 	}
  
    	public boolean getToQuench() {
 		return toQuench;
 	}
 	public void setToQuench(boolean newValue) {
 		toQuench = newValue;
 		this.isDefault = false;
 	}
 
 	public boolean getGreedySearch() {
 		return greedySearch;
 	}
 	
 	public void setGreedySearch(boolean newValue){
 		this.greedySearch = newValue;
 	}
 
 	public int getMaxThreads() {
 		return maxThreads;
 	}
 
 	public void setMaxThreads(int maxThreads) {
 		this.maxThreads = maxThreads;
 	}
 
 	public double getOverlapThreshold() {
 		return overlapThreshold;
 	}
 
 	public void setOverlapThreshold(double newValue) {
 		if (newValue < 0 || newValue > 1) {
 			throw new IllegalArgumentException("Invalid overlap value: " + newValue);
 		}
 		this.overlapThreshold = newValue;
 	}
 
 	public void reloadExpressionAttributes() {
 
 		possibleExpressionAttrs.clear();
 
 		if (this.network == null){
 			return;
 		}
 		// find all of the double type parameters
 		CyTable nodeAttrs = this.network.getDefaultNodeTable();
		Object[] objs = CyTableUtil.getColumnNames(nodeAttrs).toArray();
		
		String[] names = new String[objs.length];//(String[])CyTableUtil.getColumnNames(nodeAttrs).toArray();
 
		for (int i=0; i<objs.length; i++){
			names[i] = (String)objs[i];
		}
		
		
 		for ( String name : names ) {
 			if ( nodeAttrs.getColumn(name).getType() == Float.class ||nodeAttrs.getColumn(name).getType() == Double.class) {
 
				List<Double>  values =nodeAttrs.getColumn(name).getValues(Double.class);
 				
 				if ( values == null ) 
 					continue; // no values have been defined for the attr yet
 
 				boolean isPValue = true;
 				for ( Object value : values ) {
 					double d = ((Double)value).doubleValue();
 					if ( d < 0 || d > 1 ) {
 						isPValue = false;
 						break;
 					}
 				}
 
 				if ( isPValue )
 					possibleExpressionAttrs.add(name);
 			}
 		}
 		Collections.sort(possibleExpressionAttrs);
 	}
 
 	public List<String> getPossibleExpressionAttributes() {
 		return possibleExpressionAttrs;
 	}
 
 	public List<String> getExpressionAttributes() {
 		return expressionAttrs;
 	}
 	
 	public void setExpressionAttributes(Collection<String> names) {
 		expressionAttrs.clear();
 		expressionAttrs.addAll(names);
 	}
 
 	//
 	public void setSwitchSigs(Collection<Boolean> switchSigs) {
 		this.switchSigs.clear();
 		this.switchSigs.addAll(switchSigs);
 	}
 
 	public void setScalingMethods(Collection<String> scalingMethods) {
 		this.scalingMethods.clear();
 		this.scalingMethods.addAll(scalingMethods);
 	}
 	
 	public List<Boolean> getSwitchSigs() {
 		return switchSigs;
 	}
 
 	public List<String> getScalingMethods() {
 		return scalingMethods;
 	}
 	//
 	
 	public void addExpressionAttribute(String name) {
 		if ( !expressionAttrs.contains(name) )
 			expressionAttrs.add(name);
 	}
 
 	public void removeExpressionAttribute(String name) {
 		expressionAttrs.remove(name); 
 	}
 
 	public String toString() {
 		StringBuffer sb = new StringBuffer();
 
 		sb.append("      initial temperature: " + initialTemperature + "\n");
 		sb.append("        final temperature: " + finalTemperature + "\n");
 		sb.append("           hub adjustment: " + hubAdjustment + "\n");
 		sb.append("         total iterations: " + totalIterations + "\n");
 		sb.append("          number of paths: " + numberOfPaths + "\n");
 		sb.append("         display interval: " + displayInterval + "\n");
 		sb.append("         minimum hub size: " + minHubSize + "\n");
 		sb.append("              random seed: " + randomSeed + "\n");
 		sb.append("                   quench: " + toQuench + "\n");
 		sb.append("            use MC at all: " + mcBoolean + "\n");
 		sb.append("                 MC file?: " + toUseMCFile + "\n");
 		sb.append("              MC filename: " + mcFileName + "\n");
 		sb.append("         regional scoring: " + regionalBoolean + "\n");
 		sb.append("             search depth: " + searchDepth + "\n");
 		sb.append("                max depth: " + maxDepth + "\n");
 		sb.append("        search from nodes: " + searchFromNodes + "\n");
 		sb.append("              max threads: " + maxThreads + "\n");
 		sb.append("        overlap threshold: " + overlapThreshold + "\n");
 		return sb.toString();
 
 	} // toString
 
 	public void setNetwork(CyNetwork network){
 		this.network = network;
 	}
 } // class ActivePathFinderParameters
 

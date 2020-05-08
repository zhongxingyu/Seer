 /*
  * Copyright (C) 2012 Red Hat, Inc.     
  * 
  * This copyrighted material is made available to anyone wishing to use, 
  * modify, copy, or redistribute it subject to the terms and conditions of the 
  * GNU General Public License v.2.
  * 
  * Authors: Jan Rusnacko (jrusnack at redhat dot com)
  */
 
 package com.redhat.engineering.jenkins.report.plugin.results;
 
 import hudson.matrix.AxisList;
 import hudson.matrix.Combination;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * This class holds information about which configuration should be present 
  * in report and which should not. After this class is fed to MatrixBuildTestResults,
  * we can fetch results from only certain subset of matrix runs (correspoding to 
  * build) controlled by this class.
  * 
  * @author Jan Rusnacko (jrusnack at redhat.com)
  */
 public class Filter {
     /**
      * Determines whether a configuration should be used in reports or not
      */
     Map<Combination, Boolean> configuration = new HashMap<Combination, Boolean>();
     String combinationFilter;
 
     private MatrixBuildTestResults owner;
     public String uuid;
     private AxisList axisList;
 
     public Filter(String uuid, AxisList axisList){
 	this.uuid = uuid;
 	this.axisList = axisList;
 	for(Combination c: axisList.list()){
 	    configuration.put(c, true);
 	}
     }
 
     public void setOwner(MatrixBuildTestResults owner){
 	this.owner = owner;
     }
     
     public MatrixBuildTestResults getOwner(){
 	return owner;
     }
     
     /**
      * Add a configuration to the filter, if already exists, it`s rewritten
      * 
      * @param config A String representing the
      *            {@link hudson.matrix.MatrixConfiguration} given as its
      *            {@link hudson.matrix.Combination}
      * @param include A boolean to determine whether to include the
      *            {@link hudson.model.Run} or not
      */
     public void setConfiguration(Combination combination, boolean include) {
 	this.configuration.put(combination, include);
     }
     
     /**
      * Remove a configuration from the filter
      * 
      * @param combination A {@link hudson.matrix.MatrixConfiguration} given
      *            as its {@link hudson.matrix.Combination}
      */
     public void removeConfiguration(Combination combination) {
 	configuration.remove(combination);
     }
     
     /**
      * Returns whether or not to include the {@link hudson.model.Run}
      * If the combination is not explicitly checked, the method returns true,
      * meaning the run will be included in report.
      * 
      * @param combination A {@link hudson.matrix.MatrixConfiguration} given
      *            as its {@link hudson.matrix.Combination}
      * @return A boolean determining whether or nor to include the
      *         {@link hudson.model.Run}
      */
     public boolean getConfiguration(Combination combination) {	
 	if(configuration.containsKey(combination)){    
 	    return configuration.get(combination);	
 	}
 	return true;
     }
     
     /**
      * Alias for {@getConfiguratiion}
      * 
      * @param combination
      * @return 
      */
     public boolean isIncluded(Combination combination){
 	return getConfiguration(combination);
     }
     
     
     private void rebuildConfiguration(){    
 	for(Combination c: configuration.keySet()){
            try{
                if(combinationFilter!= null && c.evalGroovyExpression(axisList, combinationFilter)){
                    configuration.put(c,true);
                } else{
                    configuration.put(c, false);
                }
            }catch(Exception e){
                // combination filter has axis that does not exist in axis list
                configuration.put(c, false);
            }
 	}
 	
     }
     
     public void addCombinationFilter(String combinationFilter){
 	this.combinationFilter = combinationFilter;
 	rebuildConfiguration();
     }
     
     public String getCombinationFilter(){
         return combinationFilter;
     }
     
     
     /**
      * Removes Groovy expression provided for filtering and resets combinations to
      * false
      */
     public void removeCombinationFilter(){
 	if(combinationFilter != null){
 	    combinationFilter = null;
 	}
 	resetCombinationsFalse();
     }
     
     /**
      * Set all combinations not to be included
      */
     public void resetCombinationsFalse(){
 	for(Combination c: configuration.keySet()){
 	    configuration.put(c, false);
 	}
     }
 }

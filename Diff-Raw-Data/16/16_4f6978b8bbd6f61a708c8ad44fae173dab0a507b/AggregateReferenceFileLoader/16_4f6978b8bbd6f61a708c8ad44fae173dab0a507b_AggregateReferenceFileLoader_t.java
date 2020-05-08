 /**
  * 
  */
 package org.eclipse.stem.ui.reports.views;
 
 /*******************************************************************************
  * Copyright (c) 2007, 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.eclipse.stem.ui.reports.Activator;
 
 
 /**
  * @author james
  *
  */
 public class AggregateReferenceFileLoader {
 	
 	private String dataFile="";
 	
 	
 	private static final String CSV_EXTENSION = ".csv";
 	private static final String TXT_EXTENSION = ".txt";
 	private List<Double> aggregateData = new ArrayList<Double>();
 	private Set<String> idSet = new HashSet<String>();
 	private File scenarioData = null;
 	private static final String[] ignoreKeys = {"time","iteration"};
 	
 	
 	/**
 	 * This class loads data from a folder containing a saved scenario run data.
 	 * The data in the folder is a set of csv files, one per location.
 	 * @param scenarioFile 
 	 * @throws AggregateFileInitializationException 
 	 * 
 	 */
 	public AggregateReferenceFileLoader(String scenarioFile) throws  AggregateFileInitializationException {
 		dataFile = scenarioFile;
 		scenarioData = new File(dataFile);
 		if(!isDataFile(scenarioData)) {
			throw new AggregateFileInitializationException("Not a valid data file, must be .csv or .txt "+dataFile);
 		} 
 		aggregateData = processFile();
 	}
 
 	
 
 	
 	
 	/**
 	 * test file name to see if it is a data file. Must have extension .txt or .csv
 	 * 
 	 * @param file
 	 * @return true if data file
 	 */
 	private boolean isDataFile(File file) {
 		boolean retVal = true;
 		String name = file.getName();
 		if((name.indexOf(TXT_EXTENSION) < 0) && (name.indexOf(CSV_EXTENSION) < 0)) retVal = false;
 		return retVal;
 	}
 
 	
 	
 	/**
 	 * process the input file
 	 * @return a map of scenario data keyed by location ID
 	 * @throws AggregateFileInitializationException 
 	 */
 	  public List<Double> processFile() throws AggregateFileInitializationException {
 		  
 		int maxTime = 0;
 		HashMap<String, List<Double>> data = new HashMap<String,List<Double>>();
 		try {
 	      String record;  
 	      String header;
 	      int recCount = 0;
 	      List<String> headerElements = new ArrayList<String>();
 	      FileInputStream fis = new FileInputStream(scenarioData); 
 	      BufferedReader d = new BufferedReader(new InputStreamReader(fis));
 	         
 	      //
 	      // Read the file header
 	      //
 	      if ( (header=d.readLine()) != null ) { 
 	          	
 		      StringTokenizer st = new StringTokenizer(header );
 		            
 		      while (st.hasMoreTokens()) {
 		    	  String val = st.nextToken(",");
 		    	  headerElements.add(val.trim());
 		      }
 	      } // read the header
 	      /////////////////////
 	          
 	      // set up the empty lists
 	      int numColumns = headerElements.size();
 	      for (int i = 0; i < numColumns; i ++) {
 	      	String key = headerElements.get(i);
 	      	 if(validate(key)) {
 	      		data.put(key, new ArrayList<Double>());
 		      }
 	      	
 	      }
 	          
 	      // Here we check the type of the data file
 	      // by checking the header elements
 	          
 	          
 	          
 	      //////////////////////
           // Read the data
           //
           while ( (record=d.readLine()) != null ) { 
              recCount++; 
             
              StringTokenizer st = new StringTokenizer(record );
              int tcount = 0;
 				while (st.hasMoreTokens()) {
 					String val = st.nextToken(",");
 					String key = headerElements.get(tcount);
 					if(validate(key)) {
 						idSet.add(key);
 						List<Double> dataList = data.get(key);
 						try {
 							Double dVal = new Double(val.trim());
 							dataList.add(dVal);
 							if (dataList.size() >= maxTime) maxTime = dataList.size();
 							data.put(key,dataList);
 						} catch(NumberFormatException nfe) {
 							data.remove(key);
 							idSet.remove(key);
 							Activator.logInformation("Not a valid number ("+val+") ... Removing key "+key, null);
 						}
 					}			
 					tcount ++;
 				}
 			} // while file has data
        } catch (IOException e) { 
           // catch io errors from FileInputStream or readLine()
       	 Activator.logError(" IOException error!", e);
       	 throw new AggregateFileInitializationException(e);
        }
        
        List<Double> aggregate = new ArrayList<Double>();
        for (int i = 0; i < maxTime; i ++) {
     	   aggregate.add(i, new Double(0.0));
        }
        // loop over all location ids
        Iterator<String> iter = idSet.iterator();
        while((iter!=null)&&(iter.hasNext())) {
     	   String key = iter.next();
     	   List<Double> dataList = data.get(key);
     	   for (int i = 0; i < maxTime; i ++) {
     		   double val = aggregate.get(i).doubleValue();
     		   val += dataList.get(i).doubleValue();
         	   aggregate.set(i,new Double(val));
            }
        }
      
        return aggregate;
 	  } // process file
 	  
 	  
 	  /**
 		 * process an incidence file
 		 * An incidence file does not contain SI data so it can not return 
 		 * a ReferenceScenarioDataInstance (it is not used to estimate parameters).
 		 * Instead this method returns the raw data in a map
 		 * 
 		 * @param fileName
 		 * @return a map of scenario data keyed by location ID
 		 * @throws AggregateFileInitializationException 
 		 */
 		  public Map<String, List<String>> processIncidenceFile(String fileName) throws AggregateFileInitializationException {
 			File file = new File(fileName);
 			HashMap<String, List<String>> data = new HashMap<String,List<String>>();
 			
 			try {
 		      String record;  
 		      String header;
 		      int recCount = 0;
 		      List<String> headerElements = new ArrayList<String>();
 		      FileInputStream fis = new FileInputStream(file); 
 		      BufferedReader d = new BufferedReader(new InputStreamReader(fis));
 		         
 		      //
 		      // Read the file header
 		      //
 		      if ( (header=d.readLine()) != null ) { 
 		          	
 			      StringTokenizer st = new StringTokenizer(header );
 			            
 			      while (st.hasMoreTokens()) {
 			    	  String val = st.nextToken(",");
 			    	  headerElements.add(val.trim());
 			      }
 		      } // read the header
 		      /////////////////////
 		          
 		      // set up the empty lists
 		      int numColumns = headerElements.size();
 		      for (int i = 0; i < numColumns; i ++) {
 		      	String key = headerElements.get(i);
 		      	data.put(key, new ArrayList<String>());
 		      }
 		          
 		      // Here we check the type of the data file
 		      // by checking the header elements
 		          
 		          
 		          
 		      
 	          //////////////////////
 	          // Read the data
 	          //
 	          while ( (record=d.readLine()) != null ) { 
 	             recCount++; 
 	             
 	             StringTokenizer st = new StringTokenizer(record );
 	             int tcount = 0;
 					while (st.hasMoreTokens()) {
 						String val = st.nextToken(",");
 						String key = headerElements.get(tcount);
 						(data.get(key)).add(val.trim());
 						tcount ++;
 					}
 				} // while file has data
 	       } catch (IOException e) { 
 	          // catch io errors from FileInputStream or readLine()
 	      	 Activator.logError(" IOException error!", e);
 	      	 throw new AggregateFileInitializationException(e);
 	       }
 	       return data;
 		  }
 		  
 		  /**
 		   * certain keys we must ignore
 		   * @param key
 		   * @return
 		   */
 		  private boolean validate(String key) {
 			  for (int i = 0; i < ignoreKeys.length; i ++) {
 				  String test = key.trim();
 				  if(test.equalsIgnoreCase(ignoreKeys[i])) return false;
 			  }
 			  return true;
 		  }
 		  
 	
 	/**
 	 * Indicate End-Of-File
 	 * 
 	 * @param buffer
 	 *            A buffer of diva data
 	 * 
 	 * @return True if we have reached End-Of-File
 	 */
 	static protected boolean EOF(String buffer) {
 		if (buffer == null || buffer.length() == 0)
 			return true;
 		return false;
 	}
 	
 	/**
 	 * 
 	 * @return aggregate data as array
 	 */
 	public List<Double> getAggregateData() {
 		return aggregateData;
 	}
 
 	/**
 	 * 
 	 * @return ids of locations that were aggregated
 	 */
 	public Set<String> getIdSet() {
 		return idSet;
 	}
 
 	
 
 } // AggregateReferenceFile

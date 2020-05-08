 /**
  * 
  */
 package org.eclipse.stem.analysis.util;
 
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
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.eclipse.stem.analysis.Activator;
 import org.eclipse.stem.analysis.AnalysisFactory;
 import org.eclipse.stem.analysis.DiseaseType;
 import org.eclipse.stem.analysis.ScenarioInitializationException;
 import org.eclipse.stem.analysis.impl.AnalysisFactoryImpl;
 import org.eclipse.stem.analysis.impl.ReferenceScenarioDataMapImpl;
 import org.eclipse.stem.analysis.impl.ReferenceScenarioDataMapImpl.ReferenceScenarioDataInstance;
 
 
 /**
 * Load a disease scenario 
 *
 */
 
 public class CSVscenarioLoader {
 	
 	private String directory="";
 	
 	
 	private static final String CSV_EXTENSION = ".csv";
 	
 	private List<File> diseaseData = new ArrayList<File>();
 	private final Map<String, String> runParameters = new HashMap<String, String>();
 	
 	/**
 	 * Name of all run parameter files (one per disease folder
 	 */
 	public static final String RUN_PARAMETER_FILE_NAME = "runparameters.csv";
 	public static final String RUN_PARAMETER_FILE_NAME_OLD = "runparamters.csv";
 		
 	private AnalysisFactory aFactory = new AnalysisFactoryImpl();
 	
 	/**
 	 * This class loads data from a folder containing a saved scenario run data for
 	 * a particular disease instance
 	 * The data in the folder is a set of csv files, one per disease state
 	 * 
 	 * @param diseaseDirectory Directory containing disease data 
 	 * @throws ScenarioInitializationException
 	 */
 	public CSVscenarioLoader(String diseaseDirectory) throws ScenarioInitializationException {
 		directory = diseaseDirectory;
 		diseaseData = this.getDataFiles();
 	}
 
 	/**
 	 * Empty constructor used when parsing a single file, e.g. an aggregate file
 	 */
 	public CSVscenarioLoader() {
 		// Empty
 	}
 	
 	
 	/**
 	 * read all the data files in a scenario folder
 	 * filter to return just the csv files with data
 	 * @return
 	 */
 	private List<File> getDataFiles() throws ScenarioInitializationException {
 		List<File> dataFiles = new ArrayList<File>();
 		File[] files = new File[0];
 		File dir = new File(directory);
 		if(dir.exists()&&dir.isDirectory()) {
 			files = dir.listFiles();
 		} else throw new ScenarioInitializationException("Cannot find file: "+dir);
 		
 		for (int i = 0; i < files.length; i ++)  {
 			File f = files[i];
 			if (this.isDataFile(f)) dataFiles.add(f);
 			if (this.isRunParameterFile(f)) readRunParameters(f);
 		}
 		if(dataFiles.size() == 0) throw new ScenarioInitializationException("No data files found!");
 		
 		return dataFiles;
 	}// getDataFiles
 	
 	/**
 	 * 
 	 * @param directory
 	 * @return true is a data directory exists
 	 */
 	public static boolean validate(String directory) {
 		File file = new File(directory);
 		return file.exists();
 	}
 	
 	/**
 	 * test file name to see if it is a data file. Must have extension .txt or .csv
 	 * must not be the runparameters summary file.
 	 * @param file
 	 * @return true if data file
 	 */
 	private boolean isDataFile(File file) {
 		boolean retVal = true;
 		String name = file.getName();
 		if(name.indexOf(RUN_PARAMETER_FILE_NAME)>= 0 || name.indexOf(RUN_PARAMETER_FILE_NAME_OLD) >= 0) retVal = false;
 		if(name.indexOf(CSV_EXTENSION) < 0) retVal = false;
 		return retVal;
 	}
 	/**
 	 * test file name to see if it is the RunParamtersFile
 	 * @param file
 	 * @return true if data file
 	 */
 	private boolean isRunParameterFile(File file) {
 		boolean retVal = false;
 		String name = file.getName();
 		if(name.indexOf(RUN_PARAMETER_FILE_NAME)>= 0 || name.indexOf(RUN_PARAMETER_FILE_NAME_OLD)>= 0) retVal = true;
 		return retVal;
 	}
 	
 	/**
 	 * read the run parameters file created when running an Experiment
 	 * @param file
 	 */
 	private void readRunParameters(File file) {
 
 		try {
 			BufferedReader fileReader = new BufferedReader(new FileReader(file));
 
 			if (fileReader != null) {
 				String buffer = null;
 				StringTokenizer headers = null;
 				StringTokenizer vals = null;
 				int line = 0;
 				while (EOF(buffer = fileReader.readLine()) != true) {
 					if(line == 0) headers = new StringTokenizer(buffer, ",");
 					else vals = new StringTokenizer(buffer, ",");
 					++line;
 				}
 				while(headers.hasMoreTokens()) {
 					String val = null;
 					try {
 						val = vals.nextToken();
 					} catch(NoSuchElementException e) {
 						Activator.logError("Mismatch between columns and values in run parameter file", e);
 						return;
 					}
 					
 					runParameters.put(headers.nextToken(), val);
 				}
 			}
 		} catch (FileNotFoundException fnfe) {
 			Activator
 					.logError(
 							"CVSscenarioLoader.readRunParametersFile() run parameters file not found: ",
 							fnfe);
 		} catch (IOException ioe) {
 			Activator.logError(
 					"CVSscenarioLoader.readRunParametersFile() io exception: ",
 					ioe);
 		}
 	}// readRunParameters
 	
 	/**
 	 * A single Reference Scenario is stored in a map
 	 * for each  key<String> = Region id
 	 * the Map contains a Map of data per region.
 	 * The region data map is keyed by property (S,E, I, R, etc) and contains Data (mostly Doubles but
 	 * STEMTime is stored as a String) so all data is stored as String.
 	 * @param filterSet 
 	 * @param resolution Which map resolution to load, -1 if all
 	 * @return the ReferenceScenarioDataMap object
 	 * @throws ScenarioInitializationException 
 	 */
 	
     public ReferenceScenarioDataMapImpl parseAllFiles(Set<String> filterSet, int resolution) throws ScenarioInitializationException {
 		ReferenceScenarioDataMapImpl scenarioDataMap = (ReferenceScenarioDataMapImpl)aFactory.createReferenceScenarioDataMap();
 		ArrayList<String>states = new ArrayList<String>(); // keeps track of the states seen 
 		for(int i = 0; i < diseaseData.size(); i ++) {
 			File f = diseaseData.get(i);
 			String name = f.getName();
 			int _idx = name.lastIndexOf('_');
 			int dotidx = name.lastIndexOf('.');
 			if(_idx <0 || dotidx < 0 || dotidx < _idx) continue; // not a data file
 			String state = name.substring(0, _idx);
 			if(!states.contains(state)) states.add(state);
 			int res = Integer.parseInt(name.substring(_idx+1, dotidx));
 			if(resolution != -1 && res != resolution) continue; // wrong resolution
 			processAndFilterFiles(filterSet, scenarioDataMap, state, f);
 		}
 		scenarioDataMap.setReferenceDirectory(directory);
 		
 		DiseaseType type;
 		
 		if(states.contains("S") &&
 			states.contains("E") &&
 			states.contains("I") &&
 			states.contains("R")) 
 				type = DiseaseType.SEIR;
 		else  if(states.contains("S") &&
 			states.contains("I") &&
 			states.contains("R"))
 			type = DiseaseType.SIR;
 		 else if(states.contains("S") &&
 			 states.contains("I"))
 			 type = DiseaseType.SI;
 		else {
 		  throw new ScenarioInitializationException("Disease type not recognized in directory: "+this.directory);
 		}
 		scenarioDataMap.setType(type);
 		return scenarioDataMap;
 	}
     
     /**
 	 * A single Reference Scenario is stored in a map
 	 * for each  key<String> = Region id
 	 * the Map contains a Map of data per region.
 	 * The region data map is keyed by property (S,E, I, R, etc) and contains Data (mostly Doubles but
 	 * STEMTime is stored as a String) so all data is stored as String.
 	 * 
 	 * @param resolution Which map resolution to load, -1 if all
 	 * @return the ReferenceScenarioDataMap object
 	 * @throws ScenarioInitializationException 
 	 */
 	
     public ReferenceScenarioDataMapImpl parseAllFiles(int resolution) throws ScenarioInitializationException {
     	ReferenceScenarioDataMapImpl scenarioDataMap = (ReferenceScenarioDataMapImpl)aFactory.createReferenceScenarioDataMap();
 		ArrayList<String>states = new ArrayList<String>(); // keeps track of the states seen 
 		for(int i = 0; i < diseaseData.size(); i ++) {
 			File f = diseaseData.get(i);
 			String name = f.getName();
			if(name.startsWith(".")) continue; //skip system files
 			int _idx = name.lastIndexOf('_');
 			int dotidx = name.lastIndexOf('.');
 			if(_idx <0 || dotidx < 0 || dotidx < _idx) continue; // not a data file
 			String state = name.substring(0, _idx);
 			if(!states.contains(state)) states.add(state);
 			int res = Integer.parseInt(name.substring(_idx+1, dotidx));
 			if(resolution != -1 && res != resolution) continue; // wrong resolution
 			processFile(scenarioDataMap, state, f);
 		}
 		scenarioDataMap.setReferenceDirectory(directory);
 		
 		DiseaseType type;
 		
 		if(states.contains("S") &&
 			states.contains("E") &&
 			states.contains("I") &&
 			states.contains("R")) 
 				type = DiseaseType.SEIR;
 		else  if(states.contains("S") &&
 			states.contains("I") &&
 			states.contains("R"))
 			type = DiseaseType.SIR;
 		 else if(states.contains("S") &&
 			 states.contains("I"))
 			 type = DiseaseType.SI;
 		else {
 		  throw new ScenarioInitializationException("Disease type not recognized in directory: "+this.directory);
 		}
 		scenarioDataMap.setType(type);
 		return scenarioDataMap;
 	}
 	
     /**
      * Read scenario for all resolutions available
      * 
      * @return ReferenceScenarioDataMap Map with all nodes loaded
      * @throws ScenarioInitializationException
      */
     
     public ReferenceScenarioDataMapImpl parseAllFiles() throws ScenarioInitializationException {
     	return this.parseAllFiles(null, -1);
     }
     
     /**
 	 * Return the type of the logged data.
 	 * 
 	 * @return ParameterEstimator.Type The type of the data
 	 * @throws ScenarioInitializationException If the type is not recognized
 	 */
 	
     public DiseaseType getType() throws ScenarioInitializationException {
 		ArrayList<String>states = new ArrayList<String>(); // keeps track of the states seen 
 		for(int i = 0; i < diseaseData.size(); i ++) {
 			File f = diseaseData.get(i);
 			String name = f.getName();
 			int _idx = name.lastIndexOf('_');
 			int dotidx = name.lastIndexOf('.');
 			if(_idx <0 || dotidx < 0 || dotidx < _idx) continue; // not a data file
 			String state = name.substring(0, _idx);
 			if(!states.contains(state)) states.add(state);
 		}
 		
 		DiseaseType type;
 		
 		if(states.contains("S") &&
 			states.contains("E") &&
 			states.contains("I") &&
 			states.contains("R")) 
 				type = DiseaseType.SEIR;
 		else  if(states.contains("S") &&
 			states.contains("I") &&
 			states.contains("R"))
 			type = DiseaseType.SIR;
 		 else if(states.contains("S") &&
 			 states.contains("I"))
 			 type = DiseaseType.SI;
 		else {
 		  throw new ScenarioInitializationException("Disease type not recognized in directory: "+this.directory);
 		}
 		return type;
 	}
     
     /**
 	 * Retrieve resolutions available in the disease folder
 	 * 
 	 * @return ArrayList<Integer> Available resolutions
 	 */
 	
     @SuppressWarnings("boxing")
 	public ArrayList<Integer> getResolutions() {
     	ArrayList<Integer> result = new ArrayList<Integer>();
     	
 		for(int i = 0; i < diseaseData.size(); i ++) {
 			File f = diseaseData.get(i);
 			String name = f.getName();
 			int _idx = name.lastIndexOf('_');
 			int dotidx = name.lastIndexOf('.');
 			if(_idx <0 || dotidx < 0 || dotidx < _idx) continue; // not a data file
 			int res = Integer.parseInt(name.substring(_idx+1, dotidx));
 			if(!result.contains(res)) result.add(res);
 			
 		}
 		return result;
 	}
     
     /**
 	 * Retrieve resolutions available in the disease folder
 	 * 
 	 * @return ArrayList<Integer> Available resolutions
 	 */
 	
 	public int getMaxResolution() {
     	int result = -1;
     	
 		for(int i = 0; i < diseaseData.size(); i ++) {
 			File f = diseaseData.get(i);
 			String name = f.getName();
 			int _idx = name.lastIndexOf('_');
 			int dotidx = name.lastIndexOf('.');
 			if(_idx <0 || dotidx < 0 || dotidx < _idx) continue; // not a data file
 			int res = Integer.parseInt(name.substring(_idx+1, dotidx));
 			if(result < res) result = res;
 			
 		}
 		return result;
 	}
 	
 	/**
 	 * process the input file
 	 * 
 	 * @param dataMap Map to store file inside
 	 * @param state The state
 	 * @param file
 	 * 
 	 * @throws ScenarioInitializationException 
 	 */
     
 	  public void processFile(ReferenceScenarioDataMapImpl dataMap, String state, File file) throws ScenarioInitializationException {
 		  	
 		try {
 	      String record;  
 	      String header;
 	      int recCount = 0;
 	      List<String> headerElements = new ArrayList<String>();
 	      FileInputStream fis = new FileInputStream(file); 
 	      BufferedReader d = new BufferedReader(new InputStreamReader(fis));
 	         
 	      //
 	      // Read the file header (iter, time, locations...)
 	      //
 	      if ( (header=d.readLine()) != null ) { 
 	          	
 		      StringTokenizer st = new StringTokenizer(header, ",");
 		            
 		      while (st.hasMoreTokens()) {
 		    	  String val = st.nextToken().trim();
 		    	  headerElements.add(val);
 		      }
 	        } // read the header
 	      /////////////////////
 	          
 	      // set up the empty lists
 	      int numColumns = headerElements.size();
 	      for (int i = 0; i < numColumns; i ++) {
 	      	String key = headerElements.get(i);
 	      	if(key.equals(ReferenceScenarioDataMapImpl.ITERATION_KEY) ||
 	      			key.equals(ReferenceScenarioDataMapImpl.TIME_KEY))
 	      		continue;
 	      	Map<String, List<String>> data;
 	      	ReferenceScenarioDataInstance result = null;
 	      	if(!dataMap.containsLocation(key)) {
 	      		data = new HashMap<String, List<String>>();
 	      		result = dataMap.new ReferenceScenarioDataInstance(data, dataMap);
 	      		dataMap.addInstance(key, result); // Add new location to 
 	      	} else {
 	      		result = dataMap.getLocation(key);
 	      		data = result.getInstance();
 	      	}
 	      	data.put(state, new ArrayList<String>()); // Add the new state
 	      }
 	          
           //////////////////////
           // Read the data
           //
           while ( (record=d.readLine()) != null ) { 
              recCount++; 
              
              StringTokenizer st = new StringTokenizer(record,",");
              int tcount = 0;
 			 while (st.hasMoreTokens() && tcount < headerElements.size() ) {// just to make sure
 			    String val = st.nextToken();
 				String key = headerElements.get(tcount);
 		    	if(key.equals(ReferenceScenarioDataMapImpl.ITERATION_KEY) ||
 		      			key.equals(ReferenceScenarioDataMapImpl.TIME_KEY))
 		      		{++tcount;continue;}
 				ReferenceScenarioDataInstance result = dataMap.getLocation(key);
 				Map<String, List<String>>data = result.getInstance();
 				data.get(state).add(val.trim());
 				tcount ++;
 			}
 		  } // while file has data
        } catch (IOException e) { 
           // catch io errors from FileInputStream or readLine()
       	 Activator.logError(" IOException error!", e);
       	 throw new ScenarioInitializationException(e);
        }
 	  }
 	  
 	  /**
 		 * process the input file
 	 	 * @param resolution 
 		 * 
 	     * @return set of locations in a file
 		 * 
 		 * @throws ScenarioInitializationException 
 		 */
 		  public Set<String> getLocations(int resolution) throws ScenarioInitializationException {
 			Set<String> locations = new HashSet<String>();
 			ArrayList<String>states = new ArrayList<String>(); // keeps track of the states seen 
 			
 			for(int i = 0; i < diseaseData.size(); i ++) {
 				File file = diseaseData.get(i);
 				String name = file.getName();
 				int _idx = name.lastIndexOf('_');
 				int dotidx = name.lastIndexOf('.');
 				if(_idx <0 || dotidx < 0 || dotidx < _idx) continue; // not a data file
 				String state = name.substring(0, _idx);
 				if(!states.contains(state)) states.add(state);
 				int res = Integer.parseInt(name.substring(_idx+1, dotidx));
 				if(resolution != -1 && res != resolution) continue; // wrong resolution
 				try {  
 				      String header;
 				      List<String> headerElements = new ArrayList<String>();
 				      FileInputStream fis = new FileInputStream(file); 
 				      BufferedReader d = new BufferedReader(new InputStreamReader(fis));
 				      // Read the file header (iter, time, locations...)
 				      //
 				      if ( (header=d.readLine()) != null ) { 
 					      StringTokenizer st = new StringTokenizer(header, ",");
 					      while (st.hasMoreTokens()) {
 					    	  String val = st.nextToken().trim();
 					    	  headerElements.add(val);
 					      }
 				        } // read the header
 				      ///////////////////// 
 				      // set up the empty lists
 				      int numColumns = headerElements.size();
 				      for (int ii = 0; ii < numColumns; ii ++) {
 				      	String key = headerElements.get(ii);
 				      	if(!((key.equals(ReferenceScenarioDataMapImpl.ITERATION_KEY) ||	key.equals(ReferenceScenarioDataMapImpl.TIME_KEY)))) {
 					        locations.add(key);
 				      	}
 					  } // while file has data
 			       } catch (IOException e) { 
 			          // catch io errors from FileInputStream or readLine()
 			      	 Activator.logError(" IOException error!", e);
 			      	 throw new ScenarioInitializationException(e);
 			       }
 			}
 			
 	       return locations;
 		  }
 	  
 
 		/**
 		 * process the input file
 		 * @param locationFilter 
 		 * 
 		 * @param dataMap Map to store file inside
 		 * @param state The state
 		 * @param file
 		 * 
 		 * @throws ScenarioInitializationException 
 		 */
 		  public void processAndFilterFiles(Set<String>locationFilter, ReferenceScenarioDataMapImpl dataMap, String state, File file) throws ScenarioInitializationException {
 			  try {
 			      String record;  
 			      String header;
 			      int recCount = 0;
 			      List<String> headerElements = new ArrayList<String>();
 			      FileInputStream fis = new FileInputStream(file); 
 			      BufferedReader d = new BufferedReader(new InputStreamReader(fis));
 			         
 			      //
 			      // Read the file header (iter, time, locations...)
 			      //
 			      if ( (header=d.readLine()) != null ) { 
 			          	
 				      StringTokenizer st = new StringTokenizer(header, ",");
 				            
 				      while (st.hasMoreTokens()) {
 				    	  String val = st.nextToken().trim();
 				    	  headerElements.add(val);
 				      }
 			        } // read the header
 			      /////////////////////
 			          
 			      // set up the empty lists
 			      int numColumns = headerElements.size();
 			      for (int i = 0; i < numColumns; i ++) {
 			      	String key = headerElements.get(i);
 			      	// if filter is null just don't filter
 			      	if(locationFilter==null || locationFilter.contains(key)) {
 			      		if(key.equals(ReferenceScenarioDataMapImpl.ITERATION_KEY) ||
 				      			key.equals(ReferenceScenarioDataMapImpl.TIME_KEY))
 				      		continue;
 				      	Map<String, List<String>> data;
 				      	ReferenceScenarioDataInstance result = null;
 				      	if(!dataMap.containsLocation(key)) {
 				      		data = new HashMap<String, List<String>>();
 				      		result = dataMap.new ReferenceScenarioDataInstance(data, dataMap);
 				      		dataMap.addInstance(key, result); // Add new location to 
 				      	} else {
 				      		result = dataMap.getLocation(key);
 				      		data = result.getInstance();
 				      	}
 				      	data.put(state, new ArrayList<String>()); // Add the new state
 			      	}
 			      }
 			          
 		          //////////////////////
 		          // Read the data
 		          //
 		          while ( (record=d.readLine()) != null ) { 
 		             recCount++; 
 		             
 		             StringTokenizer st = new StringTokenizer(record,",");
 		             int tcount = 0;
 					 while (st.hasMoreTokens() && tcount < headerElements.size() ) {// just to make sure
 					    String val = st.nextToken();
 						String key = headerElements.get(tcount);
 						// if filter is null just don't filter
 						if(locationFilter==null || locationFilter.contains(key)) {
 							if(key.equals(ReferenceScenarioDataMapImpl.ITERATION_KEY) ||
 					      			key.equals(ReferenceScenarioDataMapImpl.TIME_KEY))
 					      		{++tcount;continue;}
 							ReferenceScenarioDataInstance result = dataMap.getLocation(key);
 							Map<String, List<String>>data = result.getInstance();
 							data.get(state).add(val.trim());
 						}
 						tcount ++;
 					}
 				  } // while file has data
 		       } catch (IOException e) { 
 		          // catch io errors from FileInputStream or readLine()
 		      	 Activator.logError(" IOException error!", e);
 		      	 throw new ScenarioInitializationException(e);
 		       }
 		  }// processAndFilterFiles
 	  
 	  /**
 		 * Parse a single aggregate data file
 		 * 
 		 * @param file File to parse
 		 * @return the ReferenceScenarioDataMap object
 		 * @throws ScenarioInitializationException 
 		 */
 	    public ReferenceScenarioDataMapImpl parseAggregateFile(String file) throws ScenarioInitializationException {
 	    	ReferenceScenarioDataMapImpl scenarioDataMap = (ReferenceScenarioDataMapImpl)aFactory.createReferenceScenarioDataMap();
 			File f = new File(file);
 			String name = f.getName();  // Disease name
 			int idx = name.indexOf(".");
 			String id = name.substring(0,idx);
 				
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
 	      	 throw new ScenarioInitializationException(e);
 	       }
 	      
 			scenarioDataMap.addInstance(
 					id,   
 					scenarioDataMap.new ReferenceScenarioDataInstance(data, scenarioDataMap));
 			return scenarioDataMap;
 		}
 	  /**
 		 * process an incidence file
 		 * An incidence file does not contain SI data so it can not return 
 		 * a ReferenceScenarioDataInstance (it is not used to estimate parameters).
 		 * Instead this method returns the raw data in a map
 		 * 
 		 * @param fileName
 		 * @return a map of scenario data keyed by location ID
 		 * @throws ScenarioInitializationException 
 		 */
 		  public Map<String, List<String>> processIncidenceFile(String fileName) throws ScenarioInitializationException {
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
 	      	 throw new ScenarioInitializationException(e);
 	       }
 	       return data;
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
 	 * Return the run parameters
 	 * 
 	 * @return Map<String, String> The run parameters
 	 */
 	
 	public Map<String, String> getRunParameters() {
 		return runParameters;
 	}
 }

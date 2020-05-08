 /* ***** BEGIN LICENSE BLOCK *****
  * 
  * Copyright (c) 2011 Colin J. Fuller
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.imageanalysistools.frontend;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.PrintWriter;
 import java.util.List;
 import java.util.Map;
 
 import edu.stanford.cfuller.imageanalysistools.metric.Measurement;
 import edu.stanford.cfuller.imageanalysistools.metric.Quantification;
 import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;
 
 /**
  * Summarizes the output from analysis routines, combining all output from a directory into a single unified file.
  * If objects have been clustered into groups, summarizes only over those groups, omitting data on individual objects.
  *
  * @author Colin J. Fuller
  *
  */
 
 
 public class DataSummary {
 
 
 
     /**
      * Creates a summary of output files created by the analysis program.
      *
      * @param directory     The full path to the directory containing the output files to be summarized.
      * @param parameterDirectory       The directory that stores the parameters for the analysis.
      * @throws java.io.IOException      If any problems reading the analysis output files or writing the summary to disk are encountered.
      */
     public static void SummarizeData(String directory, String parameterDirectory) throws java.io.IOException {
 
 
         final String outputFileExtension = ".out.txt";
 
         File dir = new File(directory);
         
         File serialDir = new File(directory + File.separator + AnalysisController.SERIALIZED_DATA_SUFFIX);
 
         if (! dir.exists() || ! serialDir.exists()) {
         	
         	return;
         }
 
         File outputFile = new File(directory + File.separator + "summary.txt");
 
 
         PrintWriter output = new PrintWriter(new FileOutputStream(outputFile));
 
         for (File f : serialDir.listFiles()) {
 
 
         	ParameterDictionary params = null;
         	
 
             if (! f.getName().matches(".*" + outputFileExtension)) {continue;}
 
             File parameterFile = new File(parameterDirectory + File.separator + f.getName().replace(outputFileExtension, AnalysisController.PARAMETER_EXTENSION));
 
             params = ParameterDictionary.readParametersFromFile(parameterFile.getAbsolutePath());
 
             ObjectInputStream o = new ObjectInputStream(new FileInputStream(f));
             
             Quantification q = null;
             
             try {
             	q = (Quantification) o.readObject();
             } catch (ClassNotFoundException e) {
             	q = null;
             }
             
             o.close();
             
             if (q == null) {continue;}
             
             Map<Long, Map<String, List<Measurement> > > intensityMeasurementsByGroup = new java.util.HashMap<Long, Map<String, List<Measurement> > >();
             
             Map<Long, Map<String, List<Measurement> > > backgroundMeasurementsByGroup = new java.util.HashMap<Long, Map<String, List<Measurement> > >();
             
             Map<Long, Map<String, List<Measurement> > > sizeMeasurementsByGroup = new java.util.HashMap<Long, Map<String, List<Measurement> > >();
             
             Map<Long, Long> groupLookup = new java.util.HashMap<Long, Long>();
             
             String imageID = null;
             
            if (q.getAllMeasurementsForType(Measurement.TYPE_GROUPING) == null || q.getAllMeasurementsForType(Measurement.TYPE_GROUPING).size() == 0) {
            	for (Measurement m : q.getAllMeasurements()) {
            		groupLookup.put(m.getFeatureID(), m.getFeatureID());
                	if (imageID == null) {imageID = m.getImageID();}
            	}
            } else {
            
	            for (Measurement m : q.getAllMeasurementsForType(Measurement.TYPE_GROUPING)) {
	            	groupLookup.put(m.getFeatureID(), (long) m.getMeasurement());
	            	if (imageID == null) {imageID = m.getImageID();}
	            }
             }
             
             if (imageID == null) {
             	imageID = f.getName();
             }
             
             for (Measurement m : q.getAllMeasurementsForType(Measurement.TYPE_INTENSITY)) {
             	
             	long groupID = m.getFeatureID();
             	if (groupLookup.containsKey(groupID)) {groupID = groupLookup.get(m.getFeatureID());}
             	
             	if (! intensityMeasurementsByGroup.containsKey(groupID)) {
             		intensityMeasurementsByGroup.put(groupID, new java.util.HashMap<String, List<Measurement> >());
             	}
             	
             	Map<String, List<Measurement> > currGroup = intensityMeasurementsByGroup.get(groupID);
             	
             	String name = m.getMeasurementName();
             	
             	if (! currGroup.containsKey(name) ) {
             		currGroup.put(name, new java.util.ArrayList<Measurement>());
             	}
             	
             	currGroup.get(name).add(m);
             	
             }
             
             for (Measurement m : q.getAllMeasurementsForType(Measurement.TYPE_BACKGROUND)) {
             	
             	long groupID = m.getFeatureID();
             	if (groupLookup.containsKey(groupID)) {groupID = groupLookup.get(m.getFeatureID());}
             	
             	if (! backgroundMeasurementsByGroup.containsKey(groupID)) {
             		backgroundMeasurementsByGroup.put(groupID, new java.util.HashMap<String, List<Measurement> >());
             	}
             	
             	Map<String, List<Measurement> > currGroup = backgroundMeasurementsByGroup.get(groupID);
             	
             	String name = m.getMeasurementName();
             	
             	if (! currGroup.containsKey(name) ) {
             		currGroup.put(name, new java.util.ArrayList<Measurement>());
             	}
             	
             	currGroup.get(name).add(m);
             	
             }
             
             for (Measurement m : q.getAllMeasurementsForType(Measurement.TYPE_SIZE)) {
             	
             	long groupID = m.getFeatureID();
             	if (groupLookup.containsKey(groupID)) {groupID = groupLookup.get(m.getFeatureID());}
             	
             	if (! sizeMeasurementsByGroup.containsKey(groupID)) {
             		sizeMeasurementsByGroup.put(groupID, new java.util.HashMap<String, List<Measurement> >());
             	}
             	
             	Map<String, List<Measurement> > currGroup = sizeMeasurementsByGroup.get(groupID);
             	
             	String name = m.getMeasurementName();
             	
             	if (! currGroup.containsKey(name) ) {
             		currGroup.put(name, new java.util.ArrayList<Measurement>());
             	}
             	
             	currGroup.get(name).add(m);
             	
             }
             
             Quantification groupQuant = new Quantification();
             
             for (Long group : intensityMeasurementsByGroup.keySet()) {
             	
             	int count = 0;
             	boolean counted = false;
             	
             	for (String name : intensityMeasurementsByGroup.get(group).keySet()) {
             	
             		Measurement m = new Measurement(true, group, 0.0, name, Measurement.TYPE_INTENSITY, imageID);
             		            		
             		for (Measurement individual : intensityMeasurementsByGroup.get(group).get(name)) {
             			
             			m.setMeasurement(m.getMeasurement() + individual.getMeasurement());
             			if (!counted) {count++;}
             		}
             		
             		m.setMeasurement(m.getMeasurement() / count);
             		
             		counted = true;
             		
                 	groupQuant.addMeasurement(m);
 
             	}
             	
             	for (String name : backgroundMeasurementsByGroup.get(group).keySet()) {
                 	
             		Measurement m = new Measurement(true, group, 0.0, name, Measurement.TYPE_BACKGROUND, imageID);
             		            		
             		for (Measurement individual : backgroundMeasurementsByGroup.get(group).get(name)) {
             			
             			m.setMeasurement(m.getMeasurement() + individual.getMeasurement());
             			if (!counted) {count++;}
             		}
             		
             		m.setMeasurement(m.getMeasurement() / count);
             		
             		counted = true;
             		
                 	groupQuant.addMeasurement(m);
 
             	}
             	
             	for (String name : sizeMeasurementsByGroup.get(group).keySet()) {
                 	
             		Measurement m = new Measurement(true, group, 0.0, name, Measurement.TYPE_SIZE, imageID);
             		            		
             		for (Measurement individual : sizeMeasurementsByGroup.get(group).get(name)) {
             			
             			m.setMeasurement(m.getMeasurement() + individual.getMeasurement());
             			if (!counted) {count++;}
             		}
             		
             		m.setMeasurement(m.getMeasurement() / count);
             		
             		counted = true;
             		
                 	groupQuant.addMeasurement(m);
 
             	}
             	
             	groupQuant.addMeasurement(new Measurement(true, group, count, "region_count", Measurement.TYPE_SIZE, imageID));
             	
             }
 
             String data = LocalAnalysis.generateDataOutputString(groupQuant, params);
             
             output.println(imageID);
             
             output.println(data);
             
         }
             
  
 
         output.close();
 
 
     }
 
 
 
 }

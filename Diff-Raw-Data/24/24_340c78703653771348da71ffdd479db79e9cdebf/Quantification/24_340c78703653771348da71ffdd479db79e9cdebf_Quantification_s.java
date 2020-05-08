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
 
 package edu.stanford.cfuller.imageanalysistools.metric;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Represents the result of quantifying an image.  Collects information (in the form
  * of Measurement objects) about each region of interest or object in the image into one place.
  * 
  * @author Colin J. Fuller
  *
  */
 public class Quantification implements Serializable {
 
 	private static final long serialVersionUID = 8194024040087176409L;
 
 	protected List<Measurement> measurements;
 	
 	protected Map<String, List<Measurement> > measurementsByName;
 	protected Map<String, List<Measurement> > measurementsByType;
 	protected Map<Long, List<Measurement> > measurementsByRegion;
 	
 	protected List<Measurement> globalMeasurements;
 	
 	
 	public Quantification() {
 		measurements = new java.util.ArrayList<Measurement>();
 		measurementsByName = new java.util.HashMap<String, List<Measurement> >();
 		measurementsByType = new java.util.HashMap<String, List<Measurement> >();
 		measurementsByRegion = new java.util.HashMap<Long, List<Measurement> >();
 	}
 	
 	public void addMeasurement(Measurement m) {
 		measurements.add(m);
 		String name = m.getMeasurementName();
 		String type = m.getMeasurementType();
 		Long id = m.getFeatureID();
 		
 		if (!measurementsByName.containsKey(name)) {
 			measurementsByName.put(name, new java.util.ArrayList<Measurement>());
 		}
 		
 		measurementsByName.get(name).add(m);
 		
 		if (!measurementsByType.containsKey(type)) {
 			measurementsByType.put(type, new java.util.ArrayList<Measurement>());
 		}
 		
 		measurementsByType.get(type).add(m);
 		
 		
 		if (m.hasAssociatedFeature()) {
 			if (!measurementsByRegion.containsKey(id)) {
 				measurementsByRegion.put(id, new java.util.ArrayList<Measurement>());
 			}
 			
 			measurementsByRegion.get(id).add(m);
 		} else {
 			globalMeasurements.add(m);
 		}
 		
 	}
 	
 	public void addAllMeasurements(Quantification q) {
 		for (Measurement m : q.measurements) {
 			this.addMeasurement(m);
 		}
 	}
 	
 	public List<Measurement> getAllMeasurements() {
 		return this.measurements;
 	}
 	
 	public List<Measurement> getAllMeasurementsForName(String name) {
 		return this.measurementsByName.get(name);
 	}
 	
 	public List<Measurement> getAllMeasurementsForRegion(long regionID) {
 		return this.measurementsByRegion.get(regionID);
 	}
 	
 	public java.util.Set<Long> getAllRegions() {
 		return this.measurementsByRegion.keySet();
 	}
 	
 	public List<Measurement> getAllMeasurementsForType(String type) {
 		return this.measurementsByType.get(type);
 	}
 	
 	public List<Measurement> getAllGlobalMeasurements() {
 		return this.globalMeasurements;
 	}
 	
 
 	
 }

 package org.geworkbench.bison.datastructure.biocollections.microarrays;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import javax.swing.JOptionPane;
 import javax.swing.ProgressMonitor;
 
 import org.apache.commons.math.stat.StatUtils;
 import org.geworkbench.bison.annotation.CSAnnotationContext;
 import org.geworkbench.bison.annotation.CSAnnotationContextManager;
 import org.geworkbench.bison.annotation.DSAnnotationContext;
 import org.geworkbench.bison.annotation.DSAnnotationContextManager;
 import org.geworkbench.bison.datastructure.biocollections.CSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.CSMarkerVector;
 import org.geworkbench.bison.datastructure.bioobjects.markers.CSExpressionMarker;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.CSAffyMarkerValue;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
 import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
 import org.geworkbench.bison.util.RandomNumberGenerator;
 import org.geworkbench.engine.preferences.GlobalPreferences;
 
 /**
  * <p>Title: caWorkbench</p>
  * <p/>
  * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype
  * Analysis</p>
  * <p/>
  * <p>Copyright: Copyright (c) 2003 -2004</p>
  * <p/>
  * <p>Company: Columbia University</p>
  *
  * @author Adam Margolin
  * @version $Id$
  */
 final public class CSMicroarraySet extends CSDataSet<DSMicroarray> implements DSMicroarraySet {
 	private static final long serialVersionUID = -8604116507886706853L;
 
     private CSMarkerVector markerVector = new CSMarkerVector();
 
     private int type = -1;
 
     public CSMicroarraySet(String id, String name) {
         setID(id);
         setLabel(name);
     }
 
     public CSMicroarraySet() {
         setID(RandomNumberGenerator.getID());
         setLabel("");
 
         type = DSMicroarraySet.expPvalueType;
 
         addDescription("Microarray experiment");
         DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(this);
         CSAnnotationContext.initializePhenotypeContext(context);
     }
 
     public double getValue(DSGeneMarker marker, int maIndex) {
         //If we get a marker that is on this array -- i.e. it has a unique identifier, then
         //just return the value for that marker and don't waste time searching by other identifiers
         DSGeneMarker maMarker = markerVector.getMarkerByUniqueIdentifier(marker.getLabel());
         if (maMarker != null) {
             double value = get(maIndex).getMarkerValue(maMarker.getSerial()).getValue();
             return value;
         } else {
             //If we don't find the unique identifier then the caller wants to match one something else,
             //not guaranteed to be unique, so by default we should return the mean of all the matching
             //markers
             return getMeanValue(marker, maIndex);
         }
     }
 
     private double[] getValues(DSGeneMarker marker, int maIndex) {
         Vector<DSGeneMarker> matchingMarkers = markerVector.getMatchingMarkers(marker);
         if (matchingMarkers != null && matchingMarkers.size() > 0) {
             int[] serials = new int[matchingMarkers.size()];
             for (int markerCtr = 0; markerCtr < matchingMarkers.size(); markerCtr++) {
                 serials[markerCtr] = matchingMarkers.get(markerCtr).getSerial();
             }
             return getValues(serials, maIndex);
         } else {
             return null;
         }
     }
 
     public double getMeanValue(DSGeneMarker marker, int maIndex) {
         double values[] = getValues(marker, maIndex);
         if (values == null || values.length < 1) {
             return Double.NaN;
         } else {
             return StatUtils.mean(values);
         }
     }
 
     private double[] getValues(int[] rows, int maIndex) {
         double[] values = new double[rows.length];
         for (int i = 0; i < values.length; i++) {
             values[i] = getValue(rows[i], maIndex);
         }
         return values;
     }
 
     public double getValue(int markerIndex, int maIndex) {
         double value = get(maIndex).getMarkerValue(markerIndex).getValue();
         return value;
     }
 
     public double[] getRow(DSGeneMarker marker) {
         double[] expressionProfile = new double[size()];
         for (int i = 0; i < expressionProfile.length; i++) {
            expressionProfile[i] = get(i).getMarkerValue(marker.getSerial()).getValue();
         }
         return expressionProfile;
     }
 
     public void setCompatibilityLabel(String compatibilityLabel) {
         this.compatibilityLabel = compatibilityLabel;
     }
 
     private void writeObject(ObjectOutputStream oos) throws IOException {
         oos.defaultWriteObject();
         // Write/save additional fields
         oos.writeObject(new java.util.Date());
     }
 
     private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
         ois.defaultReadObject();
     }
 
     public CSMarkerVector getMarkers() {
         return markerVector;
     }
 
 	public void mergeMicroarraySet(DSMicroarraySet newMaSet) {
         /**
          * Stores the markers of the microarray set (the same markers that are also
          * found in {@link org.geworkbench.bison.model.microarray.AbstractMicroarraySet#markerInfoVector}).
          * The efficient searching afforded by using <code>TreeMap</code> enhances
          * a number of operations that require searching for markers.
          */
         TreeMap<Integer, Integer> markerInfoIndices = new TreeMap<Integer, Integer>();
         DSItemList<DSGeneMarker> markerInfos = newMaSet.getMarkers();
         DSMicroarray microarray = null;
         int oldIndex = 0, newIndex = 0;
         if (markerInfos != null) {
             for (int i = 0; i < markerInfos.size(); i++) {
                 if (!markerVector.contains(markerInfos.get(i))) {
                     oldIndex = markerInfos.get(i).getSerial();
                     markerInfos.get(i).setSerial(markerVector.size() - 1);
                     markerVector.add(markerInfos.get(i));
                     newIndex = markerInfos.get(i).getSerial();
                     markerInfoIndices.put(new Integer(oldIndex), new Integer(newIndex));
                 } else {
                     oldIndex = markerInfos.get(i).getSerial();
                     markerInfoIndices.put(new Integer(oldIndex), new Integer(oldIndex));
                 }
             }
         }
 
         int count = size();
         for (int ac = 0; ac < count; ac++) {
             microarray = get(ac);
             DSMarkerValue[] values = microarray.getMarkerValues();
             DSMutableMarkerValue refValue = null;
             DSMutableMarkerValue missingValue = null;
             int mvLength = values.length;
             int size = markerVector.size();
             if (mvLength < size) {
                 if (mvLength > 0) {
                     refValue = (DSMutableMarkerValue) values[0];
                 } else {
                     refValue = new CSAffyMarkerValue();
                 }
                 microarray.resize(size);
                 for (int i = 0; i < size; ++i) {
                     if (microarray.getMarkerValue(i) == null) {
                         missingValue = (DSMutableMarkerValue) refValue.deepCopy();
                         missingValue.setMissing(true);
                         microarray.setMarkerValue(i, missingValue);
                     }
                 }
             }
         }
 
         count = newMaSet.size();
         for (int ac = 0; ac < count; ac++) {
             microarray = (DSMicroarray)newMaSet.get(ac).deepCopy();
             int size = 0;
             DSMutableMarkerValue refValue = null;
             DSMutableMarkerValue missingValue = null;
             DSMarkerValue[] newValues = microarray.getMarkerValues();
             size = markerVector.size();
             microarray.resize(size);
             for (int i = 0; i < newValues.length; i++) {
                 Integer key = (Integer) markerInfoIndices.get(new Integer(i));
                 if (key != null) {
                     newIndex = key.intValue();
                     if (newIndex < microarray.getMarkerNo()) {
                         microarray.setMarkerValue(newIndex, newValues[i]);
                     }
                 }
             }
             if (newValues.length != 0) {
                 refValue = (DSMutableMarkerValue) newValues[0];
             } else {
                 refValue = new CSAffyMarkerValue();
             }
             for (int i = 0; i < size; i++) {
                 if (microarray.getMarkerValue(i) == null) {
                     missingValue = (DSMutableMarkerValue) refValue.deepCopy();
                     missingValue.setMissing(true);
                     microarray.setMarkerValue(i, missingValue);
                 }
             }
             add(size(), microarray);
         }
     }
 
     public DSMicroarray getMicroarrayWithId(String string) {
         for (DSMicroarray ma : this) {
             if (ma.getLabel().equalsIgnoreCase(string)) {
                 return ma;
             }
         }
         return null;
     }
 
 	public void writeToFile(String fileName) {
 		File file = new File(fileName);
 
 		try {
 			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
 			// start processing the data.
 			// Start with the header line, comprising the array names.
 			String outLine = "AffyID" + "\t" + "Annotation";
 			for (int i = 0; i < size(); ++i) {
 				outLine = outLine.concat("\t" + get(i).toString());
 			}
 			writer.write(outLine);
 			writer.newLine();
 
 			DSAnnotationContextManager manager = CSAnnotationContextManager
 					.getInstance();
 			int n = manager.getNumberOfContexts(this);
 			for (int i = 0; i < n; i++) {
 				DSAnnotationContext<DSMicroarray> context = manager.getContext(
 						this, i);
 				StringBuilder line = new StringBuilder("Description" + '\t'
 						+ context.getName());
 				for (Iterator<DSMicroarray> iterator = this.iterator(); iterator
 						.hasNext();) {
 					DSMicroarray microarray = iterator.next();
 					String label = "";
 					String[] labels = context.getLabelsForItem(microarray);
 					// watkin - Unfortunately, the file format only supports one
 					// label per context.
 					if (labels.length > 0) {
 						label = labels[0];
 						if (labels.length > 1)
 							for (int j = 1; j < labels.length; j++)
 								label += "|" + labels[j];
 					}
 					line.append('\t' + label);
 				}
 				writer.write(line.toString());
 				writer.newLine();
 			}
 
 			ProgressMonitor pm = new ProgressMonitor(null, "Total "
 					+ markerVector.size(), "saving ", 0, markerVector.size());
 			// Proceed to write one marker at a time
 			for (int i = 0; i < markerVector.size(); ++i) {
 				pm.setProgress(i);
 				pm.setNote("saving " + i);
 				outLine = markerVector.get(i).getLabel();
 				outLine = outLine.concat('\t' + getMarkers().get(i).getLabel());
 				for (int j = 0; j < size(); ++j) {
 					DSMarkerValue mv = get(j).getMarkerValue(i);
 					if (!mv.isMissing())
 						outLine = outLine.concat("\t" + (float) mv.getValue()
 								+ '\t')
 								+ (float) mv.getConfidence();
 					else
 						outLine = outLine.concat("\t" + "n/a" + '\t')
 								+ (float) mv.getConfidence();
 				}
 				writer.write(outLine);
 				writer.newLine();
 			}
 			pm.close();
 			writer.flush();
 			writer.close();
 		} catch (IOException e) {
 			JOptionPane.showMessageDialog(null, "File " + fileName
 					+ " is not saved due to IOException " + e.getMessage(),
 					"File Saving Failed", JOptionPane.ERROR_MESSAGE);
 
 		}
 	}
 
 	public void initialize(int maNo, int mrkNo) {
         // this is required so that the microarray vector may create arrays of the right size
         for (int microarrayId = 0; microarrayId < maNo; microarrayId++) {
             add(microarrayId, (DSMicroarray)new CSMicroarray(microarrayId, mrkNo, "Test", type));
         }
 
         for (int i = 0; i < mrkNo; i++) {
             CSExpressionMarker mi = new CSExpressionMarker();
             mi.reset(i, maNo, mrkNo);
             markerVector.add(i, mi);
         }
     }
 
     private String annotationFileName = null;
 
 	/**
 	 * @return the annotationFileName
 	 */
 	public String getAnnotationFileName() {
 		return annotationFileName;
 	}
 
 	/**
 	 * @param annotationFileName the annotationFileName to set
 	 */
 	public void setAnnotationFileName(String annotationFileName) {
 		this.annotationFileName = annotationFileName;
 	}
     
     private int[] newid;
     public int[] getNewMarkerOrder(){
     	return newid;
     }
     public void sortMarkers(int mrkNo) {
 		newid = new int[mrkNo];
 		int i = 0;
 		if (GlobalPreferences.getInstance().getMarkerLoadOptions() == GlobalPreferences.ORIGINAL
 				|| (this.getAnnotationFileName() == null && GlobalPreferences
 						.getInstance().getMarkerLoadOptions() == GlobalPreferences.SORTED_GENE)) {
 			for (i = 0; i < markerVector.size(); newid[i] = i++);
 		} else {
 			if (GlobalPreferences.getInstance().getMarkerLoadOptions() == GlobalPreferences.SORTED_GENE) 
 				Collections.sort(markerVector, new MarkerOrderByGene());
 			else
 				Collections.sort(markerVector, new MarkerOrderByProbe());
 
 			for (DSGeneMarker item : markerVector) {
 				newid[item.getSerial()] = i++;
 			}
 			i = 0;
 			for (DSGeneMarker item : markerVector) {
 				item.setSerial(i++);
 			}
 		}
 	}
 
     private class MarkerOrderByGene implements Comparator<DSGeneMarker> {
 		public int compare(DSGeneMarker o1, DSGeneMarker o2) {
 			int res = o1.getGeneName().compareToIgnoreCase(((DSGeneMarker)o2).getGeneName());
 			if (res == 0)
 				return o1.getLabel().compareToIgnoreCase(((DSGeneMarker)o2).getLabel());
 			return res;
 		}
     }
 
     private class MarkerOrderByProbe implements Comparator<DSGeneMarker> {
 		public int compare(DSGeneMarker o1, DSGeneMarker o2) {
 			return o1.getLabel().compareToIgnoreCase(((DSGeneMarker)o2).getLabel());
 		}
     }
 
     private String markerOrder = "original";
     public String getSelectorMarkerOrder(){
     	return markerOrder;
     }
     
     public void setSelectorMarkerOrder(String order){
     	markerOrder = order;
     }
 
 }

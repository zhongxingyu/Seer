 package org.geworkbench.bison.datastructure.biocollections.microarrays;
 
 import org.apache.commons.math.stat.StatUtils;
 import org.geworkbench.bison.annotation.CSAnnotationContextManager;
 import org.geworkbench.bison.annotation.DSAnnotationContext;
 import org.geworkbench.bison.annotation.DSAnnotationContextManager;
 import org.geworkbench.bison.datastructure.biocollections.CSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.CSMarkerVector;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.*;
 import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
 import org.geworkbench.bison.util.RandomNumberGenerator;
 import org.geworkbench.engine.preferences.GlobalPreferences;
 
 import java.io.*;
 import java.util.*;
 
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
 public class CSMicroarraySet<T extends DSMicroarray> extends CSDataSet<T> implements DSMicroarraySet<T> {
 	private static final long serialVersionUID = -8604116507886706853L;
 
     protected CSMarkerVector markerVector = new CSMarkerVector();
 
     protected Date timeStamp = new java.util.Date();
 
     protected int maskedSpots = 0;
     protected int type = -1;
 
     public CSMicroarraySet(String id, String name) {
         setID(id);
         setLabel(name);
     }
 
     public CSMicroarraySet() {
         setID(RandomNumberGenerator.getID());
         setLabel("");
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
 
     /**
      * @param markerIndex
      * @param maIndex
      * @return
      * @todo - watkin - This seems to violate the contract for {@link DSMatrixDataSet#getValue(int, int)}. This
      * method passes markerIndex and microarray index, whereas the interface refers to a row and a column. Confusing.
      * Same thing.... this is the implementation of the method. You can change markerIndex and maIndex to row/column
      * if you want but it's just makes it more clear if people can know what the row and column refer to.
      * The getExpressionProfile can be changed to getRow(int markerIndex) which should also be in the DSMatrixDataSet
      * interface. This is all legacy from before DSMatrixDataSet existed. Likewise, DSMatrixDataSet can have
      * double getValue(T row, int column) and double[] getRow(T row) methods. -- AM
      */
     public double getValue(int markerIndex, int maIndex) {
         double value = get(maIndex).getMarkerValue(markerIndex).getValue();
         return value;
     }
 
     /**
      * Note-- changes to the returned row will not have any effect on the underlying MicroarraySet.
      * @param markerIndex
      * @return
      */
     public double[] getRow(int markerIndex) {
         double[] expressionProfile = new double[size()];
         for (int i = 0; i < expressionProfile.length; i++) {
             expressionProfile[i] = getValue(markerIndex, i);
         }
         return expressionProfile;
     }
 
     public double[] getRow(DSGeneMarker marker) {
         double[] expressionProfile = new double[size()];
         for (int i = 0; i < expressionProfile.length; i++) {
             expressionProfile[i] = getValue(marker, i);
         }
         return expressionProfile;
     }
 
     public void setCompatibilityLabel(String compatibilityLabel) {
         this.compatibilityLabel = compatibilityLabel;
     }
 
     @SuppressWarnings("unchecked")
 	private void writePhenotypeValueArray(BufferedWriter writer) throws IOException {
         DSAnnotationContextManager manager = CSAnnotationContextManager.getInstance();
         DSAnnotationContext<T>[] contexts = manager.getAllContexts(this);
         for (int j = 0; j < contexts.length; j++) {
             DSAnnotationContext<T> context = contexts[j];
             writer.write("Description\t" + context.getName());
             for (int i = 0; i < size(); i++) {
                 DSMicroarray mArray = get(i);
                 String[] labels = context.getLabelsForItem((T)mArray);
                 // TODO - watkin - this file format does not support multiple labellings per item
                 if (labels.length > 0) {
                     writer.write("\t" + labels[0]);
                 } else {
                     writer.write("\tUndefined");
                 }
             }
             writer.write('\n');
         }
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
 
     @SuppressWarnings("unchecked")
 	public void mergeMicroarraySet(DSMicroarraySet<T> newMaSet) {
         /**
          * Stores the markers of the microarray set (the same markers that are also
          * found in {@link org.geworkbench.bison.model.microarray.AbstractMicroarraySet#markerInfoVector}).
          * The efficient searching afforded by using <code>TreeMap</code> enhances
          * a number of operations that require searching for markers.
          */
         TreeMap<Integer, Integer> markerInfoIndices = new TreeMap<Integer, Integer>();
         DSItemList<DSGeneMarker> markerInfos = newMaSet.getMarkers();
         T microarray = null;
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
             microarray = (T)newMaSet.get(ac).deepCopy();
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
 
     public void writeToFile(String file) {
         BufferedWriter writer;
         try {
             writer = new BufferedWriter(new FileWriter(file));
             writer.write("PDFModel\tAutoNormal\n");
 
             writer.write("Description\tAccession");
             for (DSMicroarray ma : this) {
                 writer.write('\t');
                 writer.write(ma.getLabel());
             }
             writer.write('\n');
             // Write the Phenotypes Definitions
             writePhenotypeValueArray(writer);
             for (DSGeneMarker m : getMarkers()) {
                 ((DSGeneMarker) m).write(writer);
                 for (DSMicroarray ma : this) {
                     writer.write('\t' + ma.getMarkerValue(m.getSerial()).toString());
                 }
                 writer.write('\n');
             }
             writer.flush();
             writer.close();
         } catch (IOException e) {
             System.out.println("Exception in JMicroarrays.Write(): " + e);
         }
     }
 
     public CSMarkerVector getMarkerVector() {
         return markerVector;
     }
 
     public void initialize(int maNo, int mrkNo) {
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
     
     public int[] newid;
     public int[] getNewMarkerOrder(){
     	return newid;
     }
     public void sortMarkers(int mrkNo) {
 		newid = new int[mrkNo];
 		int i = 0;
 		if (GlobalPreferences.getInstance().getMarkerLoadOptions() == GlobalPreferences.ORIGINAL) {
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
 }

 package org.geworkbench.bison.datastructure.biocollections.microarrays;
 
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.DSMatrixDataSet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
 
 /**
  * Defines a generic set of Microarrays.
  * <p/>
  * DSDataSet. The objects in a matrix dataset will themselves contain vectors of values. -- AM
  * todo - watkin - this should have two generic type variables, one for microarray, one for marker.
  * Currently, it is just generic for microarrays.
  */
 public interface DSMicroarraySet <T extends DSMicroarray> extends DSMatrixDataSet<T>, DSDataSet<T> {
 
     public final static int geneExpType = 0; // Probably needs to be removed
 
     public final static int expPvalueType = 3; // added by xuegong
     public final static int chipChipType = 4; // added by xuegong
     public final static int genepixGPRType = 5;
     public final static int affyTxtType = 6; // Txt MAS 4.0/5.0 file type
 
     public double getValue(DSGeneMarker marker, int maIndex);
 
     public double getMeanValue(DSGeneMarker marker, int maIndex);
 
     public double[] getRow(DSGeneMarker marker);
 
     /**
      * Merges another <code>MicroarraySet</code> into this one
      *
      * @param newMaSet MicroarraySet
      */
     void mergeMicroarraySet(DSMicroarraySet<T> newMaSet) throws Exception;
 
     /**
      * get the marker list
      *
      * @return DSItemList
      */
     public DSItemList<DSGeneMarker> getMarkers();
 
     void setCompatibilityLabel(String compatibilityLabel);
 
     // It is defined in the classes. In CSMicroarraySet (AC)
     public void initialize(int maNo, int mrkNo);
     
     public String getAnnotationFileName();
     public void setAnnotationFileName(String annotationFileName);
 
     public void sortMarkers(int mrkNo);
     public int[] getNewMarkerOrder();
 }

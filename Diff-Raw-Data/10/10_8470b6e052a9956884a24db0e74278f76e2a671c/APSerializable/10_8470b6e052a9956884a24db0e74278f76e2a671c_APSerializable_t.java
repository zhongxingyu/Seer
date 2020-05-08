 package org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser;
 
 import org.apache.commons.collections15.MultiMap;
 import org.apache.commons.collections15.map.ListOrderedMap;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser.MarkerAnnotation;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 /**
  * @author John Watkinson
  * @version $Id: APSerializable.java,v 1.3 2009-11-25 17:31:53 jiz Exp $
  */
 public class APSerializable implements Serializable {
 	private static final long serialVersionUID = 6455427625940524515L;
 
 	DSDataSet<? extends DSBioObject> currentDataSet = null;
 	Map<DSDataSet<? extends DSBioObject>, String> datasetToChipTypes = new HashMap<DSDataSet<? extends DSBioObject>, String>();
 
 	Map<String, ListOrderedMap<String, Vector<String>>> geneNameMap = new HashMap<String, ListOrderedMap<String, Vector<String>>>();
 	ArrayList<String> chipTypes = new ArrayList<String>();
 	MultiMap<String, String> affyToGOID = null;
 	
 	Map<String, MarkerAnnotation> chipTypeToAnnotation = null;
 
 
 	public APSerializable(
 			DSDataSet<? extends DSBioObject> currentDataSet2,
			Map<DSDataSet<? extends DSBioObject>, String> datasetToChipTypes,
 			Map<String, ListOrderedMap<String, Vector<String>>> geneNameMap,
 			ArrayList<String> chipTypes,
 			MultiMap<String, String> affyToGOID,
 			Map<String, MarkerAnnotation> chipTypeToAnnotation) {
 		this.currentDataSet = currentDataSet2;
		this.datasetToChipTypes = datasetToChipTypes;
 		this.geneNameMap = geneNameMap;
 		this.chipTypes = chipTypes;
 		this.affyToGOID = affyToGOID;
 		
 		this.chipTypeToAnnotation = chipTypeToAnnotation;
 	}
 }

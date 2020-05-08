 package org.geworkbench.bison.model.analysis;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.analysis.AbstractAnalysis;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
 
 /**
  * Abstract class to tag filtering analysis and defines common methods for
  * filtering.
  * 
  * @author zji
  * @version $Id$
  */
 public abstract class FilteringAnalysis extends AbstractAnalysis {
 
 	private static final long serialVersionUID = -7232110290771712959L;
 
 	protected DSMicroarraySet maSet = null;
 
 	protected enum CriterionOption {
 		COUNT, PERCENT
 	};
 
 	protected CriterionOption criterionOption = null;
 	protected double percentThreshold;
 	protected int numberThreshold;
 
 	private static Log log = LogFactory.getLog(FilteringAnalysis.class);
 
 	// this is not useful, but required by the interface AbsractAnalysis
 	public int getAnalysisType() {
 		return AbstractAnalysis.IGNORE_TYPE;
 	}
 
 	public AlgorithmExecutionResults execute(Object input) {
 		if (input == null || !(input instanceof DSMicroarraySet))
 			return new AlgorithmExecutionResults(false, "Invalid input.", null);
 
 		maSet = (DSMicroarraySet) input;
 
 		if (!expectedType()) {
 			return new AlgorithmExecutionResults(false,
 					"This filter can only be used with " + expectedTypeName
 							+ " datasets", null);
 		}
 
 		getParametersFromPanel();
 		remove(getMarkersToBeRemoved(maSet));
 		log.debug("finished with fitering");
 		
 		String description = "Microarray experiment. # of microarrays: " + maSet.size() + ",   "
 				+ "# of markers: " + maSet.getMarkers().size();
 		maSet.setDescription(description);
 
 		return new AlgorithmExecutionResults(true, "No errors", input);
 	}
 
 	protected abstract void getParametersFromPanel();
 
 	// for those derived class who want to check type, they need to override
 	// this
 	protected boolean expectedType() {
 		return true;
 	}
 
 	protected String expectedTypeName = null;
 
 	// This may not be the best implementation
 	// especially the resizing operation seems unnecessary is CSMicroarray is
 	// implemented cleaned
 	// in other words, CSMicroarray should make sure that when you do
 	// markers.remove, resizing will happen automatically
 	private void remove(List<Integer> tobeRemoved) {
 		int markerCount = maSet.getMarkers().size();
 
 		int removeCount = tobeRemoved.size();
 		int finalCount = markerCount - removeCount;
 		DSItemList<DSGeneMarker> markers = maSet.getMarkers();
 		for (int i = 0; i < removeCount; i++) {
 			// Account for already-removed markers
 			int index = tobeRemoved.get(i) - i ;
 			// Remove the marker
 			markers.remove(markers.get(index));
 		}
 		
 		List<Integer> remains = new ArrayList<Integer>();
 		for(int i=0; i<markerCount; i++) {
 			if (!tobeRemoved.contains(i)) remains.add(i);
 		}
 
 		// Resize each microarray
 		for (DSMicroarray microarray : maSet) {
 			DSMarkerValue[] newValues = new DSMarkerValue[finalCount];
 			int index = 0;
 			for (int i: remains) {
 				newValues[index] = microarray.getMarkerValue(i);
 				index++;
 			}
 			microarray.resize(finalCount);
 			for (int i = 0; i < finalCount; i++) {
 				microarray.setMarkerValue(i, newValues[i]);
 			}
 		}
 
 	}
 
 	public List<Integer> getMarkersToBeRemoved(DSMicroarraySet input) {
 
 		maSet = (DSMicroarraySet) input;
 
 		getParametersFromPanel();
 
 		int arrayCount = maSet.size();
 		int markerCount = maSet.getMarkers().size();
 
 		// Identify the markers that do not meet the cutoff value.
 		List<Integer> removeList = new ArrayList<Integer>();
 		for (int i = 0; i < markerCount; i++) {
 			if ((criterionOption == CriterionOption.COUNT && countMissing(i) > numberThreshold)
 					|| (criterionOption == CriterionOption.PERCENT && 
 							(double) countMissing(i) / arrayCount > percentThreshold)) {
 				removeList.add(i);
 			}
 		}
 		return removeList;
 	}
 	
 	// for MARKING, both indices matter; for REMOVAL, arrayIndex should be ignored
 	abstract protected boolean isMissing(int arrayIndex, int markerIndex);
 	
 	protected int countMissing(int markerIndex) {
 		int arrayCount = maSet.size();
 		int numMissing = 0;
 		for (int i = 0; i < arrayCount; i++) {
 			if (isMissing(i, markerIndex))
 				++numMissing;
 		}
 		return numMissing;
 	}
 
 }

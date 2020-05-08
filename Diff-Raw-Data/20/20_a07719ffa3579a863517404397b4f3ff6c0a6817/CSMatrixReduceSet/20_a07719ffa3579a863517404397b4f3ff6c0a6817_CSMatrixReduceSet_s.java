 package org.geworkbench.bison.datastructure.complex.pattern.matrix;
 
 import java.io.File;
 import java.util.List;
 
 import org.apache.commons.collections15.map.ListOrderedMap;
 import org.geworkbench.bison.datastructure.biocollections.CSAncillaryDataSet;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 
 /**
  * @author John Watkinson
  * @author ch2514
  */
 public class CSMatrixReduceSet extends
 		CSAncillaryDataSet<DSPositionSpecificAffintyMatrix> implements
 		DSMatrixReduceSet {
 
 	private ListOrderedMap<String, String> sequences;
 
 	// key=psam id; value=list of experiment data associated with the psam id
 	private ListOrderedMap<String, List<DSMatrixReduceExperiment>> experiments;
 
 	public CSMatrixReduceSet(DSDataSet parent, String label) {
 		super(parent, label);
 	}
 
 	public File getDataSetFile() {
 		return null;
 	}
 
 	public void setDataSetFile(File file) {
 	}
 
 	public ListOrderedMap<String, String> getSequences() {
 		return sequences;
 	}
 
 	public void setSequences(ListOrderedMap<String, String> sequences) {
 		this.sequences = sequences;
 	}
 
 	public ListOrderedMap<String, List<DSMatrixReduceExperiment>> getMatrixReduceExperiments() {
 		return experiments;
 	}
 
 	public void setMatrixReduceExperiments(ListOrderedMap<String, List<DSMatrixReduceExperiment>> experiments) {
 		this.experiments = experiments;
 	}	
 }
 
 

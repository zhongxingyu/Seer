 package org.geworkbench.bison.datastructure.complex.pattern.matrix;
 
 import java.util.List;
 
 import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
 import org.apache.commons.collections15.map.ListOrderedMap;
 
 import javax.swing.*;
 
 /**
  * @author John Watkinson
  * @author ch2514
  */
 public interface DSMatrixReduceSet extends DSAncillaryDataSet<DSPositionSpecificAffintyMatrix> {
 
     ListOrderedMap<String, String> getSequences();
     
     ListOrderedMap<String, List<DSMatrixReduceExperiment>> getMatrixReduceExperiments();
 
     void setSequences(ListOrderedMap<String, String> sequences);
     
     void setMatrixReduceExperiments(ListOrderedMap<String, List<DSMatrixReduceExperiment>> experiments);
     
    void setRunLog(String runlog);
    
    String getRunLog();
    
 }

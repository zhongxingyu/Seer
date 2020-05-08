 package org.geworkbench.bison.datastructure.bioobjects.microarray;
 
 import java.util.Map;
 
 import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
 
 public interface DSMasterRagulatorResultSet <T extends DSGeneMarker> extends DSAncillaryDataSet<DSMicroarray> {
 
 /**
  * @author Yih-Shien Chiang
  * @version $Id$
  */
 
 	public DSMicroarraySet<DSMicroarray> getMicroarraySet();
     public void setGenesInRegulon(DSGeneMarker TF, DSItemList<DSGeneMarker> markers);
     public void setGenesInTargetList(DSGeneMarker TF, DSItemList<DSGeneMarker> markers);
     public DSItemList<DSGeneMarker> getGenesInRegulon(DSGeneMarker TF);
     public DSItemList<DSGeneMarker> getGenesInTargetList(DSGeneMarker TF);
     public void setPValueOf(DSGeneMarker TF, DSGeneMarker targetGene, double pValue);
     public void setTTestValueOf(DSGeneMarker TF, DSGeneMarker targetGene, double tTestValue);
     public double getPValueOf(DSGeneMarker TF, DSGeneMarker targetGene);
     public double getTTestValueOf(DSGeneMarker TF, DSGeneMarker targetGene);
     public int getGeneNumInRegulon(DSGeneMarker TF);
     public int getGeneNumInTargetList(DSGeneMarker TF);
     public void setPValue(DSGeneMarker TF, double pValue);
     public double getPValue(DSGeneMarker TF);
 	public DSItemList<DSGeneMarker> getTFs();
	public void setSignificanceResultSet(DSSignificanceResultSet<DSGeneMarker> sigSet);
	public DSSignificanceResultSet<DSGeneMarker> getSignificanceResultSet();
 	public int getMarkerCount();
 	public double getTValue(DSGeneMarker marker);
 	public void setTValues(Map<DSGeneMarker, Double> tTestResult);
 }

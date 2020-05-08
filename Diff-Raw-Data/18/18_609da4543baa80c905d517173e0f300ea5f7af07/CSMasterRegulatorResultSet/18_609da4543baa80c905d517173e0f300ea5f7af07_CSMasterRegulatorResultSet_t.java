 package org.geworkbench.bison.datastructure.bioobjects.microarray;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.geworkbench.bison.datastructure.biocollections.CSAncillaryDataSet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
 import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
 
 public class CSMasterRegulatorResultSet <T extends DSGeneMarker> extends CSAncillaryDataSet<DSMicroarray> implements DSMasterRagulatorResultSet<T> {
 	private static final long serialVersionUID = -416598071322541982L;
 	
 	HashMap<DSGeneMarker,DSItemList<DSGeneMarker>> TF2GenesInRegulon = new HashMap<DSGeneMarker,DSItemList<DSGeneMarker>>();
 	HashMap<DSGeneMarker,DSItemList<DSGeneMarker>> TF2GenesInTargetList = new HashMap<DSGeneMarker,DSItemList<DSGeneMarker>>();
 	HashMap<DSGeneMarker,Double> TF2PValue = new HashMap<DSGeneMarker,Double>();
 	HashMap<String,Double> TFGeneAndTargetGene2PValue = new HashMap<String,Double>();
 	HashMap<String,Double> TFGeneAndTargetGene2TTestValue = new HashMap<String,Double>();

 	DSMicroarraySet<DSMicroarray> maSet = null;
 	private int markerCount = 0;
 
 	private Map<DSGeneMarker, Double> tValues;
 
 	public CSMasterRegulatorResultSet(DSMicroarraySet<DSMicroarray> parent, String label, int markerCount) {
 		super(parent, label);
 		this.maSet = parent;
 		this.markerCount = markerCount;
 	}
 	
 	public DSMicroarraySet<DSMicroarray> getMicroarraySet(){
 		return maSet;
 	}
 	
 	public File getDataSetFile() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void setDataSetFile(File file) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void setGenesInRegulon(DSGeneMarker TF, DSItemList<DSGeneMarker> markers) {
 		TF2GenesInRegulon.put(TF, markers);
 	}
 
 	public void setGenesInTargetList(DSGeneMarker TF, DSItemList<DSGeneMarker> markers) {
 		TF2GenesInTargetList.put(TF, markers);
 	}
 
 	public DSItemList<DSGeneMarker> getGenesInRegulon(DSGeneMarker TF) {
 		return TF2GenesInRegulon.get(TF);
 	}
 
 	public DSItemList<DSGeneMarker> getGenesInTargetList(DSGeneMarker TF) {
 		return TF2GenesInTargetList.get(TF);
 	}
 
 	public double getPValueOf(DSGeneMarker TF, DSGeneMarker targetGene) {
 		return TFGeneAndTargetGene2PValue.get(TF.getSerial()+"-"+targetGene.getSerial()).doubleValue();
 	}
 
 	public double getTTestValueOf(DSGeneMarker TF, DSGeneMarker targetGene) {
 		return TFGeneAndTargetGene2TTestValue.get(TF.getSerial()+"-"+targetGene.getSerial()).doubleValue();
 	}
 
 	public void setPValueOf(DSGeneMarker TF, DSGeneMarker targetGene, double pValue) {
 		TFGeneAndTargetGene2PValue.put(TF.getSerial()+"-"+targetGene.getSerial(), new Double(pValue));
 	}
 
 	public void setTTestValueOf(DSGeneMarker TF, DSGeneMarker targetGene, double tTestValue) {
 		TFGeneAndTargetGene2TTestValue.put(TF.getSerial()+"-"+targetGene.getSerial(), new Double(tTestValue));
 	}
 
 	public int getGeneNumInRegulon(DSGeneMarker TF) {
 		return getGenesInRegulon(TF).size();
 	}
 
 	public int getGeneNumInTargetList(DSGeneMarker TF) {
 		return getGenesInTargetList(TF).size();
 	}
 
 	public void setPValue(DSGeneMarker TF, double pValue) {
 		TF2PValue.put(TF,pValue);
 	}
 
 	public double getPValue(DSGeneMarker TF) {
 		return TF2PValue.get(TF).doubleValue();
 	}
 	
 	//here we assume user has already called setGenesInTargetList(), in that case, the keys will be the answer
 	public DSItemList<DSGeneMarker> getTFs(){
 		DSItemList<DSGeneMarker> result = new CSItemList<DSGeneMarker>();
 		result.addAll(TF2GenesInTargetList.keySet());
 		return result;
 	}
 	
 	public int getMarkerCount() {
 		return markerCount;
 	}
 
 	public void setTValues(Map<DSGeneMarker, Double> tValues){
 		this.tValues = tValues;
 	}
 
 	public double getTValue(DSGeneMarker key) {
 		if(tValues==null) return Double.NaN;
 		
 		Double v =  tValues.get(key);
 		if(v==null) v = Double.NaN;
 		return v;
 	}
 	
 }

 package cz.opendata.linked.geocoder.krovak;
 
 import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;
 
 /**
  *
  * Put your DPU's configuration here.
  *
  */
 public class ExtractorConfig extends DPUConfigObjectBase {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8719241993054209502L;
 
 	public int numofrecords = 35000;
 	
 	public int interval = 1000;
 	
	public String sessionId = "c34mab45sjcdjf235dxrvarb";

 	@Override
     public boolean isValid() {
 		return true;
     }
 
 }

 package cz.opendata.linked.psp_cz.metadata;
 
 import java.util.Calendar;
 
 import cz.cuni.xrg.intlib.commons.configuration.DPUConfigObject;
 
 /**
  *
  * Put your DPU's configuration here.
  *
  */
 public class ExtractorConfig implements DPUConfigObject {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -5577275030298541080L;
 
 	public int Start_year = 1918;
 
 	public int End_year = Calendar.getInstance().get(Calendar.YEAR);
         
 	public String outputFileName = "sbirka.ttl";
 	
 	public boolean rewriteCache = false;
 	
 	public boolean cachedLists = false;
 	
 	public int timeout = 10000;
 
 	public int interval = 2000;
 
 	@Override
     public boolean isValid() {
        return Start_year < End_year && Start_year >= 1918 && End_year <= Calendar.getInstance().get(Calendar.YEAR);
     }
 
 }

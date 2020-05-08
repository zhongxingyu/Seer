 package savi.hcat.analytics.coprocessor;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import savi.hcat.common.util.XConstants;
 import savi.hcat.common.util.XTimestamp;
 
 public class RStatResult implements Serializable{
 
 	private static final long serialVersionUID = 1L;
 	private static Log LOG = LogFactory.getLog(RStatResult.class);
 	
 	RCopResult copStat = new RCopResult();// to store the statistics of coprocessor running time
 	
 	String region = null;
 	String unit = null;
 	
 	// To store the result
 	Hashtable<String,Integer> hashUnit = null; // per month, weekly, daily
 	Hashtable<String, Hashtable<String,Integer>> hashHash = null; // service/media:{status/type=>number}	
 	Hashtable<String, Hashtable<String, int[]>> hashUnitArray = null; // month, service/media((status/type=>number)
 	// the previous format to store the result
 	List<String> res = null;
 	HashMap<String,Double> distances = null;
 	
 	
 	
 	public RCopResult getCopStat() {
 		return copStat;
 	}
 
 	public HashMap<String, Double> getDistances() {
		if(this.distances == null)
			this.distances = new HashMap<String,Double>();
 		return distances;
 	}
 
 	public List<String> getRes() {
 		if(null == this.res)
 			this.res = new ArrayList<String>();
 		return res;
 	}
 	
 	/**
 	 * This is used in KNN query
 	 * @param dist
 	 * @param id
 	 */
 	public void addDistance(double dist,String id){
 		if(this.distances == null)
 			this.distances = new HashMap<String,Double>();		
 		this.distances.put(id,dist);
 	}
 	
 	
 	
 	public RStatResult initHashUnit(){
 		hashUnit = new Hashtable<String,Integer>();
 		return this;
 	}
 	
 	public RStatResult initHashHash(){
 		hashHash = new Hashtable<String, Hashtable<String,Integer>>();
 		return this;
 	}
 	
 	public RStatResult initHashUnitArray(){
 		hashUnitArray = new Hashtable<String, Hashtable<String, int[]>>();
 		return this;
 	}
 	
 	
 	public RStatResult setHashUnit(String u,String starttime,String endtime){
 		this.unit =  u;
 		if(unit.equals("m")){
 			HashSet<String> unitSet = XTimestamp.getHashSetMonth(starttime, endtime);
 			for(String unit: unitSet){
 				this.hashUnit.put(unit, 0);
 			}
 		}else if(unit.equals("w")){
 			//
 		}
 		return this;
 	}
 	/**
 	 * month=><s1=>[23,34,45,11]> at this month, service with status 0 is 23, with status 1 is 34....
 	 * month=><m0=>[23]> at this month, media with picture is 23
 	 * @param u
 	 * @param starttime
 	 * @param endtime
 	 * @return
 	 */
 	public RStatResult setHashUnitArray(String u,String starttime,String endtime){
 		this.unit =  u;
 		if(unit.equals("m")){
 			HashSet<String> unitSet = XTimestamp.getHashSetMonth(starttime, endtime);
 			for(String unit: unitSet){
 				this.hashUnitArray.put(unit,new Hashtable<String,int[]>());
 			}
 		}else if(unit.equals("w")){
 			//
 		}
 		return this;
 	}	
 	
 	public void addOnHashUnit(String unitKey, int count){		
 		//LOG.info("addOnHashUnit: "+unitKey+";"+count);		
 		if(hashUnit.containsKey(unitKey)){
 			int num = this.hashUnit.get(unitKey).intValue();
 			this.hashUnit.put(unitKey, num+count);
 		}else{
 			this.hashUnit.put(unitKey, count);
 		}
 	}	
 	
 	public void addOnHashHash(String hashKey, String pairKey, int count){		
 		//LOG.info("addOnHashHash: "+hashKey+";"+pairKey+";"+count);		
 		if(this.hashHash.containsKey(hashKey)){
 			Hashtable<String, Integer> pair = this.hashHash.get(hashKey);	
 			if(pair.containsKey(pairKey)){
 				int num = pair.get(hashKey).intValue();
 				pair.put(pairKey, num+count);					
 			}else{
 				pair.put(pairKey, count);
 			}
 			this.hashHash.put(hashKey, pair);
 		}else{
 			Hashtable<String, Integer> pair = new Hashtable<String,Integer>();
 			pair.put(pairKey, count);
 			this.hashHash.put(hashKey,pair);
 		}
 	}	
 	/**
 	 * eg. unitKey=> month, typeKey => s1, itemIndex => status(0,1,2,3) or media ("m2":[2],"m3":[1],"m0":[1])
 	 * @param unitKey
 	 * @param hashKey
 	 * @param index
 	 * @param totalItems: to initialize the length of arrays, service needs 4, while media needs 1
 	 */
 	public void addOnUnitHashArray(String unitKey, String typeKey, int itemIndex,int totalItems){		
 		//LOG.info("addOnUnitHashArray: "+unitKey+";"+typeKey+";"+itemIndex);		
 		if(this.hashUnitArray.containsKey(unitKey)){ // month this should have because I already set the unit, but in case. 
 			Hashtable<String, int[]> pair = this.hashUnitArray.get(unitKey);	
 			if(pair.containsKey(typeKey)){  // numerator's type
 				int[] items = pair.get(typeKey);
 				items[itemIndex]++ ;
 				pair.put(typeKey, items);					
 			}else{
 				int[] items = new int[totalItems];
 				for(int i=0;i<items.length;i++) items[i] = 0;
 				items[itemIndex]++;
 				pair.put(typeKey, items);
 			}
 			this.hashUnitArray.put(unitKey, pair);
 		}else{
 			Hashtable<String, int[]> pair = new Hashtable<String,int[]>();
 			int[] items = new int[totalItems];
 			for(int i=0;i<items.length;i++) items[i] = 0;
 			items[itemIndex]++;
 			pair.put(typeKey, items);
 			this.hashUnitArray.put(unitKey,pair);
 		}
 		//LOG.info("addOnUnitHashArray:new result:  "+this.hashUnitArray.toString());
 	}	
 	
 	public void mergeUnitHashArray(Hashtable<String, Hashtable<String, int[]>> hashArray){
 		//LOG.info("mergeUnitHashArray: orign=> "+this.hashUnitArray.toString()+";append=>"+hashArray.toString());
 		for(String unit: hashArray.keySet()){ // month
 			Hashtable<String, int[]> numerator = hashArray.get(unit);
 			if(this.hashUnitArray.containsKey(unit)){ // month
 				Hashtable<String, int[]> orig_numerator = this.hashUnitArray.get(unit);
 				for(String type: numerator.keySet()){ // type of numerator
 					int[] items = numerator.get(type);
 					if(orig_numerator.containsKey(type)){ // mulitple types of numerators
 						int[] orign_items = orig_numerator.get(type);
 						for(int i=0;i<orign_items.length;i++){
 							orign_items[i]+= items[i];
 						}
 					}else{ // if the type of numerator does not exist
 						orig_numerator.put(type, items);
 					}					
 				}
 				
 			}else{  // the month
 				this.hashUnitArray.put(unit, numerator);
 			}
 		}
 		
 		//LOG.info("mergeUnitHashArray: new=> "+this.hashUnitArray.toString());
 		
 	}
 	
 	
 	
 	public void setRegion(String r){
 		this.region = r;
 	}	
 	/**
 	 * row key: region-20130405-block-hcaid-pid
 	 * @param rowKey
 	 * @return
 	 */
 	public String parseUnitKey(String rowKey){
 		//LOG.info("parseUnitKey: rowkey: " + rowKey);
 		String result = null;
 		if(this.unit.equals("m")){
 			result = rowKey.split(XConstants.ROW_KEY_DELIMETER)[1].substring(0, 6);
 		}else if(this.unit.equals("w")){
 			result = rowKey.split(XConstants.ROW_KEY_DELIMETER)[1].substring(0,6); // problems
 		}
 		//LOG.info("the parsed unit key : "+ result);
 		return result;
 	}
 
 	public Hashtable<String, Integer> getHashUnit() {
 		return hashUnit;
 	}
 
 	public String getRegion() {
 		return region;
 	}
 
 	public Hashtable<String, Hashtable<String, int[]>> getHashUnitArray() {
 		return hashUnitArray;
 	}
 	
 	
 }

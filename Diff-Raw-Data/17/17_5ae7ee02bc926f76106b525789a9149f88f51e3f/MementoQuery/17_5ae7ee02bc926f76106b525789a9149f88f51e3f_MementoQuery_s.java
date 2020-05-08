 /**
  * 
  */
 package models.memento;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Andrew Jackson <Andrew.Jackson@bl.uk>
  *
  */
 public class MementoQuery extends MementoSearchBean {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -7116498827412293266L;
 	
 	public boolean isUrlSet() {
 		if( this.getUrl() == null ) return false;
 		if( "".equals(this.getUrl())) return false;
 		if( "http://".equals(this.getUrl())) return false;
 		return true;
 	}
 
 	public boolean hasValidResults() {
 		if( ! this.isUrlSet() ) return false;
 		if( this.getErrorMessage() != null ) return false;
 		return true;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getHostPieData() {
 		String val = "";
 		Map<String,Integer> hc = sortByValue(this.getHostCounts());
 		for( String host : hc.keySet() ) {
 			val += "{ label: \""+host+"\", value: "+hc.get(host)+" },";
 		}
 		return val;		
 	}
 	
 	public Map<String,String> getHostSelection() {
         Map<String, String> result = new LinkedHashMap<String, String>();
        result.put("","Any");
         for( String h : this.getHosts() ) {
         	result.put(h, h);
         }
         return result;
 	}
 	
 	private static Map<String, Integer> sortByValue(Map<String, Integer> map) {
         List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(map.entrySet());
 
         Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
 
             public int compare(Map.Entry<String, Integer> m1, Map.Entry<String, Integer> m2) {
                 return (m2.getValue()).compareTo(m1.getValue());
             }
         });
 
         Map<String, Integer> result = new LinkedHashMap<String, Integer>();
         for (Map.Entry<String, Integer> entry : list) {
             result.put(entry.getKey(), entry.getValue());
         }
         return result;
     }
 }

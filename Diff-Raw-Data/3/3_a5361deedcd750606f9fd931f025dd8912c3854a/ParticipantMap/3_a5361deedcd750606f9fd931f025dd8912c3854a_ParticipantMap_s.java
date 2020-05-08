 package fuschia.tagger.common;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * Best case scenario is to get mappings from S1 to other surveys
  * @author morteza
  *
  */
 public class ParticipantMap{
     private Map<String, String> s1s2Map = new ConcurrentHashMap<String, String>();
     private Map<String, String> s1s3Map = new ConcurrentHashMap<String, String>();
     
     /**
      * Optional (for speedup situations)
      */
     // private Map<String, String> s2s3Map = new ConcurrentHashMap<String, String>();
 
     synchronized public void put(String keyS1, String keyS2, String keyS3){
         s1s2Map.put(keyS1, keyS2);
         s1s2Map.put(keyS2, keyS1);
         s1s3Map.put(keyS1, keyS3);
         s1s3Map.put(keyS3, keyS1);  
         // s2s3Map.put(keyS2, keyS3);
         // s2s3Map.put(keyS3, keyS2);     
     }
 
     public boolean containsKey(String key){
     	boolean result = s1s2Map.containsKey(key) | s1s3Map.containsKey(key) /*| s2s3Map.containsKey(key)*/;
         return result;
     }
 
     public boolean containsValue(String value){
     	boolean result = s1s2Map.containsValue(value) | s1s3Map.containsValue(value) /*| s2s3Map.containsValue(value)*/;
     	return result;
     }
 
     public String getS1(String key){
     	if ((key==null) || (key.trim().length() == 0))
     			return null;
     	
     	if (key.toUpperCase().charAt(key.length()-1) == 'B') {
     		return s1s2Map.get(key);
     	} else if (key.toUpperCase().charAt(key.length()-1) == 'C') {
     		return s1s3Map.get(key);
     	}
     	
     	// key is already in S1 format
         return key;
     }
 
     public String getS2(String key){
     	if ((key==null) || (key.trim().length() == 0)) 
     			return null;
     	
     	if (key.toUpperCase().charAt(key.length()-1) == 'B') {
     		return key;
     	} else if (key.toUpperCase().charAt(key.length()-1) == 'C') {
     		return s1s3Map.get(key);
     	}
 
     	// key is in S1 format
         return s1s2Map.get(key);
     }
     
     public String getS3(String key) {
     	if ((key==null) || (key.trim().length() == 0))
     			return null;
     	
     	if (key.toUpperCase().charAt(key.length()-1) == 'B') {
    		return s1s2Map.get(key);
     	} else if (key.toUpperCase().charAt(key.length()-1) == 'C') {
     		return key;
     	}
     	
     	// key is in S1 format
         return s1s3Map.get(key);
     }
 }

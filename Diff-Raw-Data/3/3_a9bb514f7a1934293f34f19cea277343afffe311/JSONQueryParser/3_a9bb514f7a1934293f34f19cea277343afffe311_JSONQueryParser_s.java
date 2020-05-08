 package io.seqware.queryengine.sandbox.testing.utils;
 
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class JSONQueryParser {
     private HashMap<String, String> readSetQuery;
     private HashMap<String, String> readsQuery; 
     private HashMap<String, String> featureMapQuery;
     private HashMap<String, String> featureSetMapQuery;
     private HashMap<String, String> regionMapQuery;
     
     public JSONQueryParser(String queryJSON) throws JSONException {
         JSONObject query = new JSONObject(queryJSON);
        Iterator<String> outerKeys = query.keys();
         JSONArray regionArray = new JSONArray(); 
         
         readSetQuery = new HashMap<String, String>();
         readsQuery = new HashMap<String, String>();
         featureMapQuery = new HashMap<String, String>();
         featureSetMapQuery = new HashMap<String, String>();
         regionMapQuery = new HashMap<String, String>();
 
 		//Generate missing keys if they are blank in the query. 
 		do{
 			JSONObject emptyObject = new JSONObject("{}");
 			JSONArray emptyArray = new JSONArray("[]");
 			if (!query.has("features")){
 				query.put("features", emptyObject);
 			} else if (!query.has("feature_sets")){
 				query.put("feature_sets", emptyObject);
 			} else if (!query.has("reads")){
 				query.put("reads", emptyObject);
 			} else if (!query.has("read_sets")){
 				query.put("read_sets", emptyObject);
 			} else if (!query.has("regions")){
 				query.put("regions", emptyArray);
 			}
 		} while (query.length() != 5);
 		
 		//READ THE JSON INPUT FILE
 		/**	"OutKey":
 		{
 			"InKey": "jsonObInner.get(InKey)"
 		}*/
         while (outerKeys.hasNext()) {
             String outKey = outerKeys.next();
             if (query.get(outKey) instanceof JSONObject) {
                 JSONObject jsonObInner = query.getJSONObject(outKey);
                 Iterator<String> innerKeys = jsonObInner.keys();
                 while (innerKeys.hasNext()) {
                     String inKey = innerKeys.next();
                     if (outKey.equals("read_sets")) {
                         readSetQuery.put(inKey, jsonObInner.getString(inKey));
                     }
                     if (outKey.equals("reads")) {
                         readsQuery.put(inKey, jsonObInner.getString(inKey));
                     }
                     if (outKey.equals("feature_sets")){
                         featureSetMapQuery.put(inKey, 
                             jsonObInner.getString(inKey));
                     }
                     if (outKey.equals("features")){
                         featureMapQuery.put(inKey, 
                             jsonObInner.getString(inKey));
                     }
                 }
                 innerKeys = null;
             } else if (query.get(outKey) instanceof JSONArray) {
                 JSONArray jsonArInner = query.getJSONArray(outKey);
                 if(outKey.equals("regions")) {
                     regionArray = query.getJSONArray(outKey);
                     
                     for (int i=0; i< regionArray.length(); i++) {
                         String region = regionArray
                             .get(i)
                             .toString();
                         
                         if (region.contains(":") == false) {
                             
                             //i.e. selects "22" from "chr22"
                             String chromosomeID = region.substring(
                                 region.indexOf("r")+1,
                                 region.length());
                             
                             regionMapQuery.put(chromosomeID.toString(), 
                                 ".");
                           
                         } else if (region.contains(":") == true) {
                             
                             //i.e. selects "22" from "chr22:1-99999"
                             String chromosomeID = region.substring(
                                 region.indexOf("r")+1,
                                 region.indexOf(":"));
                             
                             String range = region.substring(  
                                 region.indexOf(":")+1,
                                 region.length());
                             
                             regionMapQuery.put(chromosomeID.toString(), 
                                 range.toString());
                         }
                     }
                 }
             } 
         } 
     }
     
     public HashMap<String, String> getReadsQuery() {
       return readsQuery;
     }
     
     public HashMap<String, String> getReadSetQuery() {
       return readSetQuery;
     }
     
     public HashMap<String, String> getFeatureSetQuery() {
       return featureSetMapQuery;
     }
     
     public HashMap<String, String> getFeaturesQuery() {
       return featureMapQuery;
     }
     
     public HashMap<String, String> getRegionsQuery() {
       return regionMapQuery;
     }
 }

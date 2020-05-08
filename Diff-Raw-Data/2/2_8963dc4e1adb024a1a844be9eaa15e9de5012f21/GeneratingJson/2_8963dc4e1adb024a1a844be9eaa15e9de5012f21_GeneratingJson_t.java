 package org.openmrs.module.chartsearch;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 /**
  * Created by Eli on 16/03/14.
  */
 
 public class GeneratingJson {
     public static String generateJson(){
         SearchAPI searchAPI =SearchAPI.getInstance();
         JSONObject jsonToReturn = new JSONObject();  //returning this object
         JSONArray arr_of_obs = new JSONArray();
         JSONObject obs ;
         for(ChartListItem item : searchAPI.getResults()){ //foreach item from the search we populate the json
             obs = new JSONObject();
 
             obs.put("date",item.getObsDate());
            obs.put("concept_name", item.getConceptName());
             obs.put("value", item.getValue());
             obs.put("location", item.getLocation());
             //TODO add locations, add not only observations. 
             arr_of_obs.add(obs);
         }
         jsonToReturn.put("observations", arr_of_obs); //add the array to the json
 
         return jsonToReturn.toString();
     }
 
 }

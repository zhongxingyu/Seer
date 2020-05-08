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
         for(ChartListItem item : searchAPI.getResults()){ //foreach item from the search we populate the json
            arr_of_obs.add(item.getObsDate());
            arr_of_obs.add(item.getConceptName());
            arr_of_obs.add(item.getValue());
            arr_of_obs.add(item.getLocation());
            //TODO add locations, add not only observations.
            jsonToReturn.put("observations", arr_of_obs); //add the array to the json
 
         }
 
         return jsonToReturn.toString();
     }
 
 }

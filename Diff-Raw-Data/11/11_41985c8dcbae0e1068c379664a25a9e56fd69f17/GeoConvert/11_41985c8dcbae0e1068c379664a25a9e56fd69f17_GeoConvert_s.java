 package com.convert;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Set;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.*;
 
 public class GeoConvert {
 
 	private static String readFile(FileReader fileIn) {
 		BufferedReader in = new BufferedReader(fileIn);
 		StringBuilder sb = new StringBuilder();
 		try {
 			String temp = in.readLine();
 			while (temp != null) {
 				sb.append(temp);
 				temp = in.readLine();
 			}
 			in.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return sb.toString();
 	}
 	
 	/**
 	 *  { "type": "Feature",
      *    "geometry": {
      *        "type": "LineString",
      *        "coordinates": [
      *           [102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0]
      *           ]
      *       }
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		FileReader fileIn;
 		String jsonText = "";
 		
 		try {
 			fileIn = new FileReader("N:\\sample_json_data.txt");
 			jsonText = readFile(fileIn);
 
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		  JSONParser parser = new JSONParser();
 		                
 		  try{
 		    JSONObject jsobj = (JSONObject) parser.parse(jsonText);
 		    GeoJSONFeature feature = new GeoJSONFeature();
 		    JSONArray tempCoordinates = new JSONArray();
 		    
 		    JSONArray metrics = (JSONArray) jsobj.get("metrics");
 		    JSONArray tests = (JSONArray) jsobj.get("tests");
 		    JSONArray conditions = (JSONArray) jsobj.get("conditions");
 		    Set coll = jsobj.entrySet();
 		    for (Object temp : coll) {
 		    	System.out.println(temp.toString());
 		    }
  
 		    for (Object temp : metrics) {
 		    	JSONObject tempjsobj = (JSONObject) temp;
 
 		    	if (tempjsobj.containsKey("longitude") && tempjsobj.containsKey("latitude")) {
 			    	JSONArray tempArray = new JSONArray();
 			    	tempArray.add(tempjsobj.get("latitude"));
 			    	tempArray.add(tempjsobj.get("longitude"));
 		    		
 			    	tempCoordinates.add(tempArray);
 			    	
 		    		//System.out.println(tempjsobj.toString());
 		    	}
 		    }
 		    GeoJSONGeometry geometry = new GeoJSONGeometry("LineString", tempCoordinates);
 		    feature.addGeometry(geometry);
 		    
		    JSONArray aggregated = new JSONArray();
 		    for (Object temp : metrics) {
 		    	JSONObject tempJSObj = (JSONObject) temp;
 		    	for (Object temp2 : tempJSObj.keySet()) {
		    		aggregated.add(temp2.toString() + ":" + tempJSObj.get(temp2));
 		    	}
 		    }
 		    for (Object temp : tests) {
 		    	JSONObject tempJSObj = (JSONObject) temp;
 		    	for (Object temp2 : tempJSObj.keySet()) {
		    		aggregated.add(temp2.toString() + ":" + tempJSObj.get(temp2));
 		    	}
 		    }
 		    for (Object temp : conditions) {
 		    	JSONObject tempJSObj = (JSONObject) temp;
 		    	for (Object temp2 : tempJSObj.keySet()) {
		    		aggregated.add(temp2.toString() + ":" + tempJSObj.get(temp2));
 		    	}
 		    }
 		    
 		    feature.addProperties(aggregated);
 		    
 		    System.out.println(feature);
 		  }
 		  catch(ParseException pe){
 		    System.out.println("position: " + pe.getPosition());
 		    System.out.println(pe);
 		  }
 	}
 
 }

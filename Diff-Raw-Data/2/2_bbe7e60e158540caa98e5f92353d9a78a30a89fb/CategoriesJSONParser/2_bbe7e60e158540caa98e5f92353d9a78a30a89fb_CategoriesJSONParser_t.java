 package com.example.bagueapp.ipmedt4.jrtl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /** A class to parse json data */
 public class CategoriesJSONParser {
 	
 	// Receives a JSONObject and returns a list
 	public List<HashMap<String,Object>> parse(JSONObject jObject){		
 		
 		JSONArray jCountries = null;
 		try {		
 			// Retrieves all the elements in the 'countries' array 
 			jCountries = jObject.getJSONArray("categories");
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
 		 // Invoking getCountries with the array of json object
 		 // where each json object represent a country
 		return getCountries(jCountries);
 	}
 	
 	
 	private List<HashMap<String, Object>> getCountries(JSONArray jCountries){
 		int countryCount = jCountries.length();
 		List<HashMap<String, Object>> countryList = new ArrayList<HashMap<String,Object>>();
 		HashMap<String, Object> country = null;	
 
 		// Taking each country, parses and adds to list object 
 		for(int i=0; i<countryCount;i++){
 			try {
 				// Call getCountry with country JSON object to parse the country 
 				country = getCountry((JSONObject)jCountries.get(i));
 				countryList.add(country);
 
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return countryList;
 	}
 	
 	// Parsing the Country JSON object 
 	private HashMap<String, Object> getCountry(JSONObject jCountry){
 
 		HashMap<String, Object> country = new HashMap<String, Object>();
 		String ID = "";
 		String category_id="";
 		String category_img="";
 		String category_name = "";
 		String abbreviation = "";
 		
 		try {
 			ID = jCountry.getString("id");
 			category_id = jCountry.getString("category_id");
 			category_img = jCountry.getString("category_img");
 			category_name = jCountry.getString("category_name");
 			abbreviation = jCountry.getString("abbreviation");
 
 			
 			String details =        "SubCategorie : " + category_name + "\n" +
                     "Afkorting : " + abbreviation + "\n" ;
 			
 			country.put("country", ID);
 			country.put("category_id", category_id);
			country.put("category_img", R.drawable.blank);
 			country.put("flag_path", category_img);
 			country.put("details", details);
 			
 		} catch (JSONException e) {			
 			e.printStackTrace();
 		}		
 		return country;
 	}
 }

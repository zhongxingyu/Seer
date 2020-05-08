 package components;
 
 import java.util.HashSet;
 import java.util.List;
 import com.google.appengine.api.datastore.*;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.labs.repackaged.org.json.*;
 
 public class DataProvider {
 
 	JSONArray jsonArray = new JSONArray();
 	JSONObject returnJsonObj = new JSONObject();
 	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 	
 	
 	/*
 	 * Panel CRUD methods
 	 */
 	public JSONObject getPanels() {
 		Query query = new Query("Panel").addSort("manufacturer", Query.SortDirection.ASCENDING);
 		query.getKind();
 		List<Entity> panels = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
 		
 		try {
 			if (panels.size() > 0) {
 		   
 		    	for (Entity panelInList : panels) {
 		    		JSONObject panelObj = new JSONObject();	        	
 					panelObj.put("manufacturer", panelInList.getProperty("manufacturer"));
 					panelObj.put("model", panelInList.getProperty("model"));
 					panelObj.put("power", panelInList.getProperty("power") + "W");
 					
 					jsonArray.put(panelObj);
 		    	}
 		        
 		        returnJsonObj.put("Panels", jsonArray);
 		        returnJsonObj.put("Success", true);
 			}
 			else {
 				returnJsonObj.put("Success", "empty");
 			}
 		}
 		catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return returnJsonObj;
 	}
 	
 	public JSONObject getPanel(String model){
 		Query query = new Query("Panel");
 		Query.Filter filter = new Query.FilterPredicate("model", FilterOperator.EQUAL, model);
 		query.setFilter(filter);
 		PreparedQuery pq = datastore.prepare(query);
 		Entity e = pq.asSingleEntity();
 		
 		try {
 			JSONObject panelObj = new JSONObject();
 			panelObj.put("manufacturer", e.getProperty("manufacturer"));
 			panelObj.put("model", e.getProperty("model"));
 			panelObj.put("power", e.getProperty("power") + "W");
 			
 			returnJsonObj.put("Panels", panelObj);
 	        returnJsonObj.put("Success", true);
 		}
 		catch (JSONException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}		
 		
 		return returnJsonObj;
 	}
 	
 	public JSONObject getPanelManufacturers() {
 		Query query = new Query("Panel");
 		query.getKind();
 		List<Entity> panelList = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());	
 		
 		try {
 			if (panelList.size() > 0) {
 
 				HashSet<String> panels = new HashSet<String>();
 				
 				// convert List<Entity> to unique manufacturer values in HashSet<String>
 		    	for (Entity panelInList : panelList) {
 		    		String m = panelInList.getProperty("manufacturer").toString();
 		    		panels.add(m);
 		    	}
 		    	
 		    	// add manufacturer values in HashSet to JSONArray
 		    	for (String s : panels) {
 		    		JSONObject panelObj = new JSONObject();	        	
 					panelObj.put("manufacturer", s);
 					
 					jsonArray.put(panelObj);
 		    	}
 		        
 		        returnJsonObj.put("Panels", jsonArray);
 		        returnJsonObj.put("Success", true);
 			}
 			else {
 				returnJsonObj.put("Success", "empty");
 			}
 		}
 		catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return returnJsonObj;
 	}
 	
 	public JSONObject getPanelModels(String manufacturer) {
 		Query query = new Query("Panel");
 		Query.Filter filter = new Query.FilterPredicate("manufacturer", FilterOperator.EQUAL, manufacturer);
 		query.setFilter(filter);
 		List<Entity> panelList = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());	
 		
 		try {
 			if (panelList.size() > 0) {
 
 				HashSet<String> panels = new HashSet<String>();
 				
 				// convert List<Entity> to unique manufacturer values in HashSet<String>
 		    	for (Entity panelInList : panelList) {
 		    		String m = panelInList.getProperty("model").toString();
 		    		panels.add(m);
 		    	}
 		    	
 		    	// add manufacturer values in HashSet to JSONArray
 		    	for (String s : panels) {
 		    		JSONObject panelObj = new JSONObject();	        	
 					panelObj.put("model", s);
 					
 					jsonArray.put(panelObj);
 		    	}
 		        
 		        returnJsonObj.put("Panels", jsonArray);
 		        returnJsonObj.put("Success", true);
 			}
 			else {
 				returnJsonObj.put("Success", "empty");
 			}
 		}
 		catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return returnJsonObj;
 	}
 	
 	public JSONObject getPanelPower(String model) {
 		Query query = new Query("Panel");
 		Query.Filter filter = new Query.FilterPredicate("model", FilterOperator.EQUAL, model);
 		query.setFilter(filter);
 		List<Entity> panelList = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());	
 		
 		try {
 			if (panelList.size() > 0) {
 
 				HashSet<String> panels = new HashSet<String>();
 				
 				// convert List<Entity> to unique manufacturer values in HashSet<String>
 		    	for (Entity panelInList : panelList) {
 		    		String m = panelInList.getProperty("power").toString();
 		    		panels.add(m);
 		    	}
 		    	
 		    	// add manufacturer values in HashSet to JSONArray
 		    	for (String s : panels) {
 		    		JSONObject panelObj = new JSONObject();	        	
 					panelObj.put("power", s);
 					
 					jsonArray.put(panelObj);
 		    	}
 		        
 		        returnJsonObj.put("Panels", jsonArray);
 		        returnJsonObj.put("Success", true);
 			}
 			else {
 				returnJsonObj.put("Success", "empty");
 			}
 		}
 		catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return returnJsonObj;
 	}
 	
 	public JSONObject addPanel(Panel panel) {		
 		Key panelKey = KeyFactory.createKey("Panel", panel.getModel());		
 		Entity e = new Entity("Panel", panelKey);
 		e.setProperty("manufacturer", panel.getManufacturer());
 		e.setProperty("model", panel.getModel());
 		e.setProperty("power", panel.getPower());
 		
         datastore.put(e);
         
 		return getPanels();
 	}
 	
 	public JSONObject deletePanel(String model) {
 		Query query = new Query("Panel");
 		Query.Filter filter = new Query.FilterPredicate("model", FilterOperator.EQUAL, model);
 		query.setFilter(filter);
 		PreparedQuery pq = datastore.prepare(query);
 		Entity e = pq.asSingleEntity();
 		datastore.delete(e.getKey());		
 		
 		return getPanels();
 	}
 	
 	
 	
 	
 	
 	/*
 	 * Inverter CRUD methods
 	 */
 	public JSONObject getInverters() {
 		Query query = new Query("Inverter").addSort("manufacturer", Query.SortDirection.ASCENDING);
 		query.getKind();
 		List<Entity> inverters = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
 		
 		try {
 			if (inverters.size() > 0) {
 		   
 		    	for (Entity inverterInList : inverters) {
 		    		JSONObject panelObj = new JSONObject();	        	
 					panelObj.put("manufacturer", inverterInList.getProperty("manufacturer"));
 					panelObj.put("model", inverterInList.getProperty("model"));
 					panelObj.put("efficiency", inverterInList.getProperty("efficiency") + "%");
 					
 					jsonArray.put(panelObj);
 		    	}
 		        
 		        returnJsonObj.put("Inverters", jsonArray);
 		        returnJsonObj.put("Success", true);
 			}
 			else {
 				returnJsonObj.put("Success", "empty");
 			}
 		}
 		catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return returnJsonObj;
 	}
 	
 	public JSONObject getInverter(String model){
 		Query query = new Query("Inverter");
 		Query.Filter filter = new Query.FilterPredicate("model", FilterOperator.EQUAL, model);
 		query.setFilter(filter);
 		PreparedQuery pq = datastore.prepare(query);
 		Entity e = pq.asSingleEntity();
 		
 		try {
 			JSONObject panelObj = new JSONObject();
 			panelObj.put("manufacturer", e.getProperty("manufacturer"));
 			panelObj.put("model", e.getProperty("model"));
 			panelObj.put("efficiency", e.getProperty("efficiency") + "%");
 			
 			returnJsonObj.put("Inverters", panelObj);
 	        returnJsonObj.put("Success", true);
 		}
 		catch (JSONException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}		
 		
 		return returnJsonObj;
 	}
 	
 	public JSONObject getInverterManufacturers() {
 		Query query = new Query("Inverter");
 		query.getKind();
 		List<Entity> inverterList = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());		
 		
 		try {
 			if (inverterList.size() > 0) {
 
 				HashSet<String> inverters = new HashSet<String>();
 				
 				// convert List<Entity> to unique manufacturer values in HashSet<String>
 		    	for (Entity invertersInList : inverterList) {
 		    		String m = invertersInList.getProperty("manufacturer").toString();
 		    		inverters.add(m);
 		    	}
 		    	
 		    	// add manufacturer values in HashSet to JSONArray
 		    	for (String s : inverters) {
 		    		JSONObject panelObj = new JSONObject();	        	
 					panelObj.put("manufacturer", s);
 					
 					jsonArray.put(panelObj);
 		    	}
 		        
 		        returnJsonObj.put("Inverters", jsonArray);
 		        returnJsonObj.put("Success", true);
 			}
 			else {
 				returnJsonObj.put("Success", "empty");
 			}
 		}
 		catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return returnJsonObj;
 	}
 	
 	public JSONObject getInverterModels(String manufacturer) {
 		Query query = new Query("Inverter");
 		Query.Filter filter = new Query.FilterPredicate("manufacturer", FilterOperator.EQUAL, manufacturer);
 		query.setFilter(filter);
 		List<Entity> inverterList = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());	
 		
 		try {
 			if (inverterList.size() > 0) {
 
 				HashSet<String> inverters = new HashSet<String>();
 				
 				// convert List<Entity> to unique manufacturer values in HashSet<String>
 		    	for (Entity inverterInList : inverterList) {
 		    		String m = inverterInList.getProperty("model").toString();
 		    		inverters.add(m);
 		    	}
 		    	
 		    	// add manufacturer values in HashSet to JSONArray
 		    	for (String s : inverters) {
 		    		JSONObject inverterObj = new JSONObject();	        	
 		    		inverterObj.put("model", s);
 					
 					jsonArray.put(inverterObj);
 		    	}
 		        
 		        returnJsonObj.put("Inverters", jsonArray);
 		        returnJsonObj.put("Success", true);
 			}
 			else {
 				returnJsonObj.put("Success", "empty");
 			}
 		}
 		catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return returnJsonObj;
 	}
 	
 	public JSONObject getInverterEfficiency(String model) {
 		Query query = new Query("Inverter");
 		Query.Filter filter = new Query.FilterPredicate("model", FilterOperator.EQUAL, model);
 		query.setFilter(filter);
 		List<Entity> inverterList = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());	
 		
 		try {
 			if (inverterList.size() > 0) {
 
 				HashSet<String> inverters = new HashSet<String>();
 				
 				// convert List<Entity> to unique efficiency values in HashSet<String>
 		    	for (Entity inverterInList : inverterList) {
 		    		String m = inverterInList.getProperty("efficiency").toString();
 		    		inverters.add(m);
 		    	}
 		    	
 		    	// add efficiency values in HashSet to JSONArray
 		    	for (String s : inverters) {
 		    		JSONObject inverterObj = new JSONObject();	        	
 		    		inverterObj.put("efficiency", s);
 					
 					jsonArray.put(inverterObj);
 		    	}
 		        
 		        returnJsonObj.put("Inverters", jsonArray);
 		        returnJsonObj.put("Success", true);
 			}
 			else {
 				returnJsonObj.put("Success", "empty");
 			}
 		}
 		catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return returnJsonObj;
 	}
 	
 	public JSONObject addInverter(Inverter inverter) {		
		Key inverterKey = KeyFactory.createKey("Inverter", inverter.getModel());		
		Entity e = new Entity("Inverter", inverterKey);
 		e.setProperty("manufacturer", inverter.getManufacturer());
 		e.setProperty("model", inverter.getModel());
		e.setProperty("efficiency", inverter.getEfficiency());
 		
         datastore.put(e);
         
 		return getInverters();
 	}
 	
 	public JSONObject deleteInverter(String model) {
 		Query query = new Query("Inverter");
 		Query.Filter filter = new Query.FilterPredicate("model", FilterOperator.EQUAL, model);
 		query.setFilter(filter);
 		PreparedQuery pq = datastore.prepare(query);
 		Entity e = pq.asSingleEntity();
 		datastore.delete(e.getKey());		
 		
 		return getInverters();
 	}
 	

 }

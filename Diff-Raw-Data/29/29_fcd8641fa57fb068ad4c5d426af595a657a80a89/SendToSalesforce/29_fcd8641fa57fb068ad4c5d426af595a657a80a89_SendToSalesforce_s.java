 package com.salsaforce.freshsalsa;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.Query;
 import com.google.gson.Gson;
 
 public class SendToSalesforce implements Getable {
 	
 	private OAuthSalesforce auth = new OAuthSalesforce();
 	private static String loginResp = "";
 	private static final int MAX_EVENTS_PER_METHOD = 10000;
 	
 	public SendToSalesforce() {
 		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 		Query query = new Query("Config");
 		List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
 		if (results.size() == 0) {
 			//Not configured
 			return;
 		}
 		String client_id = (String)results.get(0).getProperty("clientID");
 		String client_secret = (String)results.get(0).getProperty("clientSecret");
 		String username = (String)results.get(0).getProperty("username");
 		String password = (String)results.get(0).getProperty("password");
 		String securityToken = (String)results.get(0).getProperty("token");
 		loginResp = auth.login(client_id, client_secret, username, password, securityToken);
 	}
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		resp.setContentType("text/plain");
 		resp.getWriter().println("This page sends the data in the datastore to salesforce. 53");
 		resp.getWriter().println("Login Response: " + loginResp);
 		if (loginResp.equals("")) {
 			resp.getWriter().println("Login failed");
 			return;
 		}
 		
 		processEvents(resp);
 	}
 	
 	/**
 	 * Sends all the events in the database to Salesforce and then deletes them.
 	 * @param resp The response to the get on this page (for debugging).
 	 */
 	private void processEvents(HttpServletResponse resp) throws IOException {
 		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 		
 		//Gets all the Events from the database.
 		Query q = new Query("Event");
 		List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
 
 		List<List<Entity>> listsOfEvents = breakUpEvents(results);
 	    
 	    for (List<Entity> events : listsOfEvents) {
 	    	String json = eventsToJSON(events);
	    	sendEvents(json, resp);
 	    }
 
		List<Key> keysToDelete = new ArrayList<Key>();
		for (Entity e : results) {
			keysToDelete.add(e.getKey());
		}
		datastore.delete(keysToDelete);
 	}
 	
 	/**
 	 * Breaks up the events into separate requests if there are more than salesforces limit.
 	 * @param events All the events currently in the database.
 	 * @return Lists of events that will fit in one request each.
 	 */
 	private List<List<Entity>> breakUpEvents(List<Entity> events) {
 		List<List<Entity>> listsOfEvents = new ArrayList<List<Entity>>();
 		listsOfEvents.add(new ArrayList<Entity>());
 		
 		int count = 0;
 		
 		//Divides up events into lists that are smaller than the salesforce limit on new objects created in one method.
 	    for (Entity event : events) {
 	    	count += event.getProperties().size();
 	    	if (count > MAX_EVENTS_PER_METHOD) {
 	    		count = event.getProperties().size();
 	    		listsOfEvents.add(new ArrayList<Entity>());
 	    	}
     		listsOfEvents.get(listsOfEvents.size() - 1).add(event);
 	    }
 	    return listsOfEvents;
 	}
 	
 	/**
 	 * Sends a list of events to salesforce.
 	 * @param json A list of events formatted as json.
 	 * @param resp The response to the get on this page (for debugging).
 	 * @throws IOException When println fails.
 	 */
	private void sendEvents(String json, HttpServletResponse resp) throws IOException {
 		resp.getWriter().println("Sent Events: " + json);
		resp.getWriter().println("Salesforce Response: "
		    + auth.createObject("/services/apexrest/salsaforce/EventAdder/", "{\"events\":" + json + "}", "application/json"));
 	}
 	
 	/**
 	 * Turns the Events from the database into JSON.
 	 * @param attrs The events from the database.
 	 * @return A JSON string representation of the list of events from the database.
 	 */
 	private String eventsToJSON(List<Entity> events) {
 		List<EventWithAttrs> eventsWithAttrs = new ArrayList<EventWithAttrs>();
 		Gson gson = new Gson();
 		
 		for (Entity event : events) {
 			eventsWithAttrs.add(new EventWithAttrs(event));
 		}
 		
 		return gson.toJson(eventsWithAttrs);
 	}
 }

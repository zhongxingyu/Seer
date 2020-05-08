 package com.appspot.iclifeplanning;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.appspot.analyser.Analyzer;
 import com.appspot.analyser.DeleteSuggestion;
 import com.appspot.analyser.IEvent;
 import com.appspot.analyser.Suggestion;
 import com.appspot.datastore.SphereName;
 import com.appspot.datastore.TokenStore;
 import com.appspot.iclifeplanning.authentication.CalendarUtils;
 import com.appspot.iclifeplanning.events.Event;
 import com.appspot.iclifeplanning.events.EventStore;
 import com.google.appengine.repackaged.org.json.JSONArray;
 import com.google.appengine.repackaged.org.json.JSONException;
 import com.google.appengine.repackaged.org.json.JSONObject;
 
 /**
  * Suggestion servlet. responsible for managing the "optimise button".
  * Initialises the EventStore and runs the analyser to create suggestions.
  * 
  * @author Agnieszka Magda Madurska (amm208@doc.ic.ac.uk)
  * 
  */
 @SuppressWarnings("serial")
 public class SuggestionServlet extends HttpServlet {
 	// This should NOT be stored like this. Reimplement to use memcache at some point.
 	private static Map<String, List<List<Suggestion>>> suggestionMap = new HashMap<String, List<List<Suggestion>>>();
 	
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws IOException {
 
 		System.out.println("Map size: " + suggestionMap.size());
 		String token = TokenStore.getToken(CalendarUtils.getCurrentUserId());
 		CalendarUtils.client.setAuthSubToken(token);
 
 		EventStore eventStore = EventStore.getInstance();
 		eventStore.initizalize();
 		List<Event> events = eventStore.getEvents();
 		// ------------------- Dummy data
 		Analyzer analyser = new Analyzer();
 
 		//List<List<Suggestion>> suggestions = analyser.getSuggestions(events, CalendarUtils.getCurrentUserId(), true);
 		List<List<Suggestion>> suggestions = new ArrayList<List<Suggestion>>();
 		suggestions.add(new ArrayList<Suggestion>());
 		suggestions.add(new ArrayList<Suggestion>());
 		suggestions.add(new ArrayList<Suggestion>());
 		suggestionMap.put(CalendarUtils.getCurrentUserId(), suggestions);
 
 		
 		IEvent event1 = (IEvent)events.get(0);
 		IEvent event2 = (IEvent)events.get(1);
 		IEvent event3 = (IEvent)events.get(2);
 		IEvent event4 = (IEvent)events.get(3);
 		IEvent event5 = (IEvent)events.get(4);
 		IEvent event6 = (IEvent)events.get(5);
 
 		Suggestion sug = new DeleteSuggestion(event1);
 		List<Suggestion> alternatives = new ArrayList<Suggestion>();
 		alternatives.add(new DeleteSuggestion(event4));
 		alternatives.add(new DeleteSuggestion(event5));
 		sug.setAlternativeSuggetions(alternatives);
 		suggestions.get(0).add(sug);
 
 		suggestions.get(0).add(new DeleteSuggestion(event3));
 		
 		sug = new DeleteSuggestion(event2);
 		alternatives = new ArrayList<Suggestion>();
 		alternatives.add(new DeleteSuggestion(event4));
 		alternatives.add(new DeleteSuggestion(event5));
 		sug.setAlternativeSuggetions(alternatives);
 		suggestions.get(1).add(sug);
 
 		suggestions.get(1).add(new DeleteSuggestion(event4));
 		
 		sug = new DeleteSuggestion(event3);
 		alternatives = new ArrayList<Suggestion>();
 		alternatives.add(new DeleteSuggestion(event4));
 		alternatives.add(new DeleteSuggestion(event5));
 		sug.setAlternativeSuggetions(alternatives);
 		suggestions.get(2).add(sug);
 
 		suggestions.get(2).add(new DeleteSuggestion(event5));
 		suggestions.get(2).add(new DeleteSuggestion(event6));
 		// ------------------- Dummy data
 		JSONArray suggestionArray = new JSONArray();
 		List<Suggestion> s;
 		for (int i = 0; i < suggestions.size(); i++) {
			s = suggestions.get(0);
 			suggestionArray.put(suggestionListToJSONArray(s));
 		}
 
 		JSONObject result = new JSONObject();
 		try {
 			result.put("userID", CalendarUtils.getCurrentUserId());
 			result.put("lists", suggestionArray);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		response.getWriter().print(result);
 	}
 
 	private JSONArray suggestionListToJSONArray(List<Suggestion> suggestionList) {
 		Suggestion suggestion;
 		JSONArray suggestionArray = new JSONArray();
 		for (int i = 0; i < suggestionList.size(); i++) {
 			suggestion = suggestionList.get(i);
 			suggestionArray.put(suggestionToJSONArray(suggestion));			
 		}
 		return suggestionArray;
 	}
 
 	private JSONArray suggestionToJSONArray(Suggestion suggestion) {
 		JSONArray suggestionArray = new JSONArray();
 		Suggestion alternativeSuggestion;
 		List<Suggestion> alternativeSuggestions = suggestion.getAlternativeSuggestions();
 		suggestionArray.put(suggestionToJSONObject(suggestion));
 		for (int j = 0; j < alternativeSuggestions.size(); j++) {
 			alternativeSuggestion = alternativeSuggestions.get(j);
 			suggestionArray.put(suggestionToJSONObject(alternativeSuggestion));
 		}
 		return suggestionArray;
 	}
 
 	private JSONObject suggestionToJSONObject(Suggestion suggestion) {
 		JSONObject suggestionObject = new JSONObject();
 		try {
 			suggestionObject.put("title", suggestion.getTitle());
 			suggestionObject.put("description", suggestion.getDescription());
 			suggestionObject.put("repeating", "");
 			
 			SimpleDateFormat date = new SimpleDateFormat("EEE, d MMM yyyy HH:mm");
 			suggestionObject.put("startDateTime", date.format(suggestion.getStartDate().getTime()));
 
 			suggestionObject.put("endDateTime", date.format(suggestion.getEndDate().getTime()));
 			suggestionObject.put("type", suggestion.getType());
 
 			List<String> spheres = new ArrayList<String>();
 			for (SphereName sphere : suggestion.getSpheres().keySet()) {
 				if (suggestion.getSpheres().get(sphere) > 0) {
 					spheres.add(sphere.name());
 				}
 			}
 			suggestionObject.put("spheres", spheres);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return suggestionObject;
 	}
 
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 		throws IOException {
 
 		JSONObject suggestionsJSON = null;
 
 		try {
 			suggestionsJSON = new JSONObject(request.getReader().toString());
 			String userID = suggestionsJSON.getString("userID");
 			int list = suggestionsJSON.getInt("listNumber");
 			JSONArray acceptedSuggestions = suggestionsJSON.getJSONArray("suggestions");
 			int lenght = acceptedSuggestions.length();
 			JSONObject suggestionJSON;
 			int suggestion;
 			int alternative;
 			String key;
 			
 			List<List<Suggestion>> suggestions = suggestionMap.get(userID);
 
 			for(int i = 0; i < lenght; i++) {
 				suggestionJSON = acceptedSuggestions.getJSONObject(i);
 				key = suggestionJSON.keys().toString();
 				suggestion = Integer.parseInt(key);
 				alternative = suggestionJSON.getInt(key);
 				suggestions.get(list).get(suggestion).makePersistent(alternative);
 			}
 		} catch (JSONException e) {
 			System.out.println("Badly formatted JSON!");
 			e.printStackTrace();
 		}
 	}
 }

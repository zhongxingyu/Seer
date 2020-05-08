 package com.appspot.iclifeplanning;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.appspot.analyser.Analyzer;
 import com.appspot.analyser.BaseCalendarSlot;
 import com.appspot.analyser.DeleteSuggestion;
 import com.appspot.analyser.IEvent;
import com.appspot.analyser.Pair;
 import com.appspot.analyser.Proposal;
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
 	
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws IOException {
 		String token = TokenStore.getToken(CalendarUtils.getCurrentUserId());
 		CalendarUtils.client.setAuthSubToken(token);
 		EventStore eventStore = EventStore.getInstance();
 		eventStore.initizalize();
 		Collection<Event> events = eventStore.getEvents();
 		// ------------------- Dummy data
 		// Analyzer analyser = new Analyzer();
 		List<Suggestion> suggestions = new ArrayList();// analyser.getSuggestions(events, CalendarUtils.getCurrentUserId());
 		Calendar startCalendar = new GregorianCalendar(110, 11, 19, 14, 00);
 		Calendar endCalendar = new GregorianCalendar(110, 11, 19, 15, 00);
		Proposal p = new Proposal("Go for a walk", "", startCalendar, endCalendar);
		double first = 1;
		double second = 2;
		p.setDurationInterval(new Pair(first, second));
		IEvent event1 = p;
 		startCalendar = new GregorianCalendar(110, 11, 20, 12, 00);
 		endCalendar = new GregorianCalendar(110, 11, 20, 13, 00);
		p = new Proposal("Meet Sarah for lunch", "", startCalendar, endCalendar);
		first = 1;
		second = 2;
		p.setDurationInterval(new Pair(first, second));
		IEvent event2 = p;
 		startCalendar = new GregorianCalendar(110, 11, 20, 16, 00);
 		endCalendar = new GregorianCalendar(110, 11, 20, 17, 00);
		p = new Proposal("Pick up Rich from school", "", startCalendar, endCalendar);
		first = 1;
		second = 2;
		p.setDurationInterval(new Pair(first, second));
		IEvent event3 = p;
 		Suggestion sug = new DeleteSuggestion(event1);
 		suggestions.add(sug);
 		sug = new DeleteSuggestion(event2);
 		suggestions.add(sug);
 		sug = new DeleteSuggestion(event3);
 		suggestions.add(sug);
 		// ------------------- Dummy data
 		JSONArray suggestionArray = new JSONArray();
 		for (Suggestion s : suggestions) {
 			suggestionArray.put(suggestionToJSONOBject(s));
 		}
 		
 		response.getWriter().print(suggestionArray);
 	}
 
 	private JSONObject suggestionToJSONOBject(Suggestion s) {
 		JSONObject suggestionObject = new JSONObject();
 		try {
 			suggestionObject.put("title", s.getTitle());
 			suggestionObject.put("description", s.getDescription());
 			suggestionObject.put("repeating", "");
 			
 			SimpleDateFormat date = new SimpleDateFormat("EEE, d MMM yyyy HH:mm");
 			suggestionObject.put("startDateTime", date.format(s.getStartDate().getTime()));
 
 			suggestionObject.put("endDateTime", date.format(s.getEndDate().getTime()));
 			suggestionObject.put("type", s.getType());
 			
 			List<String> spheres = new ArrayList<String>();
 			for (SphereName sphere : s.getSpheres().keySet()) {
 				if (s.getSpheres().get(sphere) > 0) {
 					spheres.add(sphere.name());
 				}
 			}
 			suggestionObject.put("spheres", spheres);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return suggestionObject;
 	}
 }

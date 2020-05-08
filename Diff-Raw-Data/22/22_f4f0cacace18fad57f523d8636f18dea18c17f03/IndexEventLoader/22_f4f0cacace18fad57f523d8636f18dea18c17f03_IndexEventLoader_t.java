 package com.alvarosantisteban.berlincurator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 
 public class IndexEventLoader implements EventLoader {
 	
 	public final static String WEBSITE_URL = "http://www.indexberlin.de/openings-and-events";
 	public final static String WEBSITE_NAME = "Index";
 	//public final static String ART = "Art (Exhibitions, Talks, etc)";
 
 	@Override
 	public List<Event> load(Context context) {
 		String html = WebUtils.downloadHtml(WEBSITE_URL, context);
 		if(html.equals("Exception")){
 			return null;
 		}
 		try{
 			return extractEventsFromIndex(html);
 		}catch(ArrayIndexOutOfBoundsException exception){
 			System.out.println("Exception catched extracting the events from Index!");
 			return null;
 		}
 	}
 	
 	/**
 	 * Creates a set of events from the html of the Index website. 
 	 * Each Event has name, day, description, time, location, origin and tag.
 	 * 
 	 * @param theHtml the String containing the html from the Index website
 	 * @return a List of Event with the name, day, description, time, location, origin and tag.
 	 */
 	private List<Event> extractEventsFromIndex(String theHtml) throws ArrayIndexOutOfBoundsException{  	
 		String myPattern = "<div class=\"line-thick\">";
 		String[] result = theHtml.split(myPattern);
 		
 		// Throw away the first entry of the array because it does not contain an event
 		List<Event> events = new ArrayList<Event>(result.length-1); 
 		for (int i=1; i<result.length; i++){
 			// Separate up to the first "</h1>"
 			String[] dateAndRest = result[i].split("</h1>", 2);
 			// Get the date
 			String[] dayAndDate = dateAndRest[0].split("<h1>", 2);
 			String[] eventsOfADay = dateAndRest[1].split("<li>"); // Marks the number of events
 			for (int j=1; j<eventsOfADay.length; j++){
 				Event event = new Event();
 				// Format the date and set it
 				String date = Utils.formatDate(dayAndDate[1].trim());
 				event.setDay(date);
 				
 				String[] placeAndRest = eventsOfADay[j].split("</a>",2);
 				String[] placeAndRest2 = placeAndRest[0].split(">");
 				// Set the location
 				//event.setLocation("<a href=\"https://maps.google.es/maps?q="+placeAndRest2[2].trim().replace(' ', '+')+"\">"+placeAndRest2[2].trim()+"</a>");
 				event.setLocation(placeAndRest2[2].trim() +", Berlin");
 				String[] tagAndHour = placeAndRest[1].split("<br />");
 				String[] tagAndMaybeHour = tagAndHour[1].split(",",2);
 				if(tagAndMaybeHour.length > 1){
 					// There is an hour, set it
 					event.setHour(Utils.extractTime(tagAndMaybeHour[1].trim()));
 				}
 				// Set the tag
 				//event.setTag(tagAndMaybeHour[0]);
 				String[] name = placeAndRest[1].split("<em>",2);
 				String[] name2 = name[1].split("</em>",2);
 				if(name2[0].equals("")){
 					// There is no name, we use the tag as name
 					event.setName(tagAndMaybeHour[0]);
 				}else{
 					event.setName(tagAndMaybeHour[0] +": " +name2[0]);
 				}
 				String[] description = placeAndRest[1].split("<br />",2);
 				String[] description2 = description[1].split("<div class=\"cal-holder\">");
				String descriptionWithoutExtraLine = description2[0].replace("<strong>", "Artist/s: ").replace("</strong>", "");
				int lastNewLine = descriptionWithoutExtraLine.lastIndexOf("<br />");
				descriptionWithoutExtraLine = descriptionWithoutExtraLine.substring(0, lastNewLine).trim();
 				// Set the description
				event.setDescription(descriptionWithoutExtraLine);
 				// Set the origin
 				event.setEventsOrigin(WEBSITE_NAME);
 				// Set the origin's website
 				event.setOriginsWebsite(WEBSITE_URL);
 				// Set the Thema tag
 				event.setThemaTag(DateActivity.ART_THEMA_TAG);
 				// Set the type tag
 				event.setTypeTag(extractTypeTag(tagAndMaybeHour[0]));
 				events.add(event);
 			}
 		}
 		return events;
 	}
 
 	
 	private String extractTypeTag(String name) {
 		if(name.contains("Talk")){
 			return DateActivity.TALK_TYPE_TAG;
 		}
 		if(name.contains("Screening")){
 			return DateActivity.SCREENING_TYPE_TAG;
 		}
 		if(name.contains("Party")){
 			return DateActivity.PARTY_TYPE_TAG;
 		}
 		return DateActivity.EXHIBITION_TYPE_TAG;
 	}
 
 	/**
 	 * THIS METHOD HAS NOT BEEN TESTED AND PROBABLY IT WONT BE NEEDED
 	 * 
 	 * Creates a set of events from the html of the Index website. 
 	 * Each Event has name, day, description, time, link and tag.
 	 * 
 	 * @param theHtml the String containing the html from the Index website
 	 * @return a List of Event with the name, day, description, time, link and tag.
 	 */
 	private List<Event> extractEventsFromIndexNotMobile(String theHtml) throws ArrayIndexOutOfBoundsException{  
 		//System.out.println("\n\n\n\n"+theHtml);
 		
 		String myPattern = "<tr class=\"venuesItem\">";
 		String[] result = theHtml.split(myPattern);
 		
 		// Throw away the first entry of the array because it does not contain an event
 		List<Event> events = new ArrayList<Event>(result.length-1); 
 		for (int i=1; i<result.length; i++){
 			System.out.println("i="+i);
 			Event event = new Event();
 			String[] nothingAndPlace = result[i].split("class=\"aul\">",2);
 			String[] placeAndNothing = nothingAndPlace[1].split("</a>",2);
 			// Set the place
 			event.setLocation("<a href=\"https://maps.google.es/maps?q="+placeAndNothing[0].trim().replace(' ', '+')+"\">"+placeAndNothing[0].trim()+"</a>");
 			String[] nothingAndDayTime = placeAndNothing[1].split("<td class=\"col2\"",2);
 			String[] nothingAndDayTime2 = nothingAndDayTime[1].split(">",2);
 			String[] dayTimeAndNothing = nothingAndDayTime2[0].split("<",2);
 			String[] dayAndTime = dayTimeAndNothing[0].split("|");
 			// Set the day
 			event.setDay(Utils.formatDate(dayAndTime[0].trim()));
 			// Set the time, if there is one
 			if (dayAndTime.length >= 3){
 				event.setHour(Utils.convertTo24Hours(Utils.extractTime(dayAndTime[2].trim())));
 			}
 			String[] nothingAndRest = dayTimeAndNothing[1].split("<td class=\"col3\">",2);
 			String[] tagAndRest = nothingAndRest[1].split("<span style=\"color: #777777\">",2);
 			String[] tagAndRest2 = tagAndRest[1].split("<",2);
 			// Set the tag
 			//event.setTag(tagAndRest2[0]);
 			//String[] EverythingAndEnd = tagAndRest2[1].split("</td>",2);
 			String[] name = tagAndRest2[1].split("<div style=\"display:inline; font-style:italic;\">",2);
 			// If the event has no name
 			if (name.length == 1){
 				// Set the tag as name
 				event.setName(tagAndRest2[0]);
 			}else{
 				String[] name2 = name[1].split("<",2);
 				// Set the name
 				event.setName(name2[0].trim());
 			}
 			String[] description = nothingAndRest[1].split("<div class=\"mapExhibitions\">",2);
 			// Set the description
 			event.setDescription(description[0]);
 			events.add(event);
 		}
 		return events;
 	}
 
 }

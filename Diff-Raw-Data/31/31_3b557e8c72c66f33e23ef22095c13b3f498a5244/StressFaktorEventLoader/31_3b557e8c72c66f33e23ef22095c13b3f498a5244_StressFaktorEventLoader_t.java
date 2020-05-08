 package com.alvarosantisteban.pathos.loader;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import com.alvarosantisteban.pathos.Event;
 import com.alvarosantisteban.pathos.R;
 import com.alvarosantisteban.pathos.utils.WebUtils;
 
 import android.content.Context;
 import android.util.Log;
 
 /**
  * Receives the html from the Stress Faktor website and parses the information to create a list of events.
  * 
  * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
  *
  */
 public class StressFaktorEventLoader implements EventLoader {
 	
 	public final static String WEBSITE_URL = "http://stressfaktor.squat.net/termine.php?display=7";
 	public final static String WEBSITE_NAME = "Stress Faktor";
 	
 	/**
 	 * Used for logging purposes
 	 */
 	private static final String TAG = "StressFaktorEventLoader";
 	
 	//Event's topic tags
 	private String POLITICAL_TOPIC_TAG;
 	private String GOING_OUT_TOPIC_TAG;
 	private String ART_TOPIC_TAG;
 				
 	// Event's type tags
 	private String CONCERT_TYPE_TAG;
 	private String PARTY_TYPE_TAG;
 	private String TALK_TYPE_TAG;
 	private String SCREENING_TYPE_TAG;
 	private String EXHIBITION_TYPE_TAG;
 	private String OTHER_TYPE_TAG;		
 	
 
 	@Override
 	public List<Event> load(Context context) {
 		String html = WebUtils.downloadHtml(WEBSITE_URL, context);
 		initializeTags(context);
 		if(html.equals("Exception")){
 			Log.w(TAG, "The html equals to Exception");
 			return null;
 		}
 		try{
 			return extractEventsFromStressFaktor(html);
 		}catch(ArrayIndexOutOfBoundsException e){
 			Log.e(TAG, "ArrayIndexOutOfBoundsException" +e);
 			return null;
 		}
 	}
 	
 	/**
 	 * Initialize the tags used to categorize the topic and the type of each event
 	 * 
 	 * @param context
 	 */
 	private void initializeTags(Context context) {
 		POLITICAL_TOPIC_TAG = context.getResources().getString(R.string.politics_topic_tag);
 		GOING_OUT_TOPIC_TAG = context.getResources().getString(R.string.goingout_topic_tag);
 		ART_TOPIC_TAG = context.getResources().getString(R.string.art_topic_tag);
 		
 		CONCERT_TYPE_TAG = context.getResources().getString(R.string.concert_type_tag);
 		PARTY_TYPE_TAG = context.getResources().getString(R.string.party_type_tag);
 		TALK_TYPE_TAG = context.getResources().getString(R.string.talk_type_tag);
 		SCREENING_TYPE_TAG = context.getResources().getString(R.string.screening_type_tag);
 		EXHIBITION_TYPE_TAG = context.getResources().getString(R.string.exhibition_type_tag);
 		OTHER_TYPE_TAG = context.getResources().getString(R.string.other_type_tag);
 	}
 
 	/**
 	 * Creates a set of events from the html of the Stress Faktor website. 
 	 * Each Event has name, day, time, a sometimes a description and a link.
 	 * 
 	 * @param theHtml the String containing the html from the Stress Faktor website
 	 * @return a List of Event with the name, day, time, description and link
 	 */
 	private List<Event> extractEventsFromStressFaktor(String theHtml) throws ArrayIndexOutOfBoundsException {
 		String[] eventsAndRest = theHtml.split("<!-- Ende Spalte 2 -->");
 		String myPattern = "<table width=\"100%\" bgcolor=\"#000000\" cellpadding=\"3\" cellspacing=\"1\">";
 		String[] result = eventsAndRest[0].split(myPattern);
 		
 		// Use an ArrayList because the number of events is unknown
 		List<Event> events = new ArrayList<Event>(); 
 		for (int i=1; i<result.length; i++){
 			// Separate up to the first "<span class=text2>"
 			String[] dateAndRest = result[i].split("<span class=text2>", 2);
 			// Get the date
 			String[] dayAndDate = dateAndRest[1].split("</b>", 2);
 			String[] eventsOfADay = dateAndRest[1].split("<tr");
 			for (int j=1; j<eventsOfADay.length; j++){
 				Event event = new Event();
 				// Format the date and set it
 				String[] date = dayAndDate[0].split(", ");
 				event.setDay(date[1].replace('.', '/').trim());
 				String[] nothingTimeAndRest = eventsOfADay[j].split("<span class=text2>");
 				String[] timeAndRest = nothingTimeAndRest[1].split(" ",2);	
 				// Set the time
 				event.setHour(timeAndRest[0].replace('.',':').trim());				
 				String[] placeAndRest = nothingTimeAndRest[2].split("</b>",2);
 				// Extract the location
 				String[] place = extractPlace(placeAndRest[0]);
 				// Set the location
 				event.setLocation(place[0]);
 				String[] nameAndRest = placeAndRest[1].split("<br>",2);
 				// set the name
 				String name = nameAndRest[0].replaceFirst(": ", "").trim();	
 				String[] descriptionAndNothing = nameAndRest[1].split("</span>");
 				// Extract the links
 				String[] links = extractLinks(descriptionAndNothing[0]);
 				// Set the links 
 				event.setLinks(links);
 				// Remove the image-links from the description
 				String description = removeImageLinks(descriptionAndNothing[0]);
 				// Set the description
 				event.setDescription(description +place[1]);
 				// Set the origin
 				event.setEventsOrigin(WEBSITE_NAME);
 				// Set the origin's website
 				event.setOriginsWebsite(WEBSITE_URL);	
 				// Set the type tag
 				event.setTypeTag(extractTypeTag(nameAndRest[0].replaceFirst(": ", "").trim()));
 				// Set the thema tag
 				event.setThemaTag(extractThemaTag(event.getTypeTag()));
				String eventTitle = getTitle(description, name, event.getTypeTag());
 				if(eventTitle != null){
 					name = name+": " +eventTitle;
 				}
 				event.setName(name);
 				events.add(event);
 			}
 		}
 		return events;		
 	}
 
 	/**
 	 * Extracts the title of the screening or the name of the first band playing, if there is any. If not, returns null.
 	 * 
 	 * @param description The description of the event, which may contains the title
	 * @param name The name of the event so far
 	 * @param tag the type tag for that event
 	 * @return the title of the screening or the name of the band. For all other tags, return null.
 	 */
	private String getTitle(String description, String name, String tag) {
 		if(tag.equals(SCREENING_TYPE_TAG) || tag.equals(CONCERT_TYPE_TAG)){
			if (name.contains("Peliculoso")){
				return null;
			}
 			String[] startTitle = description.split("\"",3);
			if(startTitle.length == 3){				
 				return startTitle[1].trim();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Extracts the thema tag assuming that the type was previously set and that if is of type "talk", it will be a political event
 	 * and if not, a going out event.
 	 * 
 	 * @param tag the type tag for this event
 	 * @return the thema tag (going out or political)
 	 */
 	private String extractThemaTag(String typeTag) {
 		if(typeTag.equals(TALK_TYPE_TAG)){
 			return POLITICAL_TOPIC_TAG; 
 		}else if(typeTag.equals(EXHIBITION_TYPE_TAG)){
 			return ART_TOPIC_TAG;
 		}
 		return GOING_OUT_TOPIC_TAG;
 	}
 
 	/**
 	 * Extracts the type tag looking for some keywords. Some keywords such as Vku, could also be easily recognized but still belong to
 	 * the "Other" type tag.
 	 * 
 	 * @param text the html with the keyword
 	 * @return the type tag 
 	 */
 	private String extractTypeTag(String text) {
 		String lowerCase = text.toLowerCase(Locale.getDefault());
 		if (lowerCase.contains("konzert") || lowerCase.contains("soli-konzert") || lowerCase.contains("festival") || 
 				lowerCase.contains("maschinenraum") || lowerCase.contains("jam session") || lowerCase.contains("rock") || 
 				lowerCase.contains("summerstomp") || lowerCase.contains("guerrilla-slam") || lowerCase.contains("fest")
 				|| lowerCase.contains("kiezfest")){
 			return CONCERT_TYPE_TAG;
 		}else if(lowerCase.contains("party") || lowerCase.contains("soliparty") || lowerCase.contains("ping-pong-bar")
 				|| lowerCase.contains("fiesta") || lowerCase.contains("soli-technoparty")){
 			return PARTY_TYPE_TAG;
 		}else if(lowerCase.contains("workshop") || lowerCase.contains("lesung") || lowerCase.contains("pressekonferenz") ||
 				lowerCase.contains("diskussionskreis") || lowerCase.contains("vortrag") || lowerCase.contains("infoveranstaltung") ||
 				lowerCase.contains("tresen") || lowerCase.contains("montagscaf") || lowerCase.contains("stricktreff") || 
 				lowerCase.contains("talk") || lowerCase.contains("lesebhne")){
 			return TALK_TYPE_TAG;
 		}else if(lowerCase.contains("kino") || lowerCase.contains("videokino") || lowerCase.contains("hofkino") ||
 				lowerCase.contains("solarpowersupercinema") || lowerCase.contains("film") || lowerCase.contains("kurzfilme")
 				|| lowerCase.contains("kino-abend")){
 			return SCREENING_TYPE_TAG;
 		}else if (lowerCase.contains("ausstellung")){
 			return EXHIBITION_TYPE_TAG;
 		}
 		return OTHER_TYPE_TAG;
 	}
 
 	/**
 	 * Extracts the address and the place where the event from Stress Faktor will take place
 	 * 
 	 * @param maybeLink the string containing the information to be extracted
 	 * @return a two positions string array with the street name and the place where it will take place
 	 */
 	private String[] extractPlace(String maybeLink) {	
 		String address = "";
 		String placeName = "";
 		if(maybeLink.contains("<a href=\"http:")){
 			String[] links = maybeLink.split("<a href=\"http:");
 			String[] nothingAddress = links[1].split("title=\"");
 			if(nothingAddress.length == 2){
 				String[] AddressAndDescription = nothingAddress[1].split("\"", 2);
 				String[] streetAndUbahn = AddressAndDescription[0].split(",", 2);
 				address = streetAndUbahn[0];
 				String[] placeAndDescription = AddressAndDescription[1].split("<",2);
 				placeName =  "<br>(Takes place at the " +placeAndDescription[0].replace(">","") +")";
 			}
 		}else{
 			String[] nothingAndPlace = maybeLink.split("<b>");
 			address = address + nothingAndPlace[1];
 		}
 		//address = "<a href=\"https://maps.google.es/maps?q=" +address.replace(' ', '+') +",+Berlin\">" +address +"</a>";
 		address = address +", Berlin";
 		return new String[]{address, placeName};
 	}
 	
 	/**
 	 * Extracts the links that are at the end of the description
 	 * 
 	 * @param description the text with the description and the links 
 	 * @return an array of strings with the links
 	 */
 	private String[] extractLinks(String description) {
 		List<String> theLinks = new ArrayList<String>();
 		if(description.contains("<a href=\"http:")){
 			String[] links = description.split("<a href=\"http:");
 			for (int z=1; z<links.length; z++){
 				// Check if it is a link
 				if (links[z].contains("title=\"Weitere Infos:")){
 					String[] linkAndNothing = links[z].split("\"",2);
 					// Set the links
 					theLinks.add("http:" +linkAndNothing[0]);
 				}
 			}
 		}
 		String[] linksArray = new String[theLinks.size()];
 		linksArray = theLinks.toArray(linksArray);
 		return linksArray;
 	}
 	
 	/**
 	 * Removes the little images that contain links
 	 * 
 	 * @param description the text with the description and the image-links  
 	 * @return a description without image-links 
 	 */
 	private String removeImageLinks(String description) {
 		return description.replaceAll("<img src=\"images/infoicon.gif\" width=\"15\" height=\"15\" border=\"0\" align=\"top\">", "");
 	}
 
 }

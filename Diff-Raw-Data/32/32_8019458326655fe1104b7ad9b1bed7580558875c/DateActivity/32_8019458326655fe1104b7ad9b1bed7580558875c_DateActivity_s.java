 package com.alvarosantisteban.berlincurator;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.LinkedHashMap;
 import java.util.Locale;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.OnChildClickListener;
 import android.widget.ExpandableListView.OnGroupClickListener;
 import android.widget.TextView;
 
 /**
  * Displays a list with the events for a concrete day organized by the origin of the website and the time.
  * 
  * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
  * 
  *  implements OnClickListener
  *
  */
 public class DateActivity extends Activity{
 
 	private TextView date;
 	ExpandableListView expandableSitesList;
 	public static final String EXTRA_EVENT = "com.alvarosantisteban.berlincurator.event";
 	Calendar calendar = Calendar.getInstance();
 	
 	/**
 	 * A LinkedHashMap with the a String as key and a HeaderInfo as value
 	 */
 	private LinkedHashMap<String, HeaderInfo> websites = new LinkedHashMap<String, HeaderInfo>();
 	/**
 	 * An ArrayList with the HeaderInfo of each website
 	 */
 	private ArrayList<HeaderInfo> websitesList = new ArrayList<HeaderInfo>();
 	 
 	private ListAdapter listAdapter;
 	
 	final Context context = this;
 		
 	/**
 	 *  Loads the elements from the resources, gets the data from the mainActitivy and calls the parsers to extract the information that
 	 *  will be shown.
 	 *  
 	 * @param savedInstanceState
 	 */
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_date);
 		
 		Intent intent = getIntent();
 		String[] htmls = intent.getStringArrayExtra(MainActivity.EXTRA_HTML);
 		
 		// Get the actual date
 		date = (TextView) findViewById(R.id.date);
 		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);
 		date.setText(dateFormat.format(calendar.getTime()));
 		
 		// Get the names of the sites
 		Resources res = this.getResources();
 		String[] sitesNames = res.getStringArray(R.array.sites_array); 
 		// Populate the ArrayList "websitesList" and the LinkedHashMap "websites"
 		createHeaderGroups(sitesNames);
 		
 		expandableSitesList = (ExpandableListView) findViewById(R.id.expandableSitesList);
 		// Create the adapter by passing the ArrayList data
 		listAdapter = new ListAdapter(DateActivity.this, websitesList);
 		// Attach the adapter to the expandableList
 		expandableSitesList.setAdapter(listAdapter);
 		
 		// TODO Quizas mejor ya abajo? Ver si se ahorra algo haciendolo aqui o vuelve a generar el array de Event
 		
 		loadEvents(htmls);
 		
 		
 		// Expand all Groups
 		//expandAll();
 		
 		// Collapse all groups
 		//collapseAll();
 		
 		// Expand the groups with events
 		expandGroupsWithEvents();
 		
 		// Listener for the events
 		expandableSitesList.setOnChildClickListener(myEventClicked);
 		// Listener for the sites
 		expandableSitesList.setOnGroupClickListener(myListGroupClicked);
 	}
 
 	/**
 	 * Expand all groups
 	 */
 	private void expandAll() {
 		int count = listAdapter.getGroupCount();
 		for (int i = 0; i < count; i++){
 			expandableSitesList.expandGroup(i);
 		}
 	}
 	
 	/**
 	 * Collapse all groups
 	 */
 	private void collapseAll() {
 		int count = listAdapter.getGroupCount();
 		for (int i = 0; i < count; i++){
 			expandableSitesList.collapseGroup(i);
 		}
 	}
 	
 	/**
 	 * Expand the groups with events on it
 	 */
 	private void expandGroupsWithEvents(){
 		int count = listAdapter.getGroupCount();
 		for (int i = 0; i < count; i++){
 			if(listAdapter.getChildrenCount(i) > 0){
 				expandableSitesList.expandGroup(i);
 			}
 		}
 	}
 	
 	/**
 	 * Loads the events from the different websites into out list
 	 */
 	private void loadEvents(String[] htmls){ 
 		// I Heart Berlin
 		Event[] iHeartBerlinEvents = extractEventFromIHeartBerlin(htmls[0]);
 		for (int i=0; i<iHeartBerlinEvents.length; i++){
 			// Add the events from the I Heart Berlin site of the selected day
 			if(iHeartBerlinEvents[i].getDay().equals(date.getText().toString())){
 				addEvent("I Heart Berlin", iHeartBerlinEvents[i]);
 			}
 		}
 		
 		// Berlin Art Parasites
 		Event[] parasitesEvents = extractEventFromArtParasites(htmls[1]);
 		for (int i=0; i<parasitesEvents.length; i++){
 			// Add the events from the Metal Concerts site of the selected day
 			if(parasitesEvents[i].getDay().equals(date.getText().toString())){
 			//if(metalEvents[i].getDay().equals("17/05/2013")){ Used to check if it works on a day that has a concert
 				addEvent("Berlin Art Parasites", parasitesEvents[i]);
 			}
 		}
 		
 		// Metal concerts
 		Event[] metalEvents = extractEventFromMetalConcerts(htmls[2]);
 		for (int i=0; i<metalEvents.length; i++){
 			// Add the events from the Metal Concerts site of the selected day
 			if(metalEvents[i].getDay().equals(date.getText().toString())){
 			//if(metalEvents[i].getDay().equals("17/05/2013")){ Used to check if it works on a day that has a concert
 				addEvent("Metal Concerts", metalEvents[i]);
 			}
 		}
 		
 		// White Trash's concerts
 		Event[] whiteTrashEvent = extractEventFromWhiteTrash(htmls[3]);
 		for (int i=0; i<whiteTrashEvent.length; i++){
 			// Add the events from the White Trash site of the selected day
 			if(whiteTrashEvent[i].getDay().equals(date.getText().toString())){
 				addEvent("White Trashs concerts", whiteTrashEvent[i]);
 			}
 		}
 
 		// Koepi's events
 		Event[] koepiEvents = extractEventFromKoepi(htmls[4]);
 		for (int i=0; i<koepiEvents.length; i++){
 			// Add the events from the Metal Concerts site of the selected day
 			if(koepiEvents[i].getDay().equals(date.getText().toString())){
 				addEvent("Koepis activities", koepiEvents[i]);
 			}
 		}
 	}
 	
 	/**
 	 * Add a event to its corresponding group (site where it comes from)
 	 * 
 	 * @param websiteName String with the name of the website from where the event comes from
 	 * @param newEvent The event to be attached
 	 * @return the position of group where the event was added
 	 */
 	private int addEvent(String websiteName, Event newEvent){
 		int groupPosition = 0;
 	   
 		// Check in the hash map if the group already exists
 		HeaderInfo headerInfo = websites.get(websiteName);
 		// Add the group if doesn't exists
 		if(headerInfo == null){
 			headerInfo = new HeaderInfo();
 			headerInfo.setName(websiteName);
 			websites.put(websiteName, headerInfo);
 			websitesList.add(headerInfo);
 		}
 	 
 		// Get the children (events) for the group
 		ArrayList<Event> eventsList = headerInfo.getEventsList();
 		// Get the size of the children list
 		int listSize = eventsList.size();
 		// Add to the counter
 		listSize++;
 	 
 		// Set the sequence for the event
 		newEvent.setSequence(String.valueOf(listSize));
 		// Add it to its list of events
 		eventsList.add(newEvent);
 		// Update the site with the "new" eventsList
 		headerInfo.setEventsList(eventsList);
 		 
 		//find the group position inside the list
 		groupPosition = websitesList.indexOf(headerInfo);
 		return groupPosition;
 	}
 	
 	/**
 	 * The child listener for the events
 	 */
 	private OnChildClickListener myEventClicked =  new OnChildClickListener() {
 		 
 		  public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
 	    
 			  // Get the group header 
 			  HeaderInfo headerInfo = websitesList.get(groupPosition);
 			  // Get the child info
 			  Event clickedEvent =  headerInfo.getEventsList().get(childPosition);
 			  // Display it or do something with it
 			  //Toast.makeText(getBaseContext(), "Clicked on Detail " + headerInfo.getName() + "/" + clickedEvent.getName(), Toast.LENGTH_LONG).show();
 			  Intent intent = new Intent(context, EventActivity.class);
 			  intent.putExtra(EXTRA_EVENT, clickedEvent);
 			  startActivity(intent);
 			  return false;
 		  }
 	};
 	
 	/**
 	 * The group listener for the sites
 	 */
 	private OnGroupClickListener myListGroupClicked =  new OnGroupClickListener() {
 		 
 		  public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
 	    
 			  // Get the group header
 			  HeaderInfo headerInfo = websitesList.get(groupPosition);
 			  // Display it or do something with it
 			  //Toast.makeText(getBaseContext(), "Child on Header " + headerInfo.getName(), Toast.LENGTH_LONG).show();
 	     
 			  return false;
 		  }
 	   
 	};
 	
 	/**
 	 * Populates the {@link websites} and the {@link websitesList} by creating {@link HeaderInfo} using the array of names of sites passed
 	 * in the parameter.
 	 * 
 	 * @param sitesNames the names of the sites which contain the events
 	 */
 	private void createHeaderGroups(String[] sitesNames) {
 		for (int i=0; i<sitesNames.length; i++){
 			HeaderInfo headerInfo = new HeaderInfo();
 			headerInfo.setName(sitesNames[i]);
 			websites.put(sitesNames[i], headerInfo);
 			websitesList.add(headerInfo);
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.date, menu);
 		return true;
 	}
 	
 	/**
 	 * Creates a set of events from the html of the I Heart Berlin website. 
 	 * Each Event has name, day, description and a link.
 	 * 
 	 * @param theHtml the String containing the html from the I Heart Berlin website
 	 * @return an array of Event with the name, day and links set
 	 */
 	private Event[] extractEventFromIHeartBerlin(String theHtml){  
 		String myPattern = "<div class=\"event_date\">";
 		String[] result = theHtml.split(myPattern);
 		
 		// Use an ArrayList because the number of events is unknown
 		ArrayList<Event> events = new ArrayList<Event>(); 
 		for (int i=1; i<result.length; i++){
 			// Separate up to the first "</div>"
 			String[] dateAndRest = result[i].split("</div>", 2);
 			// Get the date
 			String[] dayAndDate = dateAndRest[0].split(", ", 2);
 			String[] eventsOfADay = dateAndRest[1].split("<div class=\"event_entry clearfix\">");
 			for (int j=1; j<eventsOfADay.length; j++){
 				Event event = new Event();
 				// Format the date and set it
 				event.setDay(formatDate(dayAndDate[1].replace(",", "").trim()));
 				String[] imageAndRest = eventsOfADay[j].split("<div class=\"event_image\">");
 				String[] imageAndRest2 = imageAndRest[1].split("<div class=\"event_info\">");
 				// Set the image with all its html code 				
 				event.setImage(imageAndRest2[0].replaceFirst("</div>", ""));
 				String[] other = imageAndRest2[1].split("<div class=\"event_time\">");
 				String[] timeAndRest = other[1].split("</div>",2);
 				// Set the time when the event begins
 				event.setHour(timeAndRest[0].replace("h", "").trim()); // Remove the "h" from 22:00h
 				String[] nameAndRest = timeAndRest[1].split("<h3>");
 				String[] nameAndRest2 = nameAndRest[1].split("</h3");
 				// Set the name of the event
 				event.setName(nameAndRest2[0].trim());
 				String[] descriptionAndLinks = nameAndRest2[1].split("</p>");
 				// Set the description
 				String description = descriptionAndLinks[0].replaceFirst("<p>", "").replaceFirst(">", "").trim();
 				event.setDescription(description);
 				// Set the links
 				String[] trashAndLinks = descriptionAndLinks[1].split("<div class=\"event_links\">");
 				// Check if there are links
 				String[] htmlLink = trashAndLinks[1].split("<a href=\"");
 				if(htmlLink.length > 0){
 					String links = "";
 					// Search for the links
 					for (int z=0; z<htmlLink.length; z++){
 						String[] pureLink = htmlLink[z].split("\"",2); // Get link
 						links = pureLink[0].trim() +"\n" +links;
 					}
 					event.setLink(links);
 				}
 				events.add(event);
 			}
 		}
 		Event[] eventsArray = new Event[events.size()];
 		eventsArray = events.toArray(eventsArray);
 		return eventsArray;
     }
 	
 	/**
 	 * Creates a set of events from the html of the Berlin Art Parasites website. 
 	 * Each Event has name, day, description and a link.
 	 * 
 	 * @param theHtml the String containing the html from the Berlin Art Parasites website
 	 * @return an array of Event with the name, day, hour, description and links set
 	 */
 	private Event[] extractEventFromArtParasites(String theHtml){  
 		// First, get rid of everthing that goes after <em>
 		String[] clearnerHtml = theHtml.split("<em>",2);
 		String myPattern = "<strong>"; // Marks the number of days
 		String[] result = clearnerHtml[0].split(myPattern);
 		
 		// Use an ArrayList because the number of events per day is unknown
 		ArrayList<Event> events = new ArrayList<Event>(); 
 		for (int i=1; i<result.length; i++){
 			System.out.println("-------------i="+i);
 			// Separate up to the first "</strong>"
 			String[] dateAndRest = result[i].split("</strong>", 2);
 			// Get the date
 			String[] dayAndDate = dateAndRest[0].split(" ", 2);
 			String[] eventsOfADay = dateAndRest[1].split("<a href=\""); // Marks the number of events
 			for (int j=1; j<=(eventsOfADay.length-1)/2; j++){
 				System.out.println("-------------J="+j);
 				Event event = new Event();
 				// Format the date and set it
 				String day = formatDate(dayAndDate[1].replace(",", "").trim());
 				System.out.println("day:"+day);
 				event.setDay(day);
 				
 				//System.out.println("eventsOfADay[(j*2)-1]"+eventsOfADay[(j*2)-1]);
 				String[] linkAndPlace = eventsOfADay[(j*2)-1].split("</a>",2);
 				// We will use the "place" for the description ---> place[1]
 				String[] place = linkAndPlace[0].split("\">"); 
 				String placeLink = place[0];
 				System.out.println("placeLink"+placeLink);
				System.out.println("place:"+place[1]); // AQUI HAY MUGRE
				event.setLink("http://www.berlin-artparasites.com"+placeLink);
 				
 				// Extract the name and a link and set them
				//System.out.println("eventsOfADay[(j*2)]"+eventsOfADay[(j*2)]);
 				String[] linkNameAndRest = eventsOfADay[(j*2)].split("</a>",2);
 				String[] linkAndName = linkNameAndRest[0].split("\">"); 
 				System.out.println("linkAndName[1]"+linkAndName[1]);
 				event.setName(linkAndName[1]);
 				System.out.println("linkAndName[0]"+linkAndName[0]);
 				if(linkAndName[0].charAt(0) == '/'){
 					event.setLink("http://www.berlin-artparasites.com"+linkAndName[0]);
 				}else{
 					String[] linkAndCrap = linkAndName[0].split("\"",2);
 					event.setLink(linkAndCrap[0]);
 				}
 				
 				// Extract the time and set it
 				System.out.println("linkNameAndRest[1]"+linkNameAndRest[1]);
 				String[] timeAndRest = linkNameAndRest[1].split("pm</p>"); 
 				String[] crapTime = timeAndRest[0].split("-",2);
 				System.out.println("crapTime[1]:"+crapTime[1].trim() +".");
 				event.setHour(crapTime[1].trim());
 				
 				String[] nothingAndDescription = timeAndRest[1].split(">", 2);
 				String[] descriptionAndNothing = nothingAndDescription[1].split("</p>",2);
 				
 				// Create the description and set it
 				String description = linkAndName[1] + " at the " +place[1] +":" +System.getProperty("line.separator") + descriptionAndNothing[0].trim(); // System.getProperty("line.separator")
 				System.out.println("description: "+description);
 				event.setDescription(description);
 				
 				events.add(event);
 			}
 		}
 		Event[] eventsArray = new Event[events.size()];
 		eventsArray = events.toArray(eventsArray);
 		return eventsArray;
     }
 
 	/**
 	 * Creates a set of events from the html of the Metal Concerts website. 
 	 * Each Event has name, day, description and a link.
 	 * 
 	 * @param theHtml the String containing the html from the Metal Concerts website
 	 * @return an array of Event with the name, day and links set
 	 */
 	private Event[] extractEventFromMetalConcerts(String theHtml){  
 		String myPattern = "<p class=\"konzerte\">"; //<p class=\"konzerte\">.*?</p>
 		String[] result = theHtml.split(myPattern);
 		
 		// Throw away the first entry of the array because it does not contain a concert
 		Event[] events = new Event[result.length-1]; 
 		for (int i=1; i<result.length; i++){
 			// Separate up to the "@"
 			String[] twoParts = result[i].split("@");
 			// Separate the date and the name of the band
 			String[] dateAndName = twoParts[0].split("\\. ");
 			Event event = new Event();
 			event.setName(dateAndName[1]);
 			String eventDate = dateAndName[0].replace('.', '/');
 			DateFormat dateFormat = new SimpleDateFormat("yyyy", Locale.GERMAN);
 			eventDate = eventDate.concat("/" +dateFormat.format(calendar.getTime()));
 			event.setDay(eventDate);
 			
 			// Remove useless code
 			String htmlLink = twoParts[1].replaceFirst("</p>", "");
 			// Check if there is a link
 			if(htmlLink.charAt(0) == '<'){
 				// Remove useless code
 				htmlLink = htmlLink.replaceFirst("</a>", "");
 				String[] pureLink = htmlLink.split("\""); // Get link
 				String[] concertPlace = htmlLink.split(">"); // Get name
 				event.setLink(pureLink[1]);
 				event.setDescription("The concert will take place at the " +concertPlace[1]);
 			}else{
 				String concertPlace = htmlLink;
 				event.setDescription("The concert will take place at the " +concertPlace);
 			}
 			events[i-1] = event;
 		}
 		return events;
     }
 	
 	/**
 	 * Creates a set of events from the html of the White Trash website. 
 	 * Each Event has name, day, description and a link.
 	 * 
 	 * @param theHtml the String containing the html from the White Trash website
 	 * @return an array of Event with the name, day, hour and link set
 	 */
 	private Event[] extractEventFromWhiteTrash(String theHtml){  
 		String myPattern = "<h4>";
 		String[] result = theHtml.split(myPattern);
 		
 		// Use an ArrayList because the number of events is unknown (is likely that they are 7)
 		ArrayList<Event> events = new ArrayList<Event>(); 
 		for (int i=1; i<result.length; i++){
 			// Separate up to the first "</a>"
 			String[] dateAndRest = result[i].split("</a", 2);
 			// Get the date
 			String[] dayAndDate = dateAndRest[0].split("\">", 2);
 			String[] eventsOfADay = dateAndRest[1].split("<p class=\"time\">");
 			for (int j=1; j<eventsOfADay.length; j++){
 				Event event = new Event();
 				// Format the date and set it
 				event.setDay(formatDate(dayAndDate[1]));
 				String[] timeAndRest = eventsOfADay[j].split("</p>",2);
 				// Set the time
 				//System.out.println("timeAndRest[0]" +timeAndRest[0]);
 				event.setHour(timeAndRest[0].replace("h", "").trim());
 				String[] linkNameAndRest = timeAndRest[1].split("<a href=\"");
 				String[] linkNameAndRest2 = linkNameAndRest[1].split("\">",2);
 				// Make the relative link absolut and set it
 				event.setLink("http://www.whitetrashfastfood.com"+linkNameAndRest2[0]); 
 				String description = "";
 				String[]nameAndRest = linkNameAndRest2[1].split("\\(",2);
 				// Set the name depending if the band name contains "(" or not
 				if (nameAndRest[0].equals(linkNameAndRest2[1])){
 					String[] nameAndBlaBla = linkNameAndRest2[1].split("</a>",2);
 					event.setName(nameAndBlaBla[0]);
 				}else{
 					event.setName(nameAndRest[0]);
 				}
 				String[] description1 = linkNameAndRest2[1].split("</a>");
 				// Set the constructed description
 				description = description1[0];
 				String[] description3 = description1[1].split("<br>"); // The place is in description3[0]
 				String[] description2a = description1[1].split("<span class=\"summ\">");
 				String[] description2b = description2a[1].split("</span>");
 				event.setDescription(description + description2b[0] + description3[0]);
 				events.add(event);
 			}
 		}
 		Event[] eventsArray = new Event[events.size()];
 		eventsArray = events.toArray(eventsArray);
 		return eventsArray;
     }
 	
 	
 	/**
 	 * Creates a set of events from the html of the Koepi website. 
 	 * Each Event has name, day, time, a description and sometimes a link.
 	 * 
 	 * @param theHtml the String containing the html from the Metal Concerts website
 	 * @return an array of Event with the name, day and links set
 	 */
 	private Event[] extractEventFromKoepi(String theHtml) {
 		String[] uselessAndGood = theHtml.split("</div -->");
 		String myPattern = "<span class=\"datum\">"; //<p class=\"konzerte\">.*?</p>
 		String[] result = uselessAndGood[1].split(myPattern);
 		
 		// Throw away the first entry of the array because it does not contain an event
 		Event[] events = new Event[result.length-1]; 
 		for (int i=1; i<result.length; i++){
 			Event event = new Event();
 			// Remove the left part of ", "
 			String[] twoParts = result[i].split(", ", 2); // We want just the first
 			// Remove useless code
 			String[] dateAndRest = twoParts[1].split("</span><br />");
 			// Format the date and set it to the event
 			event.setDay(dateAndRest[0].replace('.', '/'));
 			// Separate the first paragraph
 			String[] paragraphs = dateAndRest[1].split("<br />", 2);
 			
 			// Get the time and name
 			String[] hourAndName = paragraphs[0].split(" Uhr: ", 2);
 			// Set the time to the event
 			event.setHour(hourAndName[0].trim());
 			// Set the name to the event
 			event.setName(hourAndName[1]);
 			// Set the description (with lots of html code)
 			event.setDescription(paragraphs[1]);
 			
 			// Check if there is a link
 			String[] htmlLink = twoParts[1].split("<a href=\"", 2);
 			if(htmlLink.length == 2){
 				// Remove useless code
 				String[] pureLink = htmlLink[1].split("\"",2); // Get link
 				event.setLink(pureLink[0]);
 			}			
 			events[i-1] = event;
 		}
 		return events;
 	}
 	
 	/**
 	 * Normalizes a date from the I Heart Berlin and Berlin Art Parasites format: "May 13 2013"  
 	 * or the White Trash format: "22 May 2013" to the app's format "13/05/2013"
 	 * 
 	 * @param inputDate the date in the I Heart Berlin, Berlin Art Parasites or White Trash format
 	 * @return a String with the date normalized
 	 */
 	public static String formatDate(String inputDate){
 		String monthNumber;
 		String monthLetter = "";
 		String day = "";
 		String[] monthDayYear = inputDate.split(" ");
 		for (int i=0;i<2;i++){ // Just the first two
 			// If there is a letter, we have the month
 			if (Character.isLetter(monthDayYear[i].charAt(0))){
 				monthLetter = monthDayYear[i];
 			}else{ // If not, we have the day
 				day = monthDayYear[i];
 			}
 		}
 		if (monthLetter.equals("January"))
 			monthNumber = "01";
 		else if (monthLetter.equals("February"))
             monthNumber = "02";
 		else if (monthLetter.equals("March"))
             monthNumber = "03";
         else if (monthLetter.equals("April"))
             monthNumber = "04";
         else if (monthLetter.equals("May"))
             monthNumber = "05";
         else if (monthLetter.equals("June"))
             monthNumber = "06";
         else if (monthLetter.equals("July"))
             monthNumber = "07";
         else if (monthLetter.equals("August"))
             monthNumber = "08";
         else if (monthLetter.equals("September"))
             monthNumber = "09";
         else if (monthLetter.equals("October"))
             monthNumber = "10";
         else if (monthLetter.equals("November"))
             monthNumber = "11";
         else if (monthLetter.equals("December"))
             monthNumber = "12";
         else
             monthNumber = "00";
 		String total = day+"/"+monthNumber+"/"+monthDayYear[2];
 		return total.trim(); 
 	}
 	
 	/*
 	public void onClick(View v) {
 	 
 		switch (v.getId()) {
 		//add entry to the List
 			case R.id.add:
 	 
 				Spinner spinner = (Spinner) findViewById(R.id.department);
 				String department = spinner.getSelectedItem().toString();
 				EditText editText = (EditText) findViewById(R.id.product);
 				String product = editText.getText().toString();
 				editText.setText("");
 				    
 				//add a new item to the list
 				int groupPosition = addProduct(department,product);
 				//notify the list so that changes can take effect
 				listAdapter.notifyDataSetChanged();
 				       
 				//collapse all groups
 				collapseAll();
 				//expand the group where item was just added
 				myList.expandGroup(groupPosition);
 				//set the current group to be selected so that it becomes visible
 				myList.setSelectedGroup(groupPosition);
 				    
 				break;
 	 
 	   // More buttons go here (if any) ...
 		}
 	}
 */
 	
 	/*
 	 * 
 	 * USING PATTERNS 
 	 * 
     Pattern p = Pattern.compile(myPattern,Pattern.DOTALL);
     Matcher m = p.matcher(example);
     String[] result = null;
 
     if ( m.matches() ){
         Log.d("Matcher", "PATTERN MATCHES!");
     	System.out.println("PATTERN MATCHES!");
     	result = p.split(example);
     	System.out.println(result.length);
     	System.out.println(result.toString());
     	System.out.println("Start:" +m.start());
     	System.out.println("End:" +m.end());
     } else{
     	System.out.println("PATTERN DOES NOT MATCH");
         Log.d("MATCHER", "PATTERN DOES NOT MATCH!");
     }*/
 	
 }

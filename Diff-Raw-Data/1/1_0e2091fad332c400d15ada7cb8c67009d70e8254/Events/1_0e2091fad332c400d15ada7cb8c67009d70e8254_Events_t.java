 package lockett_streiff.swarthmobile2;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.htmlparser.Parser;
 import org.htmlparser.Tag;
 import org.htmlparser.lexer.Lexer;
 import org.htmlparser.util.ParserException;
 import org.htmlparser.visitors.NodeVisitor;
 
 import android.app.Activity;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.androidquery.AQuery;
 import com.androidquery.callback.AjaxCallback;
 import com.androidquery.callback.AjaxStatus;
 
 /* Goal 1: Add events to native Android calendar 
  * -----------------------------------
  * Scrap adding personal events
  * Users can't modify campus events
  * Scrape HTML more thoroughly - Done
  * Better campus event display - Pending
  */
 
 public class Events extends Activity {
 
 	private final String tag = "Events";
 	
 	/* Handle events ListView */
 	private ListView eventsPane;
 	private List<Event> eventsList;
 
 	/* Event constants */
 	private final int NAME = 0;
 	private final int DATE = 1;
 	private final int TIME = 2;
 	private final int ALL_DAY = 3;
 	private final int LOCATION = 4;
 	private final int PAGE = 5;
 	private final int DESCRIPTION = 6;
 	private final int CONTACT = 7;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.events);
 
 		/* Handle ListView */
 		eventsPane = (ListView) this.findViewById(R.id.event_viewing_pane_lv);
 		eventsList = new ArrayList<Event>();
 		
 		/* Add a sample event as a test */
 		eventsList.add(new Event(
 				"Orchestra Concert",
 				"7:00pm - 10:00pm",
 				"Lang Concert Hall",
 				"Andrew Hauze\n(610) 555-3940",
 				"David Kim of the Philadelphia orchestra!"
 				));
 		
 		
 		/* Setup */
 		getHTML();
 		
 		
 		
 		
 
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		/* Inflate the menu; this adds items to the action bar if it is present. */
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	/* onClick event listener for buttons */
 	public void showEvents(View v) {
 		Log.i(tag, "showEvents");
 	}
 	
 	/*
 	 * Checks if a network connection is present
 	 * NOTE: Uses ACCESS_NETWORK_STATE permission
 	 */
 	private boolean isNetworkOnline() {
 		boolean status = false;
 		try {
 			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 			NetworkInfo netInfo = cm.getNetworkInfo(0);
 			if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
 				status = true;
 			} else {
 				netInfo = cm.getNetworkInfo(1);
 				if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
 					status = true;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 		if (!status) {
 			Toast.makeText(Events.this, "Could not find network connection", Toast.LENGTH_SHORT).show();
 		}
 		return status;
 	}
 
 
 	/////////// Event retrieval backend  ///////////
 
 	/*
 	 * Retrieves HTML from calendar.swarthmore.edu
 	 */
 	private String getHTML() {
 		//Log.i("Events - getHTML", "HTML parsing - 1st round");
 
 		/* Only runs if network is online */
 		if (!isNetworkOnline()) {return "";}
 		final AQuery aq = new AQuery(Events.this);
 		//final AQuery aq2 = new AQuery(Events.this);
 		//final TextView tv = (TextView) this.findViewById(R.id.tv);
 		String url = "http://calendar.swarthmore.edu/calendar";
 		aq.ajax(url, String.class, new AjaxCallback<String>() {
 
 			@Override
 			public void callback(String url, String html, AjaxStatus status) {
 				/* First round HTML parsing - All events */
 
 				/* Debugging */
 				/*Log.i(tag, "URL: "+url);
 				Log.i(tag, "HTML: "+html);
 				tv.setMovementMethod(new ScrollingMovementMethod());
 				tv.setText(html);*/
 
 				//Log.i(tag, "MSG: "+status.getMessage());
 
 				/* Second round HTML parsing - href tags */
 				// Do I really need to store a value here? There's only gonna be one
 				// PageInfo Object...
 				String[] html2 = getNestedHTML(html);
 				//Log.i(tag, "Nested HTML obtained");
 			}
 		});
 		return "";
 	}
 
 	/*
 	 * getHTML helper function
 	 * 
 	 * Navigates the href attributes in each event accessed in getHTML
 	 * to get complete event information
 	 */
 	private String[] getNestedHTML(String html) {
 		class PageInfo {
 
 			/* Event fields */
 			public String[] eventArr;
 
 			/* Initialize to null */
 			PageInfo() {eventArr = new String[8];}
 		}
 
 		/* PageInfo enables us to circumvent scoping issues when scraping HTML */
 		final PageInfo event = new PageInfo();
 
 		/* Start parsing HTML */
 		NodeVisitor visitor = new NodeVisitor() {
 			AQuery aq = new AQuery(Events.this);
 			public void visitTag(Tag tag) {
 				String name = tag.getTagName();
 				String url = "http://calendar.swarthmore.edu/calendar/";
 
 				/* Get event date */
 				if ("TD".equals(name)) {
 					if (tag.getAttribute("class") != null && tag.getAttribute("class").equals("listheadtext")) {
 						event.eventArr[DATE] = tag.toPlainTextString();
 						//Log.i("Events", "Date: "+evDate);
 						
 						/* Set text of tabs to date */
 					}
 				}
 
 				/* Get event name */
 				if ("A".equals(name)) {
 					if (tag.getAttribute("class") != null && tag.getAttribute("class").equals("listtext")) {
 						event.eventArr[NAME] = tag.toPlainTextString();
 						//Log.i("Events", "Name: "+evName);
 					}
 				}
 
 				/* Get event time, page, location, description, and contact information */
 				if ("A".equals(name)) {
 					if (tag.getAttribute("class") != null && tag.getAttribute("class").equals("url")) {
 						event.eventArr[TIME] = tag.toPlainTextString();
 
 						/* Log.i("Events", "Time: "+event.time);
 						Log.i("Events", "------------------------------------------------"); */
 
 						/* Navigate to link in href attribute and parse HTML for event description and location */
 						String eventPage = url + tag.getAttribute("href");
 						//Log.i("Events", "Navigating to url: "+eventPage);
 
 						/* Get event page */
 						event.eventArr[PAGE] = eventPage;
 
 						/* Not getting event description - parsing all the <p> tags would
 						be a computationally-expensive hassle. Just make a "more info" button */
 
 						/* AQuery AJAX call to navigate into href link */
 						aq.ajax(eventPage, String.class, new AjaxCallback<String>() {
 
 							@Override
 							public void callback(String url, String html, AjaxStatus status) {
 
 								/* Debugging */
 								/*TextView tv = (TextView) findViewById(R.id.tv);
 								tv.setText(html);
 								tv.setMovementMethod(new ScrollingMovementMethod());
 								Log.i("visitTag", "Internal HTML status: "+status.getMessage());*/								
 
 								/* Get event location */
 								NodeVisitor visitor = new NodeVisitor() {
 									public void visitTag(Tag tag) {
 										String name = tag.getTagName();
 										//Log.i("Events", "Tag:"+name);
 
 										/* Get location (note: can add a "More info" button to go to event page in lieu of description) */				
 										if ("A".equals(name)) {
 											if (tag.getAttribute("class") != null && tag.getAttribute("class").equals("calendartext")) {
 												if (tag.toPlainTextString().contains("Swarthmore College")) {
 													event.eventArr[LOCATION] = tag.toPlainTextString().replace("*Swarthmore College - ", "");
 													//Log.i("Events", "Location: "+event.location);
 
 
 													/* How do I navigate scoping in order to retrieve the location?
 													Answer: PageInfo, it's actually useful for something after all! */												
 												}						
 											}
 										}
 
 										/* Get event description*/
 										if (("META").equals(name)) {
 											if (tag.getAttribute("name") != null && tag.getAttribute("name").equals("description")) {
 												String description;
 												if ((description = tag.getAttribute("content")) != null) {
 													//Log.i("Events", "----------------------------------------------");
 													//Log.i("Events", description);
 													event.eventArr[DESCRIPTION] = description;
 												}
 											}	
 										}
 
 										/* Get event contact information */
 										if (("TD").equals(name)) {
 											if (tag.getAttribute("class") != null && tag.getAttribute("class").equals("detailsview") ) {
 												String contactInfo = tag.toPlainTextString();
 												if (contactInfo.contains("Contact Information")) {
 													//Log.i("Events","----------------------------------------------");
 													/* Idea: split on email, phone, and name, in that order. */
 													//Log.i("Events",contactInfo);
 													event.eventArr[CONTACT] = parseContactInfo(contactInfo.replace("Contact Information:", ""));
 												}												
 											}
 										}
 									}
 								};
 								/* Execute inner HTML parsing */
 								Parser parser = new Parser(new Lexer(html));
 								try {parser.visitAllNodesWith(visitor);} 
 								catch (ParserException e) {e.printStackTrace();}
 							}
 						});		
 					}
 				}
 			}
 		};
 
 		/* Execute outer HTML parsing */
 		Parser parser = new Parser(new Lexer(html));
 		try {parser.visitAllNodesWith(visitor);}
 		catch (ParserException e) {e.printStackTrace();}
 
 		/* Return the String[] in event (the PageInfo class instance). Aren't I clever? */
 		//Log.i("Events", "Event: "+Arrays.toString(event.eventArr));
 		return event.eventArr;
 	}
 
 	/*
 	 * getNestedHTML helper function
 	 * 
 	 * Contact information is passed in as a messy String that contains
 	 * up to three pieces of useful information: name, phone, and email.
 	 * 
 	 * Splitting in reverse order should produce a cleaner String
 	 * delimited by newlines. 
 	 * 
 	 */
 	public String parseContactInfo(String ci) {
 		String myCi = "";
 		ci = ci.replace(": ", "");
 		String[] ciSplit = ci.split("Email|Name|Phone");
 		//Log.i("Events - parseContactInfo",Arrays.toString(ciSplit));
 
 		for (String str: ciSplit) {
 			/* Emails have the @ symbol */
 			if (str.contains("@")) { myCi += (str.trim()+"\n"); }
 
 			/* Phone numbers have the (610) extension */
 			else if (str.contains("(610)")) { myCi += (str.trim()+"\n"); }
 
 			/* Otherwise, it's a name */
 			else { myCi += (str.trim()+"\n"); }
 		}
 
 		myCi = myCi.substring(1, myCi.length()-1);
 		//Log.i("Events - parseContactInfo",myCi);
 		//Log.i("Events - parseContactInfo","-----------------------------------------------");
 
 		return myCi;
 	}
 }

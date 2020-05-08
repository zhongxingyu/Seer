 package cal_master;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.JOptionPane;
 
 import org.joda.time.DateTime;
 
 import cal_master.Index.IndexType;
 import calendar.*;
 import calendar_exporters.*;
 import calendar_exporters.When2MeetExporter.NameAlreadyExistsException;
 import calendar_importers.*;
 
 import com.thoughtworks.xstream.XStream;
 
 import ftf.TimeFinderSlots;
 
 
 public class Communicator {
 
 	private CalendarsImporter<CalendarResponses> _userCalImporter = null;
 	private CalendarGroup<CalendarResponses> _userCal = null;
 	private String _userCalID = "userCal", _indexID = "index";
 	
 	private HashMap<String, When2MeetEvent> _w2mEvents = new HashMap<String, When2MeetEvent>();
 	private When2MeetImporter _importer = new When2MeetImporter();
 	private When2MeetExporter _exporter = new When2MeetExporter();
 
 	// TODO edit 
 	//private HashMap<String, When2MeetImporter> _w2mImporters = new HashMap<String, When2MeetImporter>();
 	private Converter _converter = new Converter();
 	private TimeFinderSlots _timeFinder = new TimeFinderSlots();
 	private ProgramOwner _owner = new ProgramOwner();
 	
 	private XStream _xstream = new XStream();
 	private Index _index = new Index();
 	
 	private static final double ATTENDEE_PERCENTAGE = .75;
 	private static final int NUM_SUGGESTIONS = 5;
 	
 	
 	private void setUpXStream() {
 		_xstream.alias("index", Index.class);
 		_xstream.alias("indextype", Index.IndexType.class);
 		_xstream.alias("calendarslots", CalendarSlots.class);
 		_xstream.alias("calendarresponses", CalendarResponses.class);
 		_xstream.alias("calendargroup", CalendarGroup.class);
 		_xstream.alias("calendar", Calendar.class);
 		_xstream.alias("when2meetevent", When2MeetEvent.class);
 		_xstream.alias("when2meetowner", When2MeetOwner.class);
 		_xstream.alias("avail", Availability.class);
 	}
 	
 	public void startUp() {
 		
 		setUpXStream();
 		// If have an index, recreate index
 		File indexFile = new File(_indexID+".xml");
 		if(indexFile.exists())
 			_index = (Index) _xstream.fromXML(indexFile);
 		
 		// Pull in XML and create when2meet events from XML
 		// Pull in XML for user cal, and create user cal
 		for(String id : _index.getFiles()) {
 			IndexType type = _index.getType(id);
 			Object o = _xstream.fromXML(new File(id + ".xml"));
 			switch(type) {
 			case When2MeetEvent: {
 				_w2mEvents.put(id,  (When2MeetEvent) o);
 				break;
 			}
 			case GCal: {
 				if(_userCal != null)
 					System.out.println("Two google calendars were recreated from index");
 				_userCal = (GoogleCalendars) o;
 				break;
 			}
 			case ProgramOwner: {
 				_owner = (ProgramOwner) o;
 			}
 			default: {
 				System.out.println("Default condition triggered on recreating index.");
 			}
 			}
 				
 		}
 		
 		if(_userCal == null) {
 			// TODO add a never option
 			Object[] options = {"Yes", "Not now"};
 			int n = JOptionPane.showOptionDialog(null, "Would you like to import your calendar?",
 					"", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
 					options, options[0]);
 			if(n == 0) {
 				// TODO import the users calendar
 				JOptionPane.showMessageDialog(null, "Now I should import a calendar");
 				
 				// from where would you like to import?
 				Object[] calOptions = {"Google Calendar" };
 				Object selectedValue = JOptionPane.showInputDialog(null, "Choose a calendar type to import.", "", 
 						JOptionPane.INFORMATION_MESSAGE, null, calOptions, calOptions[0]);
 				
 				// switch on user response
 				if(selectedValue == "Google Calendar"){
 					this.setCalImporter(new GCalImporter());
 					this.pullCal(DateTime.now(), DateTime.now().plusDays(30));
 				}
 			}
 		}
 		
 		// TODO
 		// Refresh when2meet events
 		// Refresh calendars -> if no calendar, pull in users calendar	
 		refresh();
 			
 	}
 	
 	/** SAVING **/
 	
 	public void updateIndex() {
 		// Rebuilds the index given the current list of events and calendar
 		for(String id : _w2mEvents.keySet())
 			_index.addItem(id, IndexType.When2MeetEvent);
 		_index.addItem(_userCalID, IndexType.GCal);
 
 		writeToFile(_indexID, _index);
 	}
 	
 	private void writeToFile(String filename, Object o) {
 		Writer out = null;
 		try {
 			out = new BufferedWriter(new FileWriter(filename+".xml"));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		_xstream.toXML(o, out);	
 	}
 	
 	// Saves one item
 	public void saveOneItem(Object o, String id) {
 		// Check index to see if it exists
 		updateIndex();
 		writeToFile(id, o);
 	}
 	
 	public void saveAll() {
 		// Store some form of an update index
 		updateIndex();
 		
 		// Store XML for when2meet events
 		for(String id : _w2mEvents.keySet())
 			writeToFile(id, _w2mEvents.get(id));
 		
 		// Store XML for calendar
 		System.out.println("CALLING WRITE USER CAL");
 		writeToFile(_userCalID, _userCal);
 		System.out.println("WROTE USER CAL");
 	}
 	
 	public class URLAlreadyExistsException extends Exception {
 		
 	}
 	
 	public When2MeetEvent addWhen2Meet(String url) throws URLAlreadyExistsException, IOException {
 		// Check if we have this url already
 		// If we do, throw an error
 		for(When2MeetEvent w2me : _w2mEvents.values())
 			if(w2me.getURL().equals(url)) {
 				System.out.println("URL already found");
 				throw new URLAlreadyExistsException();
 			}
 
 		// If we don't, check that it's a valid url
 		// If it's a valid url, pull in that when2meet using an importer
 		// If it's not a valid url, error message to user
 		
 		When2MeetEvent newEvent = _importer.importCalendarGroup(url);
 		String id = newEvent.getID() + "";
 		_w2mEvents.put(id, newEvent);
 		saveOneItem(newEvent, id);
 		
 		return newEvent;
 
 		
 	}
 	
 	public void setUserCal(CalendarGroup<CalendarResponses> userCal){
 		_userCal = userCal;
 	}
 	
 	public void removeWhen2Meet(String eventID) {
 		// Check that we do have an event with this ID
 		When2MeetEvent toRemove = _w2mEvents.get(eventID);
 		
 		// If we do, remove it form our list of events
 		// Save this when2meet, and our new index
 		if(toRemove != null){
 			String id = eventID + "";
 			_w2mEvents.remove(id);
 			saveOneItem(toRemove, id);
 		}
 		
 		// If we didn't have it, huh? confused, how did this happen
 	}
 	
 	public void setCalImporter(CalendarsImporter<CalendarResponses> importer){
 		_userCalImporter = importer;
 	}
 	
 	public void setOwnerName(String name){
 		_owner.setName(name);
 	}
 	
 	public void refresh() {
 		//TODO: deal with URL exception
 		// Update all when2meet events
 		When2MeetEvent temp = null;
 		for(When2MeetEvent w2m : _w2mEvents.values()){
 			//repull info
 			_importer.refreshEvent(w2m);
 		}
 		// Update user calendar
 		if(_userCal != null){
 			pullCal(_userCal.getStartTime(), _userCal.getEndTime());
 		}
 		
 		// Rebuild index and store all files
 		saveAll();
 	}
 	
 	public void calToW2M(String eventID){
 		When2MeetEvent w2m = _w2mEvents.get(eventID);
 		if(w2m != null && !w2m.userHasSubmitted()){
 			
 			//check to see that w2m in range of userCal
 			//If it isn't, pullCall before 
 			DateTime wStart = w2m.getStartTime();
 			DateTime wEnd = w2m.getEndTime();
 			DateTime cStart = _userCal.getStartTime();
 			DateTime cEnd = _userCal.getEndTime();
 			
 			if(wStart.isBefore(cStart) || wEnd.isAfter(cEnd)){
 				DateTime start, end;
 				
 				if(wStart.isBefore(cStart))
 					start = wStart;
 				else
 					start = cStart;
 				
 				if(wEnd.isAfter(cEnd))
 					end = wEnd;
 				else
 					end = cEnd;
 				
 				pullCal(start, end);
 			}
 			
 			CalendarSlots cal = _converter.calToSlots(_userCal, w2m);
 			w2m.setUserResponse(cal);
 		}
 	}
 	
 	public When2MeetEvent getW2MByID(String id){
 		return _w2mEvents.get(id);
 	}
 	
 	public When2MeetEvent getW2M(String id){
 		When2MeetEvent toReturn = _w2mEvents.get(id);
 		if(toReturn.getUserResponse() == null){
 			
 			//ask user if they've responded to the event
 			int resp = JOptionPane.showConfirmDialog(null, "Have you already responded to this When2Meet?", "", JOptionPane.YES_NO_OPTION);
 			
 			//if they have, ask them to select their response from the list of all responses
 			if(resp == JOptionPane.YES_OPTION){
 				
 				Object[] responseNames = new Object[toReturn.getCalendars().size()];
 				
 				for(int i=0; i< toReturn.getCalendars().size(); i++){
 					responseNames[i] = toReturn.getCalendars().get(i).getOwner().getName();
 				}
 				
				int selected = JOptionPane.showOptionDialog(null, "Please select the name that represents your response from the list below",
						"", responseNames.length, JOptionPane.INFORMATION_MESSAGE, null,
 						responseNames, responseNames[0]);
 				
 				//take the selected cal, remove it from the list, and set it to be the userResponse
				CalendarSlots user = toReturn.getCalByName(responseNames[selected].toString());
				//toReturn.removeCalendar(user);
				toReturn.setUserResponse(user);
				
 			}
 			//if they haven't, set userResponse to be a new CalendarSlots with them as the owner
 			else{
 				toReturn.setUserResponse(new CalendarSlots(toReturn.getStartTime(), toReturn.getEndTime(), 15, Availability.free));
 				toReturn.getUserResponse().setOwner(new When2MeetOwner(_owner.getName(), -1));
 			}
 			
 		}
 		if(!toReturn.userHasSubmitted()){
 			//calToW2M(id);
 		}
 		
 		return toReturn;
 	}
 	
 	private void getNewUserName(When2MeetEvent event, String currName){
 		//TODO : set newName to something useful (i.e. actually ask for user response)
 		String newName = JOptionPane.showInputDialog("The name '" + currName + "' has already been used. Please enter another name.");
 		boolean isValidName = true;
 		CalendarSlots userResp = event.getUserResponse();
 		for(CalendarSlots c : event.getCalendars()){
 			if(c.getOwner().getName().equalsIgnoreCase(newName)){
 				isValidName = false;
 				getNewUserName(event, newName);
 				break;
 			}
 		}
 		if(isValidName)
 			event.getUserResponse().setOwner(new When2MeetOwner(newName, event.getUserResponse().getOwner().getID()));
 	}
 	
 	public void submitResponse(String eventID, CalendarSlots response) {
 		When2MeetEvent w2m = _w2mEvents.get(eventID);	
 		boolean didNotPost = true;
 		while(didNotPost){
 			try {
 				_exporter.postAllAvailability(w2m);
 				didNotPost = false;
 			} catch (NameAlreadyExistsException e) {
 				getNewUserName(w2m, w2m.getUserResponse().getOwner().getName());
 			}
 		}
 		
 		//save to index
 	}
 	
 	public CalendarSlots getBestTimes(String eventID, int duration){
 		//TODO make more generic (hard-coding 15, limiting to w2m)
 		
 		When2MeetEvent w2m = _w2mEvents.get(eventID);
 		CalendarSlots toReturn = null;
 		
 		if(w2m != null){
 			int minAttendees = (int) (w2m.getCalendars().size() * ATTENDEE_PERCENTAGE);
 			toReturn = _timeFinder.findBestTimes(w2m, 15, duration, NUM_SUGGESTIONS, minAttendees);
 		}
 		
 		return toReturn;
 	}
 	
 	
 	public ArrayList<NameIDPair> getNameIDPairs() {
 		// Return the list of name ID pairs associated with all events
 		ArrayList<NameIDPair> toReturn = new ArrayList<NameIDPair>();
 		
 		for(When2MeetEvent e : _w2mEvents.values())
 			toReturn.add(new NameIDPair(e.getName(), String.valueOf(e.getID())));
 	
 		return toReturn;
 	}
 	
 	public CalendarGroup<CalendarResponses> getCal(){
 		return _userCal;
 	}
 	
 	public void pullCal(DateTime start, DateTime end){
 		_userCal = _userCalImporter.refresh(start, end);
 		
 	}
 }

 package cal_master;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 import javax.swing.JOptionPane;
 
 import org.joda.time.DateTime;
 
 //import cal_master.Index.IndexType;
 import calendar.*;
 import calendar_exporters.*;
 import calendar_exporters.When2MeetExporter.NameAlreadyExistsException;
 import calendar_importers.*;
 import calendar_importers.EventImporter.InvalidURLException;
 
 import com.thoughtworks.xstream.XStream;
 
 import ftf.TimeFinderSlots;
 
 
 public class Communicator {
 
 	private CalendarsImporter<CalendarResponses> _userCalImporter;
 	private StoredDataType _userCalImporterType; // = IndexType.GCalImporter;
 	private CalendarGroup<CalendarResponses> _userCal = null;
 	private String _userCalID = "userCal", _indexID = "index", _importerID = "userCalImporter";
 	
 	private HashMap<String, Event> _events = new HashMap<String, Event>();
 	private EventImporter _eventImporter = new EventImporter();
 	private When2MeetExporter _exporter = new When2MeetExporter();
 
 	private Converter _converter = new Converter();
 	private TimeFinderSlots _timeFinder = new TimeFinderSlots();
 	private ProgramOwner _owner = new ProgramOwner();
 	
 	private XStream _xstream = new XStream();
 	//private Index _index = new Index();
 	
 	private static final double ATTENDEE_PERCENTAGE = .75;
 	private static final int NUM_SUGGESTIONS = 5;
 	
 	public Communicator() {
 		_userCalImporter = null;
 		//_userCalImporterType = null;
 	}
 	
 	
 	private void setUpXStream() {
 		_xstream.alias("index", Index.class);
 		_xstream.alias("storeddatatype", StoredDataType.class);
 		_xstream.alias("calendarslots", CalendarSlots.class);
 		_xstream.alias("calendarresponses", CalendarResponses.class);
 		_xstream.alias("calendargroup", CalendarGroup.class);
 		_xstream.alias("calendar", Calendar.class);
 		_xstream.alias("when2meetevent", When2MeetEvent.class);
 		_xstream.alias("when2meetowner", When2MeetOwner.class);
 		_xstream.alias("eventupdate", EventUpdate.class);
 		_xstream.alias("avail", Availability.class);
 	}
 	
 	public Index recreateIndex() {
 		File indexFile = new File(_indexID+".xml");
 		if(indexFile.exists())
 			return (Index) _xstream.fromXML(indexFile);
 		else
 			return new Index();
 	}
 	
 	public void startUp() {
 		
 		setUpXStream();
 		// If have an index, recreate index
 		Index index = recreateIndex();
 		
 		// Pull in XML and create when2meet events from XML
 		// Pull in XML for user cal, and create user cal
 		for(String id : index.getFiles()) {
 			StoredDataType type = index.getType(id);
 			//System.out.println("Type: " + type);
 			assert type != null;
 			Object o = _xstream.fromXML(new File(id + ".xml"));
 			switch(type) {
 			case When2MeetEvent: {
 				_events.put(id,  (When2MeetEvent) o);
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
 				break;
 			} 
 			case GCalImporter: {
 				_userCalImporter = (GCalImporter) o;
 				break;
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
 					_userCalImporterType = StoredDataType.GCalImporter;
 					this.pullCal(DateTime.now(), DateTime.now().plusDays(30));
 				}
 			}
 		}
 		
 		if(this.hasEvent() == false)
 			try {
 				this.addEvent("http://www.when2meet.com/?426631-GoPHt");
 			} catch (URLAlreadyExistsException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		
 		// TODO
 		// Refresh when2meet events
 		// Refresh calendars -> if no calendar, pull in users calendar	
 		refresh();
 			
 	}
 	
 	public boolean hasEvent() {
 		return _events != null && _events.size() > 0;
 	}
 	
 	public boolean hasUserCal() {
 		return _userCal != null;
 	}
 	
 	public CalendarGroup<CalendarSlots> getFirstEvent() {
 		System.out.println("Num events: " + _events.values().size());
 		for(CalendarGroup<CalendarSlots> cal : _events.values())
 			return cal;
 		return null;
 	}
 	
 	public String getFirstEventID() {
 		for(String id : _events.keySet())
 			return id;
 		return null;
 	}
 	
 	public CalendarGroup<CalendarResponses> getUserCal() {
 		return _userCal;
 	}
 	
 	/** SAVING **/
 	
 	/*
 	public void updateIndex() {
 		// Rebuilds the index given the current list of events and calendar
 		for(String id : _events.keySet())
 			_index.addItem(id, IndexType.When2MeetEvent);
 		_index.addItem(_userCalID, IndexType.GCal);
 		_index.addItem(_importerID, _userCalImporterType);
 		writeToFile(_indexID, _index);
 	}
 	*/
 	
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
 	
 	private void saveIndex(Index temp) {
 		writeToFile(_indexID, temp);
 	}
 	
 	// Saves one item
 	public void saveOneItem(Object o, String id, StoredDataType type) {
 		// Check index to see if it exists
 		Index temp = recreateIndex();
 		temp.addItem(id, type);
 		saveIndex(temp);
 		//writeToFile(_indexID,temp);
 		// updateIndex();
 		writeToFile(id, o);
 	}
 	
 	public void removeOneItem(String id) {
 		Index temp = recreateIndex();
 		temp.removeItem(id);
 		saveIndex(temp);
 	}
 	
 	/*
 	public void saveAll() {
 		// Store some form of an update index
 		updateIndex();
 		
 		// Store XML for when2meet events
 		for(String id : _events.keySet()) {
 			writeToFile(id, _events.get(id));
 		}
 		
 		// Store XML for calendar
 		writeToFile(_userCalID, _userCal);
 		
 	}*/
 	
 	public class URLAlreadyExistsException extends Exception {
 		
 	}
 	
 	public Event addEvent(String url) throws URLAlreadyExistsException, IOException {
 		// Check if we have this url already
 		// If we do, throw an error
 		for(Event event : _events.values())
 			if(event.getURL().equals(url)) {
 				System.out.println("URL already found");
 				throw new URLAlreadyExistsException();
 			}
 
 		// If we don't, check that it's a valid url
 		// If it's a valid url, pull in that when2meet using an importer
 		// If it's not a valid url, error message to user
 		
 		Event newEvent;
 		try {
 			newEvent = _eventImporter.importNewEvent(url);
 		} catch (InvalidURLException e) {
 			JOptionPane.showMessageDialog(null, "Invalid URL");
 			return null;
 		}
 
 		StoredDataType eventType = calGroupTypeToIndexType(newEvent.getCalGroupType());
 		String id = newEvent.getID() + "";
 		_events.put(id, newEvent);
 		saveOneItem(newEvent, id, eventType);
 		
 		return newEvent;
 	}
 	
 	public StoredDataType calGroupTypeToIndexType(CalGroupType type) {
 		switch(type) {
 		case When2MeetEvent: { return StoredDataType.When2MeetEvent; }
 		case GCal: {return StoredDataType.GCal;}
 		}
 		return null;
 	}
 	
 	public void setUserCal(CalendarGroup<CalendarResponses> userCal){
 		_userCal = userCal;
 		StoredDataType type = calGroupTypeToIndexType(_userCal.getCalGroupType());
 		saveOneItem(_userCal, _userCalID, type);
 	}
 	
 	public void removeWhen2Meet(String eventID) {
 		// Check that we do have an event with this ID
 		Event toRemove = _events.get(eventID);
 		
 		// If we do, remove it form our list of events
 		// Save this when2meet, and our new index
 		if(toRemove != null){
 			String id = eventID + "";
 			_events.remove(id);
 			removeOneItem(id);
 		}
 		
 		// If we didn't have it, huh? confused, how did this happen
 	}
 	
 	public void setCalImporter(CalendarsImporter<CalendarResponses> importer){
 		System.out.println("Cal importer set");
 		_userCalImporter = importer;
 		StoredDataType type = null;
 		if(_userCalImporter.getClass() == GCalImporter.class)
 			type = StoredDataType.GCalImporter;
 		else {
 			try {
 				throw new Exception();
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.out.println("Wrong type of cal importer");
 			}
 			
 		}
 		this.saveOneItem(_userCalImporter, _importerID, type);
 	}
 	
 	public void setOwnerName(String name){
 		_owner.setName(name);
 	}
 	
 	public void refresh() {
 		//TODO: deal with URL exception
 		// Update all when2meet events
 		When2MeetEvent temp = null;
 		for(Event event : _events.values()){
 			//repull info
 			if(event.getCalGroupType() == CalGroupType.When2MeetEvent) {
 				_eventImporter.refreshEvent((When2MeetEvent) event);
 				saveOneItem(event, event.getID()+"", StoredDataType.When2MeetEvent);
 			}
 			else
 				System.out.println("Invalid event type - not when2meet");
 		}
 		// Update user calendar
 		if(_userCal != null){
 			pullCal(_userCal.getStartTime(), _userCal.getEndTime());
 			saveOneItem(_userCal, _userCalID, StoredDataType.GCal);
 		}
 		
 		// Rebuild index and store all files
 		//saveAll();
 	}
 	
 	public void calToW2M(String eventID){
 		Event w2m = _events.get(eventID);
		
 		if(w2m != null && !w2m.userHasSubmitted()){
			CalendarSlots cal = _converter.calToSlots(_userCal, w2m);
			w2m.setUserResponse(cal);
		}
	}
	
	private void checkUserCal(String eventID){
		Event w2m = _events.get(eventID);
		if(w2m != null){
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
 		}
 	}
 	
 	public Event getEventByID(String id){
 		return _events.get(id);
 	}
 	
 	public Event getW2M(String id){
 		Event toReturn = _events.get(id);
 		if(toReturn.getUserResponse() == null){
 			
 			//ask user if they've responded to the event
 			int resp = JOptionPane.showConfirmDialog(null, "Have you already responded to this When2Meet?", "", JOptionPane.YES_NO_OPTION);
 			
 			//if they have, ask them to select their response from the list of all responses
 			if(resp == JOptionPane.YES_OPTION){
 				
 				Object[] responseNames = new Object[toReturn.getCalendars().size()];
 				
 				for(int i=0; i< toReturn.getCalendars().size(); i++){
 					responseNames[i] = toReturn.getCalendars().get(i).getOwner().getName();
 				}
 				
 				Object selected = JOptionPane.showInputDialog(null, "Please select the name that represents your response from the list below",
 						"", JOptionPane.INFORMATION_MESSAGE, null,
 						responseNames, responseNames[0]);
 				
 				//System.out.println("SELECTED: " + selected);
 				//take the selected cal, remove it from the list, and set it to be the userResponse
 				if(selected != null){
 					CalendarSlots user = toReturn.getCalByName(selected.toString());
 					//toReturn.removeCalendar(user);
 					toReturn.setUserResponse(user);
 				}
 			}
 			//if they haven't, set userResponse to be a new CalendarSlots with them as the owner
 			else{
 				toReturn.setUserResponse(new CalendarSlots(toReturn.getStartTime(), toReturn.getEndTime(), 15, Availability.free));
 				toReturn.getUserResponse().setOwner(new When2MeetOwner(_owner.getName(), -1));
 			}
 			
 		}
		
		checkUserCal(id);
		
 		if(!toReturn.userHasSubmitted()){
 			calToW2M(id);
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
 			userResp.setOwner(new When2MeetOwner(newName, userResp.getOwner().getID()));
 		saveOneItem(event, event.getID()+"", StoredDataType.When2MeetEvent);
 	}
 	
 	private void getOwnerName(){
 		String newName = JOptionPane.showInputDialog("Please enter the name you would like to use in your When2Meet response");
 		if(newName != null){
 			_owner.setName(newName);
 		}
 		//save program owner
 	}
 	
 	public void submitResponse(String eventID, CalendarSlots response) {
 		Event event = _events.get(eventID);	
 		if(_owner.getName() == null){
 			getOwnerName();
 		}
 		
 		if(event.getCalGroupType() == CalGroupType.When2MeetEvent) {
 			boolean didNotPost = true;
 			while(didNotPost){
 				try {
 					_exporter.postAllAvailability((When2MeetEvent) event);
 					didNotPost = false;
 				} catch (NameAlreadyExistsException e) {
 					getNewUserName((When2MeetEvent) event, event.getUserResponse().getOwner().getName());
 				}
 			}
 		} else {
 			System.out.println("Tried to submit a response for event type not when2meet");
 		}
 		
 		//save to index
 	}
 	
 	public CalendarSlots getBestTimes(String eventID, int duration){
 		//TODO make more generic (hard-coding 15, limiting to w2m)
 		
 		Event w2m = _events.get(eventID);
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
 		
 		for(Event e : _events.values())
 			toReturn.add(new NameIDPair(e.getName(), String.valueOf(e.getID())));
 	
 		return toReturn;
 	}
 	
 	public Collection<String> getEventIDs() {
 		return _events.keySet();
 	}
 	
 	public CalendarGroup<CalendarResponses> getCal(){
 		return _userCal;
 	}
 	
 	public void pullCal(DateTime start, DateTime end){
 		_userCal = _userCalImporter.refresh(start, end);
 		
 	}
 }

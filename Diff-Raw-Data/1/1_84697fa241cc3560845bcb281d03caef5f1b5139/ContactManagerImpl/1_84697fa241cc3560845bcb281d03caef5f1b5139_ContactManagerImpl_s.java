 package main;
 
 import java.util.*;
 import java.io.File;
 import java.io.IOException;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 import util.CalendarUtil;
 import util.DataManager;
 import util.DataManagerImpl;
 
 /**
  * A class to manage your contacts and meetings.
  **/
 public class ContactManagerImpl implements ContactManager {
 	private final String DATA_FILE = "contacts.txt";
 	private DataManager data = new DataManagerImpl();
 	private int nextContactId = 0;
 	private int nextMeetingId = 0;
 	private Set<Contact> knownContacts = new HashSet<Contact>();
 	private List<PastMeeting> pastMeetings = new LinkedList<PastMeeting>();
 	private List<FutureMeeting> futureMeetings = new LinkedList<FutureMeeting>();
 	private Map<Integer, Contact> contactIds = new HashMap<Integer, Contact>();
 	private Map<Integer, PastMeeting> pastMeetingIds = new HashMap<Integer, PastMeeting>();
 	private Map<Integer, FutureMeeting> futureMeetingIds = new HashMap<Integer, FutureMeeting>();
 	private Map<Contact, Set<PastMeeting>> contactAttended = new HashMap<Contact, Set<PastMeeting>>();
 	private Map<Contact, Set<FutureMeeting>> contactAttending = new HashMap<Contact, Set<FutureMeeting>>();
 	private Map<Calendar, Set<Meeting>> meetingsOnDate = new HashMap<Calendar, Set<Meeting>>();
 	
 	/**
 	 * Creates a ContactManager using data from previous sessions stored in the local file
 	 * "contacts.txt". If no such file exists an empty ContactManager will be created.
 	 */
 	public ContactManagerImpl() {
 		// Recover previous session info from file if it exists
 		if(new File(DATA_FILE).isFile()) {
 			try {
 				// Load and retrieve the stored contacts and meetings
 				data.loadData(DATA_FILE);
 				knownContacts = data.getContacts();
 				pastMeetings = data.getPastMeetings();
 				futureMeetings = data.getFutureMeetings();
 				
 				// Use this data to populate ID, contact & meeting maps
 				populateMaps();
 			} catch (IOException e) {
 				System.out.println(DATA_FILE + " could not be read");
 				e.printStackTrace();
 			} catch (ParserConfigurationException e) {
 				e.printStackTrace();
 			} catch (SAXException e) {
 				System.out.println("Could not parse XML in " + DATA_FILE);
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Populates the mappings from ID to contacts, past meetings and future meetings,
 	 * plus the mappings from contact to meetings attended/attending, and from date to meetings
 	 * occurred/occurring on that date. 
 	 */
 	private void populateMaps() {
 		// Contacts
 		for(Contact contact : knownContacts) {
 			contactIds.put(contact.getID(), contact);
 			// Initialise the set of past and future meetings attended,
 			// using tree set to keep meetings ordered chronologically
 			// (see http://java2novice.com/java-collections-and-util/treeset/comparator-object/)
 			contactAttended.put(contact, new TreeSet<PastMeeting>(CalendarUtil.getMeetingComparator()));
 			contactAttending.put(contact, new TreeSet<FutureMeeting>(CalendarUtil.getMeetingComparator()));
 		}
 		// Past meetings
 		for(PastMeeting meeting : pastMeetings) {
 			pastMeetingIds.put(meeting.getID(), meeting);
 			for(Contact attendee : meeting.getContacts()) {
 				contactAttended.get(attendee).add(meeting);
 			}
 			// Initialise the set of meetings that occurred on this date
 			meetingsOnDate.put(CalendarUtil.trimTime(meeting.getDate()), new TreeSet<Meeting>(CalendarUtil.getMeetingComparator()));
 		}
 		// Future meetings
 		for(FutureMeeting meeting : futureMeetings) {
 			futureMeetingIds.put(meeting.getID(), meeting);
 			for(Contact attendee : meeting.getContacts()) {
 				contactAttending.get(attendee).add(meeting);
 			}
 		}
 	}
 
 	@Override
 	public int addFutureMeeting(Set<Contact> contacts, Calendar date) {
 		// Check that the given date is in the future
 		if (!CalendarUtil.isInFuture(date)) {
 			throw new IllegalArgumentException("Given date, " + CalendarUtil.format(date) + ", is not in the future");
 		}
 
 		// Make sure each contact is known
 		try {
 			checkContactsAreKnown(contacts);
 		} catch(NullPointerException e) {
 			throw new NullPointerException("Given contacts contains null contact");
 		} catch(IllegalArgumentException e) {
 			throw new IllegalArgumentException("Given contacts contains unknown contact", e);
 		}
 		
 		// All is well, add the meeting
 		int id = nextMeetingId++;
 		FutureMeeting newMeeting = new FutureMeetingImpl(id, contacts, date);
 		futureMeetings.add(newMeeting);
 		futureMeetingIds.put(id, newMeeting);
 		
 		return id;
 	}
 
 	/**
 	 * Checks that no contacts in the given set are either null or unknown.
 	 * 
 	 * @param contacts the set of contacts to check
 	 * @throws NullPointerException if any contact is null
 	 * @throws IllegalArgumentException if any contact is unknown
 	 */
 	private void checkContactsAreKnown(Set<Contact> contacts) {
 		for(Contact contact : contacts) {
 			if(contact == null) {
 				throw new NullPointerException();
 			}
 			if(!contactIds.containsKey(contact.getID())) {
 				throw new IllegalArgumentException();
 			}
 		}
 	}
 
 	@Override
 	public PastMeeting getPastMeeting(int id) {
 		// Check that the ID isn't that of a future meeting
 		if(futureMeetingIds.containsKey(id)) {
 			throw new IllegalArgumentException("Requested ID, " + id + ", belongs to a future meeting");
 		}
 
 		// Fetch the meeting with this id (if no mapping for id get(id) returns null)
 		PastMeeting requestedMeeting = pastMeetingIds.get(id);
 		return requestedMeeting;
 	}
 
 	@Override
 	public FutureMeeting getFutureMeeting(int id) {
 		// Check that the ID isn't that of a past meeting
 		if(pastMeetingIds.containsKey(id)) {
 			throw new IllegalArgumentException("Requested ID, " + id + ", belongs to a past meeting");
 		}
 
 		// Fetch the meeting with this id (if no mapping for id get(id) returns null)
 		FutureMeeting requestedMeeting = futureMeetingIds.get(id);
 		return requestedMeeting;
 	}
 
 	@Override
 	public Meeting getMeeting(int id) {
 		Meeting requestedMeeting = pastMeetingIds.get(id);
 		
 		if(requestedMeeting == null) {
 			return (Meeting) futureMeetingIds.get(id);
 		}
 		return requestedMeeting; 
 	}
 
 	@Override
 	public List<Meeting> getFutureMeetingList(Contact contact) {
 		// Check that this contact is known and not null
 		if(contact == null) {
 			throw new NullPointerException("Given contact is null");
 		}
 		if(!knownContacts.contains(contact)) {
 			throw new IllegalArgumentException("Given contact does not exist");
 		}
 		
 		// Fetch the set of future meetings this contact is contactAttending
 		// (tree set has taken care of chronological ordering)
 		// (may be empty)
 		return new LinkedList<Meeting>(contactAttending.get(contact));
 	}
 
 	@Override
 	public List<Meeting> getFutureMeetingList(Calendar date) {
 		// Fetch meetings on this date
 		Set<Meeting> requestedMeetings = meetingsOnDate.get(CalendarUtil.trimTime(date));
 		
 		// If no meetings on this date, requestedMeetings is null
 		if(requestedMeetings == null) {
 			// Return empty list
 			return new LinkedList<Meeting>();
 		}
 		
 		return new LinkedList<Meeting>(requestedMeetings);
 	}
 
 	@Override
 	public List<PastMeeting> getPastMeetingList(Contact contact) {
 		// Check that this contact is known and not null
 		if(contact == null) {
 			throw new NullPointerException("Given contact is null");
 		}
 		if(!knownContacts.contains(contact)) {
 			throw new IllegalArgumentException("Given contact does not exist");
 		}
 		
 		// Fetch the set of past meetings this contact attended
 		// (tree set has taken care of chronological ordering)
 		// (may be empty)
 		return new LinkedList<PastMeeting>(contactAttended.get(contact));		
 	}
 
 	@Override
 	public void addNewPastMeeting(Set<Contact> contacts, Calendar date, String text) {
 		// Ensure arguments are not null
 		if(contacts == null) {
 			throw new NullPointerException("Contacts is null");
 		}
 		if(date == null) {
 			throw new NullPointerException("Date is null");
 		}
 		if(text == null) {
 			throw new NullPointerException("Text is null");
 		}
 		
 		// Ensure all contacts are known... 
 		try {
 			checkContactsAreKnown(contacts);
 		} catch (IllegalArgumentException e) {
 			throw new IllegalArgumentException("Contacts contains an unknown contact", e);
 		}
 		// ...and the date is in the past
 		if(!CalendarUtil.isInPast(date)) {
 			throw new IllegalArgumentException("Date is not in past");
 		}
 		
 		// Arguments check out, create meeting and add it to collections
 		createPastMeeting(contacts, date, text);
 	}
 
 	/**
 	 * Handles the initialisation of a PastMeeting and adds it to the appropriate collections
 	 * using the contacts, date and text provided.
 	 *  
 	 * @param contacts the contacts who attended the meeting
 	 * @param date the date and time the meeting took place
 	 * @param text messages to record about the meeting
 	 */
 	private void createPastMeeting(Set<Contact> contacts, Calendar date, String text) {
 		// Get an ID
 		int id = nextMeetingId++;
 		
 		// Initialise
 		PastMeeting newMeeting = new PastMeetingImpl(id, contacts, date, text);
 		
 		// Add to collections
 		pastMeetings.add(newMeeting);
 		pastMeetingIds.put(id, newMeeting);
 		meetingsOnDate.get(CalendarUtil.trimTime(date)).add(newMeeting);
 		for(Contact contact : contacts) {
 			contactAttended.get(contact).add(newMeeting);
 		}
 	}
 
 	@Override
 	public void addMeetingNotes(int id, String text) {
 		// Check notes not null
 		if(text == null) {
 			throw new NullPointerException("Notes are null");
 		}
 		
 		// Determine if meeting is in past or future
 		if(pastMeetingIds.containsKey(id)) {
 			// As meeting is already in past, no need to check date
 			// Can append to the meeting's notes using PastMeetingImpl method addNotes(String)
 			((PastMeetingImpl) pastMeetingIds.get(id)).addNotes(text);
 			
 		} else if(futureMeetingIds.containsKey(id)) {
 			// Ensure this meeting has occurred
 			Meeting meeting = futureMeetingIds.remove(id);
 			if(!CalendarUtil.isInPast(meeting.getDate())) {
 				throw new IllegalStateException("Meeting with ID = " + id + " is a future meeting");
 			}
 			
 			// As meeting has occurred, remove it from collections
 			futureMeetings.remove(meeting);
 			for(Contact contact : meeting.getContacts()) {
 				contactAttending.get(contact).remove(meeting);
 			}
 			meetingsOnDate.get(CalendarUtil.trimTime(meeting.getDate())).remove(meeting);
 			
 			// Initialise a new past meeting 
 			PastMeeting pastMeeting = new PastMeetingImpl(meeting, text);
 			
 			// Add to collections
 			pastMeetings.add(pastMeeting);
 			pastMeetingIds.put(id, pastMeeting);
 			meetingsOnDate.get(CalendarUtil.trimTime(pastMeeting.getDate())).add(pastMeeting);
 			
 			for(Contact contact : pastMeeting.getContacts()) {
 				Set<PastMeeting> meetingsAttended = contactAttended.get(contact); 
 				// May be no meetings attended (this meeting may have been the first attended by this contact)
 				if(meetingsAttended == null) {
 					meetingsAttended = new TreeSet<PastMeeting>(CalendarUtil.getMeetingComparator());
 				}
 				meetingsAttended.add(pastMeeting);
 			}
 		} else {
 			throw new IllegalArgumentException("Meeting with ID = " + id + " does not exist");
 		}
 	}
 
 	@Override
 	public void addNewContact(String name, String notes) {
 		// Check for null arguments
 		if(name == null) {
 			throw new NullPointerException("Name is null");
 		}
 		if(notes == null) {
 			throw new NullPointerException("Notes are null");
 		}
 		
 		// Obtain an ID
 		int id = nextContactId++;
 		
 		// Initialise and add
 		Contact contact = new ContactImpl(id, name, notes);
 		knownContacts.add(contact);
 	}
 
 	@Override
 	public Set<Contact> getContacts(int... ids) {
 		Set<Contact> requestedContacts = new HashSet<Contact>();
 		
 		for(int id : ids) {
 			// Make sure the contact is known
 			if(!contactIds.containsKey(id)) {
 				throw new IllegalArgumentException("A contact with ID = " + id + " does not exist");
 			}
 			requestedContacts.add(contactIds.get(id));
 		}
 		return requestedContacts;
 	}
 
 	@Override
 	public Set<Contact> getContacts(String name) {
 		if(name == null) {
 			throw new NullPointerException("Name is null");
 		}
 		Set<Contact> matchingContacts = new HashSet<Contact>();
 		
 		for(Contact contact : knownContacts) {
 			if(contact.getName().contains(name)) {
 				matchingContacts.add(contact);
 			}
 		}
 		
 		return matchingContacts;
 	}
 
 	@Override
 	public void flush() {
 		// Send contacts, past and future meetings to the data manager
 		data.addContacts(knownContacts);
 		data.addPastMeetings(pastMeetings);
 		data.addFutureMeetings(futureMeetings);
 		
 		// Save data to disk
 		try {
 			data.saveData(DATA_FILE);
 		} catch (IOException e) {
 			System.out.println("Could not write to file " + DATA_FILE);
 			e.printStackTrace();
 		}
 	}
 }

 import java.util.Calendar;
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Collections;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 //A class to manage your contacts and meetings
 public class ContactManagerImpl implements ContactManager {
 	private static final String FILE = "contacts.txt";
 	private Map<Integer, Contact> idContactsMap;
 	private Map<Integer, Meeting> idMeetingsMap;
 	
 	@SuppressWarnings("unchecked")//Suppresses due unchecked casts.
 	public ContactManagerImpl() {//Don't know what input.readObject() will be an instance of.
 		try {
 			if (new File(FILE).exists()) {
 				ObjectInputStream input = new ObjectInputStream(
 						new BufferedInputStream(new FileInputStream(FILE)));
 				idContactsMap = (Map<Integer, Contact>) input.readObject();//UNCHECKED CAST	
 				idMeetingsMap = (Map<Integer, Meeting>) input.readObject();//UNCHECKED CAST
 			} else {
 				idContactsMap = new HashMap<>();
 				idMeetingsMap = new HashMap<>();
 			}
 		} catch (IOException | ClassNotFoundException ex) {
 			ex.printStackTrace();
 			System.err.println("Error on read; " + ex);
 		}
 	}
 	
 					/**************************************
 					*    SUPPLEMENTARY PRIVATE METHODS    *
 					**************************************/
 	//Takes a set of contacts as argument and complains if one or more contact(s) is null/empty/unknown.
 	private void checkContactsAreKnown(Set<Contact> contacts) {
 		if (contacts == null) {
 			throw new NullPointerException("Set of contacts points to null");
 		} else if (contacts.isEmpty()) {
 			throw new IllegalArgumentException("Set of contacts is empty.");
 		}
 		for (Contact contact : contacts) {
 			boolean unknownContact = !idContactsMap.containsValue(contact);
 			if (unknownContact) {
 				throw new IllegalArgumentException(contact.getName() + " is an unknown contact");
 			}
 		}
 	}
 	//Takes one contact as argument and complains if contact is null/unknown.
 	private void checkContactIsKnown(Contact contact) {
 		if (contact == null) {
 			throw new NullPointerException("Contact points to null");
 		} else if (!idContactsMap.containsValue(contact)) {
 			throw new IllegalArgumentException(contact.getName() + " is an unknown contact");
 		}
 	}
 	//Following two methods checks date and text arguments for null, respectively.
 	private void checkForNull(Calendar date) {
 		if (date == null) {
 			throw new NullPointerException("Date points to null");
 		}
 	}
 	private void checkForNull(String text) {
 		if (text == null) {
 			throw new NullPointerException("Text, i.e. name or notes, points to null");
 		}
 	}
 	//Following two methods makes sure dates are in the past or future, respectively.
 	private void complainIfFuture(Calendar date) {
 		checkForNull(date);
 		if (date.after(Calendar.getInstance())) {
 			throw new IllegalArgumentException("Date of meeting should be in the past.");
 		}
 	}
 	private void complainIfPast(Calendar date) {
 		checkForNull(date);
 		if (date.before(Calendar.getInstance())) {
 			throw new IllegalArgumentException("Date of meeting should be in the future.");
 		}
 	}
 	
 					/***********************
 					*    MEETING METHODS   *
 					***********************/
 	//Returns the meeting with the requested ID, or null if it there is none.
 	public Meeting getMeeting(int id) {
 		return idMeetingsMap.get(id);
 	}
 	//Adds notes to a past meeting or converts future meeting to a past meeting then adds notes
 	public void addMeetingNotes(int id, String text) {
 		Meeting meeting = getMeeting(id);
 		checkForNull(text);
 		if (meeting == null) {
 			throw new IllegalArgumentException("Meeting does not exist.");
 		} else if (meeting.getDate().after(Calendar.getInstance())) {
 			throw new IllegalStateException("Meeting is set for a date in the future.");
 		} else if (meeting instanceof PastMeetingImpl) {
 			PastMeetingImpl sameMeeting = (PastMeetingImpl) meeting;//Downcast because PastMeetingImpl has addNotes()
 			sameMeeting.addNotes(text);
 		} else {//Instanceof FutureMeeting and needs to be replaced by an instance of PastMeeting
 			idMeetingsMap.remove(meeting);
 			Meeting pastMeeting = new PastMeetingImpl(id, meeting.getContacts(), meeting.getDate(), text);
 			idMeetingsMap.put(id, pastMeeting);	
 		}
 	}
 	//Returns the list of sorted meetings that are scheduled for, OR THAT TOOK PLACE ON, the specified date.
 	public List<Meeting> getFutureMeetingList(Calendar date) {//Better name would have been getMeetingList()
 		checkForNull(date);
 		List<Meeting> meetingsList = new LinkedList<>();
 		for (Meeting meeting : idMeetingsMap.values()) {
 			if (meeting.getDate().equals(date)) {
 				meetingsList.add(meeting);
 			}
 		}
 		Collections.sort(meetingsList, new DateMeetingComparator());
 		return meetingsList;
 	}
 	
 					/****************************
 					*    PASTMEETING METHODS    *
 					****************************/
 	//Returns the PAST meeting with the requested ID, or null. Complains if the meeting is in the future.
 	public PastMeeting getPastMeeting(int id) {
 		Meeting pastMeeting = getMeeting(id);
 		complainIfFuture(pastMeeting.getDate());
 		if (pastMeeting == null) {
 			return null;
 		} else if (!(pastMeeting instanceof PastMeeting)) {
 			addMeetingNotes(id, "");//Converts this meeting from FutureMeeting type to PastMeeting type
 		}
 		return (PastMeeting) getMeeting(id);
 	}
 	//Returns the list of past meetings in which this contact has participated. Complains if contact is unknown.
 	public List<PastMeeting> getPastMeetingList(Contact contact) {
 		checkContactIsKnown(contact);
 		List<PastMeeting> contactPastMeetings = new LinkedList<>();
 		for (Meeting meeting : idMeetingsMap.values()) {
 			if (meeting.getContacts().contains(contact) && meeting.getDate().before(Calendar.getInstance())) {
 				if (!(meeting instanceof PastMeeting)) {
 					int id = meeting.getId();
 					addMeetingNotes(id, "");//Converts this meeting from FutureMeeting type to PastMeeting type
 					meeting = getMeeting(id);
 				}
 				contactPastMeetings.add((PastMeeting) meeting);
 			}
 		}
 		Collections.sort(contactPastMeetings, new DateMeetingComparator());
 		return contactPastMeetings;
 	}
 	//Create a new record for a meeting that took place in the past.
 	public void addNewPastMeeting(Set<Contact> contacts, Calendar date, String text) {
 		checkContactsAreKnown(contacts);
 		complainIfFuture(date);
 		checkForNull(text);
 		Meeting pastMeeting = new PastMeetingImpl(contacts, date, text);
 		int id = pastMeeting.getId();
 		idMeetingsMap.put(id, pastMeeting);
 	}
 	
 					/******************************
 					*    FUTUREMEETING METHODS    *
 					******************************/
 	//Adds a new meeting to be held in the future. Complains if a contact is unknown or if the date is in the past.
 	public int addFutureMeeting(Set<Contact> contacts, Calendar date) {
 		checkContactsAreKnown(contacts);
 		complainIfPast(date);
 		Meeting futureMeeting = new FutureMeetingImpl(contacts, date);
 		int id = futureMeeting.getId();
 		idMeetingsMap.put(id, futureMeeting);
 		return id;
 	}
 	//Returns the FUTURE meeting with the requested ID, or null. Complains if the meeting is in the past.
 	public FutureMeeting getFutureMeeting(int id) {
 		Meeting futureMeeting = getMeeting(id);
 		complainIfPast(futureMeeting.getDate());
 		return (FutureMeeting) futureMeeting;
 	}
 	
 	//Returns the list of sorted future meetings scheduled with this contact. Complains if contact is unknown.
 	public List<Meeting> getFutureMeetingList(Contact contact) {
 		checkContactIsKnown(contact);
 		List<Meeting> contactFutureMeetings = new LinkedList<>();
 		for (Meeting meeting : idMeetingsMap.values()) {
 			if (meeting.getContacts().contains(contact) && meeting.getDate().after(Calendar.getInstance())) {
 				contactFutureMeetings.add(meeting);
 			}
 		}
 		Collections.sort(contactFutureMeetings, new DateMeetingComparator());
 		return contactFutureMeetings;
 	}
 	
 					/************************
 					*    CONTACT METHODS    *
 					************************/
 	//Create a new contact with the specified name and notes.
 	public void addNewContact(String name, String notes) {
 		checkForNull(name);
 		checkForNull(notes);
 		Contact newContact = new ContactImpl(name, notes);
 		int id = newContact.getId();
 		idContactsMap.put(id, newContact);
 	}
 	//Returns a list containing the contacts that correspond to the IDs.
 	public Set<Contact> getContacts(int... ids) {
 		Set<Contact> contacts = new HashSet<>();
 		for (int i = 0; i < ids.length; i++) {
 			Contact contact = idContactsMap.get(ids[i]);
 			contacts.add(contact);
 		}
 		return contacts;
 	}
 	//Returns a list with the contacts whose name contains that string.
 	public Set<Contact> getContacts(String name) {
 		checkForNull(name);
 		Set<Contact> contacts = new HashSet<>();
 		for (Contact contact : idContactsMap.values()) {
 			if (contact.getName().toLowerCase().trim().equals(name.toLowerCase().trim())) {
 				contacts.add(contact);
 			}
 		}
 		return contacts;
 	}
 	
 					/******************************
 					*    SAVE ALL DATA TO DISK    *
 					******************************/
 	public void flush() {
 		ObjectOutputStream output = null;
 		try {
 			output = new ObjectOutputStream(
 					new BufferedOutputStream(new FileOutputStream(FILE)));
			output.writeObject(idContactsMap);
			output.writeObject(idMeetingsMap);
		} catch (IOException ex) {
			ex.printStackTrace();
 			System.err.println("Error on write: " + ex);
		} finally {
 			try {
 				output.close();
 			} catch (IOException ex) {
 				ex.printStackTrace();
 				System.err.println("Error on close: " + ex);
 			}
 		}
 	}
 }

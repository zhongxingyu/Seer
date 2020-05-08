 import java.util.Calendar;
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Set;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Collections;
 //A class to manage your contacts and meetings
 public class ContactManagerImpl implements ContactManager {
 	private Map<Integer, Contact> idContactsMap;
 	private Map<Integer, Meeting> idMeetingsMap;
 
 	public ContactManagerImpl() {
 		idContactsMap = new HashMap<>();
 		idMeetingsMap = new HashMap<>();
 	}
 
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
 
 	//Adds a new meeting to be held in the future. Complains if a contact is unknown or if the date is in the past.
 	public int addFutureMeeting(Set<Contact> contacts, Calendar date) {
 		checkContactsAreKnown(contacts);
 		complainIfPast(date);
 		Meeting futureMeeting = new FutureMeetingImpl(contacts, date);
 		int meetingId = futureMeeting.getId();
 		idMeetingsMap.put(meetingId, futureMeeting);
 		return meetingId;
 	}
 
 	//Returns the PAST meeting with the requested ID, or null. Complains if the meeting is in the future.
 	public PastMeeting getPastMeeting(int id) {
 		Meeting pastMeeting = getMeeting(id);
 		complainIfFuture(pastMeeting.getDate());
 		if (pastMeeting == null) {
 			return null;
 		} else if (!(pastMeeting instanceof PastMeeting)) {
//FIX HERE
 		}
		return (PastMeeting) pastMeeting;
 	}
 
 	//Returns the FUTURE meeting with the requested ID, or null. Complains if the meeting is in the past.
 	public FutureMeeting getFutureMeeting(int id) {
 		Meeting futureMeeting = getMeeting(id);
 		complainIfPast(futureMeeting.getDate());
 		return (FutureMeeting) futureMeeting;
 	}
 
 	//Returns the meeting with the requested ID, or null if it there is none.
 	public Meeting getMeeting(int id) {
 		return idMeetingsMap.get(id);
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
 
 	//Returns the list of sorted meetings that are scheduled for, OR THAT TOOK PLACE ON, the specified date.
 	public List<Meeting> getFutureMeetingList(Calendar date) {
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
 
 	//Returns the list of past meetings in which this contact has participated. Complains if contact is unknown.
 	public List<PastMeeting> getPastMeetingList(Contact contact) {
 		checkContactIsKnown(contact);
 		List<PastMeeting> contactPastMeetings = new LinkedList<>();
 		for (Meeting meeting : idMeetingsMap.values()) {
 			if (meeting.getContacts().contains(contact) && meeting.getDate().before(Calendar.getInstance())) {
 				if (!(meeting instanceof PastMeeting)) {
//FIX HERE
 				}
				contactPastMeeting.add(pastMeeting);
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
 		int meetingId = pastMeeting.getId();
 		idMeetingsMap.put(meetingId, pastMeeting);
 	}
 	//Adds notes to a past meeting or after a future meeting has taken place and is be converted to a past meeting.
 	public void addMeetingNotes(int id, String text) {
 		String notes;
 		Meeting meeting = getMeeting(id);
 		checkForNull(text);
 		if (meeting == null) {
 			throw new IllegalArgumentException("Meeting does not exist.");
		} else if (meeting.getDate().after(Calendar.getInstance()) {
 			throw new IllegalStateException("Meeting is set for a date in the future.");
 		} else if (meeting instanceof PastMeeting) {
 			meeting.addNotes(text);
 		} else {
			PastMeeting pastMeeting = new PastMeetingImpl(id, meeting.getContacts(), meeting.getDate(), text);
 			idMeetingsMap.remove(meeting);
 			idMeetingsMap.put(id, pastMeeting);
 		}	
 	}
 
 	public void addNewContact(String name, String notes) {
 		checkForNull(name);
 		checkForNull(notes);
 		
 	}
 
 	public Set<Contact> getContacts(int... ids) {
 		
 	}
 
 	public Set<Contact> getContacts(String name) {
 		checkForNull(name);
 		
 	}
 
 	public void flush() {
 		
 	}
 }

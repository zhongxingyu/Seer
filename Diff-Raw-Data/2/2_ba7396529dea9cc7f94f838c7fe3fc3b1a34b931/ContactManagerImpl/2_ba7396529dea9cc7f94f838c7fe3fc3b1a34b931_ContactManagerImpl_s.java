 /**
 *The purpose of this assignment it writing a program to keep track of contacts and 
 *meetings. The application will keep track of contacts, past and future meetings, etc. 
 *When the application is closed, all data must be stored in a text file called 
 *”contacts.txt”. This file must be read at startup to recover all data introduced in a 
 *former session.
 */
 
 import java.util.*;
 import java.io.*;
 
 
 public class ContactManagerImpl { 
 	private IllegalArgumentException illegalArgEx = new IllegalArgumentException();
 	private NullPointerException nullPointerEx = new NullPointerException();
 	private IllegalStateException illegalStateEx = new IllegalStateException();
 	private Set<Contact> contactList = new HashSet<Contact>(); //contacts added to this via addContact()
 	private Set<Contact> attendeeList = new HashSet<Contact>(); //contacts attending a specific meeting; may be removed to be replaced with more temporary set in main method
 	private Set<Meeting> pastMeetings = new HashSet<Meeting>();//list of past meetings
 	private Set<Meeting> futureMeetings = new HashSet<Meeting>();
 	private Set<Meeting> allMeetings = new HashSet<Meeting>();//may not be required
 
 	
 	public int addFutureMeeting(Set<Contact> contacts, Calendar date) {
 		boolean isEmpty = false; //these booleans facilitate display of pertinent error message
 		boolean falseContact = false;
 		boolean falseDate = false;
 		Contact element = null;//to keep track of contacts being iterated
 		String unknownContacts = "The following contacts do not exist in your contact list: ";//for multiple unknowns
 		Meeting futureMeeting = null;
 		try {
 			if (contacts.isEmpty()) {
 				isEmpty = true;
 			}
 			Iterator<Contact> iterator = contacts.iterator();//check that contacts are known/existent against central contact list
 			while (iterator.hasNext()) {
 				element = iterator.next();
 				if (!contactList.contains(element)) {
 					falseContact = true;
 					unknownContacts = unknownContacts + "\n" + element.getName();				
 				}
 			}
 			Calendar now = Calendar.getInstance(); //what about scheduling a meeting for today?			
 			if (date.before(now)) {
 				falseDate = true;
 			}
 			if (isEmpty || falseContact || falseDate) {
 				throw illegalArgEx;
 			}				
 		}		
 		catch (IllegalArgumentException illegalArgEx) {
 			if (isEmpty == true) {
 				System.out.println("Error: No contacts have been specified.");
 			}
 			if (falseContact == true) {
 				System.out.println("Error: " + unknownContacts);
 				//Need to consider the users options after exception is thrown - retry the creation of meeting/allow reentry of contacts
 			} 
 			if (falseDate == true) {
 				System.out.println("Error: Invalid date. Please ensure the date and time are in the future.");
 			}			 
 		}	
 		futureMeeting = new FutureMeetingImpl(contacts, date);
 		futureMeetings.add(futureMeeting);
 		int meetingID = futureMeeting.getId();
 		return meetingID;
 	}
 	
 	/**
 	* Returns the PAST meeting with the requested ID, or null if it there is none. 
 	* 
 	* @param id the ID for the meeting 
 	* @return the meeting with the requested ID, or null if it there is none.
 	* @throws IllegalArgumentException if there is a meeting with that ID happening in the future
 	*/
 	public PastMeeting getPastMeeting(int id) {
 		try {
 			Iterator<Meeting> iteratorFM = futureMeetings.iterator();
 			Meeting meeting = null;
 			while (iteratorFM.hasNext()) {
 				meeting = iteratorFM.next();
 				if (meeting.getId() == id) {
 					throw illegalArgEx;
 				}
 			}
 		}
 		catch (IllegalArgumentException ex) {
 			System.out.print("Error: The meeting with this ID has not taken place yet!");
 		}
 		Iterator<Meeting> iteratorPM = pastMeetings.iterator();
 			Meeting meeting = null;
 			while (iteratorPM.hasNext()) {
 				meeting = iteratorPM.next();
 				if (meeting.getId() == id) {
 					PastMeeting pastMeeting = (PastMeeting) meeting;
 					return pastMeeting;
 				}
 			}			
 		return null;
 	}
 	
 	/** 
 	* Returns the FUTURE meeting with the requested ID, or null if there is none. 
 	* 
 	* @param id the ID for the meeting 
 	* @return the meeting with the requested ID, or null if it there is none. 
 	* @throws IllegalArgumentException if there is a meeting with that ID happening in the past 
 	*/
 	public FutureMeeting getFutureMeeting(int id) {
 		try {
 			Iterator<Meeting> iteratorPM = pastMeetings.iterator();
 			Meeting meeting = null;
 			while (iteratorPM.hasNext()) {
 				meeting = iteratorPM.next();
 				if (meeting.getId() == id) {
 					throw illegalArgEx;
 				}
 			}
 		}
 		catch (IllegalArgumentException ex) {
 			System.out.print("Error: The meeting with this ID has already taken place!");
 		}
 		Iterator<Meeting> iteratorFM = pastMeetings.iterator();
 		Meeting meeting = null;
 		while (iteratorFM.hasNext()) {
 			meeting = iteratorFM.next();
 			if (meeting.getId() == id) {
 				FutureMeeting futureMeeting = (FutureMeeting) meeting;
 				return futureMeeting;
 			}
 		}
 		return null;
 	}
 	
 	
 	public Meeting getMeeting(int id) {
 		Iterator<Meeting> iterator = allMeetings.iterator();//rather than allMeetings, check through past/future sets
 		Meeting meeting = null;
 		while (iterator.hasNext()) {
 			meeting = iterator.next();
 			if (meeting.getId() == id) {
 				return meeting;
 			}
 		}
 		return null;
 	}
 	
 	/** 
 	* Returns the list of future meetings scheduled with this contact. 
 	* 
 	* If there are none, the returned list will be empty. Otherwise, 
 	* the list will be chronologically sorted and will not contain any 
 	* duplicates. 
 	* 
 	* @param contact one of the user’s contacts 
 	* @return the list of future meeting(s) scheduled with this contact (may be empty)
 	* @throws IllegalArgumentException if the contact does not exist
 	*/
 	public List<Meeting> getFutureMeetingList(Contact contact) {
 		List<Meeting> list = new ArrayList<Meeting>();//list to contain meetings attended by specified contact
 		try {
 			if (!contactList.contains(contact)) {//may need to use id to identify -> iterator required
 				throw illegalArgEx;
 			}
 			Iterator<Meeting> iterator = futureMeetings.iterator();
 			Meeting meeting = null;
 			while (iterator.hasNext()) { //goes through all future meetings
 				meeting = iterator.next();
 				Iterator<Contact> conIterator = meeting.getContacts().iterator();
 				Contact item = null;
 				while (conIterator.hasNext()) { //goes through contacts associated with a meeting
 					item = conIterator.next();
 					if (item.getId() == contact.getId()) {
 						list.add(meeting);
 					}
 				}
 			}
 			list = sort(list);//elimination of duplicates? With sets, there shouldn't be any...
 			return list;
 		}
 		catch (IllegalArgumentException ex) {
 			System.out.println("Error: The specified contact doesn't exist!");
 		}
 	return list; //may be empty
 	}
 	
 	/**
 	* Sorts a list into chronological order
 	*/
 	private List<Meeting> sort(List<Meeting> list) {
 		Meeting tempMeeting1 = null;
 		Meeting tempMeeting2 = null;
 		boolean sorted = true;
 		for (int j = 0; j < list.size() - 1; j++) {
 			tempMeeting1 = list.get(j);
 			tempMeeting2 = list.get(j + 1);
 			if (tempMeeting1.getDate().after(tempMeeting2.getDate())) {
 				//swaps elements over if first element has later date than second
 				list.set(j, tempMeeting2); //replaced add with set to avoid list growing when rearranging elements
 				list.set(j + 1, tempMeeting1);
 			}
 		}
 		for (int i = 0; i < list.size() - 1; i++) { //loop that checks whether list is sorted
 			if (list.get(i).getDate().after(list.get(i + 1).getDate())) {
 				sorted = false;
 			}			
 		}
 		if (!sorted) {
 			list = sort(list);//recursively calls this method until the list is sorted
 		}
 		return list;
 	}
 		
 	/** 
 	* Returns the list of meetings that are scheduled for, or that took 
 	* place on, the specified date 
 	* 
 	* If there are none, the returned list will be empty. Otherwise, 
 	* the list will be chronologically sorted and will not contain any 
 	* duplicates. 
 	* 	
 	* @param date the date 
 	* @return the list of meetings 
 	*/
 	List<Meeting> getFutureMeetingList(Calendar date) {
 		List<Meeting> meetingList = new ArrayList<Meeting>();
 		//go through future meetings and past meetings, unless all meetings are also added to allMeetings?
 		Iterator<Meeting> iteratorPM = pastMeetings.iterator();
 		Meeting pastMeeting = null;
 		while (iteratorPM.hasNext()) {
 			pastMeeting = iteratorPM.next();
 			if (pastMeeting.getDate().equals(date)) {
 			//or futureMeeting.getDate().get(Calendar.YEAR) == date.get(Calendar.YEAR) etc
 				meetingList.add(pastMeeting);
 			}
 		}
 		Iterator<Meeting> iteratorFM = futureMeetings.iterator();
 		Meeting futureMeeting = null;
 		while (iteratorFM.hasNext()) {
 			futureMeeting = iteratorFM.next();
 			if (futureMeeting.getDate().equals(date)) {
 				meetingList.add(futureMeeting);
 			}
 		}
 		meetingList = sort(meetingList);
 		return meetingList;	
 	}
 	
 	/** 
 	* Returns the list of past meetings in which this contact has participated. 
 	* 
 	* If there are none, the returned list will be empty. Otherwise, 
 	* the list will be chronologically sorted and will not contain any 
 	* duplicates. 
 	* 
 	* @param contact one of the user’s contacts 
 	* @return the list of future meeting(s) scheduled with this contact (maybe empty). 
 	* @throws IllegalArgumentException if the contact does not exist
 	*/ 
 	List<PastMeeting> getPastMeetingList(Contact contact) {
 		List<Meeting> meetingList = new ArrayList<Meeting>();
 		List<PastMeeting> pastMeetingList = new ArrayList<PastMeeting>();
 		try {
 			if (!contactList.contains(contact)) {
 				throw illegalArgEx;
 			}
 			Iterator<Meeting> iterator = pastMeetings.iterator();
 			Meeting meeting = null;
 			while (iterator.hasNext()) {
 				meeting = iterator.next();
 				if (meeting.getContacts().contains(contact)) { 
 					meetingList.add(meeting);
 				}
 			}
 			meetingList = sort(meetingList);
 			for (int i = 0; i < meetingList.size(); i++) {//convert List<Meeting> to List<PastMeeting>
 				Meeting m = meetingList.get(i);
 				PastMeeting pm = (PastMeeting) m;
 				pastMeetingList.add(pm);
 			}	
 			return pastMeetingList;				
 		}
 		catch (IllegalArgumentException ex) {
 			System.out.println("Error: The specified contact doesn't exist.");
 		}
 		return null;
 	}
 	
 	/** 
 	* Create a new record for a meeting that took place in the past. 
 	* 
 	* @param contacts a list of participants 
 	* @param date the date on which the meeting took place 
 	* @param text messages to be added about the meeting. 
 	* @throws IllegalArgumentException if the list of contacts is 
 	* empty, or any of the contacts does not exist 
 	* @throws NullPointerException if any of the arguments is null 
 	*/
 	//what about an exception for a date that's in the future?
 	void addNewPastMeeting(Set<Contact> contacts, Calendar date, String text) {
 		boolean emptyContacts = false;//to allow simultaneous error correction for user
 		boolean nullContacts = false;
 		boolean nullDate = false;
 		boolean nullText = false;
 		boolean falseContact = false;	
 		String unknownContacts = "The following contacts are not on your contact list: ";	
 		try {
 			if (contacts.isEmpty()) {
 				emptyContacts = true;
 			}
 			Iterator<Contact> iterator = contacts.iterator();
 			Contact contact = null;
 			while (iterator.hasNext()) {
 				contact = iterator.next();				
 				if (!contactList.contains(contact)) {				
 					falseContact = true;
 					unknownContacts = unknownContacts + "\n" + contact.getName();
 				}
 			}
 			if (contacts == null) {
 				nullContacts = true;
 			}
 			if (date == null) {
 				nullDate = true;
 			}
 			if (text == null) {
 				nullText = true;
 			} 
 			if (emptyContacts || falseContact) {
 				throw illegalArgEx;
 			}
 			if (nullContacts || nullDate || nullText) {
 				throw nullPointerEx;
 			}
 			Meeting pastMeeting = new PastMeetingImpl(contacts, date, text);
 			pastMeetings.add(pastMeeting);
 		}
 		catch (IllegalArgumentException ex) {
 			if (emptyContacts) {
 				System.out.println("Error: No contacts specified!");
 			}
 			if (falseContact) {
 				System.out.println("Error: " + unknownContacts);
 			}
 		}
 		catch (NullPointerException nex) {
 			if (nullText) {
 				System.out.println("Error: No meeting notes specified!");
 			}
 			if (nullContacts) {
 				System.out.println("Error: No contacts specified!");
 			}
 			if (nullDate) {
 				System.out.println("Error: No date specified!");
 			}
 		}
 	}
 		
 	
 	/** 
 	* Add notes to a meeting. 
 	* 
 	* This method is used when a future meeting takes place, and is 
 	* then converted to a past meeting (with notes). 
 	* 
 	* It can be also used to add notes to a past meeting at a later date. 
 	* 
 	* @param id the ID of the meeting 
 	* @param text messages to be added about the meeting. 
 	* @throws IllegalArgumentException if the meeting does not exist 
 	* @throws IllegalStateException if the meeting is set for a date in the future 
 	* @throws NullPointerException if the notes are null 
 	*/
 	void addMeetingNotes(int id, String text) {//as this is also used to add notes to an existing past meeting, must check if
 	//meeting is in pastMeetings set first - if it is, then take the route of adding notes to existing meeting, otherwise check
 	//future meetings: if ID not found at this point, it doesn't exist. No exceptions should be thrown if meeting is not found
 	//in pastMeetings - only if it's not found in futureMeetings.
 		Iterator<Meeting> pmIterator = pastMeetings.iterator();
 		Meeting pMeeting = null;
 		boolean pastMeetingFound = false;//to determine whether program should proceed to look through futureMeetings if no matching meeting
 		//is found in pastMeetings.
 		while (pmIterator.hasNext()) {
 			pMeeting = pmIterator.next();
 			if (pMeeting.getId() == id) {
 				PastMeetingImpl pmi = (PastMeetingImpl) pMeeting;
 				pmi.addNotes(text);
 				pastMeetingFound = true;
 				System.out.println("Notes for meeting ID No. " + id + " updated successfully.");
 			}
 			break;
 		}
 		if (!pastMeetingFound) {			
 			boolean containsMeeting = false;
 			boolean futureDate = false;
 			Calendar now = Calendar.getInstance();
 			Meeting meeting = null;//to allow the meeting matching the id to be used throughout the method
 			try {
 				Iterator<Meeting> iterator = futureMeetings.iterator();
 				while (iterator.hasNext()) {
 					meeting = iterator.next();
 					if (meeting.getId() == id) {
 						containsMeeting = true;
 					}
 					break;
 				}
 				System.out.println("Meeting ID: " + meeting.getId());
 				//is being updated.
 				if (meeting.getDate().after(now)) {
 					futureDate = true;
 				}			
 				if (text == null) {
 					throw nullPointerEx;
 				}
 				if (!containsMeeting) {
 					throw illegalArgEx;
 				}
 				if (futureDate) {
 					throw illegalStateEx;
 				}
 				Meeting pastMeeting = new PastMeetingImpl(meeting.getContacts(), meeting.getDate(), text, meeting.getId());
 				pastMeetings.add(pastMeeting);
 				futureMeetings.remove(meeting);			
 			}
 			catch (IllegalArgumentException aEx) {
 				System.out.println("Error: No meeting with that ID exists!");
 			}
 			catch (IllegalStateException sEx) {
 				System.out.println("Error: The meeting with this ID has not taken place yet!");
 			}
 			catch (NullPointerException pEx) {
 				System.out.println("Error: No notes have been specified!");
 			}
 		}
 	}
 	
 	/** 
 	* Create a new contact with the specified name and notes. 
 	*
 	* @param name the name of the contact. 
 	* @param notes notes to be added about the contact.
 	* @throws NullPointerException if the name or the notes are null
 	*/ 
 	void addNewContact(String name, String notes) {
 		try {
 			if (name == null || notes == null) {
 				throw nullPointerEx;
 			}
 			Contact contact = new ContactImpl(name);
 			contact.addNotes(notes);
 			contactList.add(contact);
 		}
 		catch (NullPointerException nex) {
 			System.out.println("Error: Please ensure that BOTH the NAME and NOTES fields are filled in.");
 		}
 	}
 	
 	/** 
 	* Returns a list containing the contacts that correspond to the IDs
 	* 
 	* @param ids an arbitrary number of contact IDs 
 	* @return a list containing the contacts that correspond to the IDs. 
 	* @throws IllegalArgumentException if any of the IDs does not correspond to a real contact 
 	*/
 	Set<Contact> getContacts(int... ids) {
 		Set<Contact> idMatches = new HashSet<Contact>();
 		int id = 0;
 		boolean found;
 		try { 
 			for (int i = 0; i < ids.length; i++) {//boolean needs to be reset to false here for each iteration 
 			//otherwise it will stay true after one id is matched!
 				found = false;
 				id = ids[i];
 				Contact contact = null;
 				Iterator<Contact> iterator = contactList.iterator();
 				while (iterator.hasNext()) {
 					contact = iterator.next();
 					if (contact.getId() == id) {
 						idMatches.add(contact);
 						found = true;
 					}				
 				}
 				if (found == false) {
 					throw illegalArgEx;
 				}								
 			}
 			return idMatches;
 		}
 		catch (IllegalArgumentException ex) {
 			System.out.println("Error: ID " + id + " not found!");
 		}
 		return null;
 	}
 		 
 	
 	
 					
 		
 				
 				
 			
 			
 				
 			
 		
 		
 	
 	
 	public static void main(String[] args) {
 	
 		ContactManagerImpl cm = new ContactManagerImpl();
 		cm.launch();
 		
 	}
 	
 	private void launch() {
 		Contact tseng = new ContactImpl("Tseng");
 		Contact reno = new ContactImpl("Reno");
 		Contact rude = new ContactImpl("Rude");
 		Contact elena = new ContactImpl("Elena");
 		Contact r2d2 = new ContactImpl("R2D2");
 		
 		contactList.add(tseng);
 		contactList.add(reno);
 		contactList.add(rude);
 		contactList.add(elena);
 		contactList.add(r2d2);
 				
 		attendeeList.add(tseng);
 		attendeeList.add(rude);
 		attendeeList.add(elena);
 		attendeeList.add(r2d2);
 		
 		Calendar cal = new GregorianCalendar(2013, 0, 2);
 		/**
 		addNewPastMeeting(attendeeList, cal, "First Test Notes");
 		addMeetingNotes(1, "Test notes");
 		PastMeeting pm = getPastMeeting(1);
 		System.out.println("ID: " + pm.getId() + " " + pm.getNotes());
 		
 		
 		Meeting testMeeting = new FutureMeetingImpl(attendeeList, cal);
 		futureMeetings.add(testMeeting);
 		addMeetingNotes(1, "Notes for the meeting that took place today.");
 		PastMeeting pm = getPastMeeting(1);
 		System.out.println(pm);
 		System.out.println("ID: " + pm.getId() + " " + pm.getNotes());
 		*/
 		
		Set<Contact> contactsTestSet = getContacts(1, 10);
 		
 		/**
 		Calendar cal2 = new GregorianCalendar(2013, 6, 5);
 		Calendar cal3 = new GregorianCalendar(2013, 6, 5);		
 		Calendar cal4 = new GregorianCalendar(2013, 1, 12);
 		
 		//Meeting testMeet = new FutureMeetingImpl(attendeeList, cal);
 		//this.addFutureMeeting(attendeeList, cal);
 		//this.addFutureMeeting(attendeeList, cal2);
 		//this.addFutureMeeting(attendeeList, cal3);
 		//attendeeList.remove(tseng);
 		//this.addFutureMeeting(attendeeList, cal4);
 		
 		
 		Calendar calPrint = new GregorianCalendar();
 		/**
 		List<Meeting> testList = getFutureMeetingList(tseng);
 		for (int i = 0; i < testList.size(); i++) {
 			System.out.println("Meeting ID: " + testList.get(i).getId());
 			calPrint = testList.get(i).getDate();
 			System.out.println(calPrint.get(Calendar.DAY_OF_MONTH) + "." + calPrint.get(Calendar.MONTH) + "." + calPrint.get(Calendar.YEAR));
 		}
 		
 		testList = getFutureMeetingList(cal2);
 		for (int i = 0; i < testList.size(); i++) {
 			System.out.println("Meeting ID: " + testList.get(i).getId() + " taking place on: ");
 			calPrint = testList.get(i).getDate();
 			System.out.println(calPrint.get(Calendar.DAY_OF_MONTH) + "." + calPrint.get(Calendar.MONTH) + "." + calPrint.get(Calendar.YEAR));
 		}
 		
 		
 		addNewPastMeeting(attendeeList, cal, "Test");
 		List<PastMeeting> testList = getPastMeetingList(r2d2);
 		for (int i = 0; i < testList.size(); i++) {
 			System.out.println("Meeting ID: " + testList.get(i).getId());
 			calPrint = testList.get(i).getDate();
 			System.out.println(calPrint.get(Calendar.DAY_OF_MONTH) + "." + calPrint.get(Calendar.MONTH) + "." + calPrint.get(Calendar.YEAR));
 		}
 		*/
 		
 		
 		
 		
 		
 	
 		
 	
 	
 	
 	}
 	
 
 
 
 }
 
 // Meeting meeting = new FutureMeeting(params);
 //ask user for dates in specific format, which can then be converted to create a new Calendar
 //make sure that if wrong format is entered, you throw an exception.
 
 //Don't forget to ensure class implements ContactManager when finished
 
 
 /** 
 	* Returns the list of meetings that are scheduled for, or that took 
 	* place on, the specified date 
 	* 
 	* If there are none, the returned list will be empty. Otherwise, 
 	* the list will be chronologically sorted and will not contain any 
 	* duplicates. 
 	* 	
 	* @param date the date 
 	* @return the list of meetings 
 	*/
 	//List<Meeting> getFutureMeetingList(Calendar date);
 	//should this be renamed, as it fetches any meeting, not just future ones?
 	
 	//if returned list is empty, write empty? in main: if list.isEmpty(), print <empty> for
 	//user clarity
 	
 	//when users specify a date, should they also include a time?
 			
 //how does before/after affect dates which are the same?
 //contains -- may have to do this in more detail with an iterator, as the naming of 
 //Contact variables may not allow for contactList.contains(contact); new contacts will probably
 //be just called contact each time they are created, with their name and id the only things to 
 //identify them by.
 
 //initialise notes variable as null in launch so that if user enters nothing, relevant
 //method is still found
 
 //for-each loops for clarity
 
 
 
 
 
 
 
 
 
 
 
 
 
 

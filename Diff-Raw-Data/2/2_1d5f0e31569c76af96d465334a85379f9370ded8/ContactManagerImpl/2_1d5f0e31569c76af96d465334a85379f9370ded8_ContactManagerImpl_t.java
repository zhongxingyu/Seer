 /**
 *The purpose of this assignment it writing a program to keep track of contacts and 
 *meetings. The application will keep track of contacts, past and future meetings, etc. 
 *When the application is closed, all data must be stored in a text file called 
 *”contacts.txt”. This file must be read at startup to recover all data introduced in a 
 *former session.
 */
 
 import java.util.*;
 import java.io.*;
 
 
 public class ContactManagerImpl implements ContactManager { 
 	private IllegalArgumentException illegalArgEx = new IllegalArgumentException();
 	private NullPointerException nullPointerEx = new NullPointerException();
 	private IllegalStateException illegalStateEx = new IllegalStateException();
 	private Set<Contact> contactList = new HashSet<Contact>(); //contacts added to this via addContact()
 	private Set<Contact> attendeeList = new HashSet<Contact>(); //contacts attending a specific meeting; may be removed to be replaced with more temporary set in main method
 	private Set<Meeting> pastMeetings = new HashSet<Meeting>();//list of past meetings
 	private Set<Meeting> futureMeetings = new HashSet<Meeting>();
 	
 
 	
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
 			return null;
 			//confirm returning null is best course of action
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
 			return null;
 			//what action to take? - safest is to go back to main menu
 		}
 		Iterator<Meeting> iteratorFM = futureMeetings.iterator();
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
 		Iterator<Meeting> iteratorPM = pastMeetings.iterator();
 		Meeting meeting = null;
 		while(iteratorPM.hasNext()) {
 			meeting = iteratorPM.next();
 			if (meeting.getId() == id) {
 				return meeting;
 			}
 		}
 		Iterator<Meeting> iteratorFM = futureMeetings.iterator();
 		meeting = null;		
 		while (iteratorFM.hasNext()) {
 			meeting = iteratorFM.next();
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
 	public List<Meeting> sort(List<Meeting> list) {
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
 	public List<Meeting> getMeetingList(Calendar date) {
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
 	* @return the list of past meeting(s) scheduled with this contact (maybe empty). 
 	* @throws IllegalArgumentException if the contact does not exist
 	*/ 
 	public List<PastMeeting> getPastMeetingList(Contact contact) {
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
 		return null;//or return an empty list?
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
 	public void addNewPastMeeting(Set<Contact> contacts, Calendar date, String text) {
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
 	public void addMeetingNotes(int id, String text) {
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
 	public void addNewContact(String name, String notes) {
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
 	public Set<Contact> getContacts(int... ids) {
 		Set<Contact> idMatches = new HashSet<Contact>();
 		int id = 0;
 		String idString = "";//to facilitate an error message that lists all invalid IDs
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
 					idString = idString + id + "\n";
 					//throw illegalArgEx;
 				}		
 			}
 			if (idString.length() > 0) {
 					throw illegalArgEx;	
 			}						
 			return idMatches;
 		}
 		catch (IllegalArgumentException ex) {
 			System.out.println("Note: The following IDs were not found and haven't " +
 				"been added to the attendee list:" + "\n" + idString);
 			//user's next option? Return to main?
 		}
 		return idMatches;
 	}
 	
 	/*
 	* Returns a single contact matching an ID.
 	*/
 	public Contact getContact(int id) {
 		for (Contact c : contactList) {//matches against list of contacts
 			if (c.getId() == id) {
 				return c;
 			}
 		}
 		System.out.println("ID not found!");
 		return null;
 	}
 		
 	
 	/** 
 	* Returns a list with the contacts whose name contains that string. 
 	* 
 	* @param name the string to search for 
 	* @return a list with the contacts whose name contains that string. 
 	* @throws NullPointerException if the parameter is null 
 	*/
 	public Set<Contact> getContacts(String name) {
 		Set<Contact> contactSet =  new HashSet<Contact>();
 		Contact contact = null;
 		try {
 			if (name == null) {
 				throw nullPointerEx;
 			}
 			Iterator<Contact> iterator = contactList.iterator();
 			while (iterator.hasNext()) {
 				contact = iterator.next();
 				if (contact.getName() == name) {
 					contactSet.add(contact);
 				}
 			}	
 		}
 		catch (NullPointerException nex) {
 			System.out.println("Error: Please ensure that you enter a name.");
 			System.out.println("Contact name: ");
 			String name2 = System.console().readLine();
 			if (name2.equals("back")) {
 				return null;//allow user to exit rather than get stuck
 			}
 			return getContacts(name2);
 		}
 		return contactSet;
 	}
 	//if nothing is found, say so -> do this in launch
 	
 	
 	/** 
 	* Save all data to disk. 
 	* 
 	* This method must be executed when the program is 
 	* closed and when/if the user requests it. 
 	*/
 	public void flush() {
 		IdStore ids = new IdStoreImpl();
 		ids.saveContactIdAssigner(ContactImpl.getIdAssigner());
 		ids.saveMeetingIdAssigner(MeetingImpl.getIdAssigner());		
 		try {
  			FileOutputStream fos = new FileOutputStream("contacts.txt");
  			System.out.println("Saving data...");
       		ObjectOutputStream oos = new ObjectOutputStream(fos);      	
       		oos.writeObject(ids);//saves IdStore object containing idAssigners
       		Iterator<Contact> contactIterator = contactList.iterator();
       		while (contactIterator.hasNext()) {//write contents of contactList to file
       			Contact c = contactIterator.next();
       			oos.writeObject(c);
       		}
       		Iterator<Meeting> iteratorPM = pastMeetings.iterator();
       		while (iteratorPM.hasNext()) {//writes contents of pastMeetings to file
       			Meeting m = iteratorPM.next();
       			oos.writeObject(m);
       		}
       		Iterator<Meeting> iteratorFM = futureMeetings.iterator();
       		while (iteratorFM.hasNext()) {//writes contents of futureMeetings to file
       			Meeting m = iteratorFM.next();
       			oos.writeObject(m);
       		}
       		oos.close();
       		System.out.println("Saved.");
       	}
       	catch (FileNotFoundException ex) {
       		System.out.println("Creating contacts.txt file for data storage...");
       		File contactsTxt = new File("./contacts.txt");
       		flush();
       	}
       	catch (IOException ex) {
       		ex.printStackTrace();//need to be more explicit?
       	} 	
 	}
 	
 	//Loads data from file upon opening program
 	public void loadData() {
 		System.out.println("Loading data from file...");
 		try {
 			File contactsFile = new File("./contacts.txt");
 			if (contactsFile.length() == 0) {
 				System.out.println("No saved data found.");
 				return;
 			}
 			FileInputStream fis = new FileInputStream("contacts.txt");
 			ObjectInputStream ois = new ObjectInputStream(fis);
 			Object obj = null;
 			while ((obj = ois.readObject()) != null) { //read to end of file
 				if (obj instanceof IdStore) {
 					IdStore ids = (IdStoreImpl) obj;
 					ContactImpl.restoreIdAssigner(ids.getContactIdAssigner());
 					MeetingImpl.restoreIdAssigner(ids.getMeetingIdAssigner());
 				}
 				if (obj instanceof Contact) {
 					Contact contact = (ContactImpl) obj;
 					contactList.add(contact);
 				}
 				if (obj instanceof FutureMeeting) {
 					Meeting meeting = (FutureMeetingImpl) obj;
 					futureMeetings.add(meeting);
 				}
 				if (obj instanceof PastMeeting) {
 					Meeting meeting = (PastMeetingImpl) obj;
 					pastMeetings.add(meeting);
 				}
 			}
 			ois.close();
 		}
 		catch (EOFException ex) {
 			System.out.println("Data from previous session loaded.");
 		}
 		catch (FileNotFoundException ex) {
       		System.out.println("File not found! Please ensure contacts.txt is in the same directory.");
       	}
 		catch (IOException ex) {
 			ex.printStackTrace();
 		}
 		catch (ClassNotFoundException ex) {
 			ex.printStackTrace();
 		}		
 	}			
 			
 		
 		
 	
 	
 	public static void main(String[] args) {
 	
 		ContactManagerImpl cm = new ContactManagerImpl();
 		cm.launch();
 		
 	}
 	
 	private void launch() {
 	
 		ContactManagerUtilities.displayWelcome();
 		loadData();
 		
 		boolean finished = false;
 		
 		while (!finished) {		
 			int userSelection = ContactManagerUtilities.chooseMainMenuOption();
 			switch (userSelection) {
 				
 				case 1: System.out.println("\n");
 						System.out.println("*** ADD A FUTURE MEETING");
 						int[] attendeeArray = ContactManagerUtilities.selectAttendees(contactList);						
 						if (attendeeArray == null) {//occurs if user opts to quit, or if contactList is empty
 							break;
 						}
 						Set<Contact> attendees = getContacts(attendeeArray);
 						Calendar date = ContactManagerUtilities.createDate();
 						if (date == null) {
 							break;
 						}
 						this.addFutureMeeting(attendees, date);
 						break; 
 						
 				case 2: System.out.println("\n");
 						System.out.println("*** LOOK UP A MEETING");
 						int userChoice = ContactManagerUtilities.lookUpMeetingOptions();
 						switch (userChoice) {
 							case 1: System.out.println("*** LOOK UP MEETING -- Search by Date");
 									System.out.println("Please enter a date: ");
 									date = ContactManagerUtilities.createDate();
 									if (date == null) {
 										break;//go back to main menu, TEST THIS
 									}
 									List<Meeting> foundMeetings = getMeetingList(date);
 									ContactManagerUtilities.printMeetingList(foundMeetings);
 									break;
 									
 							case 2: System.out.println("*** LOOK UP MEETING -- Search by Meeting ID");
 									System.out.println("Please enter a meeting ID: ");
 									String entry = System.console().readLine();
 									if (entry.equals("back")) {
 										break;//go back to main menu
 									}
 									int id = ContactManagerUtilities.validateNumber(entry);								
 									Meeting meeting = getMeeting(id);
 									if (meeting != null) {
										ContactManagerUtilities.printMeetingDetails(meeting);
 										break;//go back to main menu
 									}
 									else {
 										System.out.println("No meetings matching that date found!");
 										break;//go back to main menu
 									}
 									break;
 							
 							case 3: System.out.println("*** LOOK UP MEETING -- Search Future Meetings by Contact");
 									int userSubChoice = ContactManagerUtilities.searchByContactOptions();
 									
 									switch (userSubChoice) {
 										case 1: System.out.println("Please enter a contact's ID:");
 												entry = System.console().readLine();
 												if (entry.equals("back")) {
 													break;//go back to main menu
 												}
 												id = ContactManagerUtilities.validateNumber(entry);
 												Contact contact = getContact(id);
 												if (contact == null) {
 													break;//go back to main menu
 												}											
 												List<Meeting> fMeetings = getFutureMeetingList(contact);
 												if (fMeetings.isEmpty()) {
 													System.out.println("No meetings found.");
 													break;//go back to main menu
 												}
 												ContactManagerUtilities.printMeetingList(fMeetings);//print details of meetings
 												break; 
 												
 										case 2: System.out.println("Please enter a contact's name:");
 												entry = System.console().readLine();
 												Set<Contact> contacts = getContacts(entry);
 												if (contacts.isEmpty()) {
 													System.out.println("No contacts found.");
 													break;
 												}
 												System.out.println("Contacts matching this name: ");
 												for (Contact c : contacts) {
 													System.out.println(c.getName() + "\t" + "ID: " + c.getId());
 												}
 												System.out.println("Enter the ID of the contact you wish to select: ");
 												entry = System.console().readLine();
 												id = ContactManagerUtilities.validateNumber(entry);
 												contact = getContact(id);
 												if (contact == null) {
 													break;//go back to main menu
 												}																			
 												fMeetings = getFutureMeetingList(contact);
 												if (fMeetings.isEmpty()) {
 													System.out.println("No meetings found.");
 													break;//go back to main menu
 												}
 												ContactManagerUtilities.printMeetingList(fMeetings);//print details of meetings
 												break; 								
 												
 										
 										case 3: break;									
 									}
 									break;
 									
 									
 							
 							case 4: System.out.println("*** LOOK UP MEETING -- Search Past Meetings by Contact");
 									userSubChoice = ContactManagerUtilities.searchByContactOptions();
 									
 									switch (userSubChoice) {
 										case 1: System.out.println("Please enter a contact's ID:");
 												entry = System.console().readLine();
 												if (entry.equals("back")) {
 													break;//go back to main menu
 												}
 												id = ContactManagerUtilities.validateNumber(entry);
 												Contact contact = getContact(id);
 												if (contact == null) {
 													break;//go back to main menu
 												}											
 												List<PastMeeting> pMeetings = getPastMeetingList(contact);
 												if (pMeetings.isEmpty()) {
 													System.out.println("No meetings found.");
 													break;//go back to main menu
 												}
 												ContactManagerUtilities.printMeetingList(pMeetings);//print details of meetings
 												break; 
 												
 										case 2: System.out.println("Please enter a contact's name:");
 												entry = System.console().readLine();
 												Set<Contact> contacts = getContacts(entry);
 												if (contacts.isEmpty()) {
 													System.out.println("No contacts found.");
 													break;
 												}
 												System.out.println("Contacts matching this name: ");
 												for (Contact c : contacts) {
 													System.out.println(c.getName() + "\t" + "ID: " + c.getId());
 												}
 												System.out.println("Enter the ID of the contact you wish to select: ");
 												entry = System.console().readLine();
 												id = ContactManagerUtilities.validateNumber(entry);
 												contact = getContact(id);	
 												if (contact == null) {
 													break;//go back to main menu
 												}																		
 												pMeetings = getPastMeetingList(contact);
 												if (pMeetings.isEmpty()) {
 													System.out.println("No meetings found.");
 													break;//go back to main menu
 												}
 												ContactManagerUtilities.printMeetingList(pMeetings);//print details of meetings
 												break; 													
 										
 										case 3: break;
 									}
 									break;
 									
 							//LOOK UP MEETING MENU
 							case 5: System.out.println("*** LOOK UP MEETING -- Search Past Meetings by ID");
 								    id = ContactManagerUtilities.validateNumber(entry);
 								    PastMeeting pastMeeting = this.getPastMeeting(id);
 								    if (pastMeeting == null) {
 								    	break;//return to main
 								    }
 								    ContactManagerUtilities.printMeetingDetails(pastMeeting);
 								    break;
 												
 							//LOOK UP MEETING MENU
 							case 6: System.out.println("*** LOOK UP MEETING -- Search Future Meetings by ID");
 								    id = ContactManagerUtilities.validateNumber(entry);
 									FutureMeeting futureMeeting = getFutureMeeting(id);
 								    if (futureMeeting == null) {
 								    	break;//return to main
 								    }
 								    ContactManagerUtilities.printMeetingDetails(futureMeeting);
 								    break;
 							
 							
 							//LOOK UP MEETING MENU
 							case 7: break;
 						
 						}
 						break;
 						
 				case 3: //create record of past meeting
 				
 				case 4: //add notes to a meeting that has taken place			
 
 				
 				case 5: System.out.println("\n");
 						System.out.println("*** ADD NEW CONTACT");
 						
 				case 6: //look up contact
 				
 				case 7: flush();
 						break;
 						
 				case 8: flush();
 						finished = true;
 						System.out.println("\n" + "Closing...");
 						break;
 			}
 		}
 
 
 //after an option is selected, the option should be followed through, and then the main
 //menu should be displayed again. The only times this doesn't happen is when the user
 //opts to save and quit: after data has been saved, the program exits. 
 //for each option that is selected, give the user a chance to return to main menu by 
 //typing 0 -- an if clause that says if entry is 0, display main menu. For this reason,
 //perhaps put main menu method into this class (whist keeping checking in util)...
 //put the whole thing inside a while loop? Then, when save and quit is called, carry out
 //the action and break from the loop.
 //to go back to main menu -- if something is null? Or enter 0
 
 //when a user has to enter something, it'll most likely be read initially as a String...
 //so if the user enters 'back' or 'quit', return to main menu.
 		
 		
 	
 		
 		
 		
 		
 		
 	
 		
 	
 	
 	
 	
 	
 
 
 
 
 
 }
 
 }
 
 //ask user for dates in specific format, which can then be converted to create a new Calendar
 //make sure that if wrong format is entered, you throw an exception.
 
 //update dates to include time?
 
 
 
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
 
 
 
 
 //when saved, contents of file will be overwritten - alert user of this
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 

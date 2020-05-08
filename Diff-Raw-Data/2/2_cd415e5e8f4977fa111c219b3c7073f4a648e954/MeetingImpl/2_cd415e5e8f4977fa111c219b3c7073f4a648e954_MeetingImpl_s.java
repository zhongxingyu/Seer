 import java.util.Calendar;
 import java.util.Set;
 import java.io.Serializable;
 /**
 * A class to represent meetings
 *
 * Meetings have unique IDs, scheduled date and a list of participating contacts
 */
 public abstract class MeetingImpl implements Meeting, Serializable {
     private static int idStatic = 0;//Prevents more than one meeting having the same id
     private int id;
     private Calendar date;
     private Set<Contact> contacts;
 	
     /**
     * Constructor with no parameters to make class serialisable
     */
     public MeetingImpl() {}
     /**
    * Constructor for MeetImpl
     *
     * @param contacts to be added to the meeting
     * @param date of meeting to be held
     */
     public MeetingImpl(Set<Contact> contacts, Calendar date) {
         id = idStatic++;
         this.date = date;
         this.contacts = contacts;
     }
     /**
     * Constructor to allow ID as argument
     * 
     * Only for PastMeetingImpl. Used by the ContactManager.addMeetingNotes() method to convert to PastMeeting type and keep same id
     *
     * @param id for meeting to be able to keep same ID when converted from FutureMeeting type to PastMeeting type
     * @param contacts to be added to the meeting
     * @param date of meeting to be held
     */
     public MeetingImpl(int id, Set<Contact> contacts, Calendar date) {
         this.id = id;
         this.date = date;
         this.contacts = contacts;
     }
     /**
     * Returns the id of the meeting.
     *
     * @return the id of the meeting.
     */
     public int getId() {
         return id;
     }
     /**
     * Return the date of the meeting.
     *
     * @return the date of the meeting.
     */
     public Calendar getDate() {
         return date;
     }
     /**
     * Return the details of people that attended the meeting.
     *
     * The list contains a minimum of one contact (if there were
     * just two people: the user and the contact) and may contain an
     * arbitraty number of them.
     *
     * @return the details of people that attended the meeting.
     */
     public Set<Contact> getContacts() {
         return contacts;
     }
 }

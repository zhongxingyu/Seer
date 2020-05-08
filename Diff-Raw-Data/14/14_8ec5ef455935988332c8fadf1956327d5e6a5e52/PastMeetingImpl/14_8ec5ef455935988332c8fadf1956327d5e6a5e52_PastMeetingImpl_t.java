 import java.util.Calendar;
 import java.util.Set;
 
public class PastMeetingImpl extends MeetingImpl implements PastMeeting{
 	
 	private String notes;
 	
 	public PastMeetingImpl(Calendar date,Set<Contact> contacts,String notes) {
 		super(date, contacts);
 		this.notes = notes;
 	}
 	
 	public PastMeetingImpl(int ID, Calendar date,Set<Contact> contacts,String notes) {
 		super(ID, date, contacts);
 		this.notes = notes;
 	}
 	
 	
 	public PastMeetingImpl(FutureMeeting meeting,String notes){
 		super(meeting.getId(), meeting.getDate(),meeting.getContacts());
 		this.notes=notes;
 	}
 
 	public String getNotes() {
 		return notes;
 	}
 
 
 
 }

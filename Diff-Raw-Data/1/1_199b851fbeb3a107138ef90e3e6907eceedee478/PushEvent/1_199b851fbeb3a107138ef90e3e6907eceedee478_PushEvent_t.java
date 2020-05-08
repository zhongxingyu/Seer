 package il.ac.huji.app4beer.DAL;
 
 import il.ac.huji.app4beer.EventManager;
 import il.ac.huji.app4beer.DAL.Contact.Attending;
 import il.ac.huji.app4beer.DAL.ParseProxy.PushType;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import com.parse.ParseUser;
 
 public class PushEvent {
 	
 	public class Attendance {
 		private String contact;
 		private String event;
 		private int att;
 		public int getAtt() {
 			return att;
 		}
 		public void setAtt(int att) {
 			this.att = att;
 		}
 		public String getEvent() {
 			return event;
 		}
 		public void setEvent(String event) {
 			this.event = event;
 		}
 		public String getContact() {
 			return contact;
 		}
 		public void setContact(String contact) {
 			this.contact = contact;
 		}
 	}
 
 	private String _title;
 	private String _description;
 	private String _location;
 	private Date _date;
 	private Contact _owner;
 	private HashSet<String> _contacts;
 	private transient Event _event;
 	private transient HashSet<Contact> _members; 
 	
 	public PushEvent(Event event) {
 		_event = event;
 		_title = event.get_title();
 		_description = event.get_description();
 		_location = event.get_location();
 		_date = event.get_date();
 		_owner = event.get_owner();
 		_contacts = new HashSet<String>();
 		_members = new HashSet<Contact>();
 		Iterator<Integer> contactsIt = event.get_contacts().iterator();
 		while (contactsIt.hasNext()) {
 			_contacts.add(DAL.Instance().readContact(contactsIt.next()).get_name());
 		}
 		Iterator<Integer> groupsIt = event.get_groups().iterator();
 		while (groupsIt.hasNext()) {
 			List<Contact> members = DAL.Instance().Members(groupsIt.next());
			if (members==null) continue;
 			Iterator<Contact> membersIt = members.iterator();
 			while (membersIt.hasNext()) {
 				Contact member = membersIt.next();
 				member.set_attending(Contact.Attending.SO);
 				member.set_source(Contact.Source.GROUP);
 				Boolean addIt = _contacts.add(member.get_name());
 				if (addIt) {
 					_members.add(member);
 				}
 			}					
 		}
 	}
 
 	public void persist() throws Exception {
 		if (_event == null) {
 			_event = new Event(-1, _title, _description, _location, _date);
 			_event.set_owner(DAL.Instance().readContact(_owner.get_id()));
 			Iterator<String> i = _contacts.iterator();
 			while (i.hasNext()) {
 				_event.add_contact(DAL.Instance().readContact(i.next()).get_id());
 			}
 		}
 		long eventId = DAL.Instance().insertEvent(_event);
 		_event.set_id((int)eventId);
 		DAL.Instance().updateParticipant(_owner, _event);
 		Iterator<Contact> membersIt = _members.iterator();
 		while (membersIt.hasNext()) {
 			try {
 				DAL.Instance().insertParticipant(membersIt.next(), _event);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	
 	
 	public void push() {
 		push(ParseProxy.PushType.NewEvent, this, ParseUser.getCurrentUser().getUsername()+" created "+_event.get_title());
 	}
 
 	public void push(PushType type, Object what, String alert) {
 		Iterator<String> i = _contacts.iterator();
 		String from = ParseUser.getCurrentUser().getUsername();
 		while (i.hasNext()) {
 			String to = i.next();
 			if (from.compareTo(to)==0) continue;
 			ParseProxy.Push(to, type, what, alert);
 		}
 	}
 
 	public void updateAttendance(String what) {
 		Attendance msg = new Attendance();
 		msg.setContact(ParseUser.getCurrentUser().getUsername());
 		msg.setEvent(_event.get_title());
 		if (what.equals(EventManager.OF_COURSE)) msg.setAtt(Attending.YES);
 		if (what.equals(EventManager.MAYBE)) msg.setAtt(Attending.MAYBE);
 		if (what.equals(EventManager.NOT_COMING)) msg.setAtt(Attending.NO);
 		push(ParseProxy.PushType.UpdateAttendance, msg, msg.getContact()+" says: "+msg.getEvent()+"? "+what);
 	}
 	
 }

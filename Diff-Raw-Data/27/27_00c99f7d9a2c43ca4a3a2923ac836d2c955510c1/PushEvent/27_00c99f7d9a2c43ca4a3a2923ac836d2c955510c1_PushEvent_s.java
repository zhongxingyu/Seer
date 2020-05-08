 package il.ac.huji.app4beer.DAL;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import com.parse.ParseUser;
 
 public class PushEvent {
 	
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
 		Iterator<String> i = _contacts.iterator();
 		String from = ParseUser.getCurrentUser().getUsername();
 		while (i.hasNext()) {
 			String to = i.next();
			if (from!=to) {
				ParseProxy.Push(to, ParseProxy.PushType.NewEvent, this, from+" created "+_event.get_title());
			}
 		}
 	}
 }

 package il.ac.huji.app4beer.DAL;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.preference.PreferenceManager;
 //import android.database.sqlite.SQLiteDatabase;
 
 import com.parse.LogInCallback;
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseInstallation;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 import com.parse.PushService;
 import com.parse.SignUpCallback;
 
 public class DAL {
 	
 	SqlLiteHelper _sqlLiteHelper;
 	private SQLiteDatabase _db;
 	private SharedPreferences _preferences;
 
 	private static DAL _instance = null;
 	
 	public static void Init(Context context) {
 		_instance = new DAL(context);
 	}
 	
 	public static DAL Instance() {
 		return _instance;
 	}
 	
 	private DAL(Context context) 
 	{  
 		_preferences = PreferenceManager.getDefaultSharedPreferences(context);
 	    _sqlLiteHelper = new SqlLiteHelper(context);
 	    _db = _sqlLiteHelper.getWritableDatabase();
 	    ParseProxy.Init(context);
 	    refreshContacts();
 	}
 	
 	public Boolean IsSignedIn() {
 		String displayname = _preferences.getString("displayname", null);
 		if (displayname == null) return false;
 		String phonenumber = _preferences.getString("phonenumber", null);
 		try {
 			ParseUser user = ParseUser.logIn(displayname, phonenumber);
 			return user != null;
 		} catch (ParseException e) {
 			return false; 
 		} 
 	}
 	
 	public void SaveCredentials(String phonenumber, String displayname) {
 		SharedPreferences.Editor editor = _preferences.edit();
 		editor.putString("phonenumber", phonenumber);
 		editor.putString("displayname", displayname);
 		editor.commit();
 	}
 	
 	public Event readEvent(int id) {
 		List<Event> events = Events("_id='"+id+"'");
 		return events.size()==0?null:events.get(0);
 	}
 
 	public List<Event> Events() {
 		return Events(null);
 	}
 		
 	private List<Event> Events(String selection) {
 		List<Event> events = new ArrayList<Event>();
 		 Cursor cursor = _db.query("events", new String[] { "name", "description", "date", "_id", "location" }, selection, null, null, null, null);
 		 if (cursor.moveToFirst()) {
 			 do {
 			    String name = cursor.getString(0);
 			    String description= cursor.getString(1);
 			    Date date = cursor.getLong(2)==0 ? null : new Date(cursor.getLong(2));
 			    Integer id = cursor.getInt(3);
 			    String location = cursor.getString(4);
 			    Event event = new Event(id, name, description, location, date);
 			    addEventParticipants(event);
 			    addEventGroups(event);
 			    events.add(event);
 			 } while (cursor.moveToNext());
 		 }
 		 return events;  
 	 }
 	 
 	private void addEventParticipants(Event event) {
 		 Cursor cursor = _db.query("participants", new String[] { "contactid" }, 
 				 "eventid = '"+event.get_id()+"'",
 				 null, null, null, null);
 		 if (cursor.moveToFirst()) {
 			 do {
 			    int contactid = cursor.getInt(0);
 			    event.add_contact(contactid);
 			 } while (cursor.moveToNext());
 		 }
 	}
 
 	public ArrayList<Contact> Participants(Integer eventId, Integer attendingType) {
 		return Participants(eventId, attendingType, null);
 	}
 	
 	public ArrayList<Contact> Participants(Integer eventId, Integer attendingType, Integer contactId) {
 		ArrayList<Contact> contacts = new ArrayList<Contact>();
 		String QUERY = "SELECT contacts.name, contacts.phone, contacts._id, participants.source, participants.attending FROM participants INNER JOIN contacts ON contacts._id=participants.contactid WHERE participants.eventid=?";
 		String[] strings;
 		if (contactId==null) {
 			QUERY = QUERY.concat(" AND participants.attending=?");
 			strings = new String[]{
 						eventId.toString(), 
 						attendingType.toString()};
 		} else {
 			QUERY = QUERY.concat(" AND contacts._id=?");
 			strings = new String[]{
 					eventId.toString(), 
 					contactId.toString()};
 		}
 		Cursor cursor = _db.rawQuery(QUERY, strings);
 		 if (cursor.moveToFirst()) {
 			 do {
 			    String name= cursor.getString(0);
 			    String phone= cursor.getString(1);
 			    int id = cursor.getInt(2);
 			    int source = cursor.getInt(3);
 			    int attending = cursor.getInt(4);
 			    Contact contact = new Contact(name, phone, id);
 			    contact.set_source(source);
 			    contact.set_attending(attending);
 			    contacts.add(contact);
 			 } while (cursor.moveToNext());
 		 }
 		 return contacts;
 	}
 	
 
 	public void updateParticipant(Contact contact, Event event) throws Exception {
 		ContentValues content = createParticipantRow(contact, event);
 	    int rows = _db.update(
 	    		"participants", 
 	    		content,
 	    		"contactid=? AND eventid=?",
 	    		new String[] { contact.get_id().toString(), event.get_id().toString() });
 	    if (rows==0) {
 	    	insertParticipant(contact, event);
 	    }
 	}
 	
 	public long insertParticipant(Contact contact, Event event) throws Exception {
 		if (readContact(contact.get_name())==null) {
 			insertContact(contact);
 		}
 		if (!Participants(event.get_id(), 0, contact.get_id()).isEmpty()) {
 			return -1;
 		}
 		ContentValues content = createParticipantRow(contact, event);
 	    long id = _db.insert("participants", null, content) ;
 		if (id == -1) throw new Exception();
 		return id;
 	}
 
 	private ContentValues createParticipantRow(Contact contact, Event event) {
 		ContentValues content = new ContentValues();
 		content.put("contactid", contact.get_id());
 		content.put("attending", contact.get_attending());
 		content.put("source", contact.get_source());
 		content.put("eventid", event.get_id());
 		return content;
 	}
 
 	private void addEventGroups(Event event) {
 		 Cursor cursor = _db.query("eventgroups", new String[] { "groupid" }, 
 				 "eventid = '"+event.get_id()+"'",
 				 null, null, null, null);
 		 if (cursor.moveToFirst()) {
 			 do {
 			    int id = cursor.getInt(0);
 			    event.add_group(id);
 			 } while (cursor.moveToNext());
 		 }
 	}
 
 	public long insertEvent(Event event) throws Exception {
 		_db.beginTransaction();
 		long id;
         try {
         	
 	        ContentValues content = new ContentValues();
 			content.put("name", event.get_title());
 			content.put("description", event.get_description());
 			content.put("location", event.get_location());
 	    	content.put("date", event.get_date().getTime());
 		    id = _db.insert("events", null, content) ;
 		    if (id  == -1) throw new Exception();
 		    
		    event.set_id((int)id);
		    
 		    List<Integer> groups = event.groups();
 		    if (groups!=null) {
 			    for (int i=0;i<groups.size();i++) {
 					content = new ContentValues();
 					content.put("eventid", event.get_id());
 					content.put("groupid", groups.get(i));
 				    long status = _db.insert("eventgroups", null, content) ;
 				    if (status == -1) throw new Exception();
 			    }
 		    }
 		    
 		    List<Integer> contacts = event.contacts();
 		    if (contacts!=null) {
 		    	Iterator<Integer> i = contacts.iterator();
 		    	while (i.hasNext()) {
 			    	Contact contact = readContact(i.next());
 			    	contact.set_attending(Contact.Attending.SO);
 			    	contact.set_source(Contact.Source.CONTACTS);
 			    	insertParticipant(contact, event);
 			    }
 		    }
 		    
 		    _db.setTransactionSuccessful();
 		    
         }finally {
         	_db.endTransaction();
         }
 	    return id;
 
 /*		    ParseObject testObject = new ParseObject("todo");
 		    testObject.put("title", todoItem.getTitle());
 		    if (todoItem.getDueDate()!=null) {
 		    	testObject.put("due", todoItem.getDueDate().getTime());
 		    }
 		    testObject.saveInBackground();
 	*/	     
 	}
 
 	public List<Group> Groups() {
 		List<Group> groups = new ArrayList<Group>();
 		 Cursor cursor = _db.query("groups", new String[] { "_id", "name" }, null, null, null, null, null);
 		 if (cursor.moveToFirst()) {
 			 do {
 				 int id = cursor.getInt(0);
 			    String name = cursor.getString(1);
 			    groups.add(new Group(name, id));
 			 } while (cursor.moveToNext());
 		 }
 		 return groups;  
 	 }
 	 
 	public long insertGroup(Group group) throws Exception {
 		ContentValues content = new ContentValues();
 		content.put("name", group.get_name());
 	    long id = _db.insert("groups", null, content) ;
 		if (id == -1) throw new Exception();
 		return id;
 	}
 
 	public boolean removeGroup(String groupName) {
 	    try {
 	    	// TODO cascade?
 		    long status = _db.delete("groups", "name=?",new String[] { groupName });
 			return status != 0;
 	    } catch (Exception e){
 	    	return false;
 	    }
 	}
 
 	public void refreshContacts() {
 		List<String> users = ParseProxy.Users();
 		Iterator<String> i = users.iterator();
 		while (i.hasNext()) {
 			String user = i.next();
 			try {
 				insertContact(new Contact(user, "",-1));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}			
 		}
 	}
 	
 	public Contact readContact(Integer id) {
 		List<Contact> contacts = Contacts("_id='"+id+"'");
 		return contacts.size()==0?null:contacts.get(0);
 	}
 
 	public Contact readContact(String name) {
 		List<Contact> contacts = Contacts("name='"+name+"'");
 		return contacts.size()==0?null:contacts.get(0);
 	}
 
 	public List<Contact> Contacts() {
 		return Contacts(null);
 	}
 	
 	private List<Contact> Contacts(String selection) {
 		List<Contact> contacts = new ArrayList<Contact>();
 		 Cursor cursor = _db.query("contacts", new String[] { "name", "phone", "_id" }, selection, null, null, null, null);
 		 if (cursor.moveToFirst()) {
 			 do {
 				    String name = cursor.getString(0);
 				    String phone = cursor.getString(1);
 				    int id = cursor.getInt(2);
 				    contacts.add(new Contact(name, phone, id));
 			 } while (cursor.moveToNext());
 		 }
 		return contacts;
 	}
 	
 	public long insertContact(Contact contact) throws Exception {
 		ContentValues content = new ContentValues();
 		content.put("name", contact.get_name());
 		content.put("phone", contact.get_phone());
 	    long id = _db.insert("contacts", null, content) ;
 		if (id == -1) throw new Exception();
 		return id;
 	}
 
 	public long insertMember(Contact contact, Group group) throws Exception {
 		ContentValues content = new ContentValues();
 		content.put("contactId", contact.get_id());
 		content.put("groupId", group.get_id());
 	    long id = _db.insert("members", null, content) ;
 		if (id == -1) throw new Exception();
 		return id;
 	}
 
 	public boolean removeMember(Contact contact, Group group) {
 	    try {
 	    	long status = _db.delete("members", "contactId=? AND groupId=?",
 	    			new String[] { contact.get_id().toString(), group.get_id().toString()});
 			return status != 0;
 	    } catch (Exception e){
 	    	return false;
 	    }
 	}
 	public List<Contact> Members(Group group) {
 		if (group == null) {
 			return new ArrayList<Contact>();
 		}
 		return Members(group.get_id());
 	}
 
 	public List<Contact> Members(Integer groupId) {
 		List<Contact> members = new ArrayList<Contact>();
 		final String QUERY = "SELECT contacts.name, contacts.phone, contacts._id FROM members INNER JOIN contacts ON members.contactId=contacts._id WHERE members.groupId=? ";
 		Cursor cursor = _db.rawQuery(QUERY, new String[]{groupId.toString()});
 		if (cursor.moveToFirst()) {
 			 do {
 				    String name = cursor.getString(0);
 				    String phone = cursor.getString(1);
 				    int id = cursor.getInt(2);
 				    Contact contact = new Contact(name, phone, id);
 				    contact.set_selected(true);
 				    members.add(contact);
 			 } while (cursor.moveToNext());
 		 }
 		return members;
 	}
 
 	/*
 	public boolean update(ITodoItem todoItem) {
 		try {
 		    ContentValues task = createRow(todoItem);
 		    String namePrefix = todoItem.getTitle();
 		    Boolean status = _db.update("tasks", task, "title=?",new String[] { namePrefix }) != 0;
 		    
 		    final ITodoItem todoItemEx = todoItem;
 		    GetCallback cb = new GetCallback() {
 			      public void done(ParseObject obj, ParseException e) {
 				        if (e == null) {
 				        		obj.put("title", todoItemEx.getTitle());
 				        		long due = todoItemEx.getDueDate() == null ? null :
 				        			todoItemEx.getDueDate().getTime();
 				        		obj.put("due", due);
 				        	    obj.saveInBackground();
 				        	}
 				        }
 				    };
 		    
 		    ParseQuery query = new ParseQuery("todo");
 		    query.whereEqualTo("title", todoItem.getTitle());
 		    query.getFirstInBackground(cb);
 
 			return status;
 	    } catch (Exception e){
 	    	return false;
 	    }
 	}
 	 
 	public boolean delete(ITodoItem todoItem) {
 		try {
 		    String namePrefix = todoItem.getTitle();
 		    Boolean status = _db.delete("tasks", "title=?",new String[] { namePrefix }) != 0;
 		   
 		    final ITodoItem todoItemEx = todoItem;
 		    GetCallback cb = new GetCallback() {
 			      public void done(ParseObject obj, ParseException e) {
 				        if (e == null) {
 				        		obj.deleteInBackground();
 				        	}
 				        }
 				    };
 		    
 		    ParseQuery query = new ParseQuery("todo");
 		    query.whereEqualTo("title", todoItem.getTitle());
 		    query.getFirstInBackground(cb);
 
 		    return status; 
 	    } catch (Exception e){
 	    	return false;
 	    }
 	}
 	 
 */	 
 }

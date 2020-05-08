 package xmlhandle;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 
 import connection.Client;
 
 import dbhandle.Event;
 import dbhandle.MessageAction;
 import dbhandle.Status;
 import dbhandle.User;
 
 public class Xmlhandle {
 	
 	private ActionListener listener;
 	private String msg;
 	private String username;
 	
 	public static Document stringToXML(String string) throws DocumentException {
 		return DocumentHelper.parseText(string);
 	}
 	
 	//Receiver methods
 	public void performMessageInstructions(Document xml) throws NumberFormatException, ParseException, SQLException{
 		
 		//Implement appropriate responses
 		
 		Element root = xml.getRootElement();
 		String ownerUsername = null;
 		
 		Element ownerElement = root.element("owner");
 		if (ownerElement != null) {
 		ownerUsername = ownerElement.attributeValue("owner_username");
 		} 
 		
 		Xmlaction actionToPerform = new Xmlaction(ownerUsername);
 		MessageAction action = MessageAction.valueOf(root.getName());
 		
 		if (action == MessageAction.LOGIN) {
 			//Respond by sending back all the information the logged in user would need
 			
 			Element loginCandidate = root.element("login_attempt");
 			String password = loginCandidate.attributeValue("password");
 			
 			Document document = actionToPerform.login(ownerUsername, password);
 			
 			serverSend(document.asXML(), ownerUsername);
 			
 		}  else if (action == MessageAction.CREATE_MEETING) {
 			
 			List<Integer> userIDList; Event newEvent; int meetingRoomID = -1; String meetingName;
 			
 			//Finds all User objects
 			userIDList = XMLtoUserIDList(root);
 			
 			//Finds the event it needs
 			newEvent = XMLtoEvent(root);
 			
 			//Finds the meeting name
 			Element meetingElement = root.element("meeting");
 			meetingName = meetingElement.attributeValue("meeting_name");
 
 			
 			//Does the meeting have a booked room? -- Returns null if not found
 			Element meetingRoomElement = root.element("meeting_room");
 			
 			if(!(meetingRoomElement == null)) {
 				meetingRoomID = Integer.valueOf(meetingRoomElement.attributeValue("meeting_room_ID"));
 			}
 			
 			//Perform the action
 			Document document = actionToPerform.createMeeting(userIDList, newEvent, meetingRoomID, meetingName);
 			
 			//Send the message to the appropriate users!
 			changeNotificationBroadcast(actionToPerform.getBroadcastTo(), document.asXML());
 			
 		} else if (action == MessageAction.EDIT_MEETING) {
 			//TODO
 			
 			Event eventChanges; int meetingID;
 			
 			eventChanges = XMLtoEvent(root);
 			
 			Element meetingElement = root.element("meeting");
 			meetingID = Integer.valueOf(meetingElement.attributeValue("meeting_ID"));
 			
 			actionToPerform.editMeeting(eventChanges, meetingID);
 			
 		} else if (action == MessageAction.CREATE_USER) {
 			//Probably not gonna be used in the "final" product
 			User newUser = XMLtoUser(root);
 			actionToPerform.createUser(newUser);
 			
 		} else if (action == MessageAction.EDIT_NAME_OF_USER) {
 			
 			Element changeName = root.element("change_name");
 			String newName = changeName.attributeValue("new_name");
 			
 			Document document = actionToPerform.editNameOfUser(newName);
 			
 			serverBroadcast(document.asXML());
 			
 		} else if (action == MessageAction.EDIT_USER_PASSWORD) {
 			
 			Element changePassword = root.element("change_password");
 			String oldPassword = changePassword.attributeValue("old_password");
 			String newPassword = changePassword.attributeValue("new_password");
 			
 			Document document = actionToPerform.editUserPassword(oldPassword, newPassword);
 			
 			serverSend(document.asXML(), ownerUsername);
 			
 		} else if (action == MessageAction.EDIT_EVENT) {
 			
 			Event eventToEdit = XMLtoEvent(root);
 			actionToPerform.editEvent(eventToEdit);
 			
 		} else if (action == MessageAction.FETCH) {
 			//What needs to be in here?
 			
 		}			
 				
 	}	
 	
 	//This will be run from the client to interpret incoming messages
 	public void interpretMessageData(Document xml, Client client) throws ParseException {
 		//This can be 
 		
 		Element root = xml.getRootElement();
 		String ownerUsername = null;
 		int ownerID;
 		String ownerName = null;
 		
 		Element ownerElement = root.element("owner");
 		ownerUsername = ownerElement.attributeValue("owner_username");
 		ownerID = Integer.valueOf(ownerElement.attributeValue("owner_ID"));
 		ownerName = ownerElement.attributeValue("owner_name");
 		
 		MessageAction action = MessageAction.valueOf(root.getName());
 		
 		if (action == MessageAction.LOGIN) {
 			
 		if (root.element("login_response").attributeValue("response").equals("Failure")) {
 			System.out.println("Wrong password or username");
 			return;
 		}
 			
 		//Create the logged in user's object
 		Models.User loginUser = new Models.User(ownerID, ownerUsername);
 		loginUser.setName(ownerName);
 		
 		//Iterates through all the personal events
 		List<Models.Event> eventList = new ArrayList<Models.Event>();
 		for ( Iterator i = root.elementIterator( "personal_event" ); i.hasNext(); ) {
             Element eventElement = (Element) i.next();
     		int eventID = Integer.valueOf(eventElement.attributeValue("event_ID"));
     		Timestamp start = StringToDate(eventElement.attributeValue("start"));
     		Timestamp end = StringToDate(eventElement.attributeValue("end"));
     		String location = eventElement.attributeValue("location");
     		String description = eventElement.attributeValue("description");
     		Status status = Status.valueOf(eventElement.attributeValue("status"));
     		int meetingID = Integer.valueOf(eventElement.attributeValue("meetingID"));
     		String title = eventElement.attributeValue("meetingName");
     		Models.Event event = new Models.Event(eventID, loginUser, title, start, end, location, description);
     		event.setStatus(status);
     		eventList.add(event);
         }
 		
 		//Iterates through all the followed users
 		List<Models.User> followedUserList = new ArrayList<Models.User>();
 		for ( Iterator i = root.elementIterator( "followed_user" ); i.hasNext(); ) {
             Element userElement = (Element) i.next();
             int userID = Integer.valueOf(userElement.attributeValue("user_ID"));
             String username = userElement.attributeValue("username");
             String name = userElement.attributeValue("name");
             Models.User followedUser = new Models.User(userID, username);
             followedUser.setName(name);
             followedUserList.add(followedUser);
             List<Models.Event> followedUserEventList = new ArrayList<Models.Event>();
           //And iterates through their events
     		for ( Iterator y = root.elementIterator( "followed_user_event" ); y.hasNext(); ) {
                 Element eventElement = (Element) y.next();
                 if (eventElement.attributeValue("event_owner").equalsIgnoreCase(username)) {
                 	int eventID = Integer.valueOf(eventElement.attributeValue("event_ID"));
             		Timestamp start = StringToDate(eventElement.attributeValue("start"));
             		Timestamp end = StringToDate(eventElement.attributeValue("end"));
             		String location = eventElement.attributeValue("location");
             		String description = eventElement.attributeValue("description");
             		Status status = Status.valueOf(eventElement.attributeValue("status"));
             		int meetingID = Integer.valueOf(eventElement.attributeValue("meetingID"));
             		String title = eventElement.attributeValue("meetingName");
             		Models.Event event = new Models.Event(eventID, followedUser, title, start, end, location, description);
             		event.setStatus(status);
             		followedUserEventList.add(event);
             		followedUser.setEvents((ArrayList<Models.Event>) followedUserEventList);
                 }
             }
         }
 		
 		//Adds all the users in the database
 		List<Models.User> allUsers = new ArrayList<Models.User>();
         for ( Iterator i = root.elementIterator( "database_user" ); i.hasNext(); ) {
             Element userElement = (Element) i.next();
             int userID = Integer.valueOf(userElement.attributeValue("user_ID"));
             String username = userElement.attributeValue("username");
             String name = userElement.attributeValue("name");
             Models.User user = new Models.User(userID, username);
             user.setName(name);
             allUsers.add(user);
         }
         
         //TODO Group them up in meetings
         
         client.setUser(loginUser);
        client.setMyUsers((ArrayList<Models.User>) followedUserList);
         client.setAllUsers((ArrayList<Models.User>)allUsers);
 		
 			
 		} else if (action == MessageAction.CREATE_MEETING) {
 			
 			//Create the meeting leader
 			Models.User meetingLeader = new Models.User(ownerID, ownerUsername);
 			meetingLeader.setName(ownerName);
 			
 			Element meetingElement = root.element("meeting");
 			int meetingID = Integer.valueOf(meetingElement.attributeValue("meeting_ID"));
 			String meetingName = meetingElement.attributeValue("name");
 			
 			//Find the invited users of the meeting
 			List<Models.User> invitedUsers = new ArrayList<Models.User>();
 	        for ( Iterator i = root.elementIterator( "participant" ); i.hasNext(); ) {
 	            Element userElement = (Element) i.next();
 	            int userID = Integer.valueOf(userElement.attributeValue("user_ID"));
 	            for (Models.User user : client.getAllUsers()) {
 					if (userID == user.getUSER_ID()) {
 						invitedUsers.add(user);
 					}
 				}
 	        }
 	        
 	        //Find the meeting leaders event
 	        Element eventElement = root.element("leader_event");
 	        int eventID = Integer.valueOf(eventElement.attributeValue("event_ID"));
 			Timestamp start = StringToDate(eventElement.attributeValue("start"));
 			Timestamp end = StringToDate(eventElement.attributeValue("end"));
 			String location = eventElement.attributeValue("location");
 			String description = eventElement.attributeValue("description");
 			Status status = Status.valueOf(eventElement.attributeValue("status"));
 			Models.Event event = new Models.Event(eventID, meetingLeader, meetingName, start, end, location, description);
 			
 			Models.Meeting createdMeeting = new Models.Meeting(event);
 			createdMeeting.setMeetingID(meetingID);
 			createdMeeting.setParticipants((ArrayList<Models.User>) invitedUsers);
 			
 			client.getMeetings().add(createdMeeting);
 			
 			
 		} else if (action == MessageAction.CREATE_USER) {
 			
 		} else if (action == MessageAction.EDIT_NAME_OF_USER) {
 			
 		} else if (action == MessageAction.EDIT_USER_PASSWORD) {
 			
 		} else if (action == MessageAction.EDIT_EVENT) {
 			
 		}
 	}
 	
 	//Sender methods
 	public void createLoginRequest(String username, String password) {
 		
 		Document document = DocumentHelper.createDocument();
 		Element root = document.addElement(MessageAction.LOGIN.toString());
 		
 		root.addElement("owner")
 		.addAttribute("owner_username",username);
 				
 		root.addElement("login_attempt")
 		.addAttribute("password", password);
 		
 		clientSend(document.asXML());
 		
 	}
 	
 	//This will run on the client side. This can probably user the User class? -- tested and should be working
 	public void createAddMeetingRequest(List<Integer> userList, Event event, int meetingRoomID, String meetingName, String requestedBy) {
 		
 		Document document = DocumentHelper.createDocument();
 		Element root = document.addElement(MessageAction.CREATE_MEETING.toString());
 		
 		//Adds the username of the owner
 		root.addElement("owner")
 		.addAttribute("owner_username",requestedBy);
 		
 		//The users to be added
 		for (int userID : userList) {
 			root.addElement("user")
 			.addAttribute("user_ID", String.valueOf(userID));
 		}
 		
 		root.addElement("meeting")
 		.addAttribute("meeting_name", meetingName);
 		
 		//The event that will be created for all the users
 		//TODO: Fix the problem with the dates
 		root.addElement("event")
 		.addAttribute("start", event.getStart().toString())
 		.addAttribute("end", event.getEnd().toString())
 		.addAttribute("location", event.getLocation())
 		.addAttribute("description",event.getDescription())
 		.addAttribute("status",event.getStatus().toString());
 		
 		//Adds the meeting room
 		if (meetingRoomID != -1) {
 			root.addElement("meeting_room")
 			.addAttribute("meeting_room_ID",String.valueOf(meetingRoomID));
 		}
 		
 		clientSend(document.asXML());
 		
 	}
 	
 	//TODO: Change it to only include the changes?
 	public void createEditMeetingRequest(Event eventChanges, int meetingID, String requestedBy) {
 		//Here eventChanges includes the original events ID aswell as all the changes
 		
 		Document document = DocumentHelper.createDocument();
 		Element root = document.addElement(MessageAction.EDIT_MEETING.toString());
 		
 		//Adds the username of the owner
 		root.addElement("owner")
 		.addAttribute("owner_username", requestedBy);
 		
 		root.addElement("event")
 		.addAttribute("event_ID", String.valueOf(eventChanges.getEvent_ID()))
 		.addAttribute("start", eventChanges.getStart().toString())
 		.addAttribute("end", eventChanges.getEnd().toString())
 		.addAttribute("location", eventChanges.getLocation())
 		.addAttribute("description",eventChanges.getDescription())
 		.addAttribute("status",eventChanges.getStatus().toString());
 		
 		root.addElement("meeting")
 		.addAttribute("meeting_ID", String.valueOf(meetingID));
 		
 		clientSend(document.asXML());
 		
 	}
 	
 	public void createAddUserRequest(User newUser, String requestedBy) {
 		
 		Document document = DocumentHelper.createDocument();
 		Element root = document.addElement(MessageAction.CREATE_USER.toString());
 		
 		//Adds the username of the owner
 		root.addElement("owner")
 		.addAttribute("owner_username",requestedBy);
 		
 		root.addElement("user")
 		.addAttribute("username", newUser.getUsername())
 		.addAttribute("password", newUser.getPassword())
 		.addAttribute("name", newUser.getName());
 
 		clientSend(document.asXML());
 	}
 	
 	public void createEditNameOfUserRequest(String newName, String requestedBy) {
 		
 		Document document = DocumentHelper.createDocument();
 		Element root = document.addElement(MessageAction.EDIT_NAME_OF_USER.toString());
 		
 		//Adds the username of the owner
 		root.addElement("owner")
 		.addAttribute("owner_username",requestedBy);
 		
 		root.addElement("change_name")
 		.addAttribute("new_name", newName);
 		
 		clientSend(document.asXML());
 	}
 	
 	public void createEditUserPasswordRequest(String oldPassword, String newPassword, String requestedBy) {
 		
 		Document document = DocumentHelper.createDocument();
 		Element root = document.addElement(MessageAction.EDIT_USER_PASSWORD.toString());
 		
 		root.addElement("owner")
 		.addAttribute("owner_username", requestedBy);
 		
 		root.addElement("change_password")
 		.addAttribute("old_password", oldPassword)
 		.addAttribute("new_password", newPassword);
 		
 		clientSend(document.asXML());
 	}
 	
 	public void createEditEventRequest(Event eventChanges, String requestedBy) {
 		
 		Document document = DocumentHelper.createDocument();
 		Element root = document.addElement(MessageAction.EDIT_EVENT.toString());
 		
 		//Adds the user_ID and the username of the owner
 		root.addElement("owner")
 		.addAttribute("owner_username",requestedBy);
 		
 		root.addElement("event")
 		.addAttribute("event_ID", String.valueOf(eventChanges.getEvent_ID()))
 		.addAttribute("start", eventChanges.getStart().toString())
 		.addAttribute("end", eventChanges.getEnd().toString())
 		.addAttribute("location", eventChanges.getLocation())
 		.addAttribute("description",eventChanges.getDescription())
 		.addAttribute("status",eventChanges.getStatus().toString());
 		
 		clientSend(document.asXML());
 	}
 	
 	//Fetch requests
 	public Document fetchUser(String username, String requestedBy) {
 		
 		return null;
 	}
 	
 	//Helper methods
 	private Timestamp StringToDate(String string) throws ParseException {
 		
 		Timestamp timestamp = Timestamp.valueOf(string);
 		
 		return timestamp;
 		}  
 	
 	private Event XMLtoEvent(Element root) throws NumberFormatException, ParseException {
 		Element eventElement = root.element("event");
 		int userID = Integer.valueOf(eventElement.attributeValue("event_ID"));
 		Timestamp start = StringToDate(eventElement.attributeValue("start"));
 		Timestamp end = StringToDate(eventElement.attributeValue("end"));
 		String location = eventElement.attributeValue("location");
 		String description = eventElement.attributeValue("description");
 		Status status = Status.valueOf(eventElement.attributeValue("status"));
 		return new Event(userID,start,end,location,description,status);
 	}
 	
 	private User XMLtoUser(Element root) {
 		Element userElement = root.element("user");
         String username = userElement.attributeValue("username");
         String password = userElement.attributeValue("password");
         String name = userElement.attributeValue("name");
         return new User(username,password,name);
 	}
 	
 	private List<Integer> XMLtoUserIDList(Element root) {
 		List<Integer> userIDList = new ArrayList<Integer>();
 		for ( Iterator i = root.elementIterator( "user" ); i.hasNext(); ) {
             Element userElement = (Element) i.next();
             String user_ID = userElement.attributeValue("user_ID");
             userIDList.add(Integer.valueOf(user_ID));
         }
 		return userIDList;
 	}
 	
 	private List<User> XMLtoUserList(Element root) {
 		List<User> userList = new ArrayList<User>();
 		for ( Iterator i = root.elementIterator( "user" ); i.hasNext(); ) {
             Element userElement = (Element) i.next();
             String user_ID = userElement.attributeValue("user_ID");
             String username = userElement.attributeValue("username");
             String password = userElement.attributeValue("password");
             String name = userElement.attributeValue("name");
             User user = new User(Integer.valueOf(user_ID),username,password,name);
             userList.add(user);
         }
 		return userList;
 	}
 	public static String extractUsername(String xml) throws DocumentException {		
 		Document document = stringToXML(xml);
 		Element root = document.getRootElement();
 		
 		Element owner = root.element("owner");
 		return owner.attributeValue("owner_username");
 	}
 	
 	public void addListener(ActionListener listener) {
 		this.listener = listener;
 	}
 	
 	//Use this in another method to send to all related users
 	private void serverSend(String msg, String username) {
 		this.msg = msg;
 		this.username = username;
 		listener.actionPerformed(new ActionEvent(this, 0, "sendingmsg"));
 	}
 	
 	//Might not be used
 	private void serverBroadcast(String msg) {
 		this.msg = msg;
 		this.username = null;
 		listener.actionPerformed(new ActionEvent(this, 0, "sendingmsg"));
 	}
 	
 	private void clientSend(String msg) {
 		this.msg = msg;
 		listener.actionPerformed(new ActionEvent(this, 0, "sendingmsg"));
 	}
 	
 	private void changeNotificationBroadcast(List<User> broadcastTo, String msg) {
 		
 		for (User user : broadcastTo) {
 			serverSend(msg, user.getUsername());
 		}
 		
 	}
 	
 	public static String extractMessageAction(String xml) throws DocumentException {
 		Document document = stringToXML(xml);
 		Element root = document.getRootElement();
 
 		return root.getName();
 	}
 	
 	public String getUsernameForSending() {
 		return username;
 	}
 	public String getMsgForSending() {
 		return msg;
 	}
 }

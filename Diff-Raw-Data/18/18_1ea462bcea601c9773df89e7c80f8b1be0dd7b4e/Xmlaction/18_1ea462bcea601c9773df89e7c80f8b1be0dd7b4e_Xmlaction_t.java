 package xmlhandle;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 
 import dbhandle.Dbhandle;
 import dbhandle.Event;
 import dbhandle.Meeting;
 import dbhandle.MeetingRoom;
 import dbhandle.MessageAction;
 import dbhandle.User;
 
 public class Xmlaction {
 	
 	//This class is used to perform the different type of action it receives from the client.
 	//This class will only be run on server side
 	
 	private String ownerUsername;
 	private int ownerID;
 	private String ownerName;
 	private List<User> broadcastTo;
 	
 	public Xmlaction(String ownerUsername) {
 		
 		this.ownerUsername = ownerUsername;
 		
 		if (ownerUsername != null) {
 		User owner = new Dbhandle().fetchUser(ownerUsername);
 		this.ownerID = owner.getUserID();
 		this.ownerName = owner.getName();
 		} else {
 			ownerUsername = "";
 		}
 	}
 	
 	//Tested and should be working
 	public Document login(String username, String password) throws SQLException {
 		
 		//Init response message
 		Document document = DocumentHelper.createDocument();
 		Element root = document.addElement(MessageAction.LOGIN.toString());
 		
 		//Adds the username of the owner
 		root.addElement("owner")
 		.addAttribute("owner_username",ownerUsername)
 		.addAttribute("owner_ID", String.valueOf(ownerID))
 		.addAttribute("owner_name", ownerName);
 			
 		
 		//Verify with database and create a connection with user.
 		Dbhandle handle = new Dbhandle();
 		User dbUser = handle.fetchUser(username);
 		
 		//Check if the passwords match
 		if (!dbUser.getPassword().equals(password)) {
 			//Login failed response message
 			
 			root.addElement("login_response")
 			.addAttribute("response", "Failed");
 			
 			return document;
 
 		}
 		
 		//Login success! Send the information the user needs
 		root.addElement("login_response")
 		.addAttribute("response", "Success");
 		
 		//List of owners personal events
 		List<Event> eventList = handle.fetchUserEvents(ownerUsername);
 		
 		//Get List of users the person follows
 		List<User> followedUsers = handle.fetchUsersFollowed(ownerID);
 		
		System.out.println(followedUsers.size());
		
 		//Get their events
 		for (User user : followedUsers) {
 			user.setPersonalEvents(handle.fetchUserEvents(user.getUsername()));
 		}
 		
 		//List of all users in the database system
 		List<User> allUsers = handle.fetchAllUsers();
 		
 		//Anything else?
 		
 		////// RESPONSE //////
 				
 		//Create the document
 		
 		//Add all the personal events
 		for (Event event : eventList) {
 			Meeting meeting = handle.fetchMeetingOfEvent(event.getEvent_ID());
 			root.addElement("personal_event")
 			.addAttribute("meetingID", String.valueOf(meeting.getMeeting_ID()))
 			.addAttribute("meetingName", String.valueOf(meeting.getName()))
 			.addAttribute("event_ID", String.valueOf(event.getEvent_ID()))
 			.addAttribute("start", event.getStart().toString())
 			.addAttribute("end", String.valueOf(event.getEnd().toString()))
 			.addAttribute("location", event.getLocation())
 			.addAttribute("description", event.getDescription())
 			.addAttribute("status", event.getStatus().toString());
 		}
 		
 			//Adds the followed users and their events
 			for (User user : followedUsers) {
 			root.addElement("followed_user")
 			.addAttribute("user_ID", String.valueOf(user.getUserID()))
 			.addAttribute("username", user.getUsername())
 			.addAttribute("name", user.getName());
 			
 			
 			for (Event event : user.getPersonalEvents()) {
 				Meeting meeting = handle.fetchMeetingOfEvent(event.getEvent_ID());
 				root.addElement("followed_user_event")
 				.addAttribute("event_owner", user.getUsername())
 				.addAttribute("meetingID", String.valueOf(meeting.getMeeting_ID()))
 				.addAttribute("meetingName", String.valueOf(meeting.getName()))
 				.addAttribute("event_ID", String.valueOf(event.getEvent_ID()))
 				.addAttribute("start", event.getStart().toString())
 				.addAttribute("end", String.valueOf(event.getEnd().toString()))
 				.addAttribute("location", event.getLocation())
 				.addAttribute("description", event.getDescription())
 				.addAttribute("status", event.getStatus().toString());
 				
 			}
 			
 		}
 			
 		for (User user : allUsers) {
 			root.addElement("database_user")
 			.addAttribute("user_ID", String.valueOf(user.getUserID()))
 			.addAttribute("username", user.getUsername())
 			.addAttribute("name", user.getName());
 		}
 		
 		
 		return document;
 	}
 	
 	//Tested and should be working
 	public Document createMeeting(List<Integer> userIDList, Event newEvent, int meetingRoomID, String meetingName) throws SQLException {
 		//Expected input: A single event that can be added multiple times for each user
 		//A user list with all the invited users
 		//Potentially a meeting room.
 		//Who the meeting leader is
 		
 		
 		//Adds a new meeting to the database -- need the generated meeting ID.
 		//Adds all the relevant meeting events
 		//Adds all the relevant user events
 		//Adds a meeting leader
 		Dbhandle handle = new Dbhandle();
 		int meeting_ID = handle.addMeeting(meetingName);
 		
 		
 				
 		//Adds all the included events into the database. It will also handle the user_event and meeting_event relations.
 		//This way, the event will be related to ONE user exclusively, but all the events will be related to the same meeting.
 		for (int element : userIDList) {
 			int event_ID = handle.addEvent(newEvent);
 			handle.addMeetingEvent(meeting_ID, event_ID);
 			handle.addUserEvent(element, event_ID);
 			//Reserve a meeting room if the meeting needs it
 			if (meetingRoomID != -1) {
 				handle.addEventMeetingRoom(event_ID, meetingRoomID);
 			}
 		}
 				
 		//Figure out the meeting leader 
 		handle.addMeetingLeader(meeting_ID, ownerID);
 		
 		////// RESPONSE //////
 		
 		
 		//Get added users -- Needed? 
 		List<User> userList = new ArrayList<User>();
 		for (int userID : userIDList) {
 			User user = handle.fetchUser(userID);
 			userList.add(user);
 		}
 		
 		//Note, will also probably need to notify the followers of a user
 		setBroadcastTo(userList);
 		
 		//Fetches the leader of the meeting -- Use owner instead?
 		User meetingLeader = handle.fetchMeetingLeader(meeting_ID);
 		
 		//Fetches the event of the meeting leader
 		Event meetingLeaderEvent = handle.fetchUserEventInMeeting(ownerID, meeting_ID);
 		
 		//Init response message
 		Document document = DocumentHelper.createDocument();
 		Element root = document.addElement(MessageAction.CREATE_MEETING.toString());
 				
 		//Adds the username of the owner
 		root.addElement("owner")
 		.addAttribute("owner_username",ownerUsername)
 		.addAttribute("owner_ID", String.valueOf(ownerID))
 		.addAttribute("owner_name", ownerName);
 		
 		root.addElement("meeting")
 		.addAttribute("meeting_ID", String.valueOf(meeting_ID))
 		.addAttribute("name", meetingName);
 		
 		//Adds the participating users ID
 		for (int userID : userIDList) {
 			root.addElement("participant")
 			.addAttribute("user_ID", String.valueOf(userID));
 		}
 		
 		//Adds the meating leaders event
 		root.addElement("leader_event")
 		.addAttribute("event_ID", String.valueOf(meetingLeaderEvent.getEvent_ID()))
 		.addAttribute("start", meetingLeaderEvent.getStart().toString())
 		.addAttribute("end", String.valueOf(meetingLeaderEvent.getEnd().toString()))
 		.addAttribute("location", meetingLeaderEvent.getLocation())
 		.addAttribute("description", meetingLeaderEvent.getDescription())
 		.addAttribute("status", meetingLeaderEvent.getStatus().toString());
 		
 		return document;
 	}
 	
 	//Tested and should be working
 	public void createUser(User newUser) throws SQLException {
 		//Expected input: The information of the new user
 		
 		//Will probably not be used
 		Dbhandle handle = new Dbhandle();
 		handle.addUser(newUser);
 		
 	}
 	
 	//Tested and should be working
 	public boolean editMeeting(Event eventChanges, int meetingID) throws SQLException {
 		//Here you should be able to change pretty much everything about the meeting/event.
 		//Can only be done by the meeting leader.
 		//Note: This must only be used to change the meeting leader's event, not anyone else's.
 		//This is done so that any user will see that something has changed before accepting it.
 		
 		//TODO
 		
 		//Init response message
 		Document document = DocumentHelper.createDocument();
 		Element root = document.addElement(MessageAction.EDIT_MEETING.toString());
 			
 		//Adds the username of the owner
 		root.addElement("owner")
 		.addAttribute("owner_username",ownerUsername)
 		.addAttribute("owner_ID", String.valueOf(ownerID))
 		.addAttribute("owner_name", ownerName);
 		
 		Dbhandle handle = new Dbhandle();
 		User user = handle.fetchMeetingLeader(meetingID);
 				
 		if (!(user.getUserID() == ownerID)) {
 			System.out.println("Not allowed to edit");
 			root.addElement("password_change")
 			.addAttribute("result", "Failure");
 		}		
 		
 		handle.updateEvent(eventChanges);
 				
 		return true;
 		
 	}
 	
 	//Tested and should be working
 	public Document editNameOfUser(String newName) throws SQLException {
 	
 		Dbhandle handle = new Dbhandle();
 		handle.updateNameOfUser(ownerID, newName);
 		
 		//Init response message
 		Document document = DocumentHelper.createDocument();
 		Element root = document.addElement(MessageAction.EDIT_NAME_OF_USER.toString());
 				
 		//Adds the username of the owner
 		root.addElement("owner")
 		.addAttribute("owner_username",ownerUsername)
 		.addAttribute("owner_ID", String.valueOf(ownerID))
 		.addAttribute("owner_name", newName);
 		
 		root.addElement("name_change")
 		.addAttribute("new_name", newName);
 		
 		return document;
 		
 	}
 	
 	//Tested and should be working
 	public Document editUserPassword(String oldPassword, String newPassword) {
 		
 		Dbhandle handle = new Dbhandle();
 		User user = handle.fetchUser(ownerID);
 		
 		
 		//Init response message
 		Document document = DocumentHelper.createDocument();
 		Element root = document.addElement(MessageAction.EDIT_USER_PASSWORD.toString());
 		
 		//Adds the username of the owner
 		root.addElement("owner")
 		.addAttribute("owner_username",ownerUsername)
 		.addAttribute("owner_ID", String.valueOf(ownerID))
 		.addAttribute("owner_name", ownerName);
 	
 		if (!oldPassword.equals(user.getPassword())) {
 			System.out.println("Incorrect password");
 			root.addElement("password_change")
 			.addAttribute("result", "Failure");
 			return document;
 		}
 		
 		handle.updateUserPassword(ownerID, newPassword);
 
 		//Dont really need to return the password?
 		root.addElement("password_change")
 		.addAttribute("result", "Success")
 		.addAttribute("new_password", newPassword);
 		
 		return document;
 		
 	}
 	
 	public Document editEvent(Event event) throws SQLException {
 		//Test to see if the user is the owner of the event
 		//TODO
 		//Init response message
 		Document document = DocumentHelper.createDocument();
 		Element root = document.addElement(MessageAction.EDIT_EVENT.toString());
 			
 		//Adds the username of the owner
 		root.addElement("owner")
 		.addAttribute("owner_username",ownerUsername)
 		.addAttribute("owner_ID", String.valueOf(ownerID))
 		.addAttribute("owner_name", ownerName);
 		
 		Dbhandle handle = new Dbhandle();
 		int userID = handle.fetchEventOwnerID(event.getEvent_ID());
 		
 		if (userID != ownerID) {
 			System.out.println("User not permitted to edit event. Reason: Not event owner");
 			root.addElement("edit_event")
 			.addAttribute("result", "Failure");
 			return document;
 		}
 		
 		handle.updateEvent(event);
 		
 		root.addElement("edit_event")
 		.addAttribute("result", "Success");
 		
 		//This needs to determine a bunch of stuff
 		//What users to notify etc
 		
 		return document;
 		
 	}
 
 	public List<User> getBroadcastTo() {
 		return broadcastTo;
 	}
 
 	public void setBroadcastTo(List<User> broadcastTo) {
 		this.broadcastTo = broadcastTo;
 	}
 	
 }

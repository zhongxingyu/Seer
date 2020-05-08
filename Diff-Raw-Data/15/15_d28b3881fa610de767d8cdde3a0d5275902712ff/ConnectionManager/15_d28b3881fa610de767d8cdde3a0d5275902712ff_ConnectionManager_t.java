 package client;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 
 import model.Event;
 import model.Invitation;
 import model.InvitationAnswer;
 import model.Room;
 import model.User;
 import structs.Request;
 import structs.Response;
 
 public class ConnectionManager {
 
 	private Program program;
 	private ClientConnection outboundConnection;
 	private ClientConnectionListener inboundConnection;
 
 	private String key;
 
 	public ConnectionManager(Program program) {
 		this.program = program;
 
 		outboundConnection = new ClientConnection("78.91.9.92", 4447);
 		inboundConnection = new ClientConnectionListener("78.91.9.92", 4447, this);
 	}
 
 	public boolean login(String username, String password) {
 
 		if (outboundConnection.openConnection()) {
 			String result;
 			Request request = new Request();
 			request.setRequest(Request.LOGIN);
 			request.addItem("username", username);
 			request.addItem("password", password);
 			outboundConnection.sendObject(request);
 			Response response = outboundConnection.reciveResponse();
 			if (response.errorExist()) {
 				System.out.println("error " + response.getItem("error"));
 				return false;
 			} else {
 				result = (String) response.getItem("result");
 				if (result.equals("loginok")) {
 					key = (String) response.getItem("key");
 					program.setUser((User) response.getItem("user"));
 					return true;
 				}
 			}
 		}
 		System.out.println("connection failed");
 		return false;
 	}
 
 	public void attachSocket() {
 		if (inboundConnection.openConnection()) {
 			Request request = new Request();
 			request.setRequest(Request.ATTACH_SOCKET);
 			request.addItem("key", key);
 			inboundConnection.sendObject(request);
 			Response response = inboundConnection.reciveResponse();
 			System.out.println(response.getItem("result"));
 			inboundConnection.run();
 		}
 	}
 
 	public ArrayList<Event> getEvents(User user) {
 		Request request = new Request(Request.GET_USERS_APPOINTMENTS);
 		request.addItem("user", user);
 		outboundConnection.sendObject(request);
 		Response response = outboundConnection.reciveResponse();
 		if (response.errorExist())
 			System.out.println(response.getItem("error"));
 		else {
 			ArrayList<Event> events = (ArrayList<Event>) response
 					.getItem("ownedevents");
 			for (Event event : events)
 				System.out.println(event.getTitle());
 
 			System.out.println("Invitated events:");
 
 			events.addAll((ArrayList<Event>) response.getItem("invitedevents"));
 //			events = (ArrayList<Event>) response.getItem("invitedevents");
 //			for (Event event : events)
 //				System.out.println(event.getTitle());
 			return events;
 		}
 		return null;
 	}
 
 	public ArrayList<User> getUsers() {
 		Request request = new Request(Request.GET_USERS);
 		outboundConnection.sendObject(request);
 		Response response = outboundConnection.reciveResponse();
 		if (response.errorExist()) {
 			System.out.println(response.getItem("error"));
 		} else {
 			return (ArrayList<User>) response.getItem("users");
 		}
 		return null;
 	}
 
 	public ArrayList<Room> getRooms(Timestamp start, Timestamp end) {
 		Request request = new Request(Request.GET_ROOMS);
 		request.addItem("start", start);
 		request.addItem("end", end);
 		outboundConnection.sendObject(request);
 		Response response = outboundConnection.reciveResponse();
 		if (response.errorExist())
 			System.out.println(response.getItem("error"));
 		else {
 			System.out.println("rooms:");
 			ArrayList<Room> rooms = (ArrayList<Room>) response.getItem("rooms");
 			return rooms;
 		}
 		return null;
 	}
 
 	public void handleNotifications(int type) 
 	{
 		System.out.println("Recieived notification");
 		switch (type) 
 		{
 		case 1:
 			System.out.println("type 1");
 			// get uppdates
 			break;
 
 		default:
 			break;
 		}
	}
		
 	public ArrayList<Invitation> getNotifications() {
 		Request request = new Request(Request.GET_USERS_NOTIFICATIONS);
 		outboundConnection.sendObject(request);
 		Response response = outboundConnection.reciveResponse();
 		if (response.errorExist()) {
 			System.out.println(response.getItem("error"));
 		}
 		else {
 			// TODO
 			ArrayList<Invitation> notifications = (ArrayList<Invitation>) response.getItem("invitation");
 			return notifications;
 		}
 		return null;
 	}
 
 	public void addEvent(Event event) {
 		Request request = new Request(Request.ADD_APPOINTMENT);
 		request.addItem("event", event);
 		outboundConnection.sendObject(request);
 		Response response = outboundConnection.reciveResponse();
 		if (response.errorExist())
 			System.out.println(response.getItem("error"));
 		else {
 			Event newEvent = (Event) response.getItem("event");
 			addInvitation(event.getParticipants(), newEvent);
 			System.out.println(response.getItem("result"));
 		}
 	}
 
 	public void addInvitation(ArrayList<Invitation> users, Event event) {
 		Request request = new Request(Request.ADD_INVITE);
 		request.addItem("invitations", users);
 		outboundConnection.sendObject(request);
 		Response response = outboundConnection.reciveResponse();
 		if (response.errorExist())
 			System.out.println(response.getItem("error"));
 		else {
 			System.out.println(response.getItem("result"));
 		}
 	}
 
 	public boolean deleteEvent(Event event) {
 		return true;
 	}
 }

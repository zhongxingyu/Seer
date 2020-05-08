 package server;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 
 import model.Event;
 import model.Invitation;
 import model.InvitationAnswer;
 import model.Room;
 import model.User;
 import structs.Request;
 import structs.Response;
 
 public class ServerMethods 
 {
 
 	public static Response handleRequest(Request request, Session session)
 	{
 		Response response = new Response();
 		DbConnection dc = new DbConnection("jdbc:mysql://arve.in/fellesprosjekt", "fellesproj", "pebcak");
 		
 		if(request == null)
 		{
 			response.addItem("error", "No request");
 		}
 		else if(!dc.connect())
 		{
 			response.addItem("error", "Unable to connect to the database");
 		}
 		else
 		{					
 			switch (request.getRequest()) 
 			{
 		        case Request.GET_USERS_APPOINTMENTS:  
 		        	getUsersAppointment(request, response, dc, session);
 		            break;
 		        case Request.GET_USERS_NOTIFICATIONS:  
 		        	getUsersNotifications(request, response, dc, session);
 		            break;
 		        case Request.ADD_APPOINTMENT:  
 		        	addAppointment(request, response, dc, session);
 		            break;
 		        case Request.GET_UPDATE_ALL:  
 		        	//TODO
 		        	testNotification(response, session);
 		            break;
 		        case Request.UPDATE_APPOINTMENT:  
 		        	updateAppointment(request, response, dc);
 		            break;
 		        case Request.UPDATE_INVITE:  
 		        	updateInvite(request, response, dc, session);
 		            break;
 		        case Request.DELETE_APPOINTMENT:  
 		        	deleteAppointment(request, response, dc, session);
 		            break;
 		        case Request.GET_USERS:  
 		        	getUsers(response, dc, session);
 		            break;
 		        case Request.GET_ROOMS:  
 		        	getRooms(request, response, dc);
 		            break;
 		        case Request.CREATE_USER:  
 		        	createUser(request, response, dc);
 		            break;
 		        case Request.ADD_INVITE:  
 		        	addInvite(request, response, dc, session);
 		            break;
 		        default:
 		        	response.addItem("error", "Unknown request");
 		            
 			}
 		}	
 		dc.closeConnection();
 		return response;		
 	}
 	
 	public static Response handleNewConnection(Request request, NewConnection connection)
 	{
 		Response response = new Response();
 		DbConnection dc = new DbConnection("jdbc:mysql://arve.in/fellesprosjekt", "fellesproj", "pebcak");
 		
 		if(request == null)
 		{
 			response.addItem("error", "No request");
 		}
 		else if(!dc.connect())
 		{
 			response.addItem("error", "Unable to connect to the database");
 		}
 		else
 		{					
 			switch (request.getRequest()) 
 			{
 				case Request.LOGIN:  
 					login(request, response, connection, dc);
 					break;
 		        case Request.ATTACH_SOCKET:  
 		        	attachSocket(request, response, connection);
 		            break;
 		        case Request.CREATE_USER:  
 		        	createUser(request, response, dc);
 		            break;
 		        default:
 		        	response.addItem("error", "Invalid request");
 		            
 			}
 		}	
 		dc.closeConnection();
 		return response;		
 	}
 	
 	private static boolean login(Request request, Response response, NewConnection connection, DbConnection dc)
 	{
 		
 		String username = (String) request.getItem("username");
 		String password = (String) request.getItem("password");
 		
 		try
 		{
 			byte[] salt = dc.getStoredHash(username, "pw_hash");
 			byte[] hashedPassword = dc.getStoredHash(username, "password");
 			
 			if(PasswordEncryption.checkPassword(password, hashedPassword, salt))	
 			{
 				Session session = new Session(connection.getSocket(), connection.getSessionManager());
 				String key = PasswordEncryption.createSalt().toString();
 				session.setKey(key);
 				session.setUser(dc.getUser(username));
 				session.addToList();
 				session.start();
 				response.addItem("key", key);
 				response.addItem("user", dc.getUser(username));
 				response.addItem("result", "loginok");
 				return true;
 			}
 			else
 				response.addItem("result", "loginfailed");
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			System.out.println("Message:");
 			e.getMessage();
 			System.out.println("Cause");
 			e.getCause();
 			response.addItem("error", e.toString());
 		}
 		return false;
 	}
 
 	private static void attachSocket(Request request, Response response, NewConnection connection)
 	{
 		try
 		{
 			String key = (String) request.getItem("key");
 			System.out.println(key.toString());
 			Session session = connection.getSessionManager().getSession(key);
 			if(session != null)
 			{
 				session.setOutboundSocket(connection.getSocket());
 				response.addItem("result", "Socket added");
 			}
 			else
 				response.addItem("result", "No Active session found");
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			response.addItem("error", e.toString());
 		}
 	}
 
 	private static void getUsersAppointment(Request request, Response response, DbConnection dc, Session session)
 	{
 		try
 		{
 			User user = (User) request.getItem("user");
 			
 			if(user == null)
 				user = session.getUser();
 
 			response.addItem("ownedevents", dc.getEventsCreatedByUser(user));			
 			ArrayList<Invitation> invites = dc.getInvites(user);
 			ArrayList<Event> events  = new ArrayList<Event>();
 			for(Invitation invite : invites)
 				events.add(invite.getEvent());
 			response.addItem("invitedevents", events);
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			response.addItem("error", e.toString());
 		}
 	}
 
 	private static void getUsersNotifications(Request request, Response response, DbConnection dc, Session session)
 	{	
 		try
 		{
 			response.addItem("invitation", dc.getInvites(session.getUser()));
 			//response.addItem("cancellations", dc.getCancellations(session.getUser()));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			response.addItem("error", e.toString());
 		}
 	}
 	
 	private static void addAppointment(Request request, Response response, DbConnection dc, Session session)
 	{
 		try
 		{
 			Event event = (Event) request.getItem("event");
 			event.setCreatedBy(session.getUser());
 			
 			if(event != null)
 			{
 				response.addItem("event", dc.createAppointment(event));
 				response.addItem("result", "Appointment added");
 			}
 			else
 				response.addItem("error", "invalid input - event is null");
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			response.addItem("error", e.toString());
 		}
 	}
 	
 	private static void addInvite(Request request, Response response, DbConnection dc, Session session)
 	{
 		try
 		{
 			ArrayList<Invitation> invites = (ArrayList<Invitation>) request.getItem("invitations");
 			
 			if(invites != null)
 			{
 				for(Invitation invite : invites)
 				{
 					dc.createInvitation(invite.getEvent().getEventId(), invite.getTo().getUserId());
 					sendNotification(1, session.getSessionManager(), invite.getTo());
 				}
 				response.addItem("result", "Invitations added");
 			}
 			else
 				response.addItem("error", "invalid input - invite is null");
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			response.addItem("error", e.toString());
 		}
 	}
 	
 	private static void updateAppointment(Request request, Response response, DbConnection dc)
 	{
 		
 		int id = (Integer)request.getItem("id"); 
 		
 		try
 		{
 			// updateAppointment(int eventId, String columnname, String value)
 			
 			if(request.hasKey("start"))
 				dc.updateAppointment(id, "start", ((Timestamp) request.getItem("start")).toString());
 			if(request.hasKey("end"))
 				dc.updateAppointment(id, "end", ((Timestamp) request.getItem("end")).toString());
 			if(request.hasKey("description"))
 				dc.updateAppointment(id, "description", (String) request.getItem("description"));
 			if(request.hasKey("room"))
 				dc.updateAppointment(id, "roomid", ""+((Room) request.getItem("room")).getId());
 			if(request.hasKey("title"))
 				dc.updateAppointment(id, "name",  (String) request.getItem("title"));
 			if(request.hasKey("participants")){
 				
 				ArrayList<User> participants = (ArrayList<User>) request.getItem("participants");
 				ArrayList<Invitation> dbInvitations = dc.getInvitationsByEvent(id);
 				ArrayList<User> dbUsers = new ArrayList<User>(); 
 				
 				for (Invitation inv : dbInvitations) {
 					dbUsers.add(inv.getTo());
 				}
 				
 				for (User user : participants) {
 					if(!dbUsers.contains(user));
 						dc.createInvitation(id, user.getUserId());
 				}
 				
 			}
 			response.addItem("result", "Update ok");
 			
 			//TODO trigger notifications
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			response.addItem("error", e.toString());
 		}
 	}
 	
 	private static void updateInvite(Request request, Response response, DbConnection dc, Session session)
 	{
 		try
 		{
 			Invitation invite = (Invitation) request.getItem("invite");
			dc.updateInvitation(invite.getId(), invite.getTo().getUserId(), invite.getStatus());
 			response.addItem("result", "Invite updated");
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			response.addItem("error", e.toString());
 		}
 	}
 	
 	private static void deleteAppointment(Request request, Response response, DbConnection dc, Session session)
 	{
 		try
 		{
 			dc.deleteAppointment((Integer) request.getItem("id"));
 			//sendNotification(1, session.getSessionManager());
 			response.addItem("result", "Delete OK");
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			response.addItem("error", e.toString());
 		}
 	}
 	
 	private static void getUsers(Response response, DbConnection dc, Session session)
 	{
 		try
 		{
 			ArrayList<User> users = dc.getUsers();
 			response.addItem("users", users);
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			response.addItem("error", e.toString());
 		}
 	}
 	
 	private static void getRooms(Request request, Response response, DbConnection dc)
 	{
 		try
 		{
 			Timestamp start = (Timestamp) request.getItem("start");
 			Timestamp end = (Timestamp) request.getItem("end");
 			response.addItem("rooms", dc.getAvailableRooms(start, end));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			response.addItem("error", e.toString());
 		}
 	}
 	
 	private static void createUser(Request request, Response response, DbConnection dc)
 	{
 		try
 		{
 			String email = (String) request.getItem("username");
 			String password = (String) request.getItem("password");
 			String name = (String)	request.getItem("name");
 			User user = new User();
 			user.setEmail(email);
 			user.setName(name);
 			
 			byte[] salt = PasswordEncryption.createSalt();
 			byte[] hashedPassword = PasswordEncryption.getHash(password, salt);
 			
 			dc.createUser(user, hashedPassword, salt);
 			response.addItem("result", "User created");
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			response.addItem("error", e.toString());
 		}
 	}
 	
 	private static void sendNotification(int type, SessionManager sessionManager, User user)
 	{
 		Session session = sessionManager.findSession(user);
 		
 		if(session != null)
 			session.sendNotification(type);
 		
 	}
 	
 	private static void testNotification( Response response, Session session)
 	{
 		response.addItem("result", "notification sent");
 		session.sendNotification(1);
 	}
 }

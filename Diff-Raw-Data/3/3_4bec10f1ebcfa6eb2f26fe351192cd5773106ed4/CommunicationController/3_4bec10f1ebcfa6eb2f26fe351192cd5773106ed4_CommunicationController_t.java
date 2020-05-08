 
 package no.ntnu.fp.net.network.client;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InterruptedIOException;
 import java.io.ObjectOutputStream;
 import java.lang.reflect.Type;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.lang.reflect.ParameterizedType;
 
 import javax.net.ssl.HostnameVerifier;
 
 import org.jdom.adapters.XML4JDOMAdapter;
 
 import no.ntnu.fp.model.Appointment;
 import no.ntnu.fp.model.Authenticate;
 import no.ntnu.fp.model.Calendar;
 import no.ntnu.fp.model.CalendarEntry;
 import no.ntnu.fp.model.Location;
 import no.ntnu.fp.model.Meeting;
 import no.ntnu.fp.model.Meeting.State;
 import no.ntnu.fp.model.Place;
 import no.ntnu.fp.model.User;
 import no.ntnu.fp.model.XmlHandler;
 import no.ntnu.fp.net.network.Request;
 import no.ntnu.fp.net.network.Request.Method;
 import no.ntnu.fp.model.Room;
 
 
 
 
 //Static ? 
 
 /**
  * @author bj0rn
  * This class provides the interface for the communication.
  * The methods can be used by the client to produce requests to the server
  * The methods are made to fake synchronization between the client and the server
  * 
  * **/
 //TODO: Find a way to notify the client model about notifications
 
 
 
 
 
 public class CommunicationController {
 	
 	public static String host = "78.91.22.12"; //"127.0.0.1"; //
 	public final static int PORT = 1337;
 
 	private static CommunicationController instance;
 	//fields
 	private BlockingQueue<Object> inQueue;
 	private Socket mySocket;
 	private UpdateHandler updateHandler;
 	private LinkedBlockingDeque<Object> testQueue;
 	
 	// Models
 	
 	/**
 	 * The authentication used throughout the session. 
 	 * Created by the {@code LoginFrame} 
 	 */
 	private Authenticate auth;
 
 	/**
 	 * The {@code User} for the client own {@code User} 
 	 */
 	private User user;
 	
 	/**
 	 * A complete {@code List} of all {@code User}s 
 	 */
 	private List<User> users;
 	
 	/**
 	 * A {@code List} of {@code User}s which the {@code user} shows.
 	 */
 	private List<User> shows; 
 	
 	/**
 	 * The complete {@code List} of {@code Room}s. 
 	 */
 	private List<Room> rooms;
 	
 	/**
 	 * A {@code List} of {@code Place}s.
 	 */
 	private List<Place> places;
 	
 	
 	
 	private DataOutputStream os;
 	private ObjectOutputStream oos;
 	
 	final static String USER = "User";
 	
 	
 	
 	public static void main(String[] args) {
 	
 		User u = new User("bjorn", "123");
 		
 		
 		List <User> users = new ArrayList<User>();
 		users.add(u);
 		
 		
 		List test = (List) users;
 		Object obj = test.get(0);
 		System.out.println(obj.getClass().getSimpleName());
 		
 	
 	}
 	
 	
 	
 	//constructor 
 	private CommunicationController(){
 		
 		
 		LinkedBlockingDeque<Object> testQueue = new LinkedBlockingDeque<Object>();
 		//CommunicationController communicationController = new CommunicationController(mySocket, testQueue)
 		Client c = new Client(host, PORT, testQueue, this);
 		
 		try {
 			this.mySocket = c.getSocket();
 			os = new DataOutputStream(mySocket.getOutputStream());
 			updateHandler = new UpdateHandler();
 			this.testQueue = testQueue;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		new Thread(c).start();
 	}
 	
 	//TODO: Test if this approach works ? 
 	public static synchronized CommunicationController getInstance() {
 		if (instance == null) {
 			instance = new CommunicationController();
 		}
 		
 		return instance;
 	}
 	
 	public static void setHost(String ip) {
 		host = ip; 
 	}
 	
 	
 	public void send(Socket socket, Object obj){
 		DataOutputStream os;
 		try {
 			os = new DataOutputStream(socket.getOutputStream());
 			ObjectOutputStream oos = new ObjectOutputStream(os);
 			oos.writeObject(obj);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	
 	public void inspect(){
 		//When notifications arrive we need to be ready
 		//How can we handle them ?
 	}
 	
 	
 	
 	
 	/**
 	 * This method will authenticate will login in the user to the server
 	 * **/
 	public boolean authenticate(Authenticate auth){
 		try {
 			Request request = new Request(auth, null);
 			request.setMethod(Request.Method.AUTHENTICATE);
 			send(mySocket, request);
 			setAuthunticate(auth);
 			//Wait for response
 			boolean good = false;
 			int i = 0;
 			while(!good){
 				Request response = (Request)testQueue.takeFirst();
 				System.out.println("Number of tries: "+i++);
 				if(response.getMethod() == Request.Method.LOGIN_SUCCEDED){
 					return true;
 				}else if(response.getMethod() == Request.Method.LOGIN_FAILED){
 					return false;
 				}
 				else {
 					System.out.println("Put back");
 					testQueue.putLast((Object)response);
 				}
 				
 			}
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return false;
 }
 
 	
 	private void setAuthunticate(Authenticate auth) {
 		this.auth = auth;		
 	}
 	
 	public Authenticate getAuthenticate() {
 		return auth;
 	}
 	
 	/**
 	 * Retrieves the full {@code List} of {@code User}s once from the server.
 	 * Returns the {@code List}.
 	 * 
 	 * @return
 	 */
 	public List<User> getListOfUsers() {
 		if (users == null) {
 			updateListOfUsers();
 		}
 		return users;
 	}
 
 	/**
 	 * This method will get all the users from the server
 	 * **/
 	public void updateListOfUsers() {
 		try {
 			Request request = new Request(auth, null);
 			request.setMethod(Request.Method.GET_USERS);
 			send(mySocket, request);
 			int i = 0;
 			while(true){
 				System.out.println("Number of tries: "+i++);
 				Request response = (Request) testQueue.takeFirst();
 				if(response.getMethod() == Request.Method.GET_USERS_RESPONSE){
 					users = (List<User>)response.getObject();
 					return;
 				}else if (response.getMethod() == Request.Method.LOGIN_FAILED){
 					//return null;
 				}else{
 					//Put it back and try again
 					System.out.println("Put back");
 					testQueue.putLast((Object)response);
 				}
 				
 			}
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void addSelectedUser(User user) {
 		shows.add(user);
 	}
 	
 	public void removeSelectedUser(User user) {
 		shows.remove(user);
 	}
 	
 	public List<User> getSelectedUsers() {
 		if (shows == null) {
 			updateSelectedUsers();
 		}
 		
 		return shows;
 	}
 	
 
 	public void updateSelectedUsers(){
 		try{
 			Request request = new Request(auth, null);
 			request.setMethod(Method.GET_SUBSCRIBERS);
 			send(mySocket, request);
 			int i = 0;
 			while(true){
 				System.out.println("Number of tries: "+i++);
 				Request response = (Request)testQueue.takeFirst();
 				if(response.getMethod() == Method.GET_SUBSCRIBERS_RESPONSE){
 					System.out.println("Yey :) ");
 					shows = (List<User>)response.getObject();
					return;
 				}else if(response.getMethod() == Method.LOGIN_FAILED){
 					System.out.println("Not logged inn");
					return;
 				}else{
 					testQueue.putLast((Object)response);
 				}
 			}
 		}catch(InterruptedException e){
 			e.printStackTrace();
 		}
 
 	}
 	
 	/**
 	 * Retrieves the current connected {@code User} from the Server.
 	 * Stores the {@code User} and returns it to the caller
 	 *  
 	 * @return the current connected {@code User}
 	 */
 	public User getClientFullUser() {
 		return (user = getFullUser(auth.getUsername()));
 	}
 	
 	
 	/**
 	 * Retrieves the requested {@code User} from the Server.
 	 * Stores the {@code User} in {@code shows} and returns it to the caller.
 	 * @param username
 	 * @return
 	 */
 	public User getOtherFullUser(String username) {
 		User user = getFullUser(username);
 		
 		//shows.put(user.getUsername(), user);
 		
 		return user;
 	}
 	
 	public User getFullUser(String user){
 		try {
 			Request request = new Request(auth, user);
 			request.setMethod(Request.Method.GET_FULL_USER);
 			send(mySocket, request);
 			int i = 0;
 			while(true){
 				System.out.println("Number of tries: "+i++);
 				Request response = (Request)testQueue.takeFirst();
 				if(response.getMethod() == Request.Method.GET_FULL_USER_RESPONSE){
 					return (User)response.getObject();
 				}
 				else if(response.getMethod() == Request.Method.LOGIN_FAILED){
 					return null;
 				}else {
 					System.out.println("Put back");
 					testQueue.putLast((Object)response);
 				}
 				
 			}
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns the current connected {@code User}
 	 * 
 	 * @return
 	 */
 	public User getUser() {
 		return user;
 	}
 	
 	
 	public int saveMeeting(Meeting meeting){
 		try{
 			Request request = new Request(auth, meeting);
 			request.setMethod(Request.Method.SAVE_MEETING);
 			send(mySocket, request);
 			int i = 0;
 			while(true){
 				System.out.println("Number of tries: "+i++);
 				//This should be a response containing the key
 				Request response = (Request)testQueue.takeFirst();
 				if(response.getMethod() == Request.Method.SAVE_MEETING_RESPONSE){
 					Integer key = (Integer)response.getObject();
 					System.out.println("Got key "+key);
 					return key;
 				}else if(response.getMethod() == Request.Method.LOGIN_FAILED){
 					return -1;
 				}
 				else {
 					System.out.println("Put back");
 					testQueue.putLast((Object)response);
 				}
 			}		
 		}catch(InterruptedException e){
 			e.printStackTrace();
 		}
 		
 		return -1;
 	}
 	
 	
 	public boolean saveAppointment(Appointment appointment){
 		try{
 			Request request = new Request(auth, appointment);
 			request.setMethod(Request.Method.SAVE_APPOINTMENT);
 			//Does saveAppointment return a key
 			send(mySocket, request);
 			int i = 0;
 			while(true){
 				System.out.println("Number of tries ");
 				//This response should contain a key
 				Request response = (Request)testQueue.takeFirst();
 				if(response.getMethod() == Request.Method.SAVE_APPOINTMENT_RESPONSE){
 					Integer id = (Integer)response.getObject();
 					appointment.setID(id);
 					return true;
 				}else if(response.getMethod() == Request.Method.LOGIN_FAILED){
 					return false;
 				}
 				else {
 					System.out.println("Put back");
 					testQueue.putLast((Object)response);
 				}
 			}
 		} catch(InterruptedException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 		
 	public boolean dispatchMeetingReply(User user, Meeting meeting, State state) {
 		try{
 			//Gather information 
 			String userInfo[] = {
 				user.getUsername(),
 				"",
 			};
 			
 			String dataValues[] = {
 				user.getId(),
 				String.valueOf(meeting.getID()),
 				state.toString()
 			};
 			//Pack and send
 			String xml = XmlHandler.dispatchMeetingReplyToXml(userInfo, dataValues, "dispatchMeetingReply");
 			Request request = new Request(auth, xml);
 			request.setMethod(Request.Method.DISPATCH_MEETING_REPLY);
 			send(mySocket, request);
 			int i = 0;
 			//Wait for response
 			while(true){
 				Request response = (Request)testQueue.takeFirst();
 				if(response.getMethod() == Method.SAVE_APPOINTMENT_RESPONSE){
 					return true;
 					
 				}else if(response.getMethod() == Method.LOGIN_FAILED){
 					return false;
 				}else {
 					System.out.println("Put back");
 					testQueue.putLast((Object)response);
 				}
 			}
 		
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 				
 			return false;
 	}
 	
 	/**
 	 * Retrieves the full {@code List} of {@code Room}s once from the server.
 	 * Returns the {@code List}.
 	 * 
 	 * @return
 	 */
 	public List<Room> getListOfRooms() {
 		if (rooms == null) {
 			updateListOfRooms();
 		}
 		return rooms;
 	}
 	
 	public void updateListOfRooms() {
 		try{
 			Request request = new Request(auth, null);
 			request.setMethod(Method.GET_LIST_OF_ROOMS);
 			send(mySocket, request);
 			int i = 0;
 			while(true){
 				Request response = (Request)testQueue.takeFirst();
 				if(response.getMethod() == Method.GET_LIST_OF_ROOMS_RESPONSE){
 					rooms = (List <Room>)response.getObject();
 					return;
 				}else if(response.getMethod() == Method.LOGIN_FAILED){
 					//return null;
 				}else {
 					System.out.println("Put back");
 					testQueue.putLast((Object)response);
 				}
 			}
 		}catch(InterruptedException e){
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/*public List <Room> getListOfRooms(){
 		try{
 			Request request = new Request(auth, null);
 			request.setMethod(Method.GET_LIST_OF_ROOMS);
 			send(mySocket, request);
 			int i = 0;
 			while(true){
 				Request response = (Request)testQueue.takeFirst();
 				if(response.getMethod() == Method.GET_LIST_OF_ROOMS_RESPONSE){
 					return (List <Room>)response.getObject();
 				}else if(response.getMethod() == Method.LOGIN_FAILED){
 					return null;
 				}else {
 					testQueue.putLast((Object)response);
 				}
 			}
 		}catch(InterruptedException e){
 			e.printStackTrace();
 		}
 		
 		return null;
 	}*/
 	
 	
 	public boolean cancelView(String username){
 		try {
 			Request request = new Request(auth, username);
 			request.setMethod(Method.CANCEL_VIEW);
 			int i = 0;
 			while(true){
 				System.out.println("Number of tries: "+i++);
 				Request response = (Request)testQueue.takeFirst();
 				if(response.getMethod() == Method.CANCEL_VIEW_SUCCEDED){
 					return true;
 				}else if(response.getMethod() == Method.LOGIN_FAILED){
 					return false;
 				}else {
 					System.out.println("Put back");
 					testQueue.putLast((Object)response);
 				}
 				
 			}
 		}catch(InterruptedException e){
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	
 	
 	public void deleteMeeting(Meeting meeting){
 		Integer id = meeting.getID();
 		Request request = new Request(auth, id);
 		request.setMethod(Method.DELETE_MEETING);
 		send(mySocket, request);
 	}
 		
 	public void deleteAppointment(Appointment appointment){
 		Integer id = appointment.getID();
 		Request request = new Request(auth, id);
 		request.setMethod(Method.DELETE_APPOINTMENT);
 		send(mySocket, request);
 	}
 	
 	
 	public boolean deleteUser(){
 		return true;
 	}
 	
 
 	
 	/**
 	 * This method is called by the {@code ClientWorker} when a 
 	 * {@code Meeting} update is received from the Server. 
 	 * @param meeting
 	 */
 	public synchronized void updateMeeting(Meeting updatedMeeting) {
 		
 		User owner = updatedMeeting.getOwner();
 		Calendar calendar = user.getCalendar();
 		
 		Meeting meeting = null;
 		
 		if (owner.equals(user)) {
 			for (CalendarEntry entry : user.getCalendar()) {
 				
 				if (entry.getID() == updatedMeeting.getID()) {
 					meeting = (Meeting)entry;
 					
 					continue;
 				}
 			}
 		} else {
 			for (User participant : updatedMeeting.getParticipants()) {
 				if (participant.equals(user)) {
 					
 					for (CalendarEntry entry : participant.getCalendar()) {
 						if (entry.getID() == updatedMeeting.getID()) {
 							meeting = (Meeting) entry;
 							continue;
 						}
 					}
 					
 					if (meeting == null ) {
 						calendar.addMeeting(updatedMeeting);
 					}
 				}
 			}
 		}
 		
 		if (meeting != null) {
 			calendar.addMeeting(updatedMeeting);
 		}
 		
 	}
 
 	public synchronized void updateMeetingState(Meeting updatedMeeting) {
 		
 		User owner = updatedMeeting.getOwner();
 		Calendar calendar = user.getCalendar();
 		
 		Meeting meeting = null;
 		
 		if (owner.equals(user)) {
 			for (CalendarEntry entry : user.getCalendar()) {
 				
 				if (entry.getID() == updatedMeeting.getID()) {
 					meeting = (Meeting)entry;
 					
 					continue;
 				}
 			}
 		} else {
 			for (User participant : updatedMeeting.getParticipants()) {
 				if (participant.equals(user)) {
 					
 					for (CalendarEntry entry : participant.getCalendar()) {
 						if (entry.getID() == updatedMeeting.getID()) {
 							meeting = (Meeting) entry;
 							continue;
 						}
 					}
 				}
 			}
 		}
 		
 		if (meeting != null) {
 			for (User user : meeting.getParticipants()) {
 				State state = meeting.getState(user); 
 				State updatedState = updatedMeeting.getState(user); 
 				
 				if (state != updatedState) {
 					meeting.setState(user, updatedState);
 				}
 			}
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * @param appointment
 	 */
 	public synchronized void updateAppointment(Appointment appointment) {
 		User user = appointment.getOwner();
 		for(User u : shows){
 			if(u.equals(user)){
 				Calendar c = u.getCalendar();
 				c.removeAppointment(appointment);
 				c.addAppointment(appointment);
 			}
 		}
 		
 		
 	}
 
 
 	
 	
 }

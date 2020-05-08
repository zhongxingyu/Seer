 package ktn;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 
 import baseClasses.Alarm;
 import baseClasses.Appointment;
 import baseClasses.Person;
 import baseClasses.Room;
 import baseClasses.Notification;
 
 //TCP client
 public class Client extends Thread{
 	
 	private Socket connection;
 
 	private final static String SERVERIP = "78.91.61.224";
 
 	private final static int SERVERPORT = 4004;
 	
 	private ObjectOutputStream objectOutput;
 	private ObjectInputStream objectInput;
 	
 	
 	
 	public Client() {
 		this.run();
 	}
 	
 	/**
 	 * Connects a client to the server
 	 * @throws InterruptedException 
 	 */
 	public void connect() throws InterruptedException {
 		this.startClient();
 	}
 	
 	/**
 	 * Closes a connection if a connection has been made
 	 */
 	public void close() {
 		this.objectInput = null;
 		this.objectOutput = null;
 		try {
 			this.connection.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		this.connection = null;
 	}
 	
 	/**
 	 * Logs a user in by making a LOGIN request to the server.
 	 * @param keyword String array as {user, password}
 	 * @return
 	 */
 	public Person login(String [] keyword) {
 		//Creates requestObject
 		SendObject reqObj = new SendObject(RequestEnum.LOGIN, keyword);
 		//Sends object to server
 		this.send(reqObj);
 		//Returns object from server
 		SendObject receivedObject = receive();
 		if(!checkObject(RequestEnum.PERSON,receivedObject))
 			try {
 				throw new IOException("Login failed. Wrong object received from server");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		System.out.println("Login object received from server: " + receivedObject);
 		return (Person)receivedObject.getObject();	
 	}
 	
 	/**
 	 * Checks a SendObjects RequestType with the expected RequestType.
 	 * Returns false if the values differ from each other.
 	 * @param value
 	 * @param object
 	 * @return
 	 */
 	private boolean checkObject(RequestEnum value, SendObject object) {
 		if(object.getSendType() == value) {
 			return true;
 		}
 		return false;
 	}
 	/**
 	 * Sends a SendObject to the server trough the open connection
 	 * @param obj
 	 */
 	private void send(SendObject obj) {
 		try {
 			this.objectOutput.writeObject(obj);
 			System.out.println("Sendt");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Receive a SendObject from the server through the open connection.
 	 * The function blocks until the object is received.
 	 * @return
 	 */
 	private SendObject receive() {
 		Object receivedObject = null;
 		try {
 			receivedObject = this.objectInput.readObject();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return (SendObject)receivedObject;
 	}
 	
 	/**
 	 * Fetches all the notification available to the specified user.
 	 * @param username
 	 * @return
 	 */
 	public ArrayList<Notification> fetchNotifications(String username) {
 		String[] keyword = {username};
 		SendObject reqObj = new SendObject(RequestEnum.NOTIFICATION, keyword);
 		this.send(reqObj);
 		SendObject obj = this.receive();
 		if(!checkObject(RequestEnum.NOTIFICATION,obj)) {
 			try {
 				throw new IOException("Request failed. Wrong object received from server");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		ArrayList<Notification> notifications = (ArrayList<Notification>)obj.getObject();
 		return notifications;
 	}
 	
 	/**
 	 * Fetches appointments on a specified date for a user.
 	 * If the date string is empty, the function will return all appointments.
 	 * @param user
 	 * @param date
 	 * @return
 	 */
 	public ArrayList<Appointment> fetchAppointments(String user, String date) {
 		String[] keyword = {user,date};
 		SendObject reqObj = new SendObject(RequestEnum.APPOINTMENT, keyword);
 		this.send(reqObj);
 		SendObject obj = this.receive();
 		if(!checkObject(RequestEnum.APPOINTMENT,obj)) {
 			try {
 				throw new IOException("Request failed. Wrong object received from server");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		ArrayList<Appointment> appointments = (ArrayList<Appointment>)obj.getObject();
 		return appointments;
 	}
 	
 	/**
 	 * Fetches all appointments for a user.
 	 * @param user
 	 * @return
 	 */
 	public ArrayList<Appointment> fetchAllAppointments(String user) {
 		return fetchAppointments(user, "");
 	}
 	
 	/**
 	 * Creates an new Person entry on the server, which can be used to
 	 * add new users to the application.
 	 * @param person
 	 * @return
 	 */
 	public boolean createPerson(String[] keyword) {
 		// Creates requestObject
 		SendObject reqObj = new SendObject(RequestEnum.S_PERSON, keyword);
 		// Sends object to server
 		this.send(reqObj);
 		// Returns object from server
 		SendObject receivedObj = receive();
 		// Return to GUI if the person is registered
 		return (Boolean) receivedObj.getObject();
 	}
 	/**
 	 * Fetches all rooms available the specified date in the time intervall.
 	 * @param date
 	 * @param start
 	 * @param end
 	 * @return
 	 */
 	public ArrayList<Room> fetchRooms(String date,String start, String end) { 
 		// hh:mm - YYYY-MM-DD
 		String[] keyword = {date, start, end};
 		SendObject reqObj = new SendObject(RequestEnum.ROOM, keyword);
 		this.send(reqObj);
 		SendObject obj = this.receive();
 		
 		
 		if(!checkObject(RequestEnum.ROOM, obj)) {
 			try {
 				throw new IOException("Request failed. Wrong object received from server");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		ArrayList<Room> rooms = (ArrayList<Room>)obj.getObject();
 		return rooms;
 	}
 	
 	/**
 	 * Fetches all persons available in the database based on
 	 * the searchword given.
 	 * @param searchWord
 	 * @return
 	 */
 	public ArrayList<Person> fetchPersons(String searchWord) {
		System.out.println("Gui did this.");
 		String[] keyword = {searchWord};
 		SendObject reqObj = new SendObject(RequestEnum.PERSON, keyword);
 		this.send(reqObj);
 		SendObject obj = this.receive();
 		if(!checkObject(RequestEnum.PERSON, obj)) {
 			try {
 				throw new IOException("Request failed. Retry query from server");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		ArrayList<Person> persons = (ArrayList<Person>)obj.getObject();
 		return persons;
 	}
 	
 	/**
 	 * Fetches all available Alarms to the user on the specified date.
 	 * @param date
 	 * @param user
 	 * @return
 	 */
 	public ArrayList<Alarm> fetchAlarms(String user, String date) {
 		String[] keyword = {user, date};
 		SendObject reqObj = new SendObject(RequestEnum.ALARM, keyword);
 		this.send(reqObj);
 		SendObject obj = this.receive();
 		if(!checkObject(RequestEnum.ALARM, obj)) {
 			try {
 				throw new IOException("Login failed. Wrong object received from server");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		ArrayList<Alarm> alarms = (ArrayList<Alarm>)obj.getObject();
 		return alarms;
 	}
 
 	/**
 	 * Creates an Appointment entry on the server.
 	 * Admin is stored in Appointment, server gets 
 	 * the username from this field.
 	 * @param app
 	 * @return
 	 */
 	public boolean createAppointment(Appointment app) {
 		if(app.getRoom() == null) {
 			System.out.println("Cannot create appointment with null room.");
 			return false;
 		}
 		// Creates requestObject
 		SendObject reqObj = new SendObject(RequestEnum.S_APPOINTMENT, app);
 		//Sens object to server
 		this.send(reqObj);
 		//Return object from server
 		SendObject receivedObj = receive();
 		//Return to GUI if the appointment is registered
 		return (Boolean)receivedObj.getObject();
 	}
 
 	/**
 	 * Changes an exisiting entry on the server with the updated information.
 	 * @param oldApp
 	 * @param newApp
 	 * @return
 	 */
 	public boolean changeAppointment(Appointment oldApp, Appointment newApp) {
 		ArrayList<Appointment> changeList = new ArrayList<Appointment>();
 		changeList.add(newApp);
 		changeList.add(oldApp);
 		SendObject sendObj = new SendObject(RequestEnum.C_APPOINTMENT, changeList);
 		this.send(sendObj);
 		SendObject receivedObj = this.receive();
 		if(!checkObject(RequestEnum.BOOLEAN,receivedObj))
 			try {
 				throw new IOException("Creating Appointment failed. Wrong object received from server");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		Boolean bol = (Boolean)receivedObj.getObject();
 		return bol.booleanValue();	
 	}
 	
 	public boolean sendAccept(String user, Appointment app) {
 		String[] keyword = {user};
 		// Creates "answer" Object
 		SendObject ansObj = new SendObject(RequestEnum.ACCEPT, keyword, app, true);
 		// Sends answer to server
 		this.send(ansObj);
 		SendObject receivedObj = receive();
 		return (Boolean)receivedObj.getObject();
 	}
 	
 	public boolean sendDecline(String user, Appointment app) {
 		String[] keyword = {user};
 		// Creates "answer" Object
 		SendObject ansObj = new SendObject(RequestEnum.ACCEPT, keyword, app, false);
 		// Sends answer to server
 		this.send(ansObj);
 		SendObject receivedObj = receive();
 		return (Boolean)receivedObj.getObject();
 	}
 	
 	/**
 	 * Internal function to start client
 	 */
 	private void startClient() {
 		try {
 			// Create a TCP connection to Server/Client
 			connection = new Socket(InetAddress.getByName(Client.SERVERIP), Client.SERVERPORT);
 			// Connection established
 			System.out.println("Connected to server");
 			//Acquire the InputStream from Socket
 			InputStream serverInputStream = connection.getInputStream();
 			// Creating InputSteamReader from the InputStream
 			InputStreamReader inFromServer = new InputStreamReader(serverInputStream);
 			// Creating ObjectInputStream from InputStream
 			this.objectInput = new ObjectInputStream(serverInputStream);
 			// Acquiring OutputStream from connection
 			OutputStream serverOutputStream = connection.getOutputStream();
 			// Creating ObjectOutputStream from OutputStream
 			this.objectOutput = new ObjectOutputStream(serverOutputStream);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void main(String[] args) throws InterruptedException {
 		new Client().connect();
 	}
 }

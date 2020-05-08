 package ktn;
 
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.ArrayList;
 
 import baseClasses.Appointment;
 import baseClasses.Notification;
 import baseClasses.Person;
 import baseClasses.Room;
 
 import database.Database;
 
 public class Server {
 	
 	private final static String SERVERIP = "78.91.61.224";
 
 	private final static int SERVERPORT = 4004;
 	private Database database;
 
 	public Server() {
 		
 	}
 
 	public void startServer() {
 		try {
 			// Creating a ServerSocket: Is not used further
 			ServerSocket serverSocket = new ServerSocket(this.SERVERPORT, 50, InetAddress.getByName(this.SERVERIP));
 			// Printing IP:Port for the server
 			System.out.println("Waiting for connections on " + this.SERVERIP + " : " + this.SERVERPORT);
 			
 			
 			// Establishing connection to database
 			try {
 				this.database = new Database();
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.out.println("ERROR while connecting to database - ktnSTYLE");
 			}
 			// A never-ending while-loop that constantly listens to the Socket
 			Socket newConnectionSocket;
 			while (true) {
 				// Accepts a in-coming connection
 				newConnectionSocket = serverSocket.accept();
 				// System message
 				System.out.println("New connection.");
 				// Delegating the further connection with the client to a thread class
 				new ClientConnection(newConnectionSocket).start();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 
 	// Thread class for handling further connection with server when connection is established
 	class ClientConnection extends Thread {
 		private Socket connection;
 		private ObjectOutputStream objectOut;
 		private ObjectInputStream objectIn;
 		ClientConnection(Socket connection) {
 			this.connection = connection;
 		}
 		private void send(SendObject obj) {
 			try {
 				this.objectOut.writeObject(obj);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		public void run() {
 			System.out.println("Connected to client on " + this.connection.getRemoteSocketAddress());
 			InputStream clientInputStream;
 			try {
 				// Fetches InputStream from connection
 				clientInputStream = this.connection.getInputStream();
 				// Fetches OutputStream from connect
 				OutputStream clientOutputStream = this.connection.getOutputStream();
 				// Create InputStreamReader for InputStream
 				InputStreamReader inFromClient = new InputStreamReader(clientInputStream);
 				
 				// Create ObjectOutputStream
 				objectOut = new ObjectOutputStream(clientOutputStream);
 				//Create InputObjectStream
 				objectIn = new ObjectInputStream(clientInputStream);
 				
 				System.out.println("Waiting for message from client");
 				
 				// While-loop to ensure continuation of reading in-coming messages
				while (true) {

 					try {
 						//Receive object from client
 						SendObject obj =(SendObject) this.objectIn.readObject();
 						//Sends object to databsefunction and return result-object
 						SendObject dbObj = databaseQuery(obj); 
 						//Sends object back to client
 						send(dbObj);
 					} catch (ClassNotFoundException e) {
 						e.printStackTrace();
 					}
 					catch (SocketException e) {
 					}
 					
 
 				}
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 
 		}
 
 	}
 	
 	SendObject databaseQuery(SendObject obj) {
 		String[] keyword;
 		RequestEnum reType;
 		keyword = obj.getKeyword();
 		reType = obj.getSendType();
 		// This is the object that the server sends.
 		SendObject sObject;
 		Boolean bool;
 		if(obj.isRequest()){
 			switch(reType) {
 			case LOGIN:
 				System.out.println("Login requested for user: " + keyword[0]);
 				Person person = database.login(keyword);
 				sObject = new SendObject(RequestEnum.PERSON, person);
 				System.out.println("Confirmation for login sent.");
 				return sObject;
 			case PERSON:
 				System.out.println("Persons requested.");
 				ArrayList<Person> persons = database.getPerson(keyword);
 				sObject = new SendObject(RequestEnum.PERSON, persons);
 				System.out.println("Persons sent.");
 				return sObject;
 			case APPOINTMENT:
 				System.out.println("Appointment requested.");
 				ArrayList<Appointment> appointments;
 				
 				/*
 				 * If string is empty, then all available appointments shall be delivered.
 				 * Used inside change Appointments panel in gui.
 				 */
 				if(keyword[1] == "") {
 					appointments = database.getAppointmentsOnPerson(keyword);
 					sObject = new SendObject(RequestEnum.APPOINTMENT, appointments);
 				}
 				/*
 				 * If string is not empty, appointments a specific date is received.
 				 * This one is used to get appointments for each row in a day view
 				 * in calender.
 				 */
 				 else {
 					 appointments = database.getAppointmentsOnPerson(keyword);
 					 sObject = new SendObject(RequestEnum.APPOINTMENT, appointments);
 				}
 				System.out.println("Appointments sent.");
 				return sObject;
 			case S_PERSON:
 				System.out.println("Registration for new user requested.");
 				bool = database.registerUser(keyword);
 				sObject = new SendObject(RequestEnum.BOOLEAN,bool);
 				System.out.println("Confirmation for reqistration sent.");
 				return sObject;
 			case ROOM:
 				System.out.println("Request for rooms.");
 				System.out.println();
 				ArrayList<Room> rooms= database.getRoomOnTime(keyword);
 				System.out.println("Rooms sent.");
 				sObject = new SendObject(RequestEnum.ROOM,rooms);
 				return sObject;
 			case NOTIFICATION:
 				System.out.println("Requst for notification for " + keyword[0]);
 				ArrayList<Notification> not = database.getNotification(keyword[0]);
 				System.out.println("Notifications sent.");
 				sObject = new SendObject(RequestEnum.NOTIFICATION, not);
 				return sObject;
 			default:
 				break;
 			}
 		}
 		else{
 			switch (reType) {
 			case S_APPOINTMENT:
 				System.out.println("Requests for storing an appointment.");
 				bool = database.createAppointment((Appointment)obj.getObject());
 				sObject = new SendObject(RequestEnum.BOOLEAN,bool);
 				createNotificationForAll((Appointment)obj.getObject(),false);
 				System.out.println("Confirmation sent and notifications created.");
 				return sObject;
 			case C_APPOINTMENT: // Received old and new appointment - ArrayList<Appointment> = {new,old}
 				System.out.println("Request for changing an appointment.");
 				ArrayList<Appointment> apps = (ArrayList<Appointment>)obj.getObject();
 				bool = database.changeAppointment(apps.get(0),apps.get(1));
 				createNotificationForAll(apps.get(1),true);
 				System.out.println("Old appointment changed and notification created.");
 				sObject = new SendObject(RequestEnum.BOOLEAN,bool);
 				return sObject;
 			case ACCEPT:
 				System.out.println("Answer on a notifiction received.");
 				Appointment app = (Appointment)obj.getObject();
 				bool = database.agreedAppointment(keyword[0], app, obj.getBoolean());
 				database.deleteNotification(new String[] {keyword[0],app.getAdmin()}, app);
 				if(!obj.getBoolean()){
 					database.createNotification(new String[] {"DECLINED",keyword[0],app.getAdmin()}, app);
 				}
 				sObject = new SendObject(RequestEnum.BOOLEAN, bool);
 				return sObject;
 			default:
 				break;
 			}
 		}
 		return null;
 		
 		
 	}
 	/**
 	 * Used to create notifications for all users when someone creates an appointment for them.
 	 * Input boolean alter is used to indicate if the appointment already exists or is a new one.
 	 * @param appointment
 	 * @param alter
 	 * @throws Exception 
 	 */
 	private void createNotificationForAll(Appointment appointment, boolean alter){
 		// IF AN APPOINTMENT CHANGE HAS BEEN MADE
 		String admin = appointment.getAdmin();
 		if(alter) {
 			for(Person person : appointment.getParticipants()) {
 				String[] keyword = {"INVITATION",person.getUsername(),admin};
 				database.deleteNotification(new String[] {person.getUsername(),admin}, appointment);
 				database.createNotification(keyword, appointment);
 			}
 			
 			
 			
 		// IF AN NEW APPOINTMENT HAS BEEN CREATED
 		} else {
 			for(Person person : appointment.getParticipants()) {
 				String[] keyword = {"INVITATION",person.getUsername(),admin};
 				database.createPersonAppointment(person.getUsername(), appointment);
 				database.createNotification(keyword, appointment);
 			}
 		}
 	}
 	
 
 
 	// Main-function to start server
 	public static void main(String[] args) {
 		new Server().startServer();
 	}
 }

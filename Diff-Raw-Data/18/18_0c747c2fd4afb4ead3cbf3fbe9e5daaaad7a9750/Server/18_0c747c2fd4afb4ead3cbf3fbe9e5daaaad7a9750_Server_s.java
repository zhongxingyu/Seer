 package gruppe19.server.ktn;
 
 import gruppe19.client.ktn.ClientMessage;
 import gruppe19.client.ktn.ServerAPI;
 import gruppe19.model.Appointment;
 import gruppe19.model.User;
 import gruppe19.server.db.DatabaseAPI;
 import gruppe19.server.ktn.ServerMessage.Type;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InvalidClassException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.BindException;
 import java.net.ConnectException;
 import java.net.SocketTimeoutException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.codec.binary.Base64;
 
 import no.ntnu.fp.net.admin.Settings;
 import no.ntnu.fp.net.co.Connection;
 import no.ntnu.fp.net.co.ConnectionImpl;
 import no.ntnu.fp.net.co.SimpleConnection;
 
 /**
  * A static class accepting incoming connections and requests.
  */
 public class Server {
 	private static List<Client> clients = new ArrayList<Client>();
 	private static Connection server = null;
 	private static Base64 stringEncoder = new Base64();
 	
 	/**
 	 * When true, the system will accept any user name / password
 	 * combination and simply return a new user with the specified
 	 * user name.
 	 */
 	public final static boolean noLogin = true;
 	public final static String serverAddress;
 	public final static int listeningPort;
 	public final static boolean usingSimpleConn;
 	
 	//Parse the settings file
 	static {
 		Settings s = new Settings();
 		
 		serverAddress = s.getServerAddress();
 		listeningPort = s.getServerPort();
 		usingSimpleConn = s.useSimpleConnection();
 	}
 	
 	/**
 	 * A class saving information about each connected client.
 	 */
 	private static class Client {
 		private RecieveThread recieveThread;
 		private Connection conn;
 		private User connectedUser;
 
 		public Client(Connection conn) {
 			this.conn = conn;
 			connectedUser = null;
 			recieveThread = new RecieveThread();
 			recieveThread.start();
 		}
 
 		/**
 		 * A thread waiting for incoming messages.
 		 */
 		private class RecieveThread extends Thread {
 			public boolean run = true;
 
 			public void run() {
 				while (run) {
 					try {
 						ObjectInputStream byteStream = 
 								new ObjectInputStream(
 										new ByteArrayInputStream(
 												stringEncoder.decode(
 														conn.receive())));
 						
 						handleMessage((ClientMessage)byteStream.readObject());
 					} 
 					catch (ConnectException e) {
 						e.printStackTrace();
 					}
 					catch (InvalidClassException e) {
 						System.err.println("[Error] Failed to convert the received" +
 								" byte stream to ClientMessage.");
 						e.printStackTrace();
 					}
 					catch (ClassNotFoundException e) {
 						System.err.println("[Error] Failed to convert the received" +
 								" byte stream to ClientMessage.");
 						e.printStackTrace();
 					}
 					catch (Exception e) {
 						//Client requested disconnect
 						run = false;
 						
 						try {
 							conn.close();
 						} 
 						catch (IOException ioe) {
 							System.err.println(
 								"[Error] Server: IOException while"
 								+ "closing connection: "
 								+ ioe.getMessage());
 						}
 					} 
 				}
 			}
 		}
 
 		/**
 		 * Maps a request from the client to the appropriate method.
 		 * 
 		 * @param request The request to handle.
 		 * @throws SQLException
 		 */
 		private void handleMessage(ClientMessage msg) throws SQLException {
 			if (msg == null) {
 				return;
 			}
 			
 			switch (msg.ID) {
 			case 'a': {
 				String loginInfo = (String)msg.payload;
 				User user = null;
 				int i = loginInfo.indexOf(0);
 
 				if (noLogin) {
 					//Ignore password and return a new user with the specified user name
 					user = new User(loginInfo.substring(0, i), 
 										"Ola", "Nordmann", 64545453, "");
 				}
 				else {
 					//Send a new user object if valid password and user name, null if not
 					user = DatabaseAPI.logIn(loginInfo.substring(0, i),
 												loginInfo.substring(i + 1));
 				}
 				send(new ServerMessage('\0', user, Type.Response));
 				connectedUser = user;
 				break;
 			}
 			case 'b':
 			{
 				/* Create a new appointment in the database
 				 * Notify participants
 				 * Return an appointment object with the correct ID
 				 */
 				Appointment a = DatabaseAPI.createAppointment((Appointment)msg.payload);
 				
 				for (User u : a.getUserList().keySet()) {
 					for (Client c : clients) {
 						if (u.getUsername().equals(c.connectedUser.getUsername())) {
 							c.send(new ServerMessage('a', a, Type.Request));
 						}
 					}
 				}
 				send(new ServerMessage('\0', a, Type.Response));
 				break;
 			}
 			case 'c': {
 				/* Update an existing appointment
 				 * Notify participants
 				 */
 				Appointment a = DatabaseAPI.updateAppointment((Appointment)msg.payload);
 				
 				for (User u : a.getUserList().keySet()) {
 					for (Client c : clients) {
 						if (u.getUsername().equals(c.connectedUser.getUsername())) {
 							c.send(new ServerMessage('a', a, Type.Request));
 						}
 					}
 				}
 				break;
 			}
 			case 'd': {
 				/* Delete the appointment
 				 * Notify participants
 				 */
 				Appointment a = (Appointment)msg.payload;
 				DatabaseAPI.removeAppointment(a);
 				
 				for (User u : a.getUserList().keySet()) {
 					for (Client c : clients) {
 						if (u.getUsername().equals(c.connectedUser.getUsername())) {
 							c.send(new ServerMessage('b', a, Type.Request));
 						}
 					}
 				}
 				break;
 			}
 			case 'e': {
 				//Notify appointment leader that the specified user deleted the appointment
 				
 
 				break;
 			}
 			case 'f': {
 				/* Change status
 				 * Send notification to all participants
 				 */
 				
 				break;
 			}
 			case 'g': {
 				
 				
 				break;
 			}
 			case 'h': {
 				//Send all appointments started by the specified user
 				send(new ServerMessage('\0',
 						DatabaseAPI.findAppointments((User)msg.payload), 
 						Type.Response));
 				break;
 			}
 			case 'i': {
 				//Send all appointments where specified user is a participant
 				send(new ServerMessage('\0',
 						DatabaseAPI.findAppointmentsParticipant((User)msg.payload),
 						Type.Response));
 				break;
 			}
 			case 'j': {
 				//Send all appointments
 
 				break;
 			}
 			case 'k': {
 				//Send all free rooms between the specified start and end dates
				Date[] dates = (Date[])msg.payload;
 				
 				send(new ServerMessage('\0',
 						DatabaseAPI.getFreeRooms(
								dates[0],
								dates[1]),
 						Type.Response));
 				break;
 			}
 			case 'l': {
 				//Send all rooms
 				send(new ServerMessage('\0',
 						DatabaseAPI.getRooms(),
 						Type.Response));
 				break;
 			}
 			case 'm': {
 				/* Send a new user object matching the specified user name
 				 * Send null if no match
 				 */
 				send(new ServerMessage('\0',
 						DatabaseAPI.getUser((String)msg.payload),
 						Type.Response));
 				break;
 			}
 			case 'n': {
 				//Send all users
 				send(new ServerMessage('\0',
 						DatabaseAPI.getUsers(),
 						Type.Response));
 				break;
 			}
 			case 'o':
 				
 				break;
 			
 			case 'p':
 				
 				break;
 			case 'q':
 				
 				break;
 			case 'r':
 				
 				break;
 			case 's':
 				
 				break;
 			
 			case 't':
 				
 				break;
 			
 			case 'u':
 				
 				break;
 			case 'v':
 				
 				break;
 			
 			case 'w':
 				
 				break;
 			default:
 				//Invalid request. Ignore
 			}
 		}
 
 		/**
 		 * Sends a <code>ServerMessage</code> to the client.
 		 * 
 		 * @param msg The <code>ServerMessage</code> to send.
 		 */
 		private void send(ServerMessage msg) {
 			try {
 		        ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		        ObjectOutputStream oos = new ObjectOutputStream(baos);
 		        oos.writeObject(msg);
 		        oos.close();
 				conn.send(stringEncoder.encodeToString(baos.toByteArray()));
 			} 
 			catch (Exception e) {
 				System.err.println("[Error] Failed to send object to client");
 				e.printStackTrace();
 			}
 		}
 	}//End of Client class
 	
 	/**
 	 * Sends a message to all clients connected to this server.
 	 * 
 	 * @param msg The message to broadcast.
 	 */
 	public static synchronized void broadcast(String msg) {
 		for (Client c : clients) {
 			c.send(new ServerMessage('\0', msg, Type.BroadCast));
 		}
 	}
 	
 	/**
 	 * Causes the server to start listening for incoming connections on
 	 * the port specified in the settings file and opens a connection
 	 * to the database.
 	 */
 	public static void startServer() {
 		if (server != null) {
 			System.err.println("[Error] Tried to start an already started server.");
 			return;
 		}
 		
 		if (usingSimpleConn) {
 			server = new SimpleConnection(listeningPort);
 		}
 		else {
 			server = new ConnectionImpl(listeningPort);
 		}
 		DatabaseAPI.open();
 		
 		//Start listening
 		new Thread() {
 			private Connection newConn;
 
 			public void run() {
 				while (true) {
 					try {
 						newConn = server.accept();
 						System.out.println("[Debug] Handling incoming connection...");
 						clients.add(new Client(newConn));
 					} 
 					catch (SocketTimeoutException e) {
 						System.err.println("[Error] Socket timed out while" +
 								" waiting for new connections");
 						e.printStackTrace();
 					} 
 					catch (BindException e) {
 						System.err.println("[Error] A server is already listening" +
 								" on this IP and port. Exiting...");
 						System.exit(1);
 					}
 					catch (IOException e) {
 						System.err.println("[Error] IOException occurred while" +
 								" waiting for new connections");
 						e.printStackTrace();
 					}
 				}
 			}
 		}.start();
 	}
 	
 	public static void main(String[] args) {
 		startServer();
 	}
 }

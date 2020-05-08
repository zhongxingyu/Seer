 package client;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.text.DateFormat;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import server.ModelEnvelope;
 import client.gui.exceptions.BadLoginException;
 import client.model.ActiveUserModel;
 import client.model.InvitationModel;
 import client.model.MeetingModel;
 import client.model.NotificationModel;
 import client.model.TransferableModel;
 import client.model.UserModel;
 
 /**
  * The clients interface to the remote calendar server
  * 
  * @author Runar B. Olsen <runar.b.olsen@gmail.com>
  */
 public class ServerConnection extends AbstractConnection {
 	
 	// Stores listeners interested in server connection changes
 	private static final Set<IServerConnectionListener> serverConnectionListeners 
 							= Collections.synchronizedSet(new HashSet<IServerConnectionListener>());
 	
 	
 	private static Logger LOGGER = Logger.getLogger("ServerConnection");
 	private static ServerConnection instance = null;	
 	
 	// Timeout in ms before one expect a store to have failed
 	private static final int STORE_WAIT_TIMEOUT = 3000;
 	
 	private ActiveUserModel user;
 	private ReaderThread readerThread;	
 	private int nextRequestId = 1;
 	
 	// Stores listeners while we wait for the server to respond
 	private Map<Integer, IServerResponseListener> listeners;
 	
 	// Stores models or exceptions that come back after a store() call
 	private Map<Integer, Object> storedModels;
 		
 	
 	/**
 	 * Attempt to login
 	 * 
 	 * If successfull a ServerConnection instance will be accessible from
 	 * instance();
 	 * 
 	 * @param address
 	 * @param port
 	 * @param username
 	 * @param password
 	 * @throws IOException
 	 */
 	public static boolean login(InetAddress address, int port, 
 			String username, String password) throws IOException {
 		
 		instance = new ServerConnection(address, port, username, password);
 		fireServerConnectionChange(IServerConnectionListener.LOGIN);
 		return true;
 	}
 	
 	/**
 	 * Logout the currently logged in user
 	 * 
 	 * @return
 	 */
 	public static boolean logout() {
 		fireServerConnectionChange(IServerConnectionListener.LOGOUT);
 		ClientMain.setActiveUser(null);
 		if(instance != null) {
 			try {
 				instance.writeLine(instance.formatCommand(0, "LOGOUT"));
 			} catch(IOException e) {
 				// Ignore
 			} finally {
 				instance = null;
 			}			
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * @return are we online?
 	 */
 	public static boolean isOnline() {
 		return instance != null;
 	}
 	
 	/**
 	 * Get singleton instance
 	 * 
 	 * @return
 	 */
 	public static ServerConnection instance() {
 		return instance;
 	}
 	
 
 	/**
 	 * Create a new ServerConnection and attempt to preform a login
 	 * 
 	 * @param address
 	 * @param port
 	 * @param username
 	 * @param password
 	 * @throws IOException On reading errors
 	 * @throws InvalidArgumentException or username/password errors
 	 */
 	private ServerConnection(InetAddress address, int port, 
 			String username, String password) throws IOException {
 		
 		super();		
 		listeners = Collections.synchronizedMap(
 				new HashMap<Integer, IServerResponseListener>()
 			);
 		storedModels = Collections.synchronizedMap(
 				new HashMap<Integer, Object>()
 			);
 			
 		
 		try {
 			socket = new Socket(address, port);
 			
 			reader = new DebugReader(new InputStreamReader(socket.getInputStream()));
 			//reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
 			//writer = new DebugWriter(new OutputStreamWriter(socket.getOutputStream()));
 			
 			LOGGER.info(reader.readLine()); // Read welcome message
 			
 			writeLine(String.format("LOGIN %s %s", username, password));			
 			String line = reader.readLine();
 			if(!line.startsWith("OK")) {
 				throw new BadLoginException();
 			}
 			
 			reader.readLine();
 			user = (ActiveUserModel) readModels().get(0);
 			ClientMain.setActiveUser(user);
 			
 			// Start a reader thread and return
 			readerThread = new ReaderThread();
 			readerThread.start();
 		} catch(IOException e) {
 			throw e;
 		}
 		
 	}
 		
 	/**
 	 * Private reader thread
 	 *
 	 */
 	class ReaderThread extends Thread {		
 		@Override
 		public void run() {
 			try {
 				String line;
 				while((line = reader.readLine()) != null) {
 					LOGGER.info(line);
 					
 					String[] parts = line.split("\\s+");
 					
 					// All server responses should contain a id and a method name
 					if(parts.length < 2) {
 						LOGGER.severe("Maformed server response: "+line);
 						continue;
 					}
 					
 					int id = Integer.parseInt(parts[0]);
 					String method = parts[1];
 					
 					
 					// Stored models are saved
 					if(method.equals("STORE") && parts.length > 2) {
 						if(parts[2].equals("ERROR")) 
 							storedModels.put(id, new IOException(reader.readLine()));
 						else
 							storedModels.put(id, readModels().get(0));							
 						continue;
 					} 
 					
 					if(method.equals("DELETE")) {
 						continue;
 					}
 					
 					List<TransferableModel> models = readModels();
 					
 					// Broadcasts come with a zero id
 					if(id == 0 && method.equals("BROADCAST")) {
 						TransferableModel model = models.get(0);
 						
 						if(model instanceof NotificationModel) {
 							user.addNotification((NotificationModel)model);
 						}
 						continue;
 					}
 					
 					// All other models are passed to their listeners
 					IServerResponseListener listener = listeners.get(id);
 					if(listener == null) {
 						LOGGER.severe("No listener registered for response "+line);						
 					} else {
 						listener.onServerResponse(id, models);
 					}	
 				}
 			} catch(IOException e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					socket.close();
 				} catch(IOException e) {}
 				instance = null;
 			}
 		}
 		
 	}
 	
 	/**
 	 * Request all meetings within a given time period from this users calendar
 	 * 
 	 * @return request id
 	 */
 	public int requestMeetings(
 			IServerResponseListener listener, Calendar startDate, Calendar endDate) {
 		return requestMeetings(listener, new UserModel[]{ClientMain.getActiveUser()}, startDate, endDate);
 	}
 	
 	/**
 	 * Request all meetings within a given time period from the given users calendars
 	 * 
 	 * @param listener
 	 * @return request id
 	 */
 	public int requestMeetings(
 			IServerResponseListener listener, UserModel[] users, Calendar startDate, 
 			Calendar endDate) {
 		
 		int id = ++nextRequestId;
 		
 		StringBuilder ul = new StringBuilder();
 		for(UserModel u : users) {
 			ul.append(",");
 			ul.append(u.getUsername());
 		}
 		
 		try {
 			DateFormat df = DateFormat.getDateTimeInstance();
 			
 			listeners.put(id, listener);			
 			writeLine(formatCommand(id, "REQUEST",  "MEETING_LIST"));
 			writeLine(df.format(startDate.getTime()));
 			writeLine(df.format(endDate.getTime()));
 			writeLine(ul.toString().substring(1));
 			writeLine("");
 		} catch(IOException e) {
 			listeners.remove(id);
 			LOGGER.severe("IOException requestMeetings");
 			LOGGER.severe(e.toString());
 			return -1;
 		}
 			
 		return id;
 		
 	}
 	
 	/**
 	 * Request a meeting based on id
 	 * 
 	 * @param listener
 	 * @param id
 	 * @return
 	 */
 	public int requestMeeting(IServerResponseListener listener, int mid) {
 		int id = ++nextRequestId;
 		
 		try {
 			listeners.put(id, listener);
 			writeLine(formatCommand(id, "REQUEST",  "MEETING "+mid));
 		} catch(IOException e) {
 			listeners.remove(id);
 			LOGGER.severe("IOException requestMeeting");
 			LOGGER.severe(e.toString());
 			return -1;
 		}			
 		return id;
 	}
 	
 	/**
 	 * Stores the given model on the remote server
 	 * 
 	 * The returned model will be equal to the given with any changes that was
 	 * caused on the server as a result to the store call.
 	 * 
 	 * <code>
 	 * ServerConnection server = ...;
 	 * Model model = ...;
 	 * 
 	 * model = server.storeModel(model);
 	 * 
 	 * // Returned model should be used here after 
 	 * </code>
 	 * 
 	 * @param model
 	 * @throws IOException on store error
 	 * @return
 	 */
 	public TransferableModel storeModel(TransferableModel model) throws IOException {
 		int id = ++nextRequestId;
 		Object stored = null;
 		try {
 			writeModels(Arrays.asList(model), id, "STORE");
 			
 			//long time = System.currentTimeMillis();
 			
 			// Updated model will come in reader thread, halt untill it's there
 			while(!storedModels.containsKey(id) 
 					/*&& (System.currentTimeMillis() - time) < STORE_WAIT_TIMEOUT*/) {				
 				try {
 					Thread.sleep(100);
 				} catch(InterruptedException e) {}
 			}
 			stored = storedModels.remove(id);
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 		
 		if(stored instanceof IOException) {
 			throw (IOException) stored;
 		}
 		
 		if(stored instanceof TransferableModel) {
 			// TODO Have mode copy the contents of stored, free stored and cache model
 			return (TransferableModel) stored;
 		}
 		return null;
 	}
 	
 	/**
 	 * Request a list of all users filtered on the given filter
 	 * 
 	 * @param listener
 	 * @param filter
 	 * @return
 	 */
 	public int requestFilteredUserList(IServerResponseListener listener, String filter) {
 		int id = ++nextRequestId;
 				
 		try {
 			listeners.put(id, listener);
 			writeLine(formatCommand(id, "REQUEST",  "FILTERED_USERLIST "+filter));
 		} catch(IOException e) {
 			listeners.remove(id);
 			LOGGER.severe("IOException requestFilteredUserList");
 			LOGGER.severe(e.toString());
 			return -1;
 		}
 			
 		return id;
 	}
 	
 	
 	/**
 	 * Request a list of available meeting rooms within the given time period
 	 * 
 	 * @param listener
 	 * @param from
 	 * @param to
 	 * @return
 	 */
 	public int requestAvailableRooms(IServerResponseListener listener, Calendar from, Calendar to) {
 		int id = ++nextRequestId;
 		
 		try {
 			DateFormat df = DateFormat.getDateTimeInstance();
 			
 			listeners.put(id, listener);
 			writeLine(formatCommand(id, "REQUEST",  "AVAILABLE_ROOMS"));
 			writeLine(df.format(from.getTime()));
 			writeLine(df.format(to.getTime()));
 			writeLine("");
 			
 		} catch(IOException e) {
 			listeners.remove(id);
 			LOGGER.severe("IOException requestFilteredUserList");
 			LOGGER.severe(e.toString());
 			return -1;
 		}
 		
 		return id;
 	}
 	
 	/**
 	 * Delete a meeting
 	 * 
 	 * @param meeting
 	 * @return
 	 */
 	public int deleteMeeting(MeetingModel meeting) {
 		int id = ++nextRequestId;
 		
 		try {
 			writeLine(formatCommand(id, "DELETE", "MEETING "+meeting.getId()));			
 		} catch(IOException e) {
 			listeners.remove(id);
 			LOGGER.severe("IOException deleteMeeting");
 			LOGGER.severe(e.toString());
 			return -1;
 		}
 		return id;
 	}
 	
 	/**
 	 * Delete a invitation
 	 * 
 	 * @param invitation
 	 * @return
 	 */
 	public int deleteInvitation(InvitationModel invitation) {
 		int id = ++nextRequestId;
 		
 		try {
 			writeLine(formatCommand(id, "DELETE", "INVITATION "+invitation.getUser().getUsername()+
 					" "+invitation.getMeeting().getId()));			
 			
 		} catch(IOException e) {
 			listeners.remove(id);
 			LOGGER.severe("IOException deleteInvitations");
 			LOGGER.severe(e.toString());
 			return -1;
 		}
 		return id;
 	}
 	
 	/**
 	 * Add server connection listener
 	 * 
 	 * @param listener
 	 */
 	public static void addServerConnectionListener(IServerConnectionListener listener) {
 		serverConnectionListeners.add(listener);
 	}
 	
 	/**
 	 * Remove the server connection listener
 	 * 
 	 * @param listener
 	 */
 	public static void removeServerConnectionListener(IServerConnectionListener listener) {
 		serverConnectionListeners.remove(listener);
 	}
 	
 	/**
 	 * Fire a server connection change event
 	 * 
 	 * @param change
 	 */
 	private static  void fireServerConnectionChange(String change) {
 		for (IServerConnectionListener listener : serverConnectionListeners)
 			listener.serverConnectionChange(change);
 	}	
 	
 	/**
 	 * Read models off stream
 	 */
 	@Override
 	protected List<TransferableModel> readModels() throws IOException {
 		ModelEnvelope envelope = new ModelEnvelope(reader, false);
 		return envelope.getModels();
 	}
 	
 	public static void main(String[] args) throws Exception {
 		ServerConnection.login(InetAddress.getLocalHost(), 9034, "runar", "runar");
 		
		Calendar from = Calendar.getInstance();
		from.set(2012, 1, 1);
		Calendar to = Calendar.getInstance();
		to.set(2013, 1, 1);
		
		ServerConnection.instance().requestMeetings(new Listener(), from, to);
 	}
 }
 
 
 class Listener implements IServerResponseListener {
 
 	@Override
 	public void onServerResponse(int requestId, Object data) {
 		MeetingModel mm = ((List<MeetingModel>) data).get(0);
 		System.out.println(mm.getName());
 		
 		System.out.println(mm.getInvitation(ClientMain.getActiveUser()));
		
 	}
 	
 }

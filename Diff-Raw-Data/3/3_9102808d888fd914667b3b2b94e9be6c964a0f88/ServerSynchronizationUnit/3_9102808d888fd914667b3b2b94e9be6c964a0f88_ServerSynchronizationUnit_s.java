 package synclogic;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.SocketTimeoutException;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.Timer;
 
 import model.Appointment;
 import model.Invitation;
 import model.InvitationStatus;
 import model.Meeting;
 import model.Notification;
 import model.NotificationType;
 import model.Room;
 import model.SaveableClass;
 import model.User;
 import no.ntnu.fp.net.co.Connection;
 import no.ntnu.fp.net.co.ConnectionImpl;
 
 public class ServerSynchronizationUnit extends SynchronizationUnit {
 
 //	private List<SyncListener> updatedButNotSavedObjects;
 	
 	private static final int TIME_BETWEEN_WRITES_TO_DB = 30000;
 	
 	private List<ClientHandler> activeUserConnections;
 	private DatabaseUnit dbUnit;
 	/**
 	 * The connection that will be used to listen for incoming connections
 	 */
 	private Connection connection;
 	
 	public ServerSynchronizationUnit() throws ConnectException {
 		super();
 //		this.updatedButNotSavedObjects = new ArrayList<SyncListener>();
 		this.activeUserConnections = new ArrayList<ClientHandler>();
 		this.dbUnit = new DatabaseUnit();
 		System.out.println(getNewKey(SaveableClass.Notification));
 		// TODO: LOADING!!!
 		try {
 			this.listeners = this.dbUnit.load();
 		} catch (SQLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		// Lagring
 		ActionListener saver = new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				 System.out.println("Writing to database..");
 				 synchronized (this) {
 					Collections.sort(listeners, new SyncListenerComparator());
 					int counter = 1;
 					for (SyncListener s : listeners) {
 						System.out.println(counter + ": " + s.getSaveableClass().toString() + ":" + s.getObjectID());
 					}
 					try {
 						dbUnit.objectsToDb(listeners);
 					} catch (SQLException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				 }
 				 System.out.println("Done");
 			}
 		};
 		new Timer(TIME_BETWEEN_WRITES_TO_DB, saver).start();
 		// TODO: Maa lagre et sted ogsaa!
 		// TODO: Add sort before save
 	}
 
 	public void removeClientConnection(ClientHandler handler) {
 		this.activeUserConnections.remove(handler);
 	}
 	
 //	public void addUpdatedObject(SyncListener object) {
 //		List<SyncListener> list = new ArrayList<SyncListener>();
 //		list.add(object);
 //		addUpdatedObjects(list);
 //	}
 
 	/**
 	 * Add the given objects to updatedButNotSavedObjects. Will remove old objects of same class with same ID
 	 * 
 	 * @param objects	The objects to add
 	 */
 //	public void addUpdatedObjects(List<SyncListener> objects) {
 //		List<SyncListener> toBeRemovedFromUpdatedButNotSavedObjects = new ArrayList<SyncListener>();
 //		for (SyncListener listener : this.updatedButNotSavedObjects) {
 //			for (SyncListener o : objects) {
 //				if(listener.getSaveableClass() == o.getSaveableClass() && listener.getObjectID().equalsIgnoreCase(o.getObjectID())) {
 //					toBeRemovedFromUpdatedButNotSavedObjects.add(listener);
 //				}
 //			}
 //		}
 //		this.updatedButNotSavedObjects.removeAll(toBeRemovedFromUpdatedButNotSavedObjects);
 //		this.updatedButNotSavedObjects.addAll(objects);
 //	}
 
 	public void listenForUserConnections(int port) {
 		this.connection = new ConnectionImpl(port);
 		while(true) {
 			try {
 				startUserSession(this.connection.accept());
 			} catch (SocketTimeoutException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * Returns the user with the given username, or null if the user is not present in the listener list
 	 * 
 	 * @param username	The user's username
 	 * @return			The user
 	 */
 	public User getUser(String username) {
 		for(SyncListener s : this.listeners) {
 			if(s.getSaveableClass() == SaveableClass.User && s.getObjectID().equalsIgnoreCase(username)) {
 				return (User) s;
 			}
 		}
 		return null;
 	}
 	
 	private void startUserSession(Connection con) {
 		ClientHandler ch = new ClientHandler(con, this);
 		this.activeUserConnections.add(ch);
 		new Thread(ch).start();
 	}
 	
 	public List<User> getActiveUsers() {
 		List<User> users = new ArrayList<User>();
 		for (ClientHandler handler : this.activeUserConnections) {
 			if(handler.getUser() != null) {
 				users.add(handler.getUser());
 			}
 		}
 		return users;
 	}
 	
 	/**
 	 * Checks if the given update is valid
 	 * 
 	 * @param update	The update to check
 	 * @param original	The original. Null if update is a new object
 	 * @return			True if the update is valid. False if not
 	 */
 	public boolean isValidUpdate(SyncListener update, SyncListener original, User sentBy) {
 		switch (update.getSaveableClass()) {
 			case Invitation :  {
 				/*
 				 * Server creates new invitations.
 				 * User can update own invitation with status only.
 				 */
 				if (original == null) {
 					return false;
 				}
 				Invitation inv = (Invitation) update;
 				Invitation old = (Invitation) original;
 				//id match?
 				if (!inv.getID().equals(old.getID())) {
 					return false;
 				}
 				//check if user owns notification
 				boolean userGotInv = false;
 				ArrayList<Notification> notlist = sentBy.getNotifications();
 				for (Notification notification : notlist) {
 					Invitation invstored = notification.getInvitation();
 					if (invstored.getID().equals(old.getID())) {
 						userGotInv = true;
 						break;
 					}
 				}
 				if (!userGotInv) {
 					return false;
 				}
 				//check if meeting was changed
 				Meeting invmeeting = inv.getMeeting();
 				Meeting oldmeeting = old.getMeeting();
 				if (!invmeeting.equals(oldmeeting)) {
 					return false;
 				}
 				return true;
 			}
 			case Notification :  {
 				/*
 				 * Server creates new notifications.
 				 * User updates own notification: read
 				 * Invitiation is bundled within.
 				 */
 				Notification notify = (Notification) update;
 				Notification old = null;
 				Invitation inv = notify.getInvitation();
 				Invitation ninv = null;
 				boolean validInvitation = false;
 				if (original != null) {
 					//update of existing notification
 					old = (Notification) original;
 					ninv = old.getInvitation();
 					
 					//same id?
 					if (!notify.getId().equals(old.getId())) {
 						return false;
 					}
 					//right user?
 					boolean userGotNotif = false;
 					ArrayList<Notification> notlist = sentBy.getNotifications();
 					for (Notification notification : notlist) {
 						if (notification.getId().equals(old.getId())) {
 							userGotNotif = true;
 							break;
 						}
 					}
 					
 					//check the invitation
 					validInvitation = isValidUpdate(inv, ninv, sentBy);
 					if (!validInvitation) {
 						return false;
 					}
 					
 					if (!userGotNotif) {
 						return false;
 					}
 					//notification type identical?
 					if (notify.getType() != old.getType()) {
 						return false;
 					}
 				} else {
 					//new notification should not be sent to server
 					return false;
 				}
 				return true;
 			}
 			case Appointment : {
 				//pass down to meeting
 			}
 			case Meeting : {
 				/* - Owner can change own app/meeting
 				 * Restrictions:
 				 * 	+ On room change, validate new room first
 				 * 	+ Can't move app backwards in time
 				 * 	+ Can't change owner
 				 * 	+ Can't change id
 				 * - Other user can change someone's meeting if:
 				 * 	+ probably never
 				 */
 				Appointment app = (Appointment) update;
 				if (original == null) {
 					return validateNewAppointment(sentBy, app);
 				} else {
 					return validateUpdatedAppointment(original, app);
 				}
 			}
 			default : {
 				System.out.println("Server does not accept changes in " + update.getClass());
 				return false;
 			}
 		}
 	}
 
 	private boolean validateUpdatedAppointment(SyncListener original,
 			Appointment app) {
 		Appointment oApp = (Appointment) original;
 		//don't change owner or id
 		if (!oApp.getOwner().getObjectID().equals(app.getOwner().getObjectID())) {
 			System.out.println("OWNER FAILED");
 			return false;
 		}
 		if (!oApp.getId().equals(app.getId())) {
 			System.out.println("ID FAILED");
 			return false;
 		}
 		
 		//Don't change start date backwards to before this day
 		Date oAppDate = oApp.getDate();
 		Date appDate = app.getDate();
 		Date now = Calendar.getInstance().getTime();
 		if ((!oAppDate.equals(appDate)) && appDate.before(now)) {
 			System.out.println("OLD DATE FAILED");
 			return false;
 		}
 		//Same as above, but for time
 		oAppDate = oApp.getStartTime();
 		appDate = app.getStartTime();
 		if (((!oAppDate.equals(appDate)) && appDate.before(now))) {
 			System.out.println("OLD TIME FAILED");
 			return false;
 		}
 		//changed room? if so, validate
 		Room appR = app.getRoom();
 		Room origR = oApp.getRoom();
 		if((appR != null && origR != null && !appR.equals(origR) && !getIsRoomAvailable(appR, appDate, app.getEndTime())) || !getIsRoomAvailable(appR, appDate, app.getEndTime())) {
 			System.out.println("ROOM FAILED!");
 			return false;
 		}
 		return true;
 	}
 
 	private boolean validateNewAppointment(User sentBy, Appointment app) {
 		// new app, verify owner
 		if (!app.getOwner().getObjectID().equals(sentBy.getObjectID())) {
 			System.out.println("OWNER NOT OK");
 			return false;
 		}
 		
 		//Don't change start date backwards to before this day
 		Date appDate = app.getStartTime();
 		Date now = Calendar.getInstance().getTime();
 		if (appDate.before(now)) {
 			System.out.println("DATE NOT OK");
 			return false;
 		}
 		
 		//verify room
 		Room room = app.getRoom();
 		if (room != null && !getIsRoomAvailable(room, app.getStartTime(), app.getEndTime())) {
 			System.out.println("ROOM NOT OK");
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Get all Appointment and meeting objects
 	 */
 	private List<Appointment> getAllAppointments() {
 		ArrayList<Appointment> apps = new ArrayList<Appointment>();
 		List<SyncListener> appsAndMeets = getObjectsFromID(SaveableClass.Appointment, null);
 		appsAndMeets.addAll(getObjectsFromID(SaveableClass.Meeting, null));
 		Iterator it = appsAndMeets.iterator();
 		while (it.hasNext()) {
 			apps.add((Appointment) it.next());
 		}
 		return apps;
 	}
 
 	/**
 	 * Get available rooms for the given time span
 	 */
 	@SuppressWarnings("rawtypes")
 	protected List<Room> getAvailableRooms(Date start, Date end) {
 		List<SyncListener> allRooms = getObjectsFromID(SaveableClass.Room, null);
 		List<Room> aRooms = new ArrayList<Room>();
 		List<Appointment> apps = getAllAppointments();
 		Iterator it = allRooms.iterator();
 		while (it.hasNext()) {
 			Room room = (Room) it.next();
 			if (getIsRoomAvailable(room, start, end, apps)) {
 				aRooms.add(room);
 			}
 		}
 		return aRooms;
 	}
 
 	/**
 	 * Check if a specified room is available during a given period of time.
 	 * If more rooms should be checked, use the variant passed with a list
 	 * of appointments.
 	 */
 	private boolean getIsRoomAvailable(Room room, Date start, Date end) {
 		if(room == null) {
 			return true;
 		}
 		return getIsRoomAvailable(room, start, end, getAllAppointments());
 	}
 	
 	/**
 	 * Check if a specified room is available during a given period of time.
 	 */
 	@SuppressWarnings("rawtypes")
 	private boolean getIsRoomAvailable(Room room, Date start, Date end, List<Appointment> apps) {
 		//iterate, looking for the room
 		Iterator it = apps.iterator();
 		DateFormat dFormat = Appointment.getDateFormat();
 		String resDay = dFormat.format(start);
 		while (it.hasNext()) {
 			Appointment a = (Appointment) it.next();
 			Calendar cal = Calendar.getInstance();
 			Date aDate = a.getDate();
 			cal.setTime(aDate);
 			String appDay = dFormat.format(aDate);
 			Room room2 = a.getRoom();
 			if (room2 != null && room2.equals(room) && resDay.equals(appDay)) {
 				//app uses room on the same day
 				System.out.println(resDay);
 				Date aStart = a.getStartTime();
 				Date aEnd = a.getEndTime();
 				//if found, check time span of app
 					//room reserved start before requested span ends
 					//room reserved end after requested span start
 					//both
 				if ((aEnd.before(start) || aStart.after(end))) {
 					//all ok?
 					continue;
 				} else {
 					return false;
 				}
 			}
 		}
 				//if no conflict, check next app
 		return true;
 		
 	}
 
 	public ClientHandler getClientHandler(User user) {
 		for (ClientHandler handler : this.activeUserConnections) {
 			// Returnerer ogsaa handler til bruker med samme brukernavn, hvis det ikke er samme objekt
 			if(handler.getUser() == user || this.getObjectFromID(SaveableClass.User, user.getUsername()) == handler.getUser()) { 
 				return handler;
 			}
 		}
 		return null;
 	}
 	
 	public String getNewKey(SaveableClass c) {
 		return this.dbUnit.getNewKey(c);
 	}
 	
 	public void sendMeetingInvitations(Meeting m) {
 		List<String> usernames = m.getUsersToInvite();
 		for (String username : usernames) {
 			User userToInvite = (User) this.getObjectFromID(SaveableClass.User, username);
 			Invitation newInv = new Invitation(InvitationStatus.NOT_ANSWERED, m, dbUnit.getNewKey(SaveableClass.Invitation));
 			this.addListener(newInv);
 			Notification newNot = new Notification(newInv, NotificationType.INVITATION_RECEIVED, dbUnit.getNewKey(SaveableClass.Notification), m.getOwner());
 			this.addListener(newNot);
 			userToInvite.addNotification(newNot);
 			// TODO: Er det greit aa sende bare notificationen, eller maa jeg legge den i User og sende User paa nytt?
 			ClientHandler ch = this.getClientHandler(userToInvite);
 			if(ch != null) {
 				ch.addToSendQueue(newNot);
 			}
 			m.addInvitation(newInv);
 			System.out.println("New invitation sent: " + newInv.getObjectID());
 			System.out.println("Notification: " + newNot.getObjectID());
 		}
 		m.setUsersToInvite(new ArrayList<String>());
 		this.getClientHandler(m.getOwner()).addToSendQueue(m);
 	}
 	
 	public void addObject(SyncListener o) {
 		this.addListener(o);
 		switch(o.getSaveableClass()) {
 		case Meeting:
 			// Ikke generell
 			((Meeting) o).setId(this.dbUnit.getNewKey(SaveableClass.Meeting));
 			sendMeetingInvitations((Meeting) o);
 		case Appointment:
 			// Ikke generell
 			if(o.getSaveableClass() == SaveableClass.Appointment) {
 				((Appointment) o).setId(this.dbUnit.getNewKey(SaveableClass.Appointment));
 			}
 			Appointment a = (Appointment) o;
 			User owner = a.getOwner();
 			List<User> users = this.getActiveUsers();
 			for (User user : users) {
 				if(user.getSubscribesTo().contains(owner)) {
 					this.getClientHandler(user).addToSendQueue(a);
 				}
 			}
 			break;
 		default:
 			throw new RuntimeException("Unexpected class!");
 		}
 	}
 	
 	public static void main2(String[] args) {
 		try {
 			ServerSynchronizationUnit ssu = new ServerSynchronizationUnit();
 			ssu.listeners.add(new User("Test2", "Testersen2", "test2", "NONE", new Date(), 911));
 			ssu.listeners.add(new User("Test5", "Testersen5", "test5", "NONE", new Date(), 911));
 			ssu.listeners.add(new User("Test3", "Testersen3", "test3", "NONE", new Date(), 911));
 			ssu.listeners.add(new User("Test1", "Testersen1", "test1", "NONE", new Date(), 911));
 			ssu.listeners.add(new User("Test4", "Testersen4", "test4", "NONE", new Date(), 911));
 		} catch (ConnectException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		new JFrame("TEST").setVisible(true);
 	}
 	
 	public static void main(String[] args) {
 		ConnectionImpl.fixLogDirectory();
 		ServerSynchronizationUnit ssu = null;
 		try {
 			ssu = new ServerSynchronizationUnit();
 		} catch (ConnectException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		for (SyncListener s : ssu.listeners) {
 			if(s instanceof User) {
 				System.out.println(s.getObjectID() + ":" + ((User)s).getPassword());
 			}
 		}
 //		User stian = new User("Stian", "Weie", "stianwe", "123", "BLANK", new Date(), 113);
 //		ssu.listeners.add(new User("Test", "Testersen", "test", "test", "BLANK", new Date(), 911));
 //		ssu.listeners.add(new User("Johan", "Reitan", "joharei", "123", "BLANK", new Date(), 113));
 //		ssu.listeners.add(stian);
 //		String id = ssu.getNewKey(SaveableClass.Appointment);
 //		System.out.println("DEN NYE IDEN ER : " + id);
 //		ssu.listeners.add(new Appointment(new Date(), new Date(), new Date(), "Dette er en test", "Her", null, id, stian, false));
 		ssu.listenForUserConnections(1337);
 	}
 }

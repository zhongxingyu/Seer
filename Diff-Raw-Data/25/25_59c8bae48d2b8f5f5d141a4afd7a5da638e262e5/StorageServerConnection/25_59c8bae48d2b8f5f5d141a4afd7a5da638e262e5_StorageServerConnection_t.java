 package client.system;
 
 import java.rmi.RemoteException;
 
 import common.StorageI;
 
 // This is just for the dummy server
 import server.storage.Storage;
 import server.model.*;
 
 public class StorageServerConnection {
 	public StorageI eventStorage;
 	public StorageI userStorage;
 	public StorageI groupStorage;
 	public StorageI roomStorage;
 	public StorageI alarmStorage;
 	public StorageI invitationStorage;
 	
 	public StorageServerConnection() throws RemoteException {
 		/*
 		RMIClient server = new RMIClient();
 		
		this.eventStorage 		= server.getClass("EventStorage");
		this.userStorage 		= server.getClass("UserStorage");
		this.groupStorage 		= server.getClass("GroupStorage");
		this.roomStorage 		= server.getClass("RoomStorage");
		this.alarmStorage 		= server.getClass("AlarmStorage");
		this.invitationStorage 	= server.getClass("InvitationStorage");
 		*/
 		
 		
 		// Dummy server
		this.eventStorage 		= new Storage(Event.class);
		this.userStorage 		= new Storage(User.class);
		this.groupStorage 		= new Storage(Group.class);
		this.roomStorage 		= new Storage(Room.class);
		this.alarmStorage 		= new Storage(Alarm.class);
		this.invitationStorage 	= new Storage(Invitation.class);
 	}
 }

 package server;
 
 import java.rmi.AccessException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Vector;
 import client.CommandsFromServer;
 
 
 /**
  * Each instance of this class is a server for one room.
  * In a first time, there is only one room server, and the names of the room available is fixed.
  * In a second time, you will have multiple room server, each managed by its own ChatServer.
  * A ChatServerManager will then be responsible for creating new rooms are they are added. 
  */
 @SuppressWarnings("serial")
 public class ChatServer implements ChatServerInterface {
 	
 	private String roomName;
 	private Vector<CommandsFromServer> registeredClients;
 	private Registry registry;
 	
 	
 	/**
 	 * Constructor: initializes the chat room and register it to the RMI registry
 	 * @param roomName
 	 */
 	public ChatServer(String roomName){
 		this.roomName = roomName;
 		registeredClients = new Vector<CommandsFromServer>();
 		
 		try {
 			ChatServerInterface stub = (ChatServerInterface)UnicastRemoteObject.exportObject(this,0);
 			registry = LocateRegistry.getRegistry();
 			registry.rebind("room_" + roomName, stub);
 		} catch (AccessException e) {
 			e.printStackTrace();
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}		
 	}
 	
 	public void publish(String message, String publisher) {
 		System.out.println("Message received from: " + publisher);
 		for(CommandsFromServer c : registeredClients) {
 			try {
 				c.receiveMsg(roomName, publisher + " : " + message);
 			} catch (RemoteException e) {
				registeredClients.remove(c);
 			}
 		}
 	}
 
 	public void register(CommandsFromServer client) {
 		System.out.println("Client joined the room " + roomName + " : " + client.toString());
 		registeredClients.add(client);
 		try {
 			registry.rebind(client.toString(), client);
 		} catch (AccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void unregister(CommandsFromServer client) {
 		System.out.println("Client left the room " + roomName + " : " + client.toString());
 		registeredClients.remove(client);
 	}
 	
 }

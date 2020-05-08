 package client;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 
 
 
 /**
  // This is the an interface between ChatClient and ChatServer.
  * This file must not be modified.
  */
 
 public interface CommandsFromServer extends Remote {
 	
 	
 	/**
 	 * Publish message in a chat room of the GUI interface. 
 	 * @param chatName The name of the chat room
 	 * @param message The message to display
 	 */
 	//it's is just a proxy for publish method of CommandsFromClient interface
 	//i.e. when Server call this receiveMsg method ChatClient call publish method of it's window to display this message
 	public void receiveMsg(String roomName, String message) throws RemoteException;
 }
 
 

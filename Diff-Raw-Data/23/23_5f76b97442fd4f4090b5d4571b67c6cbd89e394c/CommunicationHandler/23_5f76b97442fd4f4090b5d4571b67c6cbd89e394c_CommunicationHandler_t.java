 package Networking;
 
 import Core.Logger;
 import Core.Message;
 import Core.MessageHandler;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 public class CommunicationHandler {
 
 	public String clientName;
 	private ObjectInputStream inFromClient;
 	private ObjectOutputStream outToClient;
 	private MessageHandler msgHandler;
 	private ConnectionListener connListener;
 
 	public CommunicationHandler (MessageHandler msgHandler, String clientName, ObjectInputStream input, ObjectOutputStream output, ConnectionListener connListener) {
 		this.clientName = clientName;
 		this.inFromClient = input;
 		this.outToClient = output;
 		this.msgHandler = msgHandler;
 		this.connListener = connListener;
 	}
 
 	public final void startListening () {
 		Message msg = null;
 
 		try {
 			while (true) {
				msg = Message.cast(this.inFromClient.readObject());
				
 				//HANDLE messages here
				if (msg != null) {
 					msgHandler.submitMessage (msg);
 				}
 				else {
 					Logger.log ("Invalid message from " + clientName);
 				}
 			}
 		}
 		catch (ClassNotFoundException classEx) {
 			Logger.logDebug (classEx.getMessage());
 		}
 		catch (IOException ioEx) {
 			Logger.log (clientName + " disconnected");
			Logger.logDebug (ioEx.getMessage () + " " + ioEx.getCause ());
 		}
 		finally {
 			disconnect ();
 		}
 	}
 
 	public final void sendData (String tag, Object data) {
 		try {
			this.outToClient.writeObject(new Message (tag, "Server", data));
 			this.outToClient.flush ();
 		}
 		catch (Exception ex) {
 			Logger.logDebug ("sendData: " + ex.getMessage ());
 		}
 	}
 
 	public void disconnect () {
 		Thread.currentThread ().interrupt ();
 		this.connListener.onDisconnect (this);
 	}
 }

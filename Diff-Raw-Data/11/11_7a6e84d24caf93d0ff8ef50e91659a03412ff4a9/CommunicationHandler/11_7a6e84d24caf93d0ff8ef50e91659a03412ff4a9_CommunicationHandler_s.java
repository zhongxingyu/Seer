 package Networking;
 
 import Core.Logger;
 import Core.Message;
 import Core.MessageHandler;
 import Core.MessageTag;
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
 					msgHandler.submitReceivedMessage (msg);
 				}
 				else {
 					Logger.log ("Invalid message from " + clientName);
 				}
 			}
 		}
 		catch (ClassNotFoundException classEx) {
 			Logger.logException ("CommunicationHandler::startListening", classEx);
 		}
 		catch (IOException ioEx) {
 			Logger.log (clientName + " disconnected");
 			Logger.logException ("CommunicationHandler::startListening", ioEx);
 		}
 		finally {
 			disconnect ();
 		}
 	}
 
 	public final void sendData (MessageTag tag, Object data) {
 		this.sendData (new Message (tag, "", data));
 	}
 	public final void sendData (Message msg) {
 		msg.from = "Server";
 		try {
 			this.outToClient.writeObject(msg);
 			this.outToClient.flush ();
 		}
 		catch (Exception ex) {
 			Logger.logDebug ("sendData: " + ex.getMessage ());
 		}
 	}
 
 	public void disconnect () {
 		Thread.currentThread ().interrupt ();
 		this.connListener.onDisconnect (this);
		msgHandler.submitSendingMessage (new Message (MessageTag.PLAYER_DROP, "", this.clientName));
 	}
 }

 package Networking;
 
 import Core.Logger;
 import Core.Message;
 import Core.MessageHandler;
 import Core.MessageTag;
 import Game.ServerGameEngine;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.util.List;
 
 public class ConnectionHandler implements Runnable, ConnectionListener {
 
 	private List<CommunicationHandler> clientList;
 	private ObjectInputStream inFromClient;
 	private ObjectOutputStream outToClient;
 	private ServerGameEngine gEngine;
 	private MessageHandler msgHandler;
 	private CommunicationHandler commHandler;
 
 	public ConnectionHandler (Socket clientSocket, final ServerGameEngine gEngine, MessageHandler msgHandler, List<CommunicationHandler> clientList) throws Exception {
 		this.gEngine = gEngine;
 		this.clientList = clientList;
 		this.msgHandler = msgHandler;
 
 		//Create streams
 		try {
 			this.outToClient = new ObjectOutputStream (clientSocket.getOutputStream ());
 			this.outToClient.flush ();
 			this.inFromClient = new ObjectInputStream (clientSocket.getInputStream ());
 
 			//Get client info here
 			Logger.logDebug ("Waiting for info...");
 			Message msg = Message.cast (this.inFromClient.readObject ());
 			if (msg == null) {
 				throw new Exception ("Invalid info");
 			}
 			Logger.logDebug ("Info received!");
 
 			this.commHandler = createCommHandler (msg);
 		}
 		catch (IOException ex) {
 			Logger.logException ("ConnectionHandler::ConnectionHandler", ex);
 		}
 	}
 
 	private CommunicationHandler createCommHandler (Message msg) {
 		return new CommunicationHandler (this.msgHandler, msg.from, this.inFromClient, this.outToClient, this);
 	}
 
 	@Override
 	public void onConnect (CommunicationHandler commHandler) throws Exception {
 		//Add comm handler to list
 		Logger.log ("Registering " + commHandler.clientName + "...");
 		boolean registered = this.gEngine.registerPlayer (commHandler.clientName);
 		if (registered) {
 			this.clientList.add (commHandler);
			this.msgHandler.submitSendingMessage (new Message (MessageTag.PLAYER_JOIN, commHandler.clientName, commHandler.clientName));
 			Logger.log (commHandler.clientName + " has connected");
 		}
 		else {
 			commHandler.sendData(MessageTag.REJECT, "Game in progress");
 			commHandler.disconnect();
 			throw new Exception ("Attempt to join while game has started");
 		}
 	}
 
 	@Override
 	public void onDisconnect (CommunicationHandler commHandler) {
 		try {
 			this.inFromClient.close ();
 			this.outToClient.close ();
 		}
 		catch (Exception ex) {
 			Logger.logException ("ConnectionHandler::onDisconnect", ex);
 		}
 		this.clientList.remove (commHandler);
 		this.gEngine.dropPlayer(commHandler.clientName);
 	}
 
 	@Override
 	public void run () {
 		try {
 			this.onConnect (this.commHandler);
 			this.commHandler.startListening ();
 		}
 		catch (Exception ex) {
 			Logger.logException ("ConnectionHandler::run ", ex);
 		}
 	}
 
 }

 package edu.berkeley.cs.cs162.Server;
 
 import edu.berkeley.cs.cs162.Synchronization.Lock;
 import edu.berkeley.cs.cs162.Writable.ClientInfo;
 import edu.berkeley.cs.cs162.Writable.ClientMessages;
 import edu.berkeley.cs.cs162.Writable.ClientMessages.ChangePasswordMessage;
 import edu.berkeley.cs.cs162.Writable.GameInfo;
 import edu.berkeley.cs.cs162.Writable.Message;
 import edu.berkeley.cs.cs162.Writable.MessageFactory;
 import edu.berkeley.cs.cs162.Writable.MessageProtocol;
 
 public abstract class ClientLogic {
     private String name;
     private GameServer server;
     Lock observingLock;
 	private int clientID;
     
     public ClientLogic(GameServer server, String name) {
         this.name = name;
         this.server = server;
         observingLock = new Lock();
     }
     public static ClientLogic getClientLogicForClientType(GameServer server, String name, byte playerType, ClientConnection connection) {
         switch (playerType) {
             case MessageProtocol.TYPE_HUMAN:
                 return new PlayerLogic.HumanPlayerLogic(server, connection, name);
             case MessageProtocol.TYPE_MACHINE:
                 return new PlayerLogic.MachinePlayerLogic(server, connection, name);
             case MessageProtocol.TYPE_OBSERVER:
                 return new ObserverLogic(server, connection, name);
         }
         throw new AssertionError("Unknown Client Type");
     }
     
     public Message handleMessage(Message message) {
         switch (message.getMsgType()) {
             case MessageProtocol.OP_TYPE_LISTGAMES: {
                 return handleListGames();
             }
             case MessageProtocol.OP_TYPE_JOIN: {
                 return handleJoinGame(((ClientMessages.JoinMessage) message).getGameInfo());
             }
             case MessageProtocol.OP_TYPE_LEAVE: {
                 return handleLeaveGame(((ClientMessages.LeaveMessage)message).getGameInfo());
             }
             case MessageProtocol.OP_TYPE_WAITFORGAME: {
             	return handleWaitForGame();
             }
             case MessageProtocol.OP_TYPE_DISCONNECT: {
                 return null;
             }
             case MessageProtocol.OP_TYPE_CHANGEPW: {
             	return handleChangePassword((ChangePasswordMessage) message);
             }
             case MessageProtocol.OP_TYPE_REGISTER: {
             	return MessageFactory.createErrorRejectedMessage();
             }
            case MessageProtocol.OP_TYPE_CONNECT: {
                return MessageFactory.createErrorRejectedMessage();
            }
         }
         throw new AssertionError("Unimplemented Method");
     }
     
     public Message handleChangePassword(ClientMessages.ChangePasswordMessage message) {
     	if (message.getClientInfo().equals(makeClientInfo())) {
     		getServer().getAuthenticationManager().changePassword(message.getClientInfo(), message.getPasswordHash());
     		return MessageFactory.createStatusOkMessage();
     	}
     	else {
     		return MessageFactory.createErrorRejectedMessage();
     	}
 	}
     
 	public Message handleWaitForGame() {
 		return MessageFactory.createErrorRejectedMessage();
 	}
 
 	public Message handleLeaveGame(GameInfo gameInfo) {
 		return MessageFactory.createErrorRejectedMessage();
 	}
 
     public Message handleJoinGame(GameInfo gameInfo) {
 		return MessageFactory.createErrorRejectedMessage();
 	}
 
     public Message handleListGames() {
 		return MessageFactory.createErrorRejectedMessage();
 	}
     
     public GameServer getServer() {
 		return server;
 	}
     
     public String getName() {
     	return name;
     }
     
 	public abstract void handleSendMessage(Message message);
 
     public abstract void cleanup();
 	public abstract ClientInfo makeClientInfo();
 	public void setID(int clientID) {
 		this.clientID = clientID;
 	}
 	public int getID() {
 		return clientID;
 	}
 }

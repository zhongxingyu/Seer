 package edu.berkeley.cs.cs162.Client;
 
 import edu.berkeley.cs.cs162.Writable.*;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.Random;
 
 abstract public class BaseClient implements Client {
 
     String name;
     byte type;
     ClientInfo clientInfo;
     ServerConnection connection;
 
     public BaseClient(String name, byte type) {
         this.name = name;
         this.type = type;
        switch(type)
        {
            case MessageProtocol.TYPE_HUMAN:
                clientInfo = MessageFactory.createHumanPlayerClientInfo(name);
                break;
            case MessageProtocol.TYPE_MACHINE:
                clientInfo = MessageFactory.createMachinePlayerClientInfo(name);
                break;
            case MessageProtocol.TYPE_OBSERVER:
                clientInfo = MessageFactory.createObserverClientInfo(name);
                break;
            default:
                clientInfo = null;
        }
     }
 
     public BaseClient(String name) {
         this(name, (byte) -1);
     }
 
     public BaseClient() {
         this("");
     }
 
     public String getName() {
         return this.name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public byte getType() {
         return this.type;
     }
 
     public void setType(byte type) {
         this.type = type;
     }
 
     public ClientInfo getClientInfo() {
         return clientInfo;
     }
 
     protected boolean connectTo(String address, Integer port){
         try
         {
             // Create the C2S and S2C sockets
             Socket c1 = new Socket(address, port);
             Socket c2 = new Socket(address, port);
 
             // Attempt to connect to the GameServer via 3-way Handshake
             connection = new ServerConnection(c1, c2);
             System.out.println(connection.initiate3WayHandshake(new Random()));
             Message connectMessage = MessageFactory.createConnectMessage(clientInfo);
             Message serverResponse = connection.sendSyncToServer(connectMessage);
 
             return (serverResponse.isOK());
         }
         catch(IOException e) { return false; }
     }
 
     protected void handleMessage(Message m) throws IOException {
         switch (m.getMsgType()) {
             case MessageProtocol.OP_TYPE_GAMESTART:
                 handleGameStart((ServerMessages.GameStartMessage) m);
                 break;
             case MessageProtocol.OP_TYPE_GAMEOVER:
                 handleGameOver((ServerMessages.GameOverMessage) m);
                 break;
             case MessageProtocol.OP_TYPE_MAKEMOVE:
                 handleMakeMove((ServerMessages.MakeMoveMessage) m);
                 break;
             case MessageProtocol.OP_TYPE_GETMOVE:
                 handleGetMove();
                 break;
             default:
                 assert false: "No method defined for message type.";
         }
     }
 
     protected void handleGameStart(ServerMessages.GameStartMessage m) throws IOException {
         connection.sendReplyToServer(MessageFactory.createStatusOkMessage());
     }
 
     protected void handleGameOver(ServerMessages.GameOverMessage m) throws IOException {
         connection.sendReplyToServer(MessageFactory.createStatusOkMessage());
     }
 
     protected void handleMakeMove(ServerMessages.MakeMoveMessage m) throws IOException {
         connection.sendReplyToServer(MessageFactory.createStatusOkMessage());
     }
 
     protected void handleGetMove() throws IOException {
         connection.sendReplyToServer(MessageFactory.createStatusOkMessage());
     }
 }

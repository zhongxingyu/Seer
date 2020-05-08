 package aigilas.net;
 
 import aigilas.Config;
 import com.badlogic.gdx.Gdx;
 import sps.bridge.Command;
 import sps.core.Logger;
 import sps.core.RNG;
 import sps.io.CommandState;
 
 import java.net.ConnectException;
 import java.net.Socket;
 
 public class LanClient extends IClient {
     // Client <-> Server
     private Message _message;
     private MessageHandler _comm;
     private static final float _heartBeatWaitSeconds = .1f;
     private float _heartBeat = _heartBeatWaitSeconds;
 
     // Client <-> Game
     private Integer _initialPlayerIndex;
     private boolean _isGameStarting;
     private boolean _dungeonHasLoaded = false;
     private boolean _isConnected = false;
     private CommandState state = new CommandState();
 
     public LanClient() {
         connect();
     }
 
     private void connect() {
         try {
             Socket server = new Socket(Config.get().serverIp(), Config.get().port());
             _comm = new MessageHandler(server);
             _comm.owner = "CLIENT";
             sendMessage(Message.createInit(0, 0));
             awaitReply(MessageTypes.Connect);
             handleResponse(_message);
         }
         catch (ConnectException cE) {
             try {
                 Thread.sleep(1000);
                 Logger.info("Waiting to connect");
                 connect();
             }
             catch (InterruptedException e) {
                 Logger.exception(e);
             }
         }
         catch (Exception e) {
             Logger.exception(e);
         }
     }
 
     // Client <-> Game communication
     public boolean isGameStarting() {
         return _isGameStarting;
     }
 
     public boolean isConnected() {
         return _isConnected;
     }
 
     public void heartBeat() {
         if (!_dungeonHasLoaded) {
             _heartBeat -= Gdx.graphics.getDeltaTime();
             if (_heartBeat <= 0) {
                 sendMessage(Message.createHeartBeat());
                 _heartBeat = _heartBeatWaitSeconds;
             }
         }
     }
 
     public void dungeonHasLoaded() {
         _dungeonHasLoaded = true;
     }
 
     public boolean nextTurn() {
         update();
         if (_message != null && _message.MessageType == MessageTypes.Sync_State) {
             _heartBeat = _heartBeatWaitSeconds;
             return true;
         }
         return false;
     }
 
     public int getFirstPlayerIndex() {
         return _initialPlayerIndex;
     }
 
     public void prepareForNextTurn() {
         sendMessage(Message.createReadyForNextTurn());
     }
 
     // Client <-> Server communication
     public boolean isActive(Command command, int playerIndex) {
         return state.isActive(playerIndex, command);
     }
 
     public void setState(Command command, int playerIndex, boolean isActive) {
         if (isActive != state.isActive(playerIndex, command)) {
             sendMessage(Message.createMovement(command, playerIndex, isActive));
         }
     }
 
     int _playerCount = 0;
 
     public int getPlayerCount() {
         if (_playerCount == 0 || !isGameStarting()) {
             sendMessage(Message.createPlayerCount(0));
             awaitReply(MessageTypes.Player_Count);
             _playerCount = _message.PlayerCount;
         }
         return _playerCount;
     }
 
     public void startGame() {
         sendMessage(Message.create(MessageTypes.Start_Game));
     }
 
     private void sendMessage(Message contents) {
         contents.PlayerIndex = _initialPlayerIndex;
         contents.LocalPort = _comm.getLocalPort();
         _comm.sendOutboundMessage(contents);
     }
 
     // If the server doesn't reply at some point with the messageType you expect
     // Then the client will hang in an infinite loop.
     private void awaitReply(MessageTypes messageType) {
         while (true) {
             _message = _comm.readInboundMessage();
             if (_message != null) {
                 if (_message.MessageType == messageType) {
                     return;
                 }
                 else {
                     handleResponse(_message);
                 }
             }
         }
     }
 
     public void update() {
         _message = _comm.readInboundMessage();
         if (_message != null) {
             handleResponse(_message);
         }
     }
 
     private void handleResponse(Message contents) {
         switch (contents.MessageType) {
             case Connect:
                 RNG.seed(contents.RngSeed);
                 _initialPlayerIndex = (int) contents.PlayerCount - 1;
                 _isConnected = true;
                 break;
             case Start_Game:
                 _isGameStarting = true;
                 _playerCount = contents.PlayerCount;
                 break;
             case Sync_State:
                 state.reset(contents.CommandState);
                 break;
             default:
                 break;
         }
     }
 
     public void close() {
         Logger.info("Shutting down a client.");
     }
 }

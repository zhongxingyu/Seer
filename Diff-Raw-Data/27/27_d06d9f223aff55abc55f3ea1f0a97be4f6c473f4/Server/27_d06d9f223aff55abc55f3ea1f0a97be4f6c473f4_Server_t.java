 package aigilas.net;
 
 import aigilas.Config;
 import com.badlogic.gdx.Gdx;
 import sps.core.Logger;
 import sps.io.CommandState;
 
 public class Server extends Thread {
     public static void reset() {
         Logger.info("Starting the server");
         Thread server = new Server();
         server.start();
         try {
             Thread.sleep(100);
         }
         catch (InterruptedException e) {
             Logger.exception(e);
         }
     }
 
     private boolean isRunning = true;
     private final CommandState state = new CommandState();
     private final int _rngSeed = (int) System.currentTimeMillis();
     private Message _message = Message.empty();
     private Integer _turnCount = 0;
     private final boolean[] _readyCheckIn = {true, true, true, true};
     private float _turnTime = 0;
 
     private final ClientManager clients;
 
     private Server() {
         setName("Server");
         clients = new ClientManager();
     }
 
     public void run() {
         while (isRunning) {
             try {
                 Thread.sleep(1);
             }
             catch (InterruptedException e) {
                 Logger.exception(e);
             }
             pollForNewMessages();
             broadCastGameState();
         }
     }
 
     private void pollForNewMessages() {
         _message = clients.readMessage();
         if (_message != null) {
             switch (_message.MessageType) {
                 case Connect:
                     sendMessage(Message.createInit(clients.size(), _rngSeed), _message.LocalPort);
                     _turnCount = 0;
                     break;
                 case Check_State:
                     _message.IsActive = state.isActive(_message.PlayerIndex, _message.Command);
                     sendMessage(_message, _message.LocalPort);
                     break;
 
                 case Movement:
                     state.setState(_message.PlayerIndex, _message.Command, _message.IsActive);
                     break;
 
                 case Start_Game:
                     _message.setPlayerCount(clients.size());
                     announce(_message);
                     break;
 
                 case Player_Count:
                     sendMessage(Message.createPlayerCount(clients.size()), _message.LocalPort);
                     break;
 
                 case Ready_For_Next_Turn:
                     _readyCheckIn[_message.PlayerIndex] = true;
                     break;
 
                 case Heart_Beat:
                     _readyCheckIn[_message.PlayerIndex] = true;
                     break;
                 default:
                     break;
             }
         }
     }
 
     private void broadCastGameState() {
         _turnTime += Gdx.graphics.getDeltaTime();
         if (_turnTime >= Config.get().turnTime) {
             int readyCount = 0;
            for (boolean checkedIn : _readyCheckIn) {
                readyCount += checkedIn ? 1 : 0;
             }
             if (readyCount >= clients.size()) {
                 _turnTime = 0;
                 announce(Message.createPlayerState(state, _turnCount++, _rngSeed));
                 for (int ii = 0; ii < _readyCheckIn.length; ii++) {
                     _readyCheckIn[ii] = false;
                 }
             }
         }
     }
 
     private void announce(Message contents) {
         clients.announce(contents);
     }
 
     private void sendMessage(Message contents, int localPort) {
         contents.LocalPort = localPort;
         clients.send(contents);
     }
 
     public void close() {
         isRunning = false;
         Logger.info("SERVER: Shutting down");
     }
 }

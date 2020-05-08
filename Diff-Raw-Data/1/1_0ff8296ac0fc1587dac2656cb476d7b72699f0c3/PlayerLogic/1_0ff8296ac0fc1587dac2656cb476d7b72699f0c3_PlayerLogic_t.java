 package edu.berkeley.cs.cs162.Server;
 
 import edu.berkeley.cs.cs162.Synchronization.Lock;
 import edu.berkeley.cs.cs162.Writable.ClientInfo;
 import edu.berkeley.cs.cs162.Writable.Message;
 import edu.berkeley.cs.cs162.Writable.MessageFactory;
 
 public abstract class PlayerLogic extends ClientLogic {
     
 	private static final int HUMAN_PLAYER_TIMEOUT_IN_MS = 30000;
     private static final int MACHINE_PLAYER_TIMEOUT_IN_MS = 2000;
     private static final int PLAYER_RECONNECT_TIMEOUT_IN_MS = 60000;
 
     public static class HumanPlayerLogic extends PlayerLogic {
         public HumanPlayerLogic(GameServer server, ClientConnection con, String name) {
             super(server, con, name, HUMAN_PLAYER_TIMEOUT_IN_MS);
         }
 
         public ClientInfo makeClientInfo() {
             return MessageFactory.createHumanPlayerClientInfo(getName());
         }
     }
 
     public static class MachinePlayerLogic extends PlayerLogic {
         public MachinePlayerLogic(GameServer server, ClientConnection con, String name) {
             super(server, con, name, MACHINE_PLAYER_TIMEOUT_IN_MS);
         }
 
         public ClientInfo makeClientInfo() {
             return MessageFactory.createMachinePlayerClientInfo(getName());
         }
     }
 
     /**
      * flag that says whether this worker is waiting for a game.
      */
     enum PlayerState {
     	CONNECTED,
     	WAITING,
     	RECONNECT,
     	RECONNECTING,
     	PLAYING,
     	DISCONNECTED
     }
     
     private PlayerState state;
     private Lock stateLock;
     private int playerTimeoutInMs;
 	private PlayerWorkerSlave slave;
     
 	public PlayerLogic(GameServer server, ClientConnection connection, String name, int playerTimeoutInMs) {
         super(server, name);
         state = PlayerState.CONNECTED;
         stateLock = new Lock();
         this.playerTimeoutInMs = playerTimeoutInMs;
         slave = new PlayerWorkerSlave(connection, this, getTimeout());
         slave.start();
     }
     
     @Override
 	public Message handleWaitForGame() {
 		stateLock.acquire();
 		if (state == PlayerState.CONNECTED) {
 			UnfinishedGame unfinishedGame = getServer().checkForUnfinishedGame(this);
 			if (unfinishedGame != null) {
 				Game reconnectedGame = unfinishedGame.reconnectGame();
 				if (reconnectedGame != null) {
 					//other client has already reconnected. 
 					//just start the game again and return the reconnected board state.
 					state = PlayerState.PLAYING;
 					stateLock.release();
 					getServer().getLog().println(makeClientInfo() + " has resumed playing the game.");
 					unfinishedGame.wakeOtherPlayer(this);
					getServer().addGame(reconnectedGame);
 					reconnectedGame.handleNextMove();
 				} else {
 					//other client hasn't connected yet
 					//start waiting for the other game.
 					state = PlayerState.RECONNECT;
 					stateLock.release();
 					getServer().getLog().println(makeClientInfo() + " has is waiting to resume the game.");
 					slave.handleWaitForReconnect(unfinishedGame, this, 
 							PLAYER_RECONNECT_TIMEOUT_IN_MS);
 				}
 				return MessageFactory.createStatusResumeMessage(
 						unfinishedGame.makeGameInfo(), 
 						unfinishedGame.makeBoardInfo(), 
 						unfinishedGame.getBlackInfo(), 
 						unfinishedGame.getWhiteInfo());
 			} else {
 				state = PlayerState.WAITING;
 				stateLock.release();
 				getServer().addPlayerToWaitQueue(this);
 				return MessageFactory.createStatusOkMessage();
 			}
 		}
 		else
 		{
 			stateLock.release();
 			return MessageFactory.createErrorRejectedMessage();
 		}
 	}
 
 	/**
 	 * Switches the client state from waiting to playing.
 	 * @return
 	 */
     public boolean startGame()
     {
     	boolean started = false;
     	stateLock.acquire();
     	if (state == PlayerState.WAITING)
     	{
 		    System.out.println("Game started for player " + getName());
     		state = PlayerState.PLAYING;
     		started = true;
     	} else {
     		System.out.println("Tried to start game for player " + getName() + " who was " + state);
     	}
     	stateLock.release();
     	return started;
     }
     
     public void beginGame(Game game) {
 		assert state == PlayerState.PLAYING : "Tried to start game when game was not active";
 		slave.handleStartNewGame(game);
     }
     
     public void cleanup()
     {
     	disconnectState();
     	slave.handleTerminate();
     }
     
 	public abstract ClientInfo makeClientInfo();
 
 	public int getTimeout() {
 		return playerTimeoutInMs;
 	}
 
 	public void terminateGame() {
 		stateLock.acquire();
     	if (state == PlayerState.PLAYING || state == PlayerState.RECONNECT)
     	{
         	//assert state == PlayerState.PLAYING : "Terminated game when not playing";
     		state = PlayerState.CONNECTED;
     	}
     	stateLock.release();
 	}
 
 	public void disconnectState() {
 		stateLock.acquire();
 		state = PlayerState.DISCONNECTED;
 		stateLock.release();
 	}
 	public void handleNextMove(Game game) {
 		getSlave().handleNextMove(game);
 	}
 	private PlayerWorkerSlave getSlave() {
 		return slave;
 	}
 	public void handleSendMessage(Message message) {
 		getSlave().handleSendMessage(message);
 	}
 
 	public boolean reconnected() {
 		stateLock.acquire();
 		state = PlayerState.PLAYING;
 		stateLock.release();
 		getServer().getLog().println(makeClientInfo() + " has woken up and is playing the game.");
 		return true;
 	}
 }

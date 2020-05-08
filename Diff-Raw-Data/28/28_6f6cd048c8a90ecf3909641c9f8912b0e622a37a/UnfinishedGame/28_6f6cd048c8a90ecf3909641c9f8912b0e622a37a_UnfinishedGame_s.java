 package edu.berkeley.cs.cs162.Server;
 
 import java.util.concurrent.TimeoutException;
 
 import edu.berkeley.cs.cs162.Synchronization.Lock;
 import edu.berkeley.cs.cs162.Synchronization.Semaphore;
 import edu.berkeley.cs.cs162.Writable.BoardInfo;
 import edu.berkeley.cs.cs162.Writable.ClientInfo;
 import edu.berkeley.cs.cs162.Writable.GameInfo;
 import edu.berkeley.cs.cs162.Writable.MessageFactory;
 
 public class UnfinishedGame {
 	private ClientInfo whitePlayerInfo;
 	private ClientInfo blackPlayerInfo;
 	private PlayerLogic blackPlayer;
 	private PlayerLogic whitePlayer;
 	private int gameID;
 	private GoBoard board;
 	private String name;
 	private Lock reconnectionLock;
 	private boolean reconnected;
 	private Semaphore reconnectionSemaphore;
 	private int reconnectionTimeout;
 	enum ReconnectionStatus {
 		NONE_CONNECTED,
 		ONE_CONNECTED,
 		TWO_CONNECTED
 	}
 	
 	public UnfinishedGame(String name, GoBoard board, ClientInfo blackPlayer, ClientInfo whitePlayer, int gameID) {
		this(name, board, whitePlayer, whitePlayer, 60000, gameID);
 	}
 	
 	public UnfinishedGame(String name, GoBoard board, ClientInfo blackPlayer, ClientInfo whitePlayer, int reconnectionTimeout, int gameID) {
 		this.board = board;
 		this.blackPlayerInfo = blackPlayer;
 		this.whitePlayerInfo = whitePlayer;
 		this.blackPlayer = null;
 		this.whitePlayer = null;
 		this.gameID = gameID;
 		this.name = name;
 		this.reconnected = false;
 		this.reconnectionLock = new Lock();
 		this.reconnectionSemaphore = new Semaphore(0);
 		this.reconnectionTimeout = reconnectionTimeout;
 	}
 	
 	public Game reconnectGame(PlayerLogic player) {
 		PlayerLogic otherPlayer;
 		if (player == blackPlayer) {
 			otherPlayer = whitePlayer;
 		} else if (player == whitePlayer) {
 			otherPlayer = blackPlayer;
 		} else {
 			otherPlayer = null;
 		}
 		
 		if (checkPlayerInvalid(otherPlayer) || reconnected) {
 			return null;
 		} else {
 			reconnected = true;
 			return new Game(name, blackPlayer, whitePlayer, board, gameID);
 		}
 	}
 	
 	public boolean matchesPlayer(PlayerLogic player) {
 		if (checkPlayerInvalid(blackPlayer) && blackPlayerInfo.equals(player.makeClientInfo())) {
 			reconnectionLock.acquire();
 			blackPlayer = player;
 			return true;
 		}
 		if (checkPlayerInvalid(whitePlayer) && whitePlayerInfo.equals(player.makeClientInfo())) {
 			reconnectionLock.acquire();
 			whitePlayer = player;
 			return true;
 		}
 		return false;
 	}
 	
 	private boolean checkPlayerInvalid(PlayerLogic logic) {
 		return logic == null || !logic.isReconnecting();
 	}
 
 	public int getGameID() {
 		return gameID;
 	}
 
 	public StoneColor getColorForInfo(ClientInfo info) {
 		if (info.equals(blackPlayerInfo))
 			return StoneColor.BLACK;
 		else if (info.equals(whitePlayerInfo))
 			return StoneColor.WHITE;
 		else 
 			return null;
 	}
 
 	public GameInfo makeGameInfo() {
 		return MessageFactory.createGameInfo(name);
 	}
 	public BoardInfo makeBoardInfo() {
 		return MessageFactory.createBoardInfo(board.getCurrentBoard());
 	}
 
 	public ClientInfo getWhiteInfo() {
 		return whitePlayerInfo;
 	}
 	public ClientInfo getBlackInfo() {
 		return blackPlayerInfo;
 	}
 
 	public void wakePlayer() {
 		reconnectionSemaphore.v();
 	}
 	
 	public boolean isReconnected() {
 		reconnectionLock.acquire();
 		try {
 			return reconnected;
 		} finally {
 			reconnectionLock.release();
 		}
 	}
 
 	public void waitForReconnect() throws TimeoutException {
 		reconnectionSemaphore.p(reconnectionTimeout);
 	}
 
 	public ClientInfo getInfoForColor(StoneColor otherColor) {
 		switch(otherColor) {
 		case BLACK:
 			return blackPlayerInfo;
 		case WHITE:
 			return whitePlayerInfo;
 		default:
 			return null;
 		}
 	}
 
 	public void finishReconnection() {
 		reconnectionLock.release();
 	}
 }

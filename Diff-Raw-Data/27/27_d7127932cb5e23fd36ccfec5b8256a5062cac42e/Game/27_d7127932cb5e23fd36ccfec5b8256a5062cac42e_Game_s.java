 package edu.berkeley.cs.cs162.Server;
 
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Vector;
 
 import edu.berkeley.cs.cs162.Server.GoBoard.IllegalMoveException;
 import edu.berkeley.cs.cs162.Synchronization.ReaderWriterLock;
 import edu.berkeley.cs.cs162.Writable.BoardInfo;
 import edu.berkeley.cs.cs162.Writable.GameInfo;
 import edu.berkeley.cs.cs162.Writable.Message;
 import edu.berkeley.cs.cs162.Writable.MessageFactory;
 import edu.berkeley.cs.cs162.Writable.MessageProtocol;
 
 public class Game {
 	enum GameState {
 		BLACK_MOVE,
 		WHITE_MOVE
 	}
 	private GameState state;
     private GoBoard board;
     private String name;
     private boolean active;
     private PlayerLogic blackPlayer;
     private PlayerLogic whitePlayer;
     private Set<ObserverLogic> observerList;
     private ReaderWriterLock observerLock;
 	private GameServer server;
 	
 	private int gameID;
 	
     public Game(String name, PlayerLogic blackPlayer, PlayerLogic whitePlayer, GoBoard board) {
     	this(name, blackPlayer, whitePlayer, board, -1);
     }
 
     public Game(String name, PlayerLogic blackPlayer,
 			PlayerLogic whitePlayer, GoBoard board, int gameID) {
     	this.gameID = gameID;
     	this.blackPlayer = blackPlayer;
     	this.whitePlayer = whitePlayer;
     	server = blackPlayer.getServer();
     	this.name = name;
         this.board = board;
         observerList = new HashSet<ObserverLogic>();
         observerLock = new ReaderWriterLock();
         state = board.getBlackMove() ? GameState.BLACK_MOVE : GameState.WHITE_MOVE;
         active = true;
 	}
 
 	public GameInfo makeGameInfo() {
         return MessageFactory.createGameInfo(name);
     }
 
     /**
      * Adds an observer to this game. can be done asynchronously by multiple threads.
      * 
      * This will simultaneously also add this game to the worker's observer list. 
      * This is done atomically because otherwise, we could have a race condition and 
      * end up with an orphaned observer.
      *
      * @param worker
      * @return 
      */
     public boolean addObserver(ObserverLogic o) {
     	if (!isActive()) {return false;}
     	observerLock.writeLock();
     	boolean added = !observerList.contains(o);
     	if (added)
     	{
     		observerList.add(o);
     		o.addGame(this);
     	}
     	observerLock.writeUnlock();
 		return added;
     }
 
     public boolean removeObserver(ObserverLogic o) {
     	observerLock.writeLock();
     	boolean removed = observerList.contains(o);
     	if (removed)
     	{
     		observerList.remove(o);
     		o.removeGame(this);
     	}
     	observerLock.writeUnlock();
 		return removed;
     }
 
     public BoardInfo makeBoardInfo() {
         return MessageFactory.createBoardInfo(board.getCurrentBoard());
     }
 
     public PlayerLogic getBlackPlayer() {
         return blackPlayer;
     }
 
     public PlayerLogic getWhitePlayer() {
         return whitePlayer;
     }
 
 	public String getName() {
 		return this.name;
 	}
 
 	public GoBoard getBoard() {
 		return this.board;
 	}
 	
     public int getBoardSize(){
         return board.getSize();
     }
 
 	public void broadcastStartMessage() {
 		Message message = MessageFactory.createGameStartMessage(this);
 		observerLock.readLock();
 		for (ObserverLogic o : observerList)
 		{
 			o.handleSendMessage(message);
 		}
 		observerLock.readUnlock();
 		blackPlayer.handleSendMessage(message);
 		whitePlayer.handleSendMessage(message);
 	}
 
 	public void makePassMove() {
 		boolean gameOver = board.makePassMove(getActiveColor());
 		try {
 			getCurrentPlayer().getServer().getStateManager().updateGameWithPass(this, getCurrentPlayer());
 		} catch (SQLException e) {
 			//unrecoverable, wrap and rethrow.
 			throw new RuntimeException(e);
 		}
 		
 		final Message message = MessageFactory.createMakeMoveMessage(makeGameInfo(), getCurrentPlayer().makeClientInfo(), MessageProtocol.MOVE_PASS, new BoardLocation(0,0), Collections.<BoardLocation>emptyList());
 		
 		broadcastMessage(message);
 		
 		if(gameOver)
 		{
 			doGameOver();
 		}
 		else {
 			advanceTurns();
 		}
 	}
 	
 	public void broadcastMessage(Message message)
 	{
 		observerLock.readLock();
 		for (ObserverLogic o : observerList)
 		{
 			o.handleSendMessage(message);
 		}
 		observerLock.readUnlock();
 		blackPlayer.handleSendMessage(message);
 		whitePlayer.handleSendMessage(message);
 	}
 	
 	public void doMakeMove(BoardLocation loc) {
 		try {
 			Vector<BoardLocation> captured = board.makeMove(loc, getActiveColor());
 			try {
 				getCurrentPlayer().getServer().getStateManager().updateGameWithMove(this, getCurrentPlayer(), loc, captured);
 			} catch (SQLException e) {
 				//unrecoverable, wrap and rethrow.
 				throw new RuntimeException(e);
 			}
 			Message message = MessageFactory.createMakeMoveMessage(makeGameInfo(), 
 					getCurrentPlayer().makeClientInfo(), MessageProtocol.MOVE_STONE, loc, captured);
 			broadcastMessage(message);
 			advanceTurns();
 		} catch (IllegalMoveException e) {
 			//illegal move, we need to send game over error.
 			try {
 				getCurrentPlayer().getServer().getStateManager().updateGameWithMove(this, getCurrentPlayer(), loc, new Vector<BoardLocation>());
 			} catch (SQLException sqle) {
 				//unrecoverable, wrap and rethrow.
 				throw new RuntimeException(sqle);
 			}
 			doGameOverError(e);
 		}
 	}
 	
 	private void advanceTurns() {
 		assert isActive(): "Something tried to advance turns when game is inactive";
 		if (state == GameState.BLACK_MOVE)
 		{
 			state = GameState.WHITE_MOVE;
 		} else if (state == GameState.WHITE_MOVE)
 		{
 			state = GameState.BLACK_MOVE;
 		}
 		handleNextMove();
 	}
 	
 	private void doGameOver() {
 		active = false;
 		getBlackPlayer().getServer().getLog().println("Game over error for " + getBlackPlayer().makeClientInfo() + " and " + getWhitePlayer().makeClientInfo() + ".\n" + board);
 		double blackScore = board.getScore(StoneColor.BLACK);
 		double whiteScore = board.getScore(StoneColor.WHITE);
 		PlayerLogic winner = blackScore > whiteScore ? blackPlayer : whitePlayer;
 		
 		try {
 			getCurrentPlayer().getServer().getStateManager().finishGame(gameID, winner, blackScore, whiteScore, MessageProtocol.GAME_OK);
 		} catch (SQLException e) {
 			//unrecoverable, wrap and rethrow.
 			throw new RuntimeException(e);
 		}
 		
 		final Message message = MessageFactory.createGameOverMessage(makeGameInfo(), blackScore, whiteScore, winner.makeClientInfo());
     	broadcastMessage(message);
 		broadcastTerminate();
 	}
 
 	void doGameOverError(IllegalMoveException reason) {
 		double blackScore = state == GameState.BLACK_MOVE ? 0 : 1;
 		double whiteScore = 1 - blackScore;
 		getBlackPlayer().getServer().getLog().println("Game over error for " + getBlackPlayer().makeClientInfo() + " and " + getWhitePlayer().makeClientInfo() + " with the error " + reason.getMessage());
 		try {
 			getCurrentPlayer().getServer().getStateManager().finishGame(gameID, getInactivePlayer(), blackScore, whiteScore, reason.getReasonByte());
 		} catch (SQLException e) {
 			//unrecoverable, wrap and rethrow.
 			throw new RuntimeException(e);
 		}
 		Message err = MessageFactory.createGameOverErrorMessage(makeGameInfo(), blackScore, whiteScore, 
 				getInactivePlayer().makeClientInfo(), reason.getReasonByte(), getCurrentPlayer().makeClientInfo(), reason.getMessage());
		active = false;
 		broadcastMessage(err);
 		broadcastTerminate();
 	}
 
 	void broadcastTerminate() {
 		//terminates the game for the players.
 		//nothing needs to be done for observers because they hold no state.
 		
 		blackPlayer.terminateGame();
 		whitePlayer.terminateGame();
 		observerLock.writeLock();
 		Iterator<ObserverLogic> i = observerList.iterator();
 		while (i.hasNext())
 		{
 			ObserverLogic obs = i.next();
 			obs.removeGame(this);
 			i.remove();
 		}
 		observerLock.writeUnlock();
 		server.removeGame(this);
 	}
 
 	private PlayerLogic getCurrentPlayer() {
 		if (state == GameState.BLACK_MOVE) { return blackPlayer;}
 		if (state == GameState.WHITE_MOVE) { return whitePlayer;}
 		//if game over, return null.
 		return null;
 	}
 
 	private PlayerLogic getInactivePlayer() {
 		if (state == GameState.BLACK_MOVE) { return whitePlayer;}
 		if (state == GameState.WHITE_MOVE) { return blackPlayer;}
 		//if game over, return null.
 		return null;
 	}
 
 	private StoneColor getActiveColor() {
 		if (state == GameState.BLACK_MOVE) { return StoneColor.BLACK;}
 		if (state == GameState.WHITE_MOVE) { return StoneColor.WHITE;}
 		return null;
 	}
 
 	public boolean isActive() {
 		return active;
 	}
 
 	public void begin() {
 		getCurrentPlayer().beginGame(this);
 	}
 
 	public void setGameID(int gameID) {
 		this.gameID = gameID;
 	}
 	public int getGameID() {
 		return gameID;
 	}
 
 	public void handleNextMove() {
 		getCurrentPlayer().handleNextMove(this);
 	}
 }

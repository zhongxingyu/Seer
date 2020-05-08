 package games.distetris.domain;
 
 import games.distetris.storage.DbHelper;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Vector;
 
 import android.database.Cursor;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 
 public class CtrlDomain {
 
 	public static final int MODE_SERVER = 1;
 	public static final int MODE_CLIENT = 2;
 
 	// controllers
 	private static CtrlDomain INSTANCE = null;
 	private CtrlNet NET = null;
 	private CtrlGame GAME = null;
 
 	private Handler handlerDomain;
 	private Handler handlerUI;
 
 	// dynamic configuration
 	private int mode = 0;
 	private Boolean gameRunning = false;
 
 	// dynamic configuration (player)
 	private Integer playerID = 0;
 	private Integer teamID = 0;
 	private Integer round = 0; //Nivel a partir de ronda
 	private boolean myTurn = false;
 	private Integer myTurns = 0;
 
 	// dynamic configuration (server)
 	// configured by function setConfCreate
 	private String serverName;
 	private Integer serverNumTeams;
 	private Integer serverNumTurns;
 	private Vector<Integer> serverTurnPointer;
 	private Integer serverTeamPointer = 0;
 
 	private CtrlDomain() {
 		this.handlerDomain = new Handler() {
 			@Override
 			public void handleMessage(Message msg) {
 				if (msg.getData().containsKey("MSG")) {
 					parserController(msg.getData().get("MSG").toString());
 				}
 			}
 		};
 	}
 
 	public static CtrlDomain getInstance() {
 		if (INSTANCE == null) {
 			INSTANCE = new CtrlDomain();
 			INSTANCE.NET = CtrlNet.getInstance();
 			INSTANCE.GAME = CtrlGame.getInstance();
 		}
 		return INSTANCE;
 	}
 
 	public void setDbHelper(DbHelper dbHelper) {
 		CtrlGame.getInstance().setDbHelper(dbHelper);
 	}
 	
 	public void closeDb() {
 		CtrlGame.getInstance().closeDb();
 	}
 
 	public int[][] getBoard() {
 		return GAME.getBoard();
 	}
 	
 	/**
 	 * it returns all the players
 	 * @return HashMap of (name -> (Class)Score)
 	 */
 	public HashMap<String,Data> getPlayers() {
 		return this.GAME.getPlayers();
 	}
 
 	private void parserController(String str) {
 		L.d(str);
 		String[] actionContent = str.split(" ", 2);
 		String[] args = null;
 		if (actionContent.length > 1) {
 			args = actionContent[1].split(",");
 		}
 
 		if (actionContent[0].equals("WAITINGROOM")) {
 			// The name of the players and the team they belong
 			// Only used in the *Waiting classes (JoinGameWaiting and NewGameWaiting)
 			// The class sent with the info is WaitingRoom
 
 			WaitingRoom room = (WaitingRoom) unserialize(args[0]);
 
 			// Populate the remaining info that the server left blank
 			room.currentPlayerID = this.playerID;
 			room.currentTeamID = this.teamID;
 
 			// Update the client UI
 			Message msg = new Message();
 			Bundle b = new Bundle();
 			b.putString("type", "WAITINGROOM");
 			b.putSerializable("room", room);
 			msg.setData(b);
 			handlerUI.sendMessage(msg);
 
 		} else if (actionContent[0].equals("SHUTDOWN")) {
 			// The server is closing the connection
 
 			shutdownUI();
 		} else if (actionContent[0].equals("STARTGAME")) {
 			// The server started the game
 			// Clients must leave the *Waiting view and change it to the Game
 
 			this.gameRunning = true;
 
 			Message msg = new Message();
 			Bundle b = new Bundle();
 			b.putString("type", "STARTGAME");
 			msg.setData(b);
 			handlerUI.sendMessage(msg);
 
 		} else if (actionContent[0].equals("UPDATEDBOARD")) {
 			// A client sent a new board
 			// Send this new board to all the clients
 
 			Board b = (Board) unserialize(args[0]);
 			NET.sendUpdatedBoardClients(b);
 			//NET.sendSignals(serialize(b));
 
 		} else if (actionContent[0].equals("UPDATEBOARD")) {
 			// The server sent a new board
 			// Update the UI
 
 			if (!myTurn) {
 				GAME.setBoard((Board) unserialize(args[0]));
 				L.d(" Rebut board amb gameover a " + GAME.getBoardToSend().isGameOver());
 			}
 
 		} else if (actionContent[0].equals("UPDATEMYTURN")) {
 			// The server sent new information about my turn
 
 			this.myTurns = Integer.parseInt(((String) args[0]));
 			this.myTurn = (this.myTurns > 0) ? true : false;
 			L.d("Turns updated. myTurn " + this.myTurn + " myTurns " + this.myTurns);
 
 		} else if (actionContent[0].equals("TURNFINISHED")) {
 			// A client just finished playing all the turns
 			// Notify all the clients with the new turn
 
 			incrementCurrentTurn();
 
 			// Save the board sent by the finished player
 			GAME.setBoard((Board) unserialize(args[0]));
 
 			// Update the current turn of the board
 			GAME.getBoardToSend().setCurrentTurnPlayer(NET.serverTCPGetConnectedPlayer(getCurrentTurn()));
 			GAME.getBoardToSend().refreshCurrentPieceColor();
 
 			// Send the new board to all the clients
 			NET.sendUpdatedBoardClients(GAME.getBoardToSend());
 
 			try {
 				Thread.sleep(500);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 
 			// Send the new turn
 			NET.sendTurns(getCurrentTurn());
 		}
 
 			
 		/*
 		 * 
 		 * 
 		 * 
 		 * SERGIO
 		 * 
 		 * 
 		 * 
 		 */
 			
 //		} else if (actionContent[0].equals("WAITING")) {
 //			// 1: assigned idPlayer
 //			// 2: number of teams
 //			// 3: num of turns
 //			playerID = (playerID == -1) ? playerID : Integer.valueOf(args[0]);
 //			teamID = GAME.windowChoiceTeam(Integer.valueOf(args[1]));
 //			myTurns = Integer.valueOf(args[2]);
 //			NET.sendSignal("JOIN " + String.valueOf(playerID) + "," + String.valueOf(teamID));
 //		} else if (actionContent[0].equals("JOIN")) {
 //			// 1: idPlayer == element position @ vector connections
 //			// 2: chosen team
 //			NET.registerPlayer(Integer.valueOf(args[0]), Integer.valueOf(args[1]));
 //			// TODO: fix it?
 //			/*
 //			if (freeSlots == 0) {
 //				NET.sendSignals("START " + serialize(GAME.getBoard()));
 //				NET.nextPlayer();
 //			}
 //			*/
 //		} else if (actionContent[0].equals("START")) {
 //			// 1: serialized Board
 //			round++;
 //			GAME.setBoard(unserialize(actionContent[1]));
 //			L.d("START");
 //		} else if (actionContent[0].equals("CONTINUE")) {
 //			// NULL
 //			String result = "DO";
 //			myTurn = true;
 //			for (int i = myTurns; i > 0; i--) {
 //				do {
 //					result = GAME.playPiece();
 //					NET.sendSignal("PING " + serialize(GAME.getPiece()));
 //
 //				} while (result.equals("DO"));
 //			}
 //
 //			// testing!
 //			if (round > 3)
 //				result = "END";
 //
 //			myTurn = false;
 //			NET.sendSignal("FINISHED " + result);
 //		} else if (actionContent[0].equals("PING")) {
 //			// 1: serialized Piece
 //			NET.sendSignals("PONG " + actionContent[1]);
 //		} else if (actionContent[0].equals("PONG")) {
 //			// 1: serialized Piece
 //			if (!myTurn) {
 //				GAME.setPiece(unserialize(actionContent[1]));
 //			}
 //		} else if (actionContent[0].equals("FINISHED")) {
 //			// 1: String result
 //			// 2: (if error) String error
 //			if (args[0].equals("END")) {
 //				NET.sendSignals("END " + serialize(GAME.getBoard()));
 //			} else if (args[0].equals("NEXT")) {
 //				GAME.setPiece(args[0]);
 //				NET.sendSignals("START " + serialize(GAME.getBoard()));
 //				NET.nextPlayer();
 //			} else if (args[0].equals("ERROR")) {
 //				NET.sendSignals("ERROR " + args[1]);
 //			} else {
 //				// nothing
 //			}
 //		} else if (actionContent[0].equals("END")) {
 //			// 1: serialized Board
 //			GAME.setBoard(unserialize(actionContent[1]));
 //			// TODO: fix it?
 //			// GAME.saveScore(numPlayers==numTeams);
 //		} else if (actionContent[0].equals("ERROR")) {
 //			// 1: String error
 //			GAME.showError(actionContent[1]);
 //		}
 			
 	}
 	
 	public Cursor getScoreInd() {
 		return GAME.getScoreInd();
 	}
 	
 	public Cursor getScoreTeam() {
 		return GAME.getScoreTeam();
 	}
 
 	// move?
 	public String serialize(Object object) {
 		byte[] result = null;
 
 		try {
 			ByteArrayOutputStream bs = new ByteArrayOutputStream();
 			ObjectOutputStream os = new ObjectOutputStream(bs);
 			os.writeObject(object);
 			os.close();
 			result = bs.toByteArray();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return new String();
 		}
 
 		return games.distetris.domain.Base64.encodeToString(result, games.distetris.domain.Base64.NO_WRAP);
 	}
 
 	// move?
 	public Object unserialize(String str) {
 		Object object = null;
 		byte[] bytes = games.distetris.domain.Base64.decode(str, games.distetris.domain.Base64.NO_WRAP);
 
 		try {
 			ByteArrayInputStream bs = new ByteArrayInputStream(bytes);
 			ObjectInputStream is = new ObjectInputStream(bs);
 			object = (Object) is.readObject();
 			is.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return new String();
 		}
 		return object;
 	}
 
 	public void serverUDPStart() {
 		NET.serverUDPStart();
 	}
 
 	public void serverUDPFind(Handler handler) {
 		NET.serverUDPFind(handler);
 	}
 
 	public void serverUDPStop() {
 		NET.serverUDPStop();
 	}
 
 	public void setWifiManager(WifiManager systemService) {
 		NET.setWifiManager(systemService);
 	}
 
 	
 	// DB HOOKS
 	
 	public String getPlayerName() {
 		return this.GAME.getPlayerName();
 	}
 	
 	public void setPlayerName(String name) {
 		this.GAME.setPlayerName(name);
 	}
 	
 	public Bundle getConfCreate() {
 		return this.GAME.getConfCreate();
 	}
 
 	public void setConfCreate(Bundle b) {
 		this.serverName = b.getString("servername");
 		this.serverNumTeams = Integer.valueOf(b.getString("numteams"));
 		this.serverNumTurns = Integer.valueOf(b.getString("numturns"));
 		this.GAME.setConfCreate(b);
 	}
 	
 	//
 
 	public String getServerName() {
 		return this.serverName;
 	}
 
 	public Integer getServerNumTeams() {
 		return this.serverNumTeams;
 	}
 
 	public Integer getServerNumTurns() {
 		return this.serverNumTurns;
 	}
 
 	public void serverTCPStart() throws Exception {
 		this.mode = MODE_SERVER;
 		this.gameRunning = false;
 		NET.serverTCPStart(serverNumTeams, serverNumTurns);
 	}
 
 	public void serverTCPStop() {
 		NET.serverTCPStop();
 	}
 
 	public void serverTCPConnect(String serverIP, int serverPort) throws Exception {
 		this.mode = MODE_CLIENT;
 		this.gameRunning = false;
 		String result = NET.serverTCPConnect(serverIP, serverPort);
 		this.playerID = Integer.parseInt(result.split("\\|")[0]);
 		this.teamID = Integer.parseInt(result.split("\\|")[1]);
 	}
 
 	public void serverTCPDisconnect() {
 		this.mode = 0;
 		this.gameRunning = false;
 		NET.serverTCPDisconnect();
 	}
 
 	public void serverTCPDisconnectClients() {
 		this.mode = 0;
 		this.gameRunning = false;
 		NET.serverTCPDisconnectClients();
 	}
 
 	public void setHandlerUI(Handler hand) {
 		this.handlerUI = hand;
 	}
 
 	/**
 	 * Notify all the clients that the WaitingRoom has changed because a new
 	 * player joined or a current player disconnected
 	 */
 	public void updatedPlayers() {
 
 		WaitingRoom r = new WaitingRoom();
 
 		// Send the info to all the connected clients
 		NET.sendSignals("WAITINGROOM " + serialize(r));
 	}
 	
 	
 	/**
 	 * Creates the single player structure to save the 
 	 * score
 	 */
 	private void setSinglePlayerStructure(){
 		Vector<Integer> teams = new Vector<Integer>();
 		Vector<String> names = new Vector<String>();
 		
 		teams.add(0);
 		names.add(this.GAME.getPlayerName());
 		this.GAME.getBoardToSend().setCurrentTurnPlayer(this.GAME.getPlayerName());
 		this.GAME.setPlayers(teams, names);
 	}
 	
 	/**
 	 * Starts a new game
 	 * 
 	 * SinglePlayer : 
 	 * Just create a new board and starts the game
 	 * 
 	 * MultiPlayer:
 	 * Send to all the connected clients that the game is going to start. The
 	 * clients must be on *Waiting views.
 	 */
 	public void startGame() {
 		if(this.isSingleplay()){
 			this.GAME.createNewCleanBoard();
 			this.setSinglePlayerStructure();
 			this.GAME.InitRandomPieces();
 			
 		}
 		else{
 			initializeTurns();
 			this.GAME.createNewCleanBoard();
 			this.GAME.setPlayers( this.NET.serverTCPGetConnectedPlayersTeam(),
 				this.NET.serverTCPGetConnectedPlayersName());
 			this.GAME.getBoardToSend().setCurrentTurnPlayer(this.NET.serverTCPGetConnectedPlayer(getCurrentTurn()));
 			this.GAME.InitRandomPieces();
 			this.NET.sendSignalsStartGame();
 			
 		}
 	}
 	/**
 	 * The UI wants to close the running game
 	 */
 	public void stopGame() {
 
 		if (mode == CtrlDomain.MODE_CLIENT) {
 			serverTCPDisconnect();
 		} else if (mode == CtrlDomain.MODE_SERVER) {
 			serverTCPDisconnectClients();
 		}
 	}
 
 	/**
 	 * Notifies the UI that some error with the connection occurred
 	 */
 	public void shutdownUI() {
 		Message msg = new Message();
 		Bundle b = new Bundle();
 		b.putString("type", "SHUTDOWN");
 		msg.setData(b);
 		handlerUI.sendMessage(msg);
 	}
 
 	/**
 	 * A disconnection has been detected. Notify the UI if in client mode or
 	 * notify the rest of the clients if in server mode.
 	 * 
 	 * @param conn
 	 */
 	public void disconnectionDetected(TCPConnection conn) {
 
 		conn.close();
 
 		if (mode == CtrlDomain.MODE_CLIENT) {
 
 			// If in client mode, notify the UI about the shutdown
 			shutdownUI();
 
 		} else if (mode == CtrlDomain.MODE_SERVER && !gameRunning) {
 
 			// This happens when there is a disconnection in the WaitingRoom
 
 			CtrlNet.getInstance().removePlayer(conn);
 			updatedPlayers();
 
 		} else if (mode == CtrlDomain.MODE_SERVER && gameRunning) {
 
 			// This happens when there is a disconnection while playing a game
 
 			CtrlNet.getInstance().sendShutdown();
 		}
 
 
 	}
 
 	/*
 	 * 
 	 * 
 	 * 
 	 * GAME HOOKS
 	 * 
 	 * 
 	 * 
 	 */
 
 	public void GameOverActionsOther(){
 		this.GAME.GameOverActions();
 	}
 	
 	/**
 	 * Actions to do when a GameOver is found
 	 */
 	public void GameOverActionsLoser(){
 		this.GAME.GameOverActions();
 		try {
 			this.NET.sendUpdatedBoardServer();
 		} catch (Exception e) {
 			this.shutdownUI();
 		}
 	}
 	
 	public ArrayList<Integer> cleanBoard(){
 		return this.GAME.cleanBoard();
 	}
 	
 	public void setNewRandomPiece(){
 		this.GAME.setNewRandomPiece();
 		
 		notifyAddedPiece();
 		
 	}
 	
 	public Piece getCurrentPiece(){
 		if(this.GAME.getCurrentPiece() == null){
 			this.setNewRandomPiece();
 		}
 		return this.GAME.getCurrentPiece();
 	}
 	
 	public void gameStep(){
 		this.GAME.gameStep();
		try {
			this.NET.sendUpdatedBoardServer();
		} catch (Exception e) {
			shutdownUI();
 		}
 	}
 	
 	public boolean currentPieceCollision(){
 		return this.GAME.currentPieceCollision();
 	}
 	
 	public boolean nextStepPieceCollision(){
 		return this.GAME.nextStepPieceCollision();
 	}
 	
 	public void currentPieceRotateLeft(){
 		this.GAME.rotateLeft();
 	}
 	
 	public void currentPieceRotateRight(){
 		this.GAME.rotateRight();
 	}
 	
 	public boolean currentPieceOffsetCollision(int offset){
 		return this.GAME.currentPieceOffsetCollision(offset);
 	}
 	
 	/**
 	 * Adds a current piece to the board
 	 * and if not single player sends
 	 * the new board
 	 */
 	public void addCurrentPieceToBoard(){
 		// Add the piece to the game logic
 		this.GAME.addCurrentPieceToBoard();
 		
 	}
 	
 	public Piece getNextPiece(){
 		return this.GAME.getNextPiece();
 	}
 	
 	public boolean isGameOver(){
 		return this.GAME.isGameOver() || this.GAME.BoardGameOverSet();
 	}
 
 	public Handler getHandlerDomain() {
 		return this.handlerDomain;
 	}
 
 	public boolean currentPieceCollisionRC(int row, int col) {
 		return this.GAME.currentPieceCollisionRC(row,col);
 	}
 
 	public boolean isMyTurn() {
 		return this.myTurn;
 	}
 
 	public void setIsMyTurn(boolean myt){
 		this.myTurn = myt;
 	}
 	
 	private void initializeTurns() {
 
 		this.serverTeamPointer = 0;
 
 		this.serverTurnPointer = new Vector<Integer>();
 
 		for (int i = 0; i < this.NET.serverTCPGetNumTeams(); i++) {
 			serverTurnPointer.add(0);
 		}
 
 		L.d("Turns initialized with teams: " + this.NET.serverTCPGetNumTeams());
 		for (int i = 0; i < this.NET.serverTCPGetNumTeams(); i++) {
 			L.d("Team " + i + " number players: " + this.NET.serverTCPGetPlayersTeam(i).size());
 		}
 
 		L.d("Vector: " + this.serverTurnPointer);
 	}
 
 	public Integer getCurrentTurn() {
 		Integer aix = this.serverTurnPointer.get(this.serverTeamPointer);
 		Integer aux = this.NET.serverTCPGetPlayersTeam(this.serverTeamPointer).get(aix);
 		return this.NET.serverTCPGetPosFromId(aux);
 	}
 
 	public void incrementCurrentTurn() {
 		this.serverTurnPointer.set(this.serverTeamPointer, ((this.serverTurnPointer.get(this.serverTeamPointer) + 1) % this.NET.serverTCPGetPlayersTeam(this.serverTeamPointer).size()));
 		this.serverTeamPointer = (this.serverTeamPointer + 1) % this.NET.serverTCPGetNumTeams();
 
 		L.d("Nuevo equipo: " + serverTeamPointer);
 		L.d("Nuevo turn del equipo: " + this.serverTurnPointer.get(this.serverTeamPointer));
 	}
 
 	/**
 	 * Drop The piece to the bottom
 	 */
 	public void currentPieceFastFall() {
 		this.GAME.currentPieceFastFall();
 		
 		notifyAddedPiece();
 
 	}
 
 	public void setSingleplay(boolean singleplay) {
 		GAME.setSingleplay(singleplay);
 	}
 
 	public boolean isSingleplay() {
 		return GAME.isSingleplay();
 	}
 
 	public void notifyAddedPiece() {
 
 		// Countdown the number of turns left
 		this.myTurns--;
 		if (this.myTurns == 0) {
 			this.myTurn = false;
 		}
 
 		if (!isSingleplay()) {
 
 			if (this.myTurn) {
 				// Update the server with the new board
 				try {
 					this.NET.sendUpdatedBoardServer();
 				} catch (Exception e) {
 					shutdownUI();
 				}
 
 				// If it was my last turn, notify the server
 			} else if (!this.myTurn) {
 				try {
 					this.NET.sendTurnFinished();
 				} catch (Exception e) {
 					shutdownUI();
 				}
 			}
 		}
 	}
 
 	public void createCleanBoard() {
 		CtrlGame.getInstance().createNewCleanBoard();
 	}
 
 }

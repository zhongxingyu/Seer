 package games.distetris.domain;
 
 import games.distetris.storage.DbHelper;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 
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
 
 	// dynamic configuration (player)
 	private Integer playerID = 0;
 	private Integer teamID = 0;
 	private Integer round = 0; //Nivel a partir de ronda
 	private boolean myTurn = false;
 	private Integer myTurns = 0;
 
 	// dynamic configuration (server)
 	// configured by function serverConfigure()
 	private String serverName;
 	private Integer serverNumTeams;
 	private Integer serverNumTurns;
 	private Integer serverTurnPointer;
 
 	private CtrlDomain() {
 		L.d("Created");
 
 		this.handlerDomain = new Handler() {
 			@Override
 			public void handleMessage(Message msg) {
 				if (msg.getData().containsKey("MSG")) {
 					try {
 						parserController(msg.getData().get("MSG").toString());
 					} catch (Exception e) {
 						// TODO: notify the UI about the disconnection
 						e.printStackTrace();
 					}
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
 
 	public int[][] getBoard() {
 		return GAME.getBoard();
 	}
 
 	private void parserController(String str) throws Exception {
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
 
 			Message msg = new Message();
 			Bundle b = new Bundle();
 			b.putString("type", "SHUTDOWN");
 			msg.setData(b);
 			handlerUI.sendMessage(msg);
 		} else if (actionContent[0].equals("STARTGAME")) {
 			// The server started the game
 			// Clients must leave the *Waiting view and change it to the Game
 
 			Message msg = new Message();
 			Bundle b = new Bundle();
 			b.putString("type", "STARTGAME");
 			msg.setData(b);
 			handlerUI.sendMessage(msg);
 
 		} else if (actionContent[0].equals("UPDATEBOARD")) {
 			// The server sent a new board
 			// Update the UI
 
 			if (!myTurn) {
 				GAME.setBoard((Board) unserialize(args[0]));
 			}
 
 		} else if (actionContent[0].equals("UPDATEMYTURN")) {
			this.myTurn = (Boolean) unserialize(args[0]);
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
 
 	public String getPlayerName() {
 		return GAME.getPlayerName();
 	}
 	
 	public void setPlayerName(String name) {
 		GAME.setPlayerName(name);
 	}
 
 	public void serverConfigure(String name, int numTeams, int numTurns) {
 		this.serverName = name;
 		this.serverNumTeams = numTeams;
 		this.serverNumTurns = numTurns;
 		this.serverTurnPointer = 0;
 	}
 
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
 		NET.serverTCPStart(serverNumTeams, serverNumTurns);
 	}
 
 	public void serverTCPStop() {
 		NET.serverTCPStop();
 	}
 
 	public void serverTCPConnect(String serverIP, int serverPort) throws Exception {
 		this.mode = MODE_CLIENT;
 		String result = NET.serverTCPConnect(serverIP, serverPort);
 		this.playerID = Integer.parseInt(result.split("\\|")[0]);
 		this.teamID = Integer.parseInt(result.split("\\|")[1]);
 	}
 
 	public void serverTCPDisconnect() {
 		this.mode = 0;
 		NET.serverTCPDisconnect();
 	}
 
 	public void serverTCPDisconnectClients() {
 		this.mode = 0;
 		NET.serverTCPDisconnectClients();
 	}
 
 	public void setHandlerUI(Handler hand) {
 		this.handlerUI = hand;
 	}
 
 	public void updatedPlayers() {
 
 		//new_player.out("WAITING " + (players.size()) + "," + (numTeams - 1) + "," + numTurns);
 
 		WaitingRoom r = new WaitingRoom();
 
 		// Send the info to all the connected clients
 		NET.sendSignals("WAITINGROOM " + serialize(r));
 	}
 	
 	/**
 	 * Send to all the connected clients that the game is going to start. The
 	 * clients must be on *Waiting views.
 	 */
 	public void startGame() {
 		this.GAME.createNewCleanBoard();
 		this.NET.sendTurns(this.serverTurnPointer);
 		this.NET.sendUpdatedBoard();
 		this.NET.sendSignals("STARTGAME");
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
 
 	public ArrayList<Integer> cleanBoard(){
 		return this.GAME.cleanBoard();
 	}
 	
 	public void setNewRandomPiece(){
 		this.GAME.setNewRandomPiece();
 		
 	}
 	
 	public Piece getCurrentPiece(){
 		if(this.GAME.getCurrentPiece() == null){
 			this.setNewRandomPiece();
 		}
 		return this.GAME.getCurrentPiece();
 	}
 	
 	public void gameStep(){
 		this.GAME.gameStep();
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
 	
 	public void addCurrentPieceToBoard(){
 		this.GAME.addCurrentPieceToBoard();
 		this.NET.sendUpdatedBoard();
 	}
 	
 	public Piece getNextPiece(){
 		return this.GAME.getNextPiece();
 	}
 	
 	public boolean isGameOver(){
 		return this.GAME.isGameOver();
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
 
 	/**
 	 * Drop The piece to the bottom
 	 */
 	public void currentPieceFastFall() {
 		this.GAME.currentPieceFastFall();	
 	}
 
 }

 package net.client;
 
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import model.MeepleStruct;
 import model.PlayerStruct;
 import ui.GameUi;
 
 // Adapter class which receives the returned messages from the server.
 // The received messages are processed, followed by the client being told to
 // update itself.
 public class ClientProtocol extends SocketClientProtocol {
 
 	private GameUi gameUi;
 
 	public ClientProtocol(GameUi gameUi) {
 		this.gameUi = gameUi;
 	}
 
 	@Override
 	public ArrayList<String> processInput(Socket sender, String input) {
 
 		// Convert the input string into a list for processing.
 		List<String> message = Arrays.asList(input.split(";"));
 
 		// Pre-game messages.
 
 		if (message.get(0).equals(SocketClientProtocol.EXIT)) {
 			gameUi.exit();
 		}
 
 		// ASSIGNPLAYER;player;<int>
 		if (message.get(0).equals("ASSIGNPLAYER")) {
 
 			int player = Integer.parseInt(message.get(2));
 
 			gameUi.assignPlayer(player);
 		}
 
 		// UPDATELOBBY[;player;<int>;name;<string>;color;<string:(RGB)>]+
 		if (message.get(0).equals("UPDATELOBBY")) {
 
 			HashMap<Integer, PlayerStruct> players;
 			players = new HashMap<Integer, PlayerStruct>();
 
 			// Parse the message, and place each player in a map to pass on.
 			for (int i = 1; i < message.size(); i = i + 6) {
 
 				int rep = Integer.parseInt(message.get(i + 1));
 				String name = message.get(i + 3);
 				String color = message.get(i + 5);
 
 				players.put(rep, new PlayerStruct(name, color));
 			}
 
 			gameUi.updateLobby(players);
 		}
 
 		// In-game messages.
 
 		// INIT;currentPlayer;<int>;gameBoardWidth;<int>;gameBoardHeight;<int>
 		if (message.get(0).equals("INIT")) {
 
 			int currentPlayer = Integer.parseInt(message.get(2));
 			int width = Integer.parseInt(message.get(4));
 			int height = Integer.parseInt(message.get(6));
 
 			gameUi.init(currentPlayer, width, height);
 		}
 
 		// DRAWTILE;currentPlayer;<int>;identifier;<string>;orientation;<int:[0-3]>
 		if (message.get(0).equals("DRAWTILE")) {
 
 			int currentPlayer = Integer.parseInt(message.get(2));
 			String identifier = message.get(4);
 			int orientation = Integer.parseInt(message.get(6));
 
 			gameUi.drawTile(currentPlayer, identifier, orientation);
 		}
 
 		// ROTATETILE;currentPlayer;<int>;direction;<string:(clockwise|counterClockwise)>
 		if (message.get(0).equals("ROTATETILE")) {
 
 			int currentPlayer = Integer.parseInt(message.get(2));
 			String direction = message.get(4);
 
 			gameUi.rotateTile(currentPlayer, direction);
 		}
 
 		// PLACETILE;currentPlayer;<int>;xBoard;<int>;yBoard;<int>;error;<int:(0|1)>
 		if (message.get(0).equals("PLACETILE")) {
 
 			int currentPlayer = Integer.parseInt(message.get(2));
 			int xBoard = Integer.parseInt(message.get(4));
 			int yBoard = Integer.parseInt(message.get(6));
 			int err = Integer.parseInt(message.get(8));
 
 			gameUi.placeTile(currentPlayer, xBoard, yBoard, err);
 		}
 
		// PLACEMEEPLE;currentPlayer;<int>;xBoard;<int>;yBoard;<int>;xTile;<int>;yTile;<int>;error;<int:(0|1)>
 		if (message.get(0).equals("PLACEMEEPLE")) {
 
 			int currentPlayer = Integer.parseInt(message.get(2));
 			int xBoard = Integer.parseInt(message.get(4));
 			int yBoard = Integer.parseInt(message.get(6));
 			int xTile = Integer.parseInt(message.get(8));
 			int yTile = Integer.parseInt(message.get(10));
 			int err = Integer.parseInt(message.get(12));
 
 			gameUi.placeMeeple(currentPlayer, xBoard, yBoard, xTile, yTile, err);
 		}
 
 		// SCORE[;meeple;xBoard;<int>;yBoard;<int>;xTile;<int>;yTile;<int>]*
 		if (message.get(0).equals("SCORE")) {
 
 			Set<MeepleStruct> meeplePositions = new HashSet<MeepleStruct>();
 
 			for (int i = 1; i < message.size(); i = i + 9) {
 
 				int xBoard = Integer.parseInt(message.get(i + 2));
 				int yBoard = Integer.parseInt(message.get(i + 4));
 				int xTile = Integer.parseInt(message.get(i + 6));
 				int yTile = Integer.parseInt(message.get(i + 8));
 
 				MeepleStruct ms = new MeepleStruct(xBoard, yBoard, xTile, yTile);
 				meeplePositions.add(ms);
 			}
 
 			gameUi.score(meeplePositions);
 		}
 
 		// ENDTURN;currentPlayer;<int>
 		if (message.get(0).equals("ENDTURN")) {
 
 			int currentPlayer = Integer.parseInt(message.get(2));
 
 			gameUi.endTurn(currentPlayer);
 		}
 
 		// INFO;player;<int>;currentPlayer;<int:(0|1)>;score;<int>;meeplesPlaced;<int>
 		if (message.get(0).equals("INFO") && message.get(1).equals("player")) {
 
 			int player = Integer.parseInt(message.get(2));
 			int currentPlayer = Integer.parseInt(message.get(4));
 			int playerScore = Integer.parseInt(message.get(6));
 			int meeplesPlaced = Integer.parseInt(message.get(8));
 
 			gameUi.playerInfo(player, currentPlayer, playerScore, meeplesPlaced);
 		}
 
 		// INFO;game;currentPlayer;<int>;drawPileEmpty;<int:(0|1)>
 		if (message.get(0).equals("INFO") && message.get(1).equals("game")) {
 
 			int currentPlayer = Integer.parseInt(message.get(3));
 			int isDrawPileEmpty = Integer.parseInt(message.get(5));
 			boolean drawPileEmpty = (isDrawPileEmpty == 0) ? false : true;
 
 			gameUi.gameInfo(currentPlayer, drawPileEmpty);
 		}
 
 		return null;
 	}
 }

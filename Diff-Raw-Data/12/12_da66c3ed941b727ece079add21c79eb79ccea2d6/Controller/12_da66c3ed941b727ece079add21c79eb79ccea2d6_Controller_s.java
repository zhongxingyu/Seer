 package controller;
 
 import java.util.Random;
 import java.util.ArrayList;
 import gameplay.Logic;
 import player.*;
 import units.*;
 import terrain.Tile;
 
 public class Controller {
 	
 	private final int MOVE = 0;
 	private final int ATTACK = 1;
 	private final int CAPTURE = 2;
 	private final int JOIN = 3;
 
 	Player firstPlayer;
 	Player secondPlayer;
 	boolean aiOn;
 	Logic log;
 	int playerTurn;
 
 	int x, y;
 
 	char selected = 'n';
 	int[] axis; //axis[0] = x and axis[1] = y
 
 	public Controller(Player p1, Player p2, boolean isAIOn, String mapName) {
 		firstPlayer = p1;
 		secondPlayer = p2;
 		aiOn = isAIOn;
 
 		axis = new int[2];
 		
 		if (aiOn) {
 			secondPlayer = new AI(secondPlayer.getPName(), 2, secondPlayer.getFact());
 		}
 
 		log = new Logic ("MapName", firstPlayer.getFact(), secondPlayer.getFact(), p1.getPName(), 
 				secondPlayer.getPName(), aiOn);
 
 		playerTurn = whoGoesFirst();	
 	}
 
 	/**
 	 * This is a simple method that simply tells the GUI whos turn it is. 
 	 * 
 	 * @return
 	 */
 	public int whosTurn() {
 		return playerTurn;
 	}
 
 	/**
 	 * You chose a tile and the controller will check to see if its a unit first if it is not a
 	 * unit then it will check to see if it's a production building. If it is not a production 
 	 * building then it will say there are no options for that particular tile. 
 	 * 
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	public ArrayList<String> selectCoordinates(int x, int y) {
 		ArrayList<String> actions = new ArrayList<String>();
 		
 		axis[0] = x;
 		axis[1] = y;
 
 		if (log.getUB()[x][y] != null)
 			actions = unitActions(actions, x, y);
 
 		else if (log.getTBoard()[x][y].getType() == 'q' || log.getTBoard()[x][y].getType() == 'Q')
 			actions = produceActions(actions, x, y);
 
 		else
 			return null;
 
 		return actions;
 	}
 
 
 	/**
<<<<<<< HEAD
	 * This method produces all of the actions for the unit producing buildlings. At the moment
=======
 	 * This method produces all of the actions for the unit producing buildings. At the moment
>>>>>>> origin/master
 	 * this is limited to only ground forces. 
 	 * 
 	 * @param pActions
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	private ArrayList<String> produceActions(ArrayList<String> pActions, int x, int y) {
 		Tile[][] board = log.getTBoard();
 		ArrayList<String> actions = new ArrayList<String>();
 
 		Tile tempTile = board[x][y];
 		if(tempTile.getType() == 'p'){
 			if(tempTile.getHasProduced() == false){
 				if(tempTile.getOwner() == playerTurn){
 					actions.add("Build Unit");
 					selected = 'p';
 				}
 			}
 		}
 			
 			return actions;
 	}
 
 	
 	public void endTurn() {
 		if (playerTurn == 0) {
 			log.econDay(secondPlayer);
 			playerTurn = 1;
 		}
 		else {
 			log.econDay(firstPlayer);
 			playerTurn = 0;
 		}
 	}
 
 	public void unitTakeAction(int unitType, int action) {
 		if (action == MOVE) {
 			
 		} else if (action == JOIN) {
 			
 		} else if (action == ATTACK) {
 			
 		} else if (action == CAPTURE) {
 			
 		} else {
 			//ERROR WILL ROBINSON ERROR!
 		}
 	}
 	
 	//so the player decides to produce a unit, so we call this method and we send back an array 
 	//of strings so that the GUI can display array of strings in a menu as possible buys. 
 	//then the GUI will send back a String or something that will tell us which unit to 
 	//produce.
 	public ArrayList<String> prodUnit() {
 		ArrayList<String> toProduce = new ArrayList<String>();
 		Store store = new Store();
 		
 		int munny = 0; 
 		if (playerTurn == 0) {
 			munny = log.getP1().getCash();
 		} else {
 			munny = log.getP2().getCash();
 		}
 		
 		ArrayList<Unit> canBuild = new ArrayList<Unit>();
 		canBuild = store.buyGroundUnit(munny);
 		
 		for (int i = 0; i < canBuild.size(); i++) {
 			toProduce.add(canBuild.get(i).getName());
 		}
 		
 		return toProduce;
 	}
 	
 	public void produceUnit(String toProd) {
 		
 	}
 
 	private ArrayList<String> unitActions(ArrayList<String> actions, int x, int y) {
 		Unit selUnit = log.getUnit(x, y);
 		Logic tempLog = log;
 		char[][] possibleMoves = tempLog.getMoves(selUnit);
 		boolean canMove = false;
 
 		for (int c = 0; c < possibleMoves.length; c++) {
 			for(int r = 0; r < possibleMoves.length; r++) {
 				if (canMove != true) {
 					if (!(possibleMoves[r][c] == '-') && !selUnit.getHasMoved()) {
 						actions.add("Move");
 						canMove = true;
 						selected = 'u';
 					}
 				} else
 					break;
 			}
 			if (canMove == true) 
 				break;
 		}
 
 		Unit[][] unitBoard = log.getUB();
 
 		for (int c = 0; c < unitBoard.length && c < selUnit.getAtkRange(); c++) {
 			for (int r = 0; r < unitBoard.length; r++) {
 				if (unitBoard[r][c] != null) {
 					Unit otherUnit = unitBoard[r][c];
 					if (otherUnit.getOwner() != playerTurn && !selUnit.getHasAttacked()) {
 						actions.add("Attack");
 					}
 				}
 			}
 		}
 
 		Tile[][] tileBoard = log.getTBoard();
 
 		if (playerTurn == 0) {
 			if (tileBoard[x][y].getType() == 'q' || tileBoard[x][y].getType() == 'p') {
 				actions.add("Capture");
 			}
 		} else {
 			if (tileBoard[x][y].getType() == 'Q' || tileBoard[x][y].getType() == 'P') {
 
 				actions.add("Capture");
 			}
 		}
 
 		return actions;
 	}
 
 	private int whoGoesFirst() {
 		Random rand = new Random();
 		int answer = 0;
 
 		answer = rand.nextInt(10);
 		answer = answer % 2;
 
 		return answer;
 	}
 
 	/**
 	 * I really think this method should be called when they click on a unit in the first
 	 * place as with the game.   So if they were to click on a unit, all possible moves that
 	 * unit can take are highlighted on screen before they choose to move
 	 * @return
 	 */
 	public char[][] move(){
 		Unit[][] temp = log.getUB();
 		return log.getMoves(temp[x][y]);
 	}
 
 //	/**
 //	 * I really think this method should be called when they click on a unit in the first
 //	 * place as with the game.   So if they were to click on a unit, all possible moves that
 //	 * unit can take are highlighted on screen before they choose to move
 //	 * @return
 //	 */
 //	private char[][] move(int x, int y){
 //		Unit[][] temp = log.getUB();
 //		return log.getMoves(temp[x][y]);
 //	}
 
 
 }

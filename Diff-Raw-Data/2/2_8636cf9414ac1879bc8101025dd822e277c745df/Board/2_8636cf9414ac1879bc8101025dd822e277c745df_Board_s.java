 package model;
 
 import java.util.HashMap;
 import java.util.ArrayList;
 
 /**
  * Class to model the game board. <br>
  * Contains accessors for the players, pawns, dice rolls, etc.
  * @author Luuk, Dustin
  *
  */
 public class Board {
 	private Player[] players;
 	private Field[] gameBoard;
 	private HashMap<Player, Field[]> playerEndMap;
 	
 	// Constructor that takes a 2D array of player types and strategies
 	public Board(String[][] options){
 		gameBoard = new Field[40];
 		players = new Player[4];
 		for(int i=0; i<40; i++){
 			gameBoard[i] = new Field();
 		}
 		for(int i=0; i<4 ; i++){
 			if ("Human".equals(options[i][0])){
 				players[i] = new HumanPlayer(i+1);
 			}
 			else {
 				if ("Move First".equals(options[i][1])){
 					players[i] = new ComputerPlayer(i+1, new MoveFirstStrategy());
 				}
 				else if ("Move Last".equals(options[i][1])){
 					players[i] = new ComputerPlayer(i+1, new MoveLastStrategy());
 				}
 				else if ("Capture".equals(options[i][1])){
 					players[i] = new ComputerPlayer(i+1, new CaptureStrategy());
 				}
 				else if ("Cautious".equals(options[i][1])){
 					players[i] = new ComputerPlayer(i+1, new CautiousStrategy());
 				}
 				else {
 					players[i] = new ComputerPlayer(i+1, new RandomStrategy());
 				}
 			}
 		}
 		for(Player player: players){
 			gameBoard[player.getStartPosition()] = new StartTile(player);
 		}
 		playerEndMap = new HashMap<Player, Field[]>();
 		for (Player player: players){
 			playerEndMap.put(player, new Field[4]);
 			Field[] EndMap = playerEndMap.get(player);
 			for(int i=0;i<4;i++){
 				EndMap[i] = new Field();
 			}
 		}
 	}
 	
 	/*==========================
 	 *   GETTERS AND SETTERS
 	 *==========================*/
 	
 	public Player getPlayer(int playerNumber){
 		return this.players[playerNumber - 1];
 	}
 	
 	public Player[] getPlayers(){
 		return this.players;
 	}
 	
 	public Field[] getBoard() {
 		return this.gameBoard;
 	}
 	
 	/**
 	 * @return Returns an array containing all the Pawn objects on the game board
 	 */
 	public Pawn[] getPawns(){
 		int count = 0;
 		Pawn[] allPawns = new Pawn[players.length*4];
 		for(Player player: players){
 			for(Pawn pawn: player.getPawns()){
 				allPawns[count++] = pawn;
 			}
 		}
 		return allPawns;
 	}
 	
 	/*===============
 	 *  METHODS
 	 *===============*/
 	
 	/**
 	 * @param owner of the EndMap you want to check
	 * @return Returns integer of closest pawn to the standard
 	 */
 	private int getClosestPawnInGoal(Player owner, Pawn pawn){
 		int standard = 44;
 		for(Pawn temp:owner.getPawns()){
 			if(temp.getPosition() < standard && temp.getPosition() > pawn.getPosition() && temp.getPosition() >= 40){
 				standard = temp.getPosition();
 			}
 		}
 		return standard;
 	}
 	
 	/**
 	 * Checks the given player's pawns for any pawns that are able to be moved based on the die roll
 	 * @return Returns all pawns that can be moved
 	 */
 	public ArrayList<Pawn> getMoveablePawns(int currentRoll, Player owner){
 		int startpos = owner.getStartPosition();
 		ArrayList<Pawn> MoveablePawns = new ArrayList<Pawn>();
 		//if a six is rolled and the spot in front of the home is not alreadyt occupied by one of the owners
 		//pawns, if possible move a pawn out from home
 		if(currentRoll == 6 && gameBoard[startpos].getPawnOwner() != owner && owner.getPawnsAtHome() != 0){
 			for(Pawn pawn: owner.getPawns()){
 				if(pawn.getPosition() == -1){
 					MoveablePawns.add(pawn); //pawn.setIsMoveable(true);
 				}else{
 					continue;
 				}
 			}
 			return MoveablePawns;
 		}
 		for(Pawn pawn: owner.getPawns()){
 			if(pawn.getPosition() == -1){
 				continue;
 			}
 			int currentpos = pawn.getPosition();
 			//if pawn is in end goal
 			//check each spot to make sure it doesnt pass the goal fork
 			for(int i=1;i<=currentRoll;i++){
 				if(currentpos >= 40){
 					if(getClosestPawnInGoal(owner,pawn) > currentRoll + currentpos && currentRoll + currentpos < 45){
 						MoveablePawns.add(pawn);
 						break;
 					}else{
 						break;
 					}
 				}
 				if(gameBoard[(currentpos + i) % 40] instanceof StartTile && gameBoard[(currentpos + i) % 40].getForkOwner() == owner){
 					int remainingMoves = currentRoll - i;
 					if(remainingMoves >= 4){
 						break;
 					}
 					//make sure the pawn doesnt pass another in the goal area
 					if(getClosestPawnInGoal(owner,pawn) > remainingMoves + 40){
 						MoveablePawns.add(pawn);
 						break;
 					}else{
 						break;
 					}
 				}
 				if( i == currentRoll){
 					if(gameBoard[(currentpos + i) % 40].getPawnOwner() == pawn.getOwner()){
 						continue;
 					}else{
 						MoveablePawns.add(pawn);
 					}
 				}
 			}
 		}
 		return MoveablePawns;
 	}
 	
 	/**
 	 * Resets the game board, pawns, players, etc.
 	 * Called when selecting start new game
 	 */
 	public void reset(){
 		for(Player player: this.players){
 			player.setPawnsAtHome(4);
 			Pawn[] pawns = player.getPawns();
 			Field[] EndMap = this.playerEndMap.get(player);
 			for(int i=0;i<40;i++){
 				this.gameBoard[i].setOccupant(null);
 				this.gameBoard[i].setPawnOwner(null);
 			}
 			for(int i=0;i<4;i++){
 				EndMap[i].setOccupant(null);
 				EndMap[i].setPawnOwner(null);
 			}
 			for(Pawn pawn:pawns){
 				pawn.setTilesMoved(0);
 				pawn.setPosition(-1);
 			}
 		}
 	}
 	
 	/**
 	 * Sends the pawn at the given position back to the owner's home
 	 * @param integer position of the pawn
 	 */
 	private Player sendPawnHome(int pos){
 		Pawn temp = gameBoard[pos].getOccupant();
 		temp.setPosition(-1);
 		temp.getOwner().incrementPawnsAtHome();
 		temp.setTilesMoved(0);
 		return temp.getOwner();
 	}
 	
 	/**
 	 * Makes a move for a computer player based on its strategy
 	 * @param The current die roll
 	 * @param The current player object
 	 */
 	public Move makeMove(int currentRoll, Player player){
 		Move move = null;
 		if(player instanceof ComputerPlayer){
 			Pawn pawn = ((ComputerPlayer) player).makeMove(currentRoll, this.getMoveablePawns(currentRoll, player), gameBoard);
 			if(pawn != null){
 				move = makeMove(pawn, currentRoll);
 			}else{
 				return null;
 			}
 		}else{
 			//error, should never happen as it is run only on computer players
 			System.exit(1);
 		}
 		return move;
 		
 	}
 	
 	/**
 	 * Moves the pawn selected by the player "currentRoll" number of spaces
 	 * @param Pawn object to be moved
 	 * @param The current die roll
 	 */
 	public Move makeMove(Pawn pawn, int currentRoll){
 		Move move = new Move();
 		move.pawn = pawn;
 		int currentpos = pawn.getPosition();
 		move.startPosition = currentpos;
 		
 		// if pawn clicked is at home, put it in the start position
 		if(pawn.getPosition() == -1){
 			int startpos = pawn.getOwner().getStartPosition();
 			if(gameBoard[startpos].getOccupant() != null){
 				move.collision = sendPawnHome(startpos);
 			}
 			updateField(startpos, pawn);
 			pawn.getOwner().decrementPawnsAtHome();
 			pawn.setPosition(pawn.getOwner().getStartPosition());
 			pawn.incrementTilesMoved(1);
 		}else{
 			//move the pawn the given number of slots, all error handling is done by the getMovablePawns method
 			for(int i=1;i<=currentRoll;i++){
 				if(currentpos >= 40){
 					Field[] EndMap = this.playerEndMap.get(pawn.getOwner());
 					EndMap[currentpos - 40].setOccupant(null);
 					EndMap[currentpos - 40].setPawnOwner(null);
 					EndMap[currentpos + currentRoll - 40].setOccupant(pawn);
 					EndMap[currentpos + currentRoll - 40].setPawnOwner(pawn.getOwner());
 					pawn.setPosition(currentpos + currentRoll);
 					pawn.incrementTilesMoved(currentRoll);
 					break;	
 				}
 				if(gameBoard[(currentpos + i) % 40].getForkOwner() == pawn.getOwner()){
 					updateField(currentpos,null);
 					Field[] EndMap = playerEndMap.get(pawn.getOwner());
 					EndMap[currentRoll-i].setOccupant(pawn);
 					EndMap[currentRoll-i].setPawnOwner(pawn.getOwner());
 					pawn.setPosition(40 + (currentRoll-i));
 					pawn.incrementTilesMoved(currentRoll);
 					return move;
 				}
 				if(i == currentRoll){
 					if(gameBoard[(currentpos + i) % 40].getOccupant() != null){
 						move.collision = sendPawnHome((currentpos + i) % 40);
 					}
 					updateField(currentpos, null);
 					updateField((currentpos + i) % 40, pawn);
 					pawn.setPosition((currentpos + i) % 40);
 					pawn.incrementTilesMoved(currentRoll);
 				}
 			}
 		}
 		return move;	
 	}
 	
 	/**
 	 * Checks if the given player has all their pawns in their goal fields
 	 * @param player to be checked for winning condition
 	 * @return Returns true if the player has won the game, false otherwise
 	 */
 	public Boolean HasWon(Player player){
 		for(Field field:playerEndMap.get(player)){
 			if(field.getOccupant() == null){
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Updates both the field tile the given pawn is moved onto and the pawn object
 	 * Each has a reference to the other, so they must both be updated
 	 * @param The position on the game board
 	 * @param The pawn object now occupying the field
 	 */
 	private void updateField(int pos, Pawn pawn){
 		if(pawn != null){
 			this.gameBoard[pos].setOccupant(pawn);
 			this.gameBoard[pos].setPawnOwner(pawn.getOwner());
 		}else{
 			this.gameBoard[pos].setOccupant(null);
 			this.gameBoard[pos].setPawnOwner(null);
 		}
 	}
 	
 	/**
 	 * Searches for a pawn at a given position on the board
 	 * @param The player object whose pawns to search (called with current player)
 	 * @param The position on the game board (can be -1 for home, 0-39 for board, 40-43 for goal
 	 * @return The pawn object at the provided position
 	 */
 	public Pawn getPawnAtPosition(Player player, int pos){
 		Pawn[] pawns = player.getPawns();
 		for(Pawn pawn: pawns){
 			if(pawn.getPosition() == pos){
 				return pawn;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Searches for a pawn at the given position on the game board
 	 * Pawn must be on the board and not in the goal or home
 	 * @param Integer position on the game board (must be between 0-39 otherwise returns null)
 	 * @return The pawn object at the given position
 	 */
 	public Pawn getPawnAtPosition(int pos){
 		if(pos>39 || pos<0){
 			return null;
 		}else{
 			return this.gameBoard[pos].getOccupant();
 		}
 	}
 	
 	/**
 	 * Finds the final position where a pawn would land based on the dieRoll
 	 * Takes into account moving out of home or into the goal
 	 * @param The pawn object which is to be moved
 	 * @param The current die roll
 	 * @return The integer position where the pawn will land
 	 */
 	public int getMoveDestination(Pawn pawn, int dieRoll){
 		Player owner = pawn.getOwner();
 		int startPos = pawn.getPosition();
 		int playerStartTile = owner.getStartPosition();
 		if(pawn.getPosition() == -1){
 			return owner.getStartPosition();
 		}
 		if (startPos > 39){
 			return startPos + dieRoll;
 		}
 		for(int i=1;i<=dieRoll;i++){
 			if((startPos + i)%40 == playerStartTile){
 				int remainingMoves = dieRoll - i;
 				return 40 + (remainingMoves);
 			}
 		}
 		return (startPos + dieRoll)%40;
 	}
 	
 	/**
 	 * Prints the pawns and their associated players on the game board
 	 */
 	public String toString(){
 		String s = "";
 		for(Field field: this.gameBoard){
 			if(field.getOccupant() != null){
 				s += field.getOccupant() + " : " + field.getOccupant().getPosition();
 			}
 		}
 		return s;
 	}
 }

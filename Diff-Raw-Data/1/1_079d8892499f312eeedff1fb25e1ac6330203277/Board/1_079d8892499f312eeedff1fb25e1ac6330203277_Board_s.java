 package model;
 
 import java.util.HashMap;
 import java.util.ArrayList;
 
 /**
  * Class to model the game board. <br>
  * Contains accessors for the players, pawns, dice rolls, etc.
  * @author Luuk
  *
  */
 public class Board {
 	private Player[] players;
 	private Field[] gameBoard;
 	private HashMap<Player, Field[]> playerEndMap;
 	
 	//Constructor
 	public Board(){
 		// Create board and player arrays
 		gameBoard = new Field[40];
 		players = new Player[4];
 		// Fake player generator for testing
 		/*for(int i=0; i<4; i++){
 			players[i] = new HumanPlayer(i+1);
 		}*/
 		players[0] = new HumanPlayer(1);
 		players[1] = new ComputerPlayer(2, new CaptureStrategy());
 		players[2] = new ComputerPlayer(3, new RandomStrategy());
 		players[3] = new ComputerPlayer(4, new MoveFirstStrategy());
 		
 		
 		for(int i=0;i<40;i++){
 			gameBoard[i] = new Field();
 		}
 		//add a fork for each player
 		for(Player player: players){
 			gameBoard[player.getStartPosition()] = new StartTile(player);
 		}
 		
 		// Map the players to their corresponding home and end fields
 		//playerHomeMap = new HashMap<Player, Field[]>();
 		playerEndMap = new HashMap<Player, Field[]>();
 		for (Player player: players){
 			playerEndMap.put(player, new Field[4]);
 			Field[] EndMap = playerEndMap.get(player);
 			for(int i=0;i<4;i++){
 				EndMap[i] = new Field();
 			}
 		}
 		
 	}
 	
 	//Constructor for given players
 //	public Board(Player[] newPlayers){
 //		gameBoard = new Field[40];
 //		for(int i=0;i<40;i++){
 //			gameBoard[i] = new Field();
 //		}
 //		this.players = newPlayers;
 //		for(Player player: players){
 //			gameBoard[player.getStartPosition()] = new StartTile(player);
 //		}
 //		playerEndMap = new HashMap<Player, Field[]>();
 //		for (Player player: players){
 //			playerEndMap.put(player, new Field[4]);
 //			Field[] EndMap = playerEndMap.get(player);
 //			for(int i=0;i<4;i++){
 //				EndMap[i] = new Field();
 //			}
 //		}
 //		
 //	}
 	
 	/**
 	 * Get a player object given their player number
 	 * @param the number of the player to return
 	 * @return the player object, assuming players are stored in sequential order
 	 */
 	public Player getPlayer(int playerNumber){
 		return this.players[playerNumber - 1];
 	}
 	
 	public Player[] getPlayers(){
 		return this.players;
 	}
 	
 	public void reset(){
 		for(Player player: this.players){
 			Pawn[] pawns = player.getPawns();
 			Field[] EndMap = this.playerEndMap.get(player);
 			for(Pawn pawn:pawns){
 				int pos = pawn.getPosition();
 				if(pos >= 40){
 					EndMap[pos-40].setOccupant(null);
 					EndMap[pos-40].setPawnOwner(null);
 				}else if(pos == -1){
 					continue;
 				}else{
 					updateField(pos,null);
 				}
 				pawn.setPosition(-1);
 			}
 		}
 	}
 	
 	/**
 	 * @return Returns an array containing all the Pawn objects on the game board
 	 * 
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
 	
 	/**
 	 * 
 	 * @param owner of the EndMap you want to check
 	 * @return Returns integer of closest pawn to the s
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
 	 * 
 	 * @param sends the pawn at the given position back to the owners home
 	 */
 	private Player sendPawnHome(int pos){
 		Pawn temp = gameBoard[pos].getOccupant();
 		temp.setPosition(-1);
 		temp.getOwner().incrementPawnsAtHome();
 		return temp.getOwner();
 	}
 	
 	/**
 	 * 
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
 					//pawn.setIsMoveable(false);
 					continue;
 				}
 			}
 			return MoveablePawns;
 		}
 		for(Pawn pawn: owner.getPawns()){
 			if(pawn.getPosition() == -1){
 				//pawn.setIsMoveable(false);
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
 						//pawn.setIsMoveable(false);
 						break;
 					}
 					//make sure the pawn doesnt pass another in the goal area
 					if(getClosestPawnInGoal(owner,pawn) > remainingMoves + 40){
 						MoveablePawns.add(pawn); //pawn.setIsMoveable(true);
 						break;
 					}else{
 						//pawn.setIsMoveable(false);
 						break;
 					}
 				}
 				if( i == currentRoll){
 					if(gameBoard[(currentpos + i) % 40].getPawnOwner() == pawn.getOwner()){
 						continue;
 						//pawn.setIsMoveable(false);
 					}else{
 						MoveablePawns.add(pawn); // pawn.setIsMoveable(true);
 					}
 				}
 			}
 			
 		}
 		return MoveablePawns;
 	}
 	
 	/**
 	 * Makes a move for a computer player based on its strategy
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
 		}else{
 			//move the pawn the given number of slots, all error handling is done by the getMovablePawns method
 			for(int i=1;i<=currentRoll;i++){
 				if(currentpos >= 40){
 					Field[] EndMap = this.playerEndMap.get(pawn.getOwner());
 					EndMap[currentpos - 40].setOccupant(null);
 					EndMap[currentpos - 40].setPawnOwner(null);
 					EndMap[currentpos + currentRoll - 40].setOccupant(pawn);
 					EndMap[currentpos + currentRoll - 40].setPawnOwner(pawn.getOwner());
 					break;
 					
 					
 				}
 				if(gameBoard[(currentpos + i) % 40].getForkOwner() == pawn.getOwner()){
 					updateField(currentpos,null);
 					Field[] EndMap = playerEndMap.get(pawn.getOwner());
 					EndMap[currentRoll-i].setOccupant(pawn);
 					EndMap[currentRoll-i].setPawnOwner(pawn.getOwner());
 					
 					pawn.setPosition(40 + (currentRoll-i));
 					return move;
 				}
 				if(i == currentRoll){
 					if(gameBoard[(currentpos + i) % 40].getOccupant() != null){
 						move.collision = sendPawnHome((currentpos + i) % 40);
 					}
 					updateField(currentpos, null);
 					updateField((currentpos + i) % 40, pawn);
 					pawn.setPosition((currentpos + i) % 40);
 				}
 			}
 		}
 		return move;
 			
 	}
 	
 	/**
 	 * 
 	 * @param player to be checked for winning condition
 	 * @return Returns whether or not the player has won the game
 	 */
 	public Boolean HasWon(Player player){
 		for(Field field:playerEndMap.get(player)){
 			if(field.getOccupant() == null){
 				return false;
 			}
 		}
 		return true;
 	}
 	
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
 	 * 
 	 * @param player to set all their pawn to non movable at the end of a round
 	 */
 	public Field[] getBoard() {
 		return this.gameBoard;
 	}
 	 
 	public Pawn getPawnAtPosition(Player player, int pos){
 		Pawn[] pawns = player.getPawns();
 		for(Pawn pawn: pawns){
 			if(pawn.getPosition() == pos){
 				return pawn;
 			}
 		}
 		return null;
 	}
 	
 	
 	public Pawn getPawnAtPosition(int pos){
 		if(pos>39){
 			return null;
 		}else{
 			return this.gameBoard[pos].getOccupant();
 		}
 	}
 	
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

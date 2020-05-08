 	/* GameBoard.java
 	 * This class will handle all logic involving the actions, and movements involved with the 
 	 * game Sorry! as well as storing all other associated objects and various statistics about 
 	 * the game thus far.  This class includes a representation of the track, deck, pawns, and 
 	 * records information about the game to be logged as statistics.
 	 */
 
 import java.util.Date; //for gameplay length
 
 public class SRGameBoard {
 	
 	//constants
 	public static int trackLength = 60;
 	public static int[] safetyZoneIndex = {60,66};
 	public static int[] safetyZoneEntrance = {2, 33};
 	public static int[] startIndex = {4,37};
 	
 	
 	//gameplay	
 	public SRSquare[] track = new SRSquare[72];	//squares that make up the regular track, safety zones, and home squares
 	public SRSquare[] startSquares  = new SRSquare[2];	//indexes into the track representing where players may move their pawns into play
 	public SRDeck[] deck;	//Deck object used in this game
 	public SRPawn[] pawns  = new SRPawn[8];	//8 pawns used in the game
 	
 	
 	//statistics
 	public Date startTime;
 	public int elapsedTime; //elapsed time in seconds
 	public int numTurns; 	//turns taken
 	public int numBumps; 	//times player bumped another player
 	public int numStories;	//successful uses of Sorry! cards
 	public String cpuStyle;	//either nice or mean, how the computer was set to play
 	public int playerPawnsHome;	//number of pawns player got home
 	public int playerDistanceTraveled; //total number of squares traveled by the human player
 	public int cpuDistanceTraveled;	//"" "" "" "" computer
 	
 
 	public SRGameBoard(){
 		this.cpuStyle = "easy";
 		this.startTime = new Date();
 		//etc.
 		
 		System.out.println("GameBoard initialized.");//for testing
 	}
 	
 	//card methods:
 	public SRCard drawCard(){
		return new SRCard();
 	}
 	
 	//movement methods:
 	public void findMoves(SRPawn pawn, SRCard card){
 	}
 
 	public void movePawn(SRPawn pawn, int location){
 	}
 	
 	public void bumpPawn(SRPawn pawn){
 	}
 	
 	public void startPawn(SRPawn pawn){
 	}
 	
 	public boolean hasWon(int player){
 		return false;
 	}
 	
 	private void endGame(){
 		Date endTime = new Date();
 		//something like:
 		//elapsedTime = endTime - this.startTime
 	}
 	
 	
 	public static void main(String[] args){
 		SRGameBoard gb = new SRGameBoard();
 	}
 }

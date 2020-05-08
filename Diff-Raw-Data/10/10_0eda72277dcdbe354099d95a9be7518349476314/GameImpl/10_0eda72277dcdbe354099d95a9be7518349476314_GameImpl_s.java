 package quoridor;
 
 import java.util.LinkedList;
 import java.util.List;
 
 public class GameImpl implements Game {
 
 	public static final short TOKEN_VOID = -1;
 	public static final short TOKEN_QUIT = 0;
 	public static final short TOKEN_MOVE = 1;
 	public static final short TOKEN_LOAD = 2;
 	public static final short TOKEN_SAVE = 3;
 	
 	Pair <Player> players;
 	List<GenericMove> moves;
 	
 	public GameImpl(Pair <Player> players) {
 		this.players = players;
 		moves = new LinkedList<GenericMove>();
 		
 	}
 
 	@Override
 	public void play() {
 		
 		Player current = players._1();
 		Pair <Pawn> pawns = new PairImpl<Pawn>(new PawnImpl(
 				new SquareImpl(4, 8), players._1()),
 				new PawnImpl(new SquareImpl(4, 0), players._2()));
 		
 		Board gameBoard = new BoardImpl(pawns);
 		
 		MoveParser parser = new MoveParserImpl();
 		
 		GenericMove nextMove;
 		
<<<<<<< HEAD
 		
 		
 		while (!isOver()) {
 			System.out.println(gameBoard.toString());
			if(current == null) {System.out.println("aohushoutnhdoae");}
=======
		while (!isOver()) {
			System.out.println(gameBoard.toString());
>>>>>>> d2bc5b4b494ea561490343ad25c3a61301bb5a0c
 			if (current.equals(players._1())) {
 				System.out.println("Enter move " +current.getName()+ " (X): ");
 			} else {
 				System.out.println("Eneter move "+current.getName()+ " (O): ");
 			}
 			
 			nextMove = parser.scanMove(current, gameBoard);
 			
 			if (nextMove == null) {
 				System.out.println("bad input");
 			} else {
 			
 				moves.add(nextMove);
 				 
 				if (nextMove.isValid()) {
 					 nextMove.makeMove();
 					 current = current.getOpponent();
 				 } else {
 					 System.out.println("Invalid Move");
 				 }
 			}
 		}
 	}
 	
 	public boolean isOver() {
 		return players._1().hasWon() || players._2().hasWon();
 	}
 	
 	/** Builds and returns a string of the players names
 	 * and the current moves that have happened up to this game
 	 * @return a string of player names and moves so far
 	 */
 	
 	public String formatFile() {
 		String retVal = "";
 		retVal+=players._1().getName();
 		retVal+=players._2().getName();
 		while(!moves.isEmpty()) {
 			retVal+= moves.get(0);
 			retVal+= " ";
 		}
 		return retVal;
 	}
 	
 }

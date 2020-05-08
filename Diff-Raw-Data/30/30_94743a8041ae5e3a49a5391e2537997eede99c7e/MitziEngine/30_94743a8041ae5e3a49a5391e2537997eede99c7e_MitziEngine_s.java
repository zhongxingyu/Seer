 package mitzi;
 
import java.util.Set;

 public class MitziEngine {
 
 	public static void main(String[] args) {
 
 		System.out.println("id name Mitzi 0.1");
 		System.out.println("id author Christoph Hofer, Stefan Lew");
 		System.out.println("uciok");
		
		NaiveBoard board = new NaiveBoard();
		board.setToInitial();
		
		board.setToFEN("8/8/p7/P5p1/P5Pp/4k2P/6K1/8 w - - 46 153");
		
		Set<IMove>	moves = board.getPossibleMoves();
		//System.out.println(moves.iterator().next());
		
		for(IMove m : moves)
			System.out.println(m.toString());
		
		
		
	

		
		
 		System.exit(0);
 
		
 	}
 
 }

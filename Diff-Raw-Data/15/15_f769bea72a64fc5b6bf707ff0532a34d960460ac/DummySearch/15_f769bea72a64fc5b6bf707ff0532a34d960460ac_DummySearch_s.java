 package snozama.amazons.mechanics.algo;
 
 import snozama.amazons.mechanics.Board;
 import snozama.amazons.mechanics.MoveManager;
 
 /**
  * LOL Will.
  * 
  * @author Graeme Douglas
  *
  */
 public class DummySearch {
 	
 	long endTime;
 	
 	public DummySearch(long end)
 	{
 		endTime = end;
 	}
 	
 	public int chooseMove(Board board, int colour, int turn)
 	{
 		if (board.isTerminal())
 		{
 			return -1;
 		}
 		// Choose your algorithm here.
 		
		return fastRandomMove(board, colour);
 		
		//return timedRandomMove();
 	}
 	
 	private int fastRandomMove(Board board, int colour)
 	{
 		MoveManager successors = board.getSuccessors(colour);
 		
 		successors.shuffle();
 		
 		return successors.getMove(0);
 	}
 	
 	private int timedRandomMove(Board board, int colour)
 	{
 		// Look like we are making good decisions
 		while (System.currentTimeMillis() < endTime){}
 		
 		return fastRandomMove(board, colour);
 	}
 
 }

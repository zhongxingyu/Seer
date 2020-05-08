 package game;
 
 public class ReversiGame extends Game {
 
 	public ReversiGame(int numberOfPlayers, int rows, int columns) {
 		super(numberOfPlayers, rows, columns);
 	}
 
 	@Override
 	public boolean isMoveValid(int r, int c) {
 		boolean okN, okNW, okW, okSW, okS, okSE, okE, okNE;
 		int i;
 		//What about empty spaces?
 		okN = r>0 && players[currentPlayerId].getColour()!= board.getPiece(r-1, c).getColour();
 		for(i=r-1;okN && i>=0; i--)
 			if(board.isEmpty(i, c)) //No, it has to be an empty space
 				return true;
 		
 		okNW = r>0 && c>0 && players[currentPlayerId].getColour()!= board.getPiece(r-1, c-1).getColour();
 		for(i=1;okNW && i<=Math.min(r,c); i++)
 			if(board.isEmpty(r-i, c-i))
 				return true;
 		
 		okW = c>0 && players[currentPlayerId].getColour()!= board.getPiece(r, c-1).getColour();
 		return false;
 	}
 
 	@Override
 	public void play() {
 
 	}
 
 	@Override
 	public void move(int r, int c)
 	{
 		
 		
 	}
 
 }

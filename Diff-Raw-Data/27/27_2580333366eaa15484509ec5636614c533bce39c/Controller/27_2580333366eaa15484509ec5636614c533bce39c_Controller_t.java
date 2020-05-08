 package othello.controller;
 
 import othello.model.Board;
 import othello.model.Listener;
 
 // interface for the controller in the Model-View-Controller for Reversi
 public abstract class Controller
 {
 	protected Board b; // state of the game (model for this controller)
 	protected Listener l; // listener for the game
 	protected boolean active; // is the game still active
 	
	public abstract void move(int x, int y);	// attempt to place a piece at coordinate x,y
	public abstract void newGame();					// start a new game
	public abstract Board getBoard();
	
 	// update the display with the current board / player move
   public void update()
   {
     l.setBoard(b);
     l.setMessage(" ");
     l.setTurn(sideToString(b) + " to move");
     l.setScore(b.getTotal(true) + " - " + b.getTotal(false));
     l.repaint();
   }
 	
 	// display the winner and disable input
 	protected void gameOver()
 	{
 		update();
 		l.setMessage("Game over");
 		l.setScore(scoreToString(b));
 		l.setTurn(winnerToString(b));
 		active = false;
 	}
 	
 	protected static String sideToString(Board b)
 	{
 		if(b.getActive() == Board.BLACK) return "Black";
 		else return "White";
 	}
 	
 	protected static String scoreToString(Board b)
 	{
 		int a = b.getTotal(true), o = b.getTotal(false);
 		return "" + (int)Math.max((double)a, (double)o) + " - " + (int)Math.min((double)a, (double)o);
 	}
 	
 	protected static String winnerToString(Board b)
 	{
 		int winner = b.getWinning();
 		if(winner == Board.BLACK) return "Black wins";
 		else if(winner == Board.WHITE) return "White wins";
 		else return "Black and White tie";
 	}
 	
 }

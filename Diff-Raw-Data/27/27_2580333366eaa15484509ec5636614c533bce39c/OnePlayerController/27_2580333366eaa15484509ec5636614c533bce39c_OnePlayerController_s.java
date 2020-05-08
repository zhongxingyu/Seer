 package othello.controller;
import othello.ai.AIThread;
import othello.ai.GreedyAI;
import othello.ai.GreedyHeuristicAI;
import othello.ai.ReversiAI;
 import othello.model.Board;
 import othello.model.Listener;
 import othello.view.BoardGUI;
 import othello.view.ReversiGUI;
 
 // a controller, where one player (BLACK) is a human, and the other (WHITE) is an AI
 
 public class OnePlayerController extends Controller
 {
 	ReversiAI r;            // AI to control the non-human player
 	
 	AIThread aiThread;
 
 	public static void main(String args[])
 	{
 		ReversiGUI gui = new ReversiGUI();
 		OnePlayerController c = new OnePlayerController(gui);
 		gui.setController(c);
 		
		
 		c.update();
 	}
 
 	public OnePlayerController(Listener l)
 	{
 		this.l = l;
 		newGame();
 		
		r = new GreedyAI();
		r.setSize(b.getSize());
		
 		aiThread = new AIThread(r, this, Board.WHITE);
 		aiThread.start();
		
 	}
 
 	public void update()
 	{
 		super.update();
 		
 		if(aiThread != null) {
 			synchronized (AIThread.syncObject) {
 				AIThread.syncObject.notifyAll();
 			}
 		}
 	}
 
 	public void move(int x, int y)
 	{
 		if(!active) return;
 
 		if(!b.move(x, y)) /* invalid move */
 		{
 			l.setMessage("Not a legal move");
 			return;
 		}
 		b.turn(); // Next players turn
 
 		// if we get here,  AI can't move
 		if(b.canMove())
 		{
 			update();
 			playerLog("Next player's turn.");
 			return;
 		} else {
 			playerLog("Can't move so yielding.");
 			b.turn();
 			if(b.canMove()) {
 				update();
 				return;
 			}
 		}
 
 		System.out.println("No more possible moves so ending.");
 		/* no more moves possible, game over */
 		update();
 		gameOver();
 		return;
 	}
 	
 	public Board getBoard() {
 		return b;
 	}
 
 	public void newGame()
 	{
 		active = true;
 		b = new Board(BoardGUI.ROWS);
 		r = new GreedyHeuristicAI();
 		r.setSize(b.getSize());
 		update();
 		l.setMessage("New game");
 	}
 	
 	public void playerLog(String msg) {
 		System.out.println(b.getActiveName() + "-" + msg);
 	}
 }

 package othello.controller;
 import othello.ai.*;
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
 		ReversiGUI gui = new ReversiGUI(true);
 		OnePlayerController c = new OnePlayerController(gui);
 		gui.setController(c);
 		
 		c.update();
 	}
 
 	public OnePlayerController(Listener l)
 	{
 		this.l = l;
 		newGame();
 		
 		aiThread = new AIThread(r, this, Board.WHITE);
 		aiThread.start();
 	}
 	
 	public Board getBoard() { return b; }
 
 	public void newGame()
 	{
 		active = true;
 		b = new Board(BoardGUI.ROWS);
		r = new PluggableHeuristicAI();
 		r.setSize(b.getSize());
 		update();
 		l.setMessage("New game");
 	}
 
 	// update 
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
 
 		if(!b.move(x, y)) // invalid move by human
 		{
 			l.setMessage("Not a legal move");
 			return;
 		}
 		b.turn(); // AI's turn
 
 		if(b.canMove()) // AI can move
 		{
 			update();
 			playerLog("Next player's turn.");
 			return;
 		}
 		
 		// AI can't move, so human's turn
 		playerLog("Can't move so yielding.");
 		b.turn();
 		
 		if(b.canMove()) // human can move
 		{
 			update();
 			return;
 		}
 
 		// no more moves possible, game over
 		System.out.println("No more possible moves so ending.");
 		update();
 		gameOver();
 		return;
 	}
 	
 	public void playerLog(String msg) {
 		System.out.println(b.getActiveName() + "-" + msg);
 	}
 }

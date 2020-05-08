 
 public class GameLoop implements Runnable{
 
 	private GameBoard board;
 	private boolean running;
 	Thread thread;
 
 	public GameLoop (GameBoard board){
 		this.board = board;
 		thread = new Thread(this);
 		thread.start();
 	}
 
 	public void stop(){
 		this.running = false;
 	}
 
 	@Override
 	public void run() {
 		this.running = true;
 		while(running){
 			board.doTurn();
 			try{
 				Thread.sleep(200);
 			}catch (InterruptedException ex){
 			}
 		}
 	}
 
 }

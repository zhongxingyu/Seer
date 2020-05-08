 
 public class GameLoop implements Runnable{
 
 	private GameBoard board;
 	private boolean running;
 
 	public GameLoop (GameBoard board){
 		this.board = board;
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

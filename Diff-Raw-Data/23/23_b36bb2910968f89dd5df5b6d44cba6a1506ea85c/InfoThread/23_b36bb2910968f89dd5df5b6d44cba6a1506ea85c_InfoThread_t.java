 package backgammon.view.helpers;
 
 public class InfoThread implements Runnable{
 
 	private ImageBoard board;
 
 	public InfoThread(ImageBoard board) {
 		this.board = board;
 	}
 
 	@Override
 	public void run() {
 		//sleep 2.5 secs and erase InfoMessage
 		try {
 			Thread.sleep(2500);
 		} catch (InterruptedException e) {
 			//do nothing
 		}
 		
		
		if(Thread.interrupted())
			return;
		
 		//delete Message
 		this.board.clearInfo();
 		
 		//Neu zeichnen
 		this.board.repaint();
 		
 	}
 
 }

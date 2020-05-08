 public class Paddle implements Runnable {
 
 	int x;
 	int y;
 	int length;
 	int player;
 	int thick;
 	final static int DEFAULT_LENGTH = 100;
	final static int DEFAULT_THICK = 5;
 	Game gui;
 
 	public Paddle(Game game, int player) {
 		gui = game;
 		this.player = player;
 		this.setDefaultLength();
 		this.setDefaultThick();
 		if (player == 1)
 			x = 50;
 		else if (player == 2)
 			x = gui.getGUIWidth() - 50;
 	}
 
 	@Override
 	public void run() {
 		// TODO Paddle Thread
 		//shadow mode
 		Coordinate p;
 			p = gui.getPlayerCoordinate(player);
 		while (true) {
 			synchronized (p) {
 				try {
 					p.wait();
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			setY(gui.getPlayerCoordinate(player).getY());
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	public synchronized int getX() {
 		return x;
 	}
 
 	public synchronized int getY() {
 		return y;
 	}
 
 	public synchronized void setY(int y) {
 		this.y = y;
 	}
 
 	public synchronized int getLength() {
 		return length;
 	}
 
 	public synchronized void setLength(int length) {
 		this.length = length;
 	}
 
 	public synchronized void setDefaultLength() {
 		this.length = DEFAULT_LENGTH;
 	}
 	
 	public synchronized void setDefaultThick() {
 		this.thick = DEFAULT_THICK;
 	}
 	
 	public synchronized void setThick(int thick) {
 		this.thick = thick;
 	}
 
 	public synchronized int getThick() {
 		return thick;
 	}
 	
 	public synchronized boolean isRangeY(double y) {
 		if( y > this.y - this.length/2 && y < this.y + this.length/2)
 			return true;
 		return false;
 	}
 }

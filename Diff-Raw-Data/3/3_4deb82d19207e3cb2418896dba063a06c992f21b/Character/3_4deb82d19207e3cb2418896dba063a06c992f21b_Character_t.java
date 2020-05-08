 public abstract class Character implements Runnable {
 	private int time;
 	private boolean running;
 	private Game game;
 	
 	/**
 	 *(precondition) game must exist, time must be positive and bigger than 0
 	 *(postcondition) creates new character at a start position in a game, ready to be run
 	 */
 	public Character(int time, Game game) {
 		if (time <= 0) {
 			time = 20;
 		} else {
 			this.time = time;
 		}
 		this.game = game;
 		running = true;
 	}
 	
 	/**
 	 * (precondition) character must be on valid field in running game, if thats not the case, it won't do anything
 	 */
 	@Override
 	public void run() {
 		try {
 			while (this.running && game.getState() == Game.State.RUNNING) {
 				this.move();
 				try {
 					Thread.sleep(this.time);
 				} catch (InterruptedException ie) {
					// This happens, just proceed
 				}
 			}
 		} catch (Exception e) { 
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * (postcondition) This Object will be set to an invalid state and will terminate the run method 
 	 * at one point in the future or not start it at all
 	 */
 	protected void stopThread() {
 		running = false;
 	}
 
 	/**
 	 *(postcondition) character moved a square, ended the game, or died
 	 */
 	protected abstract void move();
 }

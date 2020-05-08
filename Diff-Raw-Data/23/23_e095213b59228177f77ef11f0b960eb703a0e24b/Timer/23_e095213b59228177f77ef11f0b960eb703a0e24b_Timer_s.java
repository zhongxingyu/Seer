 package hu.miracle.workers;
 
 public class Timer extends Thread {
 
 	// Members
 	private int interval;
 	private Game game;
 	private boolean suspended;
 
 	public Timer(Game game) {
 		this.game = game;
 		this.interval = 1000;
		this.suspended = false;
 	}
 
 	public Timer(Game game, int interval) {
 		this.game = game;
 		this.interval = interval;
		this.suspended = false;
 	}
 
 	@Override
 	public void run() {
 		try {
 
 			while (true) {
				sleep(interval);
				tick();
 				synchronized (this) {
 					while (suspended) {
 						wait();
 					}
 				}
 			}
 
 		} catch (InterruptedException e) {
 			System.out.println("Timer Interrupted");
 		}
 	}
 
 	// Protected methods
 	// Nem biztos hogy protected lesz, implementaciotol fugg
 	protected void tick() {
 		System.out.println(getClass().getCanonicalName() + ".tick()");
 		game.getScene().delegateTick();
 	}
 
 	// Public interface
 	public int getInterval() {
 		System.out.println(getClass().getCanonicalName() + ".getInterval()");
 		return interval;
 	}
 
 	synchronized public void startTimer() {
 		System.out.println(getClass().getCanonicalName() + ".startTimer()");
 		suspended = false;
 		notify();
 	}
 
 	synchronized public void stopTimer() {
 		System.out.println(getClass().getCanonicalName() + ".stopTimer()");
 		suspended = true;
 	}
 
 	public Game getGame() {
 		System.out.println(getClass().getCanonicalName() + ".getGame()");
 		return game;
 	}
 
 	public void setGame(Game game) {
 		System.out.println(getClass().getCanonicalName() + ".setGame()");
 		this.game = game;
 	}
 
 	public void setInterval(int interval) {
 		System.out.println(getClass().getCanonicalName() + ".setInterval()");
 		this.interval = interval;
 	}
 }

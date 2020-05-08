 package obstacles;
 
 public abstract class Obstacle {
 	
 	/**
 	 * 
 	 */
	public abstract void update();
 	/**
 	 * 
 	 */
 	public abstract  void draw();
 	/**
 	 * 
 	 */
 	public abstract boolean haveCollided();
 	
 	public int timer;
 	public int origTimer;
 
 }

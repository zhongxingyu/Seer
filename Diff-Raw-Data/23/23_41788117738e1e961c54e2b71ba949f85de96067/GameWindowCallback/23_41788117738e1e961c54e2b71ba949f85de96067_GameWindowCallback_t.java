 package ca.couchware.wezzle2d;
 
 /**
  * An interface describing any class that wishes to be notified as the game
  * window renders.
  * 
  * @author Kevin Glass
  */
 public interface GameWindowCallback
 {
 	/**
 	 * Notification that game should initialise any resources it needs to use.
 	 * This includes loading sprites.
 	 */
 	public void initialize();
 
 	/**
 	 * Notification that the display is being rendered. The implementor should
 	 * render the scene and update any game logic
 	 */
 	public boolean frameRendering();
 
 	/**
 	 * Notification that game window has been closed.
 	 */
 	public void windowClosed();
     
     /**
      * Notification that the game window has been deactivated.
      */
     public void windowDeactivated();
     
     /**
      * Notification that the game window has been reactivated.
      */
    public void windowActivated();
 }

 package ca.couchware.wezzle2d;
 
 import ca.couchware.wezzle2d.java2d.Java2DText;
 
 /**
  * The TimeManager is a class that manages the time for the Timer.
  * The class works as follows: An internal count is held starting at 0.
  * An offset is passed in every update with an increment. When the internal count
  * goes above 1000ms (or 1 second) the timer is decremented.
  * 
  * @author Kevin Grad
  *
  */
 public class TimeManager 
 {
 
 	/** The current time on the timer. */
 	private int currentTime;
 	
 	/** The initial time for this timer. */
 	private int initialTime;
 	
 	/** Holds the internal time for comparison to time increments. */
 	private long internalTime;
     
     /** is the game paused? */
     private boolean paused;
 	
 	
 	
 	/**
 	 * The overloaded constructor created a timer manager with a passed in 
 	 * initial time.
 	 * 
 	 * @param initialTime The initial time on the timer.
 	 */
 	public TimeManager( int initialTime )
 	{
 		assert(initialTime > 0);
 		
 		this.currentTime = initialTime;
 		this.initialTime = initialTime;
 		this.internalTime = 0;
         this.paused = false;
 	}
 
 	/**
 	 * The Default constructor constructs a timer manager with an 
 	 * initial time of 20 seconds.
 	 */
 	public TimeManager()
 	{
 		// Call the overloaded constructor.
 		this(20);
 	}
 
 	/**
 	 * A method to set the time on the timer.
 	 * 
 	 * @param time The new time.
 	 */
 	public void setTime(int time)
 	{
 		assert(time > 0);
 		
 		this.currentTime = time;
 	}
 
 	/**
 	 * Get the timer time.
 	 * @return The time.
 	 */
 	public int getTime()
 	{
 		return this.currentTime;
 	}
 	
 	/**
 	 * Reset the internal second count. I.e. the part that is modified by 
      * delta.
 	 */
 	public void resetInternalTimer()
 	{
 		this.internalTime = 0;
 	}
 
 	/**
 	 * Reset the timer.
 	 */
 	public void resetTimer()
 	{
 		this.currentTime = this.initialTime;
         this.resetInternalTimer();
 	}
     
     /**
      * Pause the timer.
      */
     public void pause()
     {
         this.paused = true;
     }
 	
     /**
      * Unpause the timer.
      */
     public void unPause()
     {
         this.paused = false;
     }
     
     /**
      * Check if the timer is paused.
      * @return Whether or not the timer is paused.
      */
     public boolean isPaused()
     {
         return this.paused;
     }
     
 	/**
 	 * Get the initial time.
 	 * @return The initial time.
 	 */
 	public int getInitialTime()
 	{
 		return this.initialTime;
 	}
 
 	/**
 	 * Set the initial time.
 	 * @param time The new time.
 	 */
 	public void setInitialTime(int time)
 	{
 		this.initialTime = time;
 	}
 	
 	/**
 	 * A method to increment the internal time. If a second has passed
 	 * the internal time goes to 0 and the current time is decremented.
 	 * 
 	 * @param offset The elapsed time.
 	 */
 	public void incrementInternalTime(long offset, final Game game)
 	{
         // If the timer is paused, don't do anything.
         if(this.paused)
             return;
         
 		assert(offset >= 0);
 		
 		this.internalTime += offset;
 		
 		// Check to see if it has been a second.
 		if(this.internalTime >= 1000)
 		{
 			// A second has elapsed, decrement the timer, reset the internal timer
 			// and check if < 0.
 			this.currentTime--;
 			this.internalTime = 0;
             
			if(this.currentTime < 0)
 			{
                 // Commit the piece.
                 game.pieceMan.initiateCommit(game);
 			}	
 		}
 		else
 		{
 			// Purposefully left blank. In this scenario nothing should be done.
 		}
 	}
 	
 	
 }

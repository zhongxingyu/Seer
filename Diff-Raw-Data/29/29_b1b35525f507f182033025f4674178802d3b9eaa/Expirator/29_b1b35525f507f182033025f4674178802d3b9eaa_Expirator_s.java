 package crescendo.base;
 
 /**
  * Runnable thread for expiring notes on.
  * This class is used to populate a thread pool object, it starts up and
  * waits around to be told to expire a note. Once it gets a note it
  * waits for that to either expire or get resolved, then waits again.
  * @author nickgartmann
  */
 public class Expirator implements Runnable{
 	
 	private final int TIMEOUT;
 	private int currentTimeout; //Needed for expiring correctly after a pause
 	private boolean doContinue;
 	private boolean isBusy;
 	private boolean isPaused;
 	private long initialMilliseconds; //Timestamp of when we started expiring the note
 	private NoteEvent currentEvent;
 	private boolean isFlagged;
 	private SongValidator validator;
 	
 	
 	/**
 	 * Constructor
 	 * sets up the initial values
 	 * @param timeout amount of time in milliseconds to wait before throwing a NoteTimeoutException
 	 */
 	public Expirator(SongValidator validator, int timeout)
 	{
 		if(timeout<1) {
 			throw new IllegalArgumentException("Timeout must be at least one");
 		}
 		TIMEOUT = timeout;
 		currentTimeout = timeout;
 		doContinue = true;
 		isBusy = false;
 		isFlagged=false;
 		initialMilliseconds = 0;
 		this.validator = validator;
 	}
 	
 	/**
 	 * Sets the note to expire
 	 * @param noteEvent event which contains the note to expire
 	 * @throws ExpiratorBusyException when expirator is already expiring a note 
 	 * 
 	 * @author forana
 	 */
 	public void expireNote(NoteEvent noteEvent) throws ExpiratorBusyException {
 		if(noteEvent==null) {
 			throw new NullPointerException("NoteEvent cannot be null");
 		}
 		if(isBusy){
 			throw new ExpiratorBusyException();
 		}
 		currentEvent = noteEvent;
 		initialMilliseconds = System.currentTimeMillis();
 		isBusy = true;
 		isFlagged=false;
 	}
 	
 	/**
 	 * Notifies the expirator that it's note has been played by the user
 	 * and should no longer timeout.
 	 */
 	public void resolveNote() {
 		// We can just set isBusy to false because then
 		// the expirator thinks it already expired the note 
 		isBusy = false;
 		isFlagged=false;
 	}
 	
 	/**
 	 * Gets the event for the note currently being expired
 	 * @return currently expiring note event
 	 */
 	public NoteEvent getNoteEvent()
 	{
 		return currentEvent;
 	}
 	
 	/**
 	 * Checks if the expirator is currently expiring a note
 	 * @return flag for if the expirator is busy
 	 */
 	public boolean isBusy()
 	{
 		return isBusy;
 	}
 	
 	/**
 	 * Checks if the expirator has been flagged.
 	 * @return true if it has, false otherwise.
 	 */
 	public boolean isFlagged()
 	{
 		return this.isFlagged;
 	}
 	
 	/**
 	 * Flags this expirator, denoting that an event has been processed but it should
 	 * still keep going.
 	 */
 	public void flag()
 	{
 		this.isFlagged=true;
 	}
 	
 	/**
 	 * Suspends the expiration of the current note
 	 */
 	public void pause()
 	{
 		isPaused = true;
 		long timeExpired = System.currentTimeMillis() - initialMilliseconds;
 		if(timeExpired < currentTimeout)
 		{
 			currentTimeout -= timeExpired;
 		}
 		else
 		{
 			//TODO investigate this
 		}
 	}
 	
 	/**
 	 * Resumes the expiration of the current note
 	 */
 	public void resume()
 	{
 		initialMilliseconds = System.currentTimeMillis();
 		isPaused = false;
 	}
 
 	/**
	 * shut down this expirator (terminate)
 	 */
 	public void stop()
 	{
 		doContinue=false;
 		isBusy=false;
 	}
 	
 	/**
 	 * Begin main loop for expiring notes and waiting for notes
 	 */
 	@Override
 	public void run() {
 		while(doContinue) {
 			if(isBusy()) {
 				if(!isPaused && (System.currentTimeMillis() - initialMilliseconds > currentTimeout)) {
 					expire();
 				}
 				else {
 					try {
 						Thread.sleep(1); //This is so we don't eat up the processor
 					} catch (InterruptedException e) {
 					}
 				}
 			}
 			else {
 				try {
 					Thread.sleep(1);
 				} catch (InterruptedException e) {
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Resets state and throws NoteExpiredException
 	 * @throws NoteExpiredException
 	 */
 	private void expire() {
 		isBusy = false;
 		currentTimeout = TIMEOUT;
 		validator.noteExpired(currentEvent);
 	}
 	
 	
 	/**
 	 * Exception to be used when the expirator is busy.
 	 * This exception is thrown when expireNote is invoked when
 	 * the expirator is already in the process of expiring a note.
 	 * @author nickgartmann
 	 *
 	 */
 	public class ExpiratorBusyException extends Exception {
 		private static final long serialVersionUID = 1L;
 	}
 }

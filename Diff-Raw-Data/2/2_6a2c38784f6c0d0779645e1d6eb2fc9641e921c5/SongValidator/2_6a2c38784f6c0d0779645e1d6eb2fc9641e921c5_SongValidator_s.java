 package crescendo.base;
 
 import crescendo.base.EventDispatcher.MidiEvent;
 import crescendo.base.EventDispatcher.MidiEventListener;
 import crescendo.base.EventDispatcher.ActionType;
 import crescendo.base.song.SongModel;
 import crescendo.base.song.Track;
 import java.util.List;
 import java.util.LinkedList;
 
 /**
  * Delegates NoteEvents from the SongPlayer to Expirators so that they can either time out or be
  * matched to user input, and pumps the resulting events out to subscribers.
  * 
  * @author forana
  */
 public class SongValidator implements NoteEventListener,FlowController,MidiEventListener
 {
 	/** The maximum number of note events that can be simultaneously expired. */
 	private static final int POOL_SIZE = 20; // theoretically more than 10 notes should never happen
 	
 	/** The track that the user is playing. */
 	private Track activeTrack;
 	
 	/** The pool that contains all of the active expirators. */
 	private ThreadPool pool;
 	
 	/** The amount of time to allow for a timeout. */
 	private int timeout;
 	
 	/** All subscribed ProcessedNoteEventListeners. */
 	private List<ProcessedNoteEventListener> processedListeners;
 	
 	/** The heuristics that will judge notes correct or incorrect. */
 	private HeuristicsModel heuristics;
 	
 	/** This constructor is kludge for ThreadPoolTest until we figure out something better. */
 	public SongValidator()
 	{
 	}
 	
 	/**
 	 * Creates a new SongValidator that listens for notes in a specified track.
 	 * 
 	 * @param activeTrack The track the user is playing.
 	 * @param timeout The amount of time to allow to pass before a note is considered missed.
 	 */
 	public SongValidator(SongModel model,Track activeTrack,HeuristicsModel heuristics)
 	{
 		this.activeTrack=activeTrack;
 		this.timeout=(int)(heuristics.getTimingInterval()*model.getBPM());
 		this.pool=new ThreadPool(this,POOL_SIZE,this.timeout);
 		this.processedListeners=new LinkedList<ProcessedNoteEventListener>();
 	}
 	
 	/**
 	 * Receives a NoteEvent and delegates it to a free expirator for expiring/storage. If no expirator is free,
 	 * this method will block until one frees up.
 	 * 
 	 * @param e The NoteEvent to delegate.
 	 */
 	public void handleNoteEvent(NoteEvent e)
 	{
		if (e.getNote().getTrack()!=this.activeTrack)
 		{
 			boolean tryAgain=true;
 			while (tryAgain)
 			{
 				Expirator free=this.pool.getAvailableExpirator();
 				if (free==null) // if we couldn't get one, well, crap, we should figure out what to do
 				{
 					tryAgain=true;
 				}
 				else
 				{
 					try
 					{
 						free.expireNote(e);
 						tryAgain=false;
 					}
 					catch (Expirator.ExpiratorBusyException ex)
 					{
 						tryAgain=true;
 					}
 				}
 				if (tryAgain)
 				{
 					try
 					{
 						Thread.sleep(10);
 					}
 					catch (InterruptedException ex)
 					{
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Pause all currently-expiring notes.
 	 */
 	public void pause()
 	{
 		this.pool.pause();
 	}
 	
 	/**
 	 * Resume all currently-paused notes.
 	 */
 	public void resume()
 	{
 		this.pool.resume();
 	}
 	
 	/**
 	 * Shuts down the held ThreadPool objects, destroying all allocated threads. This object can no longer be used
 	 * after this method is called.
 	 */
 	public void songEnd()
 	{
 		this.pool.shutdown();
 	}
 	
 	/**
 	 * Stops all expiring notes.
 	 */
 	public void stop()
 	{
 		this.pool.stop();
 	}
 	
 	/**
 	 * Suspends the expiraton of all notes.
 	 */
 	public void suspend()
 	{
 		this.pool.pause();
 	}
 	
 	/**
 	 * Subscribe a ProcessedNoteEventListener to this object, so that it will be pumped events. This method does not check
 	 * for duplicate subscriptions.
 	 * 
 	 * @param l The listening object.
 	 */
 	public void attach(ProcessedNoteEventListener l)
 	{
 		this.processedListeners.add(l);
 	}
 	
 	/**
 	 * Remove a subscribed ProcessedNoteEventListener. If the same object has been subscribed multiple times, this will only
 	 * remove one of the subscriptions.
 	 * 
 	 * @param l The listener to remove the subscription for.
 	 */
 	public void detach(ProcessedNoteEventListener l)
 	{
 		this.processedListeners.remove(l);
 	}
 	
 	/**
 	 * Attempts to pair a MidiEvent to one of the currently expiring NoteEvents. A ProcessedNoteEvent will be pumped to
 	 * listeners, with a matched or null NoteEvent parameter, depending on the success of the pairing. If a note is paired,
 	 * it may be removed from the expiring notes if the match is exceptionally good. If it is not removed, no expiration event
 	 * will be sent when the note would have expired.
 	 * 
 	 * This method will never pair a 'press' with a 'note off', nor a 'release' with a 'note on'.
 	 * 
 	 * @param midiEvent The input event to attempt to match up.
 	 */
 	public void handleMidiEvent(MidiEvent midiEvent)
 	{
 		List<Expirator> busy=this.pool.getBusyExpirators();
 		Expirator matched=null;
 		int matchedScore=0;
 		// score is a bit of trickery, may need some tuning
 		//  pitch:
 		//   exact:      +1000
 		//   within 5:   + 500
 		//  timing:
 		//   distance:   + 750 - (|distance|/(timeout/2) * 750)
 		//  velocity:
 		//   not currently taken into account for score
 		int noteThreshold=1250;
 		int aPitch=midiEvent.getNote();
 		long aTime=midiEvent.getTimestamp();
 		for (Expirator current : busy)
 		{
 			// don't match if the actions don't correspond
 			if ((current.getNoteEvent().getAction()==NoteAction.BEGIN)
 			     == (midiEvent.getAction()==ActionType.PRESS))
 			{
 				int currentScore=0;
 				int ePitch=current.getNoteEvent().getNote().getPitch();
 				long eTime=midiEvent.getTimestamp();
 				
 				if (ePitch==aPitch)
 				{
 					currentScore+=1000;
 				}
 				else if (Math.abs(ePitch-aPitch)<5)
 				{
 					currentScore+=500;
 				}
 				
 				currentScore+=750-(int)(((int)Math.abs(eTime-aTime))/(this.timeout/2.0)*750);
 				if (matched==null || currentScore>matchedScore)
 				{
 					matched=current;
 					matchedScore=currentScore;
 				}
 			}
 		}
 		
 		NoteEvent matchedEvent=null;
 		if (matched!=null)
 		{
 			matchedEvent=matched.getNoteEvent();
 			if (matchedScore>=noteThreshold)
 			{
 				matched.resolveNote();
 			}
 			else
 			{
 				matched.flag();
 			}
 		}
 		ProcessedNoteEvent processed=new ProcessedNoteEvent(matchedEvent,midiEvent,heuristics.judge(matchedEvent,midiEvent));
 		for (ProcessedNoteEventListener listener : this.processedListeners)
 		{
 			listener.handleProcessedNoteEvent(processed);
 		}
 	}
 	
 	/**
 	 * Signifies that a NoteEvent was never matched, and was therefore missed. A ProcessedNoteEvent will be pumped out
 	 * with a null midiEvent.
 	 * 
 	 * @param noteEvent The missed note.
 	 */
 	public void noteExpired(NoteEvent noteEvent)
 	{
 		ProcessedNoteEvent processed=new ProcessedNoteEvent(noteEvent,null,false);
 		for (ProcessedNoteEventListener listener : this.processedListeners)
 		{
 			listener.handleProcessedNoteEvent(processed);
 		}
 	}
 }

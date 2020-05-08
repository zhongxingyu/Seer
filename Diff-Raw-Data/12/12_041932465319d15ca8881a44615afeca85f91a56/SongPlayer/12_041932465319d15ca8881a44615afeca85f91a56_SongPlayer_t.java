 package crescendo.base;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import crescendo.base.song.Note;
 import crescendo.base.song.SongModel;
 import crescendo.base.song.Track;
 import crescendo.base.song.Track.TrackIterator;
 import crescendo.base.song.modifier.NoteModifier;
 
 /**
  * A SongPlayer does the actual running through the song and propagation of events.
  * 
  * @author forana
  * @author gartmannn
  */
 public class SongPlayer implements FlowController,Updatable
 {
 
 
 	/** The song that is being played.*/
 	private SongModel songModel;
 	/** map of listeners and the number of milliseconds early to send their note events */
 	private Map<NoteEventListener,Integer> listeners;
 	/** list of notes which are currently in the queue */
 	private Map<NoteEvent,List<NoteEventListener>> activeNotes;
 
 	/** list of flow controllers (objects that get told about flow events such as pause and resume) */
 	private Set<FlowController> controllers;
 
 
 	/** Runnable which does timing */
 	private UpdateTimer timer;
 	/** Thread which holds our timer runnable */
 	private Thread timerContainer;
 
 	private boolean isPaused;
 	private boolean doContinue;
 
 	/** The number of milliseconds the song has been paused */
 	private long pauseOffset;
 
 	private int longestListener = 0;
 
 	private Map<Track,TrackIterator> iterators;
 	private long nextPoll;
 	
 	/** The object that tracks chaning meta-info */
 	private SongState songState;
 	
 	/**
 	 * Create a song player for the given song model. 
 	 * Initializes the lists of controllers and listeners, and creates the timer
 	 * There should be a 1 to 1 relationship between song players and song models
 	 * @param songModel song model representing the song we wish to play.
 	 */
 	public SongPlayer(SongModel songModel)
 	{
 		this.songModel=songModel;
 		listeners = Collections.synchronizedMap(new HashMap<NoteEventListener,Integer>());
 		controllers = Collections.synchronizedSet(new HashSet<FlowController>());
 		timer = new UpdateTimer(this);
 		timerContainer = new Thread(timer);
 		this.songState=new SongState(songModel.getBPM(),songModel.getTimeSignature(),songModel.getKeySignature());
 		initializeIterators();
 	}
 	
 	/**
 	 * Get the object that tracks song meta-info.
 	 * @return The held SongState instance.
 	 */
 	public SongState getSongState()
 	{
 		return this.songState;
 	}
 
 	/**
 	 * Begin playing the song
 	 * This resets all of the state variables, <b>DO NOT</b> resume the song after pausing, use the resume function. 
 	 */
 	public void play() {
 		activeNotes = new HashMap<NoteEvent,List<NoteEventListener>>();
 		nextPoll = System.currentTimeMillis();
 		isPaused = false;
 		doContinue = true;
 		pauseOffset = 0;
 		timerContainer.start();
 	}
 
 	public void pause() {
 		isPaused = true;
 		timer.pause();
 		for(FlowController controller : controllers) {
 			controller.pause();
 		}
 	}
 
 	public void resume() {
 		isPaused = false;
 		for(NoteEvent event : activeNotes.keySet()) {
 			event.setTimestamp(event.getTimestamp()+pauseOffset);
 		}
 		pauseOffset = 0;
 		for(FlowController controller : controllers) {
 			controller.resume();
 		}
 		timer.resume();
 	}
 
 	public void stop() {
 		for(FlowController controller : controllers) {
 			controller.stop();
 		}
 		doContinue=false;
 		timer.stop();
 		try {
 			// 3/15/11 added interrupt; apparently just joining will just block
 			timerContainer.interrupt();
 			timerContainer.join();
 		} catch (InterruptedException e) {}
 		timerContainer = new Thread(new UpdateTimer(this));
 		
 		//We have to do this here just in case the user presses play again
 		initializeIterators();
 	}
 
 	private void initializeIterators(){
 		iterators = new HashMap<Track,TrackIterator>();
 		for(Track track : this.songModel.getTracks()){
 			iterators.put(track, track.iterator());
 		}
 	}
 	
 	@Override
 	public void songEnd() {
 		for(FlowController controller : controllers) {
 			controller.songEnd();
 		}
 		timer.stop();
 		doContinue=false;
 	}
 
 	@Override
 	public void suspend() {
 		isPaused = true;
 		timer.pause();
 		for(FlowController controller : controllers) {
 			controller.suspend();
 		}		
 	}
 
 	/**
 	 * Get the song model for this song
 	 * @return the song model that this player is playing
 	 */
 	public SongModel getSong()
 	{
 		return this.songModel;
 	}
 
 	/** 
 	 * Subscribe to receive flow control calls from the song player
 	 * @param controller instance of FlowController to call on
 	 */
 	public void attach(FlowController controller) {
 		controllers.add(controller);
 	}
 
 	/**
 	 * Subscribe to receive note events from the song player
 	 * @param listener instance of NoteEventListener to pump events to
 	 * @param time number of milliseconds in advance the listener wants to receive note events
 	 */	
 	public void attach(NoteEventListener listener, int time) {
 		listeners.put(listener, time);
 		if (listener instanceof FlowController) {
 			this.attach((FlowController)listener);
 		}
 		if(time>this.longestListener){
 			longestListener = time;
 		}
 	}
 
 	/**
 	 * Remove listener from the listeners who are receiving note events from the player.
 	 * @param listener - NoteEventLIstener instance to remove from the observer list
 	 */
 	public void detach(NoteEventListener listener) {
 		listeners.remove(listener);
 	}
 
 	/**
 	 * Remove controller from the controllers who are receiving control calls from the player
 	 * @param controller instance of FlowController to remove from the list
 	 */
 	public void detach(FlowController controller) {
 		controllers.remove(controller);
 	}
 
 	/**
 	 * Set the current iterators to be a percentage of the way through the song
 	 * @param percentage - percent progress through the song
 	 */
 	public void setPosition(double percentage){
 		double totalDuration = songModel.getDuration();
 		double currentDuration = 0;
 		final double beatCount = .5;
 		while(currentDuration/totalDuration<percentage){
 			for(Iterator<TrackIterator> iterIter = iterators.values().iterator(); iterIter.hasNext();){
 				TrackIterator iter = iterIter.next();
 				iter.next(beatCount);
 				currentDuration+=beatCount;
 			}
 		}
 	}
 
 	/**
 	 * Set the current iterators to be at the position of the given note
 	 * @param startNote - note which marks the current progress through the song
 	 */
 	public void setPosition(Note startNote){
 		Track activeTrack = startNote.getTrack();
 		int beatCount = 0;
 		Note currentNote = null;
 		TrackIterator activeTrackIterator = activeTrack.iterator();
 		//Find the number of beats into its track the startNote is
 		while((currentNote = activeTrackIterator.next()) != startNote){
 			if(currentNote!=null){
 				beatCount+=currentNote.getDuration();
 			}
 		}
 		//Move all of the iterators to that position
 		for(Iterator<TrackIterator> iterIter = iterators.values().iterator(); iterIter.hasNext();){
 			TrackIterator iter = iterIter.next();
 			if(!iter.equals(iterators.get(activeTrack))){
 				iter.next(beatCount);
 			}
 		}
 	}
 
 	/**
 	 * Sends out notes to the listeners. Only sends them out if it is
 	 * within the requested time frame. This method also removes old notes 
 	 * from the list of active notes.
 	 */
 	public void update() {
 		long now = System.currentTimeMillis();
 
 		if(now>=nextPoll){
 			double bpms = (double)songState.getBPM()/(double)60000; //calculate the number of beats per millisecond
 			double beats= longestListener * bpms;
 			for(Iterator<TrackIterator> iterIter=iterators.values().iterator(); iterIter.hasNext();){
 				TrackIterator iter=iterIter.next();
 				double offset = iter.getOffset() / bpms;	//figure out the offset in milliseconds (rather than in beats)
 				//get the list of notes within the next amount of beats. 
 				//Get notes more than how many beats ahead the longest listener wants their beats
 				if (iter.hasNext())
 				{
 					List<Note> notes = iter.next(beats);
 					for(Note note : notes) {
 						if(note.getDynamic()>0){
 							Set<Note> playNotes=new HashSet<Note>();
 							playNotes.add(note);
 							for (NoteModifier modifier : note.getModifiers())
 							{
 								playNotes.addAll(modifier.getNotes());
 							}
 							for (Note pnote : playNotes)
 							{
 								//Create the note events
 								NoteEvent ne = new NoteEvent(pnote, NoteAction.BEGIN, (long) ((longestListener)+offset+nextPoll));
 								NoteEvent neEnd = new NoteEvent(pnote,NoteAction.END, (long) ((longestListener)+offset+nextPoll+pnote.getDuration()/bpms));
 								activeNotes.put(ne,new LinkedList<NoteEventListener>());
 								activeNotes.put(neEnd,new LinkedList<NoteEventListener>());
 							}
 						}
 						offset+=note.getDuration()/bpms;
 						// handle modifiers
 						for (NoteModifier modifier : note.getModifiers())
 						{
 							modifier.execute(songState);
 						}
 					}
 				}
 				else
 				{
 					iterIter.remove();
 				}
 			}
 			nextPoll = nextPoll+longestListener;
 		}
 
 		//Pump out note events
 		NoteEvent event = null;
 		for(Iterator<NoteEvent> i = activeNotes.keySet().iterator(); i.hasNext();) {
 			event = i.next();
 			if(now > event.getTimestamp())
 			{
 				i.remove(); //This removes the note from the map
 			}else{
 				List<NoteEventListener> thisList=activeNotes.get(event);
 				for(NoteEventListener listener : listeners.keySet()) {
 					//Only send the note if it is within the correct range, and the listener has not received it yet
 					if(now >= (event.getTimestamp()-listeners.get(listener))
 							&& !thisList.contains(listener)) {
 						listener.handleNoteEvent(event);
 						thisList.add(listener);
 					}
 				}
 			}
 		}
 		
 		// check if song is done
 		if (iterators.size()==0 && activeNotes.size()==0)
 		{
			songEnd();
 		}
 	}
 }

 package net.thiagoalz.hermeto.panel;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import net.thiagoalz.hermeto.panel.listeners.ExecutionControl;
 import net.thiagoalz.hermeto.panel.listeners.ExecutionEvent;
 import net.thiagoalz.hermeto.panel.listeners.ExecutionListener;
 import android.util.Log;
 
 /**
  * The sequencer is responsible for execute the sequence action on the game 
  * like start, stop, reset the music.
  *  
  * @see GameManager#GameManager
  */
 public class Sequencer implements ExecutionControl {
 	private static final String TAG = Sequencer.class.getCanonicalName();
 	
 	private GameManager gameManager;
 	private Timer timer;
 	private List<ExecutionListener> executionListeners;
 	private int currentPlayingLine = 0;
 	private int timeSequence = 200;
 	
 	public Sequencer(GameManager gameManager) {
 		this.gameManager = gameManager;
 		this.executionListeners = new ArrayList<ExecutionListener>();
 		this.executionListeners.add(gameManager);
 	}
 	
 	@Override
 	public boolean isPlaying() {
 		boolean state = gameManager.getGameContext().isPlaying();
 		Log.d(TAG, "The game is playing? " + state);
 		return state;
 	}
 
 	@Override
 	public void start() {
 		Log.d(TAG, "Starting the sequencer at column #" + currentPlayingLine + ".");
 		registerSoundTimer();
 		ExecutionEvent event = new ExecutionEvent();
 		for (ExecutionListener listener : executionListeners) {
 			listener.onStart(event);
 		}
 	}
 
 	@Override
 	public void stop() {
		this.pause();
 		Log.d(TAG, "Stoping the sequencer, and reset the current line.");
		currentPlayingLine = 0;
 		ExecutionEvent event = new ExecutionEvent();
 		for (ExecutionListener listener : executionListeners) {
 			listener.onStop(event);
 		}
 	}
 
 	@Override
 	public void pause() {
 		Log.d(TAG, "Pausing the sequencer at column #" + currentPlayingLine + ".");
 		
 		if (timer != null) {
			if(gameManager.getBPM()>=60){
				synchronized(timer) {
					timer.cancel();
					timer = null;
				}
			}else{//If BPM is too slow, do not wait for completion.
 				timer.cancel();
 				timer = null;
 			}
 		}
 		ExecutionEvent event = new ExecutionEvent();
 		for (ExecutionListener listener : executionListeners) {
 			listener.onPause(event);
 		}
 	}
 
 	@Override
 	public void reset() {
 		Log.d(TAG, "Reseting the sequencer.");
 		this.stop();
 		ExecutionEvent event = new ExecutionEvent();
 		for (ExecutionListener listener : executionListeners) {
 			listener.onReset(event);
 		}
 	}
 
 	@Override
 	public void updateBPM(int bpm) {
 		// TODO Auto-generated method stub
 	}
 	
 	public void removeExecutionListener(ExecutionListener executionListener) {
 		this.executionListeners.remove(executionListener);
 	}
 	
 	public void addExecutionListener(ExecutionListener executionListener) {
 		this.executionListeners.add(executionListener);
 	}
 	
 	private void registerSoundTimer() {
 		if (timer != null) {
			if(gameManager.getBPM()>=60){
				synchronized(timer) {
					Log.d(TAG, "Cancelling timer.");
					timer.cancel();
				}
			}else{//If BPM is too slow, do not wait for completion.
 				Log.d(TAG, "Cancelling timer.");
 				timer.cancel();
 			}
 		}
 		// Think this timer is not that trustable. It may be the cause of the lags.
 		timer = new Timer();
 		Log.d(TAG, "Scheduling a new timer.");
 		timer.scheduleAtFixedRate(new TimerTask() {
 			public void run() {
 				long startTime = System.currentTimeMillis();
 				synchronized(timer) {
 					startPlayingGroup(currentPlayingLine);
 					
 					/* keep it turned on until the half of the total period time */
 					long waitTime = (timeSequence / 2)
 							- (System.currentTimeMillis() - startTime); 
 	
 					// Check if it can wait more
 					if (waitTime > 0) { 
 						try {
 							Thread.sleep(waitTime);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 					}
 					stopPlayingGroup(currentPlayingLine);
 					// Circular
 					int[] dimensions = gameManager.getGameContext().getDimensions();
 					currentPlayingLine = (currentPlayingLine + 1) % dimensions[0];
 					Log.d(TAG, "Setting the current playing column to : " + currentPlayingLine + ".");
 				}
 			}
 		}, 0, timeSequence);
 	}
 	
 	private void startPlayingGroup(int group) {
 		Log.d(TAG, "Starting playing group " + group);
 		List<Position> playingPositions = gameManager.getGameContext().getColumnMarkedSquares(group);
 		if (playingPositions.size() > 0) {
 			ExecutionEvent event = new ExecutionEvent();
 			event.setPositions(playingPositions);
 			for (ExecutionListener listener : executionListeners) {
 				listener.onStartPlayingGroup(event);
 			}
 		}
 	}
 
 	private void stopPlayingGroup(int group) {
 		Log.d(TAG, "Stoping playing group " + group);
 		List<Position> playingPositions = gameManager.getGameContext().getColumnMarkedSquares(group);
 		if (playingPositions.size() > 0) {
 			ExecutionEvent event = new ExecutionEvent();
 			event.setPositions(playingPositions);
 			for (ExecutionListener listener : executionListeners) {
 				listener.onStopPlayingGroup(event);
 			}
 		}
 	}
 
 	public GameManager getGameManager() {
 		return gameManager;
 	}
 
 	public void setGameManager(GameManager gameManager) {
 		this.gameManager = gameManager;
 	}
 
 	public int getTimeSequence() {
 		return timeSequence;
 	}
 
 	public void setTimeSequence(int timeSequence) {
 		this.timeSequence = timeSequence;
 	}
 }

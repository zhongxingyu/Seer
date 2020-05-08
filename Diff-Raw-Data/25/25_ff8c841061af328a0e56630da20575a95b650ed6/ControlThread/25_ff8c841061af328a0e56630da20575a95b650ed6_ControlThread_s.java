 package at.fhv.audioracer.client.android.activity.thread;
 
 import at.fhv.audioracer.client.android.aui.SoundPlayer2D;
 import at.fhv.audioracer.client.android.controller.ClientManager;
 import at.fhv.audioracer.client.player.IPlayerClientListener;
 import at.fhv.audioracer.core.util.Position;
 
 public abstract class ControlThread implements Runnable {
 	
 	protected long _maxControlWait;
 	
 	protected volatile long _lastUpdate;
 	private volatile Thread _thread;
 	
 	private SoundPlayer2D _soundPlayer;
 	
 	private IPlayerClientListener _listener;
 	
 	// We should probably get this from the server.
	protected static double MAX_DISTANCE = 1000; // sqrt(957² + 540²) = ~1000 --> x = 957, y = 540
 	protected static double SCALE_OF_VELOCITY = 1;
 	
 	public ControlThread() {
 		_maxControlWait = 10;
 		_soundPlayer = new SoundPlayer2D(MAX_DISTANCE, SCALE_OF_VELOCITY);
 		
 		_listener = new IPlayerClientListener.Adapter() {
 			@Override
 			public void onUpdateCheckpointDirection(Position position) {
 				_soundPlayer.setPosition(position);
 			}
 		};
 		
 		ClientManager.getInstance().getPlayerClient().getListenerList().add(_listener);
 	}
 	
 	public void start() {
 		_thread = new Thread(this);
 		_thread.start();
 		_soundPlayer.play();
 	}
 	
 	public void stop() {
 		_thread = null;
 		_soundPlayer.stop();
 		reset();
 	}
 	
 	protected abstract void reset();
 	
 	@Override
 	public void run() {
 		Thread thisThread = Thread.currentThread();
 		_lastUpdate = System.currentTimeMillis();
 		while (thisThread == _thread) {
 			long now = System.currentTimeMillis();
 			// call the 'hook'
 			control();
 			long wait = _maxControlWait - (System.currentTimeMillis() - _lastUpdate);
 			_lastUpdate = now;
 			if (wait > 0) {
 				try {
 					Thread.sleep(wait);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	public abstract void control();
 	
 }

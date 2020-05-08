 package com.pwr.zpi.services;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
 import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.location.LocationListener;
 import com.google.android.gms.location.LocationRequest;
 import com.pwr.zpi.ActivityActivity;
 import com.pwr.zpi.MainScreenActivity;
 import com.pwr.zpi.R;
 import com.pwr.zpi.RunListener;
 import com.pwr.zpi.RunListenerApi;
 import com.pwr.zpi.database.Database;
 import com.pwr.zpi.database.entity.SingleRun;
 import com.pwr.zpi.database.entity.Workout;
 import com.pwr.zpi.database.entity.WorkoutActionWarmUp;
 import com.pwr.zpi.listeners.ICountDownListner;
 import com.pwr.zpi.utils.AssetsPlayer;
 import com.pwr.zpi.utils.AssetsPlayer.AssetsMp3Files;
 import com.pwr.zpi.utils.CounterRunnable;
 import com.pwr.zpi.utils.Notifications;
 import com.pwr.zpi.utils.Pair;
 import com.pwr.zpi.utils.SpeechSynthezator;
 
 public class LocationService extends Service implements LocationListener, ConnectionCallbacks,
 	OnConnectionFailedListener, ICountDownListner {
 	
 	private static final String TAG = LocationService.class.getSimpleName();
 	
 	private LocationClient mLocationClient;
 	private LocationRequest mLocationRequest;
 	
 	public static final int STARTED = 1;
 	public static final int PAUSED = 2;
 	public static final int STOPED = 3;
 	public static final int COUNTER_COUNT_DOWN = 0x1;
 	public static final int COUNTER_WARM_UP = 0x2;
 	private int state;
 	private static final long LOCATION_UPDATE_FREQUENCY = 1000;
 	private static final long MAX_UPDATE_TIME = 5000;
 	public static final int REQUIRED_ACCURACY = 3000; //FIXME change to lower
 	private LinkedList<LinkedList<Pair<Location, Long>>> traceWithTime;
 	private final List<RunListener> listeners = new ArrayList<RunListener>();
 	private boolean isWromUpInProgress;
 	private boolean isFirstTime;
 	private ConnectionResult connectionResult;
 	private Location latestLocation;
 	private ArrayList<Location> locationList;
 	private SingleRun singleRun;
 	private Calendar calendar;
 	// time counting fields
 	private Handler handler;
 	private Runnable timeHandler;
 	public static final String CONNECTION_FIAILED_TAG = "connectionFailed";
 	
 	private SpeechSynthezator speechSynthezator;
 	private AssetsPlayer soundsPlayer;
 	private int countDownTime;
 	
 	long startTime;
 	long pauseStartTime;
 	long pauseTime;
 	//saved here in case of activity closing
 	Long time;
 	double distance;
 	Workout workout;
 	private boolean connectionFailed;
 	private boolean isConnected;
 	private final RunListenerApi.Stub apiEndpoint = new RunListenerApi.Stub() {
 		
 		@Override
 		public List<Location> getWholeRun() throws RemoteException {
 			return locationList;
 		}
 		
 		@Override
 		public double getDistance() throws RemoteException {
 			return distance;
 		}
 		
 		@Override
 		public long getTime() throws RemoteException {
 			synchronized (time) {
 				return time;
 			}
 		}
 		
 		@Override
 		public Location getLatestLocation() throws RemoteException {
 			return latestLocation;
 		}
 		
 		@Override
 		public Intent getConnectionResult() throws RemoteException {
 			Intent intent = new Intent(LocationService.class.getSimpleName());
 			intent.putExtra(CONNECTION_FIAILED_TAG, connectionFailed);
 			if (connectionFailed) {
 				int statusCode = connectionResult.getErrorCode();
 				PendingIntent pendingIntent = connectionResult.getResolution();
 				// Add data
 				intent.putExtra("pending_intent", connectionResult.getResolution());
 				intent.putExtra("status_code", statusCode);
 			}
 			return intent;
 		}
 		
 		@Override
 		public void setStarted(Workout workout, int countDownTime) throws RemoteException {
 			if (state == STOPED) {
 				locationList = new ArrayList<Location>();
 				state = STARTED;
 				isFirstTime = true;
 				LocationService.this.countDownTime = countDownTime;
 				prepareWorkout(workout);
 				initActivityRecording();
 				Notification note = Notifications.createNotification(LocationService.this, ActivityActivity.class,
 					R.string.app_name, R.string.notification_message);
 				startForeground(1, note);
 			}
 			
 		}
 		
 		@Override
 		public void setPaused() throws RemoteException {
 			pauseStartTime = System.currentTimeMillis();
 			state = PAUSED;
 			//handler.removeCallbacks(timeHandler);
 		}
 		
 		@Override
 		public void setResumed() throws RemoteException {
 			if (state == PAUSED) {
 				pauseTime += System.currentTimeMillis() - pauseStartTime;
 				state = STARTED;
 				Log.i(TAG, state + "");
 				traceWithTime.add(new LinkedList<Pair<Location, Long>>());
 				//handler.post(timeHandler);
 			}
 		}
 		
 		@Override
 		public void setStoped() throws RemoteException {
 			state = STOPED;
 		}
 		
 		@Override
 		public void addListener(RunListener listener) throws RemoteException {
 			synchronized (listeners) {
 				listeners.add(listener);
 			}
 		}
 		
 		@Override
 		public void removeListener(RunListener listener) throws RemoteException {
 			
 			synchronized (listeners) {
 				listeners.clear();
 			}
 			//we dont need more then one listener at once
 		}
 		
 		@Override
 		public int getGPSStatus() throws RemoteException {
 			
 			return checkGPS();
 		}
 		
 		@Override
 		public void prepareTextToSpeech() throws RemoteException {
 			speechSynthezator = new SpeechSynthezator(getApplicationContext());
 		}
 		
 		@Override
 		public void doSaveRun(boolean save, String name) throws RemoteException {
 			if (save) {
 				saveRun(name);
 			}
 			distance = 0;
 			pauseTime = 0;
 			locationList = null;
 			
 			stopForeground(true);
 		}
 		
 		@Override
 		public void onSoundSettingChange(boolean enabled) throws RemoteException {
 			speechSynthezator.setSpeakingEnabled(enabled);
 			soundsPlayer.setCanPlay(enabled);
 		}
 		
 	};
 	
 	private void zeroFields() {
 		startTime = 0;
 		pauseStartTime = 0;
 		pauseTime = 0;
 		time = 0L;
 		distance = 0;
 	}
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		if (LocationService.class.getName().equals(intent.getAction())) {
 			Log.d(TAG, "Bound by intent " + intent);
 			return apiEndpoint;
 		}
 		else return null;
 	}
 	
 	private void prepareWorkout(Workout workout) {
 		this.workout = workout;
 		if (workout != null) {
 			workout.getOnNextActionListener().setConext(getApplicationContext());
 		}
 	}
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		state = STOPED;
 		pauseTime = 0;
 		time = new Long(0);
 		isConnected = false;
 		connectionFailed = false;
 		isWromUpInProgress = false;
 		handler = new Handler();
 		mLocationClient = new LocationClient(getApplicationContext(), this, this);
 		mLocationRequest = LocationRequest.create();
 		mLocationRequest.setInterval(LOCATION_UPDATE_FREQUENCY);
 		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
 		mLocationClient.connect();
 		Log.i(TAG, "Service creating");
 		soundsPlayer = new AssetsPlayer(getApplicationContext(), AssetsMp3Files.Beep);
 		Toast.makeText(getApplicationContext(), "Service creating", Toast.LENGTH_LONG).show(); //TODO remove
 	}
 	
 	@Override
 	public void onDestroy() {
 		if (soundsPlayer != null) {
 			soundsPlayer.stopPlayer();
 		}
 		if (mLocationClient.isConnected()) {
 			mLocationClient.removeLocationUpdates(this);
 		}
 		
 		mLocationClient.disconnect();
 		Log.i(TAG, "Service destroying");
 		super.onDestroy();
 	}
 	
 	@Override
 	public void onLocationChanged(Location location) {
 		//		Log.i(TAG, "new Location state: " + state);
 		
 		if (state == STARTED) {
 			locationList.add(location);
 			updateTraceWithTime(location);
 			if (latestLocation != null) {
 				distance += location.distanceTo(latestLocation);
 			}
 		}
 		latestLocation = location;
 		synchronized (listeners) {
 			Iterator<RunListener> it = listeners.iterator();
 			while (it.hasNext()) {
 				RunListener listener = it.next();
 				try {
 					listener.handleLocationUpdate();
 				}
 				catch (RemoteException e) {
 					Log.w(TAG, "Failed to tell listener" + listener + " about update ", e);
 					it.remove();
 				}
 			}
 		}
 		
 	}
 	
 	private void updateTraceWithTime(Location newLocation) {
 		if (newLocation.getAccuracy() < REQUIRED_ACCURACY) {
 			// not first point after start or resume
 			
 			if (traceWithTime.isEmpty()) {
 				traceWithTime.add(new LinkedList<Pair<Location, Long>>());
 			}
 			synchronized (time) {
 				traceWithTime.getLast().add(new Pair<Location, Long>(newLocation, time));
 			}
 		}
 	}
 	
 	@Override
 	public void onConnected(Bundle bundle) {
 		Log.i(TAG, "onConnected LocationListener");
 		mLocationClient.requestLocationUpdates(mLocationRequest, this);
 		isConnected = true;
 	}
 	
 	@Override
 	public void onDisconnected() {
 		Log.i(TAG, "onDisconnected LocationListener");
 		isConnected = false;
 	}
 	
 	@Override
 	public void onConnectionFailed(ConnectionResult connectionResult) {
 		Log.i(TAG, "onConnectionFailed LocationListener telling " + listeners.size() + " listeners");
 		this.connectionResult = connectionResult;
 		connectionFailed = true;
 		for (RunListener listener : listeners) {
 			try {
 				listener.handleConnectionResult();
 			}
 			catch (RemoteException e) {
 				Log.w(TAG, "Failed to tell listener about connaction failed ", e);
 			}
 		}
 	}
 	
 	private int checkGPS() {
 		
 		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
 		//service.addGpsStatusListener(this);
 		short gpsStatus = 0;
 		
 		boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
 		
 		if (!enabled) {
 			gpsStatus = MainScreenActivity.GPS_NOT_ENABLED;
 		}
 		else if (isConnected && (latestLocation == null || latestLocation.getAccuracy() > REQUIRED_ACCURACY)) {
 			gpsStatus = MainScreenActivity.NO_GPS_SIGNAL;
 		}
 		else {
 			gpsStatus = MainScreenActivity.GPS_WORKING;
 		}
 		
 		return gpsStatus;
 	}
 	
 	private void initActivityRecording() {
 		
 		singleRun = new SingleRun();
 		calendar = Calendar.getInstance();
 		singleRun.setStartDate(calendar.getTime());
 		traceWithTime = new LinkedList<LinkedList<Pair<Location, Long>>>();
 		
 		if (workout != null) {
 			workout.getOnNextActionListener().setSyntezator(speechSynthezator);
 			workout.notifyListeners(workout.getActions().get(0));
 		}
 		
 		startTimeEvaluation();
 	}
 	
 	private void startTimeEvaluation() {
 		Log.i(TAG, "starting time evaluation");
 		prepareTimeCountingHandler();
 		if (workout != null && workout.isWarmUp()) {
 			Log.i(TAG, "warm up count down");
 			startWarmUp();
 		}
 		else {
 			Log.i(TAG, "count down without warm up");
 			startRunAfterCountDown();
 		}
 	}
 	
 	Runnable startTimeSetHandler = new Runnable() {
 		@Override
 		public void run() {
 			startTime = System.currentTimeMillis();
 		}
 	};
 	
 	Runnable zeroFieldsHandler = new Runnable() {
 		
 		@Override
 		public void run() {
 			zeroFields();
 		}
 	};
 	
 	private void startWarmUp() {
 		handler.post(startTimeSetHandler);
 		//		int minutes = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(this.getString(R.string.key_warm_up_time), "3"));
 		int minutes = ((WorkoutActionWarmUp) workout.getActions().get(0)).getWorkoutTime();
 		int seconds = minutes * 60;
 		handler.post(new CounterRunnable(COUNTER_WARM_UP, seconds, this));
 	}
 	
 	private void startRunAfterCountDown() {
 		soundsPlayer.changePlayer(AssetsMp3Files.Beep);
 		Log.i(TAG, "count down start " + countDownTime + " handler " + handler);
 		handler.post(new CounterRunnable(COUNTER_COUNT_DOWN, countDownTime, this));
 	}
 	
 	@Override
 	public void onCountDownUpadte(int counterID, int howMuchLeft) {
 		Log.i(TAG, "Count down update " + howMuchLeft);
 		switch (counterID) {
 			case COUNTER_COUNT_DOWN:
 				listenersHandleCountDownChange(howMuchLeft);
 				Log.i(TAG, "beep !");
 				soundsPlayer.play();
 				break;
 			case COUNTER_WARM_UP:
 				processWorkout();
 				Iterator<RunListener> it = listeners.iterator();
 				time = System.currentTimeMillis() - startTime - pauseTime;
 				while (it.hasNext()) {
 					RunListener listener = it.next();
 					try {
 						listener.handleTimeChange();
 						listener.handleWorkoutChange(workout, isFirstTime);
 						isFirstTime = false;
 						Log.i(TAG, listeners.size() + "");
 					}
 					catch (RemoteException e) {
 						Log.w(TAG, "Failed to tell listener about workout update", e);
 					}
 				}
 				break;
 			default:
 				break;
 		}
 		handler.postDelayed(new CounterRunnable(counterID, howMuchLeft - 1, this), 1000);
 	}
 	
 	@Override
 	public void onCountDownDone(int counterID, int howMuchLeft) {
 		switch (counterID) {
 			case COUNTER_COUNT_DOWN:
 				soundsPlayer.stopPlayer();
 				soundsPlayer.changePlayer(AssetsMp3Files.Go);
 				listenersHandleCountDownChange(howMuchLeft);
 				soundsPlayer.play();
 				break;
 			case COUNTER_WARM_UP:
 				singleRun = new SingleRun();
 				calendar = Calendar.getInstance();
 				singleRun.setStartDate(calendar.getTime());
 				traceWithTime = new LinkedList<LinkedList<Pair<Location, Long>>>();
 				distance = 0;
 				pauseTime = 0;
 				time = 0L;
 				locationList = new ArrayList<Location>();
 				startRunAfterCountDown();
 				isWromUpInProgress = false;
				workout.progressWorkout(0,
					(long) (((WorkoutActionWarmUp) workout.getActions().get(0)).getWorkoutTime() * 60 * 1000));
 				break;
 			default:
 				break;
 		}
 		Log.i(TAG, "Count down done");
 		handler.postDelayed(new CounterRunnable(counterID, howMuchLeft - 1, this), 1000);
 	}
 	
 	@Override
 	public void onCountDownPostAction(int counterID, int howMuchLeft) {
 		switch (counterID) {
 			case COUNTER_COUNT_DOWN:
 				soundsPlayer.stopPlayer();
 				listenersHandleCountDownChange(howMuchLeft);
 				startCountingTime();
 				break;
 			default:
 				break;
 		}
 	}
 	
 	private void listenersHandleCountDownChange(int howMuchLeft) {
 		Iterator<RunListener> it = listeners.iterator();
 		while (it.hasNext()) {
 			RunListener listener = it.next();
 			try {
 				listener.handleCountDownChange(howMuchLeft);
 			}
 			catch (RemoteException e) {
 				Log.w(TAG, "Failed to tell listener about count down update ", e);
 			}
 		}
 	}
 	
 	private void startCountingTime() {
 		//		startTime = System.currentTimeMillis();
 		handler.post(zeroFieldsHandler);
 		handler.post(startTimeSetHandler);
 		handler.post(timeHandler);
 	}
 	
 	private void prepareTimeCountingHandler() {
 		timeHandler = new Runnable() {
 			
 			@Override
 			public void run() {
 				runTimerTask();
 			}
 		};
 	}
 	
 	protected void runTimerTask() {
 		if (state == STARTED && startTime != 0) {
 			synchronized (time) {
 				time = System.currentTimeMillis() - startTime - pauseTime;
 				boolean changeWorkout = false;
 				Log.i(TAG, "current and start times: " + System.currentTimeMillis() + " " + startTime);
 				if (workout != null) {
 					processWorkout();
 					changeWorkout = true;
 				}
 				Log.i(TAG, time + "");
 				Iterator<RunListener> it = listeners.iterator();
 				while (it.hasNext()) {
 					RunListener listener = it.next();
 					try {
 						listener.handleTimeChange();
 						if (changeWorkout) {
 							listener.handleWorkoutChange(workout, isFirstTime);
 						}
 						Log.i(TAG, listeners.size() + "");
 					}
 					catch (RemoteException e) {
 						Log.w(TAG, "Failed to tell listener about new Time ", e);
 						//		it.remove();
 						
 					}
 				}
 			}
 		}
 		handler.postDelayed(timeHandler, 1000);
 	}
 	
 	private void processWorkout() {
 		if (workout.hasNextAction()) {
 			workout.getOnNextActionListener().setSyntezator(speechSynthezator);
 			workout.progressWorkout(distance, time);
 		}
 	}
 	
 	// invoke when finishing activity
 	private void saveRun(String name) {
 		// add last values
 		singleRun.setEndDate(calendar.getTime());
 		singleRun.setRunTime(time);
 		singleRun.setDistance(distance);
 		singleRun.setTraceWithTime(traceWithTime);
 		singleRun.setName(name);
 		//TODO remove
 		Log.i("debug1", time + " " + distance + " " + name);
 		
 		// store in DB
 		Database db = new Database(this);
 		db.insertSingleRun(singleRun);
		zeroFields();
 	}
 }

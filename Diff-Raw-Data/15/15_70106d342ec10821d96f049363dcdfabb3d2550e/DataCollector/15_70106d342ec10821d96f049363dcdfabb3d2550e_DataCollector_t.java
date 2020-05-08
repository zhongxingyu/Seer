 package cmusv.mr.carbon.data;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Locale;
 
 import org.json.JSONObject;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.sqlite.SQLiteException;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.util.Log;
 import android.widget.Toast;
 import cmusv.mr.carbon.TrafficLog;
 import cmusv.mr.carbon.data.algorithm.DataAnalyst;
 import cmusv.mr.carbon.data.algorithm.DataAnalyst.DataType;
 import cmusv.mr.carbon.data.stats.TripStatistics;
 import cmusv.mr.carbon.db.DatabaseHelper;
 import cmusv.mr.carbon.io.file.CsvTrackWriter;
 import cmusv.mr.carbon.io.sendToServer.ClientHelper;
 import cmusv.mr.carbon.utils.LocationUtils;
 import cmusv.mr.carbon.utils.SharepreferenceHelper;
 
 public class DataCollector implements LocationListener, SensorEventListener {
 	private static final int TWO_MINUTES = 1000 * 60 * 2;
 	private LocationManager mLocationManager;
 	private SensorManager mSensorManager;
 	private Vibrator mVibrator;
 	private double length;
 	private int minRecordingDistance = 1; // 5m
 	private int maxRecordingDistance = 200; // 200m
 	private int minRequiredAccuracy = 100;
 	private int sensorTypes[] = new int[] { Sensor.TYPE_ACCELEROMETER };
 	private static final float NS2S = 1.0f / 1000000000.0f;
 	private float[] previousEventValue = new float[] { 0, 0, 0 };
 	private float mDeltaAccelerometer = 0;
 	private String TAG = DataCollector.class.getSimpleName();
 	private DatabaseHelper dbHelper;
 	private long recordingTrackId = -1L;
 	private boolean isRecording = false;
 	private final long timeWindow = 15 * 1000;
 	private DataAnalyst dataAnalyst;
 	private Location lastLocation;
 	private DataWindow dataWindow;
 	private boolean isMoving;
 	private Location lastValidLocation;
 	private Track recordingTrack;
 	private DataType trackDataType = DataType.ERROR;
 	private int walkingCnt = 0;
 	private int bikingCnt = 0;
 	private int trainCnt = 0;
 	private int drivingCnt = 0;
 
 	private float deltaAccelerometerReading(float[] oldReading,
 			float[] newReading) {
 		float delta = 0;
 		for (int i = 0; i < 3; i++) {
 			delta += Math.abs(oldReading[i] - newReading[i]);
 		}
 		Log.d(TAG, "delta:" + delta);
 
 		return delta;
 	}
 
 	private Context mContext;
 
 	public DataCollector(Context context) {
 		mLocationManager = (LocationManager) context
 				.getSystemService(Context.LOCATION_SERVICE);
 		mSensorManager = (SensorManager) context
 				.getSystemService(Context.SENSOR_SERVICE);
 		mVibrator = (Vibrator) context
 				.getSystemService(Service.VIBRATOR_SERVICE);
 
 		dbHelper = new DatabaseHelper(context);
 		dataAnalyst = new DataAnalyst(null);
 
 		mContext = context;
 	}
 
 	public void startNewTrack() {
 		long startTime = System.currentTimeMillis();
 		Track track = new Track();
 		TripStatistics trackStats = track.getTripStatistics();
 		trackStats.setStartTime(startTime);
 		recordingTrackId = dbHelper.insertTrack(track);
 		length = 0;
 		Toast.makeText(mContext, "new track id:" + recordingTrackId,
 				Toast.LENGTH_SHORT).show();
 
 	}
 
 	private boolean isTrackInProgress() {
 		return recordingTrackId != -1 || isRecording;
 	}
 
 	private void endCurrentTrack() {
 		if (!isTrackInProgress()) {
 			return;
 		}
 		isRecording = false;
 		Track recordedTrack = dbHelper.getTrack(recordingTrackId);
 		if (recordedTrack != null) {
 			long lastRecordedLocationId = dbHelper
 					.getLastLocationId(recordingTrackId);
 			if (lastRecordedLocationId >= 0 && recordedTrack.getStopId() >= 0) {
 				recordedTrack.setStopId(lastRecordedLocationId);
 			}
 			TripStatistics tripStatistics = recordedTrack.getTripStatistics();
 			tripStatistics.setStopTime(System.currentTimeMillis());
 			tripStatistics.setTotalTime(tripStatistics.getStopTime()
 					- tripStatistics.getStartTime());
 			tripStatistics.setTotalDistance(length);
 			Log.d(TAG, tripStatistics.toString());
 			// length
 			dbHelper.updateTrack(recordedTrack);
 			final File file = writeTrack2File(recordedTrack);
 			UploadThread t = new UploadThread();
 			t.setTripStatistics(tripStatistics);
			t.setDataTypeCnt(walkingCnt, bikingCnt, drivingCnt, trainCnt);
 			t.setFile(file);
 			t.start();
 		}
 
 		recordingTrackId = -1L;
 
 	}
 
 	class UploadThread extends Thread {
 		private TripStatistics tripStatistics = null;
 		private File file = null;
 		private int walkingCnt = 0;
 		private int bikingCnt = 0;
 		private int drivingCnt = 0;
 		private int trainCnt = 0;
 		public void setTripStatistics(TripStatistics tripStatistics) {
 			this.tripStatistics = tripStatistics;
 		}
 		public void setDataTypeCnt(int walkingCnt,int bikingCnt, int drivingCnt, int trainCnt){
 			this.walkingCnt = walkingCnt;
 			this.bikingCnt = bikingCnt;
 			this.drivingCnt = drivingCnt;
 			this.trainCnt = trainCnt;
 		}
 		public void setFile(File file) {
 			this.file = file;
 		}
 
 		@Override
 		public void run() {
 			try {
 				SharedPreferences settings = mContext.getSharedPreferences(
 						"account", Context.MODE_PRIVATE);
 				SharepreferenceHelper preferenceHelper = new SharepreferenceHelper(
 						settings);
 
 				ClientHelper clientHelper = new ClientHelper();
 				JSONObject ret = clientHelper.uploadFile(
 						preferenceHelper.getUserToken(), file);
 				assert (ret.has("trip_id"));
 				String tripId = ret.getString("trip_id");
 				Log.d(TAG, "trip_id:" + tripId);
 				Log.d(TAG, "max:" + tripStatistics.getMaxSpeed());
 				float totalCnt = walkingCnt + bikingCnt + drivingCnt + trainCnt;
 				clientHelper.sendCurrentTripToServer(
 						preferenceHelper.getUserToken(),
 						trackDataType.toString(), tripId,
 						tripStatistics.getAverageSpeed(),
 						tripStatistics.getMaxSpeed(),
 						tripStatistics.getTotalDistance(),
 						tripStatistics.getTotalTime(),
 						tripStatistics.getStartTime(),
 						tripStatistics.getStopTime(),
 						walkingCnt/totalCnt,
 						bikingCnt/totalCnt,
 						drivingCnt/totalCnt,
 						trainCnt/totalCnt);
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public File writeTrack2File(Track track) {
 		CsvTrackWriter writer = new CsvTrackWriter(mContext);
 		File file = new File(mContext.getExternalCacheDir(),
 				System.currentTimeMillis() + ".csv");
 		Log.d(TAG, file.getAbsolutePath());
 		OutputStream out;
 		try {
 			out = new FileOutputStream(file);
 			writer.prepare(track, out);
 			writer.writeHeader();
 			writer.writeBeginTrack();
 			writer.writeLocations();
 			writer.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return file;
 
 	}
 
 	public void startRecording() {
 		startNewTrack();
 		initialDataTypeCnt();
 		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
 				0, 0, this);
 		/*
 		 * mLocationManager.requestLocationUpdates(
 		 * LocationManager.NETWORK_PROVIDER, 0, 0, this);
 		 */
 		for (int i = 0; i < sensorTypes.length; i++) {
 			Sensor mSensor = mSensorManager.getDefaultSensor(sensorTypes[i]);
 			if (mSensor != null) {
 				mSensorManager.registerListener(this, mSensor,
 						SensorManager.SENSOR_DELAY_NORMAL);
 			}
 
 		}
 		dataWindow = new DataWindow(timeWindow);
 		isRecording = true;
 	}
 
 	public void stopRecording() {
 		endCurrentTrack();
 		initialDataTypeCnt();
 		mLocationManager.removeUpdates(this);
 		mSensorManager.unregisterListener(this);
 	}
 
 	/* Location parts */
 	@Override
 	public void onLocationChanged(Location location) {
 		onLocationChangedAsync(location);
 
 	}
 
 	public void updateActivityLevelMessage(long currentTime) {
 		if (isRecording) {
 			Intent intent = new Intent();
 			intent.setAction(TrafficLog.ACTION);
 			float activityLevel = dataWindow.getCurrentActivityLevel(currentTime);
 			// dataAnalyst.setAnotherAccelerData(dataWindow.getCurrentAccelerWindow(time));
 			Log.d("ActivityLevel", "ActivityLevel:" + activityLevel);
 			intent.putExtra("activityLevel", activityLevel);
 			mContext.sendBroadcast(intent);
 		}
 	}
 
 	public void updateDataTypeMessage(Location locationToInsert) {
 		if (isRecording) {
 
 			long time = locationToInsert.getTime();
 			Intent intent = new Intent();
 			intent.setAction(TrafficLog.ACTION);
 			dataWindow.addLocationToWindow(locationToInsert);
 			dataAnalyst.setAnotherTripData(dataWindow
 					.getCurrentLocationWindow(time));
			trackDataType = dataAnalyst.getAnalysisResult();
 			updateDataTypeSummary(trackDataType);
 			intent.putExtra("dataType", trackDataType);
 			mContext.sendBroadcast(intent);
 		}
 	}
 	private void updateDataTypeSummary(DataType dataType){
 		switch(dataType){
 		case WALKING:
 			walkingCnt++;
 			break;
 		case BIKING:
 			bikingCnt++;
 			break;
 		case TRAIN:
 			trainCnt++;
 			break;
 		case DRIVING:
 			drivingCnt++;
 			break;
 		case ERROR:
 			//it should be impossible to get here...
 			Log.e(TAG,"error DataType returned");
 			break;
 		}
 		float totalCnt = walkingCnt + bikingCnt + trainCnt + drivingCnt;
 		Log.d("Summary","total:"+totalCnt +" walk:"+walkingCnt + " bike:" + bikingCnt + " train:" + trainCnt + " drive:" + drivingCnt);
 		Log.d("Summary", "walk:"+walkingCnt/totalCnt+" bike:"+bikingCnt/totalCnt+" train:"+ trainCnt/totalCnt + " drive:" + drivingCnt/totalCnt);
 	}
 	private void initialDataTypeCnt(){
 		walkingCnt = 0;
 		bikingCnt = 0;
 		trainCnt = 0;
 		drivingCnt = 0;
 	}
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 	}
 
 	public void onProviderEnabled(String provider) {
 	}
 
 	public void onProviderDisabled(String provider) {
 	}
 
 	private void onLocationChangedAsync(Location location) {
 
 		try {
 			// Don't record if the service has been asked to pause recording:
 			if (!isRecording) {
 				Log.w(TAG, "Not recording because recording has been paused.");
 				return;
 			}
 
 			// This should never happen, but just in case (we really don't want
 			// the
 			// service to crash):
 			if (location == null) {
 				Log.w(TAG, "Location changed, but location is null.");
 				return;
 			}
 
 			// Don't record if the accuracy is too bad:
 			if (location.getAccuracy() > minRequiredAccuracy) {
 				Log.d(TAG, "Not recording. Bad accuracy.");
 				return;
 			}
 
 			// At least one track must be available for appending points:
 			recordingTrack = getRecordingTrack();
 			if (recordingTrack == null) {
 				Log.d(TAG, "Not recording. No track to append to available.");
 				return;
 			}
 
 			// Update the idle time if needed.
 			/*
 			 * locationListenerPolicy.updateIdleTime(statsBuilder.getIdleTime());
 			 * addLocationToStats(location); if (currentRecordingInterval !=
 			 * locationListenerPolicy.getDesiredPollingInterval()) {
 			 * registerLocationListener(); }
 			 */
 
 			Location lastRecordedLocation = dbHelper.getLastLocation();
 			double distanceToLastRecorded = Double.POSITIVE_INFINITY;
 			if (lastRecordedLocation != null) {
 				distanceToLastRecorded = location
 						.distanceTo(lastRecordedLocation);
 
 			}
 			double distanceToLast = Double.POSITIVE_INFINITY;
 			if (lastLocation != null) {
 				distanceToLast = location.distanceTo(lastLocation);
 
 			}
 			/*
 			 * boolean hasSensorData = sensorManager != null &&
 			 * sensorManager.isEnabled() && sensorManager.getSensorDataSet() !=
 			 * null && sensorManager.isSensorDataSetValid();
 			 */
 			// If the user has been stationary for two recording just record the
 			// first
 			// two and ignore the rest. This code will only have an effect if
 			// the
 			// maxRecordingDistance = 0
 			boolean hasSensorData = isMoving(mDeltaAccelerometer);
 			if (distanceToLast == 0 && !hasSensorData) {
 				if (isMoving) {
 					Log.d(TAG, "Found two identical locations.");
 					isMoving = false;
 					if (lastLocation != null && lastRecordedLocation != null
 							&& !lastRecordedLocation.equals(lastLocation)) {
 						// Need to write the last location. This will happen
 						// when
 						// lastRecordedLocation.distance(lastLocation) <
 						// minRecordingDistance
 						if (!insertLocation(lastLocation, lastRecordedLocation,
 								recordingTrackId)) {
 							return;
 						}
 					}
 				} else {
 					Log.d(TAG,
 							"Not recording. More than two identical locations.");
 				}
 			} else if (distanceToLastRecorded > minRecordingDistance
 					|| hasSensorData) {
 				if (lastLocation != null && !isMoving) {
 					// Last location was the last stationary location. Need to
 					// go back and
 					// add it.
 					if (!insertLocation(lastLocation, lastRecordedLocation,
 							recordingTrackId)) {
 						return;
 					}
 					isMoving = true;
 				}
 
 				// If separation from last recorded point is too large insert a
 				// separator to indicate end of a segment:
 				boolean startNewSegment = lastRecordedLocation != null
 						&& lastRecordedLocation.getLatitude() < 90
 						&& distanceToLastRecorded > maxRecordingDistance
 						&& recordingTrack.getStartId() >= 0;
 				if (startNewSegment) {
 					// Insert a separator point to indicate start of new track:
 					Log.d(TAG, "Inserting a separator.");
 					Location separator = new Location(
 							LocationManager.GPS_PROVIDER);
 					separator.setLongitude(0);
 					separator.setLatitude(100);
 					separator.setTime(lastRecordedLocation.getTime());
 					dbHelper.insertTrackPoint(separator, recordingTrackId);
 				}
 
 				if (!insertLocation(location, lastRecordedLocation,
 						recordingTrackId)) {
 					return;
 				}
 			} else {
 				Log.d(TAG,
 						String.format(
 								Locale.US,
 								"Not recording. Distance to last recorded point (%f m) is less than %d m.",
 								distanceToLastRecorded, minRecordingDistance));
 				// Return here so that the location is NOT recorded as the last
 				// location.
 				return;
 			}
 		} catch (Error e) {
 			// Probably important enough to rethrow.
 			Log.e(TAG, "Error in onLocationChanged", e);
 			throw e;
 		} catch (RuntimeException e) {
 			// Safe usually to trap exceptions.
 			Log.e(TAG, "Trapping exception in onLocationChanged", e);
 			throw e;
 		}
 		lastLocation = location;
 	}
 
 	/**
 	 * Inserts a new location in the track points db and updates the
 	 * corresponding track in the track db.
 	 * 
 	 * @param location
 	 *            the location to be inserted
 	 * @param lastRecordedLocation
 	 *            the last recorded location before this one (or null if none)
 	 * @param trackId
 	 *            the id of the track
 	 * @return true if successful. False if SQLite3 threw an exception.
 	 */
 	private boolean insertLocation(Location location,
 			Location lastRecordedLocation, long trackId) {
 
 		// Keep track of length along recorded track (needed when a waypoint is
 		// inserted):
 		if (LocationUtils.isValidLocation(location)) {
 			if (lastValidLocation != null) {
 				length += location.distanceTo(lastValidLocation);
 			}
 			lastValidLocation = location;
 		}
 
 		// Insert the new location:
 		try {
 			Location locationToInsert = location;
 			/*
 			 * if (sensorManager != null && sensorManager.isEnabled()) {
 			 * SensorDataSet sd = sensorManager.getSensorDataSet(); if (sd !=
 			 * null && sensorManager.isSensorDataSetValid()) { locationToInsert
 			 * = new MyTracksLocation(location, sd); } }
 			 */
 			long pointId = dbHelper.insertTrackPoint(locationToInsert, trackId);
 			Log.d(TAG, "rowId:" + pointId + " location:" + locationToInsert);
 
 			updateDataTypeMessage(locationToInsert);
 			updateActivityLevelMessage(locationToInsert.getTime());
 			// Update the current track:
 			if (lastRecordedLocation != null
 					&& lastRecordedLocation.getLatitude() < 90) {
 				/*
 				 * TripStatistics tripStatistics = statsBuilder.getStatistics();
 				 * tripStatistics.setStopTime(System.currentTimeMillis());
 				 */
 
 				if (recordingTrack.getStartId() < 0) {
 					recordingTrack.setStartId(pointId);
 				}
 				recordingTrack.setStopId(pointId);
 				recordingTrack.setNumberOfPoints(recordingTrack
 						.getNumberOfPoints() + 1);
 				// recordingTrack.setTripStatistics(tripStatistics);
 				dbHelper.updateTrack(recordingTrack);
 				// updateCurbentWaypoint();
 			}
 		} catch (SQLiteException e) {
 			// Insert failed, most likely because of SqlLite error code 5
 			// (SQLite_BUSY). This is expected to happen extremely rarely (if
 			// our
 			// listener gets invoked twice at about the same time).
 			Log.w(TAG, "Caught SQLiteException: " + e.getMessage(), e);
 			return false;
 		}
 		/*
 		 * announcementExecutor.update(); splitExecutor.update();
 		 */
 		return true;
 	}
 
 	private Track getRecordingTrack() {
 		if (recordingTrackId < 0) {
 			return null;
 		}
 
 		return dbHelper.getTrack(recordingTrackId);
 	}
 
 	private boolean isBetterLocation(Location location,
 			Location currentBestLocation) {
 		if (currentBestLocation == null) {
 			// A new location is always better than no location
 			return true;
 		}
 
 		// Check whether the new location fix is newer or older
 		long timeDelta = location.getTime() - currentBestLocation.getTime();
 		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
 		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
 		boolean isNewer = timeDelta > 0;
 
 		// If it's been more than two minutes since the current location, use
 		// the new location
 		// because the user has likely moved
 		if (isSignificantlyNewer) {
 			return true;
 			// If the new location is more than two minutes older, it must be
 			// worse
 		} else if (isSignificantlyOlder) {
 			return false;
 		}
 
 		// Check whether the new location fix is more or less accurate
 		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
 				.getAccuracy());
 		boolean isLessAccurate = accuracyDelta > 0;
 		boolean isMoreAccurate = accuracyDelta < 0;
 		boolean isSignificantlyLessAccurate = accuracyDelta > 200;
 
 		// Check if the old and new location are from the same provider
 		boolean isFromSameProvider = isSameProvider(location.getProvider(),
 				currentBestLocation.getProvider());
 
 		// Determine location quality using a combination of timeliness and
 		// accuracy
 		if (isMoreAccurate) {
 			return true;
 		} else if (isNewer && !isLessAccurate) {
 			return true;
 		} else if (isNewer && !isSignificantlyLessAccurate
 				&& isFromSameProvider) {
 			return true;
 		}
 		return false;
 	}
 
 	/** Checks whether two providers are the same */
 	private boolean isSameProvider(String provider1, String provider2) {
 		if (provider1 == null) {
 			return provider2 == null;
 		}
 		return provider1.equals(provider2);
 	}
 
 	/*
 	 * ====================================================================
 	 * Sensor part
 	 */
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		// TODO Auto-generated method stub
 		Log.d("SensorEventListener", "onSensorChanged called");
 		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
 			if (!isRecording) {
 				return;
 			}
 
 			Log.d("onSensorChanged", Arrays.toString(event.values));
 			float newDeltaAccelerometer = deltaAccelerometerReading(
 					previousEventValue, event.values);
 			previousEventValue = event.values.clone();
 			mDeltaAccelerometer = lowpassFilter(newDeltaAccelerometer,
 					mDeltaAccelerometer, 0.6f);
 			updateIsMovingMessage(isMoving(mDeltaAccelerometer));
 			dataWindow.addAccelerToWindow(event.values, new Date().getTime());
 
 		}
 
 	}
 
 	private boolean isMoving(Location prevLocation, Location currLocation) {
 		double timeInterval = (currLocation.getTime() - prevLocation.getTime())
 				* NS2S;
 		Log.d(TAG, "timeInterval:" + timeInterval);
 		double distance = prevLocation.distanceTo(currLocation);
 		double speed = distance / timeInterval;
 		Log.d(TAG, "distanceToLast:" + distance + " moving speed:" + speed);
 		Toast.makeText(mContext,
 				"distanceToLast:" + distance + " moving speed:" + speed,
 				Toast.LENGTH_SHORT).show();
 		if (speed <= 1)
 			return false;
 		return true;
 	}
 
 	private boolean isMoving(float deltaAccelerometer) {
 		Log.d(TAG, "" + deltaAccelerometer);
 		return (deltaAccelerometer > 1);
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 		// TODO Auto-generated method stub
 		Log.d("SensorEventListener", "onAccuracyChanged called");
 
 	}
 
 	public void updateIsMovingMessage(boolean isMoving) {
 		Intent intent = new Intent();
 		intent.setAction(TrafficLog.ACTION);
 		intent.putExtra("isMoving", isMoving);
 		mContext.sendBroadcast(intent);
 	}
 
 	private float lowpassFilter(float newValue, float oldValue, float alpha) {
 		return alpha * newValue + (1 - alpha) * oldValue;
 	}
 
 }

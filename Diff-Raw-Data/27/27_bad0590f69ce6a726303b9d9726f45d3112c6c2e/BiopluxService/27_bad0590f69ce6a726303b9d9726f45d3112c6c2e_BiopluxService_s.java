 package ceu.marten.services;
 
 
import plux.android.bioplux.Device.Frame;
import plux.android.processing.IEventHandler;
 import plux.android.processing.IRawDataHandler;
 import plux.android.processing.ProcessingDevice;
 import android.annotation.SuppressLint;
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.PowerManager;
import android.os.Handler.Callback;
 import android.os.PowerManager.WakeLock;
 import android.os.RemoteException;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import ceu.marten.bplux.R;
 import ceu.marten.model.DeviceConfiguration;
 import ceu.marten.model.io.DataManager;
 import ceu.marten.ui.NewRecordingActivity;
 import ceu.marten.ui.SettingsActivity;
 
 /**
  * Creates a connection with a bioplux device and receives frames sent from
  * device
  * 
  * @author Carlos Marten
  * 
  */
 public class BiopluxService extends Service implements IRawDataHandler {
 
 	private static final String TAG = BiopluxService.class.getName();
 
 	// messages 'what' fields for the communication with the client
 	public static final int MSG_REGISTER_CLIENT = 1;
 	public static final int MSG_DATA = 2;
 	public static final int MSG_RECORDING_DURATION = 3;
 	public static final int MSG_SAVED = 4;
 	public static final int MSG_CONNECTION_ERROR = 5;
 
 	public static final String KEY_X_VALUE = "xValue";
 	public static final String KEY_FRAME_DATA = "frame";
 
 	// Codes for the activity to display the correct error message
 	public static final int CODE_ERROR_WRITING_TEXT_FILE = 6;
 	public static final int CODE_ERROR_SAVING_RECORDING = 7;
 
 	// Used to synchronize timer and main thread
 	private static final Object weAreWritingDataToFileLock = new Object();
 	private boolean areWeWritingDataToFile;
 	// Used to keep activity running while device screen is turned off
 	private PowerManager powerManager;
 	private WakeLock wakeLock = null;
 
 	private DeviceConfiguration configuration;
 	private ProcessingDevice connection;
 
 
 	private DataManager dataManager;
 	private double samplingFrames;
 	private double samplingCounter = 0;
 	private double timeCounter = 0;
 	private double xValue = 0;
 	private boolean drawInBackground = true;
 	private boolean killServiceError = false;
 	private boolean clientActive = false;
 	Notification serviceNotification = null;
 	private SharedPreferences sharedPref;
 	
 	private final String SENSOR_NAME = "Bioplux";
 
 	//Target we publish for clients to send messages to IncomingHandler
 	private final Messenger mMessenger = new Messenger(new IncomingHandler());
 
 	//Messenger with interface for sending messages from the service
 	private Messenger client = null;
 
 	private boolean recivingData= false;
 
 	/**
 	 * Handler of incoming messages from clients.
 	 */
 	@SuppressLint("HandlerLeak")
 	class IncomingHandler extends Handler{
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_REGISTER_CLIENT:
 				// register client
 				client = msg.replyTo;
 				clientActive = true;
 				// removes notification
 				stopForeground(true);
 				break;
 			case MSG_RECORDING_DURATION:
 				dataManager.setDuration(msg.getData().getString(NewRecordingActivity.KEY_DURATION));
 				break;
 			default:
 				super.handleMessage(msg);
 			}
 		}
 
 	}
 	
 	@Override
 	public void onRowData(String sensor, int[] values) {
		// Log.e(TAG, sensor +" 1 "+ values.length+" " + values.length);
		// Log.e(TAG, "mascara "+connection.getMask());
		// Log.e(TAG, "mascara "+connection.getBits());
		// Log.e(TAG, "mascara "+connection.getFrequency());
 		if (recivingData) {
 			synchronized (weAreWritingDataToFileLock) {
 				areWeWritingDataToFile = true;
 			}
 
 			if (!dataManager.writeArrayToTmpFile(values)) {
 				sendErrorToActivity(CODE_ERROR_WRITING_TEXT_FILE);
 				killServiceError = true;
 				stopSelf();
 				synchronized (weAreWritingDataToFileLock) {
 					areWeWritingDataToFile = false;
 				}
 				return;
 			}
			// ************************
 			if (samplingCounter++ >= samplingFrames) {
 				// calculates x value of graphs
 				timeCounter++;
 				xValue = timeCounter / configuration.getSamplingFrequency()
 						* 1000;
 				// gets default share preferences with multi-process flag
 
 				if (clientActive || !clientActive && drawInBackground)
 					sendArrayToActivity(values);
 				// retains the decimals
 				samplingCounter -= samplingFrames;
 			}
			// ************************

 			synchronized (weAreWritingDataToFileLock) {
 				areWeWritingDataToFile = false;
 			}
 		}
 	}
 
 	@Override
 	public void onRowData(String sensor, int[] values, int counter) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onRowData(String sensor, int[][] values) {
 		// TODO Auto-generated method stub
 		
 	}
 	/**
 	 * Connects to a bioplux device and begins to acquire frames Returns true
 	 * connection has established. False if an exception was caught
 	 */
 	private boolean connectToBiopluxDevice() {
 
 		Log.e(TAG, "connectToBiopluxDevice "+configuration.getMacAddress());
 		// BIOPLUX INITIALIZATION
 		try { 
 			connection = new ProcessingDevice(configuration.getMacAddress());
 			connection.createSensor(SENSOR_NAME, configuration.getActiveChannelsAsIntArray());
 			connection.subscribeRawData(SENSOR_NAME, configuration.getVisualizationFrequency(),
 					configuration.getNumberOfBits(), this);
 		} catch (Exception e) {
 			try {
 				connection.EndAcq();
 				connection.Close();
 			} catch (Exception e1) {
 				Log.e(TAG, "bioplux close connection exception", e1);
 				sendErrorToActivity(1);//todo cdigo de error
 				killServiceError = true;
 				stopSelf();
 				return false;
 			}
 			Log.e(TAG, "Bioplux connection exception", e);
 			sendErrorToActivity(1);//todo cdigo de error
 			killServiceError = true;
 			stopSelf();
 			return false;
 		}
 		return true;
 	}
 	
 
 	/**
 	 * Initializes the wake lock
 	 */
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		sharedPref = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
 		drawInBackground = sharedPref.getBoolean(SettingsActivity.KEY_DRAW_IN_BACKGROUND, true);
 		powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
 		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyWakeLock");
 		if ((wakeLock != null) && (wakeLock.isHeld() == false)) {
 			wakeLock.acquire();
 		}
 	}
 
 	/**
 	 * Returns the communication channel to the service or null if clients
 	 * cannot bind to the service
 	 */
 	@Override
 	public IBinder onBind(Intent intent) {	
 		Log.i(TAG, "onBind");
 		return mMessenger.getBinder();
 	}
 
 	/**
 	 * Changes the service to be run in the foreground and shows the
 	 * notification
 	 */
 	@Override
 	public boolean onUnbind(Intent intent) {
 		Log.i(TAG, "onUNBind");
 		clientActive = false;
 		startForeground(R.string.service_id, serviceNotification);
 		return true;
 	}
 
 	/**
 	 * Gets information from the activity extracted from the intent and connects
 	 * to bioplux device. Returns a do not re-create flag if killed by system
 	 */
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		String recordingName = intent.getStringExtra (NewRecordingActivity.KEY_RECORDING_NAME).toString();
 		configuration = (DeviceConfiguration) intent.getSerializableExtra(NewRecordingActivity.KEY_CONFIGURATION);
 		samplingFrames = (double) configuration.getVisualizationFrequency() / configuration.getSamplingFrequency();
 		//borradas varias lneas
 
 		if (connectToBiopluxDevice()) {
 			dataManager = new DataManager(this, recordingName, configuration);
 
 			try {
 				// create a buffer with 1 frame
 				connection.setNFrames(10);
 				connection.BeginAcq();
 				recivingData= true;
 				Log.i(TAG, "empezado");
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			createNotification();
 		}
 		return START_NOT_STICKY; // do not re-create service if system kills it
 	}
 
 
 	private void createNotification() {
 
 		// SET THE BASICS
 		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
 				this).setSmallIcon(R.drawable.notification)
 				.setContentTitle(getString(R.string.bs_notification_title))
 				.setContentText(getString(R.string.bs_notification_message));
 
 		// CREATE THE INTENT CALLED WHEN NOTIFICATION IS PRESSED
 		Intent newRecordingIntent = new Intent(this, NewRecordingActivity.class);
 
 		// PENDING INTENT
 		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
 				newRecordingIntent, PendingIntent.FLAG_CANCEL_CURRENT);
 		mBuilder.setContentIntent(pendingIntent);
 
 		// CREATES THE NOTIFICATION AND START SERVICE AS FOREGROUND
 		serviceNotification = mBuilder.build();
 	}
 
 	/**
 	 * Sends data to the activity via message
 	 * 
 	 * @param values
 	 *            acquired from the bioplux device
 	 */
 	//comentario intentar optimizar el envo de datos?
 	private void sendArrayToActivity(int[] values) {
 		Bundle b = new Bundle();
 		b.putDouble(KEY_X_VALUE, xValue);
 		b.putIntArray(KEY_FRAME_DATA, values);
 		Message message = Message.obtain(null, MSG_DATA);
 		message.setData(b);
 		try {
 			client.send(message);
 		} catch (RemoteException e) {
 			clientActive = false;
 			Log.i(TAG, "client is dead");
 		}
 	}
 
 	/**
 	 * Notifies the client that the recording frames were stored properly
 	 */
 	private void sendSavedNotification() {
 		Message message = Message.obtain(null, MSG_SAVED);
 		try {
 			client.send(message);
 		} catch (RemoteException e) {
 			Log.e(TAG, "client is dead. Service is being stopped", e);
 			killServiceError = true;
 			stopSelf();
 		}
 	}
 
 	/**
 	 * Sends the an error code to the client with the corresponding error that
 	 * it has encountered
 	 */
 	private void sendErrorToActivity(int errorCode) {
 		try {
 			client.send(Message.obtain(null, MSG_CONNECTION_ERROR, errorCode, 0));
 		} catch (RemoteException e) {
 			Log.e(TAG, "Exception sending error message to activity. Service is stopping", e);
 		}
 	}
 
 	@SuppressLint("NewApi")
 	@Override
 	public void onTaskRemoved(Intent rootIntent) {
 		killServiceError = true;
 		stopSelf();
 		super.onTaskRemoved(rootIntent);
 	}
 
 	/**
 	 * Stops the service properly whilst being destroyed
 	 */
 	private void stopService() {
 
 		while (areWeWritingDataToFile) {
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e2) {
 				Log.e(TAG, "Exception thread is sleeping", e2);
 			}
 		}
 		recivingData=false;
 		if (!dataManager.closeWriters())
 			sendErrorToActivity(CODE_ERROR_SAVING_RECORDING);
 		try {
 			connection.EndAcq();
 			Log.e(TAG, "Cerrando conexiones");
 		} catch (Exception e) {
 			Log.e(TAG, "Exception ending ACQ", e);
 			sendErrorToActivity(1);//todo error
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		if (!killServiceError) {
 			stopService();
 			new Thread() {
 				@Override
 				public void run() {
 					boolean errorSavingRecording = false;
 					if (!dataManager.saveAndCompressFile(client)) {
 						errorSavingRecording = true;
 						sendErrorToActivity(CODE_ERROR_SAVING_RECORDING);
 					}
 					if (!errorSavingRecording)
 						sendSavedNotification();
 					wakeLock.release();
 				}
 			}.start();
 		}
 		Log.i(TAG, "service destroyed");
 	}
 }

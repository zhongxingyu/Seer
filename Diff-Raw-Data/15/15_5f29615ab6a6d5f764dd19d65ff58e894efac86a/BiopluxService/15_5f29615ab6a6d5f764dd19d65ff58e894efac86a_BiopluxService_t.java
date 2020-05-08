 package ceu.marten.services;
 
 
 import java.io.File;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import plux.android.bioplux.BPException;
 import plux.android.bioplux.Device;
 import plux.android.bioplux.Device.Frame;
 import android.annotation.SuppressLint;
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.os.RemoteException;
 import android.os.StatFs;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import ceu.marten.bplux.R;
 import ceu.marten.model.DeviceConfiguration;
 import ceu.marten.model.io.DataManager;
 import ceu.marten.ui.NewRecordingActivity;
 
 /**
  * Creates a connection with a bioplux device and receives frames sent from device
  * @author Carlos Marten
  *
  */
 public class BiopluxService extends Service {
 
 	// Standard debug constant
 	private static final String TAG = BiopluxService.class.getName();
 
 	// messages 'what' fields for the communication with client
 	public static final int MSG_REGISTER_AND_START = 1;
 	public static final int MSG_DATA = 2;
 	public static final int MSG_RECORDING_DURATION = 3;
 	public static final int MSG_SAVED = 4;
 	public static final int MSG_CONNECTION_ERROR = 5;
 	
 	// Codes for the activity to display the correct error message
 	public static final int CODE_ERROR_PROCESSING_FRAMES = 6;
 	public static final int CODE_ERROR_SAVING_RECORDING = 7;
 	
 	// Get 80 frames every 50 miliseconds
 	public static final int NUMBER_OF_FRAMES = 80; 
 	public static final long TIMER_TIME = 50L;
 	
 	// Used to synchronize timer and main thread
 	private static final Object writingLock = new Object();
 	private boolean isWriting;
 	
 	// Used to keep activity running while device screen is turned off
 	private PowerManager powerManager;
	private WakeLock wakeLock = null;
 		
 	private DeviceConfiguration configuration;
 	private Device connection;
 	private Device.Frame[] frames;
 	
 	private Timer timer = new Timer();
 	private DataManager dataManager;
 	private String recordingName;
 	private double samplingFrames;
 	private double samplingCounter = 0;
 	private boolean killServiceError = false;
 
 	/**
 	 * Target we publish for clients to send messages to IncomingHandler
 	 */
 	private final Messenger mMessenger = new Messenger(new IncomingHandler());
 	
 	/**
 	 * Messenger with interface for sending messages from the service
 	 */
 	private Messenger client = null;
 	
 	/**
      * Handler of incoming messages from clients.
      */
 	@SuppressLint("HandlerLeak")
 	class IncomingHandler extends Handler {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_REGISTER_AND_START:
 				// register client
 				client = msg.replyTo;
				if ((wakeLock != null) && (wakeLock.isHeld() == false)) {
					wakeLock.acquire();
				}
 				timer.schedule(new TimerTask() {
 					public void run() {
 						processFrames();
 					}
 				}, 0, TIMER_TIME);
 				break;
 			case MSG_RECORDING_DURATION:
 				dataManager.setDuration(msg.getData().getString("duration")); //TODO HARD CODED
 				break;
 			default:
 				super.handleMessage(msg);
 			}
 		}
 	}
 
 	/**
 	 * Initializes the wake lock and the frames array
 	 */
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
 		powerManager = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
 		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
 		frames = new Device.Frame[NUMBER_OF_FRAMES];
 		for (int i = 0; i < frames.length; i++)
 			frames[i] = new Frame();
 		
 		Log.i(TAG, "Service created");
 	}
 
 	/**
 	 * Gets information from the activity extracted from the intent and connects
 	 * to bioplux device. Returns the communication channel to the service or
 	 * null if clients cannot bind to the service
 	 */
 	@Override
 	public IBinder onBind(Intent intent) {
 		Log.i(TAG, "service onbind()");
 		recordingName = intent.getStringExtra("recordingName").toString(); //TODO HARD CODED
 		configuration = (DeviceConfiguration) intent.getSerializableExtra("configSelected");//TODO HARD CODED
 		samplingFrames = (double)configuration.getReceptionFrequency() / configuration.getSamplingFrequency();
 		
 		if(connectToBiopluxDevice()){
 			dataManager = new DataManager(this, recordingName, configuration);
 			showNotification(intent);
 		}
 		return mMessenger.getBinder();
 	}
 	
 	/**
 	 * Gets and process the frames from the bioplux device. Saves all the frames
 	 * receives to a text file and send the requested frames to the activity
 	 */
 	private void processFrames() {
 		
 		synchronized (writingLock) {
 			isWriting = true;
 		}
 		
 		getFrames(NUMBER_OF_FRAMES);
 		
 		loop:
 		for (Frame frame : frames) {
 			if(!dataManager.writeFramesToTmpFile(frame)){
 				sendErrorToActivity(CODE_ERROR_PROCESSING_FRAMES);
 				killServiceError = true;
 				stopSelf();
 				break loop;
 			}
 			
 			if(samplingCounter++ >= samplingFrames){
 				sendFrameToActivity(frame.an_in);
 				samplingCounter -= samplingFrames;
 			}	
 		}
 		synchronized (writingLock) {
 			isWriting = false;
 		}
 	}
 
 	/**
 	 * Get frames from the bioplux device
 	 * @param numberOfFrames
 	 */
 	private void getFrames(int numberOfFrames) {
 		try {
 			connection.GetFrames(numberOfFrames, frames);
 		} catch (BPException e) {
 			Log.e(TAG, "Exception getting frames", e);
 			sendErrorToActivity(e.code);
 			stopSelf();
 		}
 	}
 
 
 	/**
 	 * Connects to a bioplux device and begins to acquire frames
 	 * Returns true connection has established. False if an exception was caught
 	 * @return boolean
 	 */
 	private boolean connectToBiopluxDevice() {
 		// BIOPLUX INITIALIZATION
 		try {
 			connection = Device.Create(configuration.getMacAddress());
 			connection.BeginAcq(configuration.getReceptionFrequency(), configuration.getActiveChannelsAsInteger(), configuration.getNumberOfBits());
 		} catch (BPException e) {
 			try {
 				connection.Close();
 			} catch (BPException e1) {
 				Log.e(TAG, "bioplux close connection exception", e1);
 				sendErrorToActivity(e1.code);
 				killServiceError = true;
 				stopSelf();
 				return false;
 			}
 			Log.e(TAG, "Bioplux connection exception", e);
 			sendErrorToActivity(e.code);
 			killServiceError = true;
 			stopSelf();
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Creates the notification and starts service in the foreground
 	 * @param parentIntent
 	 */
 	private void showNotification(Intent parentIntent) {
 
 		// SET THE BASICS
 		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
 				this).setSmallIcon(R.drawable.notification)
 				.setContentTitle(getString(R.string.bs_notification_title))
 				.setContentText(getString(R.string.bs_notification_message));
 
 		// CREATE THE INTENT CALLED WHEN NOTIFICATION IS PRESSED
 		Intent newRecordingIntent = new Intent(this, NewRecordingActivity.class);
 
 		// PENDING INTENT
 		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
 				newRecordingIntent, Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
 		mBuilder.setContentIntent(pendingIntent);
 
 		// CREATES THE NOTIFICATION AND START SERVICE AS FOREGROUND
 		Notification serviceNotification = mBuilder.build();
 		startForeground(R.string.service_id, serviceNotification);
 	}
 
 	/**
 	 * Sends frame to activity via message
 	 * @param frame acquired from the bioplux device
 	 */
 	private void sendFrameToActivity(short[] frame) {
 		Bundle b = new Bundle();
         b.putShortArray("frame", frame);//TODO HARD CODED
         Message message = Message.obtain(null, MSG_DATA);
         message.setData(b);
 		try {
 			client.send(message);
 		} catch (RemoteException e) {
 			Log.e(TAG, "client is dead. Service is being stopped", e);
 			killServiceError = true;
 			stopSelf();
 			client = null;
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
 			client = null;
 		}
 	}
 
 	/**
 	 * Sends the an error code to the client with the corresponding error that
 	 * it has encountered
 	 * 
 	 * @param errorCode
 	 */
 	private void sendErrorToActivity(int errorCode) {
 		try {
 			client.send(Message.obtain(null, MSG_CONNECTION_ERROR, errorCode, 0));
 		} catch (RemoteException e) {
 			Log.e(TAG, "Exception sending error message to activity. Service is stopping", e);
 		}
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		return START_NOT_STICKY; // do not re-create service if system kills it
 	}
 	
 	/**
 	 * Stops the service properly when service is being destroyed
 	 */
 	private void stopService(){
 		if (timer != null)
 			timer.cancel();
 
 		while (isWriting) {
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e2) {
 				Log.e(TAG, "Exception thread is sleeping", e2);
 			}
 		}
 		if(!dataManager.closeWriters())
 			sendErrorToActivity(CODE_ERROR_SAVING_RECORDING);
 		try {
 			connection.EndAcq();
 			connection.Close();
 		} catch (BPException e) {
 			Log.e(TAG, "Exception ending ACQ", e);
 			sendErrorToActivity(e.code);
 		}
 	}
 	
 
 	@Override
 	public boolean onUnbind(Intent intent) {
 		// returns true so that next time the client binds, onRebind() will be called
 		return true;
 	}
 
 	@Override
 	public void onRebind(Intent intent) {
 		// Override and do nothing. Needed for the notification to be dismissed when the service stops	
 	}
 
 	@Override
 	public void onDestroy() {
 		stopForeground(true);
 		if(!killServiceError){
 			stopService();
 			if(!dataManager.saveAndCompressFile(client))
 				sendErrorToActivity(CODE_ERROR_SAVING_RECORDING);
 			sendSavedNotification();
 		}
 		wakeLock.release();
 		super.onDestroy();
 		Log.i(TAG, "service destroyed");
 	}
 	
 	/*********************** LOW MEMORY? ********************/
 	
 	public static boolean externalMemoryAvailable() {
         return android.os.Environment.getExternalStorageState().equals(
                 android.os.Environment.MEDIA_MOUNTED);
     }
 	
 	public static String formatSize(long size) {
         String suffix = null;
 
         if (size >= 1024) {
             suffix = "KB";
             size /= 1024;
             if (size >= 1024) {
                 suffix = "MB";
                 size /= 1024;
             }
         }
 
         StringBuilder resultBuffer = new StringBuilder(Long.toString(size));
 
         int commaOffset = resultBuffer.length() - 3;
         while (commaOffset > 0) {
             resultBuffer.insert(commaOffset, ',');
             commaOffset -= 3;
         }
 
         if (suffix != null) resultBuffer.append(suffix);
         return resultBuffer.toString();
     }
 	
 	 public static String getAvailableInternalMemorySize() {
 	        File path = Environment.getDataDirectory();
 	        StatFs stat = new StatFs(path.getPath());
 	        @SuppressWarnings("deprecation")
 			long blockSize = stat.getBlockSize();
 	        @SuppressWarnings("deprecation")
 			long availableBlocks = stat.getAvailableBlocks();
 	        return formatSize(availableBlocks * blockSize);
 	 }
 	 
 	 public static String getAvailableExternalMemorySize() {
 	        if (externalMemoryAvailable()) {
 	            File path = Environment.getExternalStorageDirectory();
 	            StatFs stat = new StatFs(path.getPath());
 	            @SuppressWarnings("deprecation")
 				long blockSize = stat.getBlockSize();
 	            @SuppressWarnings("deprecation")
 				long availableBlocks = stat.getAvailableBlocks();
 	            return formatSize(availableBlocks * blockSize);
 	        } else {
 	            return "ERROR";
 	        }
 	    }
 }

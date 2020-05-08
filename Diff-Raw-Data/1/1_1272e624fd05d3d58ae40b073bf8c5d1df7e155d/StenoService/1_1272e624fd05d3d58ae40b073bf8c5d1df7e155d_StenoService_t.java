 package com.willwhitney.steno;
 import java.util.ArrayList;
 import java.util.List;
 
 import cc.gtank.bt.Bluetooth;
 import cc.gtank.bt.Honeycomb;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.speech.RecognitionListener;
 import android.speech.RecognizerIntent;
 import android.speech.SpeechRecognizer;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.widget.Toast;
 
 
 public class StenoService extends Service {
 
     private Handler mHandler;
 	private NotificationManager notificationManager;
 
 	private PowerManager powerManager;
 	private WakeLock wakeLock;
 
 	private SpeechRecognizer recognizer;
 	private RecognitionListener listener;
 	private LocationManager locationManager;
 	private TranscriptCache transcriptCache = new TranscriptCache(null);
 
 	public static StenoService instance;
 	public static boolean listening = false;
 //	public static LocalBroadcastManager utteranceBroadcastManager = new LocalBroadcastManager();
 
 
 	//	private TextToSpeech tts;
 
     // Unique Identification Number for the Notification.
     // We use it on Notification start, and to cancel it.
     private int NOTIFICATION = 0;
     	
 	private Bluetooth bluetooth;
 	private boolean bluetoothConnected;
 	
     private BroadcastReceiver btReceiver = new BroadcastReceiver() {
 		public void onReceive(Context context, Intent intent) {
 			Log.d("Steno", "Intent from BluetoothState: " + intent);
 			if(intent.getAction().equals(Bluetooth.BLUETOOTH_STATE)) {
 				if (intent.hasExtra("bluetooth_connected")) {
 					bluetoothConnected = intent.getExtras().getBoolean("bluetooth_connected");
 					if(bluetoothConnected) {
 						listen();
 					}
 				} 
 			}
 		}
     };
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
     	Log.d("Steno", "Intent from onStartCommand: " + intent);
     	if (intent != null) {
     		if (intent.hasExtra("terminate")) {
         		stopSelf();
         		return START_NOT_STICKY;
         	} else if (intent.hasExtra("username")) {
         		transcriptCache = new TranscriptCache(intent.getStringExtra("username"));
         	}
     	} else {
             return START_NOT_STICKY;
         }
 
     	instance = this;
         Log.i("Steno", "Received start id " + startId + ": " + intent);
         Log.d("Steno", "Service received start command.");
 
 //        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
 //
 //			@Override
 //				public void onInit(int status) {
 //			}
 //
 //        });
 //        tts.setOnUtteranceCompletedListener(this);
         locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
         powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
         wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StenoLock");
         wakeLock.acquire(24 * 60 * 60 * 1000);
 
         mHandler = new Handler();
         Thread repeatedUploadThread = new Thread(new DelayedTranscriptUploader(true));
         repeatedUploadThread.start();
 
 //        mHandler.postDelayed(transcriptUploader, 60 * 60 * 1000);
 
         bluetooth = new Honeycomb();
         bluetooth.setContext(this);
         if(bluetooth.obtainProxy()) {
         	this.registerReceiver(btReceiver, new IntentFilter(Bluetooth.BLUETOOTH_STATE));
         	Log.d("Steno", "Got bluetooth proxy");
         } else {
         	Log.d("Steno", "Unable to obtain bluetooth proxy");
         }
 
         listen();
         return START_STICKY;
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onCreate() {
     	listening = true;
     	Intent utteranceBroadcastIntent = new Intent();
     	utteranceBroadcastIntent.setAction(StenoStarter.SERVICE_CREATED_KEY);
     	LocalBroadcastManager.getInstance(this).sendBroadcast(utteranceBroadcastIntent);
 
     	// Display a notification while Steno is awake.
         notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
         startForeground(NOTIFICATION, buildNotification("No phrases received yet."));
     }
 
 //    public void speak(String words) {
 //    	HashMap<String, String> speechParams = new HashMap<String, String>();
 //    	speechParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "WORDS");
 //    	tts.speak(words, TextToSpeech.QUEUE_FLUSH, speechParams);
 //    }
 
     public void handleSpeech(List<String> matches) {
     	if (matches == null) {
     		return;
     	} else if (matches.size() == 0) {
     		return;
     	}
     	Log.d("Steno", "Best match: " + matches.get(0));
     	transcriptCache.addBlob(buildTranscriptBlob(matches));
 
     	Intent utteranceBroadcastIntent = new Intent();
     	utteranceBroadcastIntent.setAction(StenoStarter.NEW_UTTERANCE_KEY);
     	utteranceBroadcastIntent.putExtra(StenoStarter.NEW_UTTERANCE_KEY, matches.get(0));
     	LocalBroadcastManager.getInstance(this).sendBroadcast(utteranceBroadcastIntent);
 
     	showNotification(matches.get(0));
 
 //    	Log.d("Steno", transcriptCache.toString());
     }
 
     public TranscriptBlob buildTranscriptBlob(List<String> interpretations) {
     	Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
     	long unixTime = System.currentTimeMillis() / 1000L;
     	return new TranscriptBlob(interpretations, unixTime, lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
     }
 
 //    @Override
 //	public void onUtteranceCompleted(String utteranceId) {
 //    	Log.d("Steno", "Some utterance was completed with id " + utteranceId);
 //    	Log.d("Steno", "I am in state " + state);
 //    	Log.d("Steno", "Running startService...");
 //
 //    	Intent utteranceCompletedIntent = new Intent(this, StenoService.class);
 //    	utteranceCompletedIntent.putExtra("utterance_completed", true);
 //        startService(utteranceCompletedIntent);
 //	}
 //
 //    public void utteranceCompletedThreadsafe() {
 //    	Log.d("Steno", "Received startService in utteranceCompletedThreadsafe");
 //    	switch (state) {
 //			case NONE:
 //				break;
 //			case AWAITING_NOTE:
 //				listen();
 //				break;
 //    	}
 //    }
 
     public void listen() {
     	
     	Log.d("Steno", "Listen called, BT available: " + bluetooth.isAvailable() + " bluetoothConnected: " + bluetoothConnected);
     	
     	if(bluetooth.isAvailable() && !bluetoothConnected) {
     		Log.d("Steno", "Found headset, attempting to start");
     		bluetooth.startVoiceRecognition();
     		return;
     	}
 
     	Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
     	intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
     	intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.willwhitney.Steno");
     	if (recognizer == null) {
     		recognizer = SpeechRecognizer.createSpeechRecognizer(this);
     	}
     	
     	listener = new RecognitionListener() {
 
     		@Override
     	    public void onResults(Bundle results) {
     	        ArrayList<String> voiceResults = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
     	        if (voiceResults == null) {
     	            Log.e("Steno", "No voice results");
     	        } else {
 //    	            Log.d("Steno", "Printing matches: ");
 //    	            for (String match : voiceResults) {
 //    	                Log.d("Steno", match);
 //    	            }
     	        }
     	        StenoService.this.handleSpeech(voiceResults);
     	        StenoService.this.listen();
     	    }
 
     	    @Override
     	    public void onReadyForSpeech(Bundle params) {
     	        Log.d("Steno", "Ready for speech");
     	    }
 
     	    @Override
     	    public void onError(int error) {
     	        Log.e("Steno", "Error listening for speech: " + error);
     	        mHandler.removeCallbacks(recognitionStopper);
     	        StenoService.this.listen();
     	    }
 
     	    @Override
     	    public void onBeginningOfSpeech() {
     	        Log.d("Steno", "Speech starting");
     	        mHandler.removeCallbacks(recognitionStopper);
     	    }
 
 			@Override
 			public void onBufferReceived(byte[] buffer) {
 				Log.d("Steno", "Speech buffer received: " + buffer);
 
 			}
 
 			@Override
 			public void onEndOfSpeech() {
 				Log.d("Steno", "Speech ended.");
 			}
 
 			@Override
 			public void onEvent(int eventType, Bundle params) {
 				Log.d("Steno", "Speech event received.");
 
 			}
 
 			@Override
 			public void onPartialResults(Bundle partialResults) {
 				Log.d("Steno", "Speech partial results received.");
 
 			}
 
 			@Override
 			public void onRmsChanged(float rmsdB) {
 				// TODO Auto-generated method stub
 
 			}
     	};
 
     	try {
 	    	mHandler.postDelayed(recognitionStopper, 3000);
 	    	recognizer.setRecognitionListener(listener);
 	    	recognizer.startListening(intent);
     	} catch (Exception e) {
     		Log.e("Steno", e.getStackTrace().toString());
     		recognizer.stopListening();
     		mHandler.removeCallbacks(recognitionStopper);
     	}
     }
 
     @Override
     public void onDestroy() {
     	Intent utteranceBroadcastIntent = new Intent();
     	utteranceBroadcastIntent.setAction(StenoStarter.SERVICE_TERMINATED_KEY);
     	LocalBroadcastManager.getInstance(this).sendBroadcast(utteranceBroadcastIntent);
 
         // Cancel the persistent notification.
     	stopForeground(true);
         notificationManager.cancel(NOTIFICATION);
 
         listener = null;
         recognizer.cancel();
         recognizer.destroy();
        recognizer = null;
         
         if(bluetooth.isAvailable()) {
         	bluetooth.stopVoiceRecognition();
         }
         
         try {
         	bluetooth.releaseProxy();
         } catch (Exception e) {
         	e.printStackTrace();
         }
         this.unregisterReceiver(btReceiver);
 
         wakeLock.release();
 
         mHandler.removeCallbacks(recognitionStopper);
         Thread uploadThread = new Thread(new TranscriptUploader());
         uploadThread.start();
 //        mHandler.post(new TranscriptUploader());
 
         listening = false;
 
         // Tell the user we stopped.
         Toast.makeText(this, "Steno service stopped.", Toast.LENGTH_SHORT).show();
         super.onDestroy();
     }
 
     /**
      * Show a notification while this service is running.
      */
     private void showNotification(String mostRecentUtterance) {
         notificationManager.notify(NOTIFICATION, buildNotification(mostRecentUtterance));
     }
 
     private Notification buildNotification(String mostRecentUtterance) {
     	Intent showIntent = new Intent(StenoService.this, StenoStarter.class);
     	PendingIntent pendingShowIntent = PendingIntent.getActivity(this, 0, showIntent, 0);
 
     	Intent terminateIntent = new Intent(this, StenoService.class);
         terminateIntent.putExtra("terminate", true);
         PendingIntent pendingTerminateIntent = PendingIntent.getService(this, 0, terminateIntent, 0);
 
         Notification notification = new NotificationCompat.Builder(this)
         		.setSmallIcon(R.drawable.ic_launcher)
         		.setContentTitle("Steno")
         		.setContentText("Running...")
         		.setContentIntent(pendingShowIntent)
         		.setStyle(new NotificationCompat.BigTextStyle().bigText(mostRecentUtterance))
         		.addAction(R.drawable.content_remove, "Stop listening", pendingTerminateIntent)
         		.setOngoing(true)
         		.build();
         return notification;
     }
 
     Runnable recognitionStopper = new Runnable() {
     	@Override
     	public void run() {
     		Log.d("Steno", "KILL ALL THE LISTENING");
     		recognizer.stopListening();
     	}
     };
 
     private class TranscriptUploader implements Runnable {
     	public static final int REPEAT_DELAY_SECONDS = 60 * 60;
     	
     	public boolean repeats;
     	
     	private StenoApiClient apiClient;
     	
     	public TranscriptUploader() {
     		this(false);
     	}
 
     	public TranscriptUploader(boolean repeats) {
     		this.repeats = repeats;
     		this.apiClient = new StenoApiClient();
     	}
 
 		@Override
 		public void run() {
 			String json = transcriptCache.dumpJson();
 			attemptUpload(json);
 			if (repeats) {
 				mHandler.postDelayed(this, REPEAT_DELAY_SECONDS * 1000);
 			}
 		}
 
 		private void attemptUpload(String json) {
 			Log.d("Steno", "Uploading transcripts.");
 			apiClient.uploadTranscripts(json);
 		}
 
     }
 
     private class DelayedTranscriptUploader extends TranscriptUploader implements Runnable {
     	public DelayedTranscriptUploader(boolean repeats) {
     		super(repeats);
     	}
 
     	@Override
     	public void run() {
     		mHandler.postDelayed(new TranscriptUploader(repeats), REPEAT_DELAY_SECONDS * 1000);
     	}
     }
 
 
 }

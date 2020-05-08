 package bzb.android.logger;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.Dialog;
 import android.app.PendingIntent;
 import android.app.TimePickerDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.location.LocationManager;
 import android.media.MediaRecorder;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.PowerManager;
 import android.os.StatFs;
 import android.os.PowerManager.WakeLock;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.SurfaceHolder.Callback;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.TimePicker;
 
 /**
  * @author bzb
  *
  */
 public class StartLoggingActivity extends Activity implements Callback {
 	
     private ProgressBar mProgress;
     private ProgressBar bProgress;
     private int mProgressStatus = 100;
     private int level = 100;
     private boolean stopProgress = false;
     private Handler mHandler = new Handler();
     private TextView countdownStatus;
     private MediaRecorder videoRecorder;
     private SurfaceHolder surfaceHolder;
 	private BroadcastReceiver br;
	public static String logId = String.valueOf(System.currentTimeMillis());
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
        
         if (Config.logMedia == Config.MEDIA_LOG_BOTH) {
 			SurfaceView surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
 	        surfaceHolder = surfaceView.getHolder();
 	        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 	        surfaceHolder.addCallback(StartLoggingActivity.this);
 		}
         
        //logId = String.valueOf(System.currentTimeMillis());
        
         countdownStatus = (TextView) findViewById(R.id.countdown_status);
         
     	TextView gpsStatus = (TextView) findViewById(R.id.gps_status);
         if (((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
         	gpsStatus.setText("GPS enabled");
         } else {
         	gpsStatus.setText("GPS not enabled - enable it in the Settings menu");
         }
 
         showDialog(R.layout.countdown_dialog);
       
         br = new BroadcastReceiver() {
         	public void onReceive(Context context, Intent intent) {
         		int rawlevel = intent.getIntExtra("level", -1);
         		int scale = intent.getIntExtra("scale", -1);
         		if (rawlevel >= 0 && scale > 0) {
         			level = (rawlevel * 100) / scale;
         			Log.i(getClass().getName(),"Battery " + level);
         		}
         	}
         };
         IntentFilter battFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
         registerReceiver(br, battFilter);
         
         Log.i(getClass().getName(),"Started activity");
     }
     
     public void onDestroy () {
     	unregisterReceiver(br);
     	
 		if (videoRecorder != null) {
 			try {
 				videoRecorder.stop();
 			} catch (IllegalStateException e) {
 				e.printStackTrace();
 			}
 			videoRecorder.release();
 		    Log.i(getClass().getName(), "Stopped video recorder");
 		}
 		
     	super.onDestroy();
     	
     	stopProgress = true;
     }
     
     @Override    
     protected Dialog onCreateDialog(int id) 
     {
         switch (id) {
             case R.layout.countdown_dialog:
                 return new TimePickerDialog(
                     this, mTimeSetListener, 0, 0, true);
         }
         return null;    
     }
  
     public PendingIntent sender;
     public AlarmManager am;
     
     private TimePickerDialog.OnTimeSetListener mTimeSetListener =
     new TimePickerDialog.OnTimeSetListener() 
     {
 
 		private WakeLock wl;
 
 		public void onTimeSet(TimePicker view, int h, int m) 
         {
 			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 	        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP|PowerManager.ON_AFTER_RELEASE, getClass().getName());
 	        wl.acquire();
 	        Log.i(getClass().getName(),"Acquired wakelock");
 			
         	int wait = h * 60 * 60 * 1000 + m * 60 * 1000;
         	
     		sender = PendingIntent.getService(StartLoggingActivity.this, 0, 
     				new Intent(StartLoggingActivity.this, LoggingService.class), 0);
     		am = (AlarmManager) getSystemService(ALARM_SERVICE);
     		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + wait + 1000, sender);
     		Log.i(getClass().getName(), "Scheduled logging service to start in " + wait);
     		
     		Log.i(getClass().getName(), "Started countdown");
 
             new CountDownTimer(wait, 1000) {
 
                 public void onTick(long millisUntilFinished) {
                 	countdownStatus.setText("Dumping in: " + millisUntilFinished / 1000 + " secs");
                 	//Log.i(getClass().getName(), "Ticking");
                 }
 
                 public void onFinish() {
                 	if (wl != null) {
             			wl.release();
             			Log.i(getClass().getName(),"Released wakelock");
             		}
                 	
                 	countdownStatus.setText("Dumping now");
                 	
                 	if (Config.logMedia == Config.MEDIA_LOG_BOTH) {
                 		videoRecorder.start();
                 		Log.i(getClass().getName(), "Started video recorder");
                 	}
                    
                     mProgress = (ProgressBar) findViewById(R.id.progress_bar);
                     bProgress = (ProgressBar) findViewById(R.id.battery_bar);
 
                     // Start lengthy operation in a background thread
                     new Thread(new Runnable() {
                         public void run() {
                         	StatFs statFs;
                             while (!stopProgress) {
                             	statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
                             	mProgressStatus = (int)((double)statFs.getAvailableBlocks() / (double)(statFs.getBlockCount() - (statFs.getFreeBlocks() - statFs.getAvailableBlocks())) * 100);
                             	//Log.i(getClass().getName(), "Available space =" + statFs.getAvailableBlocks() * statFs.getBlockSize());
                             	
                             	if (mProgressStatus < 2 || level < 2) {
                             		Intent loggingServiceIntent = new Intent();
                                     loggingServiceIntent.setAction("bzb.android.logger.STARTLOGGING");
                                     stopService(loggingServiceIntent);
                                     Log.i(getClass().getName(),"Memory/battery out: sent intent to stop logging service");
                                     
                                     stopProgress = true;
                                     StartLoggingActivity.this.finish();
                                     break;
                             	}
                             	
                                 // Update the progress bar
                                 mHandler.post(new Runnable() {
                                     public void run() {
                                         mProgress.setProgress(mProgressStatus);
                                         bProgress.setProgress(level);
                                     }
                                 });
                                 
                                 try {
             						Thread.sleep(30000);
             					} catch (InterruptedException e) {
             						e.printStackTrace();
             					}
                             }
                         }
                     }).start();
                 }
              }.start();
         }
     };
 
     /////////////////////////
     
 	@Override
 	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {}
 	
 	@Override
 	public void surfaceCreated(SurfaceHolder arg0) {
 		Log.i(getClass().getName(), "Created surface");
 		videoRecorder = new MediaRecorder();
 	    videoRecorder.setPreviewDisplay(surfaceHolder.getSurface());
 		videoRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
 		videoRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
 		videoRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
 		videoRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
 		videoRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
 		videoRecorder.setVideoFrameRate(5);
 		videoRecorder.setVideoSize(320, 240);
		videoRecorder.setOutputFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/vlog_" + logId + ".3gp");
         
         try {
 			videoRecorder.prepare();
 			Log.i(getClass().getName(), "Prepared video recorder");
 		} catch (IllegalStateException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder arg0) {}
 	
 	
 }

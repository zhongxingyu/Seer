 package com.ddyystudio.audiocontrollight;
 
 import java.io.IOException;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.hardware.SensorListener;
 import android.hardware.SensorManager;
 import android.media.MediaRecorder;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.animation.Animation;
 import android.view.animation.RotateAnimation;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 
 @SuppressLint("HandlerLeak")
 public class MainActivity extends Activity implements SensorListener {
 	private static final String LOG_TAG = "MainActivity";
 	private RelativeLayout linear;
 	private boolean close = true;
 	private Button myBtn;
 	private MediaRecorder mRecorder = null;
 	private static String mFileName = null;
 	private Camera camera = null;
 	private Parameters parameters = null;
 	private ImageView ImgCompass;
 	private SensorManager sm = null;
 	private RotateAnimation myAni = null;
 	private float DegressQuondam = 0.0f;
 	private Handler mhandler = new Handler(){
 		public void handleMessage(Message msg) { 
     	//
 			lightSwitch(close);
     	super.handleMessage(msg); 
     	} 
     };
 	private Thread thread = new Thread(new Runnable() {
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			long time1 = System.currentTimeMillis();
 			long time2 = time1;
			while (true) {
 				time2 = System.currentTimeMillis();
 				if (time2 - time1 > 30) {
 					if (mRecorder.getMaxAmplitude() > 10000) {
 						mhandler.sendEmptyMessage(0);
 						time1 = time2 + 600;
 					}else{
 						time1 = time2;
 					}
 				}
 			}
 		}
 	});
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
      // ȫãشװ
  		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
  				WindowManager.LayoutParams.FLAG_FULLSCREEN);
  		requestWindowFeature(Window.FEATURE_NO_TITLE); // Ļʾޱ⣬Ҫúãٴα
  		getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
  				WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
  		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
  				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         setContentView(R.layout.activity_main);
         
         mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
         mFileName += "/audiorecord.3gp";
         
         linear = (RelativeLayout) findViewById(R.id.LinearLayout1);
         setContentView(linear);
         startRecording();
 		thread.start();
 		camera = Camera.open();
 		ImgCompass = (ImageView) findViewById(R.id.imageView1);
         myBtn = (Button) findViewById(R.id.button1);
         myBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				lightSwitch(close);
 			}
 		});
     }
 	
 	protected void lightSwitch(boolean close) {
 		if (close) {
 			linear.setBackgroundResource(R.drawable.light_on);
 			myBtn.setBackgroundResource(R.drawable.on);
 			parameters = camera.getParameters();
 			parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);// 
 			camera.setParameters(parameters);
 			this.close = false;
 		} else {
 			linear.setBackgroundResource(R.drawable.light_off);
 			myBtn.setBackgroundResource(R.drawable.off);
 			parameters.setFlashMode(Parameters.FLASH_MODE_OFF);// ر
 			camera.setParameters(parameters);
 			this.close = true;
 		}
 	}
 	
 	private void AniRotateImage(float fDegress) {
 		myAni = new RotateAnimation(DegressQuondam, fDegress,
 				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
 				0.5f);
 		myAni.setDuration(30);
 		myAni.setFillAfter(true);
 
 		ImgCompass.startAnimation(myAni);
 
 		DegressQuondam = fDegress;
 	}
 
     @Override
 	protected void onStart() {
     	sm = (SensorManager) getSystemService(SENSOR_SERVICE);
     	super.onStart();
 	}
 
 	@Override
 	protected void onPause() {
 		stopRecording();
 		camera.release();
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		sm.registerListener(this, SensorManager.SENSOR_ORIENTATION
 				 | SensorManager.SENSOR_ACCELEROMETER, SensorManager.SENSOR_DELAY_FASTEST);
 		super.onResume();
 	}
 
 	@Override
 	protected void onStop() {
 		sm.unregisterListener(this);
 		super.onStop();
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 	
 	public void onSensorChanged(int sensor, float[] values) {
 		synchronized (this) {
 			if (sensor == SensorManager.SENSOR_ORIENTATION) {
 				// OrientText.setText("--- NESW ---");
 				if (Math.abs(values[0] - DegressQuondam) < 1)
 					return;
 				if (DegressQuondam != -values[0])
 					AniRotateImage(-values[0]);
 			}
 		}
 	}
 	
 	@Override
 	public void onAccuracyChanged(int sensor, int accuracy) {
 		// TODO Auto-generated method stub
 	}
 	
 	private void startRecording() {
 		mRecorder = new MediaRecorder();
         mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
         mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
         mRecorder.setOutputFile(mFileName);
         mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
         try {
             mRecorder.prepare();
         } catch (IOException e) {
             Log.e(LOG_TAG, "prepare() failed");
         }
         mRecorder.start();
 	}
 	
 	private void stopRecording() {
         mRecorder.stop();
         mRecorder.release();
         mRecorder = null;
 	}
 }

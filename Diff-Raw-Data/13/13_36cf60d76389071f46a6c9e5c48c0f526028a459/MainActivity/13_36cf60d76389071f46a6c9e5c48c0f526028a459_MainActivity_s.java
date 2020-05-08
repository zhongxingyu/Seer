 package com.ddyystudio.audiocontrollight;
 
 import java.io.IOException;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.media.MediaRecorder;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Display;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.view.animation.RotateAnimation;
 import android.view.animation.TranslateAnimation;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 
 @SuppressLint("HandlerLeak")
 public class MainActivity extends Activity implements SensorEventListener {
 	
 	private static final String LOG_TAG = "MainActivity";
 	private RelativeLayout linear;
 	private boolean close = true;
 	private Button myBtn;
 	private MediaRecorder mRecorder = null;
 	private static String mFileName = null;
 	private Camera camera = null;
 	private Parameters parameters = null;
 	private ImageView ImgCompass;
 	private SensorManager mSensorManager = null;
 	private float currentDegree = 0f; //ָͼƬתĽǶ
 	private boolean isRun = false;
 	private Button buttonAdd;
 	private Button buttonSosOff;
 	private Button buttonSetting;
 	private Button buttonCommit;
 	private Animation animationTranslate, animationRotate;
 	private LayoutParams params = new LayoutParams(0, 0);
 	private static Boolean isClick = false;
 	private static int width, height;
 	
 	private Handler mhandler = new Handler(){
 		public void handleMessage(Message msg) { 
     	//
 //			Log.e("handleMessage", "handleMessage");
 			lightSwitch();
     	super.handleMessage(msg); 
     	} 
     };
 	private Thread thread = new Thread(new Runnable() {
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			long time1 = System.currentTimeMillis();
 			long time2 = time1;
 			while (isRun) {
 				time2 = System.currentTimeMillis();
 				if (time2 - time1 > 30) {
 //					float angle = 0;
 //					angle = (float)mRecorder.getMaxAmplitude()/10000;
 //					Log.e("mRecorder", ""+angle);
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
         
         Display display = getWindowManager().getDefaultDisplay(); 
 		height = display.getHeight();  
 		width = display.getWidth();
         
         mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
         mFileName += "/audiorecord.3gp";
         
         linear = (RelativeLayout) findViewById(R.id.LinearLayout1);
         setContentView(linear);
         
         thread.start();
 		
 		ImgCompass = (ImageView) findViewById(R.id.imageView1);
 		buttonAdd = (Button) findViewById(R.id.add);
 		buttonAdd.setVisibility(View.VISIBLE);
 		buttonSosOff = (Button) findViewById(R.id.sosoff);
 		buttonSosOff.setVisibility(View.INVISIBLE);
 		buttonSetting = (Button) findViewById(R.id.setting);
 		buttonSetting.setVisibility(View.INVISIBLE);
 		buttonCommit = (Button) findViewById(R.id.commit);
 		buttonCommit.setVisibility(View.INVISIBLE);
         myBtn = (Button) findViewById(R.id.button1);
         
         buttonAdd.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if(isClick == false)
 				{
 					isClick = true;
 					buttonSosOff.setVisibility(View.VISIBLE);
 					buttonSetting.setVisibility(View.VISIBLE);
 					buttonCommit.setVisibility(View.VISIBLE);
 					buttonAdd.startAnimation(animRotate(-45.0f, 0.5f, 0.45f));					
 					buttonSosOff.startAnimation(animTranslate(0.0f, -220.0f, width - 100, height - 320, buttonSosOff, 400));
 					buttonSetting.startAnimation(animTranslate(0.0f, -150.0f, width - 100, height - 250, buttonSetting, 400));
 					buttonCommit.startAnimation(animTranslate(0.0f, -80.0f, width - 100, height - 180, buttonCommit, 400));
 				}
 				else
 				{					
 					isClick = false;
 					buttonAdd.startAnimation(animRotate(90.0f, 0.5f, 0.45f));
 					buttonSosOff.startAnimation(animTranslate(0.0f, 220.0f, width - 100, height - 100, buttonSosOff, 400));
 					buttonSetting.startAnimation(animTranslate(0.0f, 150.0f, width - 100, height - 100, buttonSetting, 400));
 					buttonCommit.startAnimation(animTranslate(0.0f, 80.0f, width - 100, height - 100, buttonCommit, 400));
 				}
 			}
 		});
         buttonSosOff.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 
 			}
 		});
         buttonSetting.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				isRun = false;
 				Intent intent = new Intent();
 				intent.setClass(MainActivity.this, SettingActivity.class);
 	    		startActivity(intent);
 	    		isClick = false;
 	    		buttonAdd.startAnimation(animRotate(90.0f, 0.5f, 0.45f));
 				buttonSosOff.startAnimation(animTranslate(0.0f, 220.0f, width - 100, height - 100, buttonSosOff, 400));
 				buttonSetting.startAnimation(animTranslate(0.0f, 150.0f, width - 100, height - 100, buttonSetting, 400));
 				buttonCommit.startAnimation(animTranslate(0.0f, 80.0f, width - 100, height - 100, buttonCommit, 400));
 			}
 		});
         buttonCommit.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Log.e("buttonCommit", "buttonCommit");
 			}
 		});
         myBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				lightSwitch();
 			}
 		});
     }
 	
 	protected void showAlert(String str,String message,String bt1,String bt2,String bt3) {
 		Dialog alertDialog = new AlertDialog.Builder(this). 
                 setTitle("ȷɾ"). 
                setMessage("ȷɾϢ"). 
                setIcon(R.drawable.ic_launcher). 
                 setPositiveButton("ȷ", new DialogInterface.OnClickListener() { 
                      
                     @Override 
                     public void onClick(DialogInterface dialog, int which) { 
                         // TODO Auto-generated method stub  
                     } 
                }). 
                 setNegativeButton("ȡ", new DialogInterface.OnClickListener() { 
                      
                     @Override 
                     public void onClick(DialogInterface dialog, int which) { 
                         // TODO Auto-generated method stub  
                     } 
                }). 
                 setNeutralButton("鿴", new DialogInterface.OnClickListener() { 
                      
                     @Override 
                     public void onClick(DialogInterface dialog, int which) { 
                         // TODO Auto-generated method stub  
                     } 
                }). 
                 create(); 
         alertDialog.show(); 
 	}
 	
 	protected void lightSwitch() {
 		if (close) {
 			linear.setBackgroundResource(R.drawable.light_on);
 			myBtn.setBackgroundResource(R.drawable.on);
 			parameters = camera.getParameters();
 			parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);// 
 			camera.setParameters(parameters);
 			close = false;
 		} else {
 			linear.setBackgroundResource(R.drawable.light_off);
 			myBtn.setBackgroundResource(R.drawable.off);
 			parameters.setFlashMode(Parameters.FLASH_MODE_OFF);// ر
 			camera.setParameters(parameters);
 			close = true;
 		}
 	}
 
     @Override
 	protected void onStart() {
     	Log.e("onStart", "onStart");
     	mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
     	super.onStart();
 	}
 
 	@Override
 	protected void onPause() {
 		Log.e("onPause", "onPause");
 		isRun = false;
 		stopRecording();
 		camera.release();
 		mSensorManager.unregisterListener(this);
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		Log.e("onResume", "onResume");
 		//ע
 		startRecording();
 		isRun = true;
 		camera = Camera.open();
     	mSensorManager.registerListener(this
     			, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
 		super.onResume();
 	}
 
 	@Override
 	protected void onStop() {
 		Log.e("onStop", "onStop");
 		mSensorManager.unregisterListener(this);
 		super.onStop();
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
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
 	
 	protected Animation animRotate(float toDegrees, float pivotXValue, float pivotYValue) {
 		// TODO Auto-generated method stub
 		animationRotate = new RotateAnimation(0, toDegrees, Animation.RELATIVE_TO_SELF, pivotXValue, Animation.RELATIVE_TO_SELF, pivotYValue);
 		animationRotate.setAnimationListener(new AnimationListener() 
 		{
 			
 			@Override
 			public void onAnimationStart(Animation animation) 
 			{
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void onAnimationRepeat(Animation animation) 
 			{
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void onAnimationEnd(Animation animation) 
 			{
 				// TODO Auto-generated method stub
 				animationRotate.setFillAfter(true);
 			}
 		});
 		return animationRotate;
 	}
 	
 	protected Animation animTranslate(float toX, float toY, final int lastX, final int lastY,
 			final Button button, long durationMillis) {
 		// TODO Auto-generated method stub
 		animationTranslate = new TranslateAnimation(0, toX, 0, toY);
 		if (isClick) {
 			animationTranslate.setInterpolator(AnimationUtils.loadInterpolator(this,  
                     android.R.anim.overshoot_interpolator));
 		}else{
 			animationTranslate.setInterpolator(AnimationUtils.loadInterpolator(this,  
                     android.R.anim.anticipate_interpolator));
 		}
 		animationTranslate.setAnimationListener(new AnimationListener()
 		{
 						
 			@Override
 			public void onAnimationStart(Animation animation)
 			{
 				// TODO Auto-generated method stub
 								
 			}
 						
 			@Override
 			public void onAnimationRepeat(Animation animation) 
 			{
 				// TODO Auto-generated method stub
 							
 			}
 						
 			@Override
 			public void onAnimationEnd(Animation animation)
 			{
 				// TODO Auto-generated method stub
 				params = new LayoutParams(0, 0);
 				params.height = 60;
 				params.width = 60;											
 				params.setMargins(lastX, lastY, 0, 0);
 				button.setLayoutParams(params);
 				button.clearAnimation();
 				if (!isClick) {
 					button.setVisibility(View.INVISIBLE);
 				}
 						
 			}
 		});																								
 		animationTranslate.setDuration(durationMillis);
 		return animationTranslate;
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		//ȡeventĴ
 				int sensorType = event.sensor.getType();
 				
 				switch(sensorType){
 				case Sensor.TYPE_ORIENTATION:
 					float degree = event.values[0]; //ȡzתĽǶ
 //					Log.e("ȡzתĽǶ", ""+degree);
 					//ת
 					RotateAnimation ra = new RotateAnimation(currentDegree,-degree,Animation.RELATIVE_TO_SELF,0.5f
 							,Animation.RELATIVE_TO_SELF,0.5f);
 				 ra.setDuration(100);//ʱ
 				 ImgCompass.startAnimation(ra);
 				 currentDegree = -degree;
 				 break;
 				
 				}
 	}
 }

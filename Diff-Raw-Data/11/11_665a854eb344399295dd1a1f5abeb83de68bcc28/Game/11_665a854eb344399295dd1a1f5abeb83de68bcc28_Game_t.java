 package com.example.spaceshiphunter;
 
 
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.SoundPool;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.TextView;
 
 
 public class Game extends Activity implements SensorEventListener, OnTouchListener {
     /** Called when the activity is first created. */
 	
 	private static final String TAG = Game.class.getSimpleName();
 	private SensorManager mySensorManager = null;
 	private Sensor myAccelerometer = null;
 	float[] accel_vals = new float[3];
 	private float maxAccel = 8;
 	
 	MainGamePanel gamePanel;
 	FrameLayout game;
 	RelativeLayout weaponLayout;
 	ImageButton weapon1;
 	ImageButton weapon2;
 	static Vibrator vb;
 
 	MediaPlayer mPlayer;
 	static SoundPool spool;
 	static int lasersfx; 
 	static int missilesfx;
 	static int hitsfx;
 	static int enemydeathsfx;
 	static int playerdeathsfx;
 	 
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	
         super.onCreate(savedInstanceState);// requesting to turn the title OFF
         requestWindowFeature(Window.FEATURE_NO_TITLE);// making it full screen
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// set our MainGamePanel as the View
         
         gamePanel = new MainGamePanel(this);
         game = new FrameLayout(this);
        	weaponLayout = new RelativeLayout(this);
        	
        	
        	vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
         
         //Load sound effects
         AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
         spool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
         
         lasersfx = spool.load(this, R.raw.laser, 5);
         missilesfx = spool.load(this, R.raw.missile, 5);
         hitsfx = spool.load(this, R.raw.hit,5);
         enemydeathsfx = spool.load(this, R.raw.enemy_destroy, 5);
         playerdeathsfx =spool.load(this, R.raw.player_destroy, 5);
         
       
        	
         //Weapon1
        	weapon1 = new ImageButton(this);
         weapon1.setBackgroundResource(R.drawable.weapon1);
         weapon1.setId(99);        
         RelativeLayout.LayoutParams b1 = new LayoutParams(
         		RelativeLayout.LayoutParams.WRAP_CONTENT,
         		RelativeLayout.LayoutParams.WRAP_CONTENT);
       
         b1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
         b1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
         b1.rightMargin = 25;
         b1.bottomMargin = 25;
         weapon1.setLayoutParams(b1);
         weapon1.setOnTouchListener(this);
         
         //Weapon2
        weapon2 = new ImageButton(this);
        weapon2.setBackgroundResource(R.drawable.weapon2);
        weapon2.setId(100);
        RelativeLayout.LayoutParams b2 = new LayoutParams(
        		RelativeLayout.LayoutParams.WRAP_CONTENT,
        		RelativeLayout.LayoutParams.WRAP_CONTENT);
        b2.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        b2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        b2.leftMargin = 25;
        b2.bottomMargin = 25;
        weapon2.setLayoutParams(b2);
        weapon2.setOnTouchListener(this);
   
        
        
        	weaponLayout.setLayoutParams(b1);  
        	weaponLayout.addView(weapon1);
        	weaponLayout.setLayoutParams(b2);  
        	weaponLayout.addView(weapon2);
     	
     	
         game.addView(gamePanel);
         game.addView(weaponLayout);
         
         setContentView(game);
         Log.d(TAG, "View added");
         mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 		myAccelerometer = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 		
     }
 
 	@Override
 	protected void onDestroy() {
 		Log.d(TAG, "Destroying...");
 		finish();
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onStop() {
 		Log.d(TAG, "Stopping...");
 		super.onStop();
 	}
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mySensorManager.registerListener(this, myAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
 	}
 	
 	
 	@Override
 	protected void onPause() {
 		mySensorManager.unregisterListener(this);
 	Log.d(TAG, "Pausing");
 	MainThread.running=false;//this is the value for stop the loop in the run()
 	super.onPause();
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		int type = event.sensor.getType();
 		if (type == Sensor.TYPE_ACCELEROMETER){
 			accel_vals = event.values;
 			float accelX = accel_vals[1];
 			float accelY = accel_vals[0];
 			MainThread.getGamePanel().setNewXY(accelX,accelY,accel_vals[1],accel_vals[0]);
 		}
 		
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		if(v.getId()== 99){
 			if(event.getAction() == MotionEvent.ACTION_DOWN){
 			MainThread.getGamePanel().check1(true);		
 			weapon1.setBackgroundResource(R.drawable.weapon1_pressed);
 			vb.vibrate(40);			
		
 			
 	       
 			}
 		else if(event.getAction() == MotionEvent.ACTION_UP){
 			MainThread.getGamePanel().check1(false);
 			weapon1.setBackgroundResource(R.drawable.weapon1);
		
 			
 			}
 		}
 		
 		if(v.getId()==100){
 			if(event.getAction() == MotionEvent.ACTION_DOWN){
 				MainThread.getGamePanel().check2(true);		
 				weapon2.setBackgroundResource(R.drawable.weapon2_pressed);
 				vb.vibrate(40);
 				
 			 
 			}
 			else if(event.getAction() == MotionEvent.ACTION_UP){
 				MainThread.getGamePanel().check2(false);
 				weapon2.setBackgroundResource(R.drawable.weapon2);
 			}
 		}
 		
 		return false;
 	}
 	
 	
 
 
 }

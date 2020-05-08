 package org.MAG;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.pm.ActivityInfo;
 import android.graphics.Canvas;
 import android.graphics.PixelFormat;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceHolder.Callback;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class Rejecterator extends Activity implements SensorEventListener, Callback {
 
 	private static final String TAG = "Rejecterator";
 	
 	//foreground view
 	private MySurfaceView foreground;
 	private SurfaceHolder holder;
 	
 	private SensorManager sensorManager;
 	private Sensor accelerometer;
 	private Vibrator vibrotron;
 	
 	private boolean rejected;
 	
 	private CatchableObject caught;
 	private Sprite caughtSprite;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		
 		setContentView(R.layout.rejecterator);
 		
 		foreground = (MySurfaceView)findViewById(R.id.rejecterator_foreground);
 		
 		holder = foreground.getHolder();
         holder.setFormat(PixelFormat.TRANSPARENT);
         
         holder.addCallback(this);
         foreground.getHolder().setFormat(PixelFormat.TRANSPARENT);
         foreground.setZOrderOnTop(true);
 		
 		caught = Reeler.getCatch();
 		caughtSprite = caught.getSprite();
 		
 		Log.e("KingFisher", "made the Rejecterator");
 		
 		SoundManager.loadSounds(SoundManager.FLINGABLE);
 		
 		vibrotron = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
 		sensorManager = (SensorManager)getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
         accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
         
         foreground.addSprite(caughtSprite);
 	}
 	
 	@Override
 	public void onPause() {
 		sensorManager.unregisterListener(this);
 		super.onPause();
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
 		
 		//TODO: wait, then "throw it back"
 	}
 	
	public void onAccuracyChanged(Sensor sensor, int accuracy) { /* Yeah, well, you know, that's just like, your opinion, man. */ }
 
 	/**TODO: we might want to make sure this is good enough. maybe a delay after loading up the activity before we start listening
 	 * Users sometimes entirely missed the rejecterator because they were still shaking the king down for treasure.
 	 * 
 	 * @param event the sensor event from the accelerometer
 	 */
 	public void onSensorChanged(SensorEvent event) {
 		if (!rejected) {
 			if (event.values[1] < -SensorManager.GRAVITY_EARTH * 1.5 && (Math.abs(event.values[0]) > SensorManager.GRAVITY_EARTH * 1.5 || Math.abs(event.values[2]) > SensorManager.GRAVITY_EARTH * 1.5)) {
 				rejected = true;
 				
 				Log.e("KingFisher", "Throw him in the river!");
 				
 				//TODO: asynctask to shrink, move, and rotate the sprite we're throwing back.
 				
 				vibrotron.vibrate(1500);
 				if (caught.isKing())
 					SoundManager.playSound(1, 1);
 				else
 					SoundManager.playSound(2, 1);
 				
 				sensorManager.unregisterListener(this);
 				//SoundManager.playJesterSound(1, 1);
 				//Log.e("KingFisher", "X: "+ event.values[0] + " Y: " + event.values[1] + " Z: " + event.values[2]);
 				
 				//TODO: play animation, jump to next activity!
 			}
 		}
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) { }
 
 	public void surfaceCreated(SurfaceHolder holder) {
 		drawSprites();
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) { }
 	
 	/**
 	 * Tell the foreground to draw its sprites
 	 */
 	private void drawSprites() {
 		if (holder.getSurface().isValid()) {
 	        Canvas canvas = holder.lockCanvas();
 	        foreground.draw(canvas);
 	        holder.unlockCanvasAndPost(canvas);
         }
 	}
 }

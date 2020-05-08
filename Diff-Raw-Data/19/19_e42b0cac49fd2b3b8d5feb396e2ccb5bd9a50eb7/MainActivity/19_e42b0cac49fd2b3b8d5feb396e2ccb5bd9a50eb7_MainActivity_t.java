 package com.nomath4u.breakbrick;
 
 
 
 
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.media.SoundPool.OnLoadCompleteListener;
 import android.os.Bundle;
 import android.os.Handler;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.view.Menu;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.WindowManager.LayoutParams;
 import android.view.Window;
import android.widget.Toast;
 
 
 public class MainActivity extends Activity {
 	public int lives;
 	public int score;
 	public int level;
 	public SensorManager mSensorManager;
 	public float adcval;
 	public float maxval;
 	private SensorEventListener adcListener;
 	private Sensor adcsensor;
 	public SurfacePanel panel;
 	public boolean over = false;
 	public SoundPool pool;
 	public int soundID;
 	public int soundIDa;
 	boolean loaded = false;
     private float vals[] = new float[] {0,0,0,0,0,0,0,0,0,0};
     public static final int AVGS = 10;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);
 		createSensorManager();
 		//WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
 		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON); //Get active wakelock
 		this.panel = new SurfacePanel(this);
 		setContentView(this.panel);
 		lives = 5;
 		score = 0;
 		level = 1;
 		
 		/*Setup sounds */
 		pool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
         pool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
             @Override
             public void onLoadComplete(SoundPool soundPool, int sampleId,
                     int status) {
                 loaded = true;
             }
         });
         soundID = pool.load(this, R.raw.blip1, 1);
         soundIDa= pool.load(this,R.raw.blip2, 0);
 
 
 		
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
     @Override
     protected void onResume(){
     	super.onResume();
     	mSensorManager.registerListener(adcListener, adcsensor, SensorManager.SENSOR_DELAY_GAME);
        panel._run = true;
     }
     
     @Override
     protected void onRestart(){
     	super.onRestart();
     	mSensorManager.registerListener(adcListener, adcsensor, SensorManager.SENSOR_DELAY_GAME);
        panel._run = true;
     }
     
     private void createSensorManager(){
     	mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
 		adcsensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 		adcListener = new SensorEventListener(){
 			@Override
 			public void onSensorChanged(SensorEvent event){
 				avgAdc(event.values[0]);
 			}
 			@Override
 			public void onAccuracyChanged(Sensor sensor, int accuracy){
 			
 			}
 		};
 		mSensorManager.registerListener(adcListener,adcsensor, SensorManager.SENSOR_DELAY_GAME);
 		maxval = adcsensor.getMaximumRange();
     }
     
     @Override
     protected void onPause(){
     	super.onPause();
     	mSensorManager.unregisterListener(adcListener);
        panel._run = false;
     }
     
     @Override
     protected void onDestroy(){
     	super.onDestroy();
     	mSensorManager.unregisterListener(adcListener);
        panel._run= false;
     }
 	
 	public void addScore(int add){
 		score = score + add;
 		
 	}
 	
 	public void addLife(int add){
 		lives = lives + add;
 	}
 	
 	public void gameOver(final Context context){
 		final int highscore = getHighScore();
 		if(score > highscore){
 			SharedPreferences manager = getPreferences(MODE_PRIVATE);
 			SharedPreferences.Editor edit = manager.edit();
 			edit.putInt("highScore", score);
 			edit.commit();
 		}
 		runOnUiThread(new Runnable(){
 			public void run(){
 				AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		        builder.setCancelable(true);
 		        builder.setInverseBackgroundForced(true);
 		        builder.setTitle(R.string.game_over_title);
 		        
 		        if(score > highscore){
 		        builder.setMessage("New High Score! Your score was: " + Integer.toString(score));
 		        }
 		        else
 		        	 builder.setMessage("Your score was: " + Integer.toString(score) + "\n Current High Score: " + Integer.toString(highscore));
 		        builder.setPositiveButton("OK",
 		                new DialogInterface.OnClickListener() {
 		                    @Override
 		                    public void onClick(DialogInterface dialog,
 		                            int which) {
 		                    	panel.reset();
 		                		dialog.dismiss();
 		                        
 		                    }
 		                });
 		        builder.setNegativeButton("Exit",
 		                new DialogInterface.OnClickListener() {
 		                    @Override
 		                    public void onClick(DialogInterface dialog,
 		                            int which) {
 
 		                		dialog.dismiss();
 		                		panel._run = false; /*Stop the thread*/
 		                		Intent intent = new Intent(Intent.ACTION_MAIN);
 		                		intent.addCategory(Intent.CATEGORY_HOME);
 		                		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		                		startActivity(intent);
 		                        
 		                    }
 		                });
 		        AlertDialog alert = builder.create();
 		        alert.show();
 			}
 		});   
 	}		
 		
 	     
 	 private int getHighScore(){
 		 SharedPreferences manager = getPreferences(MODE_PRIVATE);
 		 int highscore = manager.getInt("highScore", 0);
 		 return highscore;
 	 }
 	
 	 public void backToGame(View view){
 		 panel.unpause();
 	 }
 
     public void avgAdc(float mValues){
         float total = 0;
 
 
         /*This loop handles entire reassignment except for the newly incoming one handled immediately after*/
         for( int i = 0; i < (AVGS-1); i++){
             this.vals[i] = this.vals[i+1];
         }
         //this.vals[AVGS] = mValues;
 
 
         /*this.vals[0]= this.vals[1];
         this.vals[1]= this.vals[2];
         this.vals[2]= this.vals[3];
         this.vals[3]= this.vals[4];*/
         this.vals[AVGS-1] = mValues;
        // this.adcval = ((this.vals[0] + this.vals[1] + this.vals[2] + this.vals [3] + this.vals [4])/5);
         for(int j = 0; j < AVGS; j++){
             total += this.vals[j];
         }
         this.adcval = (total/AVGS);
     }
 
     public void selfDestruct(View view){
        panel._run = false;
         Intent intent = new Intent(Intent.ACTION_MAIN);
         intent.addCategory(Intent.CATEGORY_HOME);
         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         startActivity(intent);
         //moveTaskToBack(true);
     }
    public void viewHighScore(View view){
       int highscore = getHighScore();
       int duration = Toast.LENGTH_LONG;
       Toast toast = Toast.makeText(getApplicationContext(), String.valueOf(highscore), duration);
       toast.show();
    }
 }

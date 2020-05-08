 package fi.wegar.gravnav;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.PowerManager;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import fi.wegar.gravnav.view.ArrowView;
 import fi.wegar.gravnav.view.SpokeView;
 
 public class GravNav extends Activity implements SensorEventListener {
 	
 	// handler for the background updating
 	private Handler mRefreshHandler = new Handler();
 	private RefreshRunner mRunner = new RefreshRunner();
 
 	private SensorManager mSensorMgr;
 	private Sensor mAccelerometer;
 	private PowerManager.WakeLock wakeLock;
 	
 	private TextView mTextDisplay;
 	private TextView mNumChoicesDisplay;
 	private ArrowView mArrowDisplay;
 	private SpokeView mSpokeDisplay;
 	
 	private ImageButton mIncrementButton;
 	private ImageButton mDecrementButton;
 	
 	private long lastUpdate = -1;
 	// hold the last known values of the accelerometer for the next update
 	private float lastX, lastY, lastZ;
 	
 	/**
 	 * The number of choices that we should iterate over when deciding next direction
 	 */
 	private int numChoices = 3;
 	
 	/**
 	 * Used to keep an intermediate value when the UI is updated
 	 */
 	private float speedDelay;
 	
 	private int time = 0;
 	
 	static final int DIALOG_NO_ACCELEROMETER_ID = 0;
 	
 	/**
 	 * The delta threshold at which the activity decides that a shake has taken place.
 	 */
 	static final int SHAKE_THRESHOLD = 700;
 	
 	/**
 	 * The threshold at which the runner is considered stopped
 	 */
 	static final int STOP_THRESHOLD = 150;
 	
 	/**
 	 * The nr of ms between each update of the shake calculations
 	 */
 	static final int UPDATE_FREQUENCY = 100;
 	
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         mTextDisplay = (TextView) findViewById(R.id.text_display);
         
         mNumChoicesDisplay = (TextView) findViewById(R.id.numchoices_display);
         mNumChoicesDisplay.setText( ""+getNumChoices() );
 
         mArrowDisplay = (ArrowView) findViewById(R.id.arrow_display);
         mArrowDisplay.setShiftRadians( 0.5*Math.PI );
         
         mSpokeDisplay = (SpokeView) findViewById(R.id.spokes_display);
         mSpokeDisplay.setShiftRadians( 0.5*Math.PI );
         mSpokeDisplay.setColor(0xff909090);
         mSpokeDisplay.setNumSpokes(numChoices+1);
         mSpokeDisplay.setDrawFromSpoke(1);
         
         
         mIncrementButton = (ImageButton) findViewById(R.id.increment_button);
         mDecrementButton = (ImageButton) findViewById(R.id.decrement_button);
         
         // attach listeners to the buttons
         mIncrementButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
             	setNumChoices( getNumChoices()+1 );
             	mSpokeDisplay.setNumSpokes(getNumChoices()+1);
             	mNumChoicesDisplay.setText( ""+getNumChoices() );
             }
 
         });
         
         mDecrementButton.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View view) {
         		setNumChoices( getNumChoices()-1 );
         		mSpokeDisplay.setNumSpokes(getNumChoices()+1);
         		mNumChoicesDisplay.setText( ""+getNumChoices() );
         	}
         	
         });
         
         
         // check if accelerometer is available
         mSensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
         mAccelerometer = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
         
         if(mAccelerometer == null) {
         	// user device does not support accelerometer, so application is useless.
         	// Show dialog informing user of this
         	showDialog(DIALOG_NO_ACCELEROMETER_ID);
         }
         
         // make sure screen does not go to sleep
         PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
         wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "GravNavTag");
         wakeLock.acquire();
         
         // next step is done in the onResume life-cycle event handler
         
     }
     
     @Override
     protected void onResume() {
     	// re-attach resourses when we continue
     	if(mSensorMgr != null) {
     		mSensorMgr.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
     	}
     	
     	// make sure the screen is not locked
     	wakeLock.acquire();
     	
     	super.onResume();
     }
     
     @Override
     protected void onPause() {
     
     	// clean up some resources when activity is paused
     	if(mSensorMgr != null) {
     		mSensorMgr.unregisterListener(this, mAccelerometer);
     	}
     	
     	// make sure we're not keeping the screen unlocked anymore
     	wakeLock.release();
     	
     	super.onPause();
     }
     
     /**
      * Create dialogs used by this Activity
      * 
      */
     @Override
     protected Dialog onCreateDialog(int id) {
     	Dialog dialog = null;
     	
     	switch(id) {
     		case DIALOG_NO_ACCELEROMETER_ID:
     			// dialog to show if the user device does not support an accelerometer
     			AlertDialog.Builder builder = new AlertDialog.Builder(this);
     			builder.setMessage(R.string.accelerometer_not_supported)
     				.setCancelable(false)
     				.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
     					
     					/**
     					 * Called if the user presses exit on the dialog. 
     					 * This doesn't really do anything other than exit to the home screen
     					 */
     					public void onClick(DialogInterface dialog, int id) {
     						finish();
     					}
     				});
     			dialog = builder.create();
     		break;
     		default:
     			dialog = null;
     	}
     	
     	return dialog;
     }
     
     /**
      * This method is not implemented, just here to honor SensorEventListener contract
      */
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 	}
 
 	/**
 	 * Called when the accelerometer changes it's values. Determines if a shake activity has taken place
 	 * based on the SHAKE_THRESHOLD
 	 */
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 
 		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
 			long curTime = System.currentTimeMillis();
 			
 			// only allow one update every X ms as determined by UPDATE_FREQUENCY
 			if( (curTime - lastUpdate) > UPDATE_FREQUENCY) {
 				
 				long diffTime = (curTime - lastUpdate);
 				lastUpdate = curTime;
 				
 				float x = event.values[0]; // x-axis
 				float y = event.values[1]; // y-axis
 				float z = event.values[2]; // z-axis
 				
 				// calculate the total delta since last update 
 				float delta = Math.abs(x+y+z - lastX - lastY - lastZ) / diffTime * 10000;
 				
 				lastX = x;
 				lastY = y;
 				lastZ = z;
 				
 				if (delta > SHAKE_THRESHOLD) {
 				    // this is a shake action
 
 					// clear the text result display
 					mTextDisplay.setText( "" );
 					
 					if(speedDelay > STOP_THRESHOLD) {
 						// the runner has not been allowed to stop (assuming the user is still shaking the device), so just add to the speed
 						speedDelay += delta;
 					} else {
 						speedDelay = delta;
 						mRefreshHandler.removeCallbacks( mRunner );
 						mRefreshHandler.postDelayed(mRunner, getNextUpdateDelay() );
 					}
 				}
 			}
 		}
 		
 	}
 	
 	/**
 	 * @param numChoices the numChoices to set
 	 */
 	public void setNumChoices(int numChoices) {
 		
 		// make sure there are always at least two choices (otherwise, what's the point)
 		numChoices = (numChoices < 2) ? 2 : numChoices;
 		
 		this.numChoices = numChoices;
 	}
 
 	/**
 	 * @return the numChoices
 	 */
 	public int getNumChoices() {
 		return numChoices;
 	}	
 	
 	/**
 	 * Calculate the number of ms to delay the next call to the runner
 	 * 
 	 * @return
 	 */
 	private long getNextUpdateDelay() {
 		
 		long delay = Math.round( 1 / speedDelay * 60000 );
 		
 		return delay;
 		
 	}
 	
 	private void updateUI() {
 		
 		// add 1 to numChoices to we account for the backwards direction in the arrow angles. The backwards arrow is not drawn on screen.
 		double stepSize = (360 / (getNumChoices()+1) );
 
 		double angle = stepSize * (++time);
 		
 		// if angle would draw arrow south, add one more to time and recalculate
		if( time == getNumChoices()+1) {
 			time = 1;
 			angle = stepSize;
 		}
 		
 		mArrowDisplay.setAngle(angle);
 		
 	}
 	
 	class RefreshRunner implements Runnable {
 
 		@Override
 		public void run() {
 			
 			if(speedDelay > STOP_THRESHOLD)
 			{
 				// logarithmically decrease the speed
 				speedDelay -= speedDelay * 0.1;
 				
 				updateUI();
 				
 				mRefreshHandler.postDelayed(this, getNextUpdateDelay() );
 			}
 			else
 			{
 				// set the direction text
 				mTextDisplay.setText( DirectionToTextConverter.getText(time, numChoices) );
 				
 				mRefreshHandler.removeCallbacks(this);
 			}
 		}
 	}
 }

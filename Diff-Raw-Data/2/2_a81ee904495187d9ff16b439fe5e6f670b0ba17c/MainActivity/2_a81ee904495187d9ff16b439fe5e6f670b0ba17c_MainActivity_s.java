 package com.example.gpsexample;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.hardware.Camera;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.hardware.Camera.AutoFocusCallback;
 import android.hardware.Camera.Parameters;
 import android.location.Location;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.Surface;
 import android.view.View;
 import android.view.View.OnLongClickListener;
 import android.widget.ImageView;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
  
 public class MainActivity extends Activity implements GPSCallBack,SensorEventListener{
 	public static double SPEED=0.0;
     private GPSManager gpsManager = null;
     private double speed = 0.0;
     private SensorManager mSensorManager;
 	private Sensor mOrientation;
 	private Sensor mAccelerometer;
 	private Sensor mCompass;
 	private Sensor lightSensor;
 	private ImageView img_side;
 	private ImageView img_front;
 	int calpitch=0;
 	int calroll=0;
 	int k=0;
 	private SeekBar seekbar;
 	Camera cam;  
 	private float[] accelValues = new float[3];
 	private float[] compassValues = new float[3];
 	private float[] inR = new float[9];
 	private float[] inclineMatrix = new float[9];
 	private float[] orientationValues = new float[3];
 	private float[] prefValues = new float[3];
 	public static int mRotation=0;
 	private boolean ready = false;
 	Drawing d;
 	  
 	MediaPlayer myMediaPlayer;
 
 
 @Override
 public void onCreate(Bundle savedInstanceState) 
 {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.activity_main);
 
      
     gpsManager = new GPSManager();
     gpsManager.startListening(getApplicationContext());
     gpsManager.setGPSCallback(this);
     img_side = (ImageView)findViewById(R.id.imgBikeSide);
     img_side.setOnLongClickListener(new OnLongClickListener() {
     	
 
 		@Override
 		public boolean onLongClick(View v) {
 			calibrate();
 			return true;
 		}
     });
     img_front = (ImageView)findViewById(R.id.imgBikeFront);
     img_front.setOnLongClickListener(new OnLongClickListener() {
     	
 
 		@Override
 		public boolean onLongClick(View v) {
 			calibrate();
 			return true;
 		}
     });
     mRotation = this.getWindowManager().getDefaultDisplay().getRotation();
     
     mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
     mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
     mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
     mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
     lightSensor= mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
     myMediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.honkhonk); 
     d=(Drawing)findViewById(R.id.view);
     
     seekbar=(SeekBar)findViewById(R.id.seekBar);
     if(lightSensor==null || !getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
     {
     	seekbar.setEnabled(false);
     }
     else
     {
     	seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
     {
 
 		@Override
 		public void onProgressChanged(SeekBar seekBar, int progress,
 				boolean fromUser) {
			if(seekbar.getProgress()==0)
 			{
 				lightlistener(0);
 				try
 				{
 				ledoff();
 				}
 				catch(Exception ex)
 		    	   {
 		    		   
 		    	   }
 				k=0;
 			}
 			else if(seekbar.getProgress()==1)
 			{
 				k=1;
 				lightlistener(1);
 				
 			}
 			else
 			{
 				k=2;
 				lightlistener(0);
 				try
 				{
 				ledon();
 				}
 				catch(Exception ex)
 		    	   {
 		    		   
 		    	   }
 			}
 			
 			
 		}
 
 		@Override
 		public void onStartTrackingTouch(SeekBar seekBar) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void onStopTrackingTouch(SeekBar seekBar) {
 			// TODO Auto-generated method stub
 			
 		}
     	
     });
     }
 
       
    
 }
 	private void getpreferences()
 {
 	k=getPreferences(MODE_PRIVATE).getInt("KEY",0);
 	calpitch=getPreferences(MODE_PRIVATE).getInt("calpitch",0);
 	calroll=getPreferences(MODE_PRIVATE).getInt("calroll",0);
     if(getPreferences(MODE_PRIVATE).getInt("Orientation",0)!=mRotation)
 	{
 		int a = calpitch;
 		calpitch=calroll;
 		calroll=a;
 	}
 
 }
 	private void savepreferences()
  {
 	 getPreferences(MODE_PRIVATE).edit().putInt("KEY", k).commit();
 	 getPreferences(MODE_PRIVATE).edit().putInt("calpitch", calpitch).commit();
 	 getPreferences(MODE_PRIVATE).edit().putInt("calroll", calroll).commit();
 	 getPreferences(MODE_PRIVATE).edit().putInt("Orientation", mRotation).commit();
  }
     @Override
     public void onGPSUpdate(Location location) 
     {
         location.getLatitude();
         location.getLongitude();
         speed = location.getSpeed();
         SPEED=speed;
     	d.invalidate();
             
     }
 	@Override
 	protected void onResume() {
 		  
 	    super.onResume();
 	    getpreferences();
 	    mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_UI );
 	    mSensorManager.registerListener(this,mAccelerometer, SensorManager.SENSOR_DELAY_UI );
 	    mSensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_UI );
 	    
 	}
     @Override
     protected void onDestroy() {
         super.onDestroy();
             gpsManager.stopListening();
             gpsManager.setGPSCallback(null);       
             gpsManager = null; 
             savepreferences();
 
     }
              
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
          
             return true;
     }
  
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
             boolean result = true;     
             return result;
     }
     @Override
 	  protected void onPause() {
 		 
 	    super.onPause();
 	    mSensorManager.unregisterListener(this);
 	    savepreferences();
 	   
 	  }
 
 	  @Override
 	  public void onSensorChanged(SensorEvent event) {
 		
 		  switch(event.sensor.getType()) {
 		    case Sensor.TYPE_ACCELEROMETER:
 		    for(int i=0; i<3; i++) {
 		    accelValues[i] = event.values[i];
 		    }
 		    if(compassValues[0] != 0)
 		    ready = true;
 		    break;
 		    case Sensor.TYPE_MAGNETIC_FIELD:
 		    for(int i=0; i<3; i++) {
 		    compassValues[i] = event.values[i];
 		    }
 		    if(accelValues[2] != 0)
 		    ready = true;
 		    break;
 		    case Sensor.TYPE_ORIENTATION:
 		    for(int i=0; i<3; i++) {
 		    orientationValues[i] = event.values[i];
 		    }
 		    break;
 		    
 		    case Sensor.TYPE_LIGHT:
 		    	if(k==1)
 		    	{
 		    	float cl = event.values[0];
 		        
 		       if(cl<=(float)30 && cl>1)
 		       {
 		    	   try
 		    	   {
 		    	   ledon();
 		    	   }
 		    	   catch(Exception ex)
 		    	   {
 		    		   
 		    	   }
 		       }
 		       else
 		       {
 		    	  try
 		    	   {
 		    	   ledoff();
 		    	   }
 		    	  catch(Exception ex)
 		    	   {
 		    		   
 		    	   }
 		    	   
 		       }
 		       }
 			  break;
 		  }
 
 		    if(!ready)
 		        return;
 		    
 		        if(SensorManager.getRotationMatrix(
 		        inR, inclineMatrix, accelValues, compassValues)) {
 		        // got a good rotation matrix
 		        SensorManager.getOrientation(inR, prefValues);
 		 
 		       if(mRotation==Surface.ROTATION_90)
 		        {
 		        img_front.setRotation((float) (360-Math.toDegrees(prefValues[1])-calpitch));
 		 	    img_side.setRotation((float) (Math.toDegrees(prefValues[2])-calroll));
 		        }
 		        else if(mRotation==Surface.ROTATION_0)
 		        {
 		        	img_side.setRotation((float) (orientationValues[1]-calpitch));
 		    	    img_front.setRotation((float) (360-orientationValues[2]+calroll));	
 		        }
 		        else if(mRotation==Surface.ROTATION_270)
 		        {
 		        	img_front.setRotation((float) (Math.toDegrees(prefValues[1])-calpitch));
 			 	    img_side.setRotation((float) (0-Math.toDegrees(prefValues[2])+calroll));
 		        }
 		        }
 		    }
 	  
 		void ledon() {
 		    cam = Camera.open();     
 		    Parameters params = cam.getParameters();
 		    params.setFlashMode(Parameters.FLASH_MODE_TORCH);
 		    cam.setParameters(params);
 		    cam.startPreview();
 		    cam.autoFocus(new AutoFocusCallback() {
 		                public void onAutoFocus(boolean success, Camera camera) {
 		                }
 		            });
 		}
 
 	     void ledoff() {
 		    cam.stopPreview();
 		    cam.release();
 	     }
 	  public void calibrate()
 	  {  
 		 
 		
 		 if(mRotation==Surface.ROTATION_90)
 	        {
 			 calpitch=(int) Math.toDegrees(prefValues[1]);
      		 calroll=(int) Math.toDegrees(prefValues[2]);
 	        }
 	        else if(mRotation==Surface.ROTATION_0)
 	        {
 	        	 calpitch=(int) orientationValues[1];
 	     		 calroll=(int) orientationValues[2]; 	
 	        }
 	        else if(mRotation==Surface.ROTATION_270)
 	        {
 	        	calpitch=(int) Math.toDegrees(prefValues[1]);
 	     		 calroll=(int) Math.toDegrees(prefValues[2]);
 	        }
 	   }
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 		
 	}
 	DoubleTapDetector doubleTapListener = new DoubleTapDetector() {
         @Override
         public void onDoubleTap() {
         	myMediaPlayer.start();
         	SPEED += (float)1.0;
         	d.invalidate();
         }        
     };
 	 @Override
 	    public boolean onTouchEvent(MotionEvent event) {
 	        if(doubleTapListener.onDoubleTapEvent(event))
 	            return true;
 	        return super.onTouchEvent(event);
 	    }    
 	 	public void btnRecTrackonClick(View v)
 	 	{
 	 		Intent i=new Intent(MainActivity.this,Aktivitetikryesore.class);
 	 		startActivity(i);
 	 	}
 	 	private void lightlistener(int a)
 	 	{
 	 	if(a==1)
 	 	{
 	 		mSensorManager.registerListener(this, lightSensor,SensorManager.SENSOR_DELAY_UI );
 	 	}
 	 	else
 	 	{
 	 		mSensorManager.unregisterListener(this, lightSensor);
 	 	}
 	 	
 	 	}
     
 }

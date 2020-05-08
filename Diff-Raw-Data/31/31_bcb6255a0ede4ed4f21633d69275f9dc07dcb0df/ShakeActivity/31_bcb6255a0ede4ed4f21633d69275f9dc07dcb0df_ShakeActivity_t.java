 package com.thirsty.view;
 
 import java.util.List;
 
 import orbotix.robot.app.StartupActivity;
 import orbotix.robot.base.ConfigureCollisionDetectionCommand;
 import orbotix.robot.base.DeviceAsyncData;
 import orbotix.robot.base.DeviceMessenger;
 import orbotix.robot.base.DeviceSensorsAsyncData;
 import orbotix.robot.base.Robot;
 import orbotix.robot.base.RobotProvider;
 import orbotix.robot.base.SetDataStreamingCommand;
 import orbotix.robot.base.StabilizationCommand;
 import orbotix.robot.sensor.AccelerometerData;
 import orbotix.robot.sensor.AttitudeData;
 import orbotix.robot.sensor.DeviceSensorsData;
 
 import com.thirsty.R;
 import com.thirsty.controller.Controller;
 import com.thirsty.controller.OnShakeListener;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 
 @SuppressWarnings("deprecation")
 public class ShakeActivity extends Activity {
 	
 	private final static String TAG = "ShakeActivity";
     private Controller _application;
 
       
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	Log.i(TAG, "onCreate()");
         super.onCreate(savedInstanceState);
         setContentView(R.layout.shake_view);
         
         this._application = (Controller)getApplication();
 
    }
    
    @Override
    public void onStart()
    {
    	Log.i(TAG, "onStart()");
    	
    	super.onStart();
    	
         this._application.setOnShakeListener(shakeListener);    
         this._application.startListeningForShake();
     }
     
     OnShakeListener shakeListener = new OnShakeListener()
     {
 		@Override
 		public void onShake() {
 			_application.stopListeningForShake();
 			_application.nextActivityFromShakeActivity(ShakeActivity.this);
 			
 		}        	
     };
     
     @Override
     public void onPause()
     {
     	super.onPause();    	
     	this._application.removeOnShakeListener(shakeListener);
     }
 
     public void onBackPressed() 
     {        
     	this._application.disconnectRobot();        
     	super.onBackPressed();    
     }
 
 }

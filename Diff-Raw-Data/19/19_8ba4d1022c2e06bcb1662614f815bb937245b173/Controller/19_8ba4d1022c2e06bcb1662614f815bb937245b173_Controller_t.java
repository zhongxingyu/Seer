 package com.thirsty.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import orbotix.robot.base.DeviceAsyncData;
 import orbotix.robot.base.DeviceMessenger;
 import orbotix.robot.base.DeviceSensorsAsyncData;
 import orbotix.robot.base.RGBLEDOutputCommand;
 import orbotix.robot.base.Robot;
 import orbotix.robot.base.RobotProvider;
 import orbotix.robot.base.SetDataStreamingCommand;
 import orbotix.robot.base.StabilizationCommand;
 import orbotix.robot.sensor.AccelerometerData;
 import orbotix.robot.sensor.AttitudeData;
 import orbotix.robot.sensor.DeviceSensorsData;
 
 import com.thirsty.R;
 import com.thirsty.view.ConnectActivity;
 import com.thirsty.view.ResultActivity;
 import com.thirsty.view.RollingActivity;
 import com.thirsty.view.SetupActivity;
 import com.thirsty.view.SplashActivity;
 import com.thirsty.view.ShakeActivity;
 
 import android.app.Application;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Handler;
 import android.util.Log;
 
 public class Controller extends Application {
 	  
	private static final int INTENT_FLAG = Intent.FLAG_ACTIVITY_SINGLE_TOP;
 	private final static String  TAG = "Application";
     /**
      * The Sphero Robot
      */
     public Robot mRobot;
 	private ComponentName _runningService;
 	
     /**
      * Data Streaming Packet Counts
      */
     private final static int TOTAL_PACKET_COUNT = 200;
     private final static int PACKET_COUNT_THRESHOLD = 50;
     private final static int STARTING_SPEED = 250;
     private final static int SHAKE_THRESHOLD = 4;
     private final static double BALL_STILL_THRESHOLD = 0.93;
     private int mPacketCounter;
     private float mAccelCurrent;
 	private boolean ballInHand = false;
     
 	
 	public String[] ruleList = new String[] {
 			"Everybody drinks",
 			"Shaker drinks", 
 			"Men drink",
 			"Ladies Drink",
 			"Left of Shaker drinks", 
 			"Right of Shaker drinks",
 			"Waterfall"
 	};
 	
 	public String[] colorList = new String[] {
 			"RED", "CYAN", "MAGENTA", "GREEN", "YELLOW", "BLUE", "ORANGE"
 	};
 	
 	public int[] gameColors = {Color.RED, Color.CYAN, Color.MAGENTA, Color.GREEN, Color.YELLOW, 
 			Color.BLUE, Color.rgb(199, 120, 38)};
 	
 	public int[] colorImageList = new int[]{
 			R.drawable.color_red, 
 			R.drawable.color_cyan, 
 			R.drawable.color_magenta, 
 			R.drawable.color_green, 
 			R.drawable.color_yellow,
 			R.drawable.color_blue,
 			R.drawable.color_orange
 			}; 
 	
 	public int[] messageImageList = new int[]{
 			R.drawable.message_me, 
 			R.drawable.message_left, 
 			R.drawable.message_girls, 
 			R.drawable.message_guys, 
 			R.drawable.message_right, 
 			R.drawable.message_everybody,
 			R.drawable.message_waterfall, 
 			}; 
 	
 	public int[] frameImageList = new int[]{
 		R.drawable.drunk_center,
 		R.drawable.drunk_right,
 		R.drawable.drunk_center,
 		R.drawable.drunk_left
 	};
 	
 	public int colorNumber = 0;
 	@Override
     public void onCreate()    
     {        
     	Log.i(TAG, "onCreate()");                  
     }
 	
     public void requestDataStreaming() {
 
         if(mRobot != null){
         	
             // Set up a bitmask containing the sensor information we want to stream
             final long mask = SetDataStreamingCommand.DATA_STREAMING_MASK_ACCELEROMETER_FILTERED_ALL |
             				  SetDataStreamingCommand.DATA_STREAMING_MASK_IMU_ANGLES_FILTERED_ALL;
 
             // Specify a divisor. The frequency of responses that will be sent is 400hz divided by this divisor.
             final int divisor = 50;
 
             // Specify the number of frames that will be in each response. You can use a higher number to "save up" responses
             // and send them at once with a lower frequency, but more packets per response.
             final int packet_frames = 1;
 
             // Reset finite packet counter
             mPacketCounter = 0;
             
             // Count is the number of async data packets Sphero will send you before
             // it stops.  You want to register for a finite count and then send the command
             // again once you approach the limit.  Otherwise data streaming may be left
             // on when your app crashes, putting Sphero in a bad state 
             final int response_count = TOTAL_PACKET_COUNT;
 
             //Send this command to Sphero to start streaming
             SetDataStreamingCommand.sendCommand(mRobot, divisor, packet_frames, mask, response_count);
         }
     }
     
     public void disconnectRobot() {
     	Log.i(TAG, "disconnectRobot()");
 
 
         mRobot = null;
 
         //Disconnect Robot
         RobotProvider.getDefaultProvider().removeAllControls();
     }
   
 
 	public void nextActivityFromSetupActivity(SetupActivity activity) 
 	{
 
 		Log.i(TAG, "nextActivityFromSetupActivity()");
     	Intent intent = new Intent(activity, RollingActivity.class); 
    	intent.addFlags(INTENT_FLAG);
     	activity.startActivity(intent); 
         activity.finish();		
 	}
 	
 	public void  nextActivityFromSplashActivity(SplashActivity activity)
 	{
 		Log.i(TAG, "nextActivityFromSplashActivity()");
     	Intent intent = new Intent(activity, ShakeActivity.class); 
    	intent.addFlags(INTENT_FLAG);
         activity.startActivity(intent);
         activity.finish();		
 	}
 	
 	public void nextActivityFromShakeActivity(ShakeActivity activity) 
 	{
 		Log.i(TAG, "nextActivityFromShakeActivity()");
     	Intent intent = new Intent(activity, SetupActivity.class); 
    	intent.addFlags(INTENT_FLAG);
     	activity.startActivity(intent); 
         activity.finish();		
 	}
 	
 	public void nextActivityFromRollingActivity(RollingActivity activity) 
 	{
 		Log.i(TAG, "nextActivityFromRollingActivity()");
     	Intent intent = new Intent(activity, ResultActivity.class); 
    	intent.addFlags(INTENT_FLAG);
     	activity.startActivity(intent); 
         activity.finish();		
 	}
 	
 	public void nextActivityFromResultActivity(ResultActivity activity) 
 	{
 		Log.i(TAG, "nextActivityFromResultActivity()");
     	Intent intent = new Intent(activity, RollingActivity.class); 
    	intent.addFlags(INTENT_FLAG);
     	activity.startActivity(intent); 
         activity.finish();		
 	}
 
 	public void nextActivityFromConnectActivity(ConnectActivity activity) {
 		Log.i(TAG, "nextActivityFromConnectActivity()");
     	Intent intent = new Intent(activity, ShakeActivity.class); 
    	intent.addFlags(INTENT_FLAG);
         activity.startActivity(intent);
         activity.finish();
 		
 	}
 
 	public void startListeningForShake() {
         requestDataStreaming();
         
         //Set the AsyncDataListener that will process each response.
         DeviceMessenger.getInstance().addAsyncDataListener(mRobot, mDataListener);
         
         StabilizationCommand.sendCommand(mRobot, false);
 		
 	}
 
 	public void stopListeningForShake() {
 		// TODO Auto-generated method stub
 		DeviceMessenger.getInstance().removeAsyncDataListener(mRobot, mDataListener);	
 		StabilizationCommand.sendCommand(mRobot, true);
 		
 			
 	}
 	
     /**
      * AsyncDataListener that will be assigned to the DeviceMessager, listen for streaming data, and then do the
      */
     private DeviceMessenger.AsyncDataListener mDataListener = new DeviceMessenger.AsyncDataListener() {
         @Override
         public void onDataReceived(DeviceAsyncData data) {
             if(data instanceof DeviceSensorsAsyncData){
 
             	// If we are getting close to packet limit, request more
             	mPacketCounter++;
             	if( mPacketCounter > (TOTAL_PACKET_COUNT - PACKET_COUNT_THRESHOLD) ) {
             		requestDataStreaming();
             	}
             	
                 //get the frames in the response
                 List<DeviceSensorsData> data_list = ((DeviceSensorsAsyncData)data).getAsyncData();
                 if(data_list != null){
 
                     //Iterate over each frame
                     for(DeviceSensorsData datum : data_list){
 
                         //Show attitude data
                         AttitudeData attitude = datum.getAttitudeData();
 
                         //Show accelerometer data
                         AccelerometerData accel = datum.getAccelerometerData();
                         if(attitude != null){
                       	  double x = accel.getFilteredAcceleration().x;
                       	  double y = accel.getFilteredAcceleration().y;
                       	  double z = accel.getFilteredAcceleration().z;
                       	  
                       	  mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
                       	  if(mAccelCurrent > SHAKE_THRESHOLD)
                       	  {
                       		  ballInHand = true;
                       		  Log.i("mathu", "SHAKE DETECTED");
                       		  
                       		  for(int i = 0; i < onShakeListenerList.size(); i++)
                       		  {
                       			  onShakeListenerList.get(i).onShake();
                       		  }
                       		  
                       		  try{
                           		  Thread.sleep(1000);
                       		  }catch(Exception e){}
                       	  }
                         }
                     }
                 }
             }
         }
 
 
     };
 	private List<OnShakeListener> onShakeListenerList = new ArrayList<OnShakeListener>();
 
 	public void setOnShakeListener(OnShakeListener onShakeListener) {
 		onShakeListenerList.add(onShakeListener);		
 	}
 
 	public void removeOnShakeListener(OnShakeListener onShakeListener) {
 		onShakeListenerList.remove (onShakeListener);	
 	}
 }

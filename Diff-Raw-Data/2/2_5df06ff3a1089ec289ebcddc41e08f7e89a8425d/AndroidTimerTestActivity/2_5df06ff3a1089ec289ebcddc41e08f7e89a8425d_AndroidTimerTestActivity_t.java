 package fpl.timers.reu;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.BatteryManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 
 public class AndroidTimerTestActivity extends Activity implements SensorEventListener {
 
   public static String BatteryTag = "Battery";
   public static String SensorTag = "Sensor";
   public static String TimerTag = "Timer";
   public static String StartTag = "Start";
   public static String TaskTag = "Task";
   public static String AccelTag = "Accelerometer";
   /** Called when the activity is first created. */
 
   Timer timer1 = new Timer();
   MyTimerTask1 firsttask;
   Timer timer2 = new Timer();
   MyTimerTask2 secondtask;
 
   private final Handler handler1 = new Handler();
   private final Handler handler2 = new Handler();
 
   private boolean activeAccel = false;
   private boolean on = false;
 
   Button startTimer;
   
   SensorManager mSensorManager;
   Sensor mAccelerometer;
   
 //battery variables
 	int scale = -1;
 	int level = -1;
 	int voltage = -1;
 	int temp = -1;
 	
 	File root;
 	File fileDir;
 	File file;
 	FileWriter filewriter;
 	BufferedWriter out;
 
 	//time variables
 	Date timestamp = new Date();
 	
 	SimpleDateFormat csvFormatter;
 	String csvFormattedDate;
 	final int duration=5000;
 	
 	int counter = 0;
 	
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
 
     mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
     mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
     
   //----------------------------------------------------------------------------------------------------------------------CSVFile---------------
   		csvFormatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");  //formatter for CSV timestamp field
   		//crashes with adding  HH:mm:ss
 
   		
   		SimpleDateFormat fileDate = new SimpleDateFormat("MM-dd-yyyy");
   		SimpleDateFormat fileTime = new SimpleDateFormat("HH-mm-ss");
   		
   		
   		String formatFileDate = fileDate.format(timestamp);
   		String formatFileTime = fileTime.format(timestamp);
   		
 
   		try {  
 
 
   			// check for SDcard   
   			root = Environment.getExternalStorageDirectory();  
 
 
   			//Log.i("Writter","path.." +root.getAbsolutePath());  
 
 
   			//check sdcard permission  
   			if (root.canWrite()){ 
 
   				fileDir = new File(root.getAbsolutePath()+"/battery_data/");  
   				fileDir.mkdirs();  
 
   				file= new File(fileDir, formatFileDate +"_duration"+ "-"+ duration+"_"+ formatFileTime + ".csv");  
   				filewriter = new FileWriter(file);  
   				out = new BufferedWriter(filewriter);
 
   				out.write("DateTime+" +","+ "BatteryLevel(0-100)" +"Sample");  
 
 
   			}  
   		} catch (IOException e) {  
   			Log.e("ERROR:---", "Could not write file to SDCard" + e.getMessage());  
   		}  
   		
     startTimer = (Button) findViewById(R.id.btnStartTimer);
     
     startTimer.setOnClickListener(new View.OnClickListener() {
       public void onClick(View v) {
         // Executes when the button is clicked
     	  //Log.d(StartTag,"Button Clicked");
         startTimer();
       }
     });
   }
   
   @Override
   protected void onDestroy() {
     //Cancel all timers
     firsttask.cancel();
     firsttask = null;
     timer1.purge();
     timer1.cancel();
     timer1=null;
     
     secondtask.cancel();
     secondtask = null;
     timer2.purge();
     timer2.cancel();
     timer2=null;
     
     unregisterReceiver(batteryReceiver);
     
 	try {
 		out.flush();
 		out.close();
 	} catch (IOException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
     
     super.onDestroy();
   }
 
   /**
    * Registers the first timer
    */
   private void startTimer() {
     firsttask = new MyTimerTask1();
     timer1.schedule(firsttask, 5000, 5000);
     Log.d(TimerTag, "On first run - scheduled timer1");
   }
 
   /**
    * This method is called from both timers to control the logic of turning off
    * one timer and turning on the other
    */
   private void checkTimers() {
     // TODO Auto-generated method stub
     if (on) {
     	
       timer2.cancel();
       Log.d(TimerTag, "canceled timer2");
       //Log.d(TAG, "On Value=" + on);
       // Scheduling wait time until the next timer triggers, which will turn the
       // accel. on
       timer1 = new Timer();
       firsttask = new MyTimerTask1();
       timer1.schedule(firsttask, 30000, 5000);
      // Log.d(TimerTag, "scheduled timer1");
       on = false;
     } else {
       timer1.cancel();
       Log.d(TimerTag, "canceled timer1");
      // Log.d(TimerTag, "On Value=" + on);
       // Scheduling on time until the next timer triggers, which will shut the
       // accel. off
       timer2 = new Timer();
       secondtask = new MyTimerTask2();
       timer2.schedule(secondtask, 5000, 30000);
       Log.d(TimerTag, "scheduled timer2");
       on = true;
     }
   }
 
   private class MyTimerTask1 extends TimerTask {
 
     @Override
     public void run() {
       // The first timer to trigger
       handler1.post(new Runnable() {
         public void run() {
 
           Log.d(TaskTag, "Running firsttask");
 
           // back on system thread
           if (activeAccel == false) {
 
             Log.d(AccelTag, "Accelerometer is inactive - Turn On");
 
             // TODO - accelerometer would be turned on here
             mSensorManager.registerListener(AndroidTimerTestActivity.this,
         			mAccelerometer,
         			SensorManager.SENSOR_DELAY_FASTEST);
             activeAccel = true;
 
           }// end if
           checkTimers();
         }// end of internal run
       });// end of handler.post
     }
 
   };// end of firsttask
   private class MyTimerTask2 extends TimerTask {
 
     @Override
     public void run() {
       handler2.post(new Runnable() {
         public void run() {
 
           Log.d(TaskTag, "Running secondtask");
 
           // back on system thread
           if (activeAccel == true) {
 
             Log.d(AccelTag, "Accelerometer is active - Turn Off");
             // battery();
             IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
 			registerReceiver(batteryReceiver, filter);
 
             // TODO - accelerometer would be turned off here
             mSensorManager.unregisterListener(AndroidTimerTestActivity.this,mAccelerometer);
             activeAccel = false;
           }// end if
 
           checkTimers();
         }// end of internal run
       });// end of handler.post
     }// end of run
     
   }//end of secondtask
 public void onAccuracyChanged(Sensor sensor, int accuracy) {
 	// TODO Auto-generated method stub
 	
 }
 
 public void onSensorChanged(SensorEvent event) {
 	// TODO Auto-generated method stub
 	
 }
 
 BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
 
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 
 		level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);//BATTERY CHARGE
 		scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);//SCALE OF FULL BATTERY CHARGE
 		temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);//BATTERY TEMPERATURE
 		voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);//BATTERY VOLTAGE
 		Log.e("BatteryManager", "level is "+level+"/"+scale+", temp is "+temp+", voltage is "+voltage);     
 
 		try {
 
			timestamp = new Date();
 			csvFormattedDate = csvFormatter.format(timestamp);
 
 			out.newLine();
 			
 			out.append(csvFormattedDate +","+ Integer.toString(level) +"," + ++counter);
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 };
 
 }

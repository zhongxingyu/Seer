 package alarm.main;
 
 import android.app.Activity;
 import android.content.Context;
 
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.os.Vibrator;
 
 import alarm.main.HorizontalSlider.OnProgressChangeListener;
 import alarm.main.R;
 
 public class AcknowledgeActivity extends Activity
 {
     /** Called when the activity is first created. */
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
     	//needed for slider?
     	//requestWindowFeature(Window.FEATURE_PROGRESS);
         super.onCreate(savedInstanceState);
         setContentView(R.layout.slider);
     	
         slider_ = (HorizontalSlider)findViewById(R.id.progressBar);
         slider_.setOnProgressChangeListener(changeListener);
 
 		//light screen, if it is dark (this does not work)
 		//Window w = getWindow();
 		//w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 		
 		PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
 		wakelock_ = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AlarmLock");
 		wakelock_.acquire();
 		
		vibrateFinished_ = false;
		soundFinished_ = false;
         vibrateThread_.start();
         musicPlayerThread_.start();
     }
 
     protected void onDestroy()
     {
     	wakelock_.release();
     	super.onDestroy();
 	}
     
     //This aknowledges an alarm
     private OnProgressChangeListener changeListener = new OnProgressChangeListener()
     {
     	public void onProgressChanged(View v, int progress)
     	{
     		if (progress > 80 )
     		{
     			alarmAknowledged_ = true;
     			
    			while ( !vibrateFinished_ || !soundFinished_ )
    			{
    				//Wait till threads have finished
    				//wait(100);
    				//TODO find some way to save resources here
    			}
    			
     			//end this activity
     			finish();
     		}
     	}
 	};
 	
     private HorizontalSlider slider_;
     private boolean alarmAknowledged_ = false;
    private boolean vibrateFinished_ = false;
    private boolean soundFinished_ = false;
     private PowerManager.WakeLock wakelock_;
     private final Thread vibrateThread_ = new VibrateThread();
     private final Thread musicPlayerThread_ = new MusicPlayerThread();
 
     class VibrateThread extends Thread 
     {
     	public void run()
     	{
 			while(!alarmAknowledged_)
 			{
 				// Vibrate the mobile phone
 				Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
 				vibrator.vibrate(500);
 				try
 				{
 					sleep(1000);
 				}
 				catch (InterruptedException e)
 				{
 					e.printStackTrace();
 				}
 			}
			
			vibrateFinished_ = true;
 
     	}
    	
     };
     
     class MusicPlayerThread extends Thread 
     {
     	public void run()
     	{
 			//Play alarm-sound
 			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
 			Ringtone alarm = RingtoneManager.getRingtone(getApplicationContext(), notification);
 			
 			while(!alarmAknowledged_)
 			{
 				alarm.play();
 				try
 				{
 					sleep(1000);
 				}
 				catch (InterruptedException e)
 				{
 					e.printStackTrace();
 				}
 			}
			soundFinished_ = true;
 
     	}
     }; 
 }

 package com.example.cardio_v1;
 
 import java.io.IOException;
 import java.util.HashMap;


 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.Menu;
 import android.widget.TextView;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.SoundPool;
 import android.media.AudioManager;
 import android.media.SoundPool.OnLoadCompleteListener;
 
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 import android.media.*;
 
 import android.os.IBinder;
 import android.content.ServiceConnection;
 import android.content.ComponentName;
 
 import android.util.Log;
 
 public class DisplayMessageActivity extends Activity implements OnSeekBarChangeListener
 {
 	private int speed;
 	private TextView textView3;
 	//private TextView textViewVol;
 	private TextView distance;
 	private TextView minSpeed;
 
 	float currentSpeed;
 	float totalDist;
 	String cSpd;
 	String tDist;
 	
 	private int count = 0;
     private GPSservice sbinder;
    // private soundService abinder;
     Intent intent;
     private Handler UIhandler = new Handler();
     
     SeekBar volControl;
 	
 	TextView volume;
 	
 	AudioManager am;
 	
 	String message2;
 	
     private ServiceConnection svcconnect = new ServiceConnection() {
     	public void onServiceConnected(ComponentName name, IBinder svc) {
     		sbinder = ((GPSservice.gpsbinder)svc).getService();
     		
     		startService(intent);
     	}
     	public void onServiceDisconnected(ComponentName name) {
     		sbinder = null;
     	}
     };
 
     
 //    private ServiceConnection sndSrvc = new ServiceConnection() {
 //    	public void onServiceConnected(ComponentName name, IBinder svc) {
 //    		
 //    		abinder = ((soundService.soundBinder)svc).getService();
 //    		
 //    		startService(intent);
 //    	}
 //    	public void onServiceDisconnected(ComponentName name) {
 //    		abinder = null;
 //    	}
 //    };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_display_message);
         intent = getIntent();
         //use new intent
         String message1 = intent.getStringExtra(MainActivity.SEND_MESSAGE);
         message2 = intent.getStringExtra(MainActivity.SEND_MESSAGE2);
         String message3 = intent.getStringExtra(MainActivity.minSpeedr);
       
         intent = new Intent(DisplayMessageActivity.this, GPSservice.class);
         //Start/bind GPS service
         bindService(intent, svcconnect, Context.BIND_AUTO_CREATE);
        // bindService(intent, sndSrvc, Context.BIND_AUTO_CREATE);
 
         
        
        // Log.d(null,message2);
         
         //TextView textView = new TextView(this);
         //TextView textView2 = new TextView(this);
         TextView textView1 = (TextView) findViewById(R.id.actSpeed);
         minSpeed = (TextView) findViewById(R.id.actMinSpeed);
         textView3 = (TextView) findViewById(R.id.yourSpeed);
         distance = (TextView) findViewById(R.id.distView1);
         
        
        // textViewVol = (TextView) findViewById(R.id.yourVol);
         //textView.setTextSize(40);
        textView1.setText(message1);
         minSpeed.setText(message1);
         //textView.setTextSize(40);
    //     textView2.setText(message2);
       //  textViewVol.setText("Volume");
         cSpd = "initializing...";
         textView3.setText(cSpd);
         distance.setText("initializing...");
         
         
         volControl = (SeekBar)findViewById(R.id.volCont);
         
         
         
         volControl.setOnSeekBarChangeListener(this);
         
         am = (AudioManager) getSystemService(AUDIO_SERVICE);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        //startService(new Intent(getBaseContext(), soundService.class));
        
     }
         
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_display_message, menu);
         
         startService(new Intent(getBaseContext(), soundService.class));
         //Get updated GPS stats
         
         gpsUpdate(message2);
         
         return true;
     }
    
     public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 		
 		//volume.setText(String.valueOf(progress));
 
 		am.setStreamVolume(AudioManager.STREAM_MUSIC, progress,0);
 	}
     
     public void onStartTrackingTouch(SeekBar seekBar) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 	public void onStopTrackingTouch(SeekBar seekBar) {
 		// TODO Auto-generated method stub
 		
 	}
     
     
     public void gpsUpdate(String msg) {
     	final String MSGS = msg;
     	int delay = 1000;   	
         	Timer timer = new Timer();
         	timer.scheduleAtFixedRate(new TimerTask() {
         		public void run() {
        			     currentSpeed = sbinder.getCurrentSpeed();
        			     
        			     speed = (int)currentSpeed;
        			 //    onSpeedChange(speed);
         			 cSpd = Float.toString(currentSpeed);
         			 if (cSpd.length() >4) cSpd = cSpd.substring(0, 4);
         			 totalDist = sbinder.getTotalDistance();
         			 tDist = Float.toString(totalDist);
         			 if (tDist.length() >5) tDist = tDist.substring(0, 5);
         			 
         			 //Update Textview on the UI thread
         			 UIhandler.post(new Runnable() {
         				 public void run() {
         					 //textViewVol.setText("volume");
         					 //textViewVol.refreshDrawableState();
         					
         					 textView3.setText(cSpd + " mph");
         					 textView3.refreshDrawableState();
         					 distance.setText(tDist + " miles");
         					 distance.refreshDrawableState();
         				 }
         			 });
         		}
           	},0, delay);
     }
     
     
    
     public void onStop()
     {
     	super.onStop();
     	stopService(new Intent(getBaseContext(), soundService.class));
     	stopService(new Intent(getBaseContext(), GPSservice.class));
     }
     public void onSpeedChange(int speed){
     	//if(speed > minSpeed+.5) vol = 0
     	//if(speed < mindpseed) vol = 100
     	//else{ curvolume = speed-min * 200
     	//volume = maxvol - curVol;
     	int s;
     	double minSpeed = 3.5;
     	if(speed> minSpeed+.5){
     		s = 0;
     	
     	//	String ss = Double.toString(s);
     	}else if(speed<minSpeed){
     		s = 100;
     	}else{
     		int curVolume = (int)(speed - minSpeed) *200;
     		s = 100 - curVolume;
     	}
     	// textViewVol.setText("volume");
 		 //textViewVol.refreshDrawableState();
     	//speed = speed*10;
     	am.setStreamVolume(AudioManager.STREAM_MUSIC, s ,0);
     
     }
   
 }

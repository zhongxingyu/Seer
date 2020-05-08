 package com.example.takeabreak;
 
 import android.app.Activity;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	
 	private SeekBar minutes;
 	private CountDownTimer mCountDown;
 	private TextView minutesView;
 	private TextView remainView;
 	private Button startBtn;
 	private Button stopBtn;
	private int min;
 	private boolean isCounting;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 				
 		minutes = (SeekBar)findViewById(R.id.minutes);
 		minutesView = (TextView)findViewById(R.id.minutesText);
 		remainView = (TextView)findViewById(R.id.remainText);
 		startBtn = (Button)findViewById(R.id.start);
 		stopBtn = (Button)findViewById(R.id.stop);
 		isCounting = false;
 		
 		minutes.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){  			  
 			@Override  
 			   public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 				   min = progress;
 				   if(min<=0){
 					   min = 1;					   
 				   }
 				   minutesView.setText(min + "");
 			   }
 
 			@Override
 			public void onStartTrackingTouch(SeekBar arg0) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar arg0) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 		
 		startBtn.setOnClickListener(new Button.OnClickListener(){             
 			@Override
             public void onClick(View v) {			
 				
 				if(isCounting){					
 					Toast.makeText(v.getContext(),"Counting now, please stop",Toast.LENGTH_LONG).show();				
 				}else{			
 				
 					mCountDown = new CountDownTimer(min*60*1000,1000){
 						
 			            @Override
 			            public void onFinish() {
 			                // TODO Auto-generated method stub
 			            	isCounting = false;
 			            	remainView.setText("Done!");
 			            	
 			            	Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
 			            	Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
 			            	r.play();
 			            }
 	
 			            @Override
 			            public void onTick(long millisUntilFinished) {
 			                // TODO Auto-generated method stub
 			            	long remainMin = millisUntilFinished/60/1000;
 			            	long remainSec = millisUntilFinished/1000%60;
 			            	String min = String.valueOf(remainMin);
 			            	String sec = String.valueOf(remainSec);
 			            	if(remainMin<10){
 			            		min = "0" + min;
 			            	}
 			            	if(remainSec<10){
 			            		sec = "0" + sec;
 			            	}
 			            	remainView.setText(min + ":" + sec);
 			            	isCounting = true;
 			            }
 			            
 			        }.start();
 				}
 			}
         });	 
 		
 		stopBtn.setOnClickListener(new Button.OnClickListener(){
 			@Override
             public void onClick(View v) {
 				isCounting = false;
 				mCountDown.cancel();
 				remainView.setText("00:00");
 			}		
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }

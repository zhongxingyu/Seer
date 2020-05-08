 package com.gastonnina.pomodrive;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.SystemClock;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Chronometer;
 import android.widget.Chronometer.OnChronometerTickListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	TextView lblReloj;
 	Button btnTimer;
 	CountDownTimer reloj;
 	Chronometer relojb;
	//long minute =60000;
	long minute =1000;
 	long second =1000;
	long pomodoroLength = 5*minute;//25
	long timeShortBreakLength = 3*minute;//5
	long timeLongBreakLength = 7*minute;//20
 	int timeLongBreakInterval=4;
 	long countPomodoro=0;//All pomodoros since you have started the app
 	long usedPomodoro=0;
 	long estimatedPomodoro=0;
 	long waited=0;
 	
 	Toast toast;
 	
 	boolean isFirstTime = true;
 	boolean isTimeBreak= false;
 	
 	long startTime;
     long countUp;
 	//The pomodors are less than an one hour
 	SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
 	Date rdate;
 	private pomodoro pomodoro;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         toast = new Toast(this);
         relojb = (Chronometer) findViewById(R.id.chrono);
         pomodoro = new pomodoro();
         lblReloj = (TextView)findViewById(R.id.lblReloj);
         btnTimer = (Button)findViewById(R.id.btnTimer);
         btnTimer.setOnClickListener(new View.OnClickListener() {
 
 		    @Override
 		    public void onClick(View v) {
 		    	startBucle();
 		    }
 		});
         
     }
     
     public void startBucle(){
     	if(!isTimeBreak){
     		btnTimer.setText("Stop");
     		btnTimer.setOnClickListener(new View.OnClickListener() {
 
     		    @Override
     		    public void onClick(View v) {
     		    	cancelBucle();
     		    }
     		});
     		startPomodoro(lblReloj);
     	}else{
     		
     		long modu = (countPomodoro % timeLongBreakInterval);
     		Log.d(TAG,"--"+countPomodoro+"-%--"+timeLongBreakInterval+"=="+modu);
     		if(modu == 0){
     			startLongBreak(lblReloj);//long break
         	}else{
         		startShortBreak(lblReloj);//short break
         	}
     	}
     	
     		
     }
     public void cancelBucle(){
     	btnTimer.setText("Start");
 		btnTimer.setOnClickListener(new View.OnClickListener() {
 			 @Override
  		    public void onClick(View v) {
 				 isTimeBreak=false;
 				 startBucle();
  		    }
  		});
     }
     public void contador(long...ls){
     	isFirstTime=false;
 		
     	if(ls[1]==1){
     		isFirstTime=true;
     		reloj = new CountDownTimer(ls[0], second) {
             	
                 public void onTick(long millisUntilFinished) {
                 	rdate= new Date(millisUntilFinished);
                 	lblReloj.setText(sdf.format(rdate));
                 }
 
                 public void onFinish() {
                 	//animacion
                 	////sleep(second);
                 	lblReloj.setText("00:00");
                 	countPomodoro++;
                 	startBucle();
                 }
              };	
     	}else{
     		waited=ls[0];
     		waited+=1000;
     		
     	        startTime = SystemClock.elapsedRealtime();
 
     	        relojb.getBase();
     	        relojb.setOnChronometerTickListener(new OnChronometerTickListener(){
     	            @Override
     	            public void onChronometerTick(Chronometer arg0) {
     	                //countUp = (SystemClock.elapsedRealtime() - arg0.getBase());
     	                countUp = (SystemClock.elapsedRealtime() - startTime);
     	                countUp+=1000;
     	                Log.i(TAG,"waited--"+waited+"--"+countUp);
     	                //countUp = countUp / 1000;
     	                //Log.i(TAG,"2_countUp="+countUp);
     	                //Log.i(TAG,"arg0="+arg0.getBase());
     	                if(waited>=countUp){
     	                	//rdate= new Date(countUp);
     	                	//String asText = (countUp / 60) + ":" + (countUp % 60); 
     	                    //lblReloj.setText(asText);	
     	                	rdate= new Date(countUp);
     	                	lblReloj.setText(sdf.format(rdate));
 
     	                }else{
     	                	relojb.stop();
     	                }
 	                }
     	        });
     	}
 
     	
     }
     private static final String TAG = "MainActivity";
     public void startPomodoro(View view){
     	isTimeBreak=false;
     	lblReloj.setTextAppearance(this,R.style.PomodriveTheme_yellowGoogle);
     	if(!isFirstTime)
     		reloj.cancel();
     	contador(pomodoroLength,1);
     	reloj.start();
     	isTimeBreak=true;
     }
     public void startShortBreak(View view){
     	lblReloj.setTextAppearance(this,R.style.PomodriveTheme_greenGoogle);
     	if(!isFirstTime)
     		reloj.cancel();
     	contador(timeShortBreakLength,1000);
     	relojb.start();
     	isTimeBreak=false;
     }
     public void startLongBreak(View view){
     	
     	lblReloj.setTextAppearance(this,R.style.PomodriveTheme_redGoogleAlpha);
     	if(!isFirstTime)
     		reloj.cancel();
     	contador(timeLongBreakLength,1000);
     	
     	relojb.start();
     }
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     private static class pomodoro{
     	
     	public pomodoro() {
     		super();
     		
     	}
     	public void Reloj(){
     			
     	}
         public void startShortPomodoro(View view){
         	
         }
     }
 }
 

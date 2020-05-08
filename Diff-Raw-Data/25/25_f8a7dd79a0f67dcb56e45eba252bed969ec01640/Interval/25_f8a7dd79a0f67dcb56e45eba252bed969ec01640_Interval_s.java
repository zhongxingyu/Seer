 package com.example.therunningapp;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.RadioButton;
 import android.widget.TextView;
 import android.support.v4.app.NavUtils;
 
 
 public class Interval extends Activity {
 	
 	public Timer run;
 	public Timer pause;
 	public Timer stop;
 	public boolean TimerRunStart = false;
 	public boolean TimerPauseStart = false;
 	public boolean TimerStopStart = false;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_interval);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.interval, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	//switch for radiobuttons, which only hide/show elements on the screen 
 	// based on what radiobutton is checked.
 	public void onRadioButtonClicked(View view){
 		boolean checked = ((RadioButton) view).isChecked();
 		
 		switch(view.getId()){
 			case R.id.A_radiobutton_time:
 				if(checked){
					findViewById(R.id.textView_time_interval).setVisibility(View.VISIBLE);
					findViewById(R.id.editText_time_interval).setVisibility(View.VISIBLE);
					findViewById(R.id.textView_distance_interval).setVisibility(View.GONE);
					findViewById(R.id.editText_distance_interval).setVisibility(View.GONE);
 				}break;
 			case R.id.A_radiobutton_distance:
 				if(checked){
					findViewById(R.id.textView_time_interval).setVisibility(View.GONE);
					findViewById(R.id.editText_time_interval).setVisibility(View.GONE);
					findViewById(R.id.textView_distance_interval).setVisibility(View.VISIBLE);
					findViewById(R.id.editText_distance_interval).setVisibility(View.VISIBLE);
 				}break;
 		}
 	}
 	
 	public void cancel(View view){
 		if(TimerRunStart){
 			run.cancel();
 			TimerRunStart = false;
 		}
 		if(TimerPauseStart){
 			pause.cancel();
 			TimerPauseStart = false;
 		}
 		if(TimerStopStart)
 		{
 			stop.cancel();
 			TimerStopStart = false;
 		}
 		finish();
 	}
 
 	public void IntervalThing(int RunTime, int PauseTime, int Repetition){
 		TextView tv = (TextView) findViewById(R.id.textView_test_A);
         tv.setText("Run");
         
         DelayRun(RunTime,PauseTime);
         DelayStop((RunTime+PauseTime)*(Repetition-1)+1 , 1);
         DelayStop((RunTime*Repetition)+(PauseTime*(Repetition-1)) , 2);
 	}
 	
 	public void DelayRun(final int RunTime, final int PauseTime){
 		TimerRunStart = true;
 		run = new Timer();
 		run.scheduleAtFixedRate(new TimerTask() {
 
 		    @Override
 		    public void run() {
 		    	runOnUiThread(new Runnable() {
 
 		    	    @Override
 		    	    public void run() {
 		    	        TextView tv = (TextView) findViewById(R.id.textView_test_A);
 		    	        tv.setText("Pause");
 		    	        
 		    	        DelayPause(PauseTime);
 		    	        TimerRunStart = false;
 		    	    }
 		    	});
 		    } //wait 'RunTime*1000' before it start, and loop every '(PauseTime+RunTime)*1000' (milliseconds)
 		},RunTime*1000 , (RunTime+PauseTime)*1000);
 	}
 	
 	public void DelayPause(int PauseTime){
 		TimerPauseStart = true;
 		pause = new Timer();
 		pause.schedule(new TimerTask() {
 
 		    @Override
 		    public void run() {
 		    	runOnUiThread(new Runnable() {
 
 		    	    @Override
 		    	    public void run() {
 		    	        TextView tv = (TextView) findViewById(R.id.textView_test_A);
 		    	        tv.setText("Run");
 		    	        TimerPauseStart = false;
 		    	    }
 		    	});
 		    } //wait 'PauseTime*1000' before it does something (milliseconds)
 		},PauseTime*1000);
 	}
 	
 	public void DelayStop(int Time, final int x){
 		TimerStopStart = true;
 		stop = new Timer();
 		stop.schedule(new TimerTask() {
 
 		    @Override
 		    public void run() {
 		    	runOnUiThread(new Runnable() {
 
 		    	    @Override
 		    	    public void run() {
 		    	    	
 		    	    	if(x == 1)
 		    	    		run.cancel();
 		    	    	else if(x == 2){
 		    	    		TextView tv = (TextView) findViewById(R.id.textView_test_A);
 		    	    		tv.setText("Stop");
 		    	    		TimerStopStart = false;
 		    	        }
 		    	    }
 		    	});
 		    } //wait 'Time*1000' before it does one of the things. (milliseconds)
 		},Time*1000);
 	}
 	
 	public void test(View view){
        IntervalThing(10,5,4);	
 	}
 }

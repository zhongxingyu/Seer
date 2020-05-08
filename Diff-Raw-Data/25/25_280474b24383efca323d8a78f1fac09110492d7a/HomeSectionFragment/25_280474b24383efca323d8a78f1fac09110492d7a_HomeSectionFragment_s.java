 package com.xenon.greenup;
 
 import com.xenon.greenup.util.Storage;
 import com.xenon.greenup.util.Storage.ChronoTime;
 
 import android.support.v4.app.Fragment;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Chronometer;
 import android.widget.CompoundButton;
 import android.widget.ToggleButton;
 
 public class HomeSectionFragment extends Fragment {
 	private boolean active = false;
 	private  Storage database;
 	
 	@Override
 	public void onCreate(Bundle bundle){
 		super.onCreate(bundle);
     	database = new Storage( this.getActivity() );
 	}
 	
 	@Override
 	public void onPause(){
 		super.onPause();
     	database = new Storage( this.getActivity() ); 
     	//database.setSecondsWorked( , chronoState);
 	}
 	
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
     	View rootview = inflater.inflate(R.layout.start_page, container, false);
     	final  ToggleButton startStopButton = (ToggleButton)rootview.findViewById(R.id.start_stop_button );
     	final Chronometer chrono = (Chronometer)rootview.findViewById(R.id.chronometer1);
     	
     	/* The chronometer starts off with a minimal 0:00 timer. Which is great, unless 
     	 * you're going for a uniform look between all applications and really want to 
     	 * have one specific format for all timers. Which we do. So here's the default:
     	 */
 
 		ChronoTime ct = database.getSecondsWorked();		
 		
 		/* Set Chronometer from database stuff */
 		chrono.setActivated(ct.state);
 		
		long stopped = getChronoTime(getChronoString(ct.secondsWorked ))*1000;
		Log.i("initialize view: ", ""+stopped);
 		chrono.setBase(SystemClock.elapsedRealtime() - stopped);
		//chrono.setBase(SystemClock.elapsedRealtime() - ((SystemClock.elapsedRealtime() - ct.stoppedTime ) + ct.secondsWorked));
		
 		if(ct.state){
 			/* If the chronometer is on */
 			startStopButton.setBackgroundResource(R.drawable.stop);
 			chrono.start();
 		}else{
 			startStopButton.setBackgroundResource(R.drawable.start);
 			chrono.stop();
 		}
	  	
		//Set the initial format:
		chrono.setText(getChronoString(0));
 		
     	startStopButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				active = isChecked;
 				ChronoTime ct = database.getSecondsWorked();
 				
 				long stopped = getChronoTime(getChronoString(ct.secondsWorked ))*1000;
 				chrono.setBase(SystemClock.elapsedRealtime() - stopped);
 				
 				if(isChecked) {

 					chrono.start();
 					startStopButton.setBackgroundResource(R.drawable.stop);
 				} else {  
 					startStopButton.setBackgroundResource(R.drawable.start);
 					//Set the state to false in db land
 					long gct = getChronoTime(chrono.getText().toString());
 					database.setSecondsWorked(gct, false);
 					chrono.stop();
 					chrono.setText( getChronoString(gct) );
 				}		
 			}
 		});
     	
     	chrono.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
     		/*Welcome to hell. 
     		 * The chronometer defaults to the minimal number of 0's with a minimum of 3.
     		 * So that means until you hit 10 minutes, your chronometer is just 4 characters 
     		 * long. Which is great unless you DONT want your whole UI to shift around whenever
     		 * someone happens to work a little bit longer. 
     		 */ 
 	        public void onChronometerTick(Chronometer chronometer) {
 	        	
 	        	if(active){        	
 	        		CharSequence text = chronometer.getText();
 	        		if (text.length() == 4) {
 	        			chronometer.setText("00:0"+text);
 	        		}else if (text.length()  == 5) {
 	        			chronometer.setText("00:"+text);
 	        		} else if (text.length() == 7) {
 	        			chronometer.setText("0"+text);
 	        		}
 	        		long ssw = getChronoTime(chrono.getText().toString());
 	
 	        		database.setSecondsWorked(ssw, active);
 	        	}
 	        }
 	        
 	    });
 
         return rootview;
     }
     
 	public long getChronoTime(String timeForm){
 		//The chronometer doesn't actually have a 'getTime' function. So here's one
 		String[] pieces = timeForm.split(":");
 	
 		long t=0;
 		for(int i= pieces.length-1; i >= 0; i-- ){
 			try{
 				long temp = Long.parseLong(pieces[i]);
 				t += temp * (temp == 0  ? 0 :  Math.pow(60,pieces.length - i -1)); 
 			}catch(Exception e){
 				//What did we get
 				Log.i("stupidChrono:",pieces[i]);
 			}
 		}
 		return t;
 	}
 	
 	public String getChronoString(long timeToParse){
 		long hours = timeToParse/3600;
 		long minutes = timeToParse/60 - hours*60;
 		long seconds = timeToParse - minutes*60 - hours*3600;
 		StringBuilder sb = new StringBuilder();
 		sb.append(hours > 10 ? "" : "0").append(hours).append(":");
 		sb.append(minutes > 10 ? "" : "0").append(minutes).append(":");
 		sb.append(seconds > 10 ? "" : "0").append(seconds);
 		Log.i("getChronoString",sb.toString());
 		return sb.toString();
 	}
 
 }

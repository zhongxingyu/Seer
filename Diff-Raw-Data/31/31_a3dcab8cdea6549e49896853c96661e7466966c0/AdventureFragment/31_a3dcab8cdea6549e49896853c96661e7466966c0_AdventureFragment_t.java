 package com.marbol.marbol;
 
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.Chronometer;
 import android.widget.EditText;
 import android.widget.TextView;
 
 /**
  * Fragment containing the logic to handle the "Map" Fragment
  */
 public class AdventureFragment extends Fragment implements View.OnClickListener{
 
 	private View rootView;
 	private Boolean running;
 	private long lastPause;
 	
 	private Adventure curAdventure;
 	
 	public AdventureFragment() {
 		curAdventure = null;
 		rootView = null;
 		running = false;
 		lastPause = 0;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 		Bundle savedInstanceState) {
 		rootView = inflater.inflate(R.layout.adventure_fragment, container, false);
 		
 		// set the on click listener for the start button
 		Button button = (Button)rootView.findViewById(R.id.start_adventure_button);
 		button.setOnClickListener(this);
 		
 		// set the on click listener for the stop button
 		button = (Button)rootView.findViewById(R.id.stop_adventure_button);
 		button.setOnClickListener(this);
 		
 		return rootView;
 	}
 	
 	public void onClick(View v){
 		switch(v.getId()){
 		case R.id.start_adventure_button:
 			startAdventureButtonClicked(v);
 			break;
 		case R.id.stop_adventure_button:
 			stopAdventureButtonClicked(v);
 			break;
 		default:
 			Log.e("UI", "Spurrious button click");
 		}
 		
 	}
 	private void stopAdventureButtonClicked(View v) {
 		running = false;
 		toggleUI(running);
 	}
 
 	public void startAdventureButtonClicked(View v){
 		EditText advNameEdit = (EditText)rootView.findViewById(R.id.AdventureNameEdit);
 		String newAdvName = advNameEdit.getText().toString();
 		
 		running = true;
 		toggleUI(running);
 		
 		curAdventure  = ((AdventureActivity)this.getActivity()).getCurAdventure();
 		curAdventure.setAdvName(newAdvName);
 		
 		TextView advTitle = (TextView)rootView.findViewById(R.id.adventure_title);
 		advTitle.setText(newAdvName);
 	}
 	
 	public Boolean isRunning(){
 		return this.running;
 	}
 	
 	public void toggleUI(Boolean running){
 		
 		Button startButton = (Button)rootView.findViewById(R.id.start_adventure_button);
 		Button endButton = (Button)rootView.findViewById(R.id.stop_adventure_button);
 
 		startButton.setVisibility(running ? Button.INVISIBLE : Button.VISIBLE);
 		startButton.setClickable(!running);
 		
 		endButton.setVisibility(running ? Button.VISIBLE : Button.INVISIBLE);
 		endButton.setClickable(running);
 		
 		Chronometer chrono = (Chronometer)rootView.findViewById(R.id.timer_clock);
 		if (running){
 			Log.i("CHRONO", "Setting chrono to "+this.lastPause);
 			chrono.setBase(SystemClock.elapsedRealtime() - lastPause);
 			chrono.start();
 		}
 		else{
			lastPause = convertToMili(chrono.getText().toString());
 			Log.i("CHRONO", "setting chronoBase to "+this.lastPause);
 			chrono.stop();
 		}

 		EditText adventureEdit = (EditText)rootView.findViewById(R.id.AdventureNameEdit);
 		adventureEdit.setVisibility(running ? EditText.INVISIBLE : EditText.VISIBLE);
 		
 		// all the other labels
 		TextView text = (TextView)rootView.findViewById(R.id.adventure_title);
 		text.setVisibility(running ? TextView.VISIBLE : TextView.INVISIBLE);
 		
 		text = (TextView)rootView.findViewById(R.id.total_area_label);
 		text.setVisibility(running ? TextView.VISIBLE : TextView.INVISIBLE);
 		
 		text = (TextView)rootView.findViewById(R.id.total_distance_label);
 		text.setVisibility(running ? TextView.VISIBLE : TextView.INVISIBLE);
 		
 		text = (TextView)rootView.findViewById(R.id.total_points_label);
 		text.setVisibility(running ? TextView.VISIBLE : TextView.INVISIBLE);
 		
 		// the actual values for said labels
 		text = (TextView)rootView.findViewById(R.id.points_collected_view);
 		text.setVisibility(running ? TextView.VISIBLE : TextView.INVISIBLE);
 		
 		text = (TextView)rootView.findViewById(R.id.total_area_view);
 		text.setVisibility(running ? TextView.VISIBLE : TextView.INVISIBLE);
 		
 		text = (TextView)rootView.findViewById(R.id.total_distance_view);
 		text.setVisibility(running ? TextView.VISIBLE : TextView.INVISIBLE);
 	}
	
	// convert a string containing a human readable time into miliseconds
	private long convertToMili(String time) {
		String[] split = time.split(":");
		long retval;
		// the H:MM:SS case
		if(split.length == 3){
			long hours = Long.parseLong(split[0]) * 3600;
			long minutes = Long.parseLong(split[1]) * 60;
			long seconds = Long.parseLong(split[2]);
			
			return (hours + minutes + seconds) * 1000;
		}
		else{  // just MM:SS
			long minutes = Long.parseLong(split[0]) * 60;
			long seconds = Long.parseLong(split[1]);
			return (minutes + seconds) * 1000;
		}
	}
 }

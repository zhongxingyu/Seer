 package com.marbol.marbol;
 
 import java.util.HashMap;
 
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.Chronometer;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.SeekBar;
 import android.widget.TextView;
 
 /**
  * Fragment containing the logic to handle the "Map" Fragment
  */
 public class AdventureFragment extends Fragment implements View.OnClickListener, MarbolUIFragment{
 
 	private View rootView;
 	private Boolean running;
 	private Boolean firstRun;
 	private long lastPause;
 	
 	private Adventure curAdventure;
 	private AdventureDataSource dSource;
 	private final int seekBarMax = 255;
 	private MarbolUnitConverter conv;
 	
 	private SharedPreferences prefs;
 	private int converterType;
 
 	
 	public AdventureFragment() {
 		curAdventure = null;
 		rootView = null;
 		running = false;
 		firstRun = true;
 		lastPause = 0;
 		conv = new MetricConverter();
 		converterType = 0;
 		
 		Log.i("Adventure", "Adventure Fragment created!");
 	}
 
 	private MarbolUnitConverter getConverter() {
 		if (prefs == null){
 			prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
 		}
 		
 		switch(Integer.parseInt(prefs.getString("units", "0"))){
 			case 0:
 				return new MetricConverter();
 			case 1:
 				return new ImperialConverter();
 			default:
 				Log.i("ERROR", "Unrecognized converter type");
 				return null;
 		}
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		
 		rootView = inflater.inflate(R.layout.adventure_fragment, container, false);
 		
 		// get the data source
 		dSource = new AdventureDataSource(this.getActivity());
 		
 		// set the on click listener for the start button
 		ImageButton button = (ImageButton)rootView.findViewById(R.id.start_adventure_button);
 		button.setOnClickListener(this);
 		
 		// set the on click listener for the stop button
 		final ImageButton stopButton = (ImageButton)rootView.findViewById(R.id.stop_adventure_button); 
 		stopButton.setOnClickListener(this);
 		stopButton.setColorFilter(Color.argb(seekBarMax, 0,0,0));
 		stopButton.setEnabled(false); // until we are unlocked we are disabled
 		
 		final SeekBar unlockBar = (SeekBar)rootView.findViewById(R.id.unlockSlider);
 		unlockBar.setMax(seekBarMax);
 		unlockBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 				stopButton.setColorFilter(Color.argb(seekBarMax - progress,0,0,0));
 				if (progress == seekBarMax)
 				{
 					stopButton.setEnabled(true);
 					unlockBar.setVisibility(SeekBar.INVISIBLE);
 				}
 				else
 				{
 					stopButton.setEnabled(false);
 					unlockBar.setVisibility(SeekBar.VISIBLE);
 				}
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				// reset the progress bar if the user didn't unlock us all the way
 				if (seekBar.getProgress() != seekBarMax)
 				{
 					seekBar.setProgress(0);	
 				}
 			}
 		});
 		return rootView;
 	}
 		
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState)
 	{
 		super.onActivityCreated(savedInstanceState);
 		AdventureActivity activity = (AdventureActivity)this.getActivity();
 		this.curAdventure = activity.getCurAdventure();
 		
 		EditText text = (EditText)rootView.findViewById(R.id.AdventureNameEdit);
 		text.setText(curAdventure.getAdvName());
 		
 		
 		if (!activity.isNewAdventure()){
 			firstRun = false;
 			Chronometer chrono = (Chronometer)rootView.findViewById(R.id.timer_clock);
 		
 			long seconds = convertToSeconds(curAdventure.getAdvTime());
 			chrono.setText("0:"+Long.toString(seconds));
 			lastPause = curAdventure.getAdvTime();
 			
 			chrono.setBase(SystemClock.elapsedRealtime() - lastPause);
 			
 			ImageButton stopButton = (ImageButton)rootView.findViewById(R.id.start_adventure_button);
 			//stopButton.setText("Resume Adventure");
 			stopButton.setImageResource(R.drawable.new_adventure_button_sm);
 			stopButton.setVisibility(Button.VISIBLE);
 			
 			updateUI(curAdventure);
 		}
 		else
 		{
 			firstRun = true;
 		}
 		
 		conv = getConverter();
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
 		
 		// got tell our activity that we are running
 		AdventureActivity activity = (AdventureActivity)this.getActivity();
 		activity.setRunning(false);
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
 
 		if (firstRun)
 		{
 			// open the data base
 			dSource.open();
 			// write our adventure to the db before we start recording
 			dSource.addAdventure(curAdventure);
 			dSource.close();	
 		}
 		
 		if (firstRun){
 			firstRun = false;
 		}		
 		// got tell our activity that we are running
 		AdventureActivity activity = (AdventureActivity)this.getActivity();
 		activity.setRunning(true);
 	}
 	
 	private void updateUI(Adventure adv){
 		Log.i("UPDATES", "Updating adventure fragment");
 		conv = getConverter();
 		TextView text;
 		String unit = "";
 		HashMap<String, String> units = conv.getUnit();
 		
 		double[] data = conv.convert(adv);
		if (data == null) {
			
			Log.i("ERROR", "my data is null!");
		}
 		
 		text = (TextView)rootView.findViewById(R.id.area_view);
 		text.setText(String.format("%.1f" + units.get("area_unit"), data[0]));
 		
 		text = (TextView)rootView.findViewById(R.id.distance_view);
 		text.setText(String.format("%.1f" + units.get("distance_unit"), data[3]));
 		
 		text = (TextView)rootView.findViewById(R.id.elevation_view);
 		text.setText(String.format("%.1f" + units.get("elevation_unit"), data[2]));
 		
 		text = (TextView)rootView.findViewById(R.id.speed_view);
 		text.setText(String.format("%.1f" + units.get("speed_unit"), data[1]));
 	}
 	
 	// call back for the main adventure activity to update the current adventure
 	public void updateAdventure(Adventure adv){
 		this.curAdventure = adv;
 		updateUI(curAdventure);
 	}
 	
 	public Boolean isRunning(){
 		return this.running;
 	}
 	
 	public void toggleUI(Boolean running){
 		
 		final ImageButton startButton = (ImageButton)rootView.findViewById(R.id.start_adventure_button);
 		final ImageButton stopButton = (ImageButton)rootView.findViewById(R.id.stop_adventure_button);
 
 		startButton.setVisibility(running ? Button.INVISIBLE : Button.VISIBLE);
 		startButton.setClickable(!running);
 		
 		stopButton.setVisibility(running ? Button.VISIBLE : Button.INVISIBLE);
 		stopButton.setClickable(running);
 		
 		final SeekBar unlockBar = (SeekBar)rootView.findViewById(R.id.unlockSlider);
 		
 		Chronometer chrono = (Chronometer)rootView.findViewById(R.id.timer_clock);
 		if (running){
 			Log.i("CHRONO", "Setting chrono to "+this.lastPause);
 			chrono.setBase(SystemClock.elapsedRealtime() - lastPause);
 			chrono.start();
 			
 			// if we are running set the progress bar back to 0 and make the unlock bar invisible again
 			unlockBar.setProgress(0);
 			stopButton.setColorFilter(Color.argb(seekBarMax, 0,0,0));
 		}
 		else{
 			lastPause = convertToMilli(chrono.getText().toString());
 			curAdventure.setAdvTime(lastPause);
 			Log.i("CHRONO", "setting chronoBase to "+this.lastPause);
 			chrono.stop();
 		}
 
 		final EditText adventureEdit = (EditText)rootView.findViewById(R.id.AdventureNameEdit);
 		adventureEdit.setVisibility(running ? EditText.INVISIBLE : EditText.VISIBLE);
 		
 		// all the other labels
 		TextView text = (TextView)rootView.findViewById(R.id.adventure_title);
 		text.setText(adventureEdit.getText());
 		text.setVisibility(running ? TextView.VISIBLE : TextView.INVISIBLE);
 		
 		ImageView img;
 		img = (ImageView)rootView.findViewById(R.id.area_icon);
 		img.setVisibility(running ? TextView.VISIBLE : TextView.INVISIBLE);
 		
 		img = (ImageView)rootView.findViewById(R.id.elevation_icon);
 		img.setVisibility(running ? TextView.VISIBLE : TextView.INVISIBLE);
 		
 		img = (ImageView)rootView.findViewById(R.id.speed_icon);
 		img.setVisibility(running ? TextView.VISIBLE : TextView.INVISIBLE);
 		
 		img = (ImageView)rootView.findViewById(R.id.distance_icon);
 		img.setVisibility(running ? TextView.VISIBLE : TextView.INVISIBLE);
 				
 		text = (TextView)rootView.findViewById(R.id.area_view);
 		text.setVisibility(running ? TextView.VISIBLE : TextView.INVISIBLE);
 		
 		text = (TextView)rootView.findViewById(R.id.distance_view);
 		text.setVisibility(running ? TextView.VISIBLE : TextView.INVISIBLE);
 		
 		text = (TextView)rootView.findViewById(R.id.elevation_view);
 		text.setVisibility(running ? TextView.VISIBLE : TextView.INVISIBLE);
 		
 		text = (TextView)rootView.findViewById(R.id.speed_view);
 		text.setVisibility(running ? TextView.VISIBLE : TextView.INVISIBLE);
 		
 		// lets make the layout and their strokes visible
 		LinearLayout layout = (LinearLayout)rootView.findViewById(R.id.distance_layout);
 		layout.setVisibility(running ? LinearLayout.VISIBLE : LinearLayout.INVISIBLE);
 		
 		layout = (LinearLayout)rootView.findViewById(R.id.area_layout);
 		layout.setVisibility(running ? LinearLayout.VISIBLE : LinearLayout.INVISIBLE);
 		
 		layout = (LinearLayout)rootView.findViewById(R.id.elevation_layout);
 		layout.setVisibility(running ? LinearLayout.VISIBLE : LinearLayout.INVISIBLE);
 		
 		layout = (LinearLayout)rootView.findViewById(R.id.speed_layout);
 		layout.setVisibility(running ? LinearLayout.VISIBLE : LinearLayout.INVISIBLE);
 		
 		unlockBar.setVisibility(running ? SeekBar.VISIBLE : SeekBar.INVISIBLE);
 	}
 	
 	// convert a string containing a human readable time into milliseconds
 	private long convertToMilli(String time) {
 		
 		String[] split = time.split(":");
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
 	
 	private long convertToSeconds(long milliTime){
 		return milliTime / 1000;
 	}
 
 	@Override
 	public void orientationChange(Configuration newConfig) {
 
 		SeekBar unlockBar = (SeekBar)rootView.findViewById(R.id.unlockSlider);
 		ImageButton startButton = (ImageButton)rootView.findViewById(R.id.start_adventure_button);
 		ImageButton stopButton = (ImageButton)rootView.findViewById(R.id.stop_adventure_button);
 
 		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
 			unlockBar.setTranslationX(500);
 			unlockBar.setTranslationY(-120);
 			unlockBar.setRotation(90);
 			
 			startButton.setTranslationX(500);
 			startButton.setTranslationY(-120);
 
 			stopButton.setTranslationX(500);
 			stopButton.setTranslationY(-120);
 		}
 		else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
 			unlockBar.setTranslationX(0);
 			unlockBar.setTranslationY(0);
 			unlockBar.setRotation(0);
 			
 			startButton.setTranslationX(0);
 			startButton.setTranslationY(0);
 
 			stopButton.setTranslationX(0);
 			stopButton.setTranslationY(0);
 		}	
 	}
 }

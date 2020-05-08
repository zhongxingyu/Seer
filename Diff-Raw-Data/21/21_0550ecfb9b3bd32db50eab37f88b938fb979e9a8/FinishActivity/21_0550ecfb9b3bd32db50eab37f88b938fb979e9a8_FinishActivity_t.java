 package de.freinsberg.pomecaloco;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class FinishActivity extends Activity {	
 	
//	private Context mContext;
	private DBHelper mDbHelper;
 	private Button go_to_results;
 	private Button new_race;	
 	private TextView finish_track;
 	private TextView finish_mode;
 	private TextView finish_heading;
 	private TextView finish_name;
 	private TextView finish_attempt;
 	private TextView finish_meters;
 	private TextView finish_fastest;
 	private TextView finish_avg_speed;
 	private TextView finish_timemode_driven_rounds;
 	private TextView finish_roundmode_driven_time;
 	private TextView finish_timemode_driven_rounds_header;
 	private TextView finish_roundmode_driven_time_header;
 	private TextView finish_right_name;
 	private TextView finish_right_attempt;
 	private TextView finish_right_meters;
 	private TextView finish_right_fastest;
 	private TextView finish_right_avg_speed;
 	private TextView finish_right_timemode_driven_rounds;
 	private TextView finish_right_roundmode_driven_time;
 	private TextView finish_left_name;
 	private TextView finish_left_attempt;
 	private TextView finish_left_meters;
 	private TextView finish_left_fastest;
 	private TextView finish_left_avg_speed;
 	private TextView finish_left_timemode_driven_rounds;
 	private TextView finish_left_roundmode_driven_time;
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {		
 		super.onCreate(savedInstanceState);
		mDbHelper = new DBHelper(this);
		mDbHelper.openDB();
 		int lane;
 		if(ObjectDetector.getInstance().car_status() != ObjectDetector.BOTH_CAR)
 		{			
 			if(ObjectDetector.getInstance().car_status() == ObjectDetector.LEFT_CAR){
 				lane = Race.LEFT_LANE;
 				Log.i("debug","Finish for left Player!");
 			}
 			else{
 				lane = Race.RIGHT_LANE;
 				Log.i("debug","Finish for right Player!");
 			}
 			
 			setContentView(R.layout.finish_oneplayer);
 	        //Setting Views and doing stuff for One-Player-Game	 
 			initPlaymodeIndependentViews();
 	        finish_heading = (TextView) findViewById(R.id.finish_heading);
 	        finish_attempt = (TextView) findViewById(R.id.finish_attempt_view);
 	        finish_name = (TextView) findViewById(R.id.finish_name_view);
 	        finish_meters = (TextView) findViewById(R.id.finish_meters_view);
 	        finish_fastest = (TextView) findViewById(R.id.finish_fastest_view);
 	        finish_avg_speed = (TextView) findViewById(R.id.finish_avg_speed_view);
 
 	        Log.i("debug", "Initialized 1 Player Views");
 	        finish_heading.setText("Einzelspieler");
 	        finish_name.setTextColor(getResources().getColor(R.color.white));
 	        finish_name.setText(Race.getInstance().getPlayerName(0));
 	        
 	        if(Race.getInstance().getGameMode() == Race.ROUND_MODE){        	
 	        	finish_mode.setText("Rundenrennen");
 	        	finish_roundmode_driven_time_header = (TextView) findViewById(R.id.finish_roundmode_driven_time);
 	        	finish_roundmode_driven_time_header.setVisibility(View.VISIBLE);
 		        finish_roundmode_driven_time = (TextView) findViewById(R.id.finish_roundmode_driven_time_view);
 	        	finish_roundmode_driven_time.setVisibility(View.VISIBLE);
 	        	finish_roundmode_driven_time.setText(Race.getInstance().getFinishedTime());
 	        }
 	        else{
 	        	finish_mode.setText("Zeitfahren");
 	        	finish_timemode_driven_rounds_header = (TextView) findViewById(R.id.finish_timemode_driven_rounds);
 	        	finish_timemode_driven_rounds_header.setVisibility(View.VISIBLE);
 	        	finish_timemode_driven_rounds = (TextView) findViewById(R.id.finish_timemode_driven_rounds_view);
 	        	finish_timemode_driven_rounds.setVisibility(View.VISIBLE);
 	        	finish_timemode_driven_rounds.setText(""+Race.getInstance().getCurrentRound(lane));
 	        }        
 	        
 	        finish_attempt.setTextColor(getResources().getColor(R.color.white));
	        finish_attempt.setText(""+mDbHelper.getPlayerTrackAttempt(Race.getInstance().getPlayerName(0), Race.getInstance().getTrackName(), Race.getInstance().getGameMode()));
 	        finish_meters.setTextColor(getResources().getColor(R.color.white));
 	        finish_meters.setText(""+Race.getInstance().getDrivenMeters(lane));
 	        finish_fastest.setTextColor(getResources().getColor(R.color.white));
 	        finish_fastest.setText(Race.getInstance().getFastestRound(lane));
 	        finish_avg_speed.setTextColor(getResources().getColor(R.color.white));
 	        finish_avg_speed.setText(String.format("%.2f",Race.getInstance().getAvgSpeed(lane))+" m/s");	        
 		}
 		else
 		{
 			Log.i("debug","Finish for Two- Player!");
 			setContentView(R.layout.finish_twoplayer);
 			initPlaymodeIndependentViews();
 			//Setting Views  and doing stuff for Two-Player-Game
 			finish_heading = (TextView) findViewById(R.id.finish_heading);
 	        //Views for the Left Lane
 			finish_left_name = (TextView) findViewById(R.id.finish_left_name);
 	        finish_left_attempt = (TextView) findViewById(R.id.finish_left_attempt_view);
 	        finish_left_meters = (TextView) findViewById(R.id.finish_left_meters_view);
 	        finish_left_fastest = (TextView) findViewById(R.id.finish_left_fastest_view);
 	        finish_left_avg_speed = (TextView) findViewById(R.id.finish_left_avg_speed_view);
 	        finish_left_timemode_driven_rounds = (TextView) findViewById(R.id.finish_left_timemode_driven_rounds_view);
 	        finish_left_roundmode_driven_time = (TextView) findViewById(R.id.finish_left_roundmode_driven_time_view);	        
 	        //Views for the Right Lane
 	        finish_right_name = (TextView) findViewById(R.id.finish_right_name);
 	        finish_right_attempt = (TextView) findViewById(R.id.finish_right_attempt_view);
 	        finish_right_meters = (TextView) findViewById(R.id.finish_right_meters_view);
 	        finish_right_fastest = (TextView) findViewById(R.id.finish_right_fastest_view);
 	        finish_right_avg_speed = (TextView) findViewById(R.id.finish_right_avg_speed_view);
 	        finish_right_timemode_driven_rounds = (TextView) findViewById(R.id.finish_right_timemode_driven_rounds_view);
 	        finish_right_roundmode_driven_time = (TextView) findViewById(R.id.finish_right_roundmode_driven_time_view);
 	        Log.i("debug", "Initialized 2 Player Views");
 	        finish_heading.setText("Mehrspieler");
 	        finish_left_name.setText(Race.getInstance().getPlayerName(0));
 	        finish_right_name.setText(Race.getInstance().getPlayerName(1));
 	        if(Race.getInstance().getGameMode() == Race.ROUND_MODE){        	
 	        	finish_mode.setText("Rundenrennen");
 	        	finish_roundmode_driven_time_header = (TextView) findViewById(R.id.finish_roundmode_driven_time);
 	        	finish_roundmode_driven_time_header.setVisibility(View.VISIBLE);
 	        	finish_right_roundmode_driven_time.setVisibility(View.VISIBLE);
 	        	finish_right_roundmode_driven_time.setText(Race.getInstance().getFinishedTime());
 	        	finish_left_roundmode_driven_time.setVisibility(View.VISIBLE);
 	        	finish_left_roundmode_driven_time.setText(Race.getInstance().getFinishedTime());
 	        }
 	        else{
 	        	finish_mode.setText("Zeitfahren");
 	        	finish_timemode_driven_rounds_header = (TextView) findViewById(R.id.finish_timemode_driven_rounds);
 	        	finish_timemode_driven_rounds_header.setVisibility(View.VISIBLE);
 	        	finish_left_timemode_driven_rounds.setVisibility(View.VISIBLE);
 	        	finish_left_timemode_driven_rounds.setText(""+Race.getInstance().getCurrentRound(Race.LEFT_LANE));
 	        	finish_right_timemode_driven_rounds.setVisibility(View.VISIBLE);
 	        	finish_right_timemode_driven_rounds.setText(""+Race.getInstance().getCurrentRound(Race.RIGHT_LANE));
 	        }
 	        Log.i("debug", "Initialized 2 Player Views 50%");
 	        finish_left_attempt.setTextColor(getResources().getColor(R.color.white));
 	        finish_left_attempt.setText(""+Race.getInstance().getAttempt(Race.LEFT_LANE));
 	        Log.i("debug", "Initialized 2 Player Views 51%");
 	        finish_left_meters.setTextColor(getResources().getColor(R.color.white));
 	        finish_left_meters.setText(""+Race.getInstance().getDrivenMeters(Race.LEFT_LANE));
 	        Log.i("debug", "Initialized 2 Player Views 52%");
 	        finish_left_fastest.setTextColor(getResources().getColor(R.color.white));
 	        finish_left_fastest.setText(Race.getInstance().getFastestRound(Race.LEFT_LANE));
 	        Log.i("debug", "Initialized 2 Player Views 53%");
 	        finish_left_avg_speed.setTextColor(getResources().getColor(R.color.white));
 	        finish_left_avg_speed.setText(String.format("%.2f",Race.getInstance().getAvgSpeed(Race.LEFT_LANE))+" m/s");
 	        Log.i("debug", "Initialized 2 Player Views 54%");
 	        
 	        finish_right_attempt.setTextColor(getResources().getColor(R.color.white));
 	        finish_right_attempt.setText(""+Race.getInstance().getAttempt(Race.RIGHT_LANE));
 	        Log.i("debug", "Initialized 2 Player Views 55%");
 	        finish_right_meters.setTextColor(getResources().getColor(R.color.white));
 	        finish_right_meters.setText(""+Race.getInstance().getDrivenMeters(Race.RIGHT_LANE));
 	        Log.i("debug", "Initialized 2 Player Views 56%");
 	        finish_right_fastest.setTextColor(getResources().getColor(R.color.white));
 	        finish_right_fastest.setText(Race.getInstance().getFastestRound(Race.RIGHT_LANE));
 	        Log.i("debug", "Initialized 2 Player Views 57%");
 	        finish_right_avg_speed.setTextColor(getResources().getColor(R.color.white));
 	        finish_right_avg_speed.setText(String.format("%.2f",Race.getInstance().getAvgSpeed(Race.RIGHT_LANE))+ " m/s");
 	        Log.i("debug", "Initialized 2 Player Views 58%");
 	        
 		}		
 		
  
         Log.i("debug", "Initialized Playerunabhängige Views");      
         finish_track.setText(Race.getInstance().getTrackName());        
         
         go_to_results.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	Intent intent = new Intent(v.getContext(), SettingsFragmentActivity.class);     
             	startActivity(intent);  
             	
             }
         });	        
 
 		new_race.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Log.i("debug", "start a new race");
 				Intent intent = new Intent().setClass(v.getContext(), StartActivity.class);
 				startActivity(intent);     
 				finish();
 			}
 		});	
 	}
 	
 	
 	private void initPlaymodeIndependentViews(){
 		//Setting Views that are Inflated wether one or Two-Player Game
 		go_to_results = (Button) findViewById(R.id.go_to_results);		
         new_race = (Button) findViewById(R.id.new_race);
         finish_track = (TextView) findViewById(R.id.finish_track_view);
         finish_mode = (TextView) findViewById(R.id.finish_mode_view);  
 	}
 	
 	@Override
     public void onBackPressed() {
 		new AlertDialog.Builder(this)
 			.setTitle("Beenden?")
 			.setMessage("Wollen Sie die App beenden?")
         	.setCancelable(false)
         	.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
         		public void onClick(DialogInterface dialog, int id) {
         			finish();
         		}
         	})
         	.setNegativeButton("Abbrechen", null)
         	.show();
     }
 }

 package edu.virginia.cs.sgd.gpsgames;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import com.google.android.gms.maps.model.LatLng;
 
 public class RaceSetupActivity extends Activity {
 
 	LatLng start;
 	LatLng end;
 	
 	Button selectStart;
 	Button selectEnd;
 	Button createGame;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_race_setup);
 		// Show the Up button in the action bar.
 		
 		setUpUI();
 		
 
 		selectEnd.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				pickStart();
 			}
 		});
 		
 		selectStart.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				pickEnd();
 			}
 		});
 		
 		createGame.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				createGame();
 			}
 		});
 		
 		getPoints();
 	}
 
 	public void setUpUI(){
 		selectStart = (Button) findViewById(R.id.pick_start);
 		selectEnd = (Button) findViewById(R.id.pick_end);
 		createGame = (Button) findViewById(R.id.create_game);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_race_setup, menu);
 		return true;
 	}
 	
 	public void moveToSelectActivity(String name) {
 		//REPLACE MenuActivity.clss BELOW WITH CLASS YOU WANT TO GO TO
 		Intent mainActivity = new Intent(this, SelectPointActivity.class);
 		mainActivity.putExtra("Name", name);
 		startActivity(mainActivity);
 	}
 	
 	public void pickStart() {
 		moveToSelectActivity("Start");
 	}
 
 	public void pickEnd() {
 		moveToSelectActivity("End");
 	}
 
 	private void getPoints() {
 		Bundle ex = getIntent().getExtras();
 		
		
		String name = (ex == null ? null : ex.getString("Name"));
 		
 		if(name == null) {
 			return;
 		}
 		else if(name.equals("Start")) {
 			double[] coords = ex.getDoubleArray("Point");
 			start = new LatLng(coords[0], coords[1]);
 		}
 		else if(name.equals("End")) {
 			double[] coords = ex.getDoubleArray("Point");
 			end = new LatLng(coords[0], coords[1]);
 		}
 		else {
 			
 		}
 		
 	}
 
 	public void createGame() {
 		if(start == null || end == null) {
 			// TODO Error
 			return;
 		}
 		
 		
 	}
 	
 }

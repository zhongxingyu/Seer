 package com.chalmers.schmaps;
 
 import android.os.Bundle;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.LightingColorFilter;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 
 public class MenuActivity extends Activity implements View.OnClickListener {
 	private static final int MICROWAVEBUTTON = 1;
 	private static final int RESTAURANTBUTTON = 2;
 	private static final int ATMBUTTON = 3;
 	private static final int LECTUREHALLBUTTON = 4;
 	private static final int BOOKINGKEY = 5;
 	private static final int BUSKEY = 6;
 
 	private Intent startMapActivity;
 	private Button searchHall;
 	private Button microwaveButton;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_menu);
 		assignInstances();
 	}
 
 	private void assignInstances() {
 		searchHall = (Button) findViewById(R.id.searchHallButton);
 		searchHall.setOnClickListener(this);
 		microwaveButton = (Button) findViewById(R.id.microwaveButton);
 		microwaveButton.setOnClickListener(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_menu, menu);
 		return true;
 	}
 
 	/**
 	 * onClick method for determining which activity will start through the use of view ID's and
 	 * a switch case to start correct activity with correct variables.
 	 */
 	public void onClick(View v) {
 
 		switch(v.getId()){
 		
 		case R.id.searchHallButton:
 			searchHall.getBackground().setColorFilter(new LightingColorFilter(0x000000, 0x4682b4)); //graphics for the button
 			startMapActivity = new Intent("android.intent.action.GOOGLEMAPACTIVITY");
 			startMapActivity.putExtra("Show searchfield", true);
 			startActivity(startMapActivity);	
 			break;
 		
 		case R.id.microwaveButton:
 			microwaveButton.getBackground().setColorFilter(new LightingColorFilter(0x000000, 0x4682b4)); //graphics for the button
 			startMapActivity = new Intent("android.intent.action.CAMPUSMENUACTIVITY");
 			startMapActivity.putExtra("Show locations", MICROWAVEBUTTON);
 			startActivity(startMapActivity);
			
 
 		}
 	}
 }

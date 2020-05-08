 package com.chalmers.schmaps;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 
 /**
 *CampusMenu was created to act as an intermediary between the MenuActivity and GoogleMapShowLocation
 *if you've pressed a button to show locations in the areas Johanneberg or Lindholmen. 
 *We have identifiers "JOHANNEBERG" and "LINDHOLMEN" so that when we've pressed the buttons, we can
 *send the identifier to the next Activity for further purposes.
 */
 
 public class CampusMenuActivity extends Activity implements View.OnClickListener {
 	private static final int JOHANNESBERG = 40; 		//identifier for Johanneberg
 	private static final int LINDHOLMEN = 42;			// -||- for Lindholmen
 	
 	private Intent startMapActivity;
 	private Button johannebergButton;
 	private Button lindholmenButton;
 	private Bundle menuActionChosen;
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_campus_menu);
         assignInstances();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_campus_menu, menu);
         return true;
     }
     
 	private void assignInstances() {
 		johannebergButton = (Button) findViewById(R.id.johannebergButton);
 		johannebergButton.setOnClickListener(this);
 		lindholmenButton = (Button) findViewById(R.id.lindholmenButton);
 		lindholmenButton.setOnClickListener(this);
 		menuActionChosen = getIntent().getExtras();
 	}
 	
 	/**
 	*onClick(View arg0) uses a switchcase for the buttons to know which identifiers should be set to
 	*the next activity. We send one two identifiers to the next Activity, one to zoom in the correct
 	*area and one, which we get from the previous Activity, to know which table should be used in
 	*the database and mark the correct locations in the area.
 	*/
 	public void onClick(View arg0) {
 		switch (arg0.getId())
 		{
 		case R.id.johannebergButton:
 			startMapActivity = new Intent("android.intent.action.GOOGLEMAPSHOWLOCATION");
 			startMapActivity.putExtra("Campus", JOHANNESBERG);
 			//Transfer what kind of locations should be drawn
 			startMapActivity.putExtra("Show locations", menuActionChosen.getInt("Show locations"));
 			startActivity(startMapActivity);
 			break;
 			
 		case R.id.lindholmenButton:
 			startMapActivity = new Intent("android.intent.action.GOOGLEMAPSHOWLOCATION");
 			startMapActivity.putExtra("Campus", LINDHOLMEN);
 			//Transfer what kind of locations should be drawn
 			startMapActivity.putExtra("Show locations", menuActionChosen.getInt("Show locations"));
 			startActivity(startMapActivity);
 			break;
 		}
 	}
 }

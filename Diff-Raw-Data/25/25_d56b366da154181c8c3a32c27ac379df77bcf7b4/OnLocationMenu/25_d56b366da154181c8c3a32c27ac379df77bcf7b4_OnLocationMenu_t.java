 package com.example.tourapp;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 
 public class OnLocationMenu extends Activity implements OnClickListener {
 	public ArrayList<DivisionObject> listOfDivisions;
 	public ArrayList<PlaceObject> listOfPlaces;
	private static final int TOUR_START_LOCATION = 0; //the guided tour starts at the first object
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.onlocationmenu);
 
 		findViewById(R.id.button_guided_tour).setOnClickListener(this);
 		findViewById(R.id.button_division_list).setOnClickListener(this);
 		findViewById(R.id.button_division_1).setOnClickListener(this);
 		
 		Bundle extras = getIntent().getExtras();
 		listOfDivisions = extras.getParcelableArrayList("com.example.tourapp.divisionArrayList");
 		listOfPlaces = extras.getParcelableArrayList("com.example.tourapp.placeArrayList");
 		
 		TextView test = (TextView) findViewById(R.id.onlocationtest);
 		
 		String testManualArray = "";
 		for (DivisionObject obj:listOfDivisions){
 			testManualArray+= obj.getName() + "\n";
 		}
 		
 		test.setText(testManualArray);
 		
 	}//end method onCreate
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()){
 		case R.id.button_guided_tour:
			GuidedTour.mCurrentPlaceId = TOUR_START_LOCATION;
 			Intent i1 = new Intent (this, GuidedTour.class);
 			i1.putParcelableArrayListExtra("com.example.tourapp.divisionArrayList", listOfDivisions);
 			i1.putParcelableArrayListExtra("com.example.tourapp.placeArrayList", listOfPlaces);
 			startActivity(i1);
 			break;
 		case R.id.button_division_list:
 			Intent i2 = new Intent (this, DivisionList.class);
 			i2.putParcelableArrayListExtra("com.example.tourapp.divisionArrayList", listOfDivisions);
 			i2.putParcelableArrayListExtra("com.example.tourapp.placeArrayList", listOfPlaces);
 			startActivity(i2);
 			break;
 		case R.id.button_division_1:
 			Intent i3 = new Intent (this, Division.class);
 			i3.putParcelableArrayListExtra("com.example.tourapp.divisionArrayList", listOfDivisions);
 			i3.putParcelableArrayListExtra("com.example.tourapp.placeArrayList", listOfPlaces);
 			startActivity(i3);
 			break;
 		}//end switch statement
 	}//end method onClick
 }//end OnLocationMenu class

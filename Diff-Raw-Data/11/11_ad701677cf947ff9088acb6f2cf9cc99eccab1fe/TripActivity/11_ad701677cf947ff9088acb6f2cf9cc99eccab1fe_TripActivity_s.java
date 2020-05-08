 package com.example.transitlogger;
 
 import com.example.transitlogger.model.Distance;
 import com.example.transitlogger.model.Trip;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class TripActivity extends Activity {
 	private Trip trip;
 	private TextView distanceText;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_trip);
 		
 		//Intent intent = getIntent();
 		// TODO: accept specific trip IDs
 
 		distanceText = (TextView) findViewById(R.id.distanceText);
		updateDistance();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.trip, menu);
 		return true;
 	}
 	
 	class EndTripClickListener implements OnClickListener {
 		private TripActivity tripActivity;
 
 		public EndTripClickListener(TripActivity tripActivity) {
 			this.tripActivity = tripActivity;
 		}
 		
 		public void onClick(View v) {
 			tripActivity.onEndTrip(v);
 		}
 	}
 	
 	public void onEndTrip(View view) {
 		;
 	}
 	
 	public void updateDistance() {
 		Distance distance = trip.getDistance();
 		distanceText.setText(distance.getKilometers() + " km");
 	}
 	
 	public void onStartTrip(View view) {
 		// Create a new trip.
 		trip = new Trip();
 		// Change the button to "End Trip"
 		
 
 		Button startStopTripButton = (Button) findViewById(R.id.startStopTripButton);
 		startStopTripButton.setText("@string/end_trip");
 		startStopTripButton.setOnClickListener(new EndTripClickListener(this));
 	}
 }

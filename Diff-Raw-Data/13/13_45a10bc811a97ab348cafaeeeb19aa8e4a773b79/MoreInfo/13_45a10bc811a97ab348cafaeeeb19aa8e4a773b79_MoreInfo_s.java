 package jre.pathtimes;
 
 import java.util.Calendar;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.TextView;
 
 public class MoreInfo extends Activity {
 
 	/**
 	 * Handles creating the more info object.
 	 * 
 	 * @param savedInstanceState the saved bundle state.
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		Intent intent = getIntent();
 		
 		Station startingStation = Station.findById(intent.getExtras().getInt("startingStationId"));
 		Station endingStation = Station.findById(intent.getExtras().getInt("endingStationId"));
 		
 		setContentView(R.layout.moreinfo);
 		
 		TextView title = (TextView) findViewById(R.id.moreInfoTitle);
 		TextView travelTime = (TextView) findViewById(R.id.travelTime);
 		TextView address = (TextView) findViewById(R.id.address);
 		
 		List<TrainLine> trainLine = ScheduleUtil.findAppropriateTrainLines(startingStation, 
 				endingStation, Calendar.getInstance());
 		
 		title.setText(startingStation.getName() + " to " + endingStation.getName());
 		
		travelTime.setText("Travel is " +trainLine.get(0).getTimeBetweenStations(startingStation, endingStation) 
				+ " minutes.");
 		
 		address.setText("Location: " + startingStation.getAddress());
 		
 	}
 	
 }

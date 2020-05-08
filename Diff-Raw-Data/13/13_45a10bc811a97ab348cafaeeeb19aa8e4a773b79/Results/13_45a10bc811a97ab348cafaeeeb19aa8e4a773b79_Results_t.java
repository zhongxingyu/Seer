 package jre.pathtimes;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class Results extends ListActivity {
 
 	/**
 	 * The callback for the activity's response.
 	 */
 	private static final int ACTIVITY_RESPONSE_CALLBACK = 2344;
 	
 	/**
 	 * The starting train station
 	 */
 	Station startingStation = null;
 	
 	/**
 	 * The ending train station.
 	 */
 	Station endingStation = null;
 	
 	/**
 	 * Displays the results of the trip
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.results);
 		
 		Intent intent = getIntent();
 		startingStation = Station.findById(intent.getExtras().getInt("startingStationId"));
 		endingStation = Station.findById(intent.getExtras().getInt("endingStationId"));
 		
 		List<String> arrivalTimeStrings = new ArrayList<String>();
 		TextView textView = (TextView) findViewById(R.id.resultsHeader);
 		textView.setText(startingStation.getName() + " to " + endingStation.getName());
 		
 		List<Calendar> nextArrivalTimes = ScheduleUtil.getNextArrivalTimes(startingStation, endingStation, Calendar.getInstance(), 6);
 		
		if (nextArrivalTimes == null || nextArrivalTimes.size() == 0) {
 			arrivalTimeStrings.add("Unable to find train line.");
 			arrivalTimeStrings.add("This app currently does not support transfers.");
 			arrivalTimeStrings.add("Choose a destination with a train line currently running.");
 		} else {
 		
 			SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
 			
 			for (Calendar time : nextArrivalTimes) {
 				long difference = time.getTime().getTime() - new Date().getTime();
 				
 				int minutes = (int) (difference / 60000);  // Convert to minutes.
 				
 				arrivalTimeStrings.add(format.format(time.getTime()) + "  (in " + minutes + " min)");
 			}
 		}
 		
 		setListAdapter(new ArrayAdapter<String>(this, R.layout.result_item, arrivalTimeStrings.toArray(new String[arrivalTimeStrings.size()])));
 		
 	}
 	
 	/**
 	 * Handle the more info button
 	 */
 	public void selectMoreInfo(View view) {
         Intent intent = new Intent(".MoreInfo");
         intent.putExtra("startingStationId", startingStation.getId());
         intent.putExtra("endingStationId", endingStation.getId());
 
 		startingStation = null;
 		endingStation = null;
         
 		startActivityForResult(intent, ACTIVITY_RESPONSE_CALLBACK);
 	}
 }

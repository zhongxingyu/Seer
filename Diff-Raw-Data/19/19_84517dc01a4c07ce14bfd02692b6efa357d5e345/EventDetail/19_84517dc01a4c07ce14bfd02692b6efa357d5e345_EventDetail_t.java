 package no.ntnu.stud.fallprevention;
 
 import java.util.Map;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 
 public class EventDetail extends Activity {
 	
 	private int eventId; 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_eventdetail);
 
 		// Get the event type and ID from the intent
 		Intent motherIntent = getIntent();
 		eventId = motherIntent
 				.getIntExtra("no.ntnu.stud.fallprevention.ID", -1);
 		
 		// Fetch description and headline from database
 		Map<String, String> eventInformation = new DatabaseHelper(this).dbGetEventInfo(eventId);
 		
 		// Fill in information
 		TextView textView = (TextView) findViewById(R.id.headlineTextView);
 		textView.setText(eventInformation.get(DatabaseContract.EventType.COLUMN_NAME_TITLE));
 		TextView textView2 = (TextView) findViewById(R.id.mainTextView);
 		textView2.setText(eventInformation.get(DatabaseContract.EventType.COLUMN_NAME_DESCRIPTION));
 	}
 	
 	public void fireDeleteButton(View view) {
 		// Delete event from database
 		new DatabaseHelper(this).dbDeleteEvent(eventId);
 		// Then go back to EventList screen
 		Intent intent = new Intent(this, EventList.class);
 		startActivity(intent);
 	}
 	
 	public void fireKeepButton(View view) {
 		// Go back to EventList screen
 		Intent intent = new Intent(this, EventList.class);
 		startActivity(intent);
 	}
 }

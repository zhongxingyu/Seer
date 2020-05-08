 package no.ntnu.stud.fallprevention;
 
 import java.util.List;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class EventList extends ListActivity {
 
 	List<Event> events;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		events = new DatabaseHelper(this).dbGetEventList();
 		
 		// Display the information
 		setListAdapter(new EventListAdapter(this, events));
 		
 		// Display a message if there are no events in the queue
 		if (events.isEmpty()) {
 			Toast.makeText(this, getString(R.string.no_events), Toast.LENGTH_LONG).show();
 		}
 	}
 
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_eventlist, menu);
		return true;
	}*/

 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		Intent intent = new Intent(this, EventDetail.class);
 		intent.putExtra("no.ntnu.stud.fallprevention.ID", events.get(position).getId());
 		startActivity(intent);
 	}
 
 }

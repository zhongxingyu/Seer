 package fi.local.social.network.activities;
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import fi.local.social.network.R;
 
 public class PeopleActivity extends ListActivity {
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		
 		String[] mockup_values=new String[]{"Alex Yang","Tom Cruise","Tom Hanks","Jason Stathon","Joe Hu"};
 		
 		setListAdapter((ListAdapter) new ArrayAdapter<String>(this, R.layout.people_item, R.id.label, mockup_values));
 		//ListView listView = getListView();
 		//listView.setTextFilterEnabled(true);
 	}
 	
 	@Override
 	protected void onListItemClick(ListView l, View view, int position, long id) {
 		startActivity(new Intent(getApplicationContext(), ChatActivity.class));
 		//Toast.makeText(getApplicationContext(),((TextView) view).getText(), Toast.LENGTH_SHORT).show();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu){
 		MenuInflater inflater=getMenuInflater();
 		inflater.inflate(R.layout.menu_people_nearby, menu);
 		return true;
 	}
 	
 	//When a user clicks on an option menu item, a toast with the item title shows up
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		// Handle item selection
 	    switch (item.getItemId()) {
 	        case R.id.friends:
 	            Toast.makeText(getApplicationContext(),"You choose option menu item: "+item.getTitle(), Toast.LENGTH_SHORT).show();
 	            return true;
 	        case R.id.event_list:
 	        	//Toast.makeText(getApplicationContext(),"You choose option menu item: "+item.getTitle(), Toast.LENGTH_SHORT).show();
 	        	startActivity(new Intent(getApplicationContext(), EventsActivity.class));
 	            return true;
 	        case R.id.groups:
 	        	Toast.makeText(getApplicationContext(),"You choose option menu item: "+item.getTitle(), Toast.LENGTH_SHORT).show();
 	        	return true;
 	        case R.id.settings:
	        	Toast.makeText(getApplicationContext(),"You choose option menu item: "+item.getTitle(), Toast.LENGTH_SHORT).show();
 	        	return true;
 	        case R.id.new_event:
 	        	startActivity(new Intent(getApplicationContext(), NewEventActivity.class));
 	        	//Toast.makeText(getApplicationContext(),"You choose option menu item: "+item.getTitle(), Toast.LENGTH_SHORT).show();
 	        	return true;
 	        default:
 	        	break;
 	    }
 	    return false;
 	}
 }

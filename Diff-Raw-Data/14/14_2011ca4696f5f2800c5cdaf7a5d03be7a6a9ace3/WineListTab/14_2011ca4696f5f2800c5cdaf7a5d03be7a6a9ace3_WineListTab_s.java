 package watsons.wine;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class WineListTab extends Activity {
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		
 		setContentView(R.layout.winelist_tab);
 		/* Wine List Tab Content */
 		ListView listView = (ListView) findViewById(R.id.list_wine);
 		String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
		  "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
		  "Linux", "OS/2" };
 
 		// Define a new Adapter
 		// First parameter - Context
 		// Second parameter - Layout for the row
 		// Third parameter - ID of the TextView to which the data is written
 		// Forth - the Array of data
 		
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 		  R.layout.list_wine_item, R.id.list_wineitem_text, values);
 
 		// Assign adapter to ListView
 		listView.setAdapter(adapter);
 		listView.setDividerHeight(0);
 
 	}
 }

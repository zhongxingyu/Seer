 package softeng206.A3;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.support.v4.app.NavUtils;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CursorAdapter;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	private Button add;
 	private Spinner spinner;
 	private ListView listView;
 	private ImageView image;
 	private EditText searchBar;
 	private Builder dialogBuilder;

 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		add = (Button) findViewById(R.id.action_add);
 		listView = (ListView) findViewById(R.id.names);
//		image = (ImageView) findViewById(R.id.contact_photo);
 		searchBar = (EditText) findViewById(R.id.searchBar);
 		spinner = (Spinner) findViewById(R.id.action_sort_spinner);
 
 		setUpListView();
 	}
 
 	public void setUpListView() {
		
		ContactDatabaseHelper helper = new ContactDatabaseHelper(this);
		CustomCursorAdapter adapter = new CustomCursorAdapter(this, helper.getAllData());
 		listView.setAdapter(adapter);
 		listView.setOnItemClickListener(new listItemClickedListener());
 	}
 
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.action_add:
 			Intent intent = new Intent();
 			intent.setClass(MainActivity.this, NewContact.class);
 			startActivity(intent);
 			return true;
 		case R.id.action_sort_spinner:
 			dialogBuilder = new AlertDialog.Builder(MainActivity.this);
 			dialogBuilder.setTitle("Sort Contacts By");
 			//dialogBuilder.setMessage("Unsaved information will be lost!");
 			
 			dialogBuilder.setNegativeButton("First name", null);
 			dialogBuilder.setNeutralButton("Last name", null);
 			dialogBuilder.setPositiveButton("phone", null);
 			dialogBuilder.setCancelable(true);
 			dialogBuilder.create().show();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		setTitle(R.string.title_main); // set title of action bar.
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	class listItemClickedListener implements AdapterView.OnItemClickListener {
 		public void onItemClick(AdapterView<?> parentView, View childView, int childViewPosition, long id) {
 			
			
 			Intent intent = new Intent();
 			intent.setClass(MainActivity.this, SingleContact.class);
 			intent.putExtra("the_key", id);
 			startActivity(intent);
 
 		}
 	}
 }

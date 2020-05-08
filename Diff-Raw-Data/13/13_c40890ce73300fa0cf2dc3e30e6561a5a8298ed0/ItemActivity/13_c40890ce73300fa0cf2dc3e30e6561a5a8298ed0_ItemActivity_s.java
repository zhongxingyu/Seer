 package edu.pitt.cs1635group3;
 
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class ItemActivity extends Activity {
 
 	private Item item, prevItem, nextItem;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.item);
 
 		TextView name, quantity, creation_details, assignee, notes;
 		name = (EditText) findViewById(R.id.item_name);
 		quantity = (EditText) findViewById(R.id.item_quantity);
 		creation_details = (TextView) findViewById(R.id.item_creation);
 		assignee = (TextView) findViewById(R.id.item_assignee);
 		notes = (EditText) findViewById(R.id.item_notes);
         
 		Intent i = getIntent();
 		Bundle extras = i.getExtras();
 		item = extras.getParcelable("Item");
 		
 		Log.i("LINKING", "prev item ID " +item.getPrev()+ ". next item ID " +item.getNext());
 		
 		DBHelper db = new DBHelper(this);
 		db.open();
 		prevItem = db.getItem(item.getPrev());
 		nextItem = db.getItem(item.getNext());
 		db.close();
 
 		name.setText(item.getName());
 		quantity.setText("" + item.getQuantity());
 		creation_details.setText("Added on " + item.getCreationDate() + " by "
 				+ item.getCreator());
 		assignee.setText(""+item.getAssignee());
 		notes.setText(item.getNotes());
 	}/*
 	 * Toast.makeText(this, "next", Toast.LENGTH_LONG).show();
 	 */
 
 	public void saveItem(View v) {
 		Toast.makeText(this, "TODO; saveItem method in ItemActivity.java",
 				Toast.LENGTH_LONG).show();
 	}
 
 	public void deleteItem(View v) {
 		Toast.makeText(this, "TODO; deleteItem method in ItemActivity.java",
 				Toast.LENGTH_LONG).show();
 	}
 
 	public void prevItem(View v) {
 
 		Intent intent = new Intent(this, ItemActivity.class);
 		intent.putExtra("Item", prevItem);
 		startActivity(intent);
 	}
 
 	public void nextItem(View v) {
 
 		Intent intent = new Intent(this, ItemActivity.class);
 		intent.putExtra("Item", nextItem);
 		startActivity(intent);
 	}
 
 }

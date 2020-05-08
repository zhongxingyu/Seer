 package edu.colorado.trackers.shoppinglist;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.graphics.Paint;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import edu.colorado.trackers.R;
 import edu.colorado.trackers.SendEmail;
 import edu.colorado.trackers.db.Database;
 import edu.colorado.trackers.db.Deleter;
 import edu.colorado.trackers.db.ResultSet;
 import edu.colorado.trackers.db.Selector;
 import edu.colorado.trackers.db.Updater;
 
 public class SLMainActivity extends Activity {
 	
 	private ListView listItems;
 	private TextView totalCostTextView;
 	private int selectedItem = -1;
 	private Database db;
 	private String tableName = "shopping_list_items";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.sl_activity_main);
         getActionBar().setDisplayHomeAsUpEnabled(true);
         
         db = new Database(this, "shopping_list.db");
         if (!db.tableExists(tableName)) {
         	createDBTable();
         }
         
         totalCostTextView = (TextView) findViewById(R.id.shopping_list_total);
         listItems = (ListView) findViewById(R.id.shopping_list);
         
         listItems.setOnItemClickListener(new SLOnItemClickListener());
         listItems.setOnItemLongClickListener(new SLOnLongItemClickListener());
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.sl_activity_main, menu);
         return true;
     }
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
     	super.onCreateContextMenu(menu, view, menuInfo);
     	getMenuInflater().inflate(R.menu.sl_context_menu, menu);
     }
 
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
                 
             case R.id.sl_menu_new_item:
             	Intent intent = new Intent(this, SLEditItem.class);
             	startActivity(intent);
             	
             case R.id.sl_menu_email:
             	ShoppingListArrayAdapter items = (ShoppingListArrayAdapter) listItems.getAdapter();
             	String content = items.getListAsString();
             	SendEmail s = new SendEmail("Shopping List", content);
             	s.show(getFragmentManager(), "Email Shopping List");
         }
         return super.onOptionsItemSelected(item);
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item) {
     	ShoppingListItem slItem = (ShoppingListItem) listItems.getItemAtPosition(selectedItem);
     	
     	if (item.getItemId() == R.id.context_menu_delete) {
     		System.out.println("Deleted item: " + slItem);
     		deleteItem(slItem.getId());
     		updateTotalCost();
     	} else if (item.getItemId() == R.id.context_menu_edit) {
     		Intent intent = new Intent(this, SLEditItem.class);
     		intent.putExtra("item_id", slItem.getId());
     		startActivity(intent);
     	} else if (item.getItemId() == R.id.context_menu_cancel) {
     		return super.onContextItemSelected(item);
     	}
     	return true;
     }
     
     @Override
     public void onResume() {
     	super.onResume();
     	listItems.setAdapter(getListItems());
     	updateTotalCost();
     }
     
     @Override
     public void onStop() {
     	super.onStop();
     	db.close();
     }
     
     public ArrayAdapter<ShoppingListItem> getListItems() {
     	ArrayAdapter<ShoppingListItem> adapter = new ShoppingListArrayAdapter(this, android.R.layout.simple_list_item_single_choice);
     	
     	Selector selector = db.selector(tableName);
     	selector.addColumns(new String[] { "id", "name", "price", "quantity", "is_crossed" });
     	selector.orderBy("name");
     	int count = selector.execute();
     	System.out.println("Selected (" + count + ") items");
     	ResultSet cursor = selector.getResultSet();
 
     	while (cursor.moveToNext()) {
     		Integer id = cursor.getInt(0);
     		String name = cursor.getString(1);
     		Double price = cursor.getDouble(2);
     		Integer quantity = cursor.getInt(3);
     		Boolean isCrossed = (cursor.getInt(4) == 0) ? false : true;
     		
     		ShoppingListItem item = new ShoppingListItem(id, name, price, quantity);
     		if (isCrossed) item.toggleCrossed();
     		adapter.add(item);
     		System.out.println("Got item: " + item);
     	}
     	cursor.close();
     	return adapter;
     }
     
     public void deleteItem(Integer id) {
     	Deleter deleter = db.deleter(tableName);
     	deleter.where("id = ?", new String[] { id.toString() });
     	deleter.execute();
     	System.out.println("Deleted: id = " + id.toString());
     	
     	listItems.setAdapter(getListItems());
     	selectedItem = -1;
     }
     
     private void createDBTable() {
     	db.createTable(tableName, "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
     			"name TEXT NOT NULL, " +
     			"price REAL NOT NULL, " +
     			"quantity INTEGER NOT NULL, " +
     			"is_crossed INTEGER NOT NULL DEFAULT 0");
     }
     
     private void toggleStikeThrough(TextView textView) {
     	if ((textView.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) {
     		textView.setPaintFlags(textView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
     	} else {    		
     		textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
     	}
     }
     
     private void updateTotalCost() {
     	ShoppingListArrayAdapter items = (ShoppingListArrayAdapter) listItems.getAdapter();
     	totalCostTextView.setText(String.format("Total: $%.2f", items.calculateTotalCost()));
     }
     
     private class SLOnItemClickListener implements OnItemClickListener {
     	
     	public void onItemClick(AdapterView<?> list, View view, int index, long id) {
 			SLMainActivity.this.selectedItem = index;
 			registerForContextMenu(listItems);
 			openContextMenu(listItems);
 			unregisterForContextMenu(listItems);
 		}
     }
     
     private class SLOnLongItemClickListener implements OnItemLongClickListener {
 
     	public boolean onItemLongClick(AdapterView<?> list, View view, int index, long id) {
 			ShoppingListItem item = (ShoppingListItem) listItems.getItemAtPosition(index);
 			item.toggleCrossed();
 			
 			ContentValues values = new ContentValues();
 			values.put("is_crossed", item.isCrossed() ? 1 : 0);
 			
 			Updater updater = db.updater(tableName);
 			updater.columnNameValues(values);
 			updater.where("id = ?", new String[] { item.getId().toString() });
 			updater.execute();
 			
 			View row = list.getChildAt(index);
 			TextView nameTextView = (TextView) row.findViewById(R.id.sl_row_name);
 			toggleStikeThrough(nameTextView);
 			
 			System.out.println("Toggle cross: " + item.toString());
 			return true;
 		}
     }
 
 }

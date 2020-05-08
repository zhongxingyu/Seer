 package com.intuitive.yummy.activities;
 
 import com.intuitive.yummy.R;
 import com.intuitive.yummy.models.*;
 
 import android.os.Bundle;
import android.os.Parcelable;
 import android.widget.AdapterView.OnItemClickListener;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ListView;
 
 public class VendorEditMenuActivity extends Activity {
 
 	private com.intuitive.yummy.models.Menu menu = new com.intuitive.yummy.models.Menu();
 	private ListView listView;
 	Button addNewItem;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
         Intent intent = getIntent();
         final int vendorID = (Integer) intent.getSerializableExtra("VendorID");
         // Connect to the server and get the list of menuitem using the vendorID
     	menu.addMenuItem(new MenuItem(1, "16 inch Cheese Pizza", 10, "Pizza", "Plain Cheese Pizza", true, null, null));
     	menu.addMenuItem(new MenuItem(2, "16 inch Pepperoni Pizza", 11, "Pizza", "Pizza with Pepperoni Topping", true, null, null));
     	menu.addMenuItem(new MenuItem(3, "16 inch Sausage Pizza", 12, "Pizza", "Pizza with Sausage", true, null, null));
     	menu.addMenuItem(new MenuItem(4, "Cheese Pizza Slice", 1.5, "Pizza", "Plain Cheese Pizza (slice)", true, null, null));
     	menu.addMenuItem(new MenuItem(5, "Pepperoni Pizza Slice", 1.65, "Pizza", "Pizza with Pepperoni Topping (slice)", true, null, null));
     	menu.addMenuItem(new MenuItem(6, "Sausage Pizza Slice", 1.75, "Pizza", "Pizza with Sausage (slice)", true, null, null));
     	menu.addMenuItem(new MenuItem(7, "Pizza Cheesesteak", 5, "Cheesesteak", "Cheesesteak filled with pizza topping", true, null, null));
     	menu.addMenuItem(new MenuItem(8, "Chicken Cheesesteak", 6, "Cheesesteak", "Cheesesteak filled with chicken", true, null, null));
     	menu.addMenuItem(new MenuItem(9, "Pepsi 2 Liter", 2.5, "Drink", "2 Liter Pepsi", true, null, null));
     	menu.addMenuItem(new MenuItem(10, "Coca-Cola 2 Liter", 2.5, "Drink", "2 Liter Coca-Cola", true, null, null));
 
     	super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_vendor_edit_menu);
 
 		addNewItem = (Button)findViewById(R.id.add_item);
 		addNewItem.setOnClickListener(new OnClickListener() {
         	public void onClick(View v) {
         		MenuItem newItem = new MenuItem(vendorID, "", 0, "", "", true, null, null);
         		// add the new item to the database
         		menu.addMenuItem(newItem);
         		startEditMenuItemActivity(v, newItem);
         	}
         });
 
         MenuItem[] itemsArray = new MenuItem[menu.getMenuItem().size()];
         menu.getMenuItem().toArray(itemsArray);
 
 		System.out.println("creating adapter");
 		MenuItemAdapter adapter = new MenuItemAdapter(this, R.layout.list_menuitem, itemsArray);
 		listView = (ListView) findViewById(R.id.editMenuItemList);
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> av, View v, int position,
 					long id) {
 		    	MenuItem menuItem = menu.get(position);
 		    	startEditMenuItemActivity(v, menuItem);
 			}			
 		});
 		listView.setAdapter(adapter);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_vendor_edit_menu, menu);
 		return true;
 	}
 
     private void startEditMenuItemActivity(View v, MenuItem menuItem) {
     	Intent intent = new Intent(this, VendorEditMenuItemActivity.class);
     	intent.putExtra("MenuItemID", menuItem.getID());
    	intent.putExtra("MenuItem", (Parcelable)menuItem);
     	System.out.println("MenuItemID: " + menuItem.getID() + ", MenuItemName: " + menuItem.getName());
     	startActivity(intent);    	
 
     }
 }

 package com.eseteam9.shoppyapp;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import com.eseteam9.shoppyapp.handlers.OnlineDatabaseHandler;
 import com.eseteam9.shoppyapp.shoppingclasses.Item;
 import com.eseteam9.shoppyapp.shoppingclasses.Items;
 import com.eseteam9.shoppyapp.shoppingclasses.ShoppingList;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 /**
  * This class is responsible to display all the items of a list and makes them clickable (long hold on an item
  * brings up the options menu on that item). Add items is also possible.
  * 
  * @author S�bastien Broggi, Sammer Puran, Marc Schneiter, Lukas Galliker
  * @extends Activity
  */
 public class DisplayItemsActivity extends Activity {
 	private int listId;
 	private String listname;
 	private ItemAdapter adapter;
 	private Item[] items; 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_display_items);
 		
 		
 		//Get ListID and Name
 		Intent intent = getIntent();
 		this.listId = intent.getIntExtra("LIST_ID", 0);
 		this.listname = intent.getStringExtra("LIST_NAME");
 		this.setTitle(listname);
 		
 		//Initialize ListView
 		ListView lv = (ListView)findViewById(R.id.itemoverview);
 		registerForContextMenu(lv);
 		lv.setClickable(true);
 		
 		//Create Adapter
 		//this.items = newItem.getByListId(this, this.listId);
 		this.items = Items.getByListId(this, this.listId);
 		ArrayList<Item> list = new ArrayList<Item>(Arrays.asList(this.items));
 		this.adapter = new ItemAdapter(this, R.id.itemoverview,  list);
 		lv.setAdapter(adapter);
 	}
 
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.display_items, menu);
 		return true;
 	}
 	
 	
 	//Creates ContextMenu (from arrays.xml) when pressing&holding an item
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 	  if (v.getId()==R.id.itemoverview) {
 	    menu.setHeaderTitle("Options");
 	    String[] menuItems = getResources().getStringArray(R.array.context_menu);
 	    for (int i = 0; i<menuItems.length; i++) {
 	      menu.add(Menu.NONE, i, i, menuItems[i]);
 	    }
 	  }
 	}
 	
 	//Reads what is selected from ContextMenu
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 	    AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo(); 
 	    Item listItem = items[menuInfo.position];
 	    
 	    switch (item.getItemId()) {
 		  case 0:
 			Items.deleteById(this, listItem.id());
 			//new ItemHandler(this).delete(listItem.id());
 			new OnlineDatabaseHandler(this).deleteItem(listItem.onlineKey());
 		    updateView();
 		    return true;
 		  case 1:
 			  editDialog(listItem);
 			  return true;
 		  default:
 		    return super.onContextItemSelected(item);
 		}
 	}
 	public void updateView(){
 		//this.items = newItem.getByListId(this, this.listId);
 		this.items = Items.getByListId(this, this.listId);
 		ArrayList<Item> list = new ArrayList<Item>(Arrays.asList(this.items));
 		adapter.clear();
 		adapter.addAll(list);
 		adapter.notifyDataSetChanged();
 	}
 	
 	//When checked an item
 	public void checkItem(View view){
 		boolean status = ((CheckBox) view).isChecked();
 		ListView lv = (ListView) findViewById(R.id.itemoverview);
 		Item item = items[lv.getPositionForView(view)];
 		//int id = item.id();
 		//new ItemHandler(this).checked(id, status);	
 		item.bought(status);
 		new OnlineDatabaseHandler(this).checkItem(item.onlineKey(), status);
 	}
 	
 	//Backbutton is pressed
 	public void onBackPressed()
 	{
 		Intent intent = new Intent(this, MainActivity.class);
 		startActivity(intent);  
 	}
 	
 	//Options on Taskbar
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle presses on the action bar items
 	    switch (item.getItemId()) {
 	        case R.id.add_list:
 	            addDialog();
 	            return true;
 	        case R.id.action_settings:
 	            //openSettings();
 	            return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	void addDialog() {
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		alert.setTitle("Add Item");
         LayoutInflater infalInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         final View view = infalInflater.inflate(R.layout.dialog_add_item, null);
 		alert.setView(view);
 		
 		AutoCompleteTextView act=(AutoCompleteTextView)view.findViewById(R.id.item_name);
 		//ArrayAdapter<String> arr = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, new ItemHandler(this).getAllNames());
 		ArrayAdapter<String> arr = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, Items.getAllNames(this));
 		act.setAdapter(arr);
 		
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int whichButton) {
 		  Context context = getApplicationContext();
 		  EditText nameView = (EditText) view.findViewById(R.id.item_name);
 		  String itemname = nameView.getText().toString();
 		  EditText quantityView = (EditText) view.findViewById(R.id.quantity);
 		  String quantity = quantityView.getText().toString();
 		  
 			if (itemname.length() == 0)
 		    	Toast.makeText(context, "Please enter a name", Toast.LENGTH_SHORT).show();
 			else {
				//ShoppingList oldList = new ShoppingListHandler(context).get(listId);
 				ShoppingList oldList = new ShoppingList(context, listId);
				//new ItemHandler(context).add(new Item(itemname, quantity, listId));
 				new Item(context, listId, itemname, quantity);
 				
				if (oldList.onlineKey() != "0")
					new OnlineDatabaseHandler(context).putItems(oldList.onlineKey(), listId);
				//items = newItem.getByListId(context, listId);
 				items = Items.getByListId(context, listId);
 				ArrayList<Item> list = new ArrayList<Item>(Arrays.asList(items));
 				adapter.clear();
 				adapter.addAll(list);
 				adapter.notifyDataSetChanged();
 		    } 
 		  }
 		});
 
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		  public void onClick(DialogInterface dialog, int whichButton) {
 		    dialog.cancel();
 		  }
 		});
 
 		alert.show();
 	}
 	
 	
 	
 	void editDialog(final Item item) {
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		alert.setTitle("Edit Item");
 		LayoutInflater infalInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         final View view = infalInflater.inflate(R.layout.dialog_add_item, null);
 		alert.setView(view);
 		EditText nameView = (EditText) view.findViewById(R.id.item_name);
 		nameView.setText(item.name());
 		EditText quantityView = (EditText) view.findViewById(R.id.quantity);
 		quantityView.setText(item.quantity());
 		nameView.setSelection(item.name().length());
 		
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int whichButton) {
 		  Context context = getApplicationContext();
 		  EditText nameView = (EditText) view.findViewById(R.id.item_name);
 		  String itemname = nameView.getText().toString();
 		  EditText quantityView = (EditText) view.findViewById(R.id.quantity);
 		  String quantity = quantityView.getText().toString();
 		  
 			if (itemname.length() == 0)
 		    	Toast.makeText(getBaseContext(), "Please enter a name", Toast.LENGTH_SHORT).show();
 			else{
 				//new ItemHandler(context).update(item.id(), itemname, quantity);
 				item.name(itemname);
 				item.quantity(quantity);
 				//items = newItem.getByListId(context, listId);
 				items = Items.getByListId(context, listId);
 				ArrayList<Item> list = new ArrayList<Item>(Arrays.asList(items));
 				adapter.clear();
 				adapter.addAll(list);
 				adapter.notifyDataSetChanged();
 		    } 
 		  }
 		});
 
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		  public void onClick(DialogInterface dialog, int whichButton) {
 		    dialog.cancel();
 		  }
 		});
 
 		alert.show();
 	}
 
 }

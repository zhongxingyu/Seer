 package cs169.project.thepantry;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.OnChildClickListener;
 import android.widget.TextView;
 import cs169.project.thepantry.ThePantryContract.Ingredients;
 
 public class InventoryAddActivity extends InventoryActivity {
 	String table = Ingredients.TABLE_NAME;
 	private DatabaseModel dm;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setTitle(getString(R.string.InventoryAddTitle));
 		table = Ingredients.TABLE_NAME;
		makeList();
 		
 		// Only should show a back button on action bar?	
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.inventory_add, menu);
 		return true;
 	}
 	
 	@Override
	public void makeList() {
		ArrayList<String> groupItem = getTypes(table);
 
 		ArrayList<Object> childItem = new ArrayList<Object>();
 		for (int i = 0; i < groupItem.size(); i ++) {
 			ArrayList<String> child = getItems(groupItem.get(i));
 			childItem.add(child);
 		}
 
 		ExpandableListView expandbleLis = getExpandableListView();
 		expandbleLis.setDividerHeight(2);
 		expandbleLis.setGroupIndicator(null);
 		expandbleLis.setClickable(true);
 
 		NewAdapter mNewAdapter = new NewAdapter(groupItem, childItem);
 		mNewAdapter.setInflater((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE),this);
 		getExpandableListView().setAdapter(mNewAdapter);
 		expandbleLis.setOnChildClickListener(new OnChildClickListener() {
 			@Override
 			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
 				CheckBox checkBox = (CheckBox) v.findViewById(R.id.textView1);
 				checkBox.toggle();
 				dm.checked(table, ((TextView)checkBox).getText().toString(), checkBox.isChecked());
 				return true;
 			}
 		});
 	}
 	
 	@Override
 	/** Same functionality as InventoryActivity search
 	 *  added ability to put it in ingredient table*/
 	public void search(String item) {
 		//eventually display items returned as buttons as each letters are added to query -- need to create a more detailed search method
 		//If item doesn't exist CustomItem button appears
 		dm = new DatabaseModel(this);
 		
 		/*
 		boolean found = dm.findItem(table, item);
 		if (found) {
 			//display button with given item
 		} else {
 			//display custom button
 			String type = customItem(); // retrieves the type user wants selected item to be
 			addEntry(item, type); // figure out a way to allow user to undo this if they screw up
 		}*/
 	}
 	
 	/** Display all types in a dropdown menu for user to select */
 	public void customItem() {
 		Cursor types = dm.findAllTypes(table);
 
 		// displays types in spinner?
 		// include custom button
 		
 		// figure out how to retrieve what button user clicked
 		//return null;
 		
 	}
 	
 	/** Adds an entry to Ingredient database */
 	public void addEntry(String item, String type) {
 		dm = new DatabaseModel(this);
 		dm.add(table, item, type, 0); //adds entry to ingredient database
 		
 		//mark item "checked"
 	}
 	
 	/** Adds all items to inventory database that have been checked */
 	public void updateInventory() {
 		//TODO: iterate through all checked items add inventory database, mark as unchecked
 		dm = new DatabaseModel(this);
 		Cursor c = dm.checkedItems(table);
 		boolean remSuccess = true; //set to true for display testing
 		
 		// TODO - parse cursor and fill this list 
 		/*List<String> items;
 		for (String item : items) {
 			boolean addSuccess = dm.add(Inventory.TABLE_NAME, item, type, amount);
 		}*/
 		if (remSuccess) {
 			// remove the item from the shopping list display, add it to inventory display
 		} else {
 			// do something else
 		}
 
 		//Maybe pop up window with items added -- maybe store what didn't get added
 	}
 
 }

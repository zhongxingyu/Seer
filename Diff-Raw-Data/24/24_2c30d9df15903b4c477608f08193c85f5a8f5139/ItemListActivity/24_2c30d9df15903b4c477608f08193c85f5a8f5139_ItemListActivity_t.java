 package edu.rit.smartFridge;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import edu.rit.smartFridge.model.InventoryItem;
 import edu.rit.smartFridge.model.ShoppingList;
 import edu.rit.smartFridge.model.ShoppingListItem;
 import edu.rit.smartFridge.util.Connector;
 import edu.rit.smartFridge.util.DataConnect;
 
 public class ItemListActivity extends ListActivity
 {
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		final Context context = this;
 		List<InventoryItem> inventory = null;
 		ShoppingList shoppingList = null;
 
 		// Get the instance of the connecter
 		DataConnect connecter = Connector.getInstance();
 
 		// Set true if we got here from a grocery list,
 		// false if it's just the inventory screen
 		final boolean fromList;
 
 		// get the current list, if it exists
 		Bundle extras = getIntent().getExtras();
 		if (extras != null)
 		{
			long listId = extras.getLong(getString(R.string.current_list));
 			shoppingList = connecter.getList(listId);
 		}
 
 		// copy somewhere final for the listener to use
 		final ShoppingList finalList = shoppingList;
 
 		// copy the item names into a list for display
 		List<String> inventoryNames = new ArrayList<String>();
 		List<String> untaggedNames = new ArrayList<String>();
 
 		// if no list was passed in, we need to get the inventory ourselves
 		if (shoppingList == null)
 		{
 			inventory = connecter.getInventory();
 			fromList = false;
 			
 			if (inventory != null && inventory.size() != 0)
 			{
 				String label; // temp storage
 				for (InventoryItem i : inventory)
 				{
 					int count = connecter.getItemCount(i.getUPC());
 					label = count + "x | " + i.getName();
 	
 					// don't add if list contains label
 					if (!inventoryNames.contains(label))
 					{
 						inventoryNames.add(count + "x | " + i.getName());
 						untaggedNames.add(i.getName());
 					}
 				}
 			}
 			
 		}
 		else
 		{
 			// we got here from a grocery list
 			shoppingList = connecter.populateItems(shoppingList);
 			String label; // temp storage
 
 			if (shoppingList.getAllItems().size() != 0)
 			{
 				for (ShoppingListItem i : shoppingList.getAllItems())
 				{
 					label = i.getQuantity() + "x | " + i.getName();
 					if (!inventoryNames.contains(label))
 					{
 						inventoryNames.add(i.getQuantity() + "x | " + i.getName());
 						untaggedNames.add(i.getName());
 					}
 				}
 			}
 			
 			// set the list title, if there is one
 			setTitle(shoppingList.getName());
 
 			fromList = true;
 		}
 		
 		if (inventoryNames.size() == 0)
 		{
 			// there are no items to display.
 			inventoryNames.add("No items to display.");
 		}
 
 		// copy the inventory into a final variable, to be accessed in the listener
 		final List<InventoryItem> finalInventory = inventory;
 		final List<String> finalNames = untaggedNames;
 
 		// display the names in a list
 		String[] a = new String[inventoryNames.size()];
 		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item,
 				inventoryNames.toArray(a)));
 
 		// event listeners
 		ListView lv = getListView();
 		lv.setTextFilterEnabled(true);
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
 			{
 				// TODO: this doesn't work for the inventory list, only shopping list items.
 				InventoryItem item = null;
 				if (finalNames.size() > 0)
 				{
 					String name = finalNames.get(position);
 					for (InventoryItem i : finalInventory)
 					{
 						if (i.getName() == name)
 						{
 							item = i;
 							break;
 						}
 					}
 					Intent i = new Intent()
 							.setClass(context, ItemDetailActivity.class)
 							.putExtra(getString(R.string.current_upc), item.getUPC())
 							.putExtra(getString(R.string.current_item), item.getName());
 					context.startActivity(i);
 				}
 			}
 		});
 
 		lv.setLongClickable(false); // item add/delete from shopping lists disabled
 		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
 			public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id)
 			{
 
 				Intent i;
 
 				if (fromList)
 				{
 					ShoppingListItem item = finalList.getAllItems().get(
 							position);
 					i = new Intent()
 							.setClass(context, ListRemoveActivity.class);
 					i.putExtra(getString(R.string.current_list), finalList.getID());
 					i.putExtra(getString(R.string.current_item), item.getName());
 					i.putExtra(getString(R.string.current_upc), item.getUPC());
 				}
 				else
 				{
 					InventoryItem item = finalInventory.get(position);
 					i = new Intent().setClass(context, ListAddActivity.class);
 					i.putExtra(getString(R.string.current_item), item.getName());
 					i.putExtra(getString(R.string.current_upc), item.getUPC());
 				}
 
 				context.startActivity(i);
 				return false;
 			}
 		});
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.layout.menu, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case R.id.refresh:
 				refresh();
 				return true;
 		}
 		
 		return false;
 	}
 	
 	public void refresh()
 	{
 		
 	}
 }

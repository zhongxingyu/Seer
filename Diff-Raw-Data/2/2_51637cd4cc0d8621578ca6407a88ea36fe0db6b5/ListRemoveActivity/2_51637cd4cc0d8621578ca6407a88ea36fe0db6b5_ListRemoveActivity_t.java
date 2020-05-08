 package edu.rit.smartFridge;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import edu.rit.smartFridge.model.InventoryItem;
 import edu.rit.smartFridge.model.ShoppingList;
 import edu.rit.smartFridge.util.Connector;
 import edu.rit.smartFridge.util.DataConnect;
 
 public class ListRemoveActivity extends ListActivity {
 
 	/** Called when the activity is first created. */
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	
 	    // get the extras and the connector
         Bundle extras = getIntent().getExtras();
         final DataConnect connector = Connector.getInstance();
         ShoppingList list;
         
         final String itemName;
         final int UPC;
         
         // get item name and UPC
         if (extras != null)
         {
         	itemName = extras.getString(getString(R.string.current_item));
         	UPC = extras.getInt(getString(R.string.current_upc));
         	int listId = extras.getInt(getString(R.string.current_list));
         	list = connector.getList(listId);
         }
         else
         {
         	itemName = "";
         	UPC = -1;
         	list = null;
         }
         
         // copy the list somewhere final so the listener can use it
         final ShoppingList finalList = connector.populateItems(list);
         
         String[] yesNo = {"Yes", "No"};
         setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, yesNo));
         
         ListView lv = getListView();
         lv.setTextFilterEnabled(true);
         
         // make a listener
         lv.setOnItemClickListener(new OnItemClickListener()
         {
         	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
         	{
         		if (position == 0)
         		{
 	        		finalList.removeItem(new InventoryItem(itemName, UPC));
         		}
         		
 				Intent i = new Intent().setClass(parent.getContext(), ItemListActivity.class)
					  					.putExtra(getString(R.string.current_list), finalList.getID());
         		parent.getContext().startActivity(i);
         	}
         });
 	}
 
 }

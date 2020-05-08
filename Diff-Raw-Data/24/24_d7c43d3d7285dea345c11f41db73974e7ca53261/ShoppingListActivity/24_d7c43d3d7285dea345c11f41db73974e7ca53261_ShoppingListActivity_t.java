 package com.openfridge;
 
 import java.util.HashSet;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Set;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.SparseBooleanArray;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckedTextView;
 import android.widget.EditText;
 import android.widget.ListView;
import android.widget.TextView;
 import android.widget.Toast;
 
 //DONE Don't let it add blank JW
 //DONE Make enter enter the item SC
 //DONE Limit a max number of characters JW 
 //DONE Make delete post to server SC/JW
 //DONE Parse shopping list JW
 
 public class ShoppingListActivity extends Activity implements Observer {
 	private static final int MAX_LENGTH = 30;
 	private ArrayAdapter<ShoppingItem> adapter;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.shopping_list);
 

		
 		adapter = new ArrayAdapter<ShoppingItem>(this,
 				android.R.layout.simple_list_item_multiple_choice, DataClient
 						.getInstance().getShoppingList());
 
 		final ListView lv = (ListView) findViewById(R.id.shoppingLV);
 		lv.setAdapter(adapter);
	    //Set shoppinglist title
        updateTitleText(); 
 
 		// Make items not focusable to avoid listitem / button conflicts
 		lv.setItemsCanFocus(false);
 		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 
 		// Listen for checked items
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View v, int arg2,
 					long arg3) {
 				((CheckedTextView)v).toggle();
 			}
 		});
 
 		final EditText itemNameTxt = (EditText) findViewById(R.id.itemName);
 		itemNameTxt.setOnKeyListener(new OnKeyListener() {
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				// If the event is a key-down event on the "enter" button
 				if ((event.getAction() == KeyEvent.ACTION_DOWN)
 						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
 					addItemToList(v);
 					return true;
 				}
 				return false;
 			}
 		});
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		DataClient.getInstance().deleteObserver(this);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		DataClient.getInstance().addObserver(this);
 		update(null, null);
 		DataClient.getInstance().reloadFoods();
 	}
 	
	private void updateTitleText()
	{
	       TextView shoppingTitleTxt 
	          = (TextView) findViewById(R.id.shoppingTitleTxt);
	        int numItems = adapter.getCount();
	        shoppingTitleTxt.setText(getString(R.string.shoppingTitle) + " : " + numItems
	                                + " item" + (numItems == 1? "" : "s"));
	}
 	//Callbacks
 	///////////////
 	
 	public void addItemToList(View view) {
 	    EditText itemName = (EditText) findViewById(R.id.itemName);
 	    String itemToAdd = itemName.getText().toString();
 		if (itemToAdd.length() > 0 && itemToAdd.length() < MAX_LENGTH) {
 			DataClient.getInstance().doNetOp(this, NetOp.PUSH,
 					new ShoppingItem(itemToAdd));
			updateTitleText();
 			itemName.setText(null);
			
	         Toast.makeText(view.getContext(), "Item " + itemToAdd + " added!",
	                    Toast.LENGTH_SHORT).show();
 		} else {
 			Toast.makeText(view.getContext(), "Item was empty or too long!",
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	public void deleteChecked(View view) {
 		ListView lv = (ListView) findViewById(R.id.shoppingLV);
 		int checkedItemsCount = 0;
 		Button dc = (Button) findViewById(R.id.deleteChecked);
 		dc.setEnabled(false);
 		final SparseBooleanArray checkedItems = lv.getCheckedItemPositions();
 		Set<ShoppingItem> itemsToDelete = new HashSet<ShoppingItem>();
 		//Get Checked Items
         checkedItemsCount = 0;
 		for (int i = 0; i < checkedItems.size(); ++i) {
 			if (checkedItems.valueAt(i)) { //if it is checked:
 			    checkedItemsCount++;
 				itemsToDelete.add(adapter.getItem(checkedItems.keyAt(i)));		
 			}
 		}
 		//If none, toast and return
 		if (checkedItemsCount == 0)
 		{
 	          Toast.makeText(view.getContext(), "Nothing selected for deletion",
 	                    Toast.LENGTH_SHORT).show();
 	             dc.setEnabled(true);
 	            return;
 		}
 		//Else toast number of items
 		else
 		{
 		    Toast.makeText(this, checkedItemsCount + " item" + 
 		                        (checkedItemsCount == 1? "" : "s") + 
 		                        " deleted!", Toast.LENGTH_SHORT)
 		    .show();
 		}
 		
         //Unset all the checkboxes
         for (int i = 0; i < lv.getAdapter().getCount() ; i++) {
             lv.setItemChecked(i, false);
         }
 
         
         //Reenable delete button
         dc.setEnabled(true);
         
 		for (ShoppingItem si : itemsToDelete) {
 			if (DataClient.getInstance().doNetOp(this, NetOp.REMOVE, si)) {
 				adapter.remove(si);
 			}
 		}
		updateTitleText();
 
 	}
 
 	@Override
 	public void update(Observable observable, Object data) {
 		adapter.notifyDataSetChanged();
 	}
 
 }

 package com.openfridge;
 
 import java.io.IOException;
 import java.util.Observable;
 import java.util.Observer;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.SparseBooleanArray;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckedTextView;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 //DONE Don't let it add blank JW
 //DONE Make enter enter the item SC
 //DONE Limit a max number of characters JW 
 //TODO Make delete post to server SC/JW
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
 
 		// Make items not focusable to avoid listitem / button conflicts
 		lv.setItemsCanFocus(false);
 		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 
 		// Listen for checked items
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View v, int arg2,
 					long arg3) {
 				CheckedTextView textView = (CheckedTextView) v;
 				textView.setChecked(!textView.isChecked());
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
 
 	public void addItemToList(View view) {
 		String itemToAdd = ((EditText) findViewById(R.id.itemName)).getText()
 				.toString();
 		if (itemToAdd.length() != 0 && itemToAdd.length() < MAX_LENGTH) {
 			ShoppingItem toAdd = new ShoppingItem(itemToAdd, "", DataClient.getInstance().getUID());
 			
 			try {
 				int id = DataClient.getInstance().pushShoppingItem(toAdd);
 				
 				toAdd.setId(id);
 				adapter.add(toAdd);
 
 				Toast.makeText(view.getContext(), "Added Item: " + itemToAdd,
 						Toast.LENGTH_SHORT).show();
 			} catch (IOException e) {
 				Toast.makeText(getBaseContext(), "Communication Error",
 						Toast.LENGTH_SHORT).show();
				Log.e("OpenFridge", e.getMessage()+"");
 			}
 
 		} else {
 			Toast.makeText(view.getContext(), "Item was empty or too long!",
 					Toast.LENGTH_SHORT).show();
 
 		}
 	}
 
 	public void deleteChecked(View view) {
 		ListView lv = (ListView) findViewById(R.id.shoppingLV);
 		final SparseBooleanArray checkedItems = lv.getCheckedItemPositions();
 		if (checkedItems == null) {
 			// That means our list is not able to handle selection
 			// (choiceMode is CHOICE_MODE_NONE for example)
 			Toast.makeText(view.getContext(), "Nothing selected for deletion",
 					Toast.LENGTH_SHORT).show();
 			return;
 		}
 
 		// For each element in the status array
 		final int checkedItemsCount = checkedItems.size();
 		String itemsToDelete = "Delete from DB :";
 		for (int i = 0; i < checkedItemsCount; ++i) {
 			// This tells us the item position we are looking at
 			final int position = checkedItems.keyAt(i);
 
 			// And this tells us the item status at the above position
 			final boolean isChecked = checkedItems.valueAt(i);
 
 			// And we can get our data from the adapter like that
 			if (isChecked) {
 				ShoppingItem x = adapter.getItem(position);
 				try {
 					DataClient.getInstance().removeShoppingItem(x);
 					itemsToDelete += " " + x.toString();
 					adapter.remove(x);
 				} catch (IOException e) {
 					Toast.makeText(getBaseContext(), "Communication Error",
 							Toast.LENGTH_SHORT).show();
 					Log.e("OpenFridge", e.getLocalizedMessage());
 				}
 			}
 		}
 
 		Toast.makeText(view.getContext(), itemsToDelete, Toast.LENGTH_SHORT)
 				.show();
 
 	}
 
 	@Override
 	public void update(Observable observable, Object data) {
 		adapter.notifyDataSetChanged();
 	}
 
 }

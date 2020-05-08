 package com.shawnhanna.shop;
 
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class ShopListActivity extends ShopActivity {
 	static final String TAG = "ListActivity";
 
 	private ImageButton searchButton;
 	private ItemAdapter itemAdapter;
 	private ArrayList<Item> itemList;
 	private ArrayList<Item> checkedList;
 	private DataService dataService;
 	private ListView listView;
 	private TextView totalPriceText;
 
 	// -----------------------------------------------------------------------------------------------------------------------------
 	// -- ONCREATE
 	// -----------------------------------------------------------------------------------------------------------------------------
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// the static items must be created
 		super.onCreate(savedInstanceState);
 
 		// initializes views and buttons
 		initializeViewItems();
 
 		dataService = DataService.getInstance();
 		itemList = dataService.getCart();
 
 		// NOTE: this is just temporary until we get the DB set up
 		if (itemList == null)
 			itemList = new ArrayList<Item>();
 
 		itemAdapter = new ItemAdapter(this, R.layout.list_entry, itemList);
 		listView = (ListView) findViewById(R.id.listList);
 		listView.setAdapter(itemAdapter);
 
 		setupMenuBarButtons(this);
 		// define button listeners
 		initializeButtonListeners();
 		refreshList();
 	}
 
 	// -----------------------------------------------------------------------------------------------------------------------------
 	// -- UTILITY FUNCTIONS
 	// -----------------------------------------------------------------------------------------------------------------------------
 
 	private void initializeViewItems() {
 		setContentView(R.layout.activity_list);
 		searchButton = (ImageButton) findViewById(R.id.add_item_button);
 		totalPriceText = (TextView) findViewById(R.id.total_price);
 	}
 
 	private void initializeButtonListeners() {
 		searchButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				Intent intent = new Intent(ShopListActivity.this,
 						SearchActivity.class);
 				startActivity(intent);
 				refreshList();
 			}
 		});
 	}
 
 	public void refreshList() {
 		totalPriceText.setText(dataService.getCartPriceAsString());
 		itemList = dataService.getCart();
 		itemAdapter.notifyDataSetChanged();
 	}
 
 	// -----------------------------------------------------------------------------------------------------------------------------
 	// -- ITEM ADAPTER PRIVATE CLASS
 	// -----------------------------------------------------------------------------------------------------------------------------
 
 	private class ItemAdapter extends ArrayAdapter<Item> {
 		private ArrayList<Item> items;
 		Item item;
 		boolean initializing;
 
 		public ItemAdapter(Context context, int textViewResourceId,
 				ArrayList<Item> items) {
 			super(context, textViewResourceId, items);
 			this.items = items;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View view = convertView;
 			if (view == null) {
 				LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				view = inflator.inflate(R.layout.list_entry, null);
 			}
 
 			item = items.get(position);
 			if (item != null) {
 				// button creation
 				CheckBox inCartCheckBox = (CheckBox) view
 						.findViewById(R.id.in_cart);
 				TextView nameField = (TextView) view
 						.findViewById(R.id.item_name);
 				TextView quantityField = (TextView) view
 						.findViewById(R.id.item_quantity);
 
 				ImageButton incrementButton = (ImageButton) view
 						.findViewById(R.id.increment_quantity);
 				ImageButton decrementButton = (ImageButton) view
 						.findViewById(R.id.decrement_quantity);
 				ImageButton itemActionButton = (ImageButton) view
 						.findViewById(R.id.item_action_button);
 
 				nameField.setTag(position);// CAUTION: the tag has to be a
 											// number or shit will break
 
 				setUpAdapterListeners(incrementButton, decrementButton,
 						itemActionButton, inCartCheckBox, nameField);
 
 				// all fields are set using the data from each item in the item
 				// array
 
 				// setting this flag is necessary because setchecked calls
 				// onCheckListener...
 				// which is stupid as fuck
 				initializing = true;
 				inCartCheckBox.setChecked(dataService.inChecked(item));
 				Log.d("--INDEX", "" + position);
 				Log.d("--CHECKED", "" + dataService.inChecked(item));
 				if (nameField != null)
 					nameField.setText("" + item.getShortName());
 				if (quantityField != null)
 					quantityField.setText("" + item.getQuantity());
 				initializing = false;
 
 			}
 			return view;
 		}
 
 		private void setUpAdapterListeners(ImageButton incrementButton,
 				ImageButton decrementButton, ImageButton itemActionButton,
 				CheckBox inCartCheckBox, final TextView nameField) {
 
 			incrementButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View arg0) {
 					DataService dataService = DataService.getInstance();
 					if ((Integer) nameField.getTag() < items.size()) {
 						dataService.incrementItem(items.get((Integer) nameField
 								.getTag()));
 						refreshList();
 					}
 				}
 			});
 
 			decrementButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View arg0) {
 					DataService dataService = DataService.getInstance();
 					Item item = items.get((Integer) nameField.getTag());
 					if ((Integer) nameField.getTag() < items.size()) {
 						dataService.decrementItem(item);
 
 						if (item.getQuantity() == 0) {
 							createDialog(item);
 						}
 					}
 					notifyDataSetChanged();
 					refreshList();
 				}
 			});
 
 			itemActionButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View arg0) {
 					Intent intent = new Intent(ShopListActivity.this,
 							ItemMenuActivity.class);
 					dataService.setSelectedItem(item);
 					startActivity(intent);
 					refreshList();
 				}
 			});
 
 			// TODO: make this work (check box persistence)
 			inCartCheckBox
 					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 						public void onCheckedChanged(CompoundButton buttonView,
 								boolean isChecked) {
 							if ((Integer) nameField.getTag() >= items.size()
 									|| initializing)
 								return;
 
 							Item clickedItem = items.get((Integer) nameField
 									.getTag());
 
 							// DataService dataService =
 							// DataService.getInstance();
 							if (dataService.inChecked(clickedItem)) {
 								dataService.removeFromChecked(clickedItem);
 								Log.d("Check", "UNCHECKED");
 							} else {
 								dataService.addToChecked(clickedItem);
 								Log.d("Check", "CHECKED");
 							}
 						}
 					});
 		}
 	}
 
 	private void createDialog(final Item item) {
 		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
 		dialog.setMessage("Are you sure?");
 		dialog.setCancelable(false);
 		dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				// if this button is clicked, close
 				// current activity
 				DataService.getInstance().removeFromCart(item);
 				Intent intent = new Intent(ShopListActivity.this,
 						ShopListActivity.class);
 				startActivity(intent);
 			}
 		});
 		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				//DataService.getInstance().removeFromCart(item);
 				//TODO: set the quantity back to 1?
 				item.setQuantity(1);
 			}
 		});
 
 	}
 }

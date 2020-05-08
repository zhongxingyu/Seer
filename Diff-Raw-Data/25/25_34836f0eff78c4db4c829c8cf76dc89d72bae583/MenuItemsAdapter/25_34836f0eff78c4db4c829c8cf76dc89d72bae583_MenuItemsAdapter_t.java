 /**
  * 
  */
 package com.witwatersrand.androidapplication.menuitems;
 
 import com.witwatersrand.androidapplication.ApplicationPreferences;
 import com.witwatersrand.androidapplication.CanteenManagerDatabase;
 import com.witwatersrand.androidapplication.MenuItem;
 import com.witwatersrand.androidapplication.R;
 
 import android.content.Context;
 import android.graphics.LightingColorFilter;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * @author Kailesh
  * 
  */
 public class MenuItemsAdapter extends ArrayAdapter<MenuItem> {
 
 	private final String LOGGER_TAG = "WITWATERSRAND";
 	private final Context _context;
 	MenuItem[] _myMenu;
 	int _LAYOUT_RESOURCE_ID;
 	View rowRootView;
 
 	public MenuItemsAdapter(Context context, int textViewResourceId,
 			MenuItem[] menuItems) {
 		super(context, textViewResourceId, menuItems);
 		Log.i(LOGGER_TAG, "MenuItemsAdapter -- Constructor");
 		this._context = context;
 		this._myMenu = menuItems;
 		this._LAYOUT_RESOURCE_ID = textViewResourceId;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
 	 * android.view.ViewGroup)
 	 */
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		Log.i(LOGGER_TAG, "MenuItemsAdapter -- getView()");
 		LayoutInflater myInflater = (LayoutInflater) _context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		rowRootView = myInflater.inflate(_LAYOUT_RESOURCE_ID, parent,
 				false);
 
 		Log.i(LOGGER_TAG, "MenuItemsAdapter -- getView() --Inflator called");
 
 		// Item name
 		TextView itemNameTV = (TextView) rowRootView
 				.findViewById(R.id.tvCartItemName);
 
 		int TEXT_WIDTH = (int) TypedValue.applyDimension(
 				TypedValue.COMPLEX_UNIT_DIP, 195, getContext().getResources()
 				.getDisplayMetrics());
 
 		itemNameTV.setWidth(TEXT_WIDTH);
 
 		Log.d(LOGGER_TAG, "_myMenu[position].getItemName() = " + _myMenu[position].getItemName());
 		itemNameTV.setText("" + _myMenu[position].getItemName());
 
 		// Station name
 		TextView stationNameTV = (TextView) rowRootView
 				.findViewById(R.id.tvCartStationName);
 		stationNameTV.setWidth(TEXT_WIDTH);
 		stationNameTV.setText(_myMenu[position].getStationName());
 
 		// Price
 		TextView priceTV = (TextView) rowRootView.findViewById(R.id.tvCartPrice);
 		priceTV.setText("R " + String.format("%.2f", _myMenu[position].getPrice()));
 
 		// Quantity
 		int puchaseQuantity = 0;
 		TextView quantityTV = (TextView) rowRootView.findViewById(R.id.tvCartQuantity);
 		quantityTV.setWidth(TEXT_WIDTH);
 		quantityTV.setText("" + puchaseQuantity);
 
 		// Add button
 		final int _selectedPosition = position;
 		final Button addButton = (Button) rowRootView.findViewById(R.id.bIncerement);
 		addButton.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0x00000000));	
 		addButton.setOnTouchListener(new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				switch(event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 					addButton.getBackground().setColorFilter(new LightingColorFilter(0xFFFF0000, 0xFFff0000));						
 					break;
 				case MotionEvent.ACTION_UP:
 
 					addButton.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0x00000000));	
 					break;
 				}
 				return false;
 			}
 		});
 		
 		addButton.setOnClickListener(new View.OnClickListener() {
 			TextView myUniqueQuantityTV = (TextView) rowRootView.findViewById(R.id.tvCartQuantity);
 			int  itemQuantity = 0;
 			public void onClick(View v) {
 				Log.i(LOGGER_TAG, "CartAdapter -- getView() -- onClick() -- Button pressed for item name: " + _myMenu[_selectedPosition].getItemName());
 				float newCost = ApplicationPreferences.getLatestOrderTotal(_context) + _myMenu[_selectedPosition].getPrice();
 				float balance = ApplicationPreferences.getAccountBalance(_context);
 				if (newCost > balance) { 
 					Toast.makeText(_context, "You do not have suffiecient funds to add more items to the cart", Toast.LENGTH_SHORT).show();
 				} else {
 					itemQuantity = Integer.parseInt(myUniqueQuantityTV.getText().toString());
 					itemQuantity++;
 					myUniqueQuantityTV.setText("" + itemQuantity);
 					int orderNumber =  ApplicationPreferences.getOrderNumber(_context);
 					Log.d(LOGGER_TAG, "orderNumber = " + orderNumber );
 					ApplicationPreferences.setOrderNumber(_context, orderNumber);
 
 					CanteenManagerDatabase myDatabase = new CanteenManagerDatabase(_context);
 					myDatabase.open();
 					myDatabase.addPurchaseItemToOrder(_myMenu[_selectedPosition], itemQuantity, ApplicationPreferences.getOrderNumber(_context));
 
 					// Get total
 					float total = myDatabase.getTotalForOrder(ApplicationPreferences.getOrderNumber(_context));				
 					myDatabase.close();
 
 					// TODO Not good practice but works
 					Items.totalTV.setText("R " + String.format("%.2f", total));
 					ApplicationPreferences.setLatestOrderTotal(_context, total);
 
 				}
 			}
 		});
 
 		// Remove button
 		final Button removeButton = (Button) rowRootView.findViewById(R.id.bDecerement);
 		removeButton.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0x00000000));	
 		removeButton.setOnTouchListener(new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				switch(event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 					removeButton.getBackground().setColorFilter(new LightingColorFilter(0xFFFF0000, 0xFFff0000));						
 					break;
 				case MotionEvent.ACTION_UP:
 
 					removeButton.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0x00000000));	
 					break;
 				}
 				return false;
 			}
 		});
 		
 		removeButton.setOnClickListener(new View.OnClickListener() {
 			TextView myUniqueQuantityTV = (TextView) rowRootView.findViewById(R.id.tvCartQuantity);
 			int  itemQuantity = 0;
 			public void onClick(View v) {
 				Log.i(LOGGER_TAG, "CartAdapter -- getView() -- onClick() -- Button pressed for item name: " + _myMenu[_selectedPosition].getItemName());
 				itemQuantity = Integer.parseInt(myUniqueQuantityTV.getText().toString());
 				if (itemQuantity == 0) {
 					Log.i(LOGGER_TAG, "CartAdapter -- getView() -- onClick() -- itemQuantity = |" + itemQuantity + "|");
 
					CanteenManagerDatabase myDatabase = new CanteenManagerDatabase(_context);
					myDatabase.open();
					myDatabase.removeItemFromOrderList(_myMenu[_selectedPosition].getItemName(), ApplicationPreferences.getOrderNumber(_context));
					myDatabase.close();
 				} else {
 					itemQuantity--;
 
 					myUniqueQuantityTV.setText("" + itemQuantity);
 
 					int orderNumber =  ApplicationPreferences.getOrderNumber(_context);
 					Log.d(LOGGER_TAG, "orderNumber = " + orderNumber );
 					ApplicationPreferences.setOrderNumber(_context, orderNumber);
 
 					CanteenManagerDatabase myDatabase = new CanteenManagerDatabase(_context);
 					myDatabase.open();
 					myDatabase.addPurchaseItemToOrder(_myMenu[_selectedPosition], itemQuantity, ApplicationPreferences.getOrderNumber(_context));
 
 					// Get total
 					float total = myDatabase.getTotalForOrder(ApplicationPreferences.getOrderNumber(_context));				
 					myDatabase.close();
 
 					// TODO Not good practice but works
 					Items.totalTV.setText("R " + String.format("%.2f", total));
 					ApplicationPreferences.setLatestOrderTotal(_context, total);
 				}
 			}
 		});
 
 		Log.i(LOGGER_TAG, "MenuItemsAdapter getView() complete");
 		return rowRootView;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.widget.BaseAdapter#isEnabled(int)
 	 */
 	@Override
 	public boolean isEnabled(int position) {
 		Log.i(LOGGER_TAG, "MenuItemsAdapter isEnabled()");
 		if (_myMenu[position].isAvailable()) {
 			return true;
 		}
 		return false;
 	}
 }

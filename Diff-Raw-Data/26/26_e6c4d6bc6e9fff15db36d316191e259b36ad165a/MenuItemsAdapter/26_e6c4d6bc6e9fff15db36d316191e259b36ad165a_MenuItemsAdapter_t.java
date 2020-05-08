 /**
  * 
  */
 package com.witwatersrand.androidapplication;
 
 import net.technologichron.android.control.NumberPicker;
 
 import android.content.Context;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
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
 				.findViewById(R.id.tvItemName);
 		int TEXT_WIDTH = (int) TypedValue.applyDimension(
 				TypedValue.COMPLEX_UNIT_DIP, 195, getContext().getResources()
 						.getDisplayMetrics());
 		itemNameTV.setWidth(TEXT_WIDTH);
 		
		Log.d(LOGGER_TAG, "_myMenu[position].getItemName() = " + _myMenu[position].getItemName());
		itemNameTV.setText(_myMenu[position].getItemName());
 		
 		// Station name
 		TextView stationNameTV = (TextView) rowRootView
 				.findViewById(R.id.tvStationName);
 		stationNameTV.setWidth(TEXT_WIDTH);
 		stationNameTV.setText(_myMenu[position].getStationName());
		
 		// Price
 		TextView priceTV = (TextView) rowRootView.findViewById(R.id.tvPrice);
 		priceTV.setText("R " + String.format("%.2f", _myMenu[position].getPrice()));
 		
 		// Add to cart button
 		final int _selectedPosition = position;
 		Button mySelectedButton = (Button) rowRootView.findViewById(R.id.selectedButton);
 		mySelectedButton.setOnClickListener(new View.OnClickListener() {
 		NumberPicker quantityPicker = (NumberPicker) rowRootView.findViewById(R.id.selectedPicker);
 			public void onClick(View v) {
 				Log.i(LOGGER_TAG, "Button pressed for item name: " + _myMenu[_selectedPosition].getItemName());
 				int enteredQuantity = quantityPicker.getValue();
 				Log.d(LOGGER_TAG, "enteredQuantity = " + enteredQuantity);
 				if (enteredQuantity == 0) {
 					Log.i(LOGGER_TAG, "MenuItemsAdapter -- getView() -- Attempted to add zero products");
 					Toast.makeText(_context, "Please increase the quantity of the item called " + _myMenu[_selectedPosition].getItemName() + " before adding to cart", Toast.LENGTH_LONG).show();					
 					return;
 				} else {
 					
 					int orderNumber =  ApplicationPreferences.getOrderNumber(_context);
 					Log.d(LOGGER_TAG, "orderNumber = " + orderNumber );
 					ApplicationPreferences.setOrderNumber(_context, orderNumber);
 					
 					CanteenManagerDatabase myDatabase = new CanteenManagerDatabase(_context);
 					
 					myDatabase.open();
 					myDatabase.addPurchaseItemToOrder(_myMenu[_selectedPosition], enteredQuantity, orderNumber);
 					
 					Log.d(LOGGER_TAG, "orderNumber = " + orderNumber);
 					float total = myDatabase.getTotalForOrder(orderNumber);
 					Log.i(LOGGER_TAG, "MenuItemsAdapter -- getView() -- Total = " + total);
 					
 					// TODO Not good practice but works
 					Items.totalTV.setText("R " + String.format("%.2f", total));
 					
 					ApplicationPreferences.setLatestOrderTotal(_context, total);
 					myDatabase.close();
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

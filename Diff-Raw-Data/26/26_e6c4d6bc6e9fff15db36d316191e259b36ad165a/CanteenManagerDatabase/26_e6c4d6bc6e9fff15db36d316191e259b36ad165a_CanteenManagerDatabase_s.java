 /**
  * 
  */
 package com.witwatersrand.androidapplication;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * @author Kailesh
  * 
  */
 public class CanteenManagerDatabase {
 	
 	// TODO make sure that this class is not prone to SQL injection attacks
 	private static final String LOGGER_TAG = "WITWATERSRAND";
 	
 	private static final String KEY_ITEM_NAME = "item_name";
 	private static final String KEY_STATION = "station";
 	private static final String KEY_PRICE = "price";
 	private static final String KEY_AVAILABILITY = "availability";
 	private static final String KEY_PURCHASE_QUANTITY = "purchase_quantity";
 	private static final String KEY_ORDER = "order_number";
 
 	private static final String DATABASE_NAME = "canteen_manager_database";
 	private static final String DATABASE_TABLE_MENU_ITEMS = "menu_items_table";
 	private static final String DATABASE_TABLE_ORDER = "order_table";
 	
 	private static int DATABASE_VERSION = 1;
 
 	private DBHelper _helper;
 	private final Context _context;
 	private SQLiteDatabase _database;
 	
 	public CanteenManagerDatabase(Context context) {
 		Log.i(LOGGER_TAG, "CanteenManagerDatabase -- Constructor");
 		this._context = context;
 	}
 	
 	public CanteenManagerDatabase open() throws SQLException {
 		Log.i(LOGGER_TAG, "CanteenManagerDatabase -- open()");
 		_helper = new DBHelper(_context, DATABASE_VERSION);
 		_database = _helper.getWritableDatabase();
 		return this;
 	}
 	
 	public void close() throws SQLException {
 		Log.i(LOGGER_TAG, "CanteenManagerDatabase -- close()");
 		_helper.close();
 	}
 	
 	public long addMenuItem(MenuItem item) throws SQLException  {
 		Log.i(LOGGER_TAG, "CanteenManagerDatabase -- addmenuItem()");
 		ContentValues newRow = new ContentValues();
 		newRow.put(KEY_ITEM_NAME, item.getItemName());
 		newRow.put(KEY_STATION, item.getStationName());
 		newRow.put(KEY_PRICE, item.getPrice());
 		newRow.put(KEY_AVAILABILITY, item.isAvailable());
 		return _database.insert(DATABASE_TABLE_MENU_ITEMS, null, newRow);
 	}
 	
 	public void setMenu(MenuItem[] menu) throws SQLException {
 		Log.i(LOGGER_TAG, "CanteenManagerDatabase -- setMenu()");
 		for(int i = 0; i < menu.length; i++) {
 			addMenuItem(menu[i]);
 		}
 	}
 	
 	public MenuItem[] getMenu() throws SQLException {
 		Log.i(LOGGER_TAG, "CanteenManagerDatabase -- getMenu");
 		String[] columns = new String[]{KEY_ITEM_NAME, KEY_STATION, KEY_PRICE, KEY_AVAILABILITY};
 		Cursor myCursor = _database.query(DATABASE_TABLE_MENU_ITEMS, columns, null, null, null, null, null, null);
 		
 		int iName = myCursor.getColumnIndex(KEY_ITEM_NAME);
 		int iStation = myCursor.getColumnIndex(KEY_STATION);
 		int iPrice = myCursor.getColumnIndex(KEY_PRICE);
 		int iAvailability = myCursor.getColumnIndex(KEY_AVAILABILITY);
 		
 		MenuItem[] menu = new MenuItem[myCursor.getCount()];
 		
 		int i = 0;	
		for (myCursor.moveToFirst(); !myCursor.isAfterLast(); myCursor.moveToNext()) {
 			menu[i] = new MenuItem();
 			menu[i].setItemName(myCursor.getString(iName));
 			menu[i].setStationName(myCursor.getString(iStation));
 			menu[i].setPrice(myCursor.getFloat(iPrice));
 			boolean tempAvailability = (myCursor.getInt(iAvailability) == 1);
 			menu[i].setAvailability(tempAvailability);
 		}
 		myCursor.close();
 		return menu;
 	}
 	
 	public void removeAllMenuItems() throws SQLException {
 		Log.i(LOGGER_TAG, "CanteenManagerDatabase -- removeAllMenuItems()");
 		_database.delete(DATABASE_TABLE_MENU_ITEMS, null, null);
 	}
 	
 	public void updateAvailability(String name, boolean availability) throws SQLException {
 		Log.i(LOGGER_TAG, "CanteenManagerDatabase -- updateAvailability()");
 		ContentValues updateRow =  new ContentValues();
 		updateRow.put(KEY_AVAILABILITY, availability);
 		_database.update(DATABASE_TABLE_MENU_ITEMS, updateRow, KEY_ITEM_NAME + "=" + name, null);
 	}
 	
 	
 	
 	public MenuItem[] getStationItems(String station) throws SQLException {
 		Log.i(LOGGER_TAG, "CanteenManagerDatabase -- getStationItems()");
 		String[] columns = new String[]{KEY_ITEM_NAME, KEY_STATION, KEY_PRICE, KEY_AVAILABILITY, KEY_PURCHASE_QUANTITY};
 		Cursor stationCursor = _database.query(DATABASE_TABLE_MENU_ITEMS, columns, KEY_STATION + "=" + station, null, null, null, null);
 
 		int iName = stationCursor.getColumnIndex(KEY_ITEM_NAME);
 		int iStation = stationCursor.getColumnIndex(KEY_STATION);
 		int iPrice = stationCursor.getColumnIndex(KEY_PRICE);
 		int iAvailability = stationCursor.getColumnIndex(KEY_AVAILABILITY);
 		
 		MenuItem[] menu = new MenuItem[stationCursor.getCount()];
 		
 		int i = 0;	
 		for (stationCursor.moveToFirst(); !stationCursor.isAfterLast(); stationCursor.moveToNext()) {
 			menu[i].setItemName(stationCursor.getString(iName));
 			menu[i].setStationName(stationCursor.getString(iStation));
 			menu[i].setPrice(stationCursor.getFloat(iPrice));
 			boolean tempAvailability = (stationCursor.getInt(iAvailability) == 1);
 			menu[i].setAvailability(tempAvailability);
 		}
 		stationCursor.close();
 		return menu;
 	}
 	
 	public void addPurchaseItemToOrder(MenuItem menuItemToPurchase, int quantity, int orderNumber)  throws SQLException {
 		Log.i(LOGGER_TAG, "CanteenManagerDatabase -- addPurchaseItemToOrder()");
 		ContentValues newRow = new ContentValues();
 		if (isInOrderTable(menuItemToPurchase.getItemName(), orderNumber) ) {
 			updatePurchaseQuantity(menuItemToPurchase.getItemName(), quantity, orderNumber); 
 			Toast.makeText(_context, menuItemToPurchase.getItemName() + " updated in order list " + orderNumber, Toast.LENGTH_SHORT).show();
 		} else {
 			newRow.put(KEY_ITEM_NAME, menuItemToPurchase.getItemName());
 			newRow.put(KEY_STATION, menuItemToPurchase.getStationName());
 			newRow.put(KEY_PRICE, menuItemToPurchase.getPrice());
 			newRow.put(KEY_PURCHASE_QUANTITY, quantity);
 			newRow.put(KEY_ORDER, orderNumber);
 			_database.insert(DATABASE_TABLE_ORDER, null, newRow);
 			Toast.makeText(_context, menuItemToPurchase.getItemName() + " added to the order list " + orderNumber, Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 	public void updatePurchaseQuantity(String name, int quantity, int orderNumber) throws SQLException {
 		Log.i(LOGGER_TAG, "CanteenManagerDatabase -- updatePurchaseQuantity()");
 		ContentValues updateRow =  new ContentValues();
 		updateRow.put(KEY_PURCHASE_QUANTITY, quantity);
 		_database.update(DATABASE_TABLE_ORDER, updateRow, KEY_ITEM_NAME + "='" + name +"' AND " +  KEY_ORDER + "='" + orderNumber + "'", null); // Change the table name here
 	}
 	
 	private boolean isInOrderTable(String itemName, int orderNumber) {
 		Log.i(LOGGER_TAG, "CanteenManagerDatabase -- isInOrderTable()");
 		String[] columns = new String[]{KEY_ITEM_NAME, KEY_STATION, KEY_PRICE, KEY_PURCHASE_QUANTITY, };
 		Cursor stationCursor = _database.query(DATABASE_TABLE_ORDER, columns, KEY_ITEM_NAME + "='" + itemName + "'" + " AND " + KEY_ORDER + "='" + orderNumber + "'", null, null, null, null);
 
 		if (stationCursor.getCount() == 0) {
 			stationCursor.close();
 			return false;
 		} else {
 			stationCursor.close();
 			return true;
 		}
 		
 	}
 	
 	public void removeAllOrderItems() {
 		Log.i(LOGGER_TAG, "CanteenManagerDatabase -- removeAllOrderItems()");
 		_database.delete(DATABASE_TABLE_ORDER, null, null);
 	}
 	
 	public OrderItem[] getOrder(int orderNumner) {
 		Log.i(LOGGER_TAG, "CanteenManagerDatabase -- getOrder()");
 		String[] columns = new String[]{KEY_ITEM_NAME, KEY_STATION, KEY_PRICE, KEY_PURCHASE_QUANTITY, KEY_ORDER};
 		Cursor orderCursor = _database.query(DATABASE_TABLE_ORDER, columns, KEY_ORDER + "='" + orderNumner + "'", null, null, null, null);
 		int iName = orderCursor.getColumnIndex(KEY_ITEM_NAME);
 		int iStation = orderCursor.getColumnIndex(KEY_STATION);
 		int iPrice = orderCursor.getColumnIndex(KEY_PRICE);
 		int iQuantity = orderCursor.getColumnIndex(KEY_PURCHASE_QUANTITY);
 
 		OrderItem[] orderList = new OrderItem[orderCursor.getCount()];
 
 		int i = 0;
 		for (orderCursor.moveToFirst(); !orderCursor.isAfterLast(); orderCursor.moveToNext()) {
 			orderList[i] = new OrderItem();
 			orderList[i].setItemName(orderCursor.getString(iName));
 			orderList[i].setStationName(orderCursor.getString(iStation));
 			orderList[i].setPrice(orderCursor.getFloat(iPrice));
 			orderList[i].setPurchaseQuantity(orderCursor.getInt(iQuantity));
 			i++;
 		}
 		orderCursor.close();
 		return orderList;
 	}
 	
 	public float getTotalForOrder(int orderNumber) {
 		Log.i(LOGGER_TAG, "CanteenManagerDatabase -- getTotalForOrder()");
 		final String sumOfOrders = "SELECT TOTAL(" + KEY_PURCHASE_QUANTITY + "*" + KEY_PRICE + ") AS my_sum FROM " + DATABASE_TABLE_ORDER + " WHERE " + KEY_ORDER + "='" + orderNumber + "'";
 		Cursor totalCursor = _database.rawQuery(sumOfOrders, null);
 		if(totalCursor.moveToFirst()) {
 		    return totalCursor.getFloat(0);
 		}
 		totalCursor.close();
 		return -1;
 	}
 
 	private static class DBHelper extends SQLiteOpenHelper {
 
 		/*		
 			*****Normal Constructor parameters******
 			----------------------------------------	
  			public DBHelper(Context context, String name, CursorFactory factory, int version) {
 				super(context, name, factory, version); 
 				}
 		}*/
 		
 		/*
 		 * Removing constructor parameters defining the name of the database,
 		 * a cursor factory, and version of the database.
 		 * 
 		 */
 		public DBHelper(Context context, int databaseVersion) {
 			super(context, DATABASE_NAME, null, databaseVersion);
 			Log.i(LOGGER_TAG, "CanteenManagerDatabase -- DBHelper -- Constructor");
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 		 	// Called the first time the database is ever created
 			Log.i(LOGGER_TAG, "CanteenManagerDatabase -- DBHelper -- onCreate()");
 			db.execSQL( "CREATE TABLE " +  DATABASE_TABLE_MENU_ITEMS + " (" + 
 					KEY_ITEM_NAME + " TEXT NOT NULL, " + 
 					KEY_STATION + " TEXT NOT NULL, " + 
 					KEY_PRICE + " REAL NOT NULL DEFAULT 0," +
 					KEY_AVAILABILITY + " INTEGER NOT NULL DEFAULT 1);"
 					);
 			db.execSQL( "CREATE TABLE " +  DATABASE_TABLE_ORDER + " (" + 
 					KEY_ITEM_NAME + " TEXT NOT NULL, " + 
 					KEY_STATION + " TEXT NOT NULL, " +
 					KEY_PRICE + " REAL NOT NULL DEFAULT 0," +
 					KEY_PURCHASE_QUANTITY + " INTEGER NOT NULL DEFAULT 0," +
 					KEY_ORDER + " INTEGER NOT NULL DEFAULT 0);"
 					);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 				Log.i(LOGGER_TAG, "CanteenManagerDatabase -- DBHelper -- onUpgrade()");
 				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_MENU_ITEMS);
 				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_ORDER);
 				onCreate(db);
 		}
 	}
 }

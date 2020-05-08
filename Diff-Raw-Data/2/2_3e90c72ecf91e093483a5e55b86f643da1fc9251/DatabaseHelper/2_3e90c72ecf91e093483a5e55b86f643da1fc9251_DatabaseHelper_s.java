 package com.aj3.kiss;
 
 import java.util.Vector;
 
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.content.ContentValues;
 import android.content.Context;
 
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 	private static final int DATABASE_VERSION = 1;
 	private static final String DATABASE_NAME = "KissDB";
 	private static final String TABLE_ITEM = "item";
 	private static final String TABLE_INVENTORY = "inventory";
 	private static final String TABLE_GROCERY = "grocery";
 	private static final String TABLE_CATEGORY = "category";
 	private static final String TABLE_UNIT = "unit";
 	private static final String KEY_ITEM_ID = "item_id";
 	private static final String KEY_CATEGORY_ID = "category_id";
 	private static final String KEY_UNIT_ID = "unit_id";
 	private static final String KEY_UPC = "upc";
 	private static final String KEY_ITEM_NAME = "item_name";
 	private static final String KEY_CATEGORY_NAME = "category_name";
 	private static final String KEY_UNIT_NAME = "unit_name";
 	private static final String KEY_QUANTITY = "quantity";
 	
 	public DatabaseHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 	
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		String createItemTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_ITEM + " ( " +
 				KEY_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
 				KEY_ITEM_NAME + " TEXT UNIQUE, " +
 				KEY_CATEGORY_ID + " INTEGER, " +
 				KEY_UNIT_ID + " TEXT, " +
 				KEY_UPC + " TEXT )";
 		String createInventoryTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_INVENTORY + " ( " +
 				KEY_ITEM_ID + " INTEGER, " +
 				KEY_QUANTITY + " REAL )";
 		String createGroceryTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_GROCERY + " ( " +
 				KEY_ITEM_ID + " INTEGER, " +
 				KEY_QUANTITY + " REAL )";
 		String createCategoryTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORY + " ( " +
 				KEY_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
 				KEY_CATEGORY_NAME + " TEXT UNIQUE )";
 		String createUnitTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_UNIT + " ( " +
 				KEY_UNIT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
 				KEY_UNIT_NAME + " TEXT UNIQUE )";
 		db.execSQL(createItemTableQuery);
 		db.execSQL(createInventoryTableQuery);
 		db.execSQL(createGroceryTableQuery);
 		db.execSQL(createCategoryTableQuery);
 		db.execSQL(createUnitTableQuery);
 	}
 
 	private void dropTables(SQLiteDatabase db) {
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEM);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROCERY);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_UNIT);
 	}
 	
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		dropTables(db);
 		this.onCreate(db);
 	}
 	
 	public void resetDatabase() {
 		SQLiteDatabase db = this.getWritableDatabase();
 		dropTables(db);
 		this.onCreate(db);
 	}
 	
 	public void closeDB() {
 		SQLiteDatabase db = this.getReadableDatabase();
 		if (db != null && db.isOpen())
 			db.close();
 	}
 	
 	public int addCategory(Category category) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		values.put(KEY_CATEGORY_NAME, category.getName());
 		
 		return (int) db.insert(TABLE_CATEGORY, null, values);
 	}
 	
 	public int addUnit(Unit unit) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		values.put(KEY_UNIT_NAME, unit.getName());
 		
 		return (int) db.insert(TABLE_UNIT, null, values);
 	}
 	
 	public int addItem(Item item) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		values.put(KEY_ITEM_NAME, item.getName());
 		values.put(KEY_CATEGORY_ID, item.getCategory().getId());
 		values.put(KEY_UNIT_ID, item.getUnit().getId());
 		values.put(KEY_UPC, item.getUpc());
 		
 		return (int) db.insert(TABLE_ITEM, null, values);
 	}
 	
 	public void addInventoryItem(ListItem item) {
 		addTableItem(TABLE_INVENTORY, item);
 	}
 	
 	public void addGroceryItem(ListItem item) {
 		addTableItem(TABLE_GROCERY, item);
 	}
 	
 	private void addTableItem(String tableName, ListItem listItem) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		values.put(KEY_ITEM_ID, listItem.getItem().getId());
 		values.put(KEY_QUANTITY, listItem.getQuantity());	
 		
 		db.insert(tableName, null, values);
 	}
 	
 	public int getCategoryId(String categoryName) {
 		String query = "SELECT " + KEY_CATEGORY_ID + " FROM " + TABLE_CATEGORY +
 				" WHERE " + KEY_CATEGORY_NAME + " = '" + categoryName + "'";
 		
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(query, null);
 		
 		if (cursor == null || cursor.getCount() == 0) {
 			return -1;
 		}
 		
 		cursor.moveToFirst();
 		
 		return cursor.getInt(cursor.getColumnIndex(KEY_CATEGORY_ID));
 	}
 	
 	public int getUnitId(String unitName) {
 		String query = "SELECT " + KEY_UNIT_ID + " FROM " + TABLE_UNIT +
 				" WHERE " + KEY_UNIT_NAME + " = '" + unitName.replace("'", "\'\'") + "'";
 		
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(query, null);
 		
 		if (cursor == null || cursor.getCount() == 0) {
 			return -1;
 		}
 		
 		cursor.moveToFirst();
 		
 		return cursor.getInt(cursor.getColumnIndex(KEY_UNIT_ID));
 	}
 	
 	public int getItemId(String itemName) {
 		String query = "SELECT " + KEY_ITEM_ID + " FROM " + TABLE_ITEM +
 				" WHERE " + KEY_ITEM_NAME + " = '" + itemName.replace("'", "\'\'") + "'";
 
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(query, null);
 		
 		if (cursor == null || cursor.getCount() == 0) {
 			return -1;
 		}
 		
 		cursor.moveToFirst();
 		
 		return cursor.getInt(cursor.getColumnIndex(KEY_ITEM_ID));
 	}
 	
 	private Item getItem(Cursor cursor) {
 		Item item = new Item();
 		item.setId(cursor.getInt(cursor.getColumnIndex(KEY_ITEM_ID)));
 		item.setName(cursor.getString(cursor.getColumnIndex(KEY_ITEM_NAME)));
 		
 		Category category = new Category();
 		category.setId(cursor.getInt(cursor.getColumnIndex(KEY_CATEGORY_ID)));
 		category.setName(cursor.getString(cursor.getColumnIndex(KEY_CATEGORY_NAME)));
 		item.setCategory(category);
 		
 		Unit unit = new Unit();
 		unit.setId(cursor.getInt(cursor.getColumnIndex(KEY_UNIT_ID)));
 		unit.setName(cursor.getString(cursor.getColumnIndex(KEY_UNIT_NAME)));
 		item.setUnit(unit);
 		
 		item.setUpc(cursor.getString(cursor.getColumnIndex(KEY_UPC)));
 		
 		return item;
 	}
 	
 	public Item getItemByUpc(String upc) {
 		String query = "SELECT * FROM " + TABLE_ITEM + " WHERE " + KEY_UPC + " = '" + upc + "'";
 		
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(query, null);
 		
 		if (cursor == null || cursor.getCount() == 0) {
 			return null;
 		}
 		
 		cursor.moveToFirst();
 		
 		return getItem(cursor);
 	}
 	
 	public Item getItemByName(String itemName) {
		String query = "SELECT * FROM " + TABLE_ITEM + " WHERE " + KEY_ITEM_NAME + " + '" + itemName.replace("'", "\'\'") + "'";
 		
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(query, null);
 		
 		if (cursor == null || cursor.getCount() == 0) {
 			return null;
 		}
 		
 		cursor.moveToFirst();
 		
 		return getItem(cursor);
 	}
 	
 	public Vector<ListItem> getGrocery() {
 		return getList(TABLE_GROCERY);
 	}
 	
 	public Vector<ListItem> getInventory() {
 		return getList(TABLE_INVENTORY);
 	}
 	
 	private Vector<ListItem> getList(String tableName) {
 		Vector<ListItem> list = new Vector<ListItem>();
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		String query = "SELECT * FROM " + tableName + " JOIN " + TABLE_ITEM + " JOIN " + TABLE_CATEGORY + " JOIN " + TABLE_UNIT + 
 				" ON " + tableName + "." + KEY_ITEM_ID + " = " + TABLE_ITEM + "." + KEY_ITEM_ID +
 				" AND " + TABLE_ITEM + "." + KEY_CATEGORY_ID + " = " + TABLE_CATEGORY + "." + KEY_CATEGORY_ID +
 				" AND " + TABLE_ITEM + "." + KEY_UNIT_ID + " = " + TABLE_UNIT + "." + KEY_UNIT_ID;
 		Cursor cursor = db.rawQuery(query, null);
 		
 		if (cursor.moveToFirst()) {
 			do {
 				ListItem listItem = new ListItem();
 				listItem.setItem(getItem(cursor));
 				listItem.setQuantity(cursor.getDouble(cursor.getColumnIndex(KEY_QUANTITY)));
 				
 				list.add(listItem);
 			} while (cursor.moveToNext());
 		}
 		
 		return list;
 	}
 	
 	public int updateItem(Item item) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		values.put(KEY_ITEM_NAME, item.getName());
 		values.put(KEY_CATEGORY_ID, item.getCategory().getId());
 		values.put(KEY_UNIT_ID, item.getUnit().getId());
 		values.put(KEY_UPC, item.getUpc());
 		
 		return db.update(TABLE_ITEM, values, KEY_ITEM_ID + " = ?", new String[] { String.valueOf(item.getId()) });
 	}
 	
 	public int updateGroceryItem(ListItem listItem) {
 		return updateListItem(TABLE_GROCERY, listItem);
 	}
 	
 	public int updateInventoryItem(ListItem listItem) {
 		return updateListItem(TABLE_INVENTORY, listItem);
 	}
 	
 	private int updateListItem(String tableName, ListItem listItem) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		values.put(KEY_QUANTITY, listItem.getQuantity());
 		
 		return db.update(tableName, values, KEY_ITEM_ID + " = ?", new String[] { String.valueOf(listItem.getItem().getId()) });
 	}
 	
 	public void deleteGroceryItem(ListItem listItem) {
 		deleteListItem(TABLE_GROCERY, listItem);
 	}
 	
 	public void deleteInventoryItem(ListItem listItem) {
 		deleteListItem(TABLE_INVENTORY, listItem);
 	}
 	
 	private void deleteListItem(String tableName, ListItem listItem) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		db.delete(tableName, KEY_ITEM_ID + " = ?", new String[] {String.valueOf(listItem.getItem().getId()) });
 	}
 }

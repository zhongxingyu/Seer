 package com.example.quickshop;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 
 @SuppressLint("NewApi")
 public class DatabaseHandler extends SQLiteOpenHelper{
 
 	
 	//All static variables
 	// Database version
 	private static final int DATABASE_VERSION = 2;
 	private static String DB_PATH = "/data/data/com.example.quickshop/databases/";
 	// Database Name
 	static final String DATABASE_NAME = "QuickShopDB";
 	
 	//store table names
 	private static final String TABLE_STORE = "store";
 	
 	//store table columns 
 	static final String KEY_STOREID = "store_id";
 	private static final String STORE_LOC_COORDINATES = "location_coordinates";
 	static final String STORE_NAME = "store_name";
 	
 	// item table name
 	private static final String TABLE_ITEM = "item";
 	
 	// item table columns 
 	private static final String KEY_ITEMID = "item_id";
 	private static final String ITEM_NAME = "item_name";
 	
 	// cartegory table name
 	private static final String TABLE_CATEGORY = "category";
 	
 	// category table columns
 	static final String KEY_CATEGORYNAME = "category_name";
 	private static final String CATEGORY_LOCATION = "category_location";
 	private static final String CATEGORY_ANCHOR_POINT = "anchor_point";
 	private static final String CATEGORY_STORE_ID = "category_store_id";
 	
 	// Item Category table
 	private static final String TABLE_ITEMCATEGORY = "item_category";
     
 	//Item Category Columns
 	private static final String KEY_ITEM = "item_name";
 	private static final String KEY_CATEGORY = "category_name";
 	
 	// ItemCatNew Table
 	private static final String TABLE_ITEMCATNEW = "item_category_new";
 	
 	// ItemCatNew cols
 	
 	private static final String KEY_ITEM_NEW = "item_name_new";
 	private static final String KEY_CATEGORY_NEW = "category_name_new";
 	
 	private SQLiteDatabase myDataBase;
 	private final  Context myContext;
 	
 	
 	public DatabaseHandler(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		this.myContext = context;
 		// TODO Auto-generated constructor stub
 	}
 
 	// Creating tables
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		
 		String CREATE_STORE_TABLE = "CREATE TABLE " + TABLE_STORE + "(" + KEY_STOREID + " INTEGER PRIMARY KEY," + STORE_NAME + " TEXT," + STORE_LOC_COORDINATES + " TEXT" + ");";
 		
 		String CREATE_ITEM_TABLE = "CREATE TABLE " + TABLE_ITEM + "(" + KEY_ITEMID + " INTEGER PRIMARY KEY," + ITEM_NAME + " TEXT" + ");";
 		
 		String CREATE_CATEGORY_TABLE = "CREATE TABLE " + TABLE_CATEGORY + "(" + KEY_CATEGORYNAME + " STRING PRIMARY KEY," + CATEGORY_LOCATION + " TEXT," + CATEGORY_ANCHOR_POINT + " TEXT," + CATEGORY_STORE_ID + " INTEGER," + "FOREIGN KEY" + "(" + CATEGORY_STORE_ID + ") REFERENCES " + TABLE_STORE + "(" + KEY_STOREID + "));";  		
 		
 	    String CREATE_ITEM_CAT_TABLE = "CREATE TABLE " + TABLE_ITEMCATEGORY + "(" + KEY_ITEM + " STRING PRIMARY KEY," + KEY_CATEGORY + " STRING PRIMARY KEY" + ");";
 	    
 	    String CREATE_ITEM_CAT_NEW_TABLE = "CREATE TABLE " + TABLE_ITEMCATNEW + "(" + KEY_ITEM_NEW + " STRING PRIMARY KEY," + KEY_CATEGORY_NEW + " TEXT" + ");";
 	    
 		db.execSQL(CREATE_STORE_TABLE);
 		db.execSQL(CREATE_ITEM_TABLE);
 		db.execSQL(CREATE_CATEGORY_TABLE);
 		//db.execSQL(CREATE_ITEM_CAT_TABLE);
 		db.execSQL(CREATE_ITEM_CAT_NEW_TABLE);
 		
 		
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_STORE);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEM);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMCATEGORY);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
 		db.execSQL("DROP TABLE IF EXISTS " +TABLE_ITEMCATNEW);
 		
 		// create tables again
 		onCreate(db);
 		
 	}
 	
 	 public void createDataBase() throws IOException{
 		 
 	    	boolean dbExist = checkDataBase();
 	 
 	    	if(dbExist){
 	    		System.out.println("DB already exists");
 	    		//do nothing - database already exist
 	    	}else{
 	    		System.out.println("DB Does not exist");
 	    		//By calling this method and empty database will be created into the default system path
 	               //of your application so we are gonna be able to overwrite that database with our database.
 	        	this.getReadableDatabase();
 	 
 	        	try {
 	 
 	    			copyDataBase();
 	 
 	    		} catch (IOException e) {
 	 
 	        		throw new Error("Error copying database");
 	 
 	        	}
 	    	}
 	 
 	    }
 	 
 	    /**
 	     * Check if the database already exist to avoid re-copying the file each time you open the application.
 	     * @return true if it exists, false if it doesn't
 	     */
 	    private boolean checkDataBase(){
 	 
 	    	SQLiteDatabase checkDB = null;
 	 
 	    	try{
 	    		String myPath = DB_PATH + DATABASE_NAME;
 	    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 	 
 	    	}catch(SQLiteException e){
 	 
 	    		//database does't exist yet.
 	 
 	    	}
 	 
 	    	if(checkDB != null){
 	 
 	    		checkDB.close();
 	 
 	    	}
 	 
 	    	return checkDB != null ? true : false;
 	    }
 	 
 	    /**
 	     * Copies your database from your local assets-folder to the just created empty database in the
 	     * system folder, from where it can be accessed and handled.
 	     * This is done by transfering bytestream.
 	     * */
 	    private void copyDataBase() throws IOException{
 	 
 	    	//Open your local db as the input stream
 	    	InputStream myInput = myContext.getAssets().open(DATABASE_NAME);
 	 
 	    	// Path to the just created empty db
 	    	String outFileName = DB_PATH + DATABASE_NAME;
 	 
 	    	//Open the empty db as the output stream
 	    	OutputStream myOutput = new FileOutputStream(outFileName);
 	 
 	    	//transfer bytes from the inputfile to the outputfile
 	    	byte[] buffer = new byte[1024];
 	    	int length;
 	    	while ((length = myInput.read(buffer))>0){
 	    		myOutput.write(buffer, 0, length);
 	    	}
 	 
 	    	//Close the streams
 	    	myOutput.flush();
 	    	myOutput.close();
 	    	myInput.close();
 	 
 	    }
 	 
 	    public void openDataBase() throws SQLException{
 	 
 	    	//Open the database
 	        String myPath = DB_PATH + DATABASE_NAME;
 	    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 	 
 	    }
 	 
 	    @Override
 		public synchronized void close() {
 	 
 	    	    if(myDataBase != null)
 	    		    myDataBase.close();
 	 
 	    	    super.close();
 	 
 		}
 	 
 		 
 	/**
 	 * THE INSERT TABLE FUNCTIONS
 	 * **/
 	// Adding Store details
 	void addStore(Store store){
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		values.put(KEY_STOREID, store.getID()); // getting store id
 		values.put(STORE_NAME, store.getName()); // getting Store name
 		values.put(STORE_LOC_COORDINATES, store.getCordinates()); // getting store coordinates
 		
 		
 		// Inserting new row
 		db.insert(TABLE_STORE, null, values);
 		db.close(); // closing the database connection 
 	}
 	
 	//adding item details 
 	
 	void addItem(Item item){
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		values.put(KEY_ITEMID , item.getItemID());
 		values.put(ITEM_NAME, item.getItemName());
 		
 		db.insert(TABLE_ITEM, null, values);
 		db.close();
 	}
 	
 	// adding category details
 	void addCategory(Category category) {
 		
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		values.put(KEY_CATEGORYNAME, category.getCatName());
 		values.put(CATEGORY_LOCATION, category.getCatLoc());
 		values.put(CATEGORY_ANCHOR_POINT, category.getAnchPt());
 		values.put(CATEGORY_STORE_ID, category.getStoreID());
 		
 		db.insert(TABLE_CATEGORY, null, values);
 		db.close();
 	}
 	
 	// adding ItemCategory Details
 	
 	void addItemCategory(ItemCategory itemCat){
 		
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		
 		values.put(KEY_ITEM , itemCat.getItemName());
 		values.put(KEY_CATEGORY, itemCat.getCatName());
 		
 		db.insert(TABLE_ITEMCATEGORY, null, values);
 		db.close();
 		
 	}
 
 	// adding ItemCatNew details
 	
 	void addItemCatNew(ItemCatNew itemCatNew){
 		 SQLiteDatabase db = this.getWritableDatabase();
 		 ContentValues values = new ContentValues();
 		 
 		 values.put(KEY_ITEM_NEW, itemCatNew.getItemName());
 		 values.put(KEY_CATEGORY_NEW, itemCatNew.getCatName());
 		 
 		 db.insert(TABLE_ITEMCATNEW, null, values);
 		 db.close();
 	}
 	 
 	/**
 	 * THE SELECT FROM TABLE FUNCTIONS (READING ROWS OF THE TABLE)
 	 */
 	
 	// Getting single store 
 	public Store getStore(int id){
 		
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		//Cursor cursor = db.query(TABLE_STORE, new String[] { KEY_STOREID , STORE_NAME, STORE_LOC_COORDINATES}, KEY_STOREID + " = " + id , new String[] {String.valueOf(STORE_NAME)}, null, null, null, null);
 		Cursor cursor = db.query(TABLE_CATEGORY, new String[] {STORE_NAME}, null, new String [] {KEY_STOREID + "=" + id}, null, null, null, null);
 		if(cursor != null)
 			cursor.moveToFirst();
 		
 		Store store = new Store(cursor.getInt(0) , cursor.getString(1), cursor.getString(2));
 		
 		return store;
 		
 	}
 	
 	
 	// getting itemCategories 
 	
 	public ItemCategory getItems(String category){
 		
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor cursor = db.query(TABLE_ITEMCATEGORY, new String[] {KEY_ITEM , KEY_CATEGORY}, KEY_CATEGORY + "=?", new String[] {String.valueOf(KEY_ITEM)}, null, null, null, null);
 		
 		if(cursor != null)
 			cursor.moveToFirst();
 		
 		ItemCategory itemCat = new ItemCategory(cursor.getString(0), cursor.getString(1));
 		
 		return itemCat;
 		
 	}
 	
 	// testing 
 public ItemCatNew getItemsNew(String category){
 		
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		//Cursor cursor = db.query(TABLE_ITEMCATNEW, new String[] {KEY_ITEM_NEW , KEY_CATEGORY_NEW}, KEY_CATEGORY_NEW + "=" + category, new String[] {String.valueOf(KEY_ITEM_NEW)}, null, null, null, null);
 		//Cursor cursor = db.query(TABLE_ITEMCATNEW, new String[] {KEY_ITEM_NEW, KEY_CATEGORY_NEW}, null, new String[] {KEY_CATEGORY_NEW + "=?"}, null, null, null);
 		Cursor cursor = db.query(TABLE_ITEMCATNEW, new String[] { KEY_ITEM_NEW , KEY_CATEGORY_NEW }, KEY_CATEGORY_NEW + "=?", new String[] {String.valueOf(category) }, null, null, null, null);
 		if(cursor != null)
 			cursor.moveToFirst();
 		
 		ItemCatNew itemCatNew = new ItemCatNew(cursor.getString(0), cursor.getString(1));
 		
 		return itemCatNew;
 		
 	}
 	
 public List<ItemCatNew> getItemsNewList(String category){
 	List<ItemCatNew> itemList = new ArrayList<ItemCatNew>();
 	SQLiteDatabase db = this.getReadableDatabase();
 	
 	//Cursor cursor = db.query(TABLE_ITEMCATNEW, new String[] {KEY_ITEM_NEW , KEY_CATEGORY_NEW}, KEY_CATEGORY_NEW + "=" + category, new String[] {String.valueOf(KEY_ITEM_NEW)}, null, null, null, null);
 	//Cursor cursor = db.query(TABLE_ITEMCATNEW, new String[] {KEY_ITEM_NEW, KEY_CATEGORY_NEW}, null, new String[] {KEY_CATEGORY_NEW + "=?"}, null, null, null);
 	Cursor cursor = db.query(TABLE_ITEMCATNEW, new String[] { KEY_ITEM_NEW , KEY_CATEGORY_NEW }, KEY_CATEGORY_NEW + "=?", new String[] {String.valueOf(category) }, null, null, null, null);
 	
 	if(cursor.moveToFirst()){
 		do {
 			ItemCatNew itemcatnew = new ItemCatNew();
 			itemcatnew.setItemName(cursor.getString(0));
 			itemcatnew.setCatName(cursor.getString(1));
 			
 			itemList.add(itemcatnew);
 		} while(cursor.moveToNext());
 	}
 	return itemList;
 }
 	
 	// deleting itemCategories
 	
 	public void deleteItems(){
 		SQLiteDatabase db = this.getWritableDatabase();
 		String deleteSQL = "DELETE FROM " + TABLE_ITEMCATNEW;
 		db.execSQL(deleteSQL);
 	}
 	
    // getting all stores 
 	
 	public List<Store> getAllStores(){
 		List<Store> storeList = new ArrayList<Store>();
 		
 		String selectQuery = "SELECT * FROM " + TABLE_STORE;
 		SQLiteDatabase db = this.getWritableDatabase();
 		Cursor cursor = db.rawQuery(selectQuery, null);
 		
 		// looping through all rows and adding to the list
 		if(cursor.moveToFirst()){
 			
 			do  {
 				Store store = new Store();
 				store.setID(cursor.getInt(0));
 				store.setName(cursor.getString(1));
 				store.setCoordinates(cursor.getString(2));
 				
 			storeList.add(store);
 			} while(cursor.moveToNext());
 		}
 		
 		return storeList;
 	}
 	
 	// Need to write a method for retrieving category list .... 
 	
 	// getting all Categories 
 	
 	public List<Category> getAllCategories(){
 		
 		List<Category> categoryList = new ArrayList<Category>();
 		
 		String selectCat = "SELECT * FROM " + TABLE_CATEGORY;
 		SQLiteDatabase db = this.getWritableDatabase();
 		Cursor cursor = db.rawQuery(selectCat, null);
 		
 		// looping through all rows and adding to the list
 		
 		if(cursor.moveToFirst()){
 	
 			do {
 				Category category = new Category();
 				category.setCatName(cursor.getString(0)); // name loca, anch, store id
 				category.setCatLoc(cursor.getString(1));
 				category.setAnchPt(cursor.getString(2));
 				category.setStoreID(cursor.getInt(3));
 				
 				categoryList.add(category);
 			}  while(cursor.moveToNext());
 	}
 
 		return categoryList;
 	}
 	
 	
 	
 	public List<ItemCatNew> testItem(String category) { //String category
 		List<ItemCatNew> itemCatNewList = new ArrayList<ItemCatNew>();
 		
 		String selectItemCat = "SELECT * FROM " + TABLE_ITEMCATNEW;//  + " WHERE " + KEY_CATEGORY_NEW + " = " + category;
 		SQLiteDatabase db = this.getWritableDatabase();
 		Cursor cursor = db.query(TABLE_ITEMCATNEW, new String[] {KEY_ITEM_NEW, KEY_CATEGORY_NEW}, null, new String[] {KEY_CATEGORY_NEW + "=?"}, null, null, null);
 		
 		if(cursor.moveToFirst()){
 			
 			do{
 				ItemCatNew itc = new ItemCatNew();
 				itc.setItemName(cursor.getString(0));
 				itc.setCatName(cursor.getString(1));
 				
 				itemCatNewList.add(itc);
 			} while (cursor.moveToNext());
 		}
 	return itemCatNewList;
 	}
 }

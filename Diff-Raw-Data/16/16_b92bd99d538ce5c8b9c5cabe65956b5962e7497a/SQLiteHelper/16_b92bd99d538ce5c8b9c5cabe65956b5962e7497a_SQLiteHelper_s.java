 package ch.unibe.ese.shopnote.core.sqlite;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /**
  * All SQL definitions of the database (create statements) can be found in this class
  * It also takes the responsibility to open the database and update it if neccessary
  */
 public class SQLiteHelper extends SQLiteOpenHelper {
 
 	public static SQLiteHelper instance;
 	// Table definitions
 	// Save all Shopping lists with an unique ID
 	public static final String TABLE_LISTS = "shoppinglists";
 	public static final String COLUMN_LIST_ID = "listid";
 	public static final String COLUMN_LIST_NAME = "listname";
 	public static final String COLUMN_LIST_ARCHIVED = "archived";
 	public static final String COLUMN_LIST_DUEDATE = "duedate";
	public static final String COLUMN_LIST_SHARED = "shared";
 	public static final String COLUMN_LIST_CHANGESCOUNT = "changescount";
 	// Save all Items with an unique name and ID
 	public static final String TABLE_ITEMS = "items";
 	public static final String COLUMN_ITEM_ID = "itemid";
 	public static final String COLUMN_ITEM_NAME = "itemname";
 	// Save all shops with an unique name and ID
 	public static final String TABLE_SHOPS = "shops";
 	public static final String COLUMN_SHOP_ID = "shopid";
 	public static final String COLUMN_SHOP_NAME = "shopname";
 	// Link items to lists
 	public static final String TABLE_ITEMTOLIST = "itemtolist";
 	public static final String COLUMN_LISTPRICE = "listprice";
 	public static final String COLUMN_ITEMBOUGHT = "itembought";
 	public static final String COLUMN_ITEM_QUANTITY = "quantity";
 	public static final String COLUMN_ITEM_UNIT = "unit";
 	// Save all friends with an unique phoneNr
 	public static final String TABLE_FRIENDS = "friendlist";
 	public static final String COLUMN_FRIEND_ID = "friendId";
 	public static final String COLUMN_FRIEND_NAME = "friendname";
 	public static final String COLUMN_FRIEND_PHONENR = "friendphonenr";
 	public static final String COLUMN_FRIEND_HASAPP = "friendhasapp";
 	// Save friends to synced lists
 	public static final String TABLE_FRIENDSTOLIST = "friendstolist";
 	// Save friends to synced recipes
 	public static final String TABLE_FRIENDSTORECIPE = "friendstorecipe";
 	// Save all recipes with an unique id and name
 	public static final String TABLE_RECIPES = "recipelist";
 	public static final String COLUMN_RECIPE_ID = "recipeid";
 	public static final String COLUMN_RECIPE_NAME = "recipename";
 	public static final String COLUMN_RECIPE_NOTES = "recipenotes";
 	public static final String COLUMN_RECIPE_SHOWNOTES = "recipeshownotes";
	public static final String COLUMN_RECIPE_SHARED = "shared";
 	// Save all items to recipeid of a recipe
 	public static final String TABLE_ITEMTORECIPE = "itemtorecipe";
 
 	private static final String DATABASE_NAME = "shoppinglist.db";
 	private static final int DATABASE_VERSION = 2;
 
 	// Database creation sql statement
 	// create table for lists
 	private static final String DATABASE_CREATE_TABLE_LISTS =
 	"create table " + TABLE_LISTS + "(" //
 			+ COLUMN_LIST_ID + " integer primary key autoincrement, " //
 			+ COLUMN_LIST_NAME + " text NOT NULL, " //
 			+ COLUMN_LIST_ARCHIVED + " integer NOT NULL, "
 			+ COLUMN_LIST_DUEDATE + " integer, " //
 			+ COLUMN_SHOP_ID + " integer, " //
 			+ COLUMN_LIST_SHARED + " integer NOT NULL, "
 			+ COLUMN_LIST_CHANGESCOUNT + " integer NOT NULL, "
 			+ "FOREIGN KEY ("+COLUMN_SHOP_ID + ") REFERENCES "
 			+ TABLE_SHOPS + "(" + COLUMN_SHOP_ID + ")"
 			+ ");";
 	
 	// Create table for items
 	private static final String DATABASE_CREATE_TABLE_ITEMS = 
 			"create table " + TABLE_ITEMS + "("
 			+ COLUMN_ITEM_ID + " integer primary key autoincrement, "
 			+ COLUMN_ITEM_NAME + " varchar(30) "
 			+ ");";
 	
 	// link items to lists
 	private static final String DATABASE_CREATE_TABLE_ITEMTOLIST =
 			"create table " + TABLE_ITEMTOLIST + "("
 			+ COLUMN_ITEM_ID + " integer NOT NULL, "
 			+ COLUMN_LIST_ID + " integer NOT NULL, "
 			+ COLUMN_LISTPRICE + " NUMERIC, "
 			+ COLUMN_ITEMBOUGHT + " integer, "
 			+ COLUMN_ITEM_QUANTITY + " NUMERIC, "
 			+ COLUMN_ITEM_UNIT + " VARCHAR, "
 			+ "PRIMARY KEY (" + COLUMN_ITEM_ID + ", " + COLUMN_LIST_ID + "), "
 			+ "FOREIGN KEY (" + COLUMN_ITEM_ID + ") REFERENCES "
 			+ TABLE_ITEMS + "("+COLUMN_ITEM_ID+"), "
 			+ "FOREIGN KEY (" + COLUMN_LIST_ID+ ") REFERENCES "
 			+ TABLE_LISTS+ "("+COLUMN_LIST_ID+")"
 			+ ");";
 	
 	// Create table for Shops
 	private static final String DATABASE_CREATE_TABLE_SHOPS =
 			"create table " + TABLE_SHOPS + "("
 			+ COLUMN_SHOP_ID + " integer primary key autoincrement, "
 			+ COLUMN_SHOP_NAME + " varchar(30) "
 			+ ");";
 
 	private static final String DATABASE_CREATE_TABLE_FRIENDS =
 			"create table " + TABLE_FRIENDS +"("
 			+ COLUMN_FRIEND_ID + " integer primary key autoincrement, "
 			+ COLUMN_FRIEND_PHONENR + " varchar(30) NOT NULL, "
 			+ COLUMN_FRIEND_NAME + " varchar(30) NOT NULL, "
 			+ COLUMN_FRIEND_HASAPP + " integer "
 			+ ");";
 	
 	private static final String DATABASE_CREATE_TABLE_FRIENDSTOLIST =
 			"create table " + TABLE_FRIENDSTOLIST +"("
 			+ COLUMN_LIST_ID + " integer NOT NULL, "
 			+ COLUMN_FRIEND_ID + " integer NOT NULL, "
 			+ "FOREIGN KEY (" + COLUMN_LIST_ID + ") REFERENCES "
 			+ TABLE_LISTS + "("+ COLUMN_LIST_ID + "), "
 			+ "FOREIGN KEY (" + COLUMN_FRIEND_ID + ") REFERENCES "
 			+ TABLE_FRIENDS +"("+ COLUMN_FRIEND_ID + ") "
 			+ ");";
 	
 	private static final String DATABASE_CREATE_TABLE_FRIENDSTORECIPE =
 			"create table " + TABLE_FRIENDSTORECIPE +"("
 			+ COLUMN_RECIPE_ID + " integer NOT NULL, "
 			+ COLUMN_FRIEND_ID + " integer NOT NULL, "
 			+ "FOREIGN KEY (" + COLUMN_RECIPE_ID + ") REFERENCES "
 			+ TABLE_RECIPES + "("+ COLUMN_RECIPE_ID + "), "
 			+ "FOREIGN KEY (" + COLUMN_FRIEND_ID + ") REFERENCES "
 			+ TABLE_FRIENDS +"("+ COLUMN_FRIEND_ID + ") "
 			+ ");";
 
	private static final String DATABASE_CREATE_TABLE_RECIPE =
 			"create table " + TABLE_RECIPES + "("
 			+ COLUMN_RECIPE_ID + " integer primary key autoincrement, "
 			+ COLUMN_RECIPE_NAME + " text NOT NULL, "
 			+ COLUMN_RECIPE_NOTES + " text, "
			+ COLUMN_RECIPE_SHOWNOTES + " integer NOT NULL "
			+ COLUMN_RECIPE_SHARED + " integer NOT NULL, "
 			+ ");";
 	
 	private static final String DATABASE_CREATE_TABLE_ITEMTORECIPE =
 			"create table " + TABLE_ITEMTORECIPE + "("
 			+ COLUMN_RECIPE_ID + " integer NOT NULL, "
 			+ COLUMN_ITEM_ID + " integer NOT NULL, "
 			+ "PRIMARY KEY (" + COLUMN_RECIPE_ID + ", " + COLUMN_ITEM_ID + "), "
 			+ "FOREIGN KEY (" + COLUMN_RECIPE_ID + ") REFERENCES "
 			+ TABLE_RECIPES + "("+ COLUMN_RECIPE_ID +"), "
 			+ "FOREIGN KEY (" + COLUMN_ITEM_ID + ") REFERENCES "
 			+ TABLE_ITEMS +"("+ COLUMN_ITEM_ID + ") "
 			+ ");";
 
 	public static String[] LISTS_COLUMNS = { SQLiteHelper.COLUMN_LIST_ID,
 			SQLiteHelper.COLUMN_LIST_NAME, SQLiteHelper.COLUMN_LIST_ARCHIVED,
 			SQLiteHelper.COLUMN_LIST_DUEDATE, SQLiteHelper.COLUMN_SHOP_ID,
 			SQLiteHelper.COLUMN_LIST_SHARED, SQLiteHelper.COLUMN_LIST_CHANGESCOUNT};
 	public static String[] SHOPS_COLUMNS = { SQLiteHelper.COLUMN_SHOP_ID,
 			SQLiteHelper.COLUMN_SHOP_NAME };
 	public static String[] ITEMS_COLUMNS = { SQLiteHelper.COLUMN_ITEM_ID,
 			SQLiteHelper.COLUMN_ITEM_NAME };
 	public static String[] ITEMTOLIST_COLUMNS = { SQLiteHelper.COLUMN_ITEM_ID,
 			SQLiteHelper.COLUMN_LIST_ID, SQLiteHelper.COLUMN_LISTPRICE,
 			SQLiteHelper.COLUMN_ITEMBOUGHT, SQLiteHelper.COLUMN_ITEM_QUANTITY };
 	public static String[] FRIENDS_COLUMNS = { SQLiteHelper.COLUMN_FRIEND_ID,
 			SQLiteHelper.COLUMN_FRIEND_PHONENR, SQLiteHelper.COLUMN_FRIEND_NAME,
 			SQLiteHelper.COLUMN_FRIEND_HASAPP};
 	public static String[] FRIENDSTOLIST_COLUMNS = {SQLiteHelper.COLUMN_LIST_ID,
 			SQLiteHelper.COLUMN_FRIEND_ID};
 	public static String[] FRIENDSTORECIPE_COLUMNS = {SQLiteHelper.COLUMN_RECIPE_ID,
 		SQLiteHelper.COLUMN_RECIPE_ID};
 	public static String[] RECIPE_COLUMNS = { SQLiteHelper.COLUMN_RECIPE_ID,
 			SQLiteHelper.COLUMN_RECIPE_NAME, SQLiteHelper.COLUMN_RECIPE_NOTES,
 			SQLiteHelper.COLUMN_RECIPE_SHOWNOTES, SQLiteHelper.COLUMN_RECIPE_SHARED};
 	public static String[] ITEMTORECIPE_COLUMNS = {
 			SQLiteHelper.COLUMN_RECIPE_ID, SQLiteHelper.COLUMN_ITEM_ID };
 
 	public SQLiteHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		SQLiteHelper.instance = this;
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase database) {
 		database.execSQL(DATABASE_CREATE_TABLE_SHOPS);
 		database.execSQL(DATABASE_CREATE_TABLE_LISTS);
 		database.execSQL(DATABASE_CREATE_TABLE_ITEMS);
 		database.execSQL(DATABASE_CREATE_TABLE_ITEMTOLIST);
 		database.execSQL(DATABASE_CREATE_TABLE_FRIENDS);
 		database.execSQL(DATABASE_CREATE_TABLE_FRIENDSTOLIST);
 		database.execSQL(DATABASE_CREATE_TABLE_FRIENDSTORECIPE);
		database.execSQL(DATABASE_CREATE_TABLE_RECIPE);
 		database.execSQL(DATABASE_CREATE_TABLE_ITEMTORECIPE);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.w(SQLiteHelper.class.getName(), "Upgrading database from version "
 				+ oldVersion + " to " + newVersion
 				+ ", which will destroy all old data");
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTS);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOPS);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMTOLIST);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDSTOLIST);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDSTORECIPE);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMTORECIPE);
 		onCreate(db);
 	}
 }

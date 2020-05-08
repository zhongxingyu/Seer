 package moshe.apps.shoppinglist;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 public class DBAdapter {
 	
 	
 	
 	public static final String KEY_ROWID = "_id";
 	public static final String KEY_ISDONE = "isdone";
 	public static final String KEY_TITLE = "title";
 	public static final String KEY_QUANTITY="quantity";
 	public static final String KEY_LIST_ID="list_id";
 	
 	public static final String KEY_LISTID = "_id";
 	public static final String KEY_LISTNAME="listName";
 	
 	
 	
 	private static final String TAG = "DBAdapter";
 	private static final String DATABASE_NAME = "shopping_lists";
 	private static final String ITEMS_TABLE_NAME = "list_items";
 	private static final String LISTS_TABLE_NAME = "lists";
 	
 	
 	private static final int DATABASE_VERSION = 13;
 
 	
 	
 	
 	//create lists table
 	private static final String LISTS_TABLE_CREATE =
 	"create table lists ("
 	+" _id integer primary key autoincrement, "
 	+" listName text not null,"
 	+" recentlyUsed boolean  not null DEFAULT false"
 	+" );";
 	
 	//create items table
 	private static final String ITEMS_TABLE_CREATE =
 	"create table list_items ("
 	+"_id integer primary key autoincrement, "
 	+" isdone boolean  not null DEFAULT false,"
 	+" title text not null,"
 	+" list_id integer,"
 	+" quantity integer not null DEFAULT 1,"
 	+" FOREIGN KEY(list_id) REFERENCES lists(_id)"	
 	+" );";		
 
 	
 	private final Context context;
 	private DatabaseHelper DBHelper;
 	private SQLiteDatabase db;
 	public DBAdapter(Context ctx)
 	{
 	   this.context = ctx;
 	   DBHelper = new DatabaseHelper(context);
 	}	
 	
 	
 	
 	
 	private static class DatabaseHelper extends SQLiteOpenHelper
 	{
 		DatabaseHelper(Context context)
 		{
 		   super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 		@Override
 		public void onCreate(SQLiteDatabase db)
 		{
 			
 		   try {
 			   
 			   db.execSQL(LISTS_TABLE_CREATE );
 			   db.execSQL(ITEMS_TABLE_CREATE );
 			   
 			} 
 		    catch (Exception e){
 		    	e.printStackTrace();
 			}
 		}
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)		
 		{/*
 			Log.w(TAG, "Upgrading database from version " + oldVersion
 			+ " to "
 			+ newVersion + ", which will destroy all old data");*/
 			db.execSQL("DROP TABLE IF EXISTS " + ITEMS_TABLE_NAME);
 			db.execSQL("DROP TABLE IF EXISTS " + LISTS_TABLE_NAME);
 			onCreate(db);
 		}
 	}	
 	
 	public DBAdapter open() throws SQLException
 	{
 	     db = DBHelper.getWritableDatabase();
 	    
 	   return this;
 	}
 	//---closes the database---
 	public void close()
 	{
 	     DBHelper.close();
 	}
 	
 	//+++++++++++++++++++++++++++++++++++++++++++++++++
 	//LISTS:
 	
 	public long insertNewList( String listName){
 		
 		Long res=(long) 0;
 		try{
 		ContentValues initialValues = new ContentValues();
 
 		initialValues.put(KEY_LISTNAME,  listName);
 	
 		res= db.insert(LISTS_TABLE_NAME, null, initialValues);
 		} catch (Exception e) {
 			
 			e.printStackTrace();
 		}
 		return res;					
 	}
 	public Cursor getAllLists(){
 		
 		//long count=DatabaseUtils.queryNumEntries(db, "lists");
               
 		
 		
 		
 		Cursor c=null;
 		try {
 			c =db.query(LISTS_TABLE_NAME, new String[] {
 			KEY_LISTID,
 			KEY_LISTNAME
             },
 			null,
 			null,
 			null,
 			null,			
 			null
            );
 		} catch (Exception e) {
 			
 			e.printStackTrace();
 		}
 		return c;		
 		
 	}
 	
 	
 	//++++++++++++++++++++++++++++++++++++++++++++++++++
 	//IEMS:
 	
 	//---insert a item into the database---
	public long insertNewItem( String title, int list_id)
 	{
 		Long res=(long) 0;
 		try{
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_ISDONE, false);
 		initialValues.put(KEY_TITLE, title);
 		initialValues.put(KEY_QUANTITY, 1);
 		initialValues.put(KEY_LIST_ID, list_id);
 		res= db.insert(ITEMS_TABLE_NAME, null, initialValues);
 		} catch (Exception e) {
 			
 			e.printStackTrace();
 		}
 		return res;
 	}
 	//---deletes a particular title---
 	public boolean deleteItem(long rowId)
 	{
 		try {
 			return db.delete(ITEMS_TABLE_NAME, KEY_ROWID + "=" + rowId, null) > 0;
 		} catch (Exception e) {
 			
 			e.printStackTrace();
 		}
 		
 		return false;
 	}
 	//---retrieves all the titles---
 	public Cursor getAllItems(long ListId)
 	{
 		Cursor c=null;
 		try {
 			c =db.query(ITEMS_TABLE_NAME, new String[] {
 			KEY_ROWID,
 			KEY_ISDONE,
 			KEY_TITLE,
 			KEY_QUANTITY},
 			KEY_LIST_ID+"="+ListId,
 			null,
 			null,
 			null,
 			null);
 		} catch (Exception e) {
 			
 			e.printStackTrace();
 		}
 		return c;
 	}
 	//---retrieves a particular title---
 	public Cursor getItem(long rowId) throws SQLException
 	{
 		Cursor mCursor =null;
 		try {
 			mCursor =
 			db.query(true, ITEMS_TABLE_NAME, new String[] {
 			KEY_ROWID,
 			KEY_ISDONE,
 			KEY_TITLE,
 			KEY_QUANTITY
 			},
 			KEY_ROWID + "=" + rowId,
 			null,
 			null,
 			null,
 			null,
 			null);
 		} catch (Exception e) {
 			
 			e.printStackTrace();
 		}
 		if (mCursor != null) {
 			try{
 		       mCursor.moveToFirst();
 			}catch (Exception e) {
 				
 				e.printStackTrace();
 			}
 		}
 		return mCursor;
 	}
 	
 	//---updates the item details---
 	public boolean updateItem(long rowId, String title, int quantity)	
 	{
 		try {
 			ContentValues args = new ContentValues();
 			//args.put(KEY_ISDONE, isdone);
 			args.put(KEY_TITLE, title);
 			args.put(KEY_QUANTITY, quantity);
 			return db.update(ITEMS_TABLE_NAME, args,
 			KEY_ROWID + "=" + rowId, null) > 0;
 		} catch (Exception e) {
 			
 			e.printStackTrace();
 		}
 		
 		return false;
 		
 	}	
 	//---updates only isDone field of item
     public boolean updateIsDone( long rowId, boolean isdone)	
 	{
 		try {
 			ContentValues args = new ContentValues();
 			args.put(KEY_ISDONE, isdone);
 
 			return db.update(ITEMS_TABLE_NAME, args,
 			KEY_ROWID + "=" + rowId, null) > 0;
 		} catch (Exception e) {
 			
 			e.printStackTrace();
 		}
 		
 		return false;
 		
 	}	
 }

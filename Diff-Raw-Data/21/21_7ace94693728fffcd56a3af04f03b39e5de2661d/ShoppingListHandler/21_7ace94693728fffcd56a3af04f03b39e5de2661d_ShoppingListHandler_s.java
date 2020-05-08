 package com.eseteam9.shoppyapp;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Date;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteStatement;
 
 public class ShoppingListHandler extends LocalDatabaseHandler{
     public static final String TABLE_NAME = "shopping_lists";
     
     private static final String KEY_ID = "id";
     private static final String KEY_TITLE = "title";
     private static final String KEY_OWNER = "owner";
     private static final String KEY_ARCHIVED = "archived";
     private static final String KEY_TIMESTAMP = "timestamp";
     
     public ShoppingListHandler(Context context) {
     	super(context);
     }
     
     public static void createTable(SQLiteDatabase db) {
         String CREATE_SHOPPING_LISTS_TABLE = "CREATE TABLE "+ TABLE_NAME + "("
                 + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
         		+ KEY_TITLE + " TEXT, " + KEY_OWNER + " TEXT,"
                 + KEY_ARCHIVED + " BOOELAN, " + KEY_TIMESTAMP
                 + "DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";
         db.execSQL(CREATE_SHOPPING_LISTS_TABLE);
     }
     
 	public static void dropTable(SQLiteDatabase db) {
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
 	}
 	
     public void add(ShoppingList shoppingList) {
         SQLiteDatabase db = this.getWritableDatabase();
 
         ContentValues values = new ContentValues();
         values.put(KEY_TITLE, shoppingList.title);
         values.put(KEY_OWNER, shoppingList.owner);
         values.put(KEY_ARCHIVED, shoppingList.archived ? "TRUE" : "FALSE");
 
         db.insert(TABLE_NAME, null, values);
         db.close();
     }
     
     public void delete(int id) {
         SQLiteDatabase db = this.getWritableDatabase();
         db.delete(TABLE_NAME, KEY_ID + "=" + id, null);
         db.close();
     }
     
     public boolean existsEntry(String name) {
         SQLiteDatabase db = this.getWritableDatabase();
 
         String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + KEY_TITLE + " = '" + name + "'";
         Cursor cursor = db.rawQuery(selectQuery, null);
         if (cursor.getCount() > 0)
         	return true;
         db.close();
         return false;
     }    
 
     public ShoppingList get(int id) {
         SQLiteDatabase db = this.getReadableDatabase();
         
         String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + KEY_ID + " = " + String.valueOf(id);
         Cursor cursor = db.rawQuery(selectQuery, null);
 
         if (cursor != null)
             cursor.moveToFirst();
 
         db.close();
         return parseShoppingList(cursor);
     }
     
     public List<ShoppingList> getAll() {
         List<ShoppingList> shoppingLists = new ArrayList<ShoppingList>();
         
         String selectQuery = "SELECT  * FROM " + TABLE_NAME;
         
         SQLiteDatabase db = this.getWritableDatabase();
         Cursor cursor = db.rawQuery(selectQuery, null);
      
         if (cursor.moveToFirst()) {
             do {
                 shoppingLists.add(parseShoppingList(cursor));
             } while (cursor.moveToNext());
         }
 
         db.close();
         return shoppingLists;
     }
     
     public void update(int id,String name) {
   	  SQLiteDatabase db = this.getWritableDatabase();
   	  //db.update(TABLE_SHOPPING_LISTS, null, S_KEY_ID+ "="+ id, null);
  	  SQLiteStatement stmt = db.compileStatement("UPDATE TABLE_SHOPPING_LISTS  SET S_KEY_TITLE = name WHERE S_KEY_ID = id ");
   	  stmt.execute();
   	  db.close();
     }
     
     private ShoppingList parseShoppingList(Cursor c) {
     	return new ShoppingList(c.getInt(0),
     			c.getString(1),
     			c.getString(2),
                 c.getString(3) == "TRUE" ? true : false,
                 new Date(c.getLong(4)));
     }
 }

 package com.example.restfulclient.helpers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /* TODO add method:
  * update -> form
  * 
  */
 
 public class DatabaseHelper extends SQLiteOpenHelper
 {
     
     // FormDatas table name
     private static final String TABLE_CATS = "categories";
     private static final String TABLE_BOOKS = "books";
     
     private static final String DATABASE_NAME = "cats";
     
     private static final int DATABASE_VERSION = 6;
     
     // FormDatas Table Columns names
     private static final String KEY_ID = "id";
     
     private static final String KEY_CAT_ID = "cid";
     private static final String KEY_CAT_NAME = "name";
     
     private static final String KEY_BOOK_ID = "bid";
     private static final String KEY_BOOK_NAME = "name";
     private static final String KEY_BOOK_AUTHOR = "author";
     private static final String KEY_BOOK_ISBN = "isbn";
     private static final String KEY_BOOK_CAT = "cid";
     private static final String KEY_BOOK_PHOTO = "photo";
     
     public DatabaseHelper(Context context, String name, CursorFactory factory,
 	    int version)
     {
 	super(context, name, factory, version);
     }
     
     public DatabaseHelper(Context context)
     {
 	super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
     
     @Override
     public void onCreate(SQLiteDatabase db)
     {
 	String CREATE_CAT_TABLE = "CREATE TABLE " + TABLE_CATS + "(" + KEY_ID
 	        + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_CAT_ID + " TEXT,"
 	        + KEY_CAT_NAME + " TEXT" + ")";
 	
 	String CREATE_BOOK_TABLE = "CREATE TABLE " + TABLE_BOOKS + "(" 
 		+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," // id
 	        + KEY_BOOK_NAME + " TEXT, " // name
 	        + KEY_BOOK_AUTHOR + " TEXT, " // author
 	        + KEY_BOOK_ISBN + " TEXT, " // isbn
 	        + KEY_BOOK_CAT + " TEXT, " // cid
 	        + KEY_BOOK_ID + " TEXT, " // bid
 	        + KEY_BOOK_PHOTO + " TEXT " // photo
 	        + ")";
 	
 	db.execSQL(CREATE_CAT_TABLE);
 	db.execSQL(CREATE_BOOK_TABLE);
     }
     
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
     {
 	// Drop older table if existed
 	db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
 	db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATS);
 	
 	// Create tables again
 	onCreate(db);
 	
     }
     
     public void addCategory(Category c)
     {
 	SQLiteDatabase db = this.getWritableDatabase();
 	
 	ContentValues values = packToValues(c);
 	
 	// Inserting Row
 	db.insert(TABLE_CATS, null, values);
 	// db.close(); // Closing database connection
     }
     
     public void addBook(Book b)
     {
 	SQLiteDatabase db = this.getWritableDatabase();
 	
 	ContentValues values = packToValues(b);
 	
 	// Inserting Row
 	db.insert(TABLE_BOOKS, null, values);
 	// db.close(); // Closing database connection
     }
     
     public List<Category> getAllCats()
     {
 	List<Category> catList = new ArrayList<Category>();
 	// Select All Query
 	String selectQuery = "SELECT  * FROM " + TABLE_CATS;
 	
 	SQLiteDatabase db = this.getReadableDatabase();
 	Cursor cursor = db.rawQuery(selectQuery, null);
 	
 	if (cursor.moveToFirst())
 	{
 	    do
 	    {
 		Category cat = packToCat(cursor);
 		
 		catList.add(cat);
 	    } while (cursor.moveToNext());
 	}
 	cursor.close();
 	db.close();
 	
 	return catList;
     }
     
     public List<Book> getAllBooks()
     {
 	List<Book> formList = new ArrayList<Book>();
 	// Select All Query
 	String selectQuery = "SELECT  * FROM " + TABLE_BOOKS;
 	
 	SQLiteDatabase db = this.getWritableDatabase();
 	Cursor cursor = db.rawQuery(selectQuery, null);
 	
 	if (cursor.moveToFirst())
 	{
 	    do
 	    {
 		Book form = packToBook(cursor);
 		
 		formList.add(form);
 	    } while (cursor.moveToNext());
 	}
 	cursor.close();
 	db.close();
 	
 	return formList;
     }
     
     public int getCatCount()
     {
 	
 	String countQuery = "SELECT  * FROM " + TABLE_CATS;
 	SQLiteDatabase db = this.getReadableDatabase();
 	
 	Cursor cursor = db.rawQuery(countQuery, null);
 	
 	int count = cursor.getCount();
 	cursor.close();
 	db.close();
 	
 	// return count
 	return count;
     }
     
     public int getBookCount()
     {
 	
 	String countQuery = "SELECT  * FROM " + TABLE_BOOKS;
 	SQLiteDatabase db = this.getReadableDatabase();
 	
 	Cursor cursor = db.rawQuery(countQuery, null);
 	
 	int count = cursor.getCount();
 	cursor.close();
 	db.close();
 	
 	// return count
 	return count;
     }
     
     public void dropDb()
     {
 	SQLiteDatabase db = this.getReadableDatabase();
 	
 	db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATS);
 	db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
 	
 	onCreate(db);
 	
 	db.close();
     }
     
     public Category getCatByCid(String cid)
     {
 	Category c = new Category();
 	// Select All Query
 	
 	String selectQuery = "SELECT  * FROM " + TABLE_CATS + " WHERE "
 	        + KEY_BOOK_CAT + "='" + cid + "'";
 	
 	SQLiteDatabase db = this.getReadableDatabase();
 	Cursor cursor = db.rawQuery(selectQuery, null);
 	
 	if (cursor.moveToFirst())
 	{
 	    c = packToCat(cursor);
 	}
 	cursor.close();
 	db.close();
 	
 	return c;
     }
     
     public Book getBookByBid(String bid)
     {
 	Book b = new Book();
 	// Select All Query
 	
 	String selectQuery = "SELECT  * FROM " + TABLE_BOOKS + " WHERE bid = "
 	        + bid;
 	
 	SQLiteDatabase db = this.getWritableDatabase();
 	Cursor cursor = db.rawQuery(selectQuery, null);
 	
 	if (cursor.moveToFirst())
 	{
 	    b = packToBook(cursor);
 	}
 	cursor.close();
 	db.close();
 	
 	return b;
     }
     
     public List<Book> getBooksByCid(String cid)
     {
 	List<Book> lb = new ArrayList<Book>();
 	
 	SQLiteDatabase db = this.getReadableDatabase();
 	Cursor cursor = db.rawQuery("SELECT  * FROM " + TABLE_BOOKS
 	        + " WHERE cid='" + cid + "'", null);
 	
 	Log.v("ilosc ksiazek w kat " + cid + ": ",
 	        String.valueOf(cursor.getCount()));
 	
 	if (cursor.moveToFirst())
 	{
 	    do
 	    {
 		Book b = packToBook(cursor);
 		
 		lb.add(b);
 	    } while (cursor.moveToNext());
 	}
 	cursor.close();
 	db.close();
 	
 	return lb;
     }
     
     private Category packToCat(Cursor cursor)
     {
 	Category c = new Category();
 	
 	c.setCategoryId(cursor.getString(1));
 	c.setCategoryName(cursor.getString(2));
 	
 	return c;
     }
     
     private Book packToBook(Cursor cursor)
     {
 	Book b = new Book();
 	
 	b.setBookName(cursor.getString(1));
 	b.setAuthor(cursor.getString(2));
 	b.setBookISBNnumber(cursor.getString(3));
 	b.setCatId(cursor.getString(4));
 	b.setBookId(cursor.getString(5));
 	b.setPhoto(cursor.getString(6));
 	
 	return b;
     }
     
     public int update(Category c)
     {
 	SQLiteDatabase db = getWritableDatabase();
 	
 	ContentValues cv = packToValues(c);
 	
	return db.update(TABLE_CATS, cv, KEY_CAT_ID + " = '" + c.getCategoryId() + "'", null);
     }
     
     public int update(Book b)
     {
 	SQLiteDatabase db = getWritableDatabase();
 	
 	ContentValues cv = packToValues(b);
 	
	return db.update(TABLE_BOOKS, cv, KEY_BOOK_ID + " = '" + b.getBookId() + "'", null);
     }
     
     private ContentValues packToValues(Category c)
     {
 	ContentValues values = new ContentValues();
 	values.put(KEY_CAT_NAME, c.getCategoryName());
 	values.put(KEY_CAT_ID, c.getCategoryId());
 	return values;
     }
     
     private ContentValues packToValues(Book b)
     {
 	ContentValues values = new ContentValues();
 	values.put(KEY_BOOK_ID, b.getBookId());
 	values.put(KEY_BOOK_NAME, b.getBookName());
 	values.put(KEY_BOOK_AUTHOR, b.getAuthor());
 	values.put(KEY_BOOK_ISBN, b.getBookISBNnumber());
 	values.put(KEY_BOOK_CAT, b.getCatId());
 	values.put(KEY_BOOK_PHOTO, b.getPhoto());
 	
 	return values;
     }
     
     public void delete(Category category)
     {
 	SQLiteDatabase db = getWritableDatabase();
	Log.v("delete Cat: ", category.toString());
	db.delete(TABLE_CATS, KEY_CAT_ID + " = '" + category.getCategoryId() + "'", null);
 	
     }
 }

 package com.ngapham.englishlearningkit;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class WordDatabase {
 
 	private DatabaseHelper myDbHelper;
 	private SQLiteDatabase myDb;
 	private final Context myContext;
 	private Cursor myCursor;
 	private ContentValues myValue;
 	
 	public static final String DATABASE_NAME = "DICTIONARY2.db";
 	public static final int DATABASE_VERSION = 1;
 	public static final String TABLE_NAME = "word";
 	public static final String WORD_ID = "id";
 	public static final String WORD_NAME = "name";
 	public static final String WORD_MEANING = "meaning";
 	public static final String WORD_TYPE = "type";
 	private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
 												  WORD_ID + " integer primary key autoincrement, " +
 												  WORD_NAME + " text not null, " +
 												  WORD_TYPE + " text not null, " +
 												  WORD_MEANING + " text not null);";
 	private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
 	
 	public static class DatabaseHelper extends SQLiteOpenHelper {
 		public DatabaseHelper(Context context, String name,
 				CursorFactory factory, int version) {
 			super(context, name, factory, version);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			try {
 				Log.i("Test", "Creating db " + DATABASE_NAME);
 				db.execSQL(CREATE_TABLE);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.i("test", "Upgrading Db " + DATABASE_NAME);
 			db.execSQL(DROP_TABLE);		//Kiem tra table, neu khong giong thi drop
 			onCreate(db);				//Tao moi CSDL
 		}
 //		
 //		@Override
 //		public void onOpen(SQLiteDatabase db) {
 //			Log.i("Test", "Open db...");
 //			db = SQLiteDatabase.openOrCreateDatabase(DATABASE_NAME, null, null);
 //		}
 		
 	}
 	
 	//Constructor
 	public WordDatabase(Context context)
 	{
 		myContext = context;
 	}
 	
 	//Mo CSDL
 	public void openToWrite()
 	{
 		myDbHelper = new DatabaseHelper(myContext, DATABASE_NAME, null, DATABASE_VERSION);
 		myDb = myDbHelper.getWritableDatabase();		//Mo database
 	}
 	
 	public void opentToRead()
 	{
 		myDbHelper = new DatabaseHelper(myContext, DATABASE_NAME, null, DATABASE_VERSION);
 		myDb = myDbHelper.getReadableDatabase();
 	}
 	
 	//Them ban ghi vao CSDL
 	public void insertWord(String name, String type, String meaning)
 	{
 		openToWrite();		//Mo database
 		myValue = new ContentValues();	//Dung de luu gia tri
 		myValue.put(WORD_NAME, name);
 		myValue.put(WORD_MEANING, meaning);
 		myValue.put(WORD_TYPE, type);
 		myDb.insert(TABLE_NAME, null, myValue);
 		myDb.close();	//Dong ket noi
 	}
 	
 	//Sua ban ghi trong CSDL dua vao id
 	public void updateWord(Word word)
 	{
 		openToWrite();
 		myValue = new ContentValues();
 		myValue.put(WORD_NAME, word.getName());
 		myValue.put(WORD_MEANING, word.getMeaning());
 		myValue.put(WORD_TYPE, word.getType());
 		myDb.update(TABLE_NAME, myValue, WORD_ID + "=?", new String[] {String.valueOf(word.getId())});
 		myDb.close();
 	}
 	
 	//Xoa ban ghi trong CSDL dua vao id
 	public void deleteWord(Word word)
 	{
 		openToWrite();
 		myValue = new ContentValues();
 		myDb.delete(TABLE_NAME, WORD_ID + "=?", new String[] {String.valueOf(word.getId())});
 		myDb.close();
 	}
 	
 	//Lay thong tin 1 Word dua vao id
 	public Word getWord(int id)
 	{
 		opentToRead();
 		String query = "SELECT * FROM " + TABLE_NAME;
 		//myCursor = myDb.rawQuery(query, new String[] {String.valueOf(id)});
		myCursor = myDb.query(TABLE_NAME, new String[] {WORD_ID}, "id=?" + id, null, null, null, null);
 		//Kiem tra du lieu rong hay khong
 		if (myCursor != null)
 		{
 			myCursor.moveToFirst();
 			return new Word(Integer.parseInt(myCursor.getString(0)), myCursor.getString(1), myCursor.getString(2), myCursor.getString(3));
 		}
 		else return null;
 	}
 	
 	//Lay thong tin 1 Word dua vao name
 		public Word getWord(String name)
 		{
 			opentToRead();
 			String query = "SELECT * FROM " + TABLE_NAME;
 			//myCursor = myDb.rawQuery(query, new String[] {name});
 			myCursor = myDb.query(TABLE_NAME, new String[] {WORD_ID, WORD_NAME, WORD_TYPE, WORD_MEANING}, "name='" + name + "'", null, null, null, null);
 			//Kiem tra du lieu rong hay khong
 			if (myCursor != null)
 			{
 				myCursor.moveToFirst();
 				return new Word(Integer.parseInt(myCursor.getString(0)), myCursor.getString(1), myCursor.getString(2), myCursor.getString(3));
 			}
 			else return null;
 		}
 	
 	//Lay ra toan bo Words
 	public List<Word> getAllWord()
 	{
 		List<Word> listOfWord = new ArrayList<Word>();
 		opentToRead();
 		String query = "SELECT * FROM " + TABLE_NAME;
 		//myCursor = myDb.rawQuery(query, null);
 		myCursor = myDb.query(TABLE_NAME, new String[] {WORD_ID, WORD_NAME, WORD_TYPE, WORD_MEANING}, null, null, null, null, null);
 		if (myCursor != null)
 		{
 			myCursor.moveToFirst();
 			do {
 				Word newWord = new Word(Integer.parseInt(myCursor.getString(0)), myCursor.getString(1), myCursor.getString(2), myCursor.getString(3));
 				listOfWord.add(newWord);
 			} while (myCursor.moveToNext());
 			myDb.close();
 			return listOfWord;
 		}
 		else
 		{
 			myDb.close();
 			return null;
 		}
 		
 	}
 	
 }

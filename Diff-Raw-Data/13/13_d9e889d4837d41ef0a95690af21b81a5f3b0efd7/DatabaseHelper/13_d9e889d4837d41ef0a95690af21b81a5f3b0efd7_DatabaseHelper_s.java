 package ngocnv371.Money.Model;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.security.InvalidKeyException;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 	private static String DB_PATH = "/data/data/ngocnv371.Money/databases/";
 	private static String DB_NAME = "transactions.db";
 		
 	private SQLiteDatabase mDatabase;
 	private final Context mContext;
 	
 	public DatabaseHelper(Context context) throws IOException {
 		super(context, DB_NAME, null, 1);
 		mContext = context;
 		createDataBase();
 		openDataBase();
 	}
 	
 	public Cursor getItems(String similar) {
 		return mDatabase.query("Items", new String[] { "_id", "Name", "Price"}, "Name like '%" + similar + "%'", null, null, null, null);		
 	}
 	
 	public Cursor getTags(String similar) {
 		return mDatabase.query("Tags", new String[] { "_id", "Name"}, "Name like '%" + similar + "%'", null, null, null, null);
 	}
 	
 	public Cursor getItemTags(long itemId) {
 		return mDatabase.rawQuery("Select _id, Name from (Select TagId From ItemTags Where ItemId = " + itemId + ") it Join Tags t on it.TagId = t._id", null);
 	}
 	
 	public Cursor getTransactions(String similar) {
 		return mDatabase.rawQuery("Select t._id, i._id, i.Name, Spend, Notes, Year, Month, Day, Hour, Minute From Transactions t Join Items i on t.ItemId = i._id Where Name Like '%" + similar + "%'", null);
 	}
 	
 	public Transaction getTransaction(long id) {
 		Cursor cursor = mDatabase.rawQuery("Select t._id, i._id, i.Name, Spend, Notes, Year, Month, Day, Hour, Minute From Transactions t Join Items i on t.ItemId = i._id Where t._id = " + id, null);
 		if (cursor.moveToFirst()) {
 			return new Transaction(cursor);
 		}
 		
 		return null;
 	}
 	
 	private long isExistedItem(String name) {
 		Cursor cursor = mDatabase.rawQuery("Select _id From Items Where Name = '" + name + "'", null);
 		if (cursor.moveToFirst()) {
 			long id = cursor.getLong(0);
 			cursor.close();
 			return id;
 		}
 		
 		return 0;
 	}
 	
 	private long isExistedTag(String name) {
 		Cursor cursor = mDatabase.rawQuery("Select _id From Tags Where Name = '" + name + "'", null);
 		if (cursor.moveToFirst()) {
 			long id = cursor.getLong(0);
 			cursor.close();
 			return id;
 		}
 		
 		return 0;
 	}
 	
 	public void addTag(long itemId, long tagId) {
 		ContentValues cv = new ContentValues();
 		cv.put("ItemId", itemId);
 		cv.put("TagId", tagId);
 		mDatabase.insertOrThrow("ItemTags", null, cv);
 	}
 	
 	public void removeTag(long itemId) {
 		mDatabase.execSQL("Delete From ItemTags Where ItemId = " + itemId);
 	}
 	
 	public long updateTransaction(long id, String itemName, int spend, String notes, String tags, int year, int month, int day, int hour, int minute) throws InvalidKeyException
 	{
		// remove old tags
		removeTag(id);
 		// update
 		ContentValues cv = new ContentValues();
 		cv.put("Spend", spend);
 		cv.put("Notes", notes);
 		cv.put("Year", year);
 		cv.put("Month", month);
 		cv.put("Day", day);
 		cv.put("Hour", hour);
 		cv.put("Minute", minute);
 		long itemId = isExistedItem(itemName);
		String[] tag = tags.split(",");
 		for (int i = 0; i < tag.length; i++) {
 			long tagId = isExistedTag(tag[i].trim());
 			addTag(itemId, tagId > 0 ? tagId : createTag(tag[i].trim()));
 		}
 		cv.put("ItemId", itemId > 0 ? updateItem(itemId, spend) : createItem(itemName, spend));
 		return mDatabase.update("Transactions", cv, "_id = " + id, null);
 	}
 	
 	public long createTransaction(String itemName, int spend, String notes, String tags, int year, int month, int day, int hour, int minute) throws InvalidKeyException
 	{
 		ContentValues cv = new ContentValues();
 		cv.put("Spend", spend);
 		cv.put("Notes", notes);
 		cv.put("Year", year);
 		cv.put("Month", month);
 		cv.put("Day", day);
 		cv.put("Hour", hour);
 		cv.put("Minute", minute);
 		long itemId = isExistedItem(itemName);
 		cv.put("ItemId", itemId > 0 ? updateItem(itemId, spend) : createItem(itemName, spend));
 		return mDatabase.insertOrThrow("Transactions", null, cv);
 	}
 	
 	public long updateItem(long id, int price) throws InvalidKeyException {
 		ContentValues cv = new ContentValues();
 		cv.put("Price", price);
 		if (mDatabase.update("Items", cv, "_id = " + id, null) == 0) {
 			throw new InvalidKeyException("The item with id " + id + " is not existed!");
 		}			
 		
 		return id;
 	}
 	
 	public long createTag(String name) {
 		ContentValues cv = new ContentValues();
 		cv.put("Name", name);
 		return mDatabase.insertOrThrow("Tags", null, cv);
 	}
 	
 	public long createItem(String name, int price)
 	{
 		ContentValues cv = new ContentValues();
 		cv.put("Name", name);
 		cv.put("Price", price);
 		return mDatabase.insertOrThrow("Items", null, cv);
 	}
 	
 	public void createDataBase() throws IOException {
 		boolean dbExisted = checkDataBase();		
 		if (!dbExisted) {
 			this.getReadableDatabase();
 			try {
 				copyDataBase();
 			} catch (IOException e) {
 				throw new Error("An error occured on copying database");
 			}
 		}
 	}
 	
 	private void copyDataBase() throws IOException {
 		InputStream source = mContext.getAssets().open(DB_NAME);				
 		OutputStream target = new FileOutputStream(DB_PATH + DB_NAME);
 		byte[] buffer = new byte[1024];
 		while (source.read(buffer) > 0) {
 			target.write(buffer);
 		}
 		target.flush();
 		target.close();
 		source.close();		
 	}
 	
 	public void openDataBase(){ 
 		mDatabase = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
 	}
 
 	@Override
 	public synchronized void close() {
 		if (mDatabase != null) {
 			mDatabase.close();
 		}
 		super.close();
 	}
 	
 	private boolean checkDataBase() {
 		SQLiteDatabase checkDB = null;		
 		try {
 			checkDB = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READONLY);
 		} catch (SQLException e) {
 			// not existed
 		}
 		
 		if (checkDB != null) {
 			checkDB.close();
 		}
 		
 		return checkDB != null;
 	}
 	
 	@Override
 	public void onCreate(SQLiteDatabase arg0) {
 
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
 
 	}
 }

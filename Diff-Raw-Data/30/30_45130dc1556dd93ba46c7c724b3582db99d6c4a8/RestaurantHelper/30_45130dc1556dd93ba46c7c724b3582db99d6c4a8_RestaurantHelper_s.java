 package csci498.tthrailk.lunchlistfix;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.content.ContentValues;
 
 class RestaurantHelper extends SQLiteOpenHelper {
 
	private static final String DATABASE_NAME="lunchlist.db";
	private static final int SCHEMA_VERSION=1;
 
 	public RestaurantHelper(Context context) {
 		super(context, DATABASE_NAME, null, SCHEMA_VERSION);
 		
 	}
 	
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL("CREATE TABLE restaurants (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, address TEXT, type TEXT, notes TEXT);");
 	}
 	
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		//unnecessary until 2nd schema 
 	}
 	
 	public void insert(String name, String address, String type, String notes) {
 		ContentValues cv = new ContentValues();
 		
 		cv.put("name", name);
 		cv.put("address", address);
 		cv.put("type", type);
 		cv.put("notes", notes);
 		
 		getWritableDatabase().insert("restaurants", "name", cv);
 	}
 	
 	public Cursor getAll() {
		return getReadableDatabase().rawQuery("SELECT _id, name, address, type, notes FROM restuarants ORDER BY name", null);
 	}
 	
 	public String getName(Cursor cursor) {
 		return cursor.getString(1);
 	}
 	
 	public String getAddress(Cursor cursor) {
 		return cursor.getString(2);
 	}
 	
 	public String getType(Cursor cursor) {
 		return cursor.getString(3);
 	}
 	
 	public String getNotes(Cursor cursor) {
 		return cursor.getString(4);
 	}
 }

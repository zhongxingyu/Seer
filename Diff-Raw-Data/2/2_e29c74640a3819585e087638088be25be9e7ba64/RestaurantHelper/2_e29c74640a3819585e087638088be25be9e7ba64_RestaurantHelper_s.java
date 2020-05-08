 package csci498.strupper.munchlist;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class RestaurantHelper extends SQLiteOpenHelper {
   private static final String DB_NAME = "munchlist.db";
   private static final int VERSION = 2;
 
   RestaurantHelper(Context context) {
     super(context, DB_NAME, null, VERSION);
   }
 
   @Override
   public void onCreate(SQLiteDatabase db) {
     db.execSQL("CREATE TABLE restaurant (" +
         "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
         "name TEXT," +
         "address TEXT," +
         "type TEXT," +
         "notes TEXT," +
         "feed TEXT);");
   }
 
   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("ALTER TABLE restaurants ADD COLUMN feed TEXT");
   }
 
   public void insert(Restaurant r) {
     ContentValues cv = new ContentValues();
     cv.put("name", r.getName());
     cv.put("address", r.getAddress());
     cv.put("type", r.getType());
     cv.put("feed", r.getFeed());
     getWritableDatabase().insert("restaurant", "name", cv);
   }
 
   public void update(String id, Restaurant r) {
     String[] args = {id};
     ContentValues cv = new ContentValues();
     cv.put("name", r.getName());
     cv.put("address", r.getAddress());
     cv.put("type", r.getType());
     cv.put("feed", r.getFeed());
 
     getWritableDatabase().update("restaurant", cv, "_ID=?",
                                   args);
 
   }
 
   public Cursor getById(String id) {
     String[] args = { id };
     return getReadableDatabase()
         .rawQuery("SELECT _id, name, address, type, feed FROM restaurant WHERE _ID=?",
                   args);
   }
 
   public Cursor getAll(String orderBy) {
     return getReadableDatabase().rawQuery(
       "SELECT * FROM restaurant ORDER BY " + orderBy,
       null);
   }
 
   /**
    * Helper method to unmarshal a restaurant from the current row of a cursor.
    * Don't you just love databases?
    *
    * @param c a cursor from the restaurant database.
    * @return the restaurant from the cursor's current row.
    */
   public static Restaurant restaurantOf(Cursor c) {
     return new Restaurant(c.getString(1),
                            c.getString(2),
                            c.getString(3),
                            c.getString(4));
   }
 }

 package csci498.lunchlist;
 
import csci498.lunchlist.R;
 import android.content.Context;
 import android.content.res.Resources;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 /**
  * RestaurantHelper provides an interface for LunchList to use a
  * persistable database for restaurant information storage
  */
 public class RestaurantHelper extends SQLiteOpenHelper {
 	
	private static final String DATABASE_NAME  = Resources.getSystem().getString(R.string.db_name);
 	private static final int    SCHEMA_VERSION = 1;
 	
 	public RestaurantHelper(Context context) {
 		super(context, DATABASE_NAME, null, SCHEMA_VERSION);
 	}
 	
 	@Override
 	public void onCreate(SQLiteDatabase db) {
		db.execSQL(Resources.getSystem().getString(R.string.db_create_table));
 	}
 	
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// no-op since this function will not be called until another db schema exists
 	}
 }

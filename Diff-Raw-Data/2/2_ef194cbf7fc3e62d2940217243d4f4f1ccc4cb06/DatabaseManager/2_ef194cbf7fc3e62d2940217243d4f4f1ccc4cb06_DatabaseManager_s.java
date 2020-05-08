 package io.meetme.database;
 
 import java.util.Date;
 
 import pl.drowidia.database.DatabaseHelper;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Looper;
 import android.util.Log;
 
 public class DatabaseManager extends DatabaseHelper {
 
     public static String TABLE = "users";
     public static String KEY_USER_ID = "user_id";
     public static String KEY_USER_NAME = "user_name";
     public static String KEY_DATE = "date";
 
     private static DatabaseManager INSTANCE;
     private static Context applicationContext;
     private static int databaseFile;
 
     private static OnUserAddedListener onUserAddedListener;
 
     public static void init(Context applicationContext, int databasefile) {
 	DatabaseManager.databaseFile = databasefile;
 	DatabaseManager.applicationContext = applicationContext;
 
 	if (applicationContext != applicationContext.getApplicationContext()) {
 	    throw new RuntimeException("Give me application context :)");
 	}
     }
 
     private DatabaseManager() {
 	super(applicationContext, databaseFile);
     }
 
     /**
      * Single connection to database.
      * http://touchlabblog.tumblr.com/post/24474750219/single-sqlite-connection
      */
     public static synchronized DatabaseManager getInstance() {
 	if (INSTANCE == null) {
 
 	    if (applicationContext == null) {
 		throw new RuntimeException("First init!");
 	    }
 	    INSTANCE = new DatabaseManager();
 	}
 
 	return INSTANCE;
     }
 
     public boolean add(User user) {
 
 	if (getUser(user.getUserID()) != null) {
 	    return false;
 	}
 
 	ContentValues contentValues = new ContentValues();
 	contentValues.put(KEY_USER_ID, user.getUserID());
 	contentValues.put(KEY_DATE, user.getMeetDate().getTime());
 
 	if (getWritableDatabase().insert(TABLE, null, contentValues) == -1) {
 	    return false;
 	}
 
 	if (onUserAddedListener != null) {
 	    onUserAddedListener.onAdded(user);
 	}
 
 	return true;
     }
 
     public int getPoints() {
 	int points = 0;
 
 	Cursor cursor = getReadableDatabase().query(true, TABLE, null, null,
 		null, null, null, null, null);
 	points = cursor.getCount();
 
 	cursor.close();
 
 	return points;
     }
 
     /**
      * @param userId
      * @return return null if no such user
      */
     public User getUser(String userId) {
 
 	Cursor query = getReadableDatabase().query(false, TABLE, null,
		KEY_USER_ID + "=" + userId, null, null, null, null, null);
 
 	if (query.moveToNext() == false) {
 	    query.close();
 	    return null;
 	}
 
 	User user = new User();
 
 	user.setUserID(query.getString(query.getColumnIndex(KEY_USER_ID)));
 	user.setName(query.getString(query.getColumnIndex(KEY_USER_NAME)));
 	user.setMeetDate(new Date(query.getLong(query.getColumnIndex(KEY_DATE))));
 	query.close();
 	return user;
     }
 
     @Override
     public synchronized SQLiteDatabase getReadableDatabase() {
 	if (Looper.myLooper() == Looper.getMainLooper()) {
 	    Log.w(this.toString(), "Running on UI thread database operation");
 	}
 	return super.getReadableDatabase();
     }
 
     @Override
     public synchronized SQLiteDatabase getWritableDatabase() {
 	if (Looper.myLooper() == Looper.getMainLooper()) {
 	    Log.w(this.toString(), "Running on UI thread database operation");
 	}
 	return super.getWritableDatabase();
     }
 
     public static OnUserAddedListener getOnUserAddedListener() {
 	return onUserAddedListener;
     }
 
     public static void setOnUserAddedListener(
 	    OnUserAddedListener onUserAddedListener) {
 	DatabaseManager.onUserAddedListener = onUserAddedListener;
     }
 
     public interface OnUserAddedListener {
 	public void onAdded(User user);
     }
 
 }

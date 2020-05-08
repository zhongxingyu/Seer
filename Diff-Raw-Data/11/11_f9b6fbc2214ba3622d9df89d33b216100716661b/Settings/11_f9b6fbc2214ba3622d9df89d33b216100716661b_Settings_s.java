 package android.topdown.game;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /**
  * @author delta11
  *
  */
 public class Settings {	
 	public static int level;
 	private Context mCtx;
     private DatabaseHelper mDbHelper;
     private SQLiteDatabase mDb;
     
 	private static final String TAG = "android.topdown.game";
     
     private static final String DATABASE_NAME = "TopDownGame";
     private static final String DATABASE_TABLE_USER = "User";
     private static final String DATABASE_TABLE_SCORES = "Scores";
     private static final int DATABASE_VERSION = 1;
     
     //User table
     private static final String KEY_INDEX = "indx";
     private static final String KEY_USER = "user";
     private static final String KEY_TOWN = "town";
     
     //HighScore Table *this table also uses al keys from the above table*
     private static final String KEY_SCORE = "score";
     private static final String KEY_LEVEL = "level";
     
     /**
      * @param context insert the context to the settings to save to private file areay
      */
     public Settings(Context context){
     	mCtx = context;
     }
     /**
      * Open the database, Throws exception if database not found
      *
      * @throws SQLException if the database could be neither opened or created
      */
     public void open() throws SQLException {
         mDbHelper = new DatabaseHelper(mCtx);
         mDb = mDbHelper.getWritableDatabase();
     }
 
     /**
      * close the database and make sure everything gets saved
      */
     public void close() {
         mDbHelper.close();
     }
 	/**
 	 * is there a user present?
 	 * @return true = y
 	 */
 	public boolean hasUser(){
 		if(getUsername().length()>0&&getTown().length()>0){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
     /**
      * set the current username
      * @param Username the name
      */
     public void setUsername(String Username)
     {
     	ContentValues args = new ContentValues();
 		args.put(KEY_USER, Username);
         mDb.update(DATABASE_TABLE_USER, args, KEY_INDEX + "=0", null);
     }
     /**
      * set the current town
      * @param Town the name for the town
      */
     public void setTown(String Town)
     {
     	ContentValues args = new ContentValues();
 		args.put(KEY_TOWN, Town);
         mDb.update(DATABASE_TABLE_USER, args, KEY_INDEX + "=0", null);
     }
     /**
      * get the current user his/her name
      * @return the name
      */
     public String getUsername()
     {
     	Cursor mCursor = mDb.query(true, DATABASE_TABLE_USER, new String[] {KEY_USER}, KEY_INDEX + "=0", null,null, null, null, null);
    	mCursor.moveToFirst();
     	return mCursor.getString(0);
     }
     /**
      * current user's town
      * @return the name of the town
      */
     public String getTown()
     {
     	Cursor mCursor = mDb.query(true, DATABASE_TABLE_USER, new String[] {KEY_TOWN}, KEY_INDEX + "=0", null,null, null, null, null);
    	mCursor.moveToFirst();
     	return mCursor.getString(0);
     }
     
     /**
      * insert a score in to the database
      * this score is automatically connected to the current user
      * @param level the level to which the score belong
      * @param score the score which this user got
      */
     public void insertScore(int level, int score){
     	ContentValues args = new ContentValues();
 		args.put(KEY_USER, getUsername());
 		args.put(KEY_TOWN, getTown());
 		args.put(KEY_LEVEL, level);
 		args.put(KEY_SCORE, score);
 		mDb.insert(DATABASE_TABLE_SCORES, null, args);
     }
     /**
      * get the top ten scores for
      * @param level this level
      * @return Array size 10 with as many scores as were found
      */
     public HighScore[] getHighScores(int level){
     	Cursor mCursor = mDb.query(true, DATABASE_TABLE_SCORES, new String[] {KEY_USER,KEY_TOWN,KEY_SCORE}, 
     			KEY_LEVEL+"="+level, null,null, null, KEY_SCORE+" DESC", null);
     	HighScore[] scores = new HighScore[10];
     	int count = 0;
     	if(mCursor.getCount()!=0)
     	{
     		mCursor.moveToFirst();
     		while (mCursor.isAfterLast() == false) 
     		{
     			scores[count] = new HighScore(mCursor.getInt(2), mCursor.getString(0), mCursor.getString(1));
     			count++;
     			mCursor.moveToNext();
     		}
     	}
     	
     	return scores;
     }
 	private static class DatabaseHelper extends SQLiteOpenHelper 
     {
 
         DatabaseHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) 
         {
             db.execSQL("CREATE TABLE "+DATABASE_TABLE_USER+" ("+KEY_INDEX+" integer primary key,"
                     + KEY_USER+" varchar," +
                     KEY_TOWN+" varchar);");
             db.execSQL("CREATE TABLE "+DATABASE_TABLE_SCORES+" ("+ KEY_INDEX + " integer primary key," 
             		+KEY_USER+" varchar not null,"
                     + KEY_TOWN+" varchar not null," +
                     KEY_SCORE+" int not null," +
                     KEY_LEVEL+" int not null);");
             ContentValues args = new ContentValues();
     		args.put(KEY_USER, "");
     		args.put(KEY_TOWN, "");
     		args.put(KEY_INDEX, 0);
             db.insert(DATABASE_TABLE_USER, null, args);
         }
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                     + newVersion + ", which will destroy all old data");
             db.execSQL("DROP TABLE *");
             onCreate(db);
 		}
     }
 }

 package edu.upenn.cis350.Trace2Learn;
 
 import java.util.List;
 
 import edu.upenn.cis350.Trace2Learn.Characters.LessonCharacter;
 import edu.upenn.cis350.Trace2Learn.Characters.Stroke;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 import android.graphics.PointF;
 import android.util.Log;
 
 public class DbAdapter {
 	
     public static final String CHAR_ROWID = "_id";
         
     public static final String CHARTAG_ROWID = "_id";
     public static final String CHARTAG_TAG= "tag";
     
     public static final String WORDTAG_ROWID = "_id";
     public static final String WORDTAG_TAG= "tag";
 
     private static final String TAG = "TagsDbAdapter";
     private DatabaseHelper mDbHelper;
     private SQLiteDatabase mDb;
 
     /**
      * Database creation sql statement
      */
     private static final String DATABASE_CREATE_CHAR =
         "CREATE TABLE Character (_id INTEGER PRIMARY KEY AUTOINCREMENT);";
     
     private static final String DATABASE_CREATE_CHARTAG =
             "CREATE TABLE CharacterTag (_id INTEGER, " +
             "tag TEXT NOT NULL, " +
             "FOREIGN KEY(_id) REFERENCES Character(_id));";
 
     private static final String DATABASE_CREATE_CHAR_DETAILS =
             "CREATE TABLE CharacterDetails (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
             "CharId INTEGER, " +
             "Stroke INTEGER NOT NULL, " +
             "PointX DOUBLE NOT NULL, " +
             "PointY DOUBLE NOT NULL," +
             "OrderPoint INTEGER NOT NULL, " +
             "FOREIGN KEY(CharId) REFERENCES Character(_id));";
     
     private static final String DATABASE_CREATE_WORDS =
             "CREATE TABLE Words (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
             "CharId INTEGER," +
            "WordOrder INTEGER NOT NULL," +
             "FlagUserCreated INTEGER," +
             "FOREIGN KEY(CharId) REFERENCES Character(_id));";
     
     private static final String DATABASE_CREATE_WORDSTAG =
             "CREATE TABLE WordsTag (_id INTEGER, " +
             "tag TEXT NOT NULL, " +
             "FOREIGN KEY(Wordid) REFERENCES Words(_id));";
 
     private static final String DATABASE_CREATE_LESSONS=
             "CREATE TABLE Lessons (_id INTEGER PRIMARY KEY AUTOINCREMENT);";
         
     private static final String DATABASE_CREATE_LESSONS_DETAILS =
             "CREATE TABLE LessonsDetails (" +
             "LessonId INTEGER, " +
             "WordId INTEGER," +
             "Order INTEGER NOT NULL, " +
             "FOREIGN KEY(LessonId) REFERENCES Lessons(_id)," +
             "FOREIGN KEY(WordId) REFERENCES Words(_id));";
     
     //DB Drop Statements
     
     private static final String DATABASE_DROP_CHAR = 
     		"DROP TABLE IF EXISTS Character";
     private static final String DATABASE_DROP_CHARTAG = 
     		"DROP TABLE IF EXISTS CharacterTag";
     private static final String DATABASE_DROP_CHAR_DETAILS = 
     		"DROP TABLE IF EXISTS CharacterDetails";
     private static final String DATABASE_DROP_WORDS = 
     		"DROP TABLE IF EXISTS Words";
     private static final String DATABASE_DROP_WORDSTAG = 
     		"DROP TABLE IF EXISTS WordsTag";
     private static final String DATABASE_DROP_LESSONS = 
     		"DROP TABLE IF EXISTS Lessons";
     private static final String DATABASE_DROP_LESSONS_DETAILS = 
     		"DROP TABLE IF EXISTS LessonsDetails";
     
     
     
     
     
     private static final String DATABASE_NAME = "CharTags";
     private static final String CHAR_TABLE = "Character";
     private static final String CHAR_DETAILS_TABLE = "CharacterDetails";
     private static final String CHARTAG_TABLE = "CharacterTag";
     private static final String WORDTAG_TABLE = "WordsTag";
     private static final String WORDS_TABLE = "Words";
     
     private static final int DATABASE_VERSION = 2;
 
     private final Context mCtx;
 
     private static class DatabaseHelper extends SQLiteOpenHelper {
 
         DatabaseHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
 
             db.execSQL(DATABASE_CREATE_CHAR);
             db.execSQL(DATABASE_CREATE_CHARTAG);
             db.execSQL(DATABASE_CREATE_CHAR_DETAILS);
             db.execSQL(DATABASE_CREATE_WORDS);
             db.execSQL(DATABASE_CREATE_WORDSTAG);
             db.execSQL(DATABASE_CREATE_LESSONS);
             db.execSQL(DATABASE_CREATE_LESSONS_DETAILS);
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                     + newVersion + ", which will destroy all old data");
             db.execSQL(DATABASE_DROP_CHAR);
             db.execSQL(DATABASE_DROP_CHARTAG);
             db.execSQL(DATABASE_DROP_CHAR_DETAILS);
             db.execSQL(DATABASE_DROP_WORDS);
             db.execSQL(DATABASE_DROP_WORDSTAG);
             db.execSQL(DATABASE_DROP_LESSONS);
             db.execSQL(DATABASE_DROP_LESSONS_DETAILS);
             onCreate(db);
         }
     }
 
     /**
      * Constructor - takes the context to allow the database to be
      * opened/created
      * 
      * @param ctx the Context within which to work
      */
     public DbAdapter(Context ctx) {
         this.mCtx = ctx;
     }
     
     /**
      * Open the CharTags database. If it cannot be opened, try to create a new
      * instance of the database. If it cannot be created, throw an exception to
      * signal the failure
      * 
      * @return this (self reference, allowing this to be chained in an
      *         initialization call)
      * @throws SQLException if the database could be neither opened or created
      */
     public DbAdapter open() throws SQLException {
         mDbHelper = new DatabaseHelper(mCtx);
         mDb = mDbHelper.getWritableDatabase();
         return this;
     }
     
     public void close() {
         mDbHelper.close();
     }
     
     /**
      * Create a new character. If the character is
      * successfully created return the new rowId for that note, otherwise return
      * a -1 to indicate failure.
      * 
      * @param id the row_id of the tag
      * @param tag the text of the tag
      * @return rowId or -1 if failed
      */
     public long createTags(long id, String tag) {
         ContentValues initialValues = new ContentValues();
         initialValues.put(CHARTAG_ROWID, id);
         initialValues.put(CHARTAG_TAG, tag);
 
         return mDb.insert(CHARTAG_TABLE, null, initialValues);
     }
     
     /**
      * Delete the tag with the given rowId and tag
      * 
      * @param rowId id of tag to delete
      * @param tag text of tag to delete
      * @return true if deleted, false otherwise
      */
     public boolean deleteTag(long rowId, String tag) {
 
         return mDb.delete(CHARTAG_TABLE, CHARTAG_ROWID + "=" + rowId + " AND " + CHARTAG_TAG+"="+tag, null) > 0;
     }
    
     /**
      * Add a character to the database
      * @param c character to be added to the database
      * @return true if character is added to DB.  False on error.
      */
     public boolean addCharacter(LessonCharacter c)
     {
     	mDb.beginTransaction();
     	//add to CHAR_TABLE
     	ContentValues initialCharValues = new ContentValues();
     	long id = mDb.insert(CHAR_TABLE, null, initialCharValues);
     	if(id == -1)
     	{
     		//if error
     		Log.e(CHAR_TABLE, "cannot add new character to table "+CHAR_TABLE);
     		mDb.endTransaction();
     		return false;
     	}
     	c.setId(id);
     	
     	//add each stroke to CHAR_DETAILS_TABLE
     	List<Stroke> l = c.getStrokes();
     	//stroke ordering
     	int strokeNumber=0;
     	for(Stroke s:l)
     	{
     		ContentValues strokeValues = new ContentValues();
     		strokeValues.put("CharId", id);
     		strokeValues.put("Stroke", strokeNumber);
     		//point ordering
     		int pointNumber=0;
     		for(PointF p : s.getSamplePoints())
     		{
     			strokeValues.put("PointX", p.x);
         		strokeValues.put("PointY", p.y);
         		strokeValues.put("OrderPoint", pointNumber);
         		long success = mDb.insert(CHAR_DETAILS_TABLE, null, strokeValues);
         		if(success == -1)
         		{	
         			//if error
         			Log.e(CHAR_DETAILS_TABLE,"cannot add stroke");
         			mDb.endTransaction();
         			return false;
         		}
         		pointNumber++;
     		}
     		strokeNumber++;
     	}
     	//need to add character as a word so that we can add them to lessons as not part of a word
     	ContentValues wordValues = new ContentValues();
     	wordValues.put("CharId", id);
     	wordValues.put("Order", 0);
     	wordValues.put("FlagUserCreated", 0);
     	long success = mDb.insert(WORDS_TABLE, null, wordValues);
 		if(success == -1)
 		{	
 			//if error
 			Log.e(WORDS_TABLE,"cannot add to table");
 			mDb.endTransaction();
 			return false;
 		}
     	
     	mDb.setTransactionSuccessful();
     	mDb.endTransaction();
     	return true;
     	
     }
     
     /**
      * Return a Cursor positioned at the tag that matches the given character's charId
      * 
      * @param charId id of character whose tags we want to retrieve
      * @return Cursor positioned to matching character, if found
      * @throws SQLException if character could not be found/retrieved
      */
     public Cursor getTags(long charId) throws SQLException {
 
         Cursor mCursor =
 
             mDb.query(true, CHARTAG_TABLE, new String[] {CHARTAG_TAG}, CHARTAG_ROWID + "=" + charId, null,
                     null, null, CHARTAG_TAG+" ASC", null);
         if (mCursor != null) {
         	Log.d(CHARTAG_TABLE, "not null");
             mCursor.moveToFirst();
             Log.d(CHARTAG_TABLE, Integer.toString(mCursor.getCount()));
         }
         return mCursor;
 
     }
     
     /**
      * Return a Cursor positioned at the character that matches the given tag
      * 
      * @param tag text of tag to match
      * @return Cursor positioned to matching character, if found
      * @throws SQLException if character could not be found/retrieved
      */
     public Cursor getChars(String tag) throws SQLException {
 
         Cursor mCursor =
 
             mDb.query(true, CHARTAG_TABLE, new String[] {CHARTAG_ROWID}, CHARTAG_TAG + "='" + tag+"'", null,
                     null, null, CHARTAG_ROWID + " ASC", null);
         if (mCursor != null) {
             mCursor.moveToFirst();
         }
         return mCursor;
 
     }
     
     /**
      * Return a Cursor positioned at the tag that matches the given word's wordId
      * 
      * @param wordId id of word whose tags we want to retrieve
      * @return Cursor positioned to matching tags, if found
      * @throws SQLException if word could not be found/retrieved
      */
     public Cursor getWordTags(long wordId) throws SQLException {
 
         Cursor mCursor =
 
             mDb.query(true, WORDTAG_TABLE, new String[] {WORDTAG_TAG}, WORDTAG_ROWID + "=" + wordId, null,
                     null, null, WORDTAG_TAG+" ASC", null);
         if (mCursor != null) {
             mCursor.moveToFirst();
         }
         return mCursor;
 
     }
     
     /**
      * Return a Cursor positioned at the word that matches the given tag
      * 
      * @param tag text of tag to match
      * @return Cursor positioned to matching word, if found
      * @throws SQLException if word could not be found/retrieved
      */
     public Cursor getWords(String tag) throws SQLException {
 
         Cursor mCursor =
 
             mDb.query(true, WORDTAG_TABLE, new String[] {WORDTAG_ROWID}, WORDTAG_TAG + "='" + tag+"'", null,
                     null, null, WORDTAG_ROWID + " ASC", null);
         if (mCursor != null) {
             mCursor.moveToFirst();
         }
         return mCursor;
 
     }
     
 }

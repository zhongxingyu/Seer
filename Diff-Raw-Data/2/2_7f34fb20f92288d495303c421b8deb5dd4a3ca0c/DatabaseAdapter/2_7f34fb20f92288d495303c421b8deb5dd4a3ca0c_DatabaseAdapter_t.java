 package com.fyodorwolf.studybudy.helpers;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DatabaseAdapter
 {
 	public final static boolean WIPE_DATABASE = false;
 	
 	protected static final String TAG = "DataAdapter";
     
 	//this is a singleton class
 	private static volatile DatabaseAdapter instance;
     private SQLiteDatabase mDb;
     private SQLiteHelper mDbHelper;
 
     private DatabaseAdapter(){}
     
     public void init(Context context){
         mDbHelper = new SQLiteHelper(context);
         this.createDatabase();
         this.open();
     }
 
 	public static DatabaseAdapter getInstance() {
 		if(instance == null){
 			synchronized(DatabaseAdapter.class){
 				if(instance == null){
 					instance = new DatabaseAdapter();
 				}
 			}
 		}
 		return instance;
 	}
 	
     private DatabaseAdapter createDatabase(){
         try{
             mDbHelper.createDataBase();
         } 
         catch (IOException mIOException){
             Log.e(TAG, mIOException.toString() + "  UnableToCreateDatabase");
         }
         return this;
     }
 	
     private DatabaseAdapter open(){
         try{
             mDbHelper.openDataBase();
             mDbHelper.close();
             mDb = mDbHelper.getReadableDatabase();
         } 
         catch (SQLException mSQLException){
             Log.e(TAG, "open >>"+ mSQLException.toString());
         }
         return this;
     }
     
     public void close(){
     	if(mDb != null){
     		mDb.close();
     	}
     }
 
     public Cursor getCursor(String query){
     	try{
 		    Cursor mCur = mDb.rawQuery(query, null);
 		    if (mCur!=null){
 		       mCur.moveToNext();
 		    }
 		    return mCur;
 		}
 		catch (SQLException mSQLException){
 		     Log.e(TAG, "getCursor >>"+ mSQLException.toString());
 	         throw mSQLException;
 		}
     }
     
 	public static String getSearchTermQuery(String term){
 		return 
 			"SELECT " +
 				"sec._id," +
 				"sec.name," +
 				"deck._id," +
 				"deck.name, " +
 				"card._id " +
 			"FROM " +
 			"	Card card "+
 			"JOIN Deck deck ON card.deckId = deck._id " +
 			"JOIN Section sec ON deck.sectionId = sec._id " +
 			"WHERE " +
 				"sec.name LIKE '%"+term+"%'" +
 				"or " +
 				"deck.name LIKE '%"+term+"%'" +
 				"or " +
 				"card.question LIKE '%"+term+"%' " +
 				"or " +
 				"card.answer LIKE '%"+term+"%' " +
 			"ORDER BY sec.name ASC";
 	}
 	
 	public static String getCardsWithIdsQuery(long[] cardIds) {
     	String ids = "";
     	for(long cardId:cardIds){
     		ids += Long.toString(cardId)+","; 
     	}
     	ids = ids.substring(0,ids.length()-1);
		return "SELECT c._id, c.question, c.answer, c.status, c.numberInDeck, p._id, p.filename, p.orderNum FROM Card c LEFT OUTER JOIN Photo p on p.cardId = c._id WHERE c._id IN ("+ids+")";
 	}
 	
 	public static String getCardsWithDeckIdQuery(long DeckId){
 		return "SELECT c._id, c.question, c.answer, c.status, c.numberInDeck, p._id, p.filename, p.orderNum FROM Card c LEFT OUTER JOIN Photo p on p.cardId = c._id WHERE deckId = "+DeckId;
 	}
 	
 	public static String getCardsWithDeckIdAndStatusQuery(long DeckId, int status){
 		return getCardsWithDeckIdQuery(DeckId)+" and status = "+status;
 	}
 	
 	public static String getCardUpdateStatusQuery(float cardId, int status){
 		return "UPDATE Card SET status = "+status+" WHERE  _id = "+cardId;
 	}
 	
 	public static String getCreateCardQuery(String question, String answer,long deckId) {
 		return "INSERT INTO Card (question,answer,deckId) VALUES (\""+question+"\",\""+answer+"\","+deckId+")";
 	}
 	
 	public static String getLastCardIdQuery() {
 		return "SELECT MAX(_id) from Card";
 	}
 
 	public static String getRemoveCardQuery(long cardId){
 		return "DELETE FROM Card WHERE _id = "+cardId;
 	}
 	
 	public static String getCreateSectionQuery(String sectionName) {
 		return "INSERT INTO Section (name) values (\""+sectionName+"\")";
 	}
 	
 	public static String getLastSectionIdQuery() {
 		return "SELECT MAX(_id) FROM Section";
 	}
 	
 	public static String getCreateDeckQuery(String deckName, long sectionId) {
 		return "INSERT INTO Deck (name,sectionId) values (\""+deckName+"\","+sectionId+")";
 	}
 	
 	public static String getCreatePhotoForLatestCardQuery(String[] imagePaths) {
 		String values= "";
 		int orderNum = 0;
 		for(String path: imagePaths){
 			values += "(\""+path+"\",(SELECT MAX(_id) from Card),"+orderNum+"),";
 			orderNum++;
 		}
 		values = values.substring(0,values.length()-1);
 		return "INSERT INTO Photo (filename,cardId,orderNum) values "+values;
 	}
 	public static String getPhotosWithCardIdQuery(long cardId){
 		return "SELECT _id, filename, orderNum FROM Photo where cardId = "+cardId;
 	}
 
 
 	public static String getRemoveDecksWithIdsQuery(long[] deckIds){
 		String ids = "";
     	for(long deckId:deckIds){
     		ids += Long.toString(deckId)+","; 
     	}
     	ids = ids.substring(0,ids.length()-1);
 		return "DELETE FROM Deck WHERE _id IN ("+ids+")";
 	}
 	
 	public static String getRemoveEmptySectionsQuery(){
 		return "DELETE FROM Section WHERE _id NOT IN (SELECT DISTINCT(sectionId) from Deck)";
 	}
 	
 	public static String getGroupedDeckQuery(){
 		return "SELECT " +
 					"sec._id," +
 					"sec.name," +
 					"deck._id," +
 					"deck.name " +
 				"FROM " +
 					"Section sec " +
 				"JOIN Deck deck ON deck.sectionId = sec._id " +
 				"ORDER BY sec.name ASC";
 	}
 	
 	
     
 /********************************************************************************************************************************************
  * 							Private Classes		 																							*
  ********************************************************************************************************************************************/
 	
 	private static class SQLiteHelper extends SQLiteOpenHelper{
 		
 		private static String TAG = "DataBaseHelper"; // Tag just for the LogCat window
 		//destination path (location) of our database on device
 		private static String DB_PATH = ""; 
 		private static String DB_NAME ="studyBudy.sqlite";// Database name
 		private SQLiteDatabase mDataBase; 
 		private final Context mContext;
 		
 		public SQLiteHelper(Context context) 
 		{
 		    super(context, DB_NAME, null, 1);// 1? its Database Version
 		    DB_PATH = context.getFilesDir() + "/../databases/";
 		    Log.d(TAG,DB_PATH);
 		    this.mContext = context;
 		}  
 
 		public void createDataBase() throws IOException
 		{
 		    //If database not exists copy it from the assets
 			boolean mDataBaseExist = checkDataBase();
 		    if(!mDataBaseExist)
 		    {
 		        this.getReadableDatabase();
 		        this.close();
 		        try 
 		        {
 		            //Copy the database from assests
 		            copyDataBase();
 		            Log.e(TAG, "createDatabase database created");
 		        } 
 		        catch (IOException mIOException) 
 		        {
 		            throw new Error("ErrorCopyingDataBase");
 		        }
 		    }
 		}
 		
 	    //Check that the database exists here: /data/data/your package/databases/Da Name
 	    private boolean checkDataBase()
 	    {
 	        File dbFile = new File(DB_PATH + DB_NAME);
 	        //Log.v("dbFile", dbFile + "   "+ dbFile.exists());
 	        if(WIPE_DATABASE){
 	        	dbFile.delete();
 	        }
 	        return dbFile.exists();
 	    }
 	
 	    //Copy the database from assets
 	    private void copyDataBase() throws IOException
 	    {
 	        InputStream mInput = mContext.getAssets().open(DB_NAME);
 	        OutputStream mOutput = new FileOutputStream(DB_PATH + DB_NAME);
 	        byte[] mBuffer = new byte[1024];
 	        int mLength;
 	        while ((mLength = mInput.read(mBuffer))>0)
 	        {
 	            mOutput.write(mBuffer, 0, mLength);
 	        }
 	        mOutput.flush();
 	        mOutput.close();
 	        mInput.close();
 	    }
 	
 	    //Open the database, so we can query it
 	    public boolean openDataBase() throws SQLException
 	    {
 	        String mPath = DB_PATH + DB_NAME;
 	        //Log.v("mPath", mPath);
 	        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
 	        return mDataBase != null;
 	    }
 	
 		@Override
 		public void onCreate(SQLiteDatabase db) {}
 	
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
 	}//E: SQLiteHelper
 
 }

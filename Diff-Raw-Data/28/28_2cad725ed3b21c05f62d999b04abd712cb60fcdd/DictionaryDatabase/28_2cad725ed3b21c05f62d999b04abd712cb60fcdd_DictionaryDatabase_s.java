 package wikem.chris.wikemv3;
 
 import android.app.SearchManager;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.os.Environment;
 import android.provider.BaseColumns;
 import android.util.Log;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 
 import wikem.chris.wikemv3.ExternalSQLHelper;
 
 
 /**
  * Contains logic to return specific words from the dictionary, and
  * load the dictionary table when it needs to be created.
  */
 public class DictionaryDatabase {
     private static final String TAG = "DictionaryDatabase";
     public static final String DELETION_TOKEN = "DELETE";
     public static final String REDIRECT_TOKEN = "REDIRECT";
 
     //The columns we'll include in the dictionary table
     public static final String KEY_WORD = SearchManager.SUGGEST_COLUMN_TEXT_1;
   //  public static final String KEY_DEFINITION = SearchManager.SUGGEST_COLUMN_TEXT_2; //optional second line for search suggestions
   //now adding categories
     public static final String KEY_DEFINITION = "DEFINITIONS"; //hash map will take care of what the actual name is
     public static final String WIKEM_CATEGORY = SearchManager.SUGGEST_COLUMN_TEXT_2;
     public static final String WIKEM_URI = "WIKEM_URI"; //now used to pass token to delete row
     public static final String FAVORITE = "FAVORITE";
     public static final String LAST_UPDATE = "LAST_UPDATE";
     public static final String BASE_STRING = BaseColumns._ID;
     private static final String DATABASE_NAME = "dictionary";
     private static final String FTS_VIRTUAL_TABLE = "FTSdictionary";
 
     //haha if change db_version. will keep crashing, as on upgrade keep getting called without having come from downloaderactivity. not meant to work this way
   //  private static int DATABASE_VERSION = 3; 
     private static int DATABASE_VERSION = 3; 
     
     private static ExternalSQLHelper e; //use here? 6/2011
     private static DictionaryOpenHelper mDatabaseOpenHelper;
     private static final HashMap<String,String> mColumnMap = buildColumnMap();
     
     /**
      * Constructor
      * @param context The Context within which to work, used to create the DB
      */
     public DictionaryDatabase(Context context) {
         mDatabaseOpenHelper = new DictionaryOpenHelper(context);
     }
      public static void upgrade(){
        	mDatabaseOpenHelper.close();
        	Log.d(TAG, "db upgrade called in dictdatabase");
        	SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        	mDatabaseOpenHelper.onUpgrade(db,DATABASE_VERSION, DATABASE_VERSION);
        }
      
  //a method i added to update
 public int updateContent(ContentValues values, String selection, String thePageToUpdate){
 	 
 	String[] whereArgs = new String [] { thePageToUpdate};
 	 SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
 	 int i = -1;
 	 
 	i = db.update(FTS_VIRTUAL_TABLE, values, KEY_WORD + "=?", whereArgs);
    
      return i;
 }
      // a method i added for inserting new column
  public long insert(ContentValues values){
 	 SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
 	 long rowID = -1;
 	 
     	 rowID = db.insert(FTS_VIRTUAL_TABLE, null, values);
      	 db.setTransactionSuccessful();
     
     	 return rowID;
  }
 /* 	public static void vacuum(){
  		//TODO try to fix werid db phenomena 0926 with reindex?
  		//don';t think anything will happen... only works on collated db... instead try vaccumm comand?
 		 Log.d(TAG, "will try vacuum");
 
  	   	 SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();   	 
  	   	// db.beginTransaction();
  	     try {
  	    	 //db.execSQL("REINDEX " + FTS_VIRTUAL_TABLE );
  	    	 // Log.d(TAG, "reindexed");
 
  	    	db.execSQL("VACUUM " + FTS_VIRTUAL_TABLE );
 			 
  			 Log.d(TAG, "vacuumed");
  	    	 //db.setTransactionSuccessful();
  	     } catch(Exception e){e.printStackTrace();Log.e("DD", "caught error vacuuming");}
  	     finally {
  	      // db.endTransaction();
  	       db.close();
  	     }
  	   	  
  	}
 */
  public static void updateDeleted() {
 		/*	 * int	 delete(String table, String whereClause, String[] whereArgs) Convenience method for deleting rows in the database.
 		 * returns the number of rows affected if a whereClause is passed in, 0 otherwise. To remove all rows and get a count pass "1" as the whereClause.
 		 */
    	 SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();   	 
    	 String[] whereArgs = new String[]{DELETION_TOKEN};
    	 
  //ck add try commit bc issues w db corruption in froyo 2.2 after deletions...
    	 db.beginTransaction();
      try {
     	 int check = db.delete(FTS_VIRTUAL_TABLE, WIKEM_URI + "=?", whereArgs);
     	 if (check ==0){
     		 Log.d(TAG, "nothing deleted");		 
     	 }
     	 else{
     		 Log.d(TAG,"congratulations, deletions made:" + Integer.toString(check) );
     	 }
 		 Log.d(TAG, "nothing try reindexing");		 
 
     	 db.execSQL("REINDEX " + FTS_VIRTUAL_TABLE );
 		 Log.d(TAG, "deleted entries and reindexed db");	 
 
     	 db.setTransactionSuccessful();
      } catch(Exception e){e.printStackTrace();Log.e("DD", "caught error in updateDeleted");}
      finally {
        db.endTransaction();
        db.close();
      }
    	  
 	} 
  
      //a method i added for updating a favorite column. the bool parameter is to add or remove.
      public static boolean addOrRemoveFavorite(String f, boolean add){
     	 
     	 SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
     	 String[] whereArgs = new String [] { f };
     	 ContentValues newFav = new ContentValues();
     	 if (add){
     		 newFav.put(FAVORITE, "1");
     	 }else{
     		 newFav.put(FAVORITE, "0");
     	 }
     	 int check = 0;
     	 
    //put 1 in favorite column where the keyword matches the passed string "f" ..i hope
     	  check = db.update(FTS_VIRTUAL_TABLE, newFav, KEY_WORD + "=?", whereArgs); //returns #row affectd
 		      
     	 if (check ==0){
     		 Log.d(TAG, "uh. addfav not working.. nothing changed for:" + f);
     		 return false;
     	 }
     	 else{
     		 Log.d(TAG,"add fav" + f +" row changed" );
     	 }
     	 return true;
      }
   
       
     /**
      * Builds a map for all columns that may be requested, which will be given to the 
      * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include 
      * all columns, even if the value is the key. This allows the ContentProvider to request
      * columns w/o the need to know real column names and create the alias itself.
      */
     private static HashMap<String,String> buildColumnMap() {
         HashMap<String,String> map = new HashMap<String,String>();
         map.put(KEY_WORD, KEY_WORD);
         map.put(KEY_DEFINITION, KEY_DEFINITION);
         map.put(WIKEM_CATEGORY, WIKEM_CATEGORY);
         map.put(WIKEM_URI, WIKEM_URI);
         map.put(FAVORITE, FAVORITE);
         map.put(LAST_UPDATE, LAST_UPDATE);
         
         map.put(BaseColumns._ID, "rowid AS " +
                 BaseColumns._ID);
         //changed rowid to keyword... don't think suggestions likes nonsequential id s/p deletions?
         map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                 SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
         map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +
                 SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
         return map;
     }
 
     /**
      * Returns a Cursor positioned at the word specified by rowId
      *
      * @param rowId id of word to retrieve
      * @param columns The columns to include, if null then all are included
      * @return Cursor positioned to matching word, or null if not found.
      */
     public Cursor getWord(String rowId, String[] columns) {
         String selection = "rowid = ?";
         String[] selectionArgs = new String[] {rowId};
 
         return query(selection, selectionArgs, columns);
 
         /* This builds a query that looks like:
          *     SELECT <columns> FROM <table> WHERE rowid = <rowId>
          */
     }
 
     //return the ID (as string) of rowID where the input to query is the Word (use for links)
     public  String getRowId(String word)
     {
  
     	String[] columns = new String[] {
     	        //   WIKEM_URI
     			BaseColumns._ID
     	          };
     	Cursor cursor = getWordMatches(word, columns);
     	
     	String rowIdofInterest = null;
    
     		if (cursor!=null ){
     			int index = cursor.getColumnIndex(BaseColumns._ID);
     			cursor.moveToFirst();
     			
 		    		if (index ==-1){
 		    			cursor.close();
 		    			return null;
 		    		}
 		    		else{
 		    		rowIdofInterest = cursor.getString(index);
 		    	
 				    	Log.d("DICTDATABASE", "getRowId: yo yo, this is the row ID:" + rowIdofInterest);
 				    	cursor.close();
 				    	return rowIdofInterest;
 		    		}
     		}
     		
     		return null;
     }
     /**
      * Returns a Cursor over all words that match the given query
      *
      * @param query The string to search for
      * @param columns The columns to include, if null then all are included
      * @return Cursor over all words that match, or null if none found.
      
      * 
      */
     public Cursor getEverything(String[] columns) {
      	String selection = null; //...ie return all
     	String[] selectionArgs = null; // selectionargs null
     	  Log.d(TAG, " lets get everything...but explicitly in order");
 		//return query(selection, selectionArgs, columns); //use the query metho dwith sort order paramenters
     	  return queryAll(selection, selectionArgs, columns, KEY_WORD + " ASC"  );
  	}
     public Cursor getBackup(String[] columns) {
      	String selection = null; //...ie return all
     	String[] selectionArgs = null; // selectionargs null
     	  Log.d(TAG, "beeear! lets get backup!");
 		//return query(selection, selectionArgs, columns);
     	  return queryExternalDb(selection, selectionArgs,columns); //test if this works
 	}
     public Cursor getFavorites( String[] columns) {
 		 String selection = FAVORITE + " MATCH ?";
 	     String [] selectionArgs = new String[]{"1"};
 	     Log.d(TAG, "yo lets get favorites");
 	      return query(selection, selectionArgs, columns); 
 		 
 	}
     public Cursor getLastUpdate( String query, String[] columns) {
     	String selection = KEY_WORD + " MATCH ?";
 		 String [] selectionArgs = new String[] {query};
 	      return query(selection, selectionArgs, columns); 
 		 
 	}
 /*
  * TODO: is this the fastest way to query the categories. on emulator
  * very slow. seems ok on my test phone hwvr
  */
 	public Cursor getAllCategory(String query, String[] columns) {
 		//String selection = WIKEM_CATEGORY + " MATCH ?";
 		String selection = WIKEM_CATEGORY + " LIKE ?";
 		//String[] selectionArgs = new String[]{query+"*"};
 		String[] selectionArgs = new String[]{"%" + query + "%"};
 		Log.d(TAG, "yo yo yo, custom category accessed");
  		
 		//return query(selection, selectionArgs, columns);
 	
 		//get categories in Alphabetical order
 		return queryAlphabetical(selection, selectionArgs, columns, KEY_WORD + " ASC"  );
  	}
 	
 	//  The LIKE command allows "Wild cards". A % may be used to match and string, _ will match any single character.
 	public Cursor getWordLike(String query, String[] columns){
 		String selection = KEY_WORD + " LIKE ?";
         String[] selectionArgs = new String[] {query+"%"};
         return query(selection, selectionArgs, columns);
 	}
     public  Cursor getWordMatches(String query, String[] columns) {
         String selection = KEY_WORD + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};

         return query(selection, selectionArgs, columns);
 
     }
     public  Cursor getWordMatchesFromTable(String query, String[] columns) { //get fulltext search
      	String selection = KEY_DEFINITION + " MATCH ?";
      	//String selection = FTS_VIRTUAL_TABLE + " MATCH ?";
         String[] selectionArgs = new String[] {query+"*"};
 
         return query(selection, selectionArgs, columns);
 
         /* This builds a query that looks like:
          *     SELECT <columns> FROM <table> WHERE < the table > MATCH 'query*'
          * which is an FTS3 search for the query text (plus a wildcard) inside the word column.
          *
          * - "rowid" is the unique id for all rows but we need this value for the "_id" column in
          *    order for the Adapters to work, so the columns need to make "_id" an alias for "rowid"
          * - "rowid" also needs to be used by the SUGGEST_COLUMN_INTENT_DATA alias in order
          *   for suggestions to carry the proper intent data.
          *   These aliases are defined in the DictionaryProvider when queries are made.
          * - This can be revised to also search the definition text with FTS3 by changing
          *   the selection clause to use FTS_VIRTUAL_TABLE instead of KEY_WORD (to search across
          *   the entire table, but sorting the relevance could be difficult.
          */
     }
     public Cursor getBackupWord(String rowId, String[] columns) { //mine.. to search table
     	String selection = DictionaryDatabase.KEY_WORD + " = ?";
         String[] selectionArgs = new String[] {rowId};
 
         return queryExternalDb(selection, selectionArgs, columns);
      }
 
     /**
      * Performs a database query.
      * @param selection The selection clause
      * @param selectionArgs Selection arguments for "?" components in the selection
      * @param columns The columns to return
      * @return A Cursor over all rows matching the query
      */
     private static Cursor query(String selection, String[] selectionArgs, String[] columns) {
         /* The SQLiteBuilder provides a map for all possible columns requested to
          * actual columns in the database, creating a simple column alias mechanism
          * by which the ContentProvider does not need to know the real column names
          */
         SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
         builder.setTables(FTS_VIRTUAL_TABLE);
         builder.setProjectionMap(mColumnMap);
    
         int currentapiVersion = android.os.Build.VERSION.SDK_INT;
         if (currentapiVersion >= android.os.Build.VERSION_CODES.FROYO){
             // Do something for froyo and above versions
             selection += " AND WIKEM_URI IS NOT ?";
             String delete = DELETION_TOKEN;
             selectionArgs = new String[]{selectionArgs[0], delete};
         } else{
             // do something for phones running an SDK before froyo
         	Log.d("dd", "before froyo query");
         }
        
         try{
         Cursor cursor = builder.query( mDatabaseOpenHelper.getReadableDatabase(),
                 columns, selection, selectionArgs, null, null, null);
 
         if (cursor == null) {
             return null;
         } else if (!cursor.moveToFirst()) {
             cursor.close();
             return null;
         }
         return cursor;
        }catch (Exception e) { e.printStackTrace(); Log.d("dictdatabase", " bad query"); errorInDBSoRebuild();}
        return null;
     }
     //added for categories, which need to be alphabetical also
     private static Cursor queryAlphabetical(String selection, String[] selectionArgs, String[] columns, String sortOrder) {
          
     	 SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
          builder.setTables(FTS_VIRTUAL_TABLE);
          builder.setProjectionMap(mColumnMap);
     
          int currentapiVersion = android.os.Build.VERSION.SDK_INT;
          if (currentapiVersion >= android.os.Build.VERSION_CODES.FROYO){
              // Do something for froyo and above versions
              selection += " AND WIKEM_URI IS NOT ?";
              String delete = DELETION_TOKEN;
              selectionArgs = new String[]{selectionArgs[0], delete};
          } else{
              // do something for phones running an SDK before froyo
          	Log.d("dd", "before froyo query");
          }
          
          try{
 		         Cursor cursor = builder.query( mDatabaseOpenHelper.getReadableDatabase(),
 		                 columns, selection, selectionArgs, null, null, sortOrder);
 		        if (cursor == null) {
 		            return null;
 		        } else if (!cursor.moveToFirst()) {
 		            cursor.close();
 		            return null;
 		        }
 		
 		        return cursor;
  
       }catch (Exception e) { e.printStackTrace(); Log.d("dictdatabase", " bad query"); errorInDBSoRebuild();}
       return null;
     }
     private static Cursor queryAll(String selection, String[] selectionArgs, String[] columns, String sortOrder) {
         /* extra parameter for sort. only called by getAll (giving no selection parameters) bc, android having isues with alphabetical unless 
          * giving query explicit sort order
          */
         SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
         builder.setTables(FTS_VIRTUAL_TABLE);
         builder.setProjectionMap(mColumnMap);
          
         int currentapiVersion = android.os.Build.VERSION.SDK_INT;
         if (currentapiVersion >= android.os.Build.VERSION_CODES.FROYO){
         	 String delete = DELETION_TOKEN;
              selection = "WIKEM_URI IS NOT ?";
          	selectionArgs = new String[]{delete};        } 
         else{
          	Log.d("dd", "before froyo queryall");
         }       
         Cursor cursor = null;
        try{ 
      	   cursor = builder.query( mDatabaseOpenHelper.getReadableDatabase(),
                 columns, selection, selectionArgs, null, null, sortOrder);
        }catch (Exception e) { e.printStackTrace();Log.d("dictdatabase", " bad query");errorInDBSoRebuild(); }
 
         if (cursor == null) {
             return null;
         } else if (!cursor.moveToFirst()) {
             cursor.close();
             return null;
         }
         return cursor;
     }
     private static void errorInDBSoRebuild(){
     	//reset so as to reinstall db in app 
     	SharedPreferences settings = mDatabaseOpenHelper.mHelperContext.getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);    
 	 	SharedPreferences.Editor editor = settings.edit();
              	editor.putBoolean("force_rebuild", true);
             	editor.commit();
             
     }
     
     public static void initializeDB(){ //for the external. otw was initializing too many times
     	 e = new ExternalSQLHelper (Singleton.DESTINATION_FILE); 
     }
     public static void closeExternalDB(){ //for the external
     	e.close();
     }
     private static Cursor queryExternalDb(String selection, String[] selectionArgs, String[] columns) {
         /* The SQLiteBuilder provides a map for all possible columns requested to
          * actual columns in the database, creating a simple column alias mechanism
          * by which the ContentProvider does not need to know the real column names
          */
         SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
         builder.setTables(FTS_VIRTUAL_TABLE);
         builder.setProjectionMap(mColumnMap);
         builder.setDistinct(true); //don't know if this works or not... still getting duplicates..
  
 		
         Cursor cursor = builder.query( e.getReadableDatabase(),
                 columns, selection, selectionArgs, null, null, null);
 
         if (cursor == null) { Log.d(TAG, "cursor in ext db is null");
             return null;
         } else if (!cursor.moveToFirst()) { Log.d(TAG, "cursor in ext db is empty");
             cursor.close();
             return null;
         }
         Log.d(TAG, "cursor in ext db has something!");
         return cursor;
     }
 
    
 
     /**
      * This creates/opens the database.
      */
     public static class DictionaryOpenHelper extends SQLiteOpenHelper {
     	 
         
         private   boolean copySuccess =false;;
 
         private final Context mHelperContext;
         private static SQLiteDatabase mDatabase;
 
         /* Note that FTS3 does not support column constraints and thus, you cannot
          * declare a primary key. However, "rowid" is automatically used as a unique
          * identifier, so when making requests, we will use "_id" as an alias for "rowid"
          */
         //constraint unique on conflict ignore so no duplicate wikem entry names
         
         private static final String FTS_TABLE_CREATE =
                     "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                     " USING fts3 (" +
                     KEY_WORD + " CONSTRAINT UNIQUE ON CONFLICT IGNORE "+ ", " +
                     KEY_DEFINITION + ", " +
                     WIKEM_CATEGORY + ", " + WIKEM_URI + ", " + FAVORITE + ", " + LAST_UPDATE + ");";
         
        
         
         DictionaryOpenHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
             mHelperContext = context;
         }
  
         
         
         @Override
         public void onCreate(SQLiteDatabase db) {
         	mDatabase = db;        
         	
         	if (copySuccess){        		
         		copySuccess = false; //reset bool.
         		//if copy of new db to old sucessful this is the last line of code
         		//returns to original call in Downloaderactivity.onDownloadSuccessful which then just finishes and restarts
         	}
         	else{ //ie brute force db copy failed, eg. on first time open app
         	//mDatabase = db;
             mDatabase.execSQL(FTS_TABLE_CREATE);
             /*
              * will be called for example when app opened for first time
              * just created a blank database with the correct tables... 
              */
                         
 		/*ck:no longer use dbloadactivity.class
             Intent intent = new Intent(mHelperContext, DbLoadActivity.class);
             intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //try without
             mHelperContext.startActivity(intent);
 		 */      	
             
         	}
   
         }
   
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         	
             Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                     + newVersion + ", which will destroy all old data");
             db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
             try{       
             bruteForceUpgrade(db.getPath()); //closes db and copies the downloaded android compiant db
            loadOldFavorites();
           // DictionaryDatabase.vacuum();
               int currentapiVersion = android.os.Build.VERSION.SDK_INT;
             if (currentapiVersion >= android.os.Build.VERSION_CODES.FROYO){
                 // Do nothing for froyo and above versions
             } else{
             	 deleteToBeDeletedRows();
             	 }
              this.close(); //ck added bc crashing on 2.2
             }catch (Exception e) { Log.e("dictdatabase", "?caught in onupgrade call");errorInDBSoRebuild(); }
     
         }
         
         
     	private void deleteToBeDeletedRows() {
 	// this is necessary bc we are loading a sql db and deletions outside of android crashed the app
     		DictionaryDatabase.updateDeleted();    		
     		}
     	
 		private void loadOldFavorites() {
     		SharedPreferences settings = mHelperContext.getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);
     		String favs = settings.getString("favorites", " "); //second thing is just default
     		Log.d("DBLOADACT", "favs are" + favs);
     		String [] temp;
     		//String delimiter = "\\."; //escaped period between  the favorites
     		String delimiter = ",,"; //double comma btw favorites to lessen chance of any future weirdness in case title of page has period or single comma
     		temp = favs.split(delimiter); //http://developer.android.com/reference/java/util/regex/Pattern.html
     		 for(int i =0; i < temp.length ; i++){
     			 DictionaryDatabase.addOrRemoveFavorite(temp[i],true);
     		 }    		 
     	}
     	
 		 private File getAppDir(){
 			  File d = null;
 				SharedPreferences settings = mHelperContext.getSharedPreferences(SearchableDictionary.PREFS_NAME, 0);
 	       		String path = settings.getString("dl-path", "null");
 	       		if (!path.equals("null")){
 	       			d = new File(path);
 	       			if (d.exists()){ 
 	       				Log.d("dictdatab", "path is " + path);
 		       			return d;
 	       			}
 	       		}
 	          //otherwise something is strange... either d is null or the path from the sharedprefs doesnt exist
  	      	   int currentapiVersion = android.os.Build.VERSION.SDK_INT;					
 		       		if (currentapiVersion >= android.os.Build.VERSION_CODES.FROYO){
 		               // Do something for froyo and above versions
 		  			  d = mHelperContext.getExternalFilesDir(null);
 		           } else {
 		               // do something for phones running an SDK before froyo
 		           	d = new File(
 					            Environment.getExternalStorageDirectory(), Singleton.BACKUP_PATH); //my datapath location			       
 					        }    
 		       		/////////////still if the path doesnt exist use the default path set up at first run
 		       		if (!d.exists()){
 		       			String defaultPath = settings.getString("default-path", "null");		       			
 		       			d = new File (defaultPath);
 		       		}
 	           return d;       
 			 
 		 }
 	    private  void bruteForceUpgrade(String path){
 	    		this.close();
 	    	overwriteNativeDb(path);
 	    }
 		
 		private   void overwriteNativeDb(String path){
 			File dbDir = getAppDir();
 			File src = new File(dbDir, Singleton.DESTINATION_FILE);
 			File dest = new File (path );
 			try {
 				copy (src, dest);
 				copySuccess=true;
 		 		} catch (IOException e) {
 		 			Log.e("dictdatabase", " uh oh...couldn't copy. doesn't exist?");
 					e.printStackTrace();}
 			
 			}
 		  // Copies src file to dst file.
 		// If the dst file does not exist, it is created
 		private   void copy(File src, File dst) throws IOException {
 		    InputStream in = new FileInputStream(src);
 		    OutputStream out = new FileOutputStream(dst);
 
 		    // Transfer bytes from in to out
 		    byte[] buf = new byte[1024];
 		    int len;
 		    while ((len = in.read(buf)) > 0) {
 		        out.write(buf, 0, len);
 		    }
 		    in.close();
 		    out.close();
 		}
 
     }//end of helper class
 
 
 
 
 }  
 
 		

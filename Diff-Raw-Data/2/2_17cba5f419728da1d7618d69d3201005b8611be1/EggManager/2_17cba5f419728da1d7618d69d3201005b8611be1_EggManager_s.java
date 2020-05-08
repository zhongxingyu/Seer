 package org.fourdnest.androidclient;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import android.text.format.DateFormat;
 import android.util.Log;
 
 /**
  * A manager class for Egg databases.
  * There are two known instances of this manager, one for the stream cache
  * (the cached contents of the streams fetched from Nests) and one for draft eggs
  * (explicitly saved drafts, and eggs in the send queue)
  * To access these instances, use the following methods in FourDNestApplication:
  * getStreamEggManager(), getDraftEggManager() 
  */
 public class EggManager {
     
     private static final String TAG = EggManager.class.getSimpleName();
     
     private static final int DB_VERSION = 7;
     
     // Table columns
     private static final String TABLE = "egg";
     private static final String C_ID = BaseColumns._ID;
     private static final String C_NESTID = "nest_id";
     private static final String C_AUTHOR = "author";
     private static final String C_LOCALFILEURI = "local_file_uri";
     private static final String C_REMOTEFILEURI = "remote_file_uri";
     private static final String C_CAPTION = "caption";
     private static final String C_LASTUPLOAD = "last_upload";
     private static final String C_TAGS = "tags";
     private static final String C_DATE = "date";
     private static final String C_REMOTETHUMBNAILURI = "remote_thumbnail_uri";
     private static final String C_LONGITUDE = "longitude";
     private static final String C_LATITUDE = "latitude";
     
     private static final String TAG_LIST_SEPARATOR = ",";
     
     private static final String[] ALL_COLUMNS = new String[] { C_ID, C_NESTID,
             C_AUTHOR, C_LOCALFILEURI, C_REMOTEFILEURI, C_CAPTION, C_LASTUPLOAD,
             C_TAGS, C_DATE, C_REMOTETHUMBNAILURI, C_LONGITUDE, C_LATITUDE };
         
     private final EggDatabase eggDb;
     private String dbName;
     
     /**
      * Creates new NestManager with specified context and
      * role. Role is used to differentiate databases and thus
      * allow running multiple parallel EggManagers 
      * @param context
      */
     public EggManager(Context context, String uniqueRole) {        
         this.dbName = "org.4dnest.androidclient.eggs." + uniqueRole + ".db";   
         this.eggDb = new EggDatabase(context, this.dbName);
         
         Log.d(TAG, "EggManager created");
     }
     
     /**
      * 
      * @return ArrayList<Nest> List of saved nests
      */
     public synchronized List<Egg> listEggs() {
     	List<Egg> eggs = new ArrayList<Egg>();
         	
     	SQLiteDatabase db = null;
     	Cursor result = null;
     	try {
 	        db = this.eggDb.getReadableDatabase();
 	        result = db.query(TABLE,
 	                ALL_COLUMNS, // Columns
 	                null, // No WHERE
 	                null, // No arguments in selection
 	                null, // No GROUP BY
 	                null, // No HAVING
 	                C_DATE, // Order by date
 	                "100"); // Limit 100
 	        
 	        if(result.getCount() > 0) {
 	            result.moveToFirst();
 	            
 	            while(!result.isAfterLast()) {
 	                Egg egg = this.extractEggFromCursor(result);
 	                
 	                eggs.add(egg);
 	                
 	                result.moveToNext();
 	            } 
 	        }
     	} catch(Exception e) {
     		Log.e(TAG, "ListEggs: " + e.getMessage() + ": " + e.getStackTrace().toString());
     	} finally {
     		if(result != null && !result.isClosed()) {
     			result.close();
     		}
     		if(db != null && db.isOpen()) {
     			db.close();
     		}
     	}
         
         return eggs;
     }
     
     /**
      * 
      * @param id of nest
      * @return Nest with specified id or null
      */
     public synchronized Egg getEgg(int id) {
     	
     	Egg egg = null;
     	SQLiteDatabase db = null;
     	Cursor result = null;
 
     	try {
 	        db = this.eggDb.getReadableDatabase();
 	        result = db.query(TABLE,
 	                ALL_COLUMNS, // Columns
 	                C_ID + "==" + id, // Where
 	                null, // No arguments in selection
 	                null, // No GROUP BY
 	                null, // No HAVING
 	                null, //No ORDER BY
 	                "1"); // Limit 1
 	        
 	        if(result.getCount() > 0) {
 	            result.moveToFirst();
 	            egg = this.extractEggFromCursor(result);            
 	        } else {
 	            Log.d(TAG, "Egg with id " + id + " not found");
 	        }
     	} catch(Exception e) {
     		Log.e(TAG, "GetEgg(" + id + "): " + e.getMessage() + ": " + e.getStackTrace().toString());
     	} finally {
     		if(result != null && !result.isClosed()) {
     			result.close();
     		}
     		if(db != null && db.isOpen()) {
     			db.close();
     		}
     	}
         
         return egg;
         
     }
     
     /**
      * Deletes Egg with given id from the database
      * @param id of egg to delete
      * @return 1 if deletion was successful, 0 if not
      */
     public synchronized int deleteEgg(int id) {
     	SQLiteDatabase db = null;
         int result = 0;
         
         try {
         	db = this.eggDb.getWritableDatabase();
         
         	result = db.delete(TABLE, C_ID + "==" + id, null);
         } catch(Exception e) {
         	Log.e(TAG, "DeleteEgg(" + id + "): " + e.getMessage() + ": " + e.getStackTrace().toString());
         } finally {
         	if(db != null && db.isOpen()) {
     			db.close();
     		}
         }
         
         return result;
     }
     
     /**
      * Deletes all saved Eggs in the database
      * @return number of deleted Eggs
      */
     public synchronized int deleteAllEggs() {
         SQLiteDatabase db = null;
         int result = 0;
         
         try {
         	db = this.eggDb.getWritableDatabase();
         	result = db.delete(TABLE, null, null);
         } catch(Exception e) {
         	Log.e(TAG, "DeleteAllEggs: " + e.getMessage() + ": " + e.getStackTrace().toString());
         } finally {
         	if(db != null && db.isOpen()) {
     			db.close();
     		}
         }
         
         return result;
     }
     
     /**
      * Saves Egg to database, updating existing Egg with same id
      * and creating new one if necessary 
      * @param egg object to save
      * @return Egg with updated info (id)
      */
     public synchronized Egg saveEgg(Egg egg) {
     	SQLiteDatabase db = null;    	
     	try {
 	        db = this.eggDb.getWritableDatabase();
 	        
 	        // Create ContentValues object for Nest
 	        ContentValues values = new ContentValues();
 	        //values.put(C_ID, egg.getId());
 	        values.put(C_NESTID, egg.getNestId());
 	        
 	        values.put(C_AUTHOR, egg.getAuthor());
 	        
 	        values.put(C_LOCALFILEURI, egg.getLocalFileURI() != null ? egg.getLocalFileURI().toString() : null);
 	        values.put(C_REMOTEFILEURI, egg.getRemoteFileURI() != null ? egg.getRemoteFileURI().toString() : null);
 	        
 	        values.put(C_CAPTION, egg.getCaption());
 	        values.put(C_LASTUPLOAD, egg.getLastUpload());
 	        
 	        // Serialize tags to a separated string
 	        String tagString = "";
 	        if(egg.getTags() != null) {
 		        for(Tag t : egg.getTags()) {
 		            tagString += t.getName();
 		            tagString += TAG_LIST_SEPARATOR;
 		        }
 	        }
 	        values.put(C_TAGS, tagString);
 	        
 	        if (egg.getCreationDate() != null) {
 	            values.put(C_DATE, DateFormat.format("yyyy-MM-dd hh:mm:ss", egg.getCreationDate()).toString());
 	        }else {
 	            values.put(C_DATE, "");
 	        }
             values.put(C_REMOTETHUMBNAILURI,
                     egg.getRemoteThumbnailUri() != null ? egg
                             .getRemoteThumbnailUri().toString() : null);
             values.put(C_LONGITUDE, egg.getLongitude());
             values.put(C_LATITUDE, egg.getLatitude());
 	        
 	        // API level 8 would have insertWithOnConflict, have to work around it
 	        // and check for conflict and then either insert or update
 	
 	        // Check if nest with id exists
 	        boolean insertNew = true;
 	        if(egg.getId() != null) {
 	            Cursor result = db.query(TABLE,
 	                    new String[] {C_ID},
 	                    C_ID + "==" + egg.getId(),
 	                    null, // No selection args
 	                    null, // No GROUP BY
 	                    null, // No HAVING
 	                    null, // No ORDER BY
 	                    "1"); // LIMIT 1
 	            if(result.getCount() > 0) {
 	                insertNew = false;
 	            }
 	            
 	            if(!result.isClosed()) {
 	            	result.close();
 	            }
 	        }
 	        
 	        
 	        long rowid;
 	        if(!insertNew) {
 	            // Update existing
 	            rowid = db.replace(TABLE, null, values);
 	            
 	            if(rowid < 0) {
 	                throw new SQLiteException("Error replacing existing Egg with id + "
 	                        + egg.getId() + " in database");
 	            }
 	            
 	            Log.d(TAG, "Updated Egg in db");
 	            
 	        } else {
 	            // Insert new row           
 	            rowid = db.insert(TABLE, null, values);
 	            if(rowid < 0) {
 	                throw new SQLiteException("Error inserting new Egg to database");
 	            }
 	            
 	            Log.d(TAG, "Inserted new Egg to db");
 	        }
 	        
 	        egg.setId((int)rowid);
     	} catch(Exception e) {
     		Log.e(TAG, "InsertEgg: " + e.getMessage() + ": " + e.getStackTrace().toString());
     	} finally {
     		if(db != null && db.isOpen()) {
     			db.close();
     		}
     	}
         
         return egg;
     }
     
     /**
      * Extracts Egg from given Cursor object. Cursor must contain columns specified in ALL_COLUMNS
      * @param cursor to be read. Will not be manipulated, only read.
      * @return Egg from cursor
      */
     private synchronized Egg extractEggFromCursor(Cursor cursor) {                   
         int id = cursor.getInt(0);
         int nestId = cursor.getInt(1);
         String author = cursor.getString(2);
         
         Uri localURI = null;
         if (cursor.getString(3) != null) {
             localURI = Uri.parse(cursor.getString(3));
         }
         
         Uri remoteURI = null;
         if (cursor.getString(4) != null) {    
             remoteURI =  Uri.parse(cursor.getString(4));
         }
         
         String caption = cursor.getString(5);
         long lastUpload = cursor.getLong(6);
         
         // Extract tags from comma separated list
         String tags = cursor.getString(7);
         String[] tagArr = tags.split(TAG_LIST_SEPARATOR);
         List<Tag> tagList = new ArrayList<Tag>();
         
         for(int i = 0; i < tagArr.length; i++) {
             String tagName = tagArr[i];
             if(tagName != "") {
                 Tag t = new Tag(tagName);
                 tagList.add(t);
             }
         }
         
         String dateStr = cursor.getString(8);
         java.text.DateFormat formatter = new SimpleDateFormat(("yyyy-MM-dd hh:mm:ss"));
         Date date;
         try {
             date = (Date) formatter.parse(dateStr);
         } catch (ParseException e) {
            Log.e(TAG, "Failed to parse date");
            date = null;
         }
         Uri remoteThumbnail = null;
         if (cursor.getString(9) != null) {
         	remoteThumbnail = Uri.parse(cursor.getString(9));
         }
         double longitude = 0;
         if (!cursor.isNull(10)) {
             longitude = cursor.getDouble(10);
         }
         double latitude = 0;
         if (!cursor.isNull(11)) {
            longitude = cursor.getDouble(11);
         }
         Egg egg = new Egg(id, nestId, author, localURI, remoteURI, remoteThumbnail, caption, tagList, lastUpload, date);
         egg.setLongitude(longitude);
         egg.setLatitude(latitude);
         
         return egg;
 
     }
 
     
     
     /**
      *  Actual database handler inside NestManager
      */
     static class EggDatabase extends SQLiteOpenHelper {
         //private Context context;
         
         public EggDatabase(Context context, String dbName) {
             super(context, dbName, null, DB_VERSION);           
             //this.context = context;           
             Log.d(TAG, "EggDatabase created");
         }
 
         // Called when DB is created for the first time (does not exist)
         @Override
         public void onCreate(SQLiteDatabase db) {
             
             // Prepare SQL table creation query 
             String tableCreateQuery = String.format(
                         "CREATE TABLE %s(" +
                         "%s INTEGER PRIMARY KEY," +
                         "%s int DEFAULT NULL, " +
                         "%s text DEFAULT NULL," +
                         "%s text DEFAULT NULL," +
                         "%s text DEFAULT NULL," +
                         "%s text DEFAULT NULL," +
                         "%s long DEFAULT NULL," +
                         "%s text DEFAULT NULL," +
                         "%s datetime DEFAULT NULL," +
                         "%s text DEFAULT NULL," +
                         "%s double DEFAULT NULL," +
                         "%s double DEFAULT NULL)",
                         TABLE,
                         C_ID,
                         C_NESTID,
                         C_AUTHOR,
                         C_LOCALFILEURI,
                         C_REMOTEFILEURI,
                         C_CAPTION,
                         C_LASTUPLOAD,
                         C_TAGS,
                         C_DATE,
                         C_REMOTETHUMBNAILURI,
                         C_LONGITUDE,
                         C_LATITUDE
             );
             
             db.execSQL(tableCreateQuery);
             
             Log.d(TAG, "onCreated SQL: " + tableCreateQuery);
         }
 
 		// Called when DB version number has changed
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			// TODO Add proper alter scripts for production use when version changes
 			
 			// For now, just drop and recreate
 			String tableDropQuery = String.format("DROP TABLE IF EXISTS %s", TABLE);
 			db.execSQL(tableDropQuery);
 			Log.d(TAG, "onUpgrade: Dropped existing table");
 			
 			onCreate(db);
 			db.close();
 		}
 		
 	}
 }

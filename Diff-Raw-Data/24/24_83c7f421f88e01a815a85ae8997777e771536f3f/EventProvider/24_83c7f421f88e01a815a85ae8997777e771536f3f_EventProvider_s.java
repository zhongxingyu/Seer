 package no.uka.findmyapp.android.rest.providers;
 
 import java.util.HashMap;
 
 import no.uka.findmyapp.android.rest.contracts.UkaEvents;
 import no.uka.findmyapp.android.rest.contracts.UkaEvents.UkaEventContract;
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.util.Log;
 
 public class EventProvider extends ContentProvider {
 	   /**
 	    * Debug tag
 	    */
 		private static final String debug = "EventProvider";
 	
 	   /**
 	    * The local event database name
 	    */
 	   private static final String DATABASE_NAME = "events.db";
 
 	   /**
 	    * The database version
 	    */
	   private static final int DATABASE_VERSION = 3;
 
 	   /**
 	    * A projection map used to select columns from the database
 	    */
 	   private static HashMap<String, String> eventProjectionMap;
 
 	   /*
 	    * Constants used by the Uri matcher to choose an action based on the pattern
 	    * of the incoming URI
 	    */
 	   private static final int EVENTS = 1;
 	   private static final int EVENT_ID = 2;
 
 	   /**
 	    * A UriMatcher instance
 	    */
 	   private static final UriMatcher uriMatcher;
 
 	   /**
 	    * A database helper is required to create, 
 	    * update and drop the events table.
 	    */
 	   private EventsDatabaseHelper dbHelper;
 
 	   /**
 	    * Instantiates the needed statics. 
 	    */
 	   static {
 	       /*
 	        * Initializes the URI matcher
 	        */
 	       uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
 
 	       // Setup the right patterns to the events "dir"
 	       uriMatcher.addURI(UkaEvents.AUTHORITY, "events", EVENTS);
 	      
 	       /*
 	        * Setup pattern to a spesific event by using the spesific 
 	        * of the event
 	        */
 	       uriMatcher.addURI(UkaEvents.AUTHORITY, "events/#", EVENT_ID);
 
 	       /*
 	        * Initializes a projection map that returns all columns
 	        */
 	       eventProjectionMap = new HashMap<String, String>();
 	       eventProjectionMap.put(UkaEventContract.ID, UkaEventContract.ID);
 	       eventProjectionMap.put(UkaEventContract.EVENT_ID, UkaEventContract.EVENT_ID);
 	       eventProjectionMap.put(UkaEventContract.BILLING_ID, UkaEventContract.BILLING_ID);	       
 	       eventProjectionMap.put(UkaEventContract.ENTRANCE_ID, UkaEventContract.ENTRANCE_ID);	       
 	       eventProjectionMap.put(UkaEventContract.TITLE, UkaEventContract.TITLE);
 	       eventProjectionMap.put(UkaEventContract.LEAD, UkaEventContract.LEAD);	       
 	       eventProjectionMap.put(UkaEventContract.TEXT, UkaEventContract.TEXT);
 	       eventProjectionMap.put(UkaEventContract.PLACE, UkaEventContract.PLACE);	       
 	       eventProjectionMap.put(UkaEventContract.EVENT_TYPE, UkaEventContract.EVENT_TYPE);
 	       eventProjectionMap.put(UkaEventContract.IMAGE, UkaEventContract.IMAGE);	       
 	       eventProjectionMap.put(UkaEventContract.THUMBNAIL, UkaEventContract.THUMBNAIL);
 	       eventProjectionMap.put(UkaEventContract.HIDDEN_FROM_LISTING, UkaEventContract.HIDDEN_FROM_LISTING);	       
 	       eventProjectionMap.put(UkaEventContract.AGE_LIMIT, UkaEventContract.AGE_LIMIT);
 	       eventProjectionMap.put(UkaEventContract.DETAIL_PHOTO_ID, UkaEventContract.DETAIL_PHOTO_ID);
 	       eventProjectionMap.put(UkaEventContract.SHOWING_TIME, UkaEventContract.SHOWING_TIME);
 	       eventProjectionMap.put(UkaEventContract.PUBLISH_TIME, UkaEventContract.PUBLISH_TIME);
 	       eventProjectionMap.put(UkaEventContract.NETSALE_FROM, UkaEventContract.NETSALE_FROM);
 	       eventProjectionMap.put(UkaEventContract.NETSALE_TO, UkaEventContract.NETSALE_TO);
 	       eventProjectionMap.put(UkaEventContract.FREE, UkaEventContract.FREE);
 	       eventProjectionMap.put(UkaEventContract.CANCELED, UkaEventContract.CANCELED);
 	   }
 
 	   /**
 	    * A custom database helper for the event database.
 	    * The helper helps creating, updating and 
 	    * deleting tables
 	    */
 	  private static class EventsDatabaseHelper extends SQLiteOpenHelper {
 		  private static final String debug = "EventesDatabaseHelper";
 
 	      public EventsDatabaseHelper(Context context) {
 	          super(context, DATABASE_NAME, null, DATABASE_VERSION);
 			  Log.v(debug, "Inside constructor");
 	      }
 
 	      /**
 	       * Creates the database table when the 
 	       * object is created.
 	       */
 	      @Override
 	      public void onCreate(SQLiteDatabase db) {
 			  Log.v(debug, "On create");
 	          db.execSQL(UkaEventContract.CREATE_TABLE_QUERY); 
 	      }
 
 	      //TODO Implement implement UKA-program caching
 	      /**
 	       * The database drops all the data while upgrading. 
 	       * The method is not implemented with the possibility
 	       * to store data between different program sessions.
 	       */
 	      @Override
 	      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			  Log.v(debug, "Inside onUpgrade");
 
 	          Log.w("EventProvider", "Upgrading database, version:" + oldVersion + " to "
 	                  + newVersion + ", the data is dropped");
 
 	          // Drops the table
 	          db.execSQL(UkaEventContract.DROP_TABLE_QUERY);
 
 	          // Recreates the database
 	          onCreate(db);
 	      }
 	  }
 
 	  @Override
 	  public boolean onCreate() {
 		  Log.v(debug, "Inside onCreate");
 	      dbHelper = new EventsDatabaseHelper(getContext());
 
 	      // Returns true by default, throws exceptions if something fails
 	      return true;
 	  }
 
 	  @Override
 	  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
 		  Log.v(debug, "Inside query");
 	      SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
 	      qb.setTables(UkaEventContract.TABLE_NAME);
 
 	      switch (uriMatcher.match(uri)) {
 	          case EVENTS:
 	              qb.setProjectionMap(eventProjectionMap);
 	              break;
 	          case EVENT_ID:
 	              qb.setProjectionMap(eventProjectionMap);
 	              qb.appendWhere(UkaEventContract.ID + "=" + uri.getPathSegments().get(UkaEventContract.EVENTS_ID_PATH_POSITION));
 	              break;
 	          default:
 	              throw new IllegalArgumentException("Unknown URI " + uri);
 	      }
 
 	      SQLiteDatabase db = dbHelper.getReadableDatabase();
 
 	      Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
 
 	      // Tells the Cursor what URI to watch, so it knows when its source data changes
 	      cursor.setNotificationUri(getContext().getContentResolver(), uri);
 	      
 	      return cursor;
 	  }
 
 	  @Override
 	  public String getType(Uri uri) {
 		  Log.v(debug, "Inside getType");
 	      switch (uriMatcher.match(uri)) {
 	          case EVENTS:
 	              return UkaEventContract.CONTENT_TYPE_EVENT;
 	          case EVENT_ID:
 	              return UkaEventContract.CONTENT_ITEM_EVENT;
 	          default:
 	              throw new IllegalArgumentException("Unknown URI " + uri);
 	      }
 	   }
 
 	   @Override
 	   public Uri insert(Uri uri, ContentValues initialValues) {
 		Log.v(debug, "Inside insert");
 	       if (uriMatcher.match(uri) != EVENTS) {
 	           throw new IllegalArgumentException("Unknown URI " + uri);
 	       }
 	       
 	       ContentValues values;
 	       if (initialValues != null) {
 	           values = new ContentValues(initialValues);
 	       } 
 	       else {
 	           values = new ContentValues();
 	       }
 
 			SQLiteDatabase db = dbHelper.getWritableDatabase(); 
 	        Log.v("INFO", "DB: " +db.toString());
 			
 			
 			/* The second insert() parameter is a nullColumnHack, 
 			 * a somewhat crappy solution which is used to avoid 
 			 * queries like "INSERT INTO tablename;" isn't 
 			 * declared illegal. Instead it automatically creates
 			 * a statement like "INSERT INTO temperature_table
 			 * (location_id) VALUES (NULL)" in this case.
 			 */
 			long rowId = db.insert(UkaEventContract.TABLE_NAME, UkaEventContract.SLUG, values);
 			if(rowId > 0) {
 				Uri eventUri = ContentUris.withAppendedId(UkaEventContract.EVENT_CONTENT_URI, rowId);
 				getContext().getContentResolver().notifyChange(uri, null);
 				return eventUri;
 			}
 			
 			throw new IllegalArgumentException("InsertUnknown URI: " + uri);
 	   }
 
 	   @Override
 	   public int delete(Uri uri, String where, String[] whereArgs) {
 			  Log.v(debug, "Inside delete");
 	       SQLiteDatabase db = dbHelper.getWritableDatabase();
 	       String finalWhere;
 
 	       int count;
 
 	       switch (uriMatcher.match(uri)) {
 	           case EVENTS:
 	               count = db.delete(UkaEventContract.TABLE_NAME, where, whereArgs);
 	               break;
 	           case EVENT_ID:
 	               finalWhere = UkaEventContract.ID + " = " +  uri.getPathSegments().get(UkaEventContract.EVENTS_ID_PATH_POSITION);
 
 	               if (where != null) {
 	                   finalWhere = finalWhere + " AND " + where;
 	               }
 	               
 	               count = db.delete( UkaEventContract.TABLE_NAME, finalWhere, whereArgs);
 	               break;
 	           default:
 	               throw new IllegalArgumentException("Unknown URI " + uri);
 	       }
 
 	       getContext().getContentResolver().notifyChange(uri, null);
 
 	       return count;
 	   }
 
 	   @Override
 	   public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
 			  Log.v(debug, "inside update");
 	       SQLiteDatabase db = dbHelper.getWritableDatabase();
 	       int count;
 	       String finalWhere;
 
 	       switch (uriMatcher.match(uri)) {
 	           case EVENTS:
 	               count = db.update(UkaEventContract.TABLE_NAME, values, where, whereArgs);
 	               break;
 	           case EVENT_ID:
 	               String eventId = uri.getPathSegments().get(UkaEventContract.EVENTS_ID_PATH_POSITION);
 	               finalWhere = UkaEventContract.ID + " = " + eventId;
 
 	               if (where !=null) {
 	                   finalWhere = finalWhere + " AND " + where;
 	               }
 
 	               count = db.update(UkaEventContract.TABLE_NAME, values, finalWhere, whereArgs);
 	               break;
 	           default:
 	               throw new IllegalArgumentException("Unknown URI " + uri);
 	       }
 	       
 	       getContext().getContentResolver().notifyChange(uri, null);
 
 	       return count;
 	   }
 }

 package com.transitwidget.provider;
 
 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import android.util.Log;
 import com.transitwidget.feed.model.Agency;
 import com.transitwidget.feed.model.Direction;
 import com.transitwidget.feed.model.Favorite;
 import com.transitwidget.feed.model.Route;
 import com.transitwidget.feed.model.Stop;
 
 public class TransitServiceDataProvider extends ContentProvider {
     private static final String TAG = TransitServiceDataProvider.class.getName();
 	
     private static final String DATABASE_NAME = "transitServiceData.db";
 	private static final int DATABASE_VERSION = 4;
 
     public static final String AUTHORITY = "transitwidget.provider.TransitServiceDataProvider";
     
     // URI matching constants
     private static final int AGENCIES = 1;
     private static final int AGENCY_ID = 2;
 
     private static final int ROUTES = 3;
     private static final int ROUTE_ID = 4;
 
     private static final int DIRECTIONS = 5;
     private static final int DIRECTION_ID = 6;
 
     private static final int STOPS = 7;
     private static final int STOP_ID = 8;
 
     private static final int FAVORITES = 9;
     private static final int FAVORITE_ID = 10;
     
     private static final UriMatcher sUriMatcher;
 
     private DatabaseHelper mHelper;
     private SQLiteDatabase database;
 
     static {
         sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
         sUriMatcher.addURI(AUTHORITY, Agency.TABLE_NAME, AGENCIES);
         sUriMatcher.addURI(AUTHORITY, Agency.TABLE_NAME + "/#", AGENCY_ID);
 
         sUriMatcher.addURI(AUTHORITY, Route.TABLE_NAME, ROUTES);
         sUriMatcher.addURI(AUTHORITY, Route.TABLE_NAME + "/#", ROUTE_ID);
 
         sUriMatcher.addURI(AUTHORITY, Direction.TABLE_NAME, DIRECTIONS);
         sUriMatcher.addURI(AUTHORITY, Direction.TABLE_NAME + "/#", DIRECTION_ID);
 
         sUriMatcher.addURI(AUTHORITY, Stop.TABLE_NAME, STOPS);
         sUriMatcher.addURI(AUTHORITY, Stop.TABLE_NAME + "/#", STOP_ID);
 
         sUriMatcher.addURI(AUTHORITY, Favorite.TABLE_NAME, FAVORITES);
         sUriMatcher.addURI(AUTHORITY, Favorite.TABLE_NAME + "/#", FAVORITE_ID);
     }
     
     @Override
     public boolean onCreate() {
         mHelper = new DatabaseHelper(getContext());
         database = mHelper.getWritableDatabase();
         
         return true;
     }
 
     @Override
     public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
         SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
         
         int match = sUriMatcher.match(uri);
         switch (match) {
 	        case AGENCIES:
 	            qBuilder.setTables(Agency.TABLE_NAME);
 	            break;
 	            
 	        case AGENCY_ID:
 	            qBuilder.setTables(Agency.TABLE_NAME);
 	            qBuilder.appendWhere(Agency._ID + " = " + uri.getLastPathSegment());
 	            break;
 	            
 	        case ROUTES:
 	            qBuilder.setTables(Route.TABLE_NAME);
 	            break;
 	            
 	        case ROUTE_ID:
 	            qBuilder.setTables(Route.TABLE_NAME);
 	            qBuilder.appendWhere(Route._ID + " = " + uri.getLastPathSegment());
 	            break;
 	            
 	        case DIRECTIONS:
 	            qBuilder.setTables(Direction.TABLE_NAME);
 	            break;
 	            
 	        case DIRECTION_ID:
 	            qBuilder.setTables(Direction.TABLE_NAME);
 	            qBuilder.appendWhere(Direction._ID + " = " + uri.getLastPathSegment());
 	            break;
 	            
 	        case STOPS:
 	            qBuilder.setTables(Stop.TABLE_NAME);
 	            break;
 	            
 	        case STOP_ID:
 	            qBuilder.setTables(Stop.TABLE_NAME);
 	            qBuilder.appendWhere(Stop._ID + " = " + uri.getLastPathSegment());
 	            break;
 	            
 	        case FAVORITES:
 	            qBuilder.setTables(Favorite.TABLE_NAME);
 	            break;
 	            
 	        case FAVORITE_ID:
 	            qBuilder.setTables(Favorite.TABLE_NAME);
 	            qBuilder.appendWhere(Favorite._ID + " = " + uri.getLastPathSegment());
 	            break;
 	            
             default:
                 throw new IllegalArgumentException("URI " + uri + " not recognized by widget configuration content provider.");
         }
 
         // Make the query.
         Cursor c = qBuilder.query(database,
                 projection,
                 selection,
                 selectionArgs,
                 null,
                 null,
                 sortOrder);
         c.setNotificationUri(getContext().getContentResolver(), uri);
         return c;
     }
 
     @Override
     public String getType(Uri uri) {
         int match = sUriMatcher.match(uri);
         switch (match)
         {
 	        case AGENCIES:
 	            return Agency.CONTENT_TYPE;
 	        case AGENCY_ID:
 	            return Agency.CONTENT_TYPE_ITEM;
 	            
 	        case ROUTES:
 	            return Route.CONTENT_TYPE;
 	        case ROUTE_ID:
 	            return Route.CONTENT_TYPE_ITEM;
 	            
 	        case DIRECTIONS:
 	            return Direction.CONTENT_TYPE;
 	        case DIRECTION_ID:
 	            return Direction.CONTENT_TYPE_ITEM;
 	            
 	        case STOPS:
 	            return Stop.CONTENT_TYPE;
 	        case STOP_ID:
 	            return Stop.CONTENT_TYPE_ITEM;
 
 	        case FAVORITES:
 	            return Favorite.CONTENT_TYPE;
 	        case FAVORITE_ID:
 	            return Favorite.CONTENT_TYPE_ITEM;
 	            
             default:
                 return null;
         }
     }
 
     @Override
     public Uri insert(Uri uri, ContentValues values) {
         String table = null;
         Uri baseUri = null;
         int match = sUriMatcher.match(uri);
         switch (match)
         {
 	        case AGENCIES:
 	            table   = Agency.TABLE_NAME;
 	            baseUri = Agency.CONTENT_URI;
                 break;
                 
 	        case ROUTES:
 	            table   = Route.TABLE_NAME;
 	            baseUri = Route.CONTENT_URI;
                 break;
                 
 	        case DIRECTIONS:
 	            table   = Direction.TABLE_NAME;
 	            baseUri = Direction.CONTENT_URI;
                 break;
                 
 	        case STOPS:
 	            table   = Stop.TABLE_NAME;
 	            baseUri = Stop.CONTENT_URI;
                 break;
 
 	        case FAVORITES:
 	            table   = Favorite.TABLE_NAME;
 	            baseUri = Favorite.CONTENT_URI;
                 break;
                 
             default:
                 throw new IllegalArgumentException("URI " + uri + " cannot be inserted by widget configuration content provider.");
         }
         
         long id = database.insert(table, null, values);
 
    	Log.d(TAG, "DB: " + id + " -> " + values);
         getContext().getContentResolver().notifyChange(baseUri, null);
     	
         return Uri.withAppendedPath(baseUri, String.valueOf(id));
     }
 
     @Override
     public int delete(Uri uri, String selection, String[] selectionArgs) {
         String table = null;
         int match = sUriMatcher.match(uri);
         switch (match)
         {
 	        case AGENCIES:
 	            table = Agency.TABLE_NAME;
 	            break;
 	            
 	        case ROUTES:
 	            table = Route.TABLE_NAME;
 	            break;
 	            
 	        case DIRECTIONS:
 	            table = Direction.TABLE_NAME;
 	            break;
 	            
 	        case STOPS:
 	            table = Stop.TABLE_NAME;
 	            break;
 
 	        case FAVORITES:
 	            table = Favorite.TABLE_NAME;
 	            break;
 	            
             default:
                 throw new IllegalArgumentException("URI " + uri + " cannot be deleted by widget configuration content provider.");
         }
         
         int count = database.delete(table, selection, selectionArgs);
         
         getContext().getContentResolver().notifyChange(uri, null);
         return count;
     }
 
     @Override
     public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
         String table = null;
         int match = sUriMatcher.match(uri);
         switch (match)
         {
             case AGENCY_ID:
                 selection = BaseColumns._ID + " = " + uri.getLastPathSegment();
 	        case AGENCIES:
 	            table = Agency.TABLE_NAME;
 	            break;
 	            
             case ROUTE_ID:
                 selection = BaseColumns._ID + " = " + uri.getLastPathSegment();
 	        case ROUTES:
 	            table = Route.TABLE_NAME;
 	            break;
 	            
             case DIRECTION_ID:
                 selection = BaseColumns._ID + " = " + uri.getLastPathSegment();
 	        case DIRECTIONS:
 	            table = Direction.TABLE_NAME;
 	            break;
 	            
             case STOP_ID:
                 selection = BaseColumns._ID + " = " + uri.getLastPathSegment();
 	        case STOPS:
 	            table = Stop.TABLE_NAME;
 	            break;
                 
 	            
             case FAVORITE_ID:
                 selection = BaseColumns._ID + " = " + uri.getLastPathSegment();
 	        case FAVORITES:
 	            table = Favorite.TABLE_NAME;
 	            break;
                 
             default:
                 throw new IllegalArgumentException("URI " + uri + " cannot be updated by widget configuration content provider.");
         }
         
         int count = database.update(table, values, selection, selectionArgs);
         
         getContext().getContentResolver().notifyChange(uri, null);
         return count;
     }
     
 
 	 class DatabaseHelper extends SQLiteOpenHelper {
 	
 		DatabaseHelper(Context context) {
 	        // calls the super constructor, requesting the default cursor factory.
 	        super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	    }
 	
 	    /**
 	     * Creates the underlying database.
 	     */
 	    @Override
 	    public void onCreate(SQLiteDatabase db) {
 	    	Agency.onCreate(db);
 	    	Route.onCreate(db);
 	    	Direction.onCreate(db);
 	    	Stop.onCreate(db);
 	    	Favorite.onCreate(db);
 	    }
 	
 	    /**
 	     * Upgrade the database tables.
 	     */
 	    @Override
 	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			// Logs that the database is being upgraded
 			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
 			
 			Agency.onUpgrade(db, oldVersion, newVersion);
 			Route.onUpgrade(db, oldVersion, newVersion);
 			Direction.onUpgrade(db, oldVersion, newVersion);
 			Stop.onUpgrade(db, oldVersion, newVersion);
 			Favorite.onUpgrade(db, oldVersion, newVersion);
 	    }
 	}
 }

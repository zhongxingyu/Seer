 package fr.itinerennes.provider;
 
 import java.util.HashMap;
 
 import org.slf4j.Logger;
 import org.slf4j.impl.AndroidLoggerFactory;
 
 import android.app.SearchManager;
 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.provider.BaseColumns;
 
 import fr.itinerennes.ItineRennesApplication;
 import fr.itinerennes.database.Columns;
 import fr.itinerennes.database.DatabaseHelper;
 
 /**
  * Content provider to handle search markers queries and suggestion markers queries.
  * 
  * @author Olivier Boudet
  */
 public class SearchMarkersProvider extends ContentProvider {
 
     /** The event logger. */
     private static final Logger LOGGER = AndroidLoggerFactory
             .getLogger(SearchMarkersProvider.class);
 
     /** Authority for this content provider. */
     public static final String AUTHORITY = "fr.itinerennes.provider.searchMarkersProvider";
 
     /** URI to access this content provider. */
     public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/markers");
 
     /** URI matcher to dispatch accesses of this content provider. */
     private static final UriMatcher URI_MATCHER;
 
     /** Used when search of markers is queried. */
     private static final int GET_MARKERS = 1;
 
     /** Used when search of markers is queried. */
     private static final int GET_MARKER = 2;
 
     /** Used when markers suggestions are queried. */
     private static final int GET_SUGGEST = 3;
 
     /** Projection map for suggestions queries. */
     private static final HashMap<String, String> PROJECTION_MAP = new HashMap<String, String>();
 
     static {
 
         URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
         URI_MATCHER.addURI(AUTHORITY, "markers", GET_MARKERS);
         URI_MATCHER.addURI(AUTHORITY, "markers/#", GET_MARKER);
         URI_MATCHER.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, GET_SUGGEST);
 
         PROJECTION_MAP.put(BaseColumns._ID, BaseColumns._ID);
         PROJECTION_MAP.put(Columns.MarkersColumns.ID, Columns.MarkersColumns.ID);
         PROJECTION_MAP.put(Columns.MarkersColumns.TYPE, Columns.MarkersColumns.TYPE);
         PROJECTION_MAP.put(Columns.MarkersColumns.LABEL, Columns.MarkersColumns.LABEL);
         PROJECTION_MAP.put(Columns.MarkersColumns.LONGITUDE, Columns.MarkersColumns.LONGITUDE);
         PROJECTION_MAP.put(Columns.MarkersColumns.LATITUDE, Columns.MarkersColumns.LATITUDE);
        PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_1, Columns.MarkersColumns.TYPE
                 + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
        PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_2, Columns.MarkersColumns.LABEL
                + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_2);
         PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, BaseColumns._ID + " AS "
                 + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
 
     }
 
     /** Database helper. */
     private DatabaseHelper dbHelper;
 
     /**
      * {@inheritDoc}
      * 
      * @see android.content.ContentProvider#onCreate()
      */
     @Override
     public final boolean onCreate() {
 
         final boolean initialized;
         if (getContext() instanceof ItineRennesApplication) {
             dbHelper = ((ItineRennesApplication) getContext()).getDatabaseHelper();
             initialized = true;
         } else {
             LOGGER.error("Bad application context type, expected {} but was {}",
                     ItineRennesApplication.class, getContext().getClass());
             initialized = false;
         }
         return initialized;
     }
 
     /**
      * Performs a database query.
      * 
      * @param selection
      *            The selection clause
      * @param selectionArgs
      *            Selection arguments for "?" components in the selection
      * @param columns
      *            The columns to return
      * @return A Cursor over all rows matching the query
      */
     private Cursor query(final String selection, final String[] selectionArgs,
             final String[] columns) {
 
         final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
         builder.setTables(Columns.MarkersColumns.MARKERS_TABLE_NAME);
         builder.setProjectionMap(PROJECTION_MAP);
 
         final Cursor cursor = builder.query(dbHelper.getReadableDatabase(), columns, selection,
                 selectionArgs, null, null, null);
 
         if (cursor == null) {
             return null;
         } else if (!cursor.moveToFirst()) {
             cursor.close();
             return null;
         }
         return cursor;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[],
      *      java.lang.String, java.lang.String[], java.lang.String)
      */
     @Override
     public final Cursor query(final Uri uri, final String[] projection, final String selection,
             final String[] selectionArgs, final String sortOrder) {
 
         String[] columns = null;
         String where = selection;
         String[] params = selectionArgs;
 
         switch (URI_MATCHER.match(uri)) {
         case GET_MARKER:
             return getMarker(uri);
         case GET_MARKERS:
 
             columns = new String[] { BaseColumns._ID, Columns.MarkersColumns.ID,
                     Columns.MarkersColumns.TYPE, Columns.MarkersColumns.LABEL,
                     Columns.MarkersColumns.LONGITUDE, Columns.MarkersColumns.LATITUDE };
             break;
         case GET_SUGGEST:
             where = String.format("%s LIKE ?", Columns.MarkersColumns.LABEL);
             params = new String[] { "%" + selectionArgs[0] + "%" };
 
            columns = new String[] { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
                    SearchManager.SUGGEST_COLUMN_TEXT_2,
                     SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID };
             break;
 
         default:
             throw new IllegalArgumentException("Unknown URI " + uri);
         }
 
         return query(where, params, columns);
     }
 
     /**
      * Returns a Cursor positioned at the marker specified at the end of the uri.
      * 
      * @param uri
      *            uri requested
      * @return Cursor positioned to matching marker, or null if not found.
      */
     private Cursor getMarker(final Uri uri) {
 
         final String[] columns = new String[] { BaseColumns._ID, Columns.MarkersColumns.ID,
                 Columns.MarkersColumns.TYPE, Columns.MarkersColumns.LABEL,
                 Columns.MarkersColumns.LONGITUDE, Columns.MarkersColumns.LATITUDE };
 
         final String rowId = uri.getLastPathSegment();
 
         final String selection = BaseColumns._ID + " = ?";
         final String[] selectionArgs = new String[] { rowId };
 
         return query(selection, selectionArgs, columns);
 
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see android.content.ContentProvider#getType(android.net.Uri)
      */
     @Override
     public final String getType(final Uri uri) {
 
         switch (URI_MATCHER.match(uri)) {
         case GET_MARKERS:
             return Columns.MarkersColumns.CONTENT_TYPE;
         case GET_SUGGEST:
             return SearchManager.SUGGEST_MIME_TYPE;
 
         default:
             throw new IllegalArgumentException("Unknown URI " + uri);
         }
     }
 
     @Override
     public final Uri insert(final Uri uri, final ContentValues values) {
 
         throw new UnsupportedOperationException();
     }
 
     @Override
     public final int delete(final Uri uri, final String selection, final String[] selectionArgs) {
 
         throw new UnsupportedOperationException();
     }
 
     @Override
     public final int update(final Uri uri, final ContentValues values, final String selection,
             final String[] selectionArgs) {
 
         throw new UnsupportedOperationException();
     }
 
 }

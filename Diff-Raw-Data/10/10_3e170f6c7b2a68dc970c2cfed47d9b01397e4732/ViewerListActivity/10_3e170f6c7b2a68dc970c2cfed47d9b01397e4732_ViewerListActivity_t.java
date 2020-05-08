 package ru.spb.cupchinolabs.androidlocator;
 
 import android.app.ListActivity;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.widget.CursorAdapter;
 import android.widget.SimpleCursorAdapter;
 
 /**
  * Created with IntelliJ IDEA.
  * User: VladimirK
  * Date: 04.03.13
  * Time: 21:46
  */
 public class ViewerListActivity extends ListActivity {
 
     private static final String TAG = ViewerListActivity.class.getSimpleName();
     private static final Uri PARSED_LOCATION_URI = Uri.parse(LocatorProviderContract.LOCATION_TABLE_URI);
 
     private final static String[] PROJECTION = {
             LocatorProviderContract.Location._ID,
             LocatorProviderContract.Location.TIME,
             LocatorProviderContract.Location.LATITUDE,
             LocatorProviderContract.Location.LONGITUDE,
             LocatorProviderContract.Location.PROVIDER
     };
 
     private final static String[] FROM_COLUMNS = {
             LocatorProviderContract.Location.TIME,
             LocatorProviderContract.Location.PROVIDER,
             LocatorProviderContract.Location.LATITUDE,
             LocatorProviderContract.Location.LONGITUDE
     };
 
     private ContentObserver contentObserver;
 
     private CursorAdapter cursorAdapter;
 
     private Handler handler;
 
     private final static int[] TO_IDS = {R.id.time, R.id.provider, R.id.latitude, R.id.longitude};
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(TAG, "onCreate");
 
         if (savedInstanceState != null) {
             //TODO restore current position in list
         }
 
         handler = new Handler();
 
         Cursor cursor = createNewCursor();
 
         if (null == cursor) {
            throw new IllegalStateException("error occurred -> provider is returning null as a cursor, URI is wrong");
         } else if (cursor.getCount() < 1) {
             Log.d(TAG, "cursor returns an empty result");
             //TODO show empty message
         }
 
        cursorAdapter = new SimpleCursorAdapter(this, R.layout.viewer_list_entry, cursor, FROM_COLUMNS, TO_IDS);
        setListAdapter(cursorAdapter);

         contentObserver = new ContentObserver(handler) {
             @Override
             public void onChange(boolean selfChange) {
                 super.onChange(selfChange);
                 Log.d(TAG + "-ContentObserver", "onChange");
                 new AsyncTask() {
                     @Override
                     protected Object doInBackground(Object... objects) {
                         final Cursor newCursor = createNewCursor();
                         handler.post(new Runnable() {
                             @Override
                             public void run() {
                                 cursorAdapter.changeCursor(newCursor);
                                 cursorAdapter.notifyDataSetChanged();
                             }
                         });
                         return null;
                     }
                 }.execute();
             }
         };
 
         getContentResolver().registerContentObserver(PARSED_LOCATION_URI, true, contentObserver);
     }
 
     private Cursor createNewCursor() {
         return getContentResolver().query(
                 PARSED_LOCATION_URI, PROJECTION, null, new String[]{""}, LocatorProviderContract.Location._ID + " DESC");
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         Log.d(TAG, "onStart");
         //TODO retrieve fresh list?
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         Log.d(TAG, "onSaveInstanceState");
         //TODO save current position in list (scroll position of a ListView?)
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         Log.d(TAG, "onDestroy");
         getContentResolver().unregisterContentObserver(contentObserver);
         contentObserver = null;
         cursorAdapter = null;
         handler = null;
     }
 }

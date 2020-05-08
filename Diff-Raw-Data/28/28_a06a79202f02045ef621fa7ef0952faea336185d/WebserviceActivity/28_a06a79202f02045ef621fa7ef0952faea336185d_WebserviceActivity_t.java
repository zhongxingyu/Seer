 package edu.aau.utzon;
 
 import edu.aau.utzon.webservice.ProviderContract;
 import edu.aau.utzon.webservice.RestServiceHelper;
 import android.app.Activity;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 
 public class WebserviceActivity extends Activity{
 	public final static  String[] mProjectionAll = {ProviderContract.Points.ATTRIBUTE_ID, 
 		ProviderContract.Points.ATTRIBUTE_X, 
 		ProviderContract.Points.ATTRIBUTE_Y, 
 		ProviderContract.Points.ATTRIBUTE_DESCRIPTION};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.webservice);
 
 		// Register content observer
 		RestContentObserver mContentObserver = new RestContentObserver(new Handler());
 		this.getApplicationContext()
 		.getContentResolver()
 		.registerContentObserver(ProviderContract.Points.CONTENT_URI, true, mContentObserver);
		
 		// Asynchornously start a REST method
 		RestServiceHelper.getServiceHelper()
 		.getLocationPoints(this);
 		
 		// Get what is currently in the local db
         Cursor c = getContentResolver().query(
                 ProviderContract.Points.CONTENT_URI,   	// The content URI of the points table
                 mProjectionAll,                        	// The columns to return for each row
                 null,                    				// Selection criteria
                 null,                     				// Selection criteria
                 null);                        			// The sort order for the returned rows
         // listadapter(c)
 
 		/* DEBUGGING content provider
         String[] mProjection = {ProviderContract.Points.ATTRIBUTE_ID, 
         		ProviderContract.Points.ATTRIBUTE_X, 
         		ProviderContract.Points.ATTRIBUTE_Y, 
         		ProviderContract.Points.ATTRIBUTE_DESCRIPTION};
 
 		// Query / Get
         getContentResolver().query(
             ProviderContract.Points.CONTENT_URI,   	// The content URI of the points table
             mProjection,                        	// The columns to return for each row
             null,                    				// Selection criteria
             null,                     				// Selection criteria
             null);                        			// The sort order for the returned rows
 
         // Insert
 		ContentValues values = new ContentValues();
 		values.put(ProviderContract.Points.ATTRIBUTE_X, 0.5);
 		values.put(ProviderContract.Points.ATTRIBUTE_Y, 1.5);
 		values.put(ProviderContract.Points.ATTRIBUTE_DESCRIPTION, "HEjsaDescription");
 
 		Uri mNewUri = getContentResolver().insert(ProviderContract.Points.CONTENT_URI, values);
 		 */
 
		Log.e("Utzon", "Why you call them hoes bitches?");
 	}
 
 	class RestContentObserver extends ContentObserver{
 		public RestContentObserver(Handler handler) {
 			super(handler);
 		}
 
 		@Override
 		public boolean deliverSelfNotifications() {
 			return true;
 		}
 
 		@Override
 		public void onChange(boolean selfChange) {
 			super.onChange(selfChange);
 			// New content is available
 			Cursor c = managedQuery(ProviderContract.Points.CONTENT_URI,   	// The content URI of the points table
 					mProjectionAll,                        	// The columns to return for each row
 					null,                    				// Selection criteria
 					null,                     				// Selection criteria
 					null);                        			// The sort order for the returned rows
 			// listadapter(c);
 
		
			
 			Log.e("Utzon", "Cos them hoes is bitches!");
 		}
 	}
 }
 
 

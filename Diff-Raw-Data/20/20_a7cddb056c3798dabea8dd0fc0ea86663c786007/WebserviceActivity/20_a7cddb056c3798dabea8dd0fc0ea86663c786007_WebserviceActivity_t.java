 package edu.aau.utzon;
 
 import java.util.ArrayList;
 
 import com.google.android.maps.GeoPoint;
 
 import edu.aau.utzon.webservice.PointModel;
 import edu.aau.utzon.webservice.ProviderContract;
 import edu.aau.utzon.webservice.RestServiceHelper;
 import android.app.Activity;
 import android.app.ListActivity;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.widget.SimpleCursorAdapter;
 
/** THIS CLASS IS USELESS ! DESTROY IT WITH FIRE **/
/** Query for POIs : getContentResolver().query(ProviderContract.Points.CONTENT_URI, ProviderContract.Points.PROJECTIONSTRING_ALL, null, null, null); **/
 public class WebserviceActivity extends ListActivity{
 	public final static  String[] mProjectionAll = {ProviderContract.Points.ATTRIBUTE_ID, 
 		ProviderContract.Points.ATTRIBUTE_X, 
 		ProviderContract.Points.ATTRIBUTE_Y, 
 		ProviderContract.Points.ATTRIBUTE_DESCRIPTION};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.webservice);
 
 //		// Register content observer
 //		RestContentObserver mContentObserver = new RestContentObserver(new Handler());
 //		this.getApplicationContext()
 //		.getContentResolver()
 //		.registerContentObserver(ProviderContract.Points.CONTENT_URI, true, mContentObserver);
 
 		// Asynchornously start a REST method
//		RestServiceHelper.getServiceHelper()
//		.getLocationPoints(this);
 
 		// Get what is currently in the local db
 		// Cursor c = getContentResolver().query(
 		//        ProviderContract.Points.CONTENT_URI,   	// The content URI of the points table
 		//        mProjectionAll,                        	// The columns to return for each row
 		//         null,                    				// Selection criteria
 		//         null,                     				// Selection criteria
 		//         null);                        			// The sort order for the returned rows
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
 
 		Log.e("TACO", "Why you call them hoes bitches?");
 	}
 
 //	private class RestContentObserver extends ContentObserver{
 //		public RestContentObserver(Handler handler) {
 //			super(handler);
 //		}
 //
 //		@Override
 //		public boolean deliverSelfNotifications() {
 //			return true;
 //		}
 //
 //		@Override
 //		public void onChange(boolean selfChange) {
 //			super.onChange(selfChange);
 //			// New content is available
 //			Cursor c = managedQuery(ProviderContract.Points.CONTENT_URI,   	// The content URI of the points table
 //					mProjectionAll,                        	// The columns to return for each row
 //					null,                    				// Selection criteria
 //					null,                     				// Selection criteria
 //					null);                        			// The sort order for the returned rows
 //			// listadapter(c);
 //
 //			//Log.e("TACO", c.getInt(0));
 //
 //			//Log.e("TACO", Integer.toString(c.getInt(0)));
 //
 //			ArrayList<PointModel> points = new ArrayList<PointModel>();
 //
 //			c.moveToFirst();
 //			do
 //			{
 //				int colIndexId = c.getColumnIndex(ProviderContract.Points.ATTRIBUTE_ID);
 //				int colIndexDesc = c.getColumnIndex(ProviderContract.Points.ATTRIBUTE_DESCRIPTION);
 //				int colIndexX = c.getColumnIndex(ProviderContract.Points.ATTRIBUTE_X);
 //				int colIndexY = c.getColumnIndex(ProviderContract.Points.ATTRIBUTE_Y);
 //
 //				int id = c.getInt(colIndexId);
 //				String desc = c.getString(colIndexDesc);
 //				float x = c.getFloat(colIndexX);
 //				float y = c.getFloat(colIndexY);
 //
 //
 //				Log.e("TACO", Integer.toString(id) + " - " + desc);
 //
 //				PointModel p = new PointModel();
 //				p.mDesc = desc;
 //				p.mId = id;
 //				p.mGeoPoint = new GeoPoint((int)x,(int)y);
 //				
 //				points.add(p);
 //
 //			} while (c.moveToNext() == true);
 //
 //			//startManagingCursor(c);
 //
 //
 //			//String[] columns = new String[] { ProviderContract.Points.ATTRIBUTE_ID,
 //			//		ProviderContract.Points.ATTRIBUTE_DESCRIPTION,
 //			//ProviderContract.Points.ATTRIBUTE_LAST_MODIFIED,
 //			//ProviderContract.Points.ATTRIBUTE_STATE,
 //			//		ProviderContract.Points.ATTRIBUTE_X,
 //			//		ProviderContract.Points.ATTRIBUTE_Y};
 //
 //			// THE XML DEFINED VIEWS WHICH THE DATA WILL BE BOUND TO
 //
 //			//int[] to = new int[] { R.id.id, R.id.desc, R.id.x, R.id.y};
 //
 //			//SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.webservice_entry, c, columns, to);
 //
 //
 //			// SET THIS ADAPTER AS YOUR LISTACTIVITY'S ADAPTER
 //
 //			//setListAdapter(mAdapter);
 //
 //
 //			Log.e("TACO", "Cos them hoes is bitches!");
 //		}
 //	}
 }
 
 

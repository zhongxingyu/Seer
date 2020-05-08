 package com.madhackerdesigns.neverbelate.ui;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
 import android.text.TextUtils;
import android.widget.Toast;
 
 import com.madhackerdesigns.neverbelate.service.CalendarHelper;
 import com.madhackerdesigns.neverbelate.settings.PreferenceHelper;
 import com.madhackerdesigns.neverbelate.util.Logger;
 
 public class CreateNewEvent extends Activity {
 	
 	private static final String LOG_TAG = "CreateNewEvent";
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// Get the intent data
 		Uri data = getIntent().getData();
 		Logger.v(LOG_TAG, "Data Uri is " + data.toString());
 		
 		// Determine the scheme of the Uri
 		String scheme = data.getScheme();
		String host = data.getHost();
 		String destination = "";
		if (scheme.equals("geo") || host.equals("maps.google.com")) {
			try {
				// geo:0,0?q=Cartersville%2C GA 30120-8232
				// http://maps.google.com/maps?q=500 Townpark Lane%2C Suite 275%2C Kennesaw%2C GA 30144-5509
				destination = data.getQuery().substring(2);
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
				Toast.makeText(this, "Oops! No location found.", Toast.LENGTH_SHORT).show();
			}
 		} else if (scheme.equals("content")) {
 			// content://com.android.contacts/data/2316
 			ContentResolver cr = getContentResolver();
 			String[] projection = new String[] { StructuredPostal._ID, StructuredPostal.FORMATTED_ADDRESS };
 			Cursor cursor = cr.query(data, projection, null, null, null);
 			if (cursor.moveToFirst()) {
 				destination = cursor.getString(1);
 			}
 		}
 		
 		// If we somehow didn't catch the destination, then die nicely
 		if (TextUtils.isEmpty(destination)) { finish(); }
 		
 		// Add star if marked events used
 		PreferenceHelper prefs = new PreferenceHelper(this);
 		if (prefs.isOnlyMarkedLocations()) {
 			destination = "*" + destination;
 		}
 		
 		// Create new event with given destination
 		Logger.v(LOG_TAG, "Creating new event for location: " + destination);
 		CalendarHelper ch = CalendarHelper.createHelper();
 		Intent intent = new Intent(Intent.ACTION_EDIT);
 		intent.setType("vnd.android.cursor.item/event");
 		intent.putExtra(ch.getEventLocationColumn(), destination);
 		intent.putExtra(ch.getHasAlarmColumn(), 0);
 		startActivity(intent);
 		
 		// Now finish to get out of the way
 		finish();
 	}
 
 }

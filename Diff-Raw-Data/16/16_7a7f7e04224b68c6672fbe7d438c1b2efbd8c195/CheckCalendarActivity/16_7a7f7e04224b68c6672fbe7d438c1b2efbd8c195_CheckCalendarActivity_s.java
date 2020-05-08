 package au.com.vivant.internal.meetingroomdisplay;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.CalendarContract.Instances;
 import android.util.Log;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class CheckCalendarActivity extends Activity {
 
 	private static final String DEBUG_TAG = "MyActivity";
 	public static final String[] INSTANCE_PROJECTION = new String[] {
 			Instances.EVENT_ID, // 0
 			Instances.BEGIN, // 1
 			Instances.END, //2
 			Instances.TITLE, // 3
 			Instances.DESCRIPTION, //4
 			Instances.ORGANIZER //5
 	};
 
 	// The indices for the projection array above.
 	private static final int PROJECTION_ID_INDEX = 0;
 	private static final int PROJECTION_BEGIN_INDEX = 1;
 	private static final int PROJECTION_END_INDEX = 2;
 	private static final int PROJECTION_TITLE_INDEX = 3;
 	private static final int PROJECTION_DESCRIPTION_INDEX = 4;
 	private static final int PROJECTION_ORGANIZER_INDEX = 5;
 	
 	DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");
 
 	// Specify the date range you want to search for recurring
 	// event instances
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.activity_check_calendar);
 		
 		Calendar beginTime = Calendar.getInstance();
		//beginTime.set(beginTime.get(Calendar.YEAR), beginTime.get(Calendar.MONTH), beginTime.get(Calendar.DAY_OF_MONTH), 8, 0);
		long startMillis = beginTime.getTimeInMillis();
 		Calendar endTime = Calendar.getInstance();
		//endTime.set(2012, 12, 12, 8, 0);
		endTime.set(beginTime.get(Calendar.YEAR), beginTime.get(Calendar.MONTH), beginTime.get(Calendar.DAY_OF_MONTH), 19, 0);
		long endMillis = endTime.getTimeInMillis();
 
 		Cursor cur = null;
 		ContentResolver cr = getContentResolver();
 
 		// The ID of the recurring event whose instances you are searching
		// for in the Instances table
 		String selection = Instances.EVENT_ID + " > ?";
 		String[] selectionArgs = new String[] {"0"};
 
 		// Construct the query with the desired date range.
 		Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
 		ContentUris.appendId(builder, startMillis);
 		ContentUris.appendId(builder, endMillis);
 
 		// Submit the query
 		cur = cr.query(builder.build(),
 		        INSTANCE_PROJECTION, selection,
 		        selectionArgs, "startDay ASC, startMinute ASC");
 		
 		StringBuilder events = new StringBuilder();
 		TextView message = (TextView)findViewById(R.id.display);
 		int i = 0;
 		while (cur.moveToNext()) {
 			// Get the field values
 			long eventId = cur.getLong(PROJECTION_ID_INDEX);
 			long beginVal = cur.getLong(PROJECTION_BEGIN_INDEX);
 			long endVal = cur.getLong(PROJECTION_END_INDEX);
 			String title = cur.getString(PROJECTION_TITLE_INDEX);
 			String description = cur.getString(PROJECTION_DESCRIPTION_INDEX);
 			String organizer = cur.getString(PROJECTION_ORGANIZER_INDEX);
 			
 			events.append(eventId).append(",");
 			events.append(formatter.format(new Date(beginVal))).append(",");
 			events.append(formatter.format(new Date(endVal))).append(",");
 			events.append(title).append(",");
 			events.append(description).append(",");
 			events.append(organizer).append(",");
 			
 			// Do something with the values.
 			Log.i(DEBUG_TAG, "Event:  " + title);
 			Calendar calendar = Calendar.getInstance();
 			calendar.setTimeInMillis(beginVal);
 			
 			if(i == 0) {
 				LinearLayout ll = (LinearLayout)findViewById(R.id.background);
 				long timeLeft = (beginVal) - System.currentTimeMillis();
 				if(timeLeft < 1000 * 60) {
 					ll.setBackgroundResource(android.R.color.holo_red_light);
 					message.setText("EVENT STARTING NOW\n" + title + "\n End Time: "+new SimpleDateFormat("hh:mm a").format(new Date(endVal)));
 				} else if (timeLeft < 1000 * 60 * 10) {
 					ll.setBackgroundResource(android.R.color.holo_orange_light);
 					message.setText("EVENT STARTING SOON\n" + title + "\n End Time: "+new SimpleDateFormat("hh:mm a").format(new Date(endVal)));
 				} else {
 					ll.setBackgroundResource(android.R.color.holo_blue_light);
 					message.setText("FREE TILL\n" + title + "\n End Time: "+new SimpleDateFormat("hh:mm a").format(new Date(endVal)));
 				}
 			}
 			
 			Log.i(DEBUG_TAG, "Date: " + formatter.format(calendar.getTime()));
 			i++;
 		}
 	}
 }

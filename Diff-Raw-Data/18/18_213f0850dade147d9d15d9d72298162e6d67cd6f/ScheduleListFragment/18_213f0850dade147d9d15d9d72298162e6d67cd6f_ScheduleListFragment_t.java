 package com.doLast.doGRT;
 
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.TwoLineListItem;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.doLast.doGRT.database.DatabaseSchema;
 import com.doLast.doGRT.database.DatabaseSchema.CalendarColumns;
 import com.doLast.doGRT.database.DatabaseSchema.RoutesColumns;
 import com.doLast.doGRT.database.DatabaseSchema.StopTimesColumns;
 import com.doLast.doGRT.database.DatabaseSchema.TripsColumns;
 
 public class ScheduleListFragment extends SherlockListFragment {
 	private RoutesActivity mActivity = null;
 	
 	// Cursor adapter
 	private SimpleCursorAdapter adapter = null;
 	
 	// Stop id, name and title
 	private String stop_id = null;
 	private String stop_name = null;
 	private String stop_title = null;
 	
 	// Route id for individual route
 	private String route_id = null;
 	
 	// Today's service id
 	private String service_ids = null;	
 	
 	// Left buses display offset
 	private final int LEFT_BUSES_OFFSET = 2;	
 	
 	// Display type
 	private int display_type = RoutesActivity.SCHEDULE_MIXED;
 	
 	// To control back key when single route is displayed
 	private boolean single_route = false;
 	
     public static ScheduleListFragment newInstance(int type) {
     	ScheduleListFragment f = new ScheduleListFragment(type);
         return f;
     }
     
     // For orientation change
     public ScheduleListFragment() {}
     
     private ScheduleListFragment(int type) {
     	display_type = type;
     }
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 	   	super.onActivityCreated(savedInstanceState);
 		mActivity = (RoutesActivity)getActivity();
 		
 		// Get the stop id, name and title
 		stop_id = mActivity.getStopId();
 		stop_name = mActivity.getStopName();
 		stop_title = mActivity.getStopTitle();
 		
         // Determine service id
     	service_ids = getServiecId();
 		
     	// Display schedule
     	switch(display_type) {
     	case RoutesActivity.SCHEDULE_MIXED:
 			displaySchedule(stop_id, null);
 			break;
     	case RoutesActivity.SCHEDULE_SELECT:
			if (isSingleRouteDisplayed()) {
				displaySchedule(stop_id, route_id);
			} else {
				displayRoutes(stop_id);
			}
 			break;
     	default:
 		}
     	
     	ListView list_view = getListView();
     	TextView text_view = (TextView)mActivity.findViewById(R.id.schedule_empty_view);
     	list_view.setEmptyView(text_view);
 	}
 	
     @Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		// TODO Auto-generated method stub
    	if (!isSingleRouteDisplayed() && display_type == RoutesActivity.SCHEDULE_SELECT) {
     		TwoLineListItem text_view = (TwoLineListItem)v;
     		route_id = (String) text_view.getText2().getText();
     		displaySchedule(stop_id, route_id);
     	}
     	
 		super.onListItemClick(l, v, position, id);
 	}
 
	public void backKeyPressed() {
     	single_route = false;
     	displayRoutes(stop_id);    	
     }
     
     // Retrieve today's service ids
 	private String getServiecId() {
     	String service_ids = new String("");
     	String selection = new String("");
         switch(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
         case Calendar.SUNDAY:
         	selection = "sunday";
         	break;
         case Calendar.THURSDAY:
         	selection = "thursday";
         	break;
         case Calendar.MONDAY:
         	selection = "monday";
         	break;
         case Calendar.TUESDAY:
         	selection = "tuesday";
         	break;
         case Calendar.WEDNESDAY:
         	selection = "wednesday";
         	break;
         case Calendar.FRIDAY:
         	selection = "friday";
         	break;
         case Calendar.SATURDAY:
         	selection = "saturday";
         	break;
         default:
         	break;
         }
         
         String[] projection = { CalendarColumns.SERVICE_ID };
         Cursor services = mActivity.managedQuery(CalendarColumns.CONTENT_URI, projection, selection, null, null);
         services.moveToFirst();
         for(int i = 0; i < services.getCount() - 1; i += 1){
         	service_ids += " OR " + TripsColumns.SERVICE_ID + " = '" + services.getString(0) + "'";
         	services.moveToNext();
         }
         service_ids = TripsColumns.SERVICE_ID + " = '" + services.getString(0) + "'" + service_ids;
         //services.close();
         return service_ids;
     }
     
 	
 	// Display the schedule with given stop id and route id, route_id can be null if want mixed schedule
     private void displaySchedule(String stop_id, String route_id) {    	
         // Remember to perform an alias of our own primary key to _id so the adapter knows what to do
         String[] projection = { StopTimesColumns.TABLE_NAME + "." + StopTimesColumns.TRIP_ID + " as _id", 
         						StopTimesColumns.DEPART,
         						RoutesColumns.LONG_NAME,
         						RoutesColumns.TABLE_NAME + "." + RoutesColumns.ROUTE_ID,
         						TripsColumns.TABLE_NAME + "." + TripsColumns.HEADSIGN };
         // Some complex selection for selecting from 3 tables
         String stop_time_id = StopTimesColumns.TABLE_NAME + "." + StopTimesColumns.STOP_ID;
         String stop_time_trip_id = StopTimesColumns.TABLE_NAME + "." + StopTimesColumns.TRIP_ID;
         String trip_trip_id = TripsColumns.TABLE_NAME + "." + TripsColumns.TRIP_ID;
         String trip_route_id = TripsColumns.TABLE_NAME + "." + TripsColumns.ROUTE_ID;
         String trip_service_id = TripsColumns.TABLE_NAME + "." + TripsColumns.SERVICE_ID;
         String route_route_id = RoutesColumns.TABLE_NAME + "." + RoutesColumns.ROUTE_ID;
         
         String selection =  stop_time_id + " = " + stop_id + " AND " +
         					stop_time_trip_id + " = " + trip_trip_id + " AND " +
         					trip_route_id + " = " + route_route_id + " AND " +
         					"(" + service_ids + ")";
         if (route_id != null) {
         	selection += " AND " + route_route_id + " = " + route_id;
         	single_route = true;
         }
         String orderBy = StopTimesColumns.DEPART;
         Cursor stop_times = mActivity.managedQuery(
         		DatabaseSchema.STTRJ_CONTENT_URI, projection, selection, null, orderBy);
         
         String[] uiBindFrom = { StopTimesColumns.DEPART, RoutesColumns.ROUTE_ID, RoutesColumns.LONG_NAME };
         int[] uiBindTo = { R.id.depart_time, R.id.route_name };                 
                 
         // Move adapter to schedule close to current time
         Calendar time = Calendar.getInstance();
         int cur_time = time.get(Calendar.HOUR_OF_DAY) * 10000;
         cur_time += time.get(Calendar.MINUTE) * 100;
         cur_time += time.get(Calendar.SECOND);
         
         // Iterate through cursor
         int cur_pos = 0;
         stop_times.moveToFirst();
         for(int i = 0; i < stop_times.getCount(); i += 1) {        	
         	int depart = stop_times.getInt(1); // Get the departure time from cursor as and integer
         	if ( depart > cur_time ) {
         		cur_pos = i;
         		//section = COMING_BUSES;
         		break;
         	}	
         	stop_times.moveToNext();
         }            
         
         // Assign adapter to ListView
         adapter = new ScheduleAdapter(mActivity, R.layout.schedule, stop_times,
                 uiBindFrom, uiBindTo, cur_pos);
         setListAdapter(adapter);
         if (cur_pos >= LEFT_BUSES_OFFSET) cur_pos -= LEFT_BUSES_OFFSET;
         setSelection(cur_pos);           
     }
     
     private void displayRoutes(String stop_id) {    	
         // Remember to perform an alias of our own primary key to _id so the adapter knows what to do
         String[] projection = { RoutesColumns.TABLE_NAME + "." + RoutesColumns.ROUTE_ID + " as _id", 
         						RoutesColumns.LONG_NAME };
         // Some complex selection for selecting from 3 tables
         String stop_time_id = StopTimesColumns.TABLE_NAME + "." + StopTimesColumns.STOP_ID;
         String stop_time_trip_id = StopTimesColumns.TABLE_NAME + "." + StopTimesColumns.TRIP_ID;
         String trip_trip_id = TripsColumns.TABLE_NAME + "." + TripsColumns.TRIP_ID;
         String trip_route_id = TripsColumns.TABLE_NAME + "." + TripsColumns.ROUTE_ID;
         String trip_service_id = TripsColumns.TABLE_NAME + "." + TripsColumns.SERVICE_ID;
         String route_route_id = RoutesColumns.TABLE_NAME + "." + RoutesColumns.ROUTE_ID;
         
         String selection =  stop_time_id + " = " + stop_id + " AND " +
         					stop_time_trip_id + " = " + trip_trip_id + " AND " +
         					trip_route_id + " = " + route_route_id + " AND " +
         					"(" + service_ids + ")";
         Cursor routes = mActivity.managedQuery(
         		DatabaseSchema.STTRJ_CONTENT_URI, projection, selection, null, null);
         
         String[] uiBindFrom = { RoutesColumns.LONG_NAME, "_id" };
         int[] uiBindTo = { android.R.id.text1, android.R.id.text2 }; 
         
         // Assign adapter to list view
         adapter = new SimpleCursorAdapter(mActivity, android.R.layout.simple_list_item_2, routes,
         		uiBindFrom, uiBindTo);
         setListAdapter(adapter);
     }    
     
     public boolean isSingleRouteDisplayed() {
     	return single_route;
     }
 }

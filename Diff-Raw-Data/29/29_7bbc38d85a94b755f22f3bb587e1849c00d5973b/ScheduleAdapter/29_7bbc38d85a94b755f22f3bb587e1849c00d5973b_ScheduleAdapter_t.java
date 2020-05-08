 package com.doLast.doGRT;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 import com.doLast.doGRT.database.DatabaseSchema.RoutesColumns;
 import com.doLast.doGRT.database.DatabaseSchema.StopTimesColumns;
 import com.doLast.doGRT.database.DatabaseSchema.TripsColumns;
 
 public class ScheduleAdapter extends SimpleCursorAdapter {
 	private Context mContext;
 	private int mLayout;
 	private LayoutInflater mInflater;
 	private int separator_pos = 0;
 	
 	public ScheduleAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int separator_pos) {
         super(context, layout, c, from, to);
         mContext = context;
         mLayout = layout;
         mInflater = LayoutInflater.from(mContext);
         this.separator_pos = separator_pos;
 	}
 
 	@Override
 	public void bindView(View view, Context context, Cursor cursor) {
 		// Check if the view has already been inflated
 		View v = view;
 		if(v == null)
 			v = mInflater.inflate(R.id.schedule_row, null);
		v.setClickable(false);
 				
 		// TODO Auto-generated method stub
 		TextView time_view = (TextView)v.findViewById(R.id.depart_time);
 		// Truncate the time into a readable format
 		String time = cursor.getString(cursor.getColumnIndex(StopTimesColumns.DEPART));
 		String minute = time.substring(time.length() - 4, time.length() - 2);
 		String hour = time.substring(0, time.length() - 4);
 		// Check if hour is greater than 24, change it to 0
 		if (Integer.valueOf(hour) >= 24)
 			hour = String.valueOf((Integer.valueOf(hour) - 24));
 		// Add a prefix 0 to hour less than 10
 		if (Integer.valueOf(hour) < 10)
 			hour = "0" + hour;
		time_view.setText(hour + ":" + minute);
 
 		// Keep original route name
 		TextView route_view = (TextView)v.findViewById(R.id.route_name);
 		route_view.setText(cursor.getString(cursor.getColumnIndex(RoutesColumns.ROUTE_ID)) + " " +
 							cursor.getString(cursor.getColumnIndex(TripsColumns.HEADSIGN)));	
 		
 		// Separator
 		TextView separator = (TextView)v.findViewById(R.id.separator);
 		if (cursor.getPosition() == separator_pos) {
 			separator.setText(R.string.coming_buses);
 			separator.setVisibility(View.VISIBLE);
 		} else if (cursor.getPosition() == 0) {
 			separator.setText(R.string.left_buses);
 			separator.setVisibility(View.VISIBLE);
 		} else {
 			separator.setVisibility(View.GONE);
 		}
 		
 		// Change the text colour if the buses passed
 		if (cursor.getPosition() < separator_pos) {
 			time_view.setTextColor(mContext.getResources().getColor(R.color.Grey));
 			route_view.setTextColor(mContext.getResources().getColor(R.color.Grey));
 
 		} else {
 			time_view.setTextColor(mContext.getResources().getColor(R.color.Black));
 			route_view.setTextColor(mContext.getResources().getColor(R.color.Black));
 		}
 	}
 
 	@Override
 	public View newView(Context context, Cursor cursor, ViewGroup parent) {
         final View view = mInflater.inflate(R.layout.schedule_row, parent, false); 
         return view;
 	}			
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		return super.getView(position, convertView, parent);
 	}
 }

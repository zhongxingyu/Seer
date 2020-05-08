 package com.engine9;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
<<<<<<< HEAD
/*This class is used to translate an array of listing data into an array
=======
  /** 
  * This class is used to translate an array of listing data into an array
>>>>>>> 44fe24194e9224a290360ffb88b96f0b2d2abe9f
  * of Views for a ViewList to use.  In this instance, it is being used
  * for TimeTableActivity
  */
 public class TimeAdapter extends ArrayAdapter<Listing> {
 
 	private Context context;
 	private Listing[] values;
 	
<<<<<<< HEAD
	/*
	 * @param context The context the ListView belongs to
	 * @param values An array of Listing classes
	 */
=======
 	/**
 	 * Basic constructor for the time
 	 * 
 	 * 		@param context
	 * 			where is being created
 	 * 		@param values
	 * 			An array of listing, all the info will be displayed*/
>>>>>>> 44fe24194e9224a290360ffb88b96f0b2d2abe9f
 	public TimeAdapter(Context context, Listing[] values) {
 		super(context, R.layout.list_timetable, values);
 		this.values = values;
 		this.context = context;
 	}
 
 	@Override
 	public int getCount() {
 		// TODO Auto-generated method stub
 		return values.length;
 	}
 
 
 	@Override
 	public long getItemId(int arg0) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
 	 * @param position 
 	 */
 	@Override
 	public View getView(int position, View convert, ViewGroup parent) {
 		LayoutInflater inflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		
 		View row = inflater.inflate(R.layout.list_timetable, parent, false);
 		TextView codeV = (TextView) row.findViewById(R.id.code);
 		TextView directionV = (TextView) row.findViewById(R.id.direction);
 		TextView timeV = (TextView) row.findViewById(R.id.time);
 		
 		codeV.setText(values[position].code);
 		directionV.setText(values[position].direction);
 		timeV.setText(String.valueOf(values[position].time));
 		
 		return row;
 	}
 
 }

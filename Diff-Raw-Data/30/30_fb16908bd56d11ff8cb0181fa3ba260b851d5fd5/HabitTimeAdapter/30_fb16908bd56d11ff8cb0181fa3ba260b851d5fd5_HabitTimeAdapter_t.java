 // From: http://www.codeproject.com/Articles/183608/Android-Lists-ListActivity-and-ListView-II-Custom
 
 package com.synaptian.smoketracker.habits;
 
 import java.util.List;
 import android.app.Activity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
import org.dhappy.android.widget.Timer;
 
 public class HabitTimeAdapter extends ArrayAdapter {
 	private final Activity activity;
 	private final List<HabitTime> habitTimes;
 
 	public HabitTimeAdapter(Activity activity, List<HabitTime> objects) {
 		super(activity, R.layout.habit_row, objects);
 		this.activity = activity;
 		this.habitTimes = objects;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View rowView = convertView;
 		HabitTimeView htView = null;
 
 		if (rowView == null) {
 			// Get a new instance of the row layout view
 			LayoutInflater inflater = activity.getLayoutInflater();
 			rowView = inflater.inflate(R.layout.habit_row, null);
 
 			// Hold the view objects in an object,
 			// so they don't need to be re-fetched
 			htView = new HabitTimeView();
 			htView.name = (TextView) rowView.findViewById(R.id.label);
			htView.timer = (Timer) rowView.findViewById(R.id.timer);
 
 			// Cache the view objects in the tag,
 			// so they can be re-accessed later
 			rowView.setTag(htView);
 		} else {
 			htView = (HabitTimeView) rowView.getTag();
 		}
 
 		// Transfer the stock data from the data object
 		// to the view objects
 		HabitTime currentTime = habitTimes.get(position);
 		htView.name.setText(currentTime.getName());
		htView.timer.setStartingTime(currentTime.getTime());
 
 		return rowView;
 	}
 
 	protected static class HabitTimeView {
 		protected TextView name;
		protected Timer timer;
 	}
 }

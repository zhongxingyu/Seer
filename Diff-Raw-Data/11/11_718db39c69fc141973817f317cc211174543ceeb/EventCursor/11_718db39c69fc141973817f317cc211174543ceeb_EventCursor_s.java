 package com.lsg.app;
 
 
 import android.content.Context;
 import android.database.Cursor;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CursorAdapter;
 import android.widget.TextView;
 
 public class EventCursor extends CursorAdapter {
 	static class Standard {
 		public TextView title;
 		public TextView date;
 		public TextView place;
 	}
 	public EventCursor(Context context, Cursor d) {
 		super(context, d);
 		}
 	@Override
 	public View newView(Context context, Cursor cursor, ViewGroup parent) {
 		LayoutInflater inflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			View rowView = inflater.inflate(R.layout.events_item, null, true);
 			Standard holder = new Standard();
 			holder.title = (TextView) rowView.findViewById(R.id.event_title);
 			holder.date = (TextView) rowView.findViewById(R.id.event_date);
 			holder.place = (TextView) rowView.findViewById(R.id.event_place);
 			rowView.setTag(holder);
 			return rowView;
 			}
 	
 	@Override
 	public void bindView(View view, Context context, Cursor cursor) {
 		Standard holder = (Standard) view.getTag();	
 		String title = cursor.getString(cursor.getColumnIndex(Functions.DB_TITLE));
 		holder.title.setText(title);
 		String datebeginning = cursor.getString(cursor.getColumnIndex(Functions.DB_DATES));
 		String timebeginning = cursor.getString(cursor.getColumnIndex(Functions.DB_TIMES));
 		String dateending = cursor.getString(cursor.getColumnIndex(Functions.DB_ENDDATES));
 		String timeending = cursor.getString(cursor.getColumnIndex(Functions.DB_ENDTIMES));
 		if (datebeginning.equals("null") && timebeginning.equals("null") && dateending.equals("null") && timeending.equals("null"))
 			holder.date.setText("Keine Zeit angegeben");
 		else if (timebeginning.equals("null") && dateending.equals("null") && timeending.equals("null"))
 			holder.date.setText("am " + datebeginning);
 		else if (timebeginning.equals("null") && timeending.equals("null"))
 			holder.date.setText("vom " + datebeginning + " bis zum " + dateending);
 		else if (dateending.equals("null") && timeending.equals("null"))
 			holder.date.setText("am " + datebeginning + " um " + timebeginning);
 		else if (dateending.equals("null"))
 			holder.date.setText("am " + datebeginning + " von " + timebeginning + " bis " + timeending);
 		else
 			holder.date.setText("von " + datebeginning + " " + timebeginning + " bis " + dateending + " " + timeending);
 		String place = cursor.getString(cursor.getColumnIndex(Functions.DB_VENUE));
		if (place.equals("null"))
 			holder.place.setText("");
		else
 			holder.place.setText("Ort: " + place);
 	}
 }

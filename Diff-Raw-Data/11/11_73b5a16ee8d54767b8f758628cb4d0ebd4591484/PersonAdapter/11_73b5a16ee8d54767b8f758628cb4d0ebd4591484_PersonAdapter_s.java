 package com.shepherd;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.shepherd.api.Person;
 
 import android.app.Activity;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class PersonAdapter extends ArrayAdapter<Person> {
 
 	public PersonAdapter(Activity context, ArrayList<Person> objects) {
 		super(context, R.layout.person_row);
 		this.activity = context;
 		this.list = objects;
 	}
 
 	private final Activity activity;
 	private final ArrayList<Person> list;
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View rowView = convertView;
 		ViewHolder view;
 
 		if (rowView == null) {
 			// Get a new instance of the row layout view
 			LayoutInflater inflater = activity.getLayoutInflater();
 			rowView = inflater.inflate(R.layout.person_row, null);
 
 			// Hold the view objects in an object, that way the don't need to be
 			// "re- finded"
 			view = new ViewHolder();
 
 			view.pic = (ImageView) rowView
 					.findViewById(R.id.thumbnailImageView);
 			view.name = (TextView) rowView.findViewById(R.id.nameTextView);
 
 			rowView.setTag(view);
 		} else {
 			view = (ViewHolder) rowView.getTag();
 		}
 
 		/** Set data to your Views. */
 		Person item = list.get(position);
 
		view.name.setText(item.firstName);
 		rowView.setMinimumHeight(96);
 		return rowView;
 	}
 
 	protected static class ViewHolder {
 		protected ImageView pic;
 		protected TextView name;
 	}
 }

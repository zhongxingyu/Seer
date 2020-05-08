 package com.parse.starter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.parse.ParseObject;
 
 import android.app.Activity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 // adapter for records listview
 public class ListAdapter extends ArrayAdapter<ParseObject> {
 	List<ParseObject> classes;
 	
 	public ListAdapter(Activity activity, List<ParseObject> classes) {
 		super(activity, R.layout.list_item, R.id.classname, classes);
 		this.classes = classes;
 	}
 	
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view = super.getView(position, convertView, parent);
 		
 		//figure out how the courses will be parsed after json and edit accordingly
 		//eg:
 		String name = classes.get(position).getString("field") + classes.get(position).getString("number");
 		((TextView) view.findViewById(R.id.classname)).setText(name);
 		String room = classes.get(position).getString("building") + " " + classes.get(position).getString("room");
 		((TextView) view.findViewById(R.id.room)).setText(room);
 		String time = classes.get(position).getString("meetings");
		time = time.replaceAll("[a-z]|\\.|\\,", "");	
 		((TextView) view.findViewById(R.id.time)).setText(time);
 
 		return view;
 	}
 }

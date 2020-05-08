 package com.parse.starter;
 
 import java.util.ArrayList;
 
 import com.parse.ParseObject;
 
 import android.app.Activity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 // adapter for records listview
 public class ListAdapter extends ArrayAdapter<ParseObject> {
	ArrayList<ParseObject> classes;
 	
	public ListAdapter(Activity activity, ArrayList<ParseObject> classes) {
 		super(activity, R.layout.list_item, R.id.classname, classes);
 		this.classes = classes;
 	}
 	
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view = super.getView(position, convertView, parent);
 		
 		//figure out how the courses will be parsed after json and edit accordingly
 		//eg:
 		
 		/*((TextView) view.findViewById(R.id.classname)).setText(classes
 				.get(position).getName()));
 		((TextView) view.findViewById(R.id.room)).setText(classes.get(position)
 				.getRoom());
 		((TextView) view.findViewById(R.id.time)).setText(classes.get(position)
 				.getTime());*/
 
 		return view;
 	}
 }

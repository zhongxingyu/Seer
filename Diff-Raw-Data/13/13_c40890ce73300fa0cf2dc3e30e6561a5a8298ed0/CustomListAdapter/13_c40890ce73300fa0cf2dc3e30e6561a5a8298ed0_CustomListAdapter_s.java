 package edu.pitt.cs1635group3;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class CustomListAdapter extends ArrayAdapter<CustomList> {
 	// This pulls in the user's lists and inflates them appropriately for the
 	// layout.
 
 	private final ArrayList<CustomList> lists;
 
 	public CustomListAdapter(Context context, int textViewResourceId,
 			ArrayList<CustomList> lists) {
 		super(context, textViewResourceId, lists);
 		this.lists = lists;
 	}
 
 	@Override
 	public View getView(final int position, View convertView, ViewGroup parent) {
 		View v = convertView;
 
 		if (v == null) {
 			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
 					Context.LAYOUT_INFLATER_SERVICE);
 			v = vi.inflate(R.layout.list_row, null);
 		}
 		CustomList o = (CustomList) lists.get(position);
 		if (o != null) {
 			TextView name = (TextView) v.findViewById(R.id.element_title);
 			TextView note = (TextView) v.findViewById(R.id.element_subtitle);
 			if (name != null)
 				name.setText(""+o.getName());
 			if (note != null) {
 				String listNote = o.getNote();
				if (listNote == null)
 					note.setText("");
 				else
					note.setText(""+listNote);
 			}
 		}
 
 		return v;
 	}
 }

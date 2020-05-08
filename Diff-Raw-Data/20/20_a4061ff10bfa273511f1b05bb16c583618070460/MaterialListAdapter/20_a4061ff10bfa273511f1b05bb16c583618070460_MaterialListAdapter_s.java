 package com.cs317m.austinrecycle;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class MaterialListAdapter extends ArrayAdapter<MaterialItem> {
 	private static final String TAG = "MaterialListAdapter.java";
 	
 	private Context _context;
 	private int _layoutResourceId;
 	private ArrayList<MaterialItem> _itemArray = null;
 	private ImageView _icon;
 	private TextView _name;
 //	private CheckBox _checkbox;
 	
 	public MaterialListAdapter(Context context, int layoutResourceId, ArrayList<MaterialItem> itemArray) {
 		super(context, layoutResourceId, itemArray);
 		_context = context;
 		_layoutResourceId = layoutResourceId;
 		_itemArray = itemArray;
 	}
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d(TAG, "in getView");
 		if(convertView == null) {
 			LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			convertView = inflater.inflate(_layoutResourceId, parent, false);
 		}
 		_icon = (ImageView) convertView.findViewById(R.id.material_icon);
 		_name = (TextView) convertView.findViewById(R.id.material_name);
 //		_checkbox = (CheckBox) convertView.findViewById(R.id.material_checkbox);
 		
 		MaterialItem data = _itemArray.get(position);
 		
 		_icon.setImageResource(data.getIcon());
 		Log.d(TAG, "icon: " + data.getIcon());
 		_name.setText(data.getName());
 		Log.d(TAG, "name: " + data.getName());
 //		_checkbox.setChecked(data.getChecked());
 //		Log.d(TAG, "checked: " + data.getChecked());
 		
 		return convertView;
 	}
 }

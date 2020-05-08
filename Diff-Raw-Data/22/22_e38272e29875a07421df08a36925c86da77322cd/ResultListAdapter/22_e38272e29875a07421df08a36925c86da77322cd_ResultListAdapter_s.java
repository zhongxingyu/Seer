 package com.cs371m.austinrecycle;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ResultListAdapter extends ArrayAdapter<FacilityItem> {
 	private static final String TAG = "ResultListAdapter.java";
 	
 	private Context _context;
 	private int _layoutResourceId;
 	private ArrayList<FacilityItem> _item;
 	private HashMap<String, Integer> _materialIcons; // To lookup material icon IDs from strings
 	private FacilityItem _data;
 	
 	// For creating efficient references to View objects - ViewHolder pattern
 	// Goal: Reduce calls to findViewById()
 	static class ViewHolder {
 		public TextView _facility_name;
 		public TextView _facility_dist;
 		public TextView _facility_address;
 		public ImageView _icon;
 	}
 	
 	public ResultListAdapter(Context context, int layoutResourceId, ArrayList<FacilityItem> item) {
 		super(context, layoutResourceId, item);
 		_context = context;
 		_layoutResourceId = layoutResourceId;
 		_item = item;
 		
 		_materialIcons = new HashMap<String, Integer>();
 		_materialIcons.put("oil", R.id.oil_icon);
 		_materialIcons.put("oil_filter", R.id.oil_filter_icon);
 		_materialIcons.put("fluids", R.id.fluids_icon);
 		_materialIcons.put("tires", R.id.tires_icon);
 		_materialIcons.put("batteries", R.id.batteries_icon);
 		_materialIcons.put("newspapers", R.id.newspapers_icon);
 		_materialIcons.put("scrap_metal", R.id.scrap_metal_icon);
 		_materialIcons.put("aluminum", R.id.aluminum_icon);
 		
 		// Make sure the object is passed correctly
 //		Log.d(TAG, "BEGIN PRINTING _facilitiesItem");
 //    	for(FacilityItem x : _item) {
 //    		Log.d(TAG, x.getName());
 //    	}
 //    	Log.d(TAG, "END PRINTING _facilitiesItem");
 	}
 	
 	// Need to override getView to display something more complicated than a TextView
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		Log.d(TAG, "in getView");
 		// Re-populate already-allocated views if possible for efficiency
 		View rowView = convertView;
 		// If no View obj to reuse, allocate one and assign it ViewHolder reference
 		if(rowView == null) {
 			LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			rowView = inflater.inflate(_layoutResourceId, parent, false);
 			
 			ViewHolder viewHolder = new ViewHolder();
 			viewHolder._facility_name = (TextView) rowView.findViewById(R.id.facility_name);
 			viewHolder._facility_dist = (TextView) rowView.findViewById(R.id.facility_dist);
 			viewHolder._facility_address = (TextView) rowView.findViewById(R.id.facility_address);
 			rowView.setTag(viewHolder);
 		}
 		
 		// Get data to populate the View object
 		ViewHolder holder = (ViewHolder) rowView.getTag();
 		_data = _item.get(position);
 		String addr_human = _data.getAddrHuman();
 		ArrayList<String> accepts = _data.getAccepts();
 		
 		// Re-format the address string into something prettier
 		try {
 			JSONObject address = new JSONObject(addr_human);
 			String addr = address.getString("address");
 	        String city = address.getString("city");
 	        String state = address.getString("state");
 	        String zip = address.getString("zip");
 
 			String textToDisplay = addr + ", " + city + ", " + state + ", " + zip + ".";
 			
			holder._facility_name.setText((position+1) + ". " + _data.getName());
 			
 			float dist_miles = (float)_data.getDistance()/(float)1609.34;
			String miles = String.format("%.2f", dist_miles);
 			holder._facility_dist.setText(miles + " mi");
 			holder._facility_address.setText(textToDisplay);
 		}
 		catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
 		// Add appropriate material ImageViews to this facility's view
 		for(String iconKey : accepts) {
 			int iconId = _materialIcons.get(iconKey);
 			if(_materialIcons.containsKey(iconKey)) {
 				holder._icon = (ImageView) rowView.findViewById(iconId);
 				holder._icon.setVisibility(View.VISIBLE);
 			}
 		}
 		
 		return rowView;
 	}
 }

 
 package ch.bfh.evoting.votinglib.adapters;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import ch.bfh.evoting.votinglib.R;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * Implements an ArrayAdapter which maps the values of the Hashmaps stored in
  * the ArrayList to the views in the layout displaying all the available WLAN
  * networks
  * 
  * @author Juerg Ritter (rittj1@bfh.ch)
  */
 public class NetworkArrayAdapter extends ArrayAdapter<HashMap<String, Object>> {
 
 	private ArrayList<HashMap<String, Object>> items;
 	private Context context;
 	private String capabilities;
 	
 	private boolean hideCreateNetwork = false;
 
 	
 	/**
 	 * @param context
 	 *            The context from which it has been created
 	 * @param textViewResourceId
 	 *            The id of the item layout
 	 * @param items
 	 *            The ArrayList with the values
 	 */
 	public NetworkArrayAdapter(Context context, int textViewResourceId,
 			ArrayList<HashMap<String, Object>> items) {
 		super(context, textViewResourceId, items);
 		this.context = context;
 		this.items = items;
 	}
 	
 	
 	/**
 	 * @param context
 	 *            The context from which it has been created
 	 * @param textViewResourceId
 	 *            The id of the item layout
 	 * @param items
 	 *            The ArrayList with the values
 	 * @param hideCreateNetwork
 	 * 			  Indicates whether the the last item should have a router icon or not, default is false
 	 */
 	public NetworkArrayAdapter(Context context, int textViewResourceId,
 			ArrayList<HashMap<String, Object>> items, boolean hideCreateNetwork) {
 		super(context, textViewResourceId, items);
 		this.context = context;
 		this.items = items;
 		
 		this.hideCreateNetwork = hideCreateNetwork;
 	}
 	
 	
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
 	 * android.view.ViewGroup)
 	 */
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view = convertView;
 		if (view == null) {
 			LayoutInflater inflater = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			view = inflater.inflate(R.layout.list_item_network, null);
 		}
 
 		// extract the Hashmap at the according postition of the ArrayList
 		HashMap<String, Object> item = items.get(position);
 		if (item != null) {
 			// extract the views in the layout
 			TextView content = (TextView) view.findViewById(R.id.textview_content);
 			TextView description = (TextView) view
 					.findViewById(R.id.textview_description);
 			ImageView icon = (ImageView) view.findViewById(R.id.imageview_icon);
 
 			if (this.getCount() - 1 == position && !hideCreateNetwork) {
 				// the last item is for the "Create Network" item, so the icon
 				// needs to be the router if we don't hide the createnetwork option
 				icon.setImageResource(R.drawable.glyphicons_046_router);
 				icon.setBackgroundColor(context.getResources().getColor(
 						android.R.color.holo_purple));
 				description.setText("");
 			} else {
 				// set the values of the labels accordingly
 				icon.setImageResource(R.drawable.glyphicons_032_wifi_alt);
 				icon.setBackgroundColor(context.getResources().getColor(
 						android.R.color.holo_blue_light));
 				capabilities = (String) item.get("capabilities");
 				if (capabilities == null) {
 					description.setText("");
 				} else if (capabilities.contains("WPA")) {
 					description.setText("WPA secured network");
 				} else if (capabilities.contains("WEP")) {
 					description.setText("WEP secured network");
 				} else {
 					description.setText("unsecure open network");
 				}
 			}
 			content.setText((String) item.get("SSID"));
 
 		}
 		return view;
 	}
 }

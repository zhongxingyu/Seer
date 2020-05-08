 package no.group09.arduinoair;
 
 import android.content.Context;
import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 
 public class Categories extends List  implements OnItemLongClickListener, OnItemClickListener{
 
 	private final String TAG = "Categories";
 
 	protected SharedPreferences prefs = null;
 	private static LayoutInflater mInflater;
 	
 	//Adapter for the lists
 	private ListAdapter adapter;
 	
 	
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 
 		Log.d(TAG, "onItemclick");
 	}
 
 	@Override
 	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		
 		Log.d(TAG, "onItemLongClick");
 		
 		return false;
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
 		requestWindowFeature(Window.FEATURE_PROGRESS);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.list_element);
 		
 		mInflater = LayoutInflater.from(ctxt);
 		
 		//List of discovered hosts (the adapter)
 		adapter = new ListAdapter(ctxt);
 		
 		//Find the list from the xml
 		ListView list = (ListView) findViewById(R.id.output);
 
 		//Set the custom made adapter to the list
 		list.setAdapter(adapter);
 		list.setItemsCanFocus(false);
 
 		//If the element in the discovery list is long pressed
 		list.setOnItemLongClickListener(this);
 
 		//If the element in the discovery list is short pressed (normal press)
 		list.setOnItemClickListener(this);
 	}
 	
 	   /**
   * A helpclass to the HostAdapter
   */
  static class ViewHolder {
 		TextView host;
 		TextView mac;
 		TextView vendor;
 		ImageView logo;
 	}
  
  
 	/** A custom made ArrayAdapter to make the elements in the list look like we want */
 	private class ListAdapter extends ArrayAdapter<Void> {
 		public ListAdapter(Context ctxt) {
 			super(ctxt, R.layout.list, R.id.list);
 		}
 
 		@Override
 		public View getView(final int position, View convertView, ViewGroup parent) {
 			ViewHolder holder;
 			if (convertView == null) {
 				convertView = mInflater.inflate(R.layout.list, null);
 				holder = new ViewHolder();
 				holder.host = (TextView) convertView.findViewById(R.id.list);
 				holder.mac = (TextView) convertView.findViewById(R.id.mac);
 				holder.vendor = (TextView) convertView.findViewById(R.id.vendor);
 				holder.logo = (ImageView) convertView.findViewById(R.id.logo);
 				convertView.setTag(holder);
 			} else {
 				holder = (ViewHolder) convertView.getTag();
 			}
 //			final HostBean host = hosts.get(position);
 
 			holder.logo.setImageResource(R.drawable.computer);
 
 			holder.host.setText("hahahahaha");
 			
 			holder.vendor.setText("hehehehehe");
 			
 			holder.mac.setText("hohohohoho");
 			
 //			if (!host.hardwareAddress.equals(NetInfo.NOMAC)) {
 //				holder.mac.setText(host.hardwareAddress);
 //				if(host.nicVendor != null){
 //					holder.vendor.setText(host.nicVendor);
 //				} else {
 //					holder.vendor.setText("Tap to connect");
 //				}
 //				holder.mac.setVisibility(View.VISIBLE);
 //				holder.vendor.setVisibility(View.VISIBLE);
 //			} else {
 //				holder.mac.setVisibility(View.GONE);
 //				holder.vendor.setVisibility(View.GONE);
 //			}
 			return convertView;
 		}
 	}
 }

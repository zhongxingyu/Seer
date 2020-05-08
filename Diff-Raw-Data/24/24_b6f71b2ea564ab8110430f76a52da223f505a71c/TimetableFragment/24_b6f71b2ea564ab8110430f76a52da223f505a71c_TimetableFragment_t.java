 package se.davor.bussar;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class TimetableFragment extends Fragment {
 	public static final String TRIP_ID = "se.davor.bussar.trip_id";
 	public static final String TRIP_NAME = "se.davor.bussar.trip_name";
 
 	private ArrayAdapter<String> aa;
 	private Station station;
 	
     public TimetableFragment(Station station) {
     	this.station = station;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
     	ConnectivityManager connMgr = 
 				(ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		TextView textView = new TextView(getActivity());    	
     	ListView listView = new ListView(getActivity());
     	ArrayList<String> list = new ArrayList<String>();
     	
 		aa = new ArrayAdapter<String>(getActivity(), 
     			android.R.layout.simple_list_item_1, list);
 		
		if (networkInfo == null || !networkInfo.isConnected()) {
			Log.e("Network", "No network connection");
			textView = new TextView(getActivity());
			textView.setText(getResources().getString(R.string.error_no_network));
			return textView;
		}    
     	try {
     		station.getTimes(this);
 		} catch (IOException e) {
 			Log.e("Network", "Connection error.");
 			e.printStackTrace();
			textView.setText(getResources().getString(R.string.error_connection));
 			return textView;
		} catch (Exception e) {
			Log.e("Network", "Error getting times.");
			e.printStackTrace();
			textView.setText(getResources().getString(R.string.error_times));
 		}
     	
     	listView.setAdapter(aa);
         return listView;
     }
 
 	public void add_time(TimeEntry entry) {
 		aa.add(entry.toString());
 	}
 }

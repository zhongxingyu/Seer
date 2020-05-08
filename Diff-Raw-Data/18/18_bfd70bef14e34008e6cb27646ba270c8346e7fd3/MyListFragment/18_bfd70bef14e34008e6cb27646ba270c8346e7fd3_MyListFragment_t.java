 package ua.newproject.ui;
 
 import ua.newproject.R;
 import ua.newproject.Util;
 import android.app.Activity;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.slidingmenu.lib.SlidingMenu;
 
 public class MyListFragment extends Fragment implements OnItemClickListener {
 
 	final String	LOG_TAG	= getClass().getSimpleName();
 	SlidingMenu		slidingMenu;
 	Context context;
 
 	@Override
	public void onCreate(Bundle savedInstanceState) {
 		context = getActivity();
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.list, null);
 		if (!isOnline()) {
 			Log.d(LOG_TAG, getString(R.string.check_network_connection));
 			getToast();
 			return view;
 		}
 		slidingMenu = new SlidingMenu(getActivity());
 		ListView listView = (ListView) view.findViewById(R.id.listView);
 		ItemListAdapter itemListAdapter = new ItemListAdapter(getActivity(),
 				new Util().getItemModelArray());
 		listView.setAdapter(itemListAdapter);
 		listView.setOnItemClickListener(this);
 		return view;
 	}
 
 	public void getToast() {
 		Toast.makeText(context, getString(R.string.check_network_connection),
 				Toast.LENGTH_LONG).show();
 	}
 
 	public boolean isOnline() {
 		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
 				Context.CONNECTIVITY_SERVICE);
 
 		return cm.getActiveNetworkInfo() != null
 				&& cm.getActiveNetworkInfo().isConnectedOrConnecting();
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
 		setDataListener.setData(position + 1);
 	}
 
 	public interface setDataListener {
 		public void setData(Integer value);
 	}
 
 	setDataListener	setDataListener;
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		try {
 			setDataListener = (setDataListener) activity;
 		} catch (ClassCastException e) {
 			throw new ClassCastException(activity.toString() + " must implement setDataListener");
 		}
 	}
 
 }

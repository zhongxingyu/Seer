 package com.station.taxi.ui;
 
 import java.util.List;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
import android.util.Log;
 import android.widget.ArrayAdapter;
 
 import com.station.taxi.sockets.SocketClientLoader;
 
 
 /**
  * A fragment representing a section of the app.
  */
 public class ItemsListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<String>> {
     private static final String ARG_SERVER_IP = "server_ip";
     private static final String ARG_TYPE = "type";
 
 	/**
      * Create a new instance of FindServerDialog
      */
     static ItemsListFragment newInstance(String ip, int i) {
     	ItemsListFragment fragment = new ItemsListFragment();
         Bundle args = new Bundle();
         args.putString(ARG_SERVER_IP,ip);
         args.putInt(ARG_TYPE,i);
         fragment.setArguments(args);
         return fragment;
     }
     
 	// This is the Adapter being used to display the list's data.
 	ArrayAdapter<String> mAdapter;
 	private Context mContext;
 	private String mServerIp;
 	private int mType;
 
 	public ItemsListFragment() {
     }
 
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		mServerIp = getArguments().getString(ARG_SERVER_IP);
 		mType = getArguments().getInt(ARG_TYPE);
 		
     	mContext = getActivity();
     	// Give some text to display if there is no data.  In a real
         // application this would come from a resource.
         setEmptyText("No items");
 
         // We have a menu item to show in action bar.
         setHasOptionsMenu(true);
 
         // Create an empty adapter we will use to display the loaded data.
     	mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
 
         setListAdapter(mAdapter);
 
         // Start out with a progress indicator.
         setListShown(false);
 
         // Prepare the loader.  Either re-connect with an existing one,
         // or start a new one.
         getLoaderManager().initLoader(0, null, this).forceLoad();
 	}
 
 	public void  setServerIp(String serverIp) {
 		mServerIp = serverIp; 
 	}
 	/**
 	 * Reload data in the fragment
 	 */
 	public void reload() {
 		getLoaderManager().initLoader(0, null, this).forceLoad();
 	}
 	
 	public void onLoadFinished(Loader<List<String>> loader, List<String> items) {
         // Swap the new cursor in.  (The framework will take care of closing the
         // old cursor once we return.)
 		mAdapter.clear();
         mAdapter.addAll(items);
 
         // The list should now be shown.
         if (isResumed()) {
             setListShown(true);
         } else {
             setListShownNoAnimation(true);
         }			
 	}
 
 	public void onLoaderReset(Loader<List<String>> loader) {
         mAdapter.clear();
 	}
 
 	public Loader<List<String>> onCreateLoader(int id, Bundle args) {
    	Log.d("test22", "Ttem::CreateLoader::type #"+mType);

 		return new SocketClientLoader(mServerIp, mType, mContext);
 	}
 
 }

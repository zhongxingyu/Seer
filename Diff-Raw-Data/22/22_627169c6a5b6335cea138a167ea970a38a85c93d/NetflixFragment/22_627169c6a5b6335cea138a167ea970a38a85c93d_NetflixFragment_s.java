 package fr.eyal.datalib.sample.netflix.fragment;
 
 import java.util.ArrayList;
 
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import fr.eyal.datalib.sample.netflix.data.service.NetflixDataManager;
 import fr.eyal.lib.data.service.DataManager.OnDataListener;
 
 public abstract class NetflixFragment extends Fragment implements OnDataListener {
 
 	/**
 	 * NetflixDataManager of the project
 	 */
 	protected NetflixDataManager mDataManager;
 
 	/**
 	 * List of requests currently running for this activity
 	 */
	protected ArrayList<Integer> mRequestIds;
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		
		mRequestIds = new ArrayList<Integer>();
 		mDataManager = NetflixDataManager.getInstance(getActivity());
 
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public void onResume() {
 		if(mRequestIds != null && mDataManager != null){
 			// we launch the request's data reception
 			synchronized (mRequestIds) {
				for (final int requestId : mRequestIds) {
					mDataManager.addOnDataListener(requestId, this);
 				}
 			}
 		}
 		super.onResume();
 	}
 	
 	@Override
 	public void onPause() {
 		if(mRequestIds != null && mDataManager != null){
 			// we stop the request's data reception
 			synchronized (mRequestIds) {
				for (final int requestId : mRequestIds) {
					mDataManager.removeOnDataListener(requestId, this);
 				}
 			}
 		}
 		super.onPause();
 	}
 }

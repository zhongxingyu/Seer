 package fr.eyal.datalib.sample.netflix.fragment;
 
 import java.util.ArrayList;
 
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import fr.eyal.datalib.sample.netflix.data.service.NetflixDataManager;
 import fr.eyal.lib.data.service.DataManager.OnDataListener;
import fr.eyal.lib.util.Out;
 
 public abstract class NetflixFragment extends Fragment implements OnDataListener {
 
 	/**
 	 * NetflixDataManager of the project
 	 */
 	protected NetflixDataManager mDataManager;
 
 	/**
 	 * List of requests currently running for this activity
 	 */
	final protected ArrayList<Integer> mRequestIds = new ArrayList<Integer>();
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		
 		mDataManager = NetflixDataManager.getInstance(getActivity());
 
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public void onResume() {
 		if(mRequestIds != null && mDataManager != null){
 			// we launch the request's data reception
 			synchronized (mRequestIds) {
				for (final Integer requestId : mRequestIds) {
					if(requestId != null) 
						mDataManager.addOnDataListener(requestId, this);
					else
						Out.e("", "REQUEST ID NULL !!!!!!!!!!!!!");
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
				for (final Integer requestId : mRequestIds) {
					if(requestId != null)
						mDataManager.removeOnDataListener(requestId, this);
					else
						Out.e("", "REQUEST ID NULL !!!!!!!!!!!!!");
 				}
 			}
 		}
 		super.onPause();
 	}
 }

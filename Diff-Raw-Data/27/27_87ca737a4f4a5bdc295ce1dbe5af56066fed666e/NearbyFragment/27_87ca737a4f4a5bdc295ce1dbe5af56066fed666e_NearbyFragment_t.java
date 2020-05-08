 package edu.uiuc.whosinline.fragments;
 
 import edu.uiuc.whosinline.R;
import android.app.ListFragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
public class NearbyFragment extends ListFragment {
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		
 		// Inflate the layout for this fragment.
 		return inflater.inflate(R.layout.fragment_nearby, container, false);
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 }

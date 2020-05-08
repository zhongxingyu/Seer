 package com.smartpark.fragments;
 
 import java.lang.ref.Reference;
 
 import android.app.Activity;
 import android.app.Application.ActivityLifecycleCallbacks;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import com.smartpark.R;
 import com.smartpark.Ref;
 
 /**
  * SmartParkFragment, this holds the general page of our application
  * @author commander
  *
  */
 public class SmartParkFragment extends Fragment  {
 	
 	// Debug Information, the boolean should be stored in a common settings file in the future!
	private static boolean D = Ref.D;
 	private static final String TAG = "SmartParkFragment";
 	
 	// This needs some fixing for code cleanup
 	public static final String ARG_SECTION_NUMBER = "section_number";
 
 	public SmartParkFragment() {
 		if (D)
 			Log.d(TAG, "Fragment: " + this.toString() + " Loaded");
 	}
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.fragment_sp_view,
 				container, false);
 		TextView dummyTextView = (TextView) rootView
 				.findViewById(R.id.section_label);
 		dummyTextView.setText(Integer.toString(getArguments().getInt(
 				ARG_SECTION_NUMBER)));
 		return rootView;
 	}
 
 	@Override
 	public void onDestroy(){
 		super.onDestroy();
 		Log.d(TAG, " -- DestroyFragment");
 		Ref.backgroundThread.b = false;
 
 	}
 	
 	
 	
 }

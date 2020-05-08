 package com.smartpark.fragments;
 
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
  * GPSFragment, this holds the GPS side of the project and will show
  * GPS specific information
  * @author commander
  *
  */
 public class GPSFragment extends Fragment {
 
 	// Debug Information, the boolean should be stored in a common settings file in the future!
	private static boolean D = Ref.D;
 	private static final String TAG = "GPSFragment";
 
 	// This needs some fixing for code cleanup
 	public static final String ARG_SECTION_NUMBER = "section_number";
 
 	public GPSFragment() {
 		if (D)
 			Log.d(TAG, "Fragment: " + this.toString() + " Loaded");
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.fragment_gps_view,
 				container, false);
 		TextView dummyTextView = (TextView) rootView
 				.findViewById(R.id.section_label);
 		dummyTextView.setText(Integer.toString(getArguments().getInt(
 				ARG_SECTION_NUMBER)));
 		return rootView;
 	}
 }

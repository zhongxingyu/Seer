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
  * DummySectionFragment, this is the playground for testing things out before
  * making changes to known fragments.
  * 
  * @author commander
  * 
  */
 public class DummySectionFragment extends Fragment {
 
 	// Debug Information, the boolean should be stored in a common settings file
 	// in the future!
	private static boolean D = Ref.d;
 	private static final String TAG = "DummySectionFragment";
 
 	// This needs some fixing for code cleanup
 	public static final String ARG_SECTION_NUMBER = "section_number";
 
 	public DummySectionFragment() {
 		if (D)
 			Log.d(TAG, "Fragment: " + this.toString() + " Loaded");
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.fragment_main_dummy,
 				container, false);
 		TextView dummyTextView = (TextView) rootView
 				.findViewById(R.id.section_label);
 		dummyTextView.setText(Integer.toString(getArguments().getInt(
 				ARG_SECTION_NUMBER)));
 		return rootView;
 	}
 }

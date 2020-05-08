 package de.gymbuetz.gsgbapp;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 public class AboutFragment extends Fragment {
 	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_about, container, false);

		TextView tv = (TextView) rootView.findViewById(R.id.text_app_version);
 		try {
			tv.setText(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return rootView;
 	}
 }

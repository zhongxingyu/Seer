 package com.anderspersson.xbmcwidget.configuration;
 
 import com.anderspersson.xbmcwidget.R;
 
 import android.app.Fragment;
 import android.os.Bundle;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 public class TroubleshootingPreferenceFragment extends Fragment {	
 	 
 	@Override
 	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View v =  inflater.inflate(R.layout.troubleshooting, container, false);
 		
 		TextView message = (TextView)v.findViewById(R.id.troubleshooting_message);
 		message.setText(Html.fromHtml(getString(R.string.message_troubleshooting)));
		
 		return v;
 	}
 }
 

 package com.frca.purtges.fragments;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import com.frca.purtges.R;
 
 /**
  * Created by Frca on 28.7.13.
  */
 public class FragmentLog extends Fragment {
 
     private TextView mStatus = null;
     private TextView mLog = null;
 
    private String log = "";
    private String currentStatus;
     public FragmentLog() {
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         View rootView = inflater.inflate(R.layout.fragment_log, container, false);
 
         mStatus = (TextView) rootView.findViewById(R.id.text);
        mStatus.setText(currentStatus);
 
         mLog = (TextView) rootView.findViewById(R.id.text_content);
        mLog.setText(log);
 
         return rootView;
     }
 
     public void appendText(final String text) {
        log += "\n --" + text;
        currentStatus = text;
         getActivity().runOnUiThread(new Runnable() {
             @Override
             public void run() {
                mLog.setText(log);
                mStatus.setText(currentStatus);
             }
         });
     }
 }
 

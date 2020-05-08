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
    public static final String ARG_SECTION_NUMBER = "section_number";
 
     private TextView mStatus = null;
     private TextView mLog = null;
 
     public FragmentLog() {
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         View rootView = inflater.inflate(R.layout.fragment_log, container, false);
 
         mStatus = (TextView) rootView.findViewById(R.id.text);
 
         mLog = (TextView) rootView.findViewById(R.id.text_content);
 
         return rootView;
     }
 
     public void appendText(final String text) {
         getActivity().runOnUiThread(new Runnable() {
             @Override
             public void run() {
                mLog.setText(mLog.getText().toString() + "\n --" + text);
                mStatus.setText(text);
             }
         });
     }
 }
 

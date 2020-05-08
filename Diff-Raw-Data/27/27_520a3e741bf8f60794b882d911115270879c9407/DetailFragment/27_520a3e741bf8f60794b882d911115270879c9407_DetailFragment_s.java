 
 package com.fanoyong.aboutphone.ui;
 
import com.fanoyong.aboutphone.R;

 import android.app.Activity;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class DetailFragment extends Fragment {
     private static final String TAG = "AP|DetailFragment|";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.v(TAG + "onCreate|", "start");
         Log.v(TAG + "onCreate|", "end");
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         Log.v(TAG + "onCreateView|", "start");
         Log.v(TAG + "onCreateView|", "end");
         View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
         return rootView;
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         Log.v(TAG + "onAttach|", "start");
         Log.v(TAG + "onAttach|", "end");
     }
 
     @Override
     public void onDetach() {
         super.onDetach();
         Log.v(TAG + "onDetach|", "start");
         Log.v(TAG + "onDetach|", "end");
     }
 
 }

 package com.tronix.tickspot.ui;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBarActivity;
 import android.view.*;
 import com.tronix.tickspot.R;
 
 public class TimersFragment extends Fragment {
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         setHasOptionsMenu(true);
     }
 
     @Override
    public void onStart() {
        super.onStart();
        ActionBarActivity activity = (ActionBarActivity)getActivity();
        activity.getSupportActionBar().setTitle("Timers");
    }
    @Override
     public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
         return layoutInflater.inflate(R.layout.fragment_timers, container, false);
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
         menuInflater.inflate(R.menu.timers_fragment_menu, menu);
     }
 }
